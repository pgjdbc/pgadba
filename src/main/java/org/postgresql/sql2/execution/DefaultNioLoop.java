package org.postgresql.sql2.execution;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Default {@link NioLoop}.
 * 
 * @author Daniel Sagenschneider
 */
public class DefaultNioLoop implements NioLoop, Runnable {

  private Logger logger = Logger.getLogger(DefaultNioLoop.class.getName());

  /**
   * {@link Selector}.
   */
  private final Selector selector;

  /**
   * Indicates whether closed.
   */
  private volatile boolean isClosed = false;

  /**
   * Instantiate.
   * 
   * @throws IOException If fails to setup.
   */
  public DefaultNioLoop() {

    // Create the selector
    try {
      this.selector = Selector.open();
    } catch (IOException ex) {
      throw new IllegalStateException("Unable to open Selector", ex);
    }
  }

  /**
   * Closes the {@link NioLoop}.
   */
  public void close() {
    this.isClosed = true;
  }

  /*
   * ============== NioLoop ==============
   */

  @Override
  public NioService registerNioService(SelectableChannel channel, NioServiceFactory nioServiceFactory)
      throws IOException {
    return new NioServiceAttachment(channel, nioServiceFactory).service;
  }

  /*
   * ============== Runnable ==============
   */

  @Override
  public void run() {
    // Ensure close selector
    try {

      // Loop until closed
      while (!this.isClosed) {

        // Select keys
        try {
          this.selector.select(50);
        } catch (IOException ex) {
          // Should not occur
          logger.log(Level.SEVERE, "Selector failure", ex);
          return; // fatal error, so can not continue
        }

        // Obtain the selected keys
        Set<SelectionKey> selectedKeys = this.selector.selectedKeys();

        // Service the selected keys
        Iterator<SelectionKey> iterator = selectedKeys.iterator();
        NEXT_KEY: while (iterator.hasNext()) {
          SelectionKey selectedKey = iterator.next();
          iterator.remove();

          // Stop processing if cancelled
          if (!selectedKey.isValid()) {
            continue NEXT_KEY;
          }

          // Obtain the attached service
          NioServiceAttachment attachment = (NioServiceAttachment) selectedKey.attachment();

          // Obtain ready operations
          int readyOps = selectedKey.readyOps();

          try {

            if (attachment == null) {
              System.out.println("Attachement is null");
            }
            if (attachment.service == null) {
              System.out.println("Service is null");
            }

            // Determine if connect
            if ((readyOps & SelectionKey.OP_CONNECT) == SelectionKey.OP_CONNECT) {
              attachment.service.handleConnect();
            }

            // Determine if read content
            if ((readyOps & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
              attachment.service.handleRead();
            }

            // Determine if write content
            if ((readyOps & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE) {
              attachment.service.handleWrite();
            }

          } catch (Throwable ex) {
            try {
              // Handle the failure
              attachment.service.handleException(ex);

            } catch (Throwable failure) {
              // Can not do anymore but log the failure
              logger.log(Level.WARNING, "Failure with " + NioService.class.getSimpleName() + " " + attachment.service,
                  ex);
            }
          }
        }
      }

    } finally {
      try {
        this.selector.close();
      } catch (IOException ex) {
        logger.log(Level.INFO, "Failed to close selector", ex);
      }
    }
  }

  private class NioServiceAttachment implements NioServiceContext {

    private SelectableChannel channel;

    private final NioService service;

    private final SelectionKey selectionKey;

    private NioServiceAttachment(SelectableChannel channel, NioServiceFactory nioServiceFactory) throws IOException {
      this.channel = channel;

      // Create the service
      this.service = nioServiceFactory.createNioService(this);
      if (this.service == null) {
        throw new IllegalStateException("No " + NioService.class.getSimpleName() + " created");
      }

      // Undertake registration
      this.selectionKey = channel.register(DefaultNioLoop.this.selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ,
          this);
    }

    /*
     * ================ NioServiceContext ==================
     */

    @Override
    public SelectableChannel getChannel() {
      return this.channel;
    }

    @Override
    public void setInterestedOps(int interestedOps) throws IOException {
      this.selectionKey.interestOps(interestedOps);
    }

    @Override
    public void writeRequired() {
      if (selectionKey.isValid()) {
        this.selectionKey.interestOps(this.selectionKey.interestOps() | SelectionKey.OP_WRITE);
        DefaultNioLoop.this.selector.wakeup();
      }
    }

    @Override
    public void unregister() throws IOException {
      this.selectionKey.cancel();
    }
  }

}