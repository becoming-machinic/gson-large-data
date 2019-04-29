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
import java.io.OutputStream;
import java.io.Writer;

/**
 * Write binary data to json value as base64.
 * This implementation is similar to the java.util.Base64.EncOutputStream but writes directly to the underlying writer without unnecessary round trip conversion.
 */
public final class JsonValueWriterOutputStream extends OutputStream implements CloseableValueStream {

  /**
   * The Base64 Alphabet.
   */
  private static final char[] base64Table = {
      'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
      'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
      'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
      'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
  };

  private final Writer out;
  private final boolean quote;
  private final char[] newline; // line separator, if needed
  private final int linemax;
  private final boolean doPadding;// whether or not to pad

  private int linepos = 0;
  private char[] buf;
  private int leftover = 0;
  private int b0, b1, b2;
  private boolean closed = false;

  /**
   * Create a value writer capable of writing base64 encoded data directly to a writer.
   * 
   * @throws IOException
   */
  JsonValueWriterOutputStream(Writer out, boolean quote, char[] newline, int linemax, boolean doPadding) throws IOException {
    this.out = out;
    this.quote = quote;
    this.newline = newline;
    this.linemax = (linemax > 0 ? linemax : -1); // Ensure linemax is not zero
    this.doPadding = doPadding;
    this.buf = new char[linemax <= 0 ? 8124 : linemax];
    if (this.quote) {
      out.write("\"");
    }
  }

  @Override
  public void write(int b) throws IOException {
    byte[] buf = new byte[1];
    buf[0] = (byte) (b & 0xff);
    write(buf, 0, 1);
  }

  private void checkNewline() throws IOException {
    if (linepos == linemax) {
      out.write(newline);
      linepos = 0;
    }
  }

  private void write(char b1, char b2, char b3, char b4) throws IOException {
    buf[0] = b1;
    buf[1] = b2;
    buf[2] = b3;
    buf[3] = b4;
    out.write(buf, 0, 4);
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    if (closed)
      throw new IOException("Stream is closed");
    if (off < 0 || len < 0 || len > b.length - off)
      throw new ArrayIndexOutOfBoundsException();
    if (len == 0)
      return;
    if (leftover != 0) {
      if (leftover == 1) {
        b1 = b[off++] & 0xff;
        len--;
        if (len == 0) {
          leftover++;
          return;
        }
      }
      b2 = b[off++] & 0xff;
      len--;
      checkNewline();
      write(base64Table[b0 >> 2],
          base64Table[(b0 << 4) & 0x3f | (b1 >> 4)],
          base64Table[(b1 << 2) & 0x3f | (b2 >> 6)],
          base64Table[b2 & 0x3f]);
      linepos += 4;
    }
    int nBits24 = len / 3;
    leftover = len - (nBits24 * 3);

    while (nBits24 > 0) {
      checkNewline();
      int dl = linemax < 0 ? buf.length : buf.length - linepos;
      int sl = off + Math.min(nBits24, dl / 4) * 3;
      int position = 0;
      for (int sp = off; sp < sl;) {
        int bits = (b[sp++] & 0xff) << 16 |
            (b[sp++] & 0xff) << 8 |
            (b[sp++] & 0xff);
        buf[position++] = base64Table[(bits >>> 18) & 0x3f];
        buf[position++] = base64Table[(bits >>> 12) & 0x3f];
        buf[position++] = base64Table[(bits >>> 6) & 0x3f];
        buf[position++] = base64Table[bits & 0x3f];
      }
      out.write(buf, 0, position);
      off = sl;
      linepos += position;
      nBits24 -= position / 4;
    }
    if (leftover == 1) {
      b0 = b[off++] & 0xff;
    } else if (leftover == 2) {
      b0 = b[off++] & 0xff;
      b1 = b[off++] & 0xff;
    }
  }

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
      if (leftover == 1) {
        checkNewline();
        out.write(base64Table[b0 >> 2]);
        out.write(base64Table[(b0 << 4) & 0x3f]);
        if (doPadding) {
          out.write('=');
          out.write('=');
        }
      } else if (leftover == 2) {
        checkNewline();
        out.write(base64Table[b0 >> 2]);
        out.write(base64Table[(b0 << 4) & 0x3f | (b1 >> 4)]);
        out.write(base64Table[(b1 << 2) & 0x3f]);
        if (doPadding) {
          out.write('=');
        }
      }
      leftover = 0;
      if (this.quote) {
        out.write("\"");
      }
    }
  }

  @Override
  public boolean isClosed() {
    return this.isClosed();
  }

}
