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


package org.postgresql.adba.util.scram.common.util;


import static org.postgresql.adba.util.scram.common.util.Preconditions.checkNotNull;


/**
 * Construct and write generic CharAttribute-Value pairs.
 *
 * <p>Concrete sub-classes should also provide a static parse(String) creation method.</p>
 */
public class AbstractCharAttributeValue extends AbstractStringWritable implements CharAttributeValue {

  private final CharAttribute charAttribute;
  private final String value;

  /**
   * Constructs a CharAttribute-Value pair.
   *
   * @param charAttribute the charAttribute
   * @param value the value
   * @throws IllegalArgumentException if any of the values are empty
   */
  public AbstractCharAttributeValue(CharAttribute charAttribute, String value) throws IllegalArgumentException {
    this.charAttribute = checkNotNull(charAttribute, "attribute");
    if (null != value && value.isEmpty()) {
      throw new IllegalArgumentException("Value should be either null or non-empty");
    }
    this.value = value;
  }

  @Override
  public char getChar() {
    return charAttribute.getChar();
  }

  @Override
  public String getValue() {
    return value;
  }

  @Override
  public StringBuffer writeTo(StringBuffer sb) {
    sb.append(charAttribute.getChar());

    if (null != value) {
      sb.append('=').append(value);
    }

    return sb;
  }
}
