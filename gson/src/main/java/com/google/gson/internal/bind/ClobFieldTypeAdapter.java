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

package com.google.gson.internal.bind;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.fields.ClobField;
import com.google.gson.fields.FileClobField;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class ClobFieldTypeAdapter extends TypeAdapter<ClobField> {
  public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
      if (type.getRawType() == ClobField.class || type.getRawType() == FileClobField.class) {
        return (TypeAdapter<T>) new ClobFieldTypeAdapter(gson);
      }
      return null;
    }
  };

  protected final Gson gson;

  ClobFieldTypeAdapter(Gson gson) {
    this.gson = gson;
  }

  @Override
  public ClobField read(JsonReader in) throws IOException {
    if (in.peek() == JsonToken.NULL) {
      in.nextNull();
      return null;
    }
    return deserializeToClobField(in);
  }

  @Override
  public void write(JsonWriter out, ClobField value) throws IOException {
    if (value == null) {
      out.nullValue();
      return;
    }

    try (Reader inputReader = value.getReader()) {
      if (inputReader == null) {
        out.nullValue();
        return;
      }

      try (Writer valueWriter = out.getValueWriter()) {
        char[] buffer = new char[1024];
        int n = 0;
        while ((n = inputReader.read(buffer)) != -1) {
          valueWriter.write(buffer, 0, n);
        }
      }
    }
  }

  protected ClobField getClobFieldInstance() throws IOException {
    return new FileClobField(File.createTempFile("jsonfield_", ".clob", this.gson.gsonTempdir()), gson.clobCharset(), true);
  }

  protected ClobField deserializeToClobField(JsonReader jsonReader) throws FileNotFoundException, IOException {
    ClobField clobField = getClobFieldInstance();
    try (Writer outputWriter = clobField.getWriter()) {
      jsonReader.nextClob(outputWriter);
    }
    return clobField;
  }
}
