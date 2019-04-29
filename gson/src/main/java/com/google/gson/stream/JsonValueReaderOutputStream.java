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
import java.io.OutputStream;
import java.io.Writer;

public class JsonValueReaderOutputStream extends Writer {

  /**
   * Base64 Alphabet (RFC 2045)
   *
   */
  private final byte[] fromBase64 = {
      // @formatter:off
     //0   1   2   3   4   5   6   7   8   9   A   B   C   D   E   F
      -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 00-0f
      -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 10-1f
      -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, 62, -1, 63, // 20-2f + - /
      52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -2, -1, -1, // 30-3f 0-9
      -1,  0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14, // 40-4f A-O
      15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, 63, // 50-5f P-Z _
      -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, // 60-6f a-o
      41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51                      // 70-7a p-z
      // @formatter:on
  };

  private final OutputStream outputStream;
  private byte[] buf = new byte[3];
  int bits = 0;
  int shiftto = 18; // pos of first byte of 4-byte atom
  private boolean closed = false;
  private boolean hitEnd = false;

  public JsonValueReaderOutputStream(OutputStream outputStream) {
    this.outputStream = outputStream;
  }

  @Override
  public void write(char[] cbuf, int off, int len) throws IOException {
    if (closed) {
      throw new IOException("Stream is closed");
    }

    if (!hitEnd) {
      int end = off + len;
      int position = 0;
      while (off < end) {
        int b = cbuf[off++] & 0xff;
        if ((b = fromBase64[b]) < 0) {
          if (b == -2) {
            // process remaining padding
            if (shiftto == 6) {
              outputStream.write(bits >> 16);
            } else if (shiftto == 0) {
              outputStream.write(bits >> 16);
              outputStream.write(bits >> 8);
            } else if (shiftto == 12) {
              // dangling single "x", incorrectly encoded.
              throw new IllegalArgumentException("Last unit does not have enough valid bits");
            }
            bits = 0;
            shiftto = 18;
            this.hitEnd = true;
            break;
          }
          // Ignore invalid charactors
          continue;
        }
        bits |= (b << shiftto);
        shiftto -= 6;
        if (shiftto < 0) {
          buf[position++] = (byte) (bits >> 16);
          buf[position++] = (byte) (bits >> 8);
          buf[position++] = (byte) (bits);
          outputStream.write(buf, 0, position);
          shiftto = 18;
          bits = 0;
          position = 0;
        }
      }
    } else {
      // anything left is invalid
      while (off < len) {
        if (fromBase64[cbuf[off++]] < 0)
          continue;
        throw new IllegalArgumentException("Input byte array has incorrect ending byte at " + off);
      }
    }
  }

  @Override
  public void flush() throws IOException {
    outputStream.flush();
  }

  @Override
  public void close() throws IOException {
    if (!closed) {
      closed = true;
      if (shiftto == 6) {
        outputStream.write(bits >> 16);
      } else if (shiftto == 0) {
        outputStream.write(bits >> 16);
        outputStream.write(bits >> 8);
      } else if (shiftto == 12) {
        // dangling single "x", incorrectly encoded.
        throw new IllegalArgumentException("Last unit does not have enough valid bits");
      }
      outputStream.close();
    }
  }
}
