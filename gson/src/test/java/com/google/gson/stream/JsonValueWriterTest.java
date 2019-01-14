package com.google.gson.stream;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.Writer;

import org.junit.Test;

public class JsonValueWriterTest {

  @Test
  public void testRawStringWrite() throws IOException {
    String data = "test value";
    assertEquals(data, writeValue(data, false, false, false));
  }

  @Test
  public void testRawCharWrite() throws IOException {
    String data = "test value";
    assertEquals(data, writeValue(data.toCharArray(), false, false, false));
  }

  @Test
  public void testRawStringWriteQuoted() throws IOException {
    String data = "test value";
    assertEquals("\"" + data + "\"", writeValue(data, false, false, true));
  }

  @Test
  public void testRawCharWriteQuoted() throws IOException {
    String data = "test value";
    assertEquals("\"" + data + "\"", writeValue(data.toCharArray(), false, false, true));
  }

  @Test
  public void testValueStringWriteQuoted() throws IOException {
    String data = "\"test\" \"value\"";
    assertEquals("\"\\\"test\\\" \\\"value\\\"\"", writeValue(data, true, false, true));
  }

  @Test
  public void testValueCharWriteQuoted() throws IOException {
    String data = "\"test\" \"value\"";
    assertEquals("\"\\\"test\\\" \\\"value\\\"\"", writeValue(data.toCharArray(), true, false, true));
  }

  @Test
  public void testValueWriteStringQuotedExcaped() throws IOException {
    String data = "test value \u2028\u2029";
    assertEquals("\"test value \\u2028\\u2029\"", writeValue(data, true, false, true));
  }

  @Test
  public void testValueWriteCharQuotedExcaped() throws IOException {
    String data = "test value \u2028\u2029";
    assertEquals("\"test value \\u2028\\u2029\"", writeValue(data.toCharArray(), true, false, true));
  }

  private String writeValue(String value, boolean excapeValues, boolean htmlSafe, boolean quote) throws IOException {
    StringWriter writer = new StringWriter();
    try (Writer jsonValueWriter = new JsonValueWriter(writer, excapeValues, htmlSafe, quote)) {
      jsonValueWriter.write(value, 0, value.length());
    }
    return writer.toString();
  }

  private String writeValue(char[] value, boolean excapeValues, boolean htmlSafe, boolean quote) throws IOException {
    StringWriter writer = new StringWriter();
    try (Writer jsonValueWriter = new JsonValueWriter(writer, excapeValues, htmlSafe, quote)) {
      jsonValueWriter.write(value, 0, value.length);
    }
    return writer.toString();
  }
}
