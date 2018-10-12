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
import java.io.InputStream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.chef.internal.metadata.ChefAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since 0.0.1
 */
@Named
@Singleton
public class ChefAttributeParser
{
  private TgzParser tgzParser;

  private ObjectMapper objectMapper;

  @Inject
  public ChefAttributeParser(final TgzParser tgzParser,
                             final ObjectMapper objectMapper) {
    this.tgzParser = checkNotNull(tgzParser);
    this.objectMapper = checkNotNull(objectMapper);
  }

  public ChefAttributes getAttributesFromInputStream(final InputStream inputStream) throws IOException {
    try (InputStream is = tgzParser.getMetadataFromInputStream(inputStream)) {
      ChefAttributes chefAttributes = this.objectMapper.readValue(is, ChefAttributes.class);

      return chefAttributes;
    }
  }
}
