package com.google.gson.internal.bind;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;

import org.junit.Test;

import com.google.gson.GsonBuilder;
import com.google.gson.fields.BlobField;
import com.google.gson.fields.FileBlobField;
import com.google.gson.fields.GsonFieldResourceManager;

public class BlobFieldTypeAdapterTest {

  @Test
  public void testDeserializer() throws IOException {
    byte[] buffer = new byte[2048];
    new Random().nextBytes(buffer);
    String name = "sample";
    String json = String.format("{\"name\": \"%s\", \"data\": \"%s\"}", name, Base64.getEncoder().encodeToString(buffer));


    try (GsonFieldResourceManager manager = GsonFieldResourceManager.create()) {
      SamplePojo sampleObject = new GsonBuilder().create().fromJson(json, SamplePojo.class);
      assertEquals(name, sampleObject.getName());
      assertArrayEquals(buffer, sampleObject.getData().getBytes());
    }
  }

  @Test
  public void testSerializer() throws IOException {
    byte[] buffer = new byte[2048];
    new Random().nextBytes(buffer);

    try (GsonFieldResourceManager manager = GsonFieldResourceManager.create()) {
      SamplePojo sampleObject = new SamplePojo();
      sampleObject.setName("sample");
      sampleObject.setData(new FileBlobField(File.createTempFile("json_", ".blob"), true).setBytes(buffer));

      String accualJson = new GsonBuilder().create().toJson(sampleObject);
      String expectedJson = String.format("{\"name\":\"%s\",\"data\":\"%s\"}", sampleObject.getName(), Base64.getEncoder().encodeToString(buffer));
      assertEquals(expectedJson, accualJson);
    }
  }

  public class SamplePojo {

    private String name;
    private BlobField data;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public BlobField getData() {
      return data;
    }

    public void setData(BlobField data) {
      this.data = data;
    }

  }
}
