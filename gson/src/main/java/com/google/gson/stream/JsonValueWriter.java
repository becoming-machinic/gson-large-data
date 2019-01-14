/*
 * Copyright (C) 2019 Becoming Machinic Inc.
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

public class JsonValueWriter extends Writer implements CloseableValueStream {

  private final Writer out;
  private final boolean excapeValues;
  private final boolean htmlSafe;
  private final boolean quote;
  private boolean closed = false;

  /**
   * Create a new value writer size.
   * 
   * @throws IOException
   */
  JsonValueWriter(Writer out, boolean excapeValues, boolean htmlSafe, boolean quote) throws IOException {
    this.out = out;
    this.excapeValues = excapeValues;
    this.htmlSafe = htmlSafe;
    this.quote = quote;
    if (this.quote) {
      out.write("\"");
    }
  }

  /**
   * Write a single character.
   * 
   * @throws IOException
   */
  @Override
  public void write(int c) throws IOException {
    this.write(new char[] { (char) c });
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
  public void write(char buf[], int off, int len) throws IOException {
    if (this.excapeValues) {
      String[] replacements = htmlSafe ? JsonWriter.HTML_SAFE_REPLACEMENT_CHARS : JsonWriter.REPLACEMENT_CHARS;
      int last = off;
      int length = off + len;
      for (int i = off; i < length; i++) {
        char c = buf[i];
        String replacement;
        if (c < 128) {
          replacement = replacements[c];
          if (replacement == null) {
            continue;
          }
        } else if (c == '\u2028') {
          replacement = "\\u2028";
        } else if (c == '\u2029') {
          replacement = "\\u2029";
        } else {
          continue;
        }
        if (last < i) {
          out.write(buf, last, i - last);
        }
        out.write(replacement);
        last = i + 1;
      }
      if (last < length) {
        out.write(buf, last, length - last);
      }
    } else {
      this.out.write(buf, off, len);
    }
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
   */
  @Override
  public void write(String str, int off, int len) throws IOException {
    if (this.excapeValues) {
      String[] replacements = htmlSafe ? JsonWriter.HTML_SAFE_REPLACEMENT_CHARS : JsonWriter.REPLACEMENT_CHARS;
      int last = off;
      int length = off + len;
      for (int i = off; i < length; i++) {
        char c = str.charAt(i);
        String replacement;
        if (c < 128) {
          replacement = replacements[c];
          if (replacement == null) {
            continue;
          }
        } else if (c == '\u2028') {
          replacement = "\\u2028";
        } else if (c == '\u2029') {
          replacement = "\\u2029";
        } else {
          continue;
        }
        if (last < i) {
          out.write(str, last, i - last);
        }
        out.write(replacement);
        last = i + 1;
      }
      if (last < length) {
        out.write(str, last, length - last);
      }
    } else {
      this.out.write(str, off, len);
    }
  }

  @Override
  public void flush() throws IOException {
    this.out.flush();
  }

  /**
   * Closing and finalize the value writer <tt>IOException</tt>.
   */
  @Override
  public void close() throws IOException {
    if (!closed) {
      closed = true;
      if (this.quote) {
        out.write("\"");
      }
    }
  }

  @Override
  public boolean isClosed() {
    return this.closed;
  }

}
