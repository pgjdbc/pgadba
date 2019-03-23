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


package org.postgresql.adba.util.scram.common.message;


import static org.postgresql.adba.util.scram.common.util.Preconditions.checkNotEmpty;
import static org.postgresql.adba.util.scram.common.util.Preconditions.checkNotNull;

import java.util.Optional;
import org.postgresql.adba.util.scram.common.ScramAttributeValue;
import org.postgresql.adba.util.scram.common.ScramAttributes;
import org.postgresql.adba.util.scram.common.ScramStringFormatting;
import org.postgresql.adba.util.scram.common.gssapi.Gs2Header;
import org.postgresql.adba.util.scram.common.util.StringWritable;
import org.postgresql.adba.util.scram.common.util.StringWritableCsv;


/**
 * Constructs and parses client-final-messages. Formal syntax is:
 *
 * {@code client-final-message-without-proof = channel-binding "," nonce ["," extensions] client-final-message =
 * client-final-message-without-proof "," proof }
 *
 * <p>Note that extensions are not supported.</p>
 *
 * @see <a href="https://tools.ietf.org/html/rfc5802#section-7">[RFC5802] Section 7</a>
 */
public class ClientFinalMessage implements StringWritable {

  private final String cbind;
  private final String nonce;
  private final byte[] proof;

  private static String generateCBind(Gs2Header gs2Header, Optional<byte[]> cbindData) {
    StringBuffer sb = new StringBuffer();
    gs2Header.writeTo(sb)
        .append(',');

    cbindData.ifPresent(
        v -> new ScramAttributeValue(
            ScramAttributes.CHANNEL_BINDING,
            ScramStringFormatting.base64Encode(cbindData.get())
        ).writeTo(sb)
    );

    return sb.toString();
  }

  /**
   * Constructus a client-final-message with the provided gs2Header (the same one used in the client-first-message), optionally
   * the channel binding data, and the nonce. This method is intended to be used by SCRAM clients, and not to be constructed
   * directly.
   *
   * @param gs2Header The GSS-API header
   * @param cbindData If using channel binding, the channel binding data
   * @param nonce The nonce
   * @param proof The bytes representing the computed client proof
   */
  public ClientFinalMessage(Gs2Header gs2Header, Optional<byte[]> cbindData, String nonce, byte[] proof) {
    this.cbind = generateCBind(
        checkNotNull(gs2Header, "gs2Header"),
        checkNotNull(cbindData, "cbindData")
    );
    this.nonce = checkNotEmpty(nonce, "nonce");
    this.proof = checkNotNull(proof, "proof");
  }

  private static StringBuffer writeToWithoutProof(StringBuffer sb, String cbind, String nonce) {
    return StringWritableCsv.writeTo(
        sb,
        new ScramAttributeValue(ScramAttributes.CHANNEL_BINDING, ScramStringFormatting.base64Encode(cbind)),
        new ScramAttributeValue(ScramAttributes.NONCE, nonce)
    );
  }

  private static StringBuffer writeToWithoutProof(
      StringBuffer sb, Gs2Header gs2Header, Optional<byte[]> cbindData, String nonce
  ) {
    return writeToWithoutProof(
        sb,
        generateCBind(
            checkNotNull(gs2Header, "gs2Header"),
            checkNotNull(cbindData, "cbindData")
        ),
        nonce
    );
  }

  /**
   * Returns a StringBuffer filled in with the formatted output of a client-first-message without the proof value. This is useful
   * for computing the auth-message, used in turn to compute the proof.
   *
   * @param gs2Header The GSS-API header
   * @param cbindData The optional channel binding data
   * @param nonce The nonce
   * @return The String representation of the part of the message that excludes the proof
   */
  public static StringBuffer writeToWithoutProof(Gs2Header gs2Header, Optional<byte[]> cbindData, String nonce) {
    return writeToWithoutProof(new StringBuffer(), gs2Header, cbindData, nonce);
  }

  @Override
  public StringBuffer writeTo(StringBuffer sb) {
    writeToWithoutProof(sb, cbind, nonce);

    return StringWritableCsv.writeTo(
        sb,
        null,   // This marks the position of writeToWithoutProof, required for the ","
        new ScramAttributeValue(ScramAttributes.CLIENT_PROOF, ScramStringFormatting.base64Encode(proof))
    );
  }

  @Override
  public String toString() {
    return writeTo(new StringBuffer()).toString();
  }
}
