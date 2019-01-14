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
import java.io.InputStream;
import java.io.OutputStream;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.fields.BlobField;
import com.google.gson.fields.FileBlobField;
import com.google.gson.fields.GsonFieldResourceManager;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class BlobFieldTypeAdapter extends TypeAdapter<BlobField> {
  public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
      if (type.getRawType() == BlobField.class || type.getRawType() == FileBlobField.class) {
        return (TypeAdapter<T>) new BlobFieldTypeAdapter(gson);
      }
      return null;
    }
  };

  protected final Gson gson;

  BlobFieldTypeAdapter(Gson gson) {
    this.gson = gson;
  }

  @Override
  public BlobField read(JsonReader in) throws IOException {
    if (in.peek() == JsonToken.NULL) {
      in.nextNull();
      return null;
    }
    return deserializeToBlobField(in);
  }

  @Override
  public void write(JsonWriter out, BlobField value) throws IOException {
    if (value == null) {
      out.nullValue();
      return;
    }

    try (InputStream blobInputStream = value.getInputStream()) {
      if (blobInputStream == null) {
        out.nullValue();
        return;
      }

      try (OutputStream valueOutputStream = out.getValueOutputStream(gson.base64Newline(), gson.Base64LineMaxLength(), gson.base64DoPadding())) {
        byte[] buffer = new byte[1024 * 8];
        int n = 0;
        while ((n = blobInputStream.read(buffer)) != -1) {
          valueOutputStream.write(buffer, 0, n);
        }
      }
    }
  }

  protected BlobField getBlobFieldInstance() throws IOException {
    BlobField blobField = new FileBlobField(File.createTempFile("jsonfield_", ".blob", this.gson.gsonTempdir()));
    GsonFieldResourceManager manager = GsonFieldResourceManager.get();
    if (manager != null) {
      manager.register(blobField);
    }
    return blobField;
  }

  protected BlobField deserializeToBlobField(JsonReader jsonReader) throws FileNotFoundException, IOException {
    BlobField blobField = getBlobFieldInstance();
    try (OutputStream blobOutputStream = blobField.getOutputStream()) {
      jsonReader.nextBlob(blobField.getOutputStream());
    }
    return blobField;
  }
}
