package com.google.gson.internal.bind;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import com.google.gson.GsonBuilder;
import com.google.gson.fields.ClobField;
import com.google.gson.fields.FileClobField;
import com.google.gson.fields.GsonFieldResourceManager;

public class ClobFieldTypeAdapterTest {

  @Test
  public void testDeserializer() throws IOException {
    String name = "sample";
    String text = "some large text object";
    String json = String.format("{\"name\":\"%s\",\"text\":\"%s\"}", name, text);


    try (GsonFieldResourceManager manager = GsonFieldResourceManager.create()) {
      SamplePojo sampleObject = new GsonBuilder().create().fromJson(json, SamplePojo.class);
      assertEquals(name, sampleObject.getName());
      assertEquals(text, sampleObject.getText().getText());
    }
  }

  @Test
  public void testSerializer() throws IOException {
    String name = "sample";
    String text = "some large text object";
    String json = String.format("{\"name\":\"%s\",\"text\":\"%s\"}", name, text);

    try (GsonFieldResourceManager manager = GsonFieldResourceManager.create()) {
      SamplePojo sampleObject = new SamplePojo();
      sampleObject.setName("sample");
      sampleObject.setData(new FileClobField(File.createTempFile("json_", ".blob"), StandardCharsets.UTF_8).setString(text));
      assertEquals(json, new GsonBuilder().create().toJson(sampleObject));
    }
  }

  public class SamplePojo {

    private String name;
    private ClobField text;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public ClobField getText() {
      return text;
    }

    public void setData(ClobField text) {
      this.text = text;
    }

  }
}
