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
package org.sonatype.nexus.repository.chef.internal.hosted

import javax.annotation.Nonnull
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Singleton

import org.sonatype.nexus.repository.Format
import org.sonatype.nexus.repository.Repository
import org.sonatype.nexus.repository.Type
import org.sonatype.nexus.repository.chef.internal.AssetKind
import org.sonatype.nexus.repository.chef.internal.ChefFormat
import org.sonatype.nexus.repository.chef.internal.ChefRecipeSupport
import org.sonatype.nexus.repository.http.HttpHandlers
import org.sonatype.nexus.repository.types.HostedType
import org.sonatype.nexus.repository.view.ConfigurableViewFacet
import org.sonatype.nexus.repository.view.Context
import org.sonatype.nexus.repository.view.Matcher
import org.sonatype.nexus.repository.view.Route
import org.sonatype.nexus.repository.view.Router
import org.sonatype.nexus.repository.view.ViewFacet
import org.sonatype.nexus.repository.view.handlers.BrowseUnsupportedHandler
import org.sonatype.nexus.repository.view.matchers.ActionMatcher
import org.sonatype.nexus.repository.view.matchers.logic.LogicMatchers
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher

import static org.sonatype.nexus.repository.chef.internal.AssetKind.*
import static org.sonatype.nexus.repository.http.HttpMethods.PUT

/**
 * Chef Hosted Recipe
 *
 * @since 0.0.2
 */
@Named(ChefHostedRecipe.NAME)
@Singleton
class ChefHostedRecipe
    extends ChefRecipeSupport
{
  public static final String NAME = 'chef-hosted'

  @Inject
  HostedHandlers hostedHandlers

  @Inject
  Provider<ChefHostedFacetImpl> hostedFacet

  @Inject
  ChefHostedRecipe(@Named(HostedType.NAME) final Type type, @Named(ChefFormat.NAME) final Format format) {
    super(type, format)
  }

  @Override
  void apply(@Nonnull final Repository repository) throws Exception {
    repository.attach(securityFacet.get())
    repository.attach(configure(viewFacet.get()))
    repository.attach(httpClientFacet.get())
    repository.attach(componentMaintenanceFacet.get())
    repository.attach(negativeCacheFacet.get())
    repository.attach(storageFacet.get())
    repository.attach(hostedFacet.get())
    repository.attach(searchFacet.get())
    repository.attach(attributesFacet.get())
  }

  /**
   * Configure {@link ViewFacet}.
   */
  private ViewFacet configure(final ConfigurableViewFacet facet) {
    Router.Builder builder = new Router.Builder()

    [downloadMatcher()].each { matcher ->
      builder.route(new Route.Builder().matcher(matcher)
          .handler(timingHandler)
          .handler(securityHandler)
          .handler(exceptionHandler)
          .handler(handlerContributor)
          .handler(negativeCacheHandler)
          .handler(partialFetchHandler)
          .handler(contentHeadersHandler)
          .handler(unitOfWorkHandler)
          .handler(hostedHandlers.get)
          .create())
    }

    builder.route(new Route.Builder().matcher(cookbookUploadMatcher())
        .handler(timingHandler)
        .handler(securityHandler)
        .handler(exceptionHandler)
        .handler(handlerContributor)
        .handler(conditionalRequestHandler)
        .handler(partialFetchHandler)
        .handler(contentHeadersHandler)
        .handler(unitOfWorkHandler)
        .handler(hostedHandlers.upload)
        .create())

    builder.route(new Route.Builder()
        .matcher(BrowseUnsupportedHandler.MATCHER)
        .handler(browseUnsupportedHandler)
        .create())

    builder.defaultHandlers(HttpHandlers.notFound())

    facet.configure(builder.create())

    return facet
  }

  static Matcher cookbookUploadMatcher() {
    LogicMatchers.and(
        new ActionMatcher(PUT),
        tokenMatcherForExtensionAndName('tar.gz'),
        new Matcher() {
          @Override
          boolean matches(final Context context) {
            context.attributes.set(AssetKind.class, COOKBOOK)
            return true
          }
        }
    )
  }

  static TokenMatcher tokenMatcherForExtensionAndName(final String extension, final String filename = '.+') {
    new TokenMatcher("/{filename:${filename}}.{extension:${extension}}")
  }
}
