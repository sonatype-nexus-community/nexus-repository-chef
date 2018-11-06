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
package org.sonatype.nexus.repository.chef.internal.hosted;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.goodies.common.ComponentSupport;
import org.sonatype.nexus.repository.chef.internal.AssetKind;
import org.sonatype.nexus.repository.chef.internal.util.ChefPathUtils;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Handler;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher.State;

import static jline.internal.Preconditions.checkNotNull;
import static org.sonatype.nexus.repository.http.HttpResponses.notFound;
import static org.sonatype.nexus.repository.http.HttpResponses.ok;

/**
 * Chef Hosted Handlers
 *
 * @since 0.0.2
 */
@Named
@Singleton
public class HostedHandlers
    extends ComponentSupport
{
  private ChefPathUtils chefPathUtils;

  @Inject
  public HostedHandlers(final ChefPathUtils chefPathUtils) {
    this.chefPathUtils = checkNotNull(chefPathUtils);
  }

  final Handler get = context -> {
    String path;

    State state = context.getAttributes().require(TokenMatcher.State.class);
    path = chefPathUtils.buildCookbookPath(state);

    Content content = context.getRepository().facet(ChefHostedFacet.class).get(path);

    return (content != null) ? ok(content) : notFound();
  };

  final Handler upload = context -> {
    State state = context.getAttributes().require(TokenMatcher.State.class);
    String path = chefPathUtils.buildCookbookPath(state);
    AssetKind assetKind = context.getAttributes().require(AssetKind.class);
    context.getRepository().facet(ChefHostedFacet.class).upload(path, context.getRequest().getPayload(), assetKind);
    return ok();
  };
}
