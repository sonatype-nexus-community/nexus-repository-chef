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
package org.sonatype.nexus.repository.chef.internal

import javax.inject.Inject
import javax.inject.Provider

import org.sonatype.nexus.repository.Format
import org.sonatype.nexus.repository.RecipeSupport
import org.sonatype.nexus.repository.Type
import org.sonatype.nexus.repository.attributes.AttributesFacet
import org.sonatype.nexus.repository.cache.NegativeCacheFacet
import org.sonatype.nexus.repository.cache.NegativeCacheHandler
import org.sonatype.nexus.repository.http.PartialFetchHandler
import org.sonatype.nexus.repository.httpclient.HttpClientFacet
import org.sonatype.nexus.repository.purge.PurgeUnusedFacet
import org.sonatype.nexus.repository.search.SearchFacet
import org.sonatype.nexus.repository.security.SecurityHandler
import org.sonatype.nexus.repository.storage.DefaultComponentMaintenanceImpl
import org.sonatype.nexus.repository.storage.StorageFacet
import org.sonatype.nexus.repository.storage.UnitOfWorkHandler
import org.sonatype.nexus.repository.view.ConfigurableViewFacet
import org.sonatype.nexus.repository.view.Context
import org.sonatype.nexus.repository.view.Matcher
import org.sonatype.nexus.repository.view.handlers.BrowseUnsupportedHandler
import org.sonatype.nexus.repository.view.handlers.ConditionalRequestHandler
import org.sonatype.nexus.repository.view.handlers.ContentHeadersHandler
import org.sonatype.nexus.repository.view.handlers.ExceptionHandler
import org.sonatype.nexus.repository.view.handlers.HandlerContributor
import org.sonatype.nexus.repository.view.handlers.TimingHandler
import org.sonatype.nexus.repository.view.matchers.ActionMatcher
import org.sonatype.nexus.repository.view.matchers.logic.LogicMatchers
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher
import org.sonatype.nexus.repository.chef.internal.security.ChefSecurityFacet

import static org.sonatype.nexus.repository.http.HttpMethods.GET
import static org.sonatype.nexus.repository.http.HttpMethods.HEAD

/**
 * Support for Chef recipes.
 *
 * @since 0.0.1
 */
abstract class ChefRecipeSupport
    extends RecipeSupport
{
  @Inject
  Provider<ChefSecurityFacet> securityFacet

  @Inject
  Provider<ConfigurableViewFacet> viewFacet

  @Inject
  Provider<StorageFacet> storageFacet

  @Inject
  Provider<SearchFacet> searchFacet

  @Inject
  Provider<AttributesFacet> attributesFacet

  @Inject
  ExceptionHandler exceptionHandler

  @Inject
  TimingHandler timingHandler

  @Inject
  SecurityHandler securityHandler

  @Inject
  PartialFetchHandler partialFetchHandler

  @Inject
  ConditionalRequestHandler conditionalRequestHandler

  @Inject
  ContentHeadersHandler contentHeadersHandler

  @Inject
  UnitOfWorkHandler unitOfWorkHandler

  @Inject
  BrowseUnsupportedHandler browseUnsupportedHandler

  @Inject
  HandlerContributor handlerContributor

  @Inject
  Provider<DefaultComponentMaintenanceImpl> componentMaintenanceFacet

  @Inject
  Provider<HttpClientFacet> httpClientFacet

  @Inject
  Provider<PurgeUnusedFacet> purgeUnusedFacet

  @Inject
  Provider<NegativeCacheFacet> negativeCacheFacet

  @Inject
  NegativeCacheHandler negativeCacheHandler

  protected ChefRecipeSupport(final Type type, final Format format) {
    super(type, format)
  }

  /**
   * Matcher for cookbook download.
   */
  static Matcher downloadMatcher() {
    builderTokenMatcherWithActionsAndAssetKind(
        '/api/v1/cookbooks/{cookbook:.+}/versions/{version:.+}/download', AssetKind.COOKBOOK, GET, HEAD
    )
  }

  /**
   * Matcher for cookbook details.
   */
  static Matcher cookbookDetailVersionMatcher() {
    builderTokenMatcherWithActionsAndAssetKind(
        '/api/v1/cookbooks/{cookbook:.+}/versions/{version:.+}', AssetKind.COOKBOOK_DETAIL_VERSION, GET, HEAD
    )
  }

  /**
   * Matcher for cookbook details.
   */
  static Matcher cookbookDetailsMatcher() {
    builderTokenMatcherWithActionsAndAssetKind(
        '/api/v1/cookbooks/{cookbook:.+}', AssetKind.COOKBOOK_DETAILS, GET, HEAD
    )
  }

  /**
   * Matcher for cookbook list.
   */
  static Matcher cookbooksMatcher() {
    builderTokenMatcherWithActionsAndAssetKind(
        '/api/v1/cookbooks', AssetKind.COOKBOOKS_LIST, GET, HEAD
    )
  }

  /**
   * Matcher for cookbook list.
   */
  static Matcher cookbookSearchMatcher() {
    builderTokenMatcherWithActionsAndAssetKind(
        '/api/v1/search', AssetKind.COOKBOOKS_SEARCH, GET, HEAD
    )
  }

  /**
   * Matcher for cookbook list.
   */
  static Matcher cookbookUniverseMatcher() {
    builderTokenMatcherWithActionsAndAssetKind(
        '/universe', AssetKind.COOKBOOKS_UNIVERSE, GET, HEAD
    )
  }

  /**
   * Method to allow building a Matcher for Chef routes
   * @param pattern
   * @param assetKind
   * @param httpMethods
   * @return Matcher
   */
  static Matcher builderTokenMatcherWithActionsAndAssetKind(final String pattern,
                                                            final AssetKind assetKind,
                                                            final String... httpMethods)
  {
    LogicMatchers.and(
        new ActionMatcher(httpMethods),
        new TokenMatcher(pattern),
        new Matcher() {
          @Override
          boolean matches(final Context context) {
            context.attributes.set(AssetKind.class, assetKind)
            return true
          }
        }
    )
  }
}
