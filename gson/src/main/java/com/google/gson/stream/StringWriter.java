/*
 * Copyright (C) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gson.stream;

import java.io.IOException;
import java.io.Writer;

/*
 * StringWriter based on the StringBuilder class rather then the StringBuffer.
 * 
 * @author Caleb Shingledecker
 * @since 2.8.6
 */
public class StringWriter extends Writer {

  private StringBuilder builder;
  private int maxLength;

  /**
   * Create a new string writer using the default initial string-builder size.
   */
  public StringWriter() {
    this.builder = new StringBuilder();
    maxLength = -1;
    lock = builder;
  }

  /**
   * Create a new string writer using the specified initial string-builder size.
   *
   * @param initialSize
   *          The number of <tt>char</tt> values that will fit into this buffer
   *          before it is automatically expanded
   *
   * @throws IllegalArgumentException
   *           If <tt>initialSize</tt> is negative
   */
  public StringWriter(int initialSize) {
    if (initialSize < 0) {
      throw new IllegalArgumentException("Negative buffer size");
    }
    builder = new StringBuilder(initialSize);
    maxLength = -1;
    lock = builder;
  }

  /**
   * Create a new string writer using the specified initial string-builder size.
   *
   * @param initialSize
   *          The number of <tt>char</tt> values that will fit into this buffer
   *          before it is automatically expanded
   *
   * @throws IllegalArgumentException
   *           If <tt>initialSize</tt> is negative
   */
  public StringWriter(int initialSize, int maxLength) {
    if (initialSize < 0) {
      throw new IllegalArgumentException("Negative buffer size");
    }
    builder = new StringBuilder(initialSize);
    this.maxLength = maxLength;
    lock = builder;
  }

  /**
   * Write a single character.
   */
  @Override
  public void write(int c) {
    builder.append((char) c);
  }

  /**
   * Write a portion of an array of characters.
   *
   * @param cbuf
   *          Array of characters
   * @param off
   *          Offset from which to start writing characters
   * @param len
   *          Number of characters to write
   * @throws IOException
   *           If buffer.length() exceeds maxLength.
   */
  @Override
  public void write(char cbuf[], int off, int len) throws IOException {
    if ((off < 0) || (off > cbuf.length) || (len < 0) || ((off + len) > cbuf.length)
        || ((off + len) < 0)) {
      throw new IndexOutOfBoundsException();
    } else if (len == 0) {
      return;
    }
    builder.append(cbuf, off, len);
    if (this.maxLength > 0 && this.builder.length() > this.maxLength) {
      throw new IOException("Buffer size exceeds maxLength.");
    }
  }

  /**
   * Write a string.
   */
  @Override
  public void write(String str) {
    builder.append(str);
  }

  /**
   * Write a portion of a string.
   *
   * @param str
   *          String to be written
   * @param off
   *          Offset from which to start writing characters
   * @param len
   *          Number of characters to write
   * @throws IOException
   *           If buffer.length() exceeds maxLength.
   */
  @Override
  public void write(String str, int off, int len) throws IOException {
    builder.append(str.substring(off, off + len));
    if (this.maxLength > 0 && this.builder.length() > this.maxLength) {
      throw new IOException("Buffer size exceeds maxLength.");
    }
  }

  /**
   * Appends the specified character sequence to this writer.
   *
   * <p>
   * An invocation of this method of the form <tt>out.append(csq)</tt> behaves
   * in exactly the same way as the invocation
   *
   * <pre>
   * out.write(csq.toString())
   * </pre>
   *
   * <p>
   * Depending on the specification of <tt>toString</tt> for the character
   * sequence <tt>csq</tt>, the entire sequence may not be appended. For
   * instance, invoking the <tt>toString</tt> method of a character buffer will
   * return a subsequence whose content depends upon the buffer's position and
   * limit.
   *
   * @param csq
   *          The character sequence to append. If <tt>csq</tt> is
   *          <tt>null</tt>, then the four characters <tt>"null"</tt> are
   *          appended to this writer.
   *
   * @return This writer
   * @throws IOException
   *           If buffer.length() exceeds maxLength.
   */
  @Override
  public StringWriter append(CharSequence csq) throws IOException {
    if (csq == null)
      write("null");
    else
      write(csq.toString());
    if (this.maxLength > 0 && this.builder.length() > this.maxLength) {
      throw new IOException("Buffer size exceeds maxLength.");
    }
    return this;
  }

  /**
   * Appends a subsequence of the specified character sequence to this writer.
   *
   * <p>
   * An invocation of this method of the form <tt>out.append(csq, start,
   * end)</tt> when <tt>csq</tt> is not <tt>null</tt>, behaves in exactly the
   * same way as the invocation
   *
   * <pre>
   * out.write(csq.subSequence(start, end).toString())
   * </pre>
   *
   * @param csq
   *          The character sequence from which a subsequence will be appended.
   *          If <tt>csq</tt> is <tt>null</tt>, then characters will be appended
   *          as if <tt>csq</tt> contained the four characters <tt>"null"</tt>.
   *
   * @param start
   *          The index of the first character in the subsequence
   *
   * @param end
   *          The index of the character following the last character in the
   *          subsequence
   *
   * @return This writer
   * @throws IOException
   *           If buffer.length() exceeds maxLength.
   * @throws IndexOutOfBoundsException
   *           If <tt>start</tt> or <tt>end</tt> are negative, <tt>start</tt> is
   *           greater than <tt>end</tt>, or <tt>end</tt> is greater than
   *           <tt>csq.length()</tt>
   *
   */
  @Override
  public StringWriter append(CharSequence csq, int start, int end) throws IOException {
    CharSequence cs = (csq == null ? "null" : csq);
    write(cs.subSequence(start, end).toString());
    if (this.maxLength > 0 && this.builder.length() > this.maxLength) {
      throw new IOException("Buffer size exceeds maxLength.");
    }
    return this;
  }

  /**
   * Appends the specified character to this writer.
   *
   * <p>
   * An invocation of this method of the form <tt>out.append(c)</tt> behaves in
   * exactly the same way as the invocation
   *
   * <pre>
   * out.write(c)
   * </pre>
   *
   * @param c
   *          The 16-bit character to append
   *
   * @return This writer
   * @throws IOException
   *           If buffer.length() exceeds maxLength.
   *
   */
  @Override
  public StringWriter append(char c) throws IOException {
    write(c);
    if (this.maxLength > 0 && this.builder.length() > this.maxLength) {
      throw new IOException("Buffer size exceeds maxLength.");
    }
    return this;
  }

  /**
   * Return the buffer's current value as a string.
   */
  @Override
  public String toString() {
    return builder.toString();
  }

  /**
   * Return the string buffer itself.
   *
   * @return StringBuffer holding the current builder value.
   */
  public StringBuilder getBuffer() {
    return builder;
  }

  /**
   * Flush the stream.
   */
  @Override
  public void flush() {
  }

  /**
   * Closing a <tt>StringWriter</tt> has no effect. The methods in this class
   * can be called after the stream has been closed without generating an
   * <tt>IOException</tt>.
   */
  @Override
  public void close() throws IOException {
  }

}
