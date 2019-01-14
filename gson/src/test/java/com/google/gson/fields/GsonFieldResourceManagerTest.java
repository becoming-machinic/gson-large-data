package com.google.gson.fields;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Random;

import org.junit.Test;

import com.google.gson.Gson;

public class GsonFieldResourceManagerTest {

  @Test
  public void testAutoRegisterBlob() throws IOException {
    byte[] buffer = new byte[2048];
    new Random().nextBytes(buffer);
    Gson gson = new Gson();

    File tempFile = File.createTempFile("json_", ".blob", gson.gsonTempdir());
    try (GsonFieldResourceManager manager = GsonFieldResourceManager.create()) {
      BlobField blobField = new FileBlobField(tempFile, true).setBytes(buffer);
      assertTrue("The resource did not get created", tempFile.exists());
      assertEquals(buffer.length, blobField.getLength());
    }
    assertFalse("The resource did not get removed", tempFile.exists());
  }

  @Test
  public void testAutoRegisterClob() throws IOException {
    byte[] buffer = new byte[2048];
    new Random().nextBytes(buffer);
    String testString = Base64.getEncoder().encodeToString(buffer);
    Gson gson = new Gson();

    File tempFile = File.createTempFile("json_", ".blob", gson.gsonTempdir());
    try (GsonFieldResourceManager manager = GsonFieldResourceManager.create()) {
      ClobField clobField = new FileClobField(tempFile, StandardCharsets.UTF_8, true).setString(testString);
      assertTrue("The resource did not get created", tempFile.exists());
      assertEquals(testString.getBytes().length, clobField.getLength());
    }
    assertFalse("The resource did not get removed", tempFile.exists());
  }

  @Test
  public void testNestedAutoRegisterResourceManagers() throws IOException {
    byte[] buffer = new byte[2048];
    Random random = new Random();

    File tempFile1 = File.createTempFile("json_", ".blob");
    File tempFile2 = File.createTempFile("json_", ".blob");
    File tempFile3 = File.createTempFile("json_", ".blob");

    try (GsonFieldResourceManager manager1 = GsonFieldResourceManager.create()) {
      random.nextBytes(buffer);
      new FileBlobField(tempFile1, true).setBytes(buffer);
      try (GsonFieldResourceManager manager2 = GsonFieldResourceManager.create()) {
        random.nextBytes(buffer);
        new FileBlobField(tempFile2, true).setBytes(buffer);
        try (GsonFieldResourceManager manager3 = GsonFieldResourceManager.create()) {
          random.nextBytes(buffer);
          new FileBlobField(tempFile3, true).setBytes(buffer);
          assertTrue("The resource get removed", tempFile3.exists());
        }
        assertTrue("The resource get removed", tempFile2.exists());
      }
      assertTrue("The resource get removed", tempFile1.exists());
    }
    assertNull("There should not be any more managers on the stack", GsonFieldResourceManager.get());

    assertFalse("The resource did not get removed", tempFile1.exists());
    assertFalse("The resource did not get removed", tempFile2.exists());
    assertFalse("The resource did not get removed", tempFile3.exists());
  }

  @Test
  public void testNestedResourceManagers() throws IOException {
    byte[] buffer = new byte[2048];
    Random random = new Random();

    File tempFile1 = File.createTempFile("json_", ".blob");
    File tempFile2 = File.createTempFile("json_", ".blob");
    File tempFile3 = File.createTempFile("json_", ".blob");

    try (GsonFieldResourceManager manager1 = GsonFieldResourceManager.create()) {
      random.nextBytes(buffer);
      manager1.register(new FileBlobField(tempFile1)).setBytes(buffer);
      try (GsonFieldResourceManager manager2 = GsonFieldResourceManager.create()) {
        random.nextBytes(buffer);
        manager2.register(new FileBlobField(tempFile2)).setBytes(buffer);
        try (GsonFieldResourceManager manager3 = GsonFieldResourceManager.create()) {
          random.nextBytes(buffer);
          manager3.register(new FileBlobField(tempFile3)).setBytes(buffer);
          assertTrue("The resource get removed", tempFile3.exists());
        }
        assertTrue("The resource get removed", tempFile2.exists());
      }
      assertTrue("The resource get removed", tempFile1.exists());
    }
    assertNull("There should not be any more managers on the stack", GsonFieldResourceManager.get());

    assertFalse("The resource did not get removed", tempFile1.exists());
    assertFalse("The resource did not get removed", tempFile2.exists());
    assertFalse("The resource did not get removed", tempFile3.exists());
  }

  public class SamplePojo {

    private String name;
    private BlobField data;
    private ClobField text;

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

    public ClobField getText() {
      return text;
    }

    public void setText(ClobField text) {
      this.text = text;
    }

  }
}
