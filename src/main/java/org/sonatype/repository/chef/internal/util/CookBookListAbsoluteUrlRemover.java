package org.sonatype.repository.chef.internal.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.goodies.common.ComponentSupport;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.ContentTypes;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.payloads.StreamPayload;
import org.sonatype.nexus.repository.view.payloads.StreamPayload.InputStreamSupplier;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.apache.http.client.utils.URIBuilder;

import static com.google.common.base.Preconditions.checkNotNull;

@Named
@Singleton
public class CookBookListAbsoluteUrlRemover
    extends ComponentSupport
{
  private ChefDataAccess chefDataAccess;

  @Inject
  public CookBookListAbsoluteUrlRemover(final ChefDataAccess chefDataAccess) {
    this.chefDataAccess = checkNotNull(chefDataAccess);
  }

  public Content rewriteJsonToRemoveAbsoluteUrls(final Content content) throws IOException, URISyntaxException {
    String filePath = UUID.randomUUID().toString();
    FileOutputStream file = new FileOutputStream(filePath);

    JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(content.openInputStream())));
    JsonWriter writer = new JsonWriter(new OutputStreamWriter(file, "UTF-8"));

    JsonToken token;
    while (!(token = reader.peek()).equals(JsonToken.END_DOCUMENT)) {
      switch (token) {
        case BEGIN_OBJECT:
          reader.beginObject();
          writer.beginObject();
          break;
        case END_OBJECT:
          reader.endObject();
          writer.endObject();
          break;
        case BEGIN_ARRAY:
          reader.beginArray();
          writer.beginArray();
          break;
        case END_ARRAY:
          reader.endArray();
          writer.endArray();
          break;
        case STRING:
          String value = reader.nextString();
          writer.value(value);
          break;
        case NUMBER:
          String number = reader.nextString();
          writer.value(new BigDecimal(number));
          break;
        case NAME:
          String name = reader.nextName();
          writer.name(name);
          maybeSetUrlToRelative(reader, writer, name);
          break;
      }
    }

    reader.close();
    writer.close();

    return chefDataAccess.toContent(
        new FileInputStreamSupplier(new FileInputStream(filePath)),
        new File(filePath).length(),
        ContentTypes.APPLICATION_JSON
    );
  }

  private void maybeSetUrlToRelative(final JsonReader reader, final JsonWriter writer, final String name)
      throws IOException, URISyntaxException
  {
    if("cookbook".equals(name)) {
      String url = reader.nextString();
      URI uri = new URIBuilder(url).build();
      if (uri.isAbsolute()) {
        String rightHand = uri.getPath();
        writer.value(rightHand);
      } else {
        writer.value(url);
      }
    }
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
}
