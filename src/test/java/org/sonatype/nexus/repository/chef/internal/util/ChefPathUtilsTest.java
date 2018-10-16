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

import java.util.HashMap;
import java.util.Map;

import org.sonatype.goodies.testsupport.TestSupport;
import org.sonatype.nexus.repository.view.Parameters;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

public class ChefPathUtilsTest
    extends TestSupport
{
  private ChefPathUtils underTest;

  @Mock
  TokenMatcher.State state;

  private Map<String, String> tokens;

  @Before
  public void setUp() throws Exception {
    underTest = new ChefPathUtils();
  }

  @Test
  public void testRemoveUnderscoresFromVersion() throws Exception
  {
    tokens = setupTokens("7_0_0", "default");
    when(state.getTokens()).thenReturn(tokens);
    String result = underTest.version(state);

    assertThat(result, is(equalTo("7.0.0")));
  }

  @Test
  public void testNormalVersion() throws Exception
  {
    tokens = setupTokens("7.0.0", "default");
    when(state.getTokens()).thenReturn(tokens);
    String result = underTest.version(state);

    assertThat(result, is(equalTo("7.0.0")));
  }

  @Test
  public void testGetCookbookByVersionPath() throws Exception {
    tokens = setupTokens("7.0.0", "apt");
    when(state.getTokens()).thenReturn(tokens);
    String result = underTest.buildCookbookDetailByVersionPath(state);

    assertThat(result, is(equalTo("api/v1/cookbooks/apt/version/7.0.0")));
  }

  @Test
  public void testGetCookbookDetailPath() throws Exception {
    tokens = setupTokens("7.0.0", "apt");
    when(state.getTokens()).thenReturn(tokens);
    String result = underTest.buildCookbookDetailPath(state);

    assertThat(result, is(equalTo("api/v1/cookbooks/apt")));
  }

  @Test
  public void testGetCookbookListPath() throws Exception {
    ListMultimap<String, String> entries = MultimapBuilder.linkedHashKeys().arrayListValues().build();
    entries.put("test", "test");
    entries.put("another", "another");
    Parameters parameters = new Parameters(entries);
    String result = underTest.buildCookbookListPath(parameters);

    assertThat(result, is(equalTo("api/v1/cookbooks?test=test&another=another")));
  }

  @Test
  public void testGetCookbookDownloadPath() throws Exception {
    tokens = setupTokens("7.0.0", "apt");
    when(state.getTokens()).thenReturn(tokens);
    String result = underTest.buildCookbookPath(state);

    assertThat(result, is(equalTo("apt/7.0.0/apt-7.0.0.tar.gz")));
  }

  private Map<String, String> setupTokens(final String version,
                                          final String cookbook)
  {
    Map<String, String> tokens = new HashMap<>();
    tokens.put("version", version);
    tokens.put("cookbook", cookbook);

    return tokens;
  }
}
