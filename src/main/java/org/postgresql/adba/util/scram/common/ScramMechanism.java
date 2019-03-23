/*
 * Copyright 2017, OnGres.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */


package org.postgresql.adba.util.scram.common;


import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;


/**
 * Definition of the functionality to be provided by every ScramMechanism.
 *
 * <p>Every ScramMechanism implemented must provide implementations of their respective {@link MessageDigest} and {@link Mac} that
 * will not throw a RuntimeException on any JVM, to guarantee true portability of this library.</p>
 */
public interface ScramMechanism {

  /**
   * The name of the mechanism, which must be a value registered under IANA:
   * <a href="https://www.iana.org/assignments/sasl-mechanisms/sasl-mechanisms.xhtml#scram">
   * SASL SCRAM Family Mechanisms</a>.
   *
   * @return The mechanism name
   */
  String getName();

  /**
   * Gets a constructed {@link MessageDigest} instance, according to the algorithm of the SCRAM mechanism.
   *
   * @return The MessageDigest instance
   * @throws RuntimeException If the MessageDigest instance of the algorithm is not provided by current JVM
   */
  MessageDigest getMessageDigestInstance() throws RuntimeException;

  /**
   * Gets a constructed {@link Mac} instance, according to the algorithm of the SCRAM mechanism.
   *
   * @return The Mac instance
   * @throws RuntimeException If the Mac instance of the algorithm is not provided by current JVM
   */
  Mac getMacInstance() throws RuntimeException;

  /**
   * Generates a key of the algorith used, based on the key given.
   *
   * @param key The bytes of the key to use
   * @return The instance of SecretKeySpec
   */
  SecretKeySpec secretKeySpec(byte[] key);

  /**
   * Gets a SecretKeyFactory for the given algorithm.
   *
   * @return The SecretKeyFactory
   */
  SecretKeyFactory secretKeyFactory();

  /**
   * Returns the length of the key length  of the algorithm.
   *
   * @return The length (in bits)
   */
  int algorithmKeyLength();

  /**
   * Whether this mechanism supports channel binding.
   *
   * @return True if it supports channel binding, false otherwise
   */
  boolean supportsChannelBinding();
}
