package org.postgresql.sql2.communication;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;

import org.postgresql.sql2.buffer.PooledByteBuffer;
import org.postgresql.sql2.communication.network.NetworkConnectRequest;

/**
 * Network {@link InputStream}.
 * 
 * @author Daniel Sagenschneider
 */
public class NetworkInputStream extends InputStream {

  /**
   * Maximum length of bytes (and by this the maximum number of characters).
   */
  private static int maxStringLength = 4096;

  /**
   * Specifies the maximum byte length of any string.
   * 
   * @param length Maximum byte length of any string.
   */
  public static void setStringLength(int length) {
    maxStringLength = length;
  }

  /**
   * {@link ThreadLocal} {@link CharBuffer} to re-use to reduce memory creation
   * (and garbage collection).
   */
  private static final ThreadLocal<ThreadLocalState> threadLocalState = new ThreadLocal<ThreadLocalState>() {
    @Override
    protected ThreadLocalState initialValue() {
      return new ThreadLocalState(maxStringLength);
    }
  };

  /**
   * {@link ThreadLocal} state.
   */
  private static class ThreadLocalState {

    /**
     * Re-useable {@link CharBuffer} for reduced memory.
     */
    private final CharBuffer charBuffer;

    /**
     * String decoder.
     */
    private final CharsetDecoder stringDecoder = Charset.forName(NetworkConnectRequest.CHARSET).newDecoder();

    /**
     * Instantiate.
     * 
     * @param charBufferLength Length of the {@link CharBuffer}.
     */
    private ThreadLocalState(int charBufferLength) {
      this.charBuffer = CharBuffer.allocate(charBufferLength);
    }
  }

  /**
   * Head {@link StreamSegment}.
   */
  private StreamSegment head = null;

  /**
   * Tail {@link StreamSegment}.
   */
  private StreamSegment tail = null;

  /**
   * Position within the head {@link StreamSegment}.
   */
  private int headPosition;

  /**
   * Number of bytes currently buffered.
   */
  private int bufferedByteCount = 0;

  /**
   * Position within the frame.
   */
  private int framePosition = 0;

  /**
   * Frame payload size.
   */
  private int framePayloadSize = -1;

  /**
   * Appends the {@link PooledByteBuffer}.
   * 
   * @param buffer {@link PooledByteBuffer}.
   */
  public void appendBuffer(PooledByteBuffer buffer, int offset, int length, boolean isRelease) {
    StreamSegment segment = new StreamSegment(buffer, offset, length, isRelease);
    this.bufferedByteCount += length;
    if (this.head == null) {
      this.head = segment;
      this.tail = segment;
    } else {
      this.tail.next = segment;
      this.tail = segment;
    }
  }

  /**
   * Specifies the bytes to end of stream.
   * 
   * @param byteCount Byte count to end of stream.
   */
  public void setBytesToEndOfStream(int byteCount) {
    this.framePayloadSize = byteCount;
    this.framePosition = 0;
  }

  /**
   * Clear frame.
   */
  public void clearFrame() throws IOException {

    // Consume remaining bytes of frame
    for (int i = this.framePosition; i < this.framePayloadSize; i++) {
      this.read(); // consume byte
    }

    // No frame active
    this.framePayloadSize = -1;
  }

  /**
   * Obtains the head {@link StreamSegment} remaining length.
   * 
   * @return Head {@link StreamSegment} remaining length.
   */
  private int getHeadRemainingLength() {
    return this.head.length - this.headPosition;
  }

  /*
   * ================== InputStream ======================
   */

  @Override
  public int read() throws IOException {
    for (;;) {

      // Determine if end of frame
      if ((this.framePayloadSize != -1) && (this.framePosition >= this.framePayloadSize)) {
        return -1; // end of frame
      }

      // Determine if end of stream
      if (this.head == null) {
        return -1;
      }

      // Obtain the next byte
      if (this.headPosition < this.head.length) {
        int index = this.head.offset + (this.headPosition++);
        this.framePosition++;
        return this.head.buffer.getByteBuffer().get(index);
      }

      // Completed the segment
      PooledByteBuffer releaseBuffer = null;
      if (this.head.isRelease) {
        releaseBuffer = this.head.buffer;
      }

      // Move to next segment for reading
      this.bufferedByteCount -= this.head.length;
      this.head = this.head.next;
      this.headPosition = 0;

      // Release buffer after move (so next not corrupted)
      if (releaseBuffer != null) {
        releaseBuffer.release();
      }
    }
  }

  @Override
  public int available() throws IOException {
    if (this.framePayloadSize == -1) {
      // Return all available
      return Math.max(0, (this.bufferedByteCount - this.headPosition));
    } else {
      // Return remaining for frame
      return (this.framePayloadSize - this.framePosition);
    }
  }

