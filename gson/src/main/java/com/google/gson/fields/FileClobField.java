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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 * 
 * @author Caleb Shingledecker
 *
 */
public class FileClobField implements ClobField {

  protected final File file;
  protected final Charset charset;

  public FileClobField(File file, Charset charset) {
    this(file, charset, true);
  }

  public FileClobField(File file, Charset charset, boolean register) {
    this.file = file;
    this.charset = charset;
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
  public Writer getWriter() throws IOException {
    return new OutputStreamWriter(new FileOutputStream(file), charset);
  }

  @Override
  public Reader getReader() throws IOException {
    return new InputStreamReader(new FileInputStream(file), charset);
  }

  @Override
  public ClobField setString(String string) throws IOException {
    try (Writer writer = this.getWriter()) {
      writer.write(string);
    }
    return this;
  }

  @Override
  public Charset getCharset() {
    return this.charset;
  }

  @Override
  public long getLength() {
    return this.file.length();
  }

  @Override
  public void close() {
    this.file.delete();
  }
}
