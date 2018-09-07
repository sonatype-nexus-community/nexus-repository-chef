/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2018-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.repository.chef.internal.util;

import java.io.IOException;
import java.math.BigDecimal;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class JsonStreamer
{
  private final JsonReader reader;

  private final JsonWriter writer;

  public JsonStreamer(final JsonReader reader, final JsonWriter writer) {
    this.reader = checkNotNull(reader);
    this.writer = checkNotNull(writer);
  }

  public JsonReader getReader() {
    return reader;
  }

  public JsonWriter getWriter() {
    return writer;
  }

  public void parseJson() throws IOException {
    JsonToken token;
    while (!(token = reader.peek()).equals(JsonToken.END_DOCUMENT)) {
      switch (token) {
        case BEGIN_OBJECT:
          getAndSetBeginObject();
          break;
        case END_OBJECT:
          getAndSetEndObject();
          break;
        case BEGIN_ARRAY:
          getAndSetBeginArray();
          break;
        case END_ARRAY:
          getAndSetEndArray();
          break;
        case STRING:
          getAndSetString();
          break;
        case NUMBER:
          getAndSetNumber();
          break;
        case NULL:
          getAndSetNull();
          break;
        case BOOLEAN:
          getAndSetBoolean();
          break;
        case NAME:
          getAndSetName();
          break;
      }
    }
  }

  public void getAndSetName() throws IOException {
    writer.name(reader.nextName());
  }

  public void getAndSetBoolean() throws IOException {
    writer.value(reader.nextBoolean());
  }

  public void getAndSetNull() throws IOException {
    reader.nextNull();
    writer.nullValue();
  }

  public void getAndSetNumber() throws IOException {
    writer.value(new BigDecimal(reader.nextString()));
  }

  public void getAndSetString() throws IOException {
    String value = reader.nextString();
    writer.value(value);
  }

  public void getAndSetEndArray() throws IOException {
    reader.endArray();
    writer.endArray();
  }

  public void getAndSetBeginArray() throws IOException {
    reader.beginArray();
    writer.beginArray();
  }

  public void getAndSetEndObject() throws IOException {
    reader.endObject();
    writer.endObject();
  }

  public void getAndSetBeginObject() throws IOException {
    reader.beginObject();
    writer.beginObject();
  }
}
