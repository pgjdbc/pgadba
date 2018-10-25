/*
MIT License

Copyright (c) [2015-2018] all contributors of https://github.com/marianobarrios/tls-channel, Alexander Kjäll

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

package org.postgresql.sql2.util.tlschannel;

import java.nio.channels.ByteChannel;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * This exception signals the caller that the operation cannot continue because bytesProduced need to be write to the underlying
 * {@link ByteChannel}, the channel is non-blocking and there are no buffer space available. The caller should try the operation
 * again, either with the channel in blocking mode of after ensuring that buffer space exists.
 *
 * <p>For {@link SocketChannel}s, a {@link Selector} can be used to find out when the method should be retried.</p>
 *
 * <p>Caveat: Any {@link TlsChannel} I/O method can throw this exception. In particular, <code>read</code> may want to write data.
 * This is because TLS handshakes may occur at any time (initiated by either the client or the server).</p>
 *
 * <p>This exception is akin to the SSL_ERROR_WANT_WRITE error code used by OpenSSL.</p>
 *
 * @see <a href="https://www.openssl.org/docs/man1.1.0/ssl/SSL_get_error.html">
 * OpenSSL error documentation</a>
 */

public class NeedsWriteException extends WouldBlockException {

}
