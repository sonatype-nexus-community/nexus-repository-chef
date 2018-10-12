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
package org.sonatype.nexus.repository.chef.internal.util;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.function.BiFunction;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since 0.0.1
 */
public class JsonStreamer
{
  private final JsonReader reader;

  private final JsonWriter writer;

  JsonStreamer(final JsonReader reader, final JsonWriter writer) {
    this.reader = checkNotNull(reader);
    this.writer = checkNotNull(writer);
  }

  private JsonReader getReader() {
    return reader;
  }

  private JsonWriter getWriter() {
    return writer;
  }

  public void parseJson(final BiFunction<String, JsonToken, Boolean> condition,
                        final UrlRemover remover) throws IOException
  {
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
          getAndSetName(condition, remover);
          break;
      }
    }
  }

  private void getAndSetName(final BiFunction<String, JsonToken, Boolean> condition,
                            final UrlRemover remover)
  {
    try {
      String name = getReader().nextName();

      getWriter().name(name);
      JsonToken peek = getReader().peek();
      if (condition.apply(name, peek)) {
        remover.remove(getReader(), getWriter(), name);
      }
    }
    catch (Exception ex) {
      throw new RuntimeException("Could not rewrite");
    }
  }

  private void getAndSetBoolean() throws IOException {
    writer.value(reader.nextBoolean());
  }

  private void getAndSetNull() throws IOException {
    reader.nextNull();
    writer.nullValue();
  }

  private void getAndSetNumber() throws IOException {
    writer.value(new BigDecimal(reader.nextString()));
  }

  private void getAndSetString() throws IOException {
    String value = reader.nextString();
    writer.value(value);
  }

  private void getAndSetEndArray() throws IOException {
    reader.endArray();
    writer.endArray();
  }

  private void getAndSetBeginArray() throws IOException {
    reader.beginArray();
    writer.beginArray();
  }

  private void getAndSetEndObject() throws IOException {
    reader.endObject();
    writer.endObject();
  }

  private void getAndSetBeginObject() throws IOException {
    reader.beginObject();
    writer.beginObject();
  }
}
