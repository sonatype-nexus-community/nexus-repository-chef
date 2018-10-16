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
package org.sonatype.nexus.repository.chef.internal;

import java.io.InputStream;

import org.sonatype.goodies.testsupport.TestSupport;
import org.sonatype.nexus.mime.MimeRulesSource;
import org.sonatype.nexus.repository.storage.DefaultContentValidator;
import org.sonatype.nexus.repository.view.ContentTypes;

import com.google.common.base.Supplier;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

public class ChefContentValidatorTest
    extends TestSupport
{
  private ChefContentValidator underTest;

  @Mock
  DefaultContentValidator defaultContentValidator;

  @Mock
  Supplier<InputStream> contentStream;

  @Mock
  MimeRulesSource mimeRulesSource;

  @Before
  public void setUp() throws Exception {
    underTest = new ChefContentValidator(defaultContentValidator);
  }

  @Test
  public void testContentValidatorJson() throws Exception {
    when(defaultContentValidator.determineContentType(
        false,
        contentStream,
        mimeRulesSource,
        "testjsonwithoutextension.json",
        ContentTypes.APPLICATION_JSON))
        .thenReturn(ContentTypes.APPLICATION_JSON);
    String result = underTest.determineContentType(
        true,
        contentStream,
        mimeRulesSource,
        "testjsonwithoutextension",
        ContentTypes.APPLICATION_JSON);

    assertThat(result, is(equalTo(ContentTypes.APPLICATION_JSON)));
  }

  @Test
  public void testContentValidatorTarGz() throws Exception {
    when(defaultContentValidator.determineContentType(
        true,
        contentStream,
        mimeRulesSource,
        "test.tar.gz",
        ContentTypes.APPLICATION_GZIP))
        .thenReturn(ContentTypes.APPLICATION_GZIP);
    String result = underTest.determineContentType(
        true,
        contentStream,
        mimeRulesSource,
        "test.tar.gz",
        ContentTypes.APPLICATION_GZIP);

    assertThat(result, is(equalTo(ContentTypes.APPLICATION_GZIP)));
  }
}
