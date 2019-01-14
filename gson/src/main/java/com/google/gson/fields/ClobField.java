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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;

import com.google.gson.stream.StringWriter;

/**
 * 
 * @author Caleb Shingledecker
 *
 */
public interface ClobField extends StreamingField {

  public Charset getCharset();

  public long getLength();

  public Writer getWriter() throws FileNotFoundException, IOException;

  public Reader getReader() throws FileNotFoundException, IOException;

  public ClobField setString(String string) throws FileNotFoundException, IOException;

  public default long setText(Reader reader) throws FileNotFoundException, IOException {
    long charactors = 0;
    int count = 0;
    char[] buffer = new char[2048];
    try (Writer writer = this.getWriter()) {
      while ((count = reader.read(buffer)) > -1) {
        writer.write(buffer, 0, count);
        charactors += count;
      }
    }
    return charactors;
  }

  public default long getText(Writer writer) throws FileNotFoundException, IOException {
    long charactors = 0;
    int count = 0;
    char[] buffer = new char[2048];
    try (Reader reader = this.getReader()) {
      while ((count = reader.read(buffer)) > -1) {
        writer.write(buffer, 0, count);
        charactors += count;
      }
    }
    return charactors;
  }

  public default String getText() throws FileNotFoundException, IOException {
    StringWriter stringWriter = new StringWriter((int) this.getLength());
    this.getText(stringWriter);
    return stringWriter.toString();
  }
}
