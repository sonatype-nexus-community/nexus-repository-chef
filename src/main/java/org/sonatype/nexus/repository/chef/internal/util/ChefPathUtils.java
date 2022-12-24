/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2022-present Sonatype, Inc.
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

import org.sonatype.nexus.repository.chef.internal.hosted.ChefHostedRecipe;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ChefPathUtils {
    private static final String COOKBOOK_TARBALL_PATH = "%s/%s/%s-%s.tgz";
    private static final String PLACEHOLDER_NAME = "placeholder_cookbook_name";
    private static final String PLACEHOLDER_VERSION = "placeholder_cookbook_version";

    public static String buildTarballPath(final Context context) {
        return buildTarballPath(
                getCookbookNameToken(context),
                getCookbookVersionToken(context)
        );
    }

    public static String buildTarballPath(final String cookbookName, final String cookbookVersion) {
        return String.format(COOKBOOK_TARBALL_PATH, cookbookName, cookbookVersion, cookbookName, cookbookVersion);
    }

    private ChefPathUtils() {
        // empty
    }

    private static String getCookbookVersionToken(final Context context) {
        TokenMatcher.State state = context.getAttributes().require(TokenMatcher.State.class);
        return checkNotNull(state.getTokens().get(ChefHostedRecipe.VERSION_TOKEN));
    }

    private static String getCookbookNameToken(final Context context) {
        TokenMatcher.State state = context.getAttributes().require(TokenMatcher.State.class);
        return checkNotNull(state.getTokens().get(ChefHostedRecipe.NAME_TOKEN));
    }

    public static String buildPlaceholderPathForPermissionCheck() {
        return buildTarballPath(PLACEHOLDER_NAME, PLACEHOLDER_VERSION);
    }
}
