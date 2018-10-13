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

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Parameters;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;

import com.google.common.base.Joiner;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since 0.0.1
 */
@Named
@Singleton
public class ChefPathUtils
{
  public String cookbook(final TokenMatcher.State state) { return match(state, "cookbook"); }

  public String version(final TokenMatcher.State state) { return match(state, "version"); }

  private String match(final TokenMatcher.State state, final String name) {
    checkNotNull(state);
    String result = state.getTokens().get(name);
    checkNotNull(result);
    return result;
  }

  /**
   * Returns the {@link TokenMatcher.State} for the content.
   */
  public TokenMatcher.State matcherState(final Context context) {
    return context.getAttributes().require(TokenMatcher.State.class);
  }

  public String buildCookbookPath(final TokenMatcher.State state) {
    String cookbook = cookbook(state);
    String version = version(state);

    return String.format("cookbooks/%s/%s/%s-%s.tar.gz", cookbook, version, cookbook, version);
  }

  public String buildCookbookDetailPath(final TokenMatcher.State state) {
    String cookbook = cookbook(state);

    return String.format("cookbooks/%s", cookbook);
  }

  public String buildCookbookDetailByVersionPath(final TokenMatcher.State state) {
    String cookbook = cookbook(state);
    String version = version(state);

    return String.format("cookbooks/%s/version/%s", cookbook, version);
  }

  public String buildCookbookListPath(final Parameters parameters) {
    checkNotNull(parameters);

    return "cookbooks/list" + "?" + Joiner.on("&").withKeyValueSeparator("=").join(parameters);
  }
}
