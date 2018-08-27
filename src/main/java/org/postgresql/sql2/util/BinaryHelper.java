package org.postgresql.sql2.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class BinaryHelper {
  public static int readInt(byte byte1, byte byte2, byte byte3, byte byte4) {
    return (byte1 & 0xFF) << 24 | (byte2 & 0xFF) << 16 | (byte3 & 0xFF) << 8 | (byte4 & 0xFF);
  }

  public static short readShort(byte b1, byte b2) {
    return (short) (((b1 & 255) << 8) + ((b2 & 255)));
  }

  /**
   * writes a long to a byte array in network byte order.
   * @param val long to write
   * @return the byte array, size 8
   */
  public static byte[] writeLong(long val) {
    byte[] bb = new byte[8];
    bb[0] = (byte) (val >>> 56);
    bb[1] = (byte) (val >>> 48);
    bb[2] = (byte) (val >>> 40);
    bb[3] = (byte) (val >>> 32);
    bb[4] = (byte) (val >>> 24);
    bb[5] = (byte) (val >>> 16);
    bb[6] = (byte) (val >>> 8);
    bb[7] = (byte) (val);
    return bb;
  }

  /**
   * writes an int to a byte array in network byte order.
   * @param val the int to write
   * @return the byte array, size 4
   */
  public static byte[] writeInt(int val) {
    byte[] bb = new byte[4];
    bb[0] = (byte) (val >>> 24);
    bb[1] = (byte) (val >>> 16);
    bb[2] = (byte) (val >>> 8);
    bb[3] = (byte) (val);
    return bb;
  }

  /**
   * writes a short to a byte array in network byte order.
   * @param val the short to write
   * @return the byte array, size 2
   */
  public static byte[] writeShort(short val) {
    byte[] bb = new byte[2];
    bb[0] = (byte) (val >>> 8);
    bb[1] = (byte) (val);
    return bb;
  }

  /**
   * Turn 16-byte stream into a human-readable 32-byte hex string.
   */
  public static void bytesToHex(byte[] bytes, byte[] hex, int offset) {
    final char[] lookup =
        {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    int i;
    int c;
    int j;
    int pos = offset;

    for (i = 0; i < 16; i++) {
      c = bytes[i] & 0xFF;
      j = c >> 4;
      hex[pos++] = (byte) lookup[j];
      j = (c & 0xF);
      hex[pos++] = (byte) lookup[j];
    }
  }

  /**
   * hashes the users password together with a salt, for authentication with postgresql.
   * @param user username
   * @param password password
   * @param salt salt value
   * @return hashed byte array
   */
  public static byte[] encode(byte[] user, byte[] password, byte[] salt) {
    MessageDigest md;
    byte[] tempDigest;
    byte[] passDigest;
    byte[] hexDigest = new byte[35];

    try {
      md = MessageDigest.getInstance("MD5");

      md.update(password);
      md.update(user);
      tempDigest = md.digest();

      bytesToHex(tempDigest, hexDigest, 0);
      md.update(hexDigest, 0, 32);
      md.update(salt);
      passDigest = md.digest();

      bytesToHex(passDigest, hexDigest, 3);
      hexDigest[0] = (byte) 'm';
      hexDigest[1] = (byte) 'd';
      hexDigest[2] = (byte) '5';
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("Unable to encode password with MD5", e);
    }

    return hexDigest;
  }


  /**
   * returns a subset of an array.
   * @param source source of the array
   * @param srcBegin start
   * @param srcEnd end
   * @return the new smaller array
   */
  public static byte[] subBytes(byte[] source, int srcBegin, int srcEnd) {
    byte[] destination = new byte[srcEnd - srcBegin];
    System.arraycopy(source, srcBegin, destination, 0, srcEnd - srcBegin);

    return destination;
  }

  /**
   * finds the next null byte.
   * @param bytes bytes to search in
   * @param pos start position to search from
   * @return next null byte position
   */
  public static int nextNullBytePos(byte[] bytes, int pos) {
    for (int i = pos; i < bytes.length; i++) {
      if (bytes[i] == 0) {
        return i;
      }
    }
    return bytes.length;
  }
}
