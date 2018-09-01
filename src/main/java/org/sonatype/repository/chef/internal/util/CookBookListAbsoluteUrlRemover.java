package org.sonatype.repository.chef.internal.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Context;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.utils.URIBuilder;

@Named
@Singleton
public class CookBookListAbsoluteUrlRemover
{
  public String rewriteJsonToRemoveAbsoluteUrls(final Content content) throws IOException, URISyntaxException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    JsonFactory factory = new JsonFactory();
    JsonParser parser = factory.createParser(IOUtils.toString(content.openInputStream()));
    JsonGenerator generator = factory.createGenerator(baos, JsonEncoding.UTF8);

    JsonToken token;

    while ((token = parser.nextToken()) != null) {
      if (token == JsonToken.START_ARRAY) {
        generator.writeFieldName(parser.getCurrentName());
        generator.writeStartArray();
      } else if (token == JsonToken.END_ARRAY) {
        generator.writeEndArray();
      } else if (token == JsonToken.START_OBJECT) {
        generator.writeStartObject();
      } else if (token == JsonToken.END_OBJECT) {
        generator.writeEndObject();
      } else if (token == JsonToken.VALUE_NUMBER_INT) {
        generator.writeNumberField(parser.getCurrentName(), parser.getIntValue());
      } else if (token == JsonToken.VALUE_STRING) {
        if ("cookbook".equals(parser.getCurrentName())) {
          String url = parser.getText();
          URI uri = new URIBuilder(url).build();
          if (uri.isAbsolute()) {
            String rightHand = uri.getPath();
            generator.writeStringField(parser.getCurrentName(), rightHand);
          } else {
            generator.writeStringField(parser.getCurrentName(), url);
          }
        } else {
          generator.writeStringField(parser.getCurrentName(), parser.getText());
        }
      }
    }

    parser.close();
    generator.close();

    return new String(baos.toByteArray());
  }
}