  /**
   * Reads in an {@link Integer} value.
   * 
   * @return {@link Integer} value or <code>-1</code> if end of stream.
   * @throws IOException If fails to read {@link Integer} value.
   */
  public int readInteger() throws IOException {

    // Ensure only reading within frame
    if (this.framePayloadSize == -1) {
      throw new IOException("Attempting to read integer outside frame");
    }

    // Determine if can read directly
    int segmentRemaining = this.getHeadRemainingLength();
    if (segmentRemaining >= 4) {
      int value = this.head.buffer.getByteBuffer().getInt(this.head.offset + this.headPosition);
      this.headPosition += 4;
      this.framePosition += 4;
      return value;
    }

    // Read in the integer
    int value = 0;
    for (int i = 0; i < 4; i++) {

      // Read the next byte
      int nextByte = this.read();
      if (nextByte == -1) {
        return -1; // end of stream
      }

      // Obtain the value
      value <<= 8;
      value += nextByte;
    }

    // Return the value
    return value;
  }

  /**
   * Reads in an {@link Short} value.
   * 
   * @return {@link Short} value or <code>-1</code> if end of stream.
   * @throws IOException If fails to read {@link Short} value.
   */
  public short readShort() throws IOException {

    // Ensure only reading within frame
    if (this.framePayloadSize == -1) {
      throw new IOException("Attempting to read short outside frame");
    }

    // Determine if can read directly
    int segmentRemaining = this.getHeadRemainingLength();
    if (segmentRemaining >= 2) {
      short value = this.head.buffer.getByteBuffer().getShort(this.head.offset + this.headPosition);
      this.headPosition += 2;
      this.framePosition += 2;
      return value;
    }

    // Read in the short
    short value = 0;
    for (int i = 0; i < 2; i++) {

      // Read the next byte
      int nextByte = this.read();
      if (nextByte == -1) {
        return -1; // end of stream
      }

      // Obtain the value
      value <<= 8;
      value += nextByte;
    }

    // Return the value
    return value;
  }

  /**
   * Reads in a {@link String} value.
   * 
   * @return {@link String} value or <code>null</code> if end of stream.
   * @throws IOException If fails to read {@link String} value.
   */
  public String readString() throws IOException {

    // Ensure only reading within frame
    if (this.framePayloadSize == -1) {
      throw new IOException("Attempting to read string outside frame");
    }

    // Obtain the char buffer (ready for use)
    ThreadLocalState state = threadLocalState.get();
    state.charBuffer.clear();

    // Decode the content into the char buffer
    boolean isComplete = false;
    while (!isComplete) {

      // Determine number of bytes to read
      int stringLength = this.getHeadRemainingLength();
      int frameRemaining = this.framePayloadSize - this.framePosition;
      isComplete = (frameRemaining <= stringLength);
      boolean isSegmentConsumed = true;

      // Scan through segment for terminating null
      FOUND_TERMINATOR: for (int i = this.headPosition; i < (this.head.length); i++) {
        if (this.head.buffer.getByteBuffer().get(this.head.offset + i) == 0) {
          // Terminating string
          stringLength = i - this.headPosition;
          isComplete = true;
          isSegmentConsumed = false;
          break FOUND_TERMINATOR;
        }
      }

      // Slice up buffer to content
      ByteBuffer input = this.head.buffer.getByteBuffer().duplicate();
      input.position(this.head.offset + this.headPosition);
      input.limit(input.position() + stringLength);

      // Decode the content into the char buffer
      CoderResult result = state.stringDecoder.decode(input, state.charBuffer, isComplete);
      if (result.isError()) {
        throw new IOException("Failed to read string: " + result);
      }

      // Increment positions
      stringLength++; // include terminating null byte
      this.headPosition += stringLength;
      this.framePosition += stringLength;

      // Move to next segment (if not yet complete)
      if (isSegmentConsumed) {

        // Completed the segment
        PooledByteBuffer releaseBuffer = null;
        if (this.head.isRelease) {
          releaseBuffer = this.head.buffer;
        }

        // Move to next segment for reading
        this.bufferedByteCount -= this.head.length;
        this.head = this.head.next;
        this.headPosition = 0;

        // Release buffer after move (so next not corrupted)
        if (releaseBuffer != null) {
          releaseBuffer.release();
        }
      }
    }

    // Flip to get content just decoded
    state.charBuffer.flip();

    // Return the string value
    return state.charBuffer.toString();
  }

  /**
   * Segment of the {@link ByteBuffer} for the sequence.
   */
  private class StreamSegment {

    /**
     * {@link PooledByteBuffer} for this segment.
     */
    private final PooledByteBuffer buffer;

    /**
     * Offset into the {@link ByteBuffer} for this segment.
     */
    private final int offset;

    /**
     * Length of data from the {@link ByteBuffer} for this segment.
     */
    private final int length;

    /**
     * Indicates if to release {@link ByteBuffer} once complete.
     */
    private final boolean isRelease;

    /**
     * Next {@link StreamSegment}.
     */
    private StreamSegment next = null;

    /**
     * Instantiate.
     * 
     * @param buffer {@link PooledByteBuffer} for this segment.
     * @param offset Offset into the {@link ByteBuffer} for this segment.
     * @param length Length of data from the {@link ByteBuffer} for this segment.
     */
    private StreamSegment(PooledByteBuffer buffer, int offset, int length, boolean isRelease) {
      this.buffer = buffer;
      this.offset = offset;
      this.length = length;
      this.isRelease = isRelease;
    }
  }

}