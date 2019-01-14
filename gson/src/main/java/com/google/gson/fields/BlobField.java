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

package com.google.gson.fields;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 
 * @author Caleb Shingledecker
 *
 */
public interface BlobField extends StreamingField {

  public long getLength();

  public OutputStream getOutputStream() throws FileNotFoundException, IOException;

  public InputStream getInputStream() throws FileNotFoundException, IOException;

  public FileBlobField setBytes(byte[] bytes) throws FileNotFoundException, IOException;

  public FileBlobField setBytes(byte[] bytes, int offset, int length) throws FileNotFoundException, IOException;

  public default long setBytes(InputStream inputStream) throws FileNotFoundException, IOException {
    long bytes = 0;
    int count = 0;
    byte[] buffer = new byte[4096];
    try (OutputStream outputStream = this.getOutputStream()) {
      while ((count = inputStream.read(buffer)) > -1) {
        outputStream.write(buffer, 0, count);
        bytes += count;
      }
    }
    return bytes;
  }

  public default long getBytes(OutputStream outputStream) throws FileNotFoundException, IOException {
    long bytes = 0;
    int count = 0;
    byte[] buffer = new byte[4096];
    try (InputStream inputStream = this.getInputStream()) {
      while((count = inputStream.read(buffer))> -1) {
        outputStream.write(buffer, 0, count);
        bytes += count;
      }
    }
    return bytes;
  }

  public default byte[] getBytes() throws FileNotFoundException, IOException {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream((int) this.getLength());
    this.getBytes(byteArrayOutputStream);
    return byteArrayOutputStream.toByteArray();
  }
}
