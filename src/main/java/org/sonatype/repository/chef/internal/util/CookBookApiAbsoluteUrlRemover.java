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
import org.sonatype.nexus.repository.view.payloads.StreamPayload.InputStreamSupplier;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.apache.http.client.utils.URIBuilder;

import static com.google.common.base.Preconditions.checkNotNull;

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

  public Content rewriteCookBookDetailJsonToRemoveAbsoluteUrls(final Content content) throws IOException, URISyntaxException {
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
        case NULL:
          reader.nextNull();
          writer.nullValue();
          break;
        case BOOLEAN:
          writer.value(reader.nextBoolean());
          break;
        case NAME:
          String name = reader.nextName();
          writer.name(name);
          JsonToken peek = reader.peek();
          if (peek.equals(JsonToken.STRING) || (name.equals("versions") && peek.equals(JsonToken.BEGIN_ARRAY))) {
            maybeSetUrlToRelativeCase(reader, writer, name);
          }
          break;
      }
    }

    reader.close();
    writer.close();

    InputStream is = new FileInputStream(filePath);

    File tempFile = new File(filePath);

    long length = tempFile.length();

    tempFile.delete();

    return chefDataAccess.toContent(
        new FileInputStreamSupplier(is),
        length,
        ContentTypes.APPLICATION_JSON
    );
  }

  public Content rewriteCookbookListJsonToRemoveAbsoluteUrls(final Content content, final String urlTokenName) throws IOException, URISyntaxException {
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
          maybeSetUrlToRelative(reader, writer, name, urlTokenName);
          break;
      }
    }

    reader.close();
    writer.close();

    InputStream is = new FileInputStream(filePath);

    File tempFile = new File(filePath);

    long length = tempFile.length();

    tempFile.delete();

    return chefDataAccess.toContent(
        new FileInputStreamSupplier(is),
        length,
        ContentTypes.APPLICATION_JSON
    );
  }

  private void maybeSetUrlToRelativeCase(final JsonReader reader, final JsonWriter writer, final String name)
      throws IOException, URISyntaxException
  {
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

  private void doSetUrlAsRelative(final JsonReader reader, final JsonWriter writer)
      throws IOException, URISyntaxException
  {
    String url = reader.nextString();
    URI uri = new URIBuilder(url).build();
    if (uri.isAbsolute()) {
      String rightHand = uri.getPath();
      writer.value(rightHand);
    } else {
      writer.value(url);
    }
  }

  private void maybeSetUrlToRelative(final JsonReader reader, final JsonWriter writer, final String name, final String urlTokenName)
      throws IOException, URISyntaxException
  {
    if(urlTokenName.equals(name)) {
      doSetUrlAsRelative(reader, writer);
    } else {
      writer.value(reader.nextString());
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
