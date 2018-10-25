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

import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLContext;
import java.util.Optional;

/**
 * Factory for {@link SSLContext}s, based in an optional {@link SNIServerName}. Implementations of this interface are supplied to
 * {@link ServerTlsChannel} instances, to select the correct context (and so the correct certificate) based on the server name
 * provided by the client.
 */
@FunctionalInterface
public interface SniSslContextFactory {

  /**
   * Return a proper {@link SSLContext}.
   *
   * @param sniServerName an optional {@link SNIServerName}; an empty value means that the client did not send and SNI value.
   * @return the chosen context, or an empty value, indicating that no context is supplied and the connection should be aborted.
   */
  Optional<SSLContext> getSslContext(Optional<SNIServerName> sniServerName);
}
