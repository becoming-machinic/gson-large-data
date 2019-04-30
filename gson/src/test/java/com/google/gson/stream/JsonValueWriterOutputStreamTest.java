package com.google.gson.stream;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Random;

import org.junit.Test;

public class JsonValueWriterOutputStreamTest {

  private static final Random RANDOM = new Random();

  @Test
  public void testValueWriterNoPadding() throws IOException {
    String base64Data = "MjM0NTIz";
    assertEquals(String.format("\"%s\"", base64Data),
        valueWriterEncoder(Base64.getDecoder().decode(base64Data), true, null, -1, true));
  }

  @Test
  public void testValueWriterPadding1() throws IOException {
    String base64Data = "c2RmZ2RmZ3M=";
    assertEquals(String.format("\"%s\"", base64Data),
        valueWriterEncoder(Base64.getDecoder().decode(base64Data), true, null, -1, true));
  }

  @Test
  public void testValueWriterPadding2() throws IOException {
    String base64Data = "c2Rmc2Fkc2ZkZg==";
    assertEquals(String.format("\"%s\"", base64Data),
        valueWriterEncoder(Base64.getDecoder().decode(base64Data), true, null, -1, true));
  }

  @Test
  public void testValueWriterNoQuote() throws IOException {
    String base64Data = "MjM0NTIz";
    assertEquals(String.format("\"%s\"", base64Data),
        valueWriterEncoder(Base64.getDecoder().decode(base64Data), false, null, -1, true));
  }

  @Test
  public void testValueWriterRandomData() throws IOException {
    byte[] buffer = new byte[RANDOM.nextInt(1024 * 16) + 1];
    RANDOM.nextBytes(buffer);
    assertEquals(String.format("\"%s\"", Base64.getEncoder().encodeToString(buffer)),
        valueWriterEncoder(buffer, true, null, -1, true));
  }

  @Test
  public void testValueWriterLineBreak() throws IOException {
    byte[] buffer = new byte[RANDOM.nextInt(1024 * 16) + 1];
    RANDOM.nextBytes(buffer);
    assertEquals(
        String.format("\"%s\"",
            Base64.getMimeEncoder(16, "\n".getBytes(StandardCharsets.UTF_8))
                .encodeToString(buffer)),
        valueWriterEncoder(buffer, true, new char[] { '\n' }, 16, true));
  }

  @Test
  public void testWriteInt() throws IOException {
    StringWriter writer = new StringWriter();
    OutputStream outputStream = null;
    try {
      outputStream = new JsonValueWriterOutputStream(writer, false, null, -1, true);
      outputStream.write('a');
    } finally {
      if(outputStream != null) {
        outputStream.close();
      }
    }
    assertEquals(Base64.getEncoder().encodeToString("a".getBytes(StandardCharsets.UTF_8)),writer.toString());
  }

  private String valueWriterEncoder(byte[] data, boolean quote, char[] newline, int linemax,
      boolean doPadding) throws IOException {
    StringWriter writer = new StringWriter();
    OutputStream outputStream = null;
    try {
      outputStream = new JsonValueWriterOutputStream(writer, doPadding, newline, linemax, doPadding);
      outputStream.write(data, 0, data.length);
    } finally {
      if(outputStream != null) {
        outputStream.close();
      }
    }
    return writer.toString();
  }

  @Test
  public void testValueWriterWithExpandingBufferSize() throws IOException {
    byte[] buffer = new byte[RANDOM.nextInt(1024 * 16) + 1];
    RANDOM.nextBytes(buffer);
    for (int i = 1; i < 16; i++) {
      assertEquals(String.format("\"%s\"", Base64.getEncoder().encodeToString(buffer)),
          valueWriterEncoder(buffer, i, true, null, -1, true));
    }
  }

  private String valueWriterEncoder(byte[] data, int copyBufferSize, boolean quote, char[] newline,
      int linemax, boolean doPadding) throws IOException {
    StringWriter writer = new StringWriter();
    OutputStream outputStream = null;
    InputStream inputStream = null;
    byte[] copyBuffer = new byte[copyBufferSize];
    try {
      outputStream = new JsonValueWriterOutputStream(writer, doPadding, newline, linemax, doPadding);
      inputStream = new ByteArrayInputStream(data);
      int read = 0;
      while ((read = inputStream.read(copyBuffer)) > -1) {
        outputStream.write(copyBuffer, 0, read);
      }
    } finally {
      if(outputStream != null) {
        outputStream.close();
      }
      if(inputStream != null) {
        inputStream.close();
      }
    }
    return writer.toString();
  }
}
