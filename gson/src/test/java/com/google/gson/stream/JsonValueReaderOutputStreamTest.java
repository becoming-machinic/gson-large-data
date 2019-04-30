package com.google.gson.stream;

import static org.junit.Assert.assertArrayEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.Base64;
import java.util.Random;

import org.junit.Test;

public class JsonValueReaderOutputStreamTest {

  private static final Random RANDOM = new Random();

  @Test
  public void testBase64DecoderNoPadding() throws IOException {
    checkReader("MjM0NTIz");
    checkReader("NDgzOTA0OGRh");
  }

  @Test
  public void testBase64DecoderPadding1() throws IOException {
    checkReader("c2RmZ2RmZ3M=");
    checkReaderTinyBuffer("c2RmZ2RmZ3M=");
  }

  @Test
  public void testBase64DecoderPadding2() throws IOException {
    checkReader("MQ==");
    checkReaderTinyBuffer("MQ==");
    checkReader("NTQyNA==");
    checkReaderTinyBuffer("NTQyNA==");
    checkReader("c2Rmc2Fkc2ZkZg==");
    checkReaderTinyBuffer("c2Rmc2Fkc2ZkZg==");
  }

  @Test
  public void testBase64DecoderBadData() throws IOException {
    checkReaderBadData("NTQyNA==", "NTQyNA==43");
    checkReaderBadData("c2Rmc2Fkc2ZkZg==", "c2Rmc2Fkc2ZkZg==yr");
  }

  private void checkReader(String base64String) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Writer writer = null;
    try {
      writer = new JsonValueReaderOutputStream(outputStream);
      writer.write(base64String.toCharArray());
    } finally {
      if(writer != null) {
        writer.close();
      }
    }
    assertArrayEquals(Base64.getDecoder().decode(base64String), outputStream.toByteArray());
  }

  private void checkReaderTinyBuffer(String base64String) throws IOException {
    char[] buffer = base64String.toCharArray();

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Writer writer = null;
    try {
      writer = new JsonValueReaderOutputStream(outputStream);
      for (int i = 0; i < buffer.length - 1; i++) {
        writer.write(buffer, i, 1);
      }
    } finally {
      if(writer != null) {
        writer.close();
      }
    }
    assertArrayEquals(Base64.getDecoder().decode(base64String), outputStream.toByteArray());
  }

  private void checkReaderBadData(String expectedResult, String base64String) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Writer writer = null;
    try {
      writer = new JsonValueReaderOutputStream(outputStream);
      writer.write(base64String.toCharArray());
    } finally {
      if(writer != null) {
        writer.close();
      }
    }
    assertArrayEquals(Base64.getDecoder().decode(expectedResult), outputStream.toByteArray());
  }

  @Test
  public void testBase64DecoderWithExpandingBufferSize() throws IOException {
    byte[] buffer = new byte[RANDOM.nextInt(1024 * 16) + 1];
    RANDOM.nextBytes(buffer);
    for (int i = 1; i < 16; i++) {
      Base64DataDecoder(buffer, i);
    }
  }


  public void Base64DataDecoder(byte[] binaryData, int copyBufferSize) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Reader reader = null;
    Writer writer = null;
    try {
      reader = new InputStreamReader(new ByteArrayInputStream(Base64.getEncoder().encode(binaryData)));
      writer = new JsonValueReaderOutputStream(outputStream);
      char[] copyBuffer = new char[copyBufferSize];
      int read = 0;
      while ((read = reader.read(copyBuffer)) > -1) {
        writer.write(copyBuffer, 0, read);
      }
    } finally {
      if(reader != null) {
        reader.close();
      }
      if(writer != null) {
        writer.close();
      }
    }
    assertArrayEquals(String.format("Decoding fail. Buffer size %s, encoded data: %s", copyBufferSize, Base64.getEncoder().encodeToString(binaryData)), binaryData, outputStream.toByteArray());
  }

}
