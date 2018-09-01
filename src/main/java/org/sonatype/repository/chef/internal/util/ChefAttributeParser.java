package org.sonatype.repository.chef.internal.util;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.repository.chef.internal.metadata.ChefAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since 0.0.1
 */
@Named
@Singleton
public class ChefAttributeParser
{
  private TgzParser tgzParser;

  @Inject
  public ChefAttributeParser(final TgzParser tgzParser) {
    this.tgzParser = checkNotNull(tgzParser);
  }

  public ChefAttributes getAttributesFromInputStream(final InputStream inputStream) throws IOException {
    try (InputStream is = tgzParser.getMetadataFromInputStream(inputStream)) {
      ObjectMapper objectMapper = new ObjectMapper();

      String metadataJson = IOUtils.toString(is);

      ChefAttributes chefAttributes = objectMapper.readValue(metadataJson, ChefAttributes.class);

      return chefAttributes;
    }
  }
}
