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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.goodies.common.ComponentSupport;
import org.sonatype.nexus.repository.chef.internal.AssetKind;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.ContentTypes;
import org.sonatype.nexus.repository.view.payloads.StreamPayload.InputStreamSupplier;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.apache.http.client.utils.URIBuilder;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since 0.0.1
 */
@Named
@Singleton
public class CookBookApiAbsoluteUrlRemover
    extends ComponentSupport
{
  private ChefDataAccess chefDataAccess;

  @Inject
  public CookBookApiAbsoluteUrlRemover(final ChefDataAccess chefDataAccess) {
    this.chefDataAccess = checkNotNull(chefDataAccess);
  }

  public Content maybeRewriteCookbookApiResponseAbsoluteUrls(final Content content,
                                                             final AssetKind assetKind) throws IOException, URISyntaxException
  {
    String filePath = UUID.randomUUID().toString();
    FileOutputStream file = new FileOutputStream(filePath);

    JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(content.openInputStream())));
    JsonWriter writer = new JsonWriter(new OutputStreamWriter(file, "UTF-8"));

    JsonStreamer jsonStreamer = new JsonStreamer(reader, writer);
    switch (assetKind) {
      case COOKBOOKS_LIST:
        jsonStreamer.parseJson(
            (name, token) ->
                token.equals(JsonToken.STRING) && "cookbook".equals(name),
            (r, w, name) ->
                doSetUrlAsRelative(r, w));
        break;
      case COOKBOOK_DETAILS:
        jsonStreamer.parseJson(
            (name, token) ->
                token.equals(JsonToken.STRING) || (name.equals("versions") && token.equals(JsonToken.BEGIN_ARRAY)),
            (r, w, name) ->
                maybeSetUrlToRelativeCase(r, w, name));
        break;
      case COOKBOOK_DETAIL_VERSION:
        jsonStreamer.parseJson(
            (name, token) ->
                token.equals(JsonToken.STRING) || (name.equals("cookbook") || name.equals("file")),
            (r, w, name) ->
                doSetUrlAsRelative(r, w));
        break;
      case COOKBOOKS_SEARCH:
        jsonStreamer.parseJson(
            (name, token) ->
                token.equals(JsonToken.STRING) || "cookbook".equals(name),
            (r, w, name) ->
                doSetUrlAsRelative(r, w));
        break;
      case COOKBOOKS_UNIVERSE:
        jsonStreamer.parseJson(
            (name, token) ->
                token.equals(JsonToken.STRING) || (name.equals("location_path") || name.equals("download_url")),
            (r, w, name) ->
                doSetUrlAsRelative(r, w));
        break;
    }

    reader.close();
    writer.close();

    File tempFile = new File(filePath);

    InputStream is = new FileInputStream(filePath);


    long length = tempFile.length();


    Content result = chefDataAccess.toContent(
        new FileInputStreamSupplier(is),
        length,
        ContentTypes.APPLICATION_JSON
    );

    tempFile.delete();

    return result;
  }

  private class FileInputStreamSupplier
    implements InputStreamSupplier
  {
    private InputStream is;

    public FileInputStreamSupplier(final InputStream is) {
      this.is = is;
    }

    @Override
    public InputStream get() {
      return this.is;
    }
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
