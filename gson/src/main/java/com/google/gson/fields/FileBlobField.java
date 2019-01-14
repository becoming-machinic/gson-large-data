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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 
 * @author Caleb Shingledecker
 *
 */
public class FileBlobField implements BlobField {

  protected final File file;

  public FileBlobField(File file) {
    this(file, true);
  }

  public FileBlobField(File file, boolean register) {
    this.file = file;
    if (register) {
      GsonFieldResourceManager manager = GsonFieldResourceManager.get();
      if (manager != null) {
        manager.register(this);
      }
    }
  }

  public File getFile() {
    return this.file;
  }

  @Override
  public OutputStream getOutputStream() throws FileNotFoundException, IOException {
    return new FileOutputStream(file);
  }

  @Override
  public InputStream getInputStream() throws FileNotFoundException, IOException {
    return new FileInputStream(file);
  }

  @Override
  public long getLength() {
    return this.file.length();
  }

  @Override
  public FileBlobField setBytes(byte[] bytes) throws FileNotFoundException, IOException {
    return this.setBytes(bytes, 0, bytes.length);
  }

  @Override
  public FileBlobField setBytes(byte[] bytes, int offset, int length) throws FileNotFoundException, IOException {
    try (OutputStream outputStream = new FileOutputStream(file)) {
      outputStream.write(bytes, offset, length);
    }
    return this;
  }

  @Override
  public void close() {
    this.file.delete();
  }

}
