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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;

import org.sonatype.goodies.testsupport.TestSupport;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.utils.URIBuilder;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

public class JsonStreamerTest
    extends TestSupport
{
  private JsonStreamer underTest;

  private JsonReader reader;

  private JsonWriter writer;

  private InputStreamReader isr;

  private ByteArrayOutputStream baos;

  private OutputStreamWriter osw;

  private static final String COOKBOOK_LIST = "cookbooklist.json";

  private static final String COOKBOOK_DETAILS = "cookbookdetails.json";

  private static final String COOKBOOK_DETAILS_BY_VERSION = "cookbookdetailversion.json";

  private static final String COOKBOOK_SEARCH = "cookbooksearch.json";

  private static final String COOKBOOK_LIST_EXPECTED = "cookbooklist_result.json";

  private static final String COOKBOOK_DETAILS_EXPECTED = "cookbookdetails_result.json";

  private static final String COOKBOOK_DETAILS_BY_VERSION_EXPECTED = "cookbookdetailversion_result.json";

  private static final String COOKBOOK_SEARCH_EXPECTED = "cookbooksearch_result.json";

  public void setUp(final InputStream is) throws Exception {
    isr = new InputStreamReader(is);
    baos = new ByteArrayOutputStream();
    osw = new OutputStreamWriter(baos);
    reader = new JsonReader(isr);
    writer = new JsonWriter(osw);
    underTest = new JsonStreamer(reader, writer);
  }

  @Test
  public void rewriteCookBookList() throws Exception {
    setUp(getClass().getResourceAsStream(COOKBOOK_LIST));
    underTest.parseJson(
        (name, token) ->
            "cookbook".equals(name),
        this::doSetUrlAsRelative);
    reader.close();
    writer.close();

    JSONAssert.assertEquals(
        baos.toString(),
        IOUtils.toString(getClass().getResourceAsStream(COOKBOOK_LIST_EXPECTED)),
        false);
  }

  //@Test
  //public void rewriteCookBookDetails() throws Exception {
  //  setUp(getClass().getResourceAsStream(COOKBOOK_DETAILS));
  //  underTest.parseJson(
  //      (name, token) ->
  //          (name.equals("versions") && token.equals(JsonToken.BEGIN_ARRAY)),
  //      this::maybeSetUrlToRelativeCase);
  //  reader.close();
  //  writer.close();
  //
  //  JSONAssert.assertEquals(
  //      baos.toString(),
  //      IOUtils.toString(getClass().getResourceAsStream(COOKBOOK_DETAILS_EXPECTED)),
  //      false);
  //}

  @Test
  public void rewriteCookBookDetailsByVersion() throws Exception {
    setUp(getClass().getResourceAsStream(COOKBOOK_DETAILS_BY_VERSION));
    underTest.parseJson(
        (name, token) ->
            (name.equals("cookbook") || name.equals("file")),
        this::doSetUrlAsRelative);
    reader.close();
    writer.close();

    JSONAssert.assertEquals(
        baos.toString(),
        IOUtils.toString(getClass().getResourceAsStream(COOKBOOK_DETAILS_BY_VERSION_EXPECTED)),
        false);
  }

  @Test
  public void rewriteCookBookSearch() throws Exception {
    setUp(getClass().getResourceAsStream(COOKBOOK_SEARCH));
    underTest.parseJson(
        (name, token) ->
            "cookbook".equals(name),
        this::doSetUrlAsRelative);
    reader.close();
    writer.close();

    JSONAssert.assertEquals(
        baos.toString(),
        IOUtils.toString(getClass().getResourceAsStream(COOKBOOK_SEARCH_EXPECTED)),
        false);
  }

  private void maybeSetUrlToRelativeCase(final JsonReader reader, final JsonWriter writer, final String name)
  {
    try {
      switch (name) {
        case "latest_version":
          doSetUrlAsRelative(reader, writer);
          break;
        case "versions":
          reader.beginArray();
          writer.beginArray();
          while (reader.hasNext()) {
            doSetUrlAsRelative(reader, writer);
          }
          reader.endArray();
          writer.endArray();
          break;
        default:
          writer.value(reader.nextString());
          break;
      }
    }
    catch (IOException ex) {
      throw new RuntimeException("Oops");
    }
  }

  private void doSetUrlAsRelative(final JsonReader reader, final JsonWriter writer)
  {
    try {
      String url = reader.nextString();
      try {
        URI uri = new URIBuilder(url).build();
        if (uri.isAbsolute()) {
          String rightHand = uri.getPath();

          writer.value(rightHand);
        }
        else {
          writer.value(url);
        }
      }
      catch (URISyntaxException ex) {
        writer.value(url);
      }
    }
    catch (IOException ex) {
      throw new RuntimeException("Oops");
    }
  }
}
