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

    // Internal paths to chef artifacts
    private static final String INTERNAL_COOKBOOK_TARBALL_PATH = "%s/%s/%s-%s.tgz";
    private static final String INTERNAL_UNIVERSE_JSON_PATH = "/universe.json";
    private static final String INTERNAL_COOKBOOK_INFO_JSON_PATH = "%s/cookbook_info.json";
    private static final String INTERNAL_COOKBOOK_VERSION_INFO_JSON_PATH = "%s/%s/cookbook_version_info.json";

    // Supermarket API urls
    private static final String SUPERMARKET_UNIVERSE_PATH = "/universe";
    private static final String SUPERMARKET_COOKBOOK_VERSION_PATH = "/api/v1/cookbooks/%s/versions/%s";
    private static final String SUPERMARKET_COOKBOOK_INFO_PATH = "/api/v1/cookbooks/%s";
    private static final String SUPERMARKET_COOKBOOK_VERSION_DOWNLOAD_PATH = "/api/v1/cookbooks/%s/versions/%s/download";

    private static final String PLACEHOLDER_NAME = "placeholder_cookbook_name";
    private static final String PLACEHOLDER_VERSION = "placeholder_cookbook_version";


    public static String buildInternalTarballPath(final String cookbookName, final String cookbookVersion) {
        return String.format(INTERNAL_COOKBOOK_TARBALL_PATH,
                cookbookName, cookbookVersion, cookbookName, cookbookVersion);
    }

    public static String buildInternalPlaceholderPathForPermissionCheck() {
        return buildInternalTarballPath(PLACEHOLDER_NAME, PLACEHOLDER_VERSION);
    }

    public static String buildInternalUniverseJsonPath() {
        return INTERNAL_UNIVERSE_JSON_PATH;
    }

    public static String buildInternalTarballPath(final Context context) {
        return buildInternalTarballPath(
                getCookbookNameToken(context),
                getCookbookVersionToken(context)
        );
    }

    public static String buildInternalCookbookInfoJsonPath(final Context context) {
        return buildInternalCookbookInfoJsonPath(
                getCookbookNameToken(context)
        );
    }

    public static String buildInternalCookbookVersionInfoJsonPath(final Context context) {
        return buildInternalCookbookVersionInfoJsonPath(
                getCookbookNameToken(context),
                getCookbookVersionToken(context)
        );
    }

    public static String buildInternalCookbookInfoJsonPath(final String cookbookName) {
        return String.format(INTERNAL_COOKBOOK_INFO_JSON_PATH, cookbookName);
    }

    public static String buildInternalCookbookVersionInfoJsonPath(final String cookbookName, final String cookbookVersion) {
        return String.format(INTERNAL_COOKBOOK_VERSION_INFO_JSON_PATH, cookbookName, cookbookVersion);
    }

    public static String buildSupermarketCookbookVersionUrl(final String baseUrl, final String cookbookName, final String cookbookVersion) {
        return baseUrl.concat(String.format(SUPERMARKET_COOKBOOK_VERSION_PATH, cookbookName, cookbookVersion));
    }

    public static String buildSupermarketCookbookUrl(final String baseUrl, final String cookbookName) {
        return baseUrl.concat(String.format(SUPERMARKET_COOKBOOK_INFO_PATH, cookbookName));
    }

    public static String buildSupermarketCookbookVersionDownloadUrl(final String baseUrl, final String cookbookName, final String cookbookVersion) {
        return baseUrl.concat(String.format(SUPERMARKET_COOKBOOK_VERSION_DOWNLOAD_PATH, cookbookName, cookbookVersion));
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
}
