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
package org.sonatype.nexus.repository.chef.internal.proxy;

import org.sonatype.goodies.testsupport.TestSupport;
import org.sonatype.nexus.common.collect.AttributesMap;
import org.sonatype.nexus.repository.chef.internal.AssetKind;
import org.sonatype.nexus.repository.chef.internal.util.ChefAttributeParser;
import org.sonatype.nexus.repository.chef.internal.util.ChefDataAccess;
import org.sonatype.nexus.repository.chef.internal.util.ChefPathUtils;
import org.sonatype.nexus.repository.chef.internal.util.CookBookApiAbsoluteUrlRemover;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Parameters;
import org.sonatype.nexus.repository.view.Request;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

public class ChefProxyFacetImplTest
    extends TestSupport
{
  private static final String PATH = "/do/a/thing";

  private static final String EXPECTED_URL_WITH_PARAMETERS = "do/a/thing?test=testvalue&test2=testvalue2";

  @Mock
  private ChefDataAccess chefDataAccess;

  @Mock
  private ChefPathUtils chefPathUtils;

  @Mock
  private ChefAttributeParser chefAttributeParser;

  @Mock
  private CookBookApiAbsoluteUrlRemover cookBookApiAbsoluteUrlRemover;

  @Mock
  private Context context;

  @Mock
  private Request request;

  @Mock
  private AttributesMap attributesMap;

  private ChefProxyFacetImpl underTest;

  @Before
  public void setUp() throws Exception {
    underTest = new ChefProxyFacetImpl(chefDataAccess, chefPathUtils, chefAttributeParser, cookBookApiAbsoluteUrlRemover);
  }

  @Test
  public void returnUrlWithParametersOnAssetKindCookbooksList() throws Exception {
    setupParameters();
    setupMocks(PATH, AssetKind.COOKBOOKS_LIST);

    String result = underTest.getUrl(context);

    assertThat(result, is(equalTo(EXPECTED_URL_WITH_PARAMETERS)));
  }

  @Test
  public void returnUrlWithParametersOnAssetKindCookbooksSearch() throws Exception {
    setupParameters();
    setupMocks(PATH, AssetKind.COOKBOOKS_SEARCH);

    String result = underTest.getUrl(context);

    assertThat(result, is(equalTo(EXPECTED_URL_WITH_PARAMETERS)));
  }

  @Test
  public void returnUrlWithoutParametersOnAssetKindWithoutParameters() throws Exception {
    setupParameters();
    setupMocks(PATH, AssetKind.COOKBOOK_DETAIL_VERSION);

    String result = underTest.getUrl(context);

    assertThat(result, is(equalTo(PATH.substring(1))));
  }

  private void setupMocks(final String path,
                          final AssetKind assetKind)
  {
    when(context.getRequest()).thenReturn(request);
    when(context.getAttributes()).thenReturn(attributesMap);
    when(attributesMap.require(AssetKind.class)).thenReturn(assetKind);
    when(request.getPath()).thenReturn(path);
  }

  private void setupParameters() {
    ListMultimap<String, String> entries = MultimapBuilder.linkedHashKeys().arrayListValues().build();
    entries.put("test", "testvalue");
    entries.put("test2", "testvalue2");
    Parameters parameters = new Parameters(entries);

    when(request.getParameters()).thenReturn(parameters);
  }
}
