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

import java.io.IOException;
import java.net.URISyntaxException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.repository.cache.CacheControllerHolder;
import org.sonatype.nexus.repository.cache.CacheInfo;
import org.sonatype.nexus.repository.chef.internal.AssetKind;
import org.sonatype.nexus.repository.chef.internal.metadata.ChefAttributes;
import org.sonatype.nexus.repository.chef.internal.util.ChefDataAccess;
import org.sonatype.nexus.repository.chef.internal.util.ChefPathUtils;
import org.sonatype.nexus.repository.chef.internal.util.CookBookApiAbsoluteUrlRemover;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.proxy.ProxyFacet;
import org.sonatype.nexus.repository.proxy.ProxyFacetSupport;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.Bucket;
import org.sonatype.nexus.repository.storage.Component;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.StorageTx;
import org.sonatype.nexus.repository.storage.TempBlob;
import org.sonatype.nexus.repository.transaction.TransactionalStoreBlob;
import org.sonatype.nexus.repository.transaction.TransactionalTouchBlob;
import org.sonatype.nexus.repository.transaction.TransactionalTouchMetadata;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Parameters;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;
import org.sonatype.nexus.transaction.UnitOfWork;
import org.sonatype.nexus.repository.chef.internal.util.ChefAttributeParser;

import com.google.common.base.Joiner;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.repository.storage.AssetEntityAdapter.P_ASSET_KIND;

/**
 * Chef {@link ProxyFacet} implementation.
 *
 * @since 0.0.1
 */
@Named
public class ChefProxyFacetImpl
    extends ProxyFacetSupport
{
  private ChefDataAccess chefDataAccess;

  private ChefPathUtils chefPathUtils;

  private ChefAttributeParser chefAttributeParser;

  private CookBookApiAbsoluteUrlRemover cookBookApiAbsoluteUrlRemover;

  @Inject
  public ChefProxyFacetImpl(final ChefDataAccess chefDataAccess,
                            final ChefPathUtils chefPathUtils,
                            final ChefAttributeParser chefAttributeParser,
                            final CookBookApiAbsoluteUrlRemover cookBookApiAbsoluteUrlRemover) {
    this.chefDataAccess = checkNotNull(chefDataAccess);
    this.chefPathUtils = checkNotNull(chefPathUtils);
    this.chefAttributeParser = checkNotNull(chefAttributeParser);
    this.cookBookApiAbsoluteUrlRemover = checkNotNull(cookBookApiAbsoluteUrlRemover);
  }

  // HACK: Workaround for known CGLIB issue, forces an Import-Package for org.sonatype.nexus.repository.config
  @Override
  protected void doValidate(final Configuration configuration) throws Exception {
    super.doValidate(configuration);
  }

  @Nullable
  @Override
  protected Content getCachedContent(final Context context) throws IOException {
    AssetKind assetKind = context.getAttributes().require(AssetKind.class);
    TokenMatcher.State matcherState = chefPathUtils.matcherState(context);
    switch (assetKind) {
      case COOKBOOK:
        return getAsset(chefPathUtils.buildCookbookPath(matcherState));
      case COOKBOOKS_LIST:
        return getAsset(chefPathUtils.buildCookbookListPath(context.getRequest().getParameters()));
      case COOKBOOK_DETAILS:
        return getAsset(chefPathUtils.buildCookbookDetailPath(matcherState));
      case COOKBOOK_DETAIL_VERSION:
        return getAsset(chefPathUtils.buildCookbookDetailByVersionPath(matcherState));
      // Let search requests pass through similar to other formats
      case COOKBOOKS_SEARCH:
        return null;
      default:
        throw new IllegalStateException("Received an invalid AssetKind of type: " + assetKind.name());
    }
  }

  @Override
  protected Content store(final Context context, final Content content) throws IOException {
    AssetKind assetKind = context.getAttributes().require(AssetKind.class);
    TokenMatcher.State matcherState = chefPathUtils.matcherState(context);
    switch(assetKind) {
      case COOKBOOK:
        return putCookbook(content,
            AssetKind.COOKBOOK,
            chefPathUtils.buildCookbookPath(matcherState));
      case COOKBOOK_DETAILS:
        return putMetadata(content,
            AssetKind.COOKBOOK_DETAILS,
            chefPathUtils.buildCookbookDetailPath(matcherState));
      case COOKBOOKS_LIST:
        return putMetadata(content,
            AssetKind.COOKBOOKS_LIST,
            chefPathUtils.buildCookbookListPath(context.getRequest().getParameters()));
      case COOKBOOK_DETAIL_VERSION:
        return putMetadata(content,
            AssetKind.COOKBOOK_DETAIL_VERSION,
            chefPathUtils.buildCookbookDetailByVersionPath(matcherState));
      case COOKBOOKS_SEARCH:
        return rewriteMetadata(content, assetKind);
      default:
        throw new IllegalStateException("Received an invalid AssetKind of type: " + assetKind.name());
    }
  }

  private Content rewriteMetadata(final Content content, final AssetKind assetKind) {
    try {
      if (assetKind.getCacheType().equals(CacheControllerHolder.METADATA)) {
        return cookBookApiAbsoluteUrlRemover.maybeRewriteCookbookApiResponseAbsoluteUrls(content, assetKind);
      }
      return content;
    }
    catch (IOException | URISyntaxException ex) {
      log.debug("Woops " + ex.toString());
      return content;
    }
  }

  private Content putMetadata(final Content content,
                              final AssetKind assetKind,
                              final String assetPath) throws IOException
  {
    StorageFacet storageFacet = facet(StorageFacet.class);
    Content rewrite = rewriteMetadata(content, assetKind);

    try (TempBlob tempBlob = storageFacet.createTempBlob(rewrite.openInputStream(), ChefDataAccess.HASH_ALGORITHMS)) {
      return doPutMetadata(tempBlob, rewrite, assetKind, assetPath);
    }
  }

  private Content putCookbook(final Content content,
                              final AssetKind assetKind,
                              final String assetPath)  throws IOException
  {
    StorageFacet storageFacet = facet(StorageFacet.class);

    try (TempBlob tempBlob = storageFacet.createTempBlob(content.openInputStream(), ChefDataAccess.HASH_ALGORITHMS)) {
      ChefAttributes chefAttributes = chefAttributeParser.getAttributesFromInputStream(tempBlob.get());
      return doPutCookbook(chefAttributes, tempBlob, content, assetKind, assetPath);
    }
  }

  @TransactionalStoreBlob
  protected Content doPutMetadata(final TempBlob tempBlob,
                                  final Payload payload,
                                  final AssetKind assetKind,
                                  final String assetPath) throws IOException
  {
    StorageTx tx = UnitOfWork.currentTx();
    Bucket bucket = tx.findBucket(getRepository());

    Asset asset = chefDataAccess.findAsset(tx, bucket, assetPath);
    if (asset == null) {
      asset = tx.createAsset(bucket, getRepository().getFormat());
      asset.name(assetPath);
      asset.formatAttributes().set(P_ASSET_KIND, assetKind.name());
    }

    return chefDataAccess.saveAsset(tx, asset, tempBlob, payload);
  }

  @TransactionalStoreBlob
  protected Content doPutCookbook(final ChefAttributes chefAttributes,
                                  final TempBlob tempBlob,
                                  final Payload payload,
                                  final AssetKind assetKind,
                                  final String assetPath) throws IOException
  {
    StorageTx tx = UnitOfWork.currentTx();
    Bucket bucket = tx.findBucket(getRepository());

    Component component = chefDataAccess.findComponent(tx,
        getRepository(),
        chefAttributes.getName(),
        chefAttributes.getVersion());

    if (component == null) {
      component = tx.createComponent(bucket, getRepository().getFormat())
          .name(chefAttributes.getName())
          .version(chefAttributes.getVersion());
    }
    tx.saveComponent(component);

    Asset asset = chefDataAccess.findAsset(tx, bucket, assetPath);
    if (asset == null) {
      asset = tx.createAsset(bucket, component);
      asset.name(assetPath);
      asset.formatAttributes().set(P_ASSET_KIND, assetKind.name());
    }

    return chefDataAccess.saveAsset(tx, asset, tempBlob, payload);
  }

  @TransactionalTouchBlob
  protected Content getAsset(final String name) {
    StorageTx tx = UnitOfWork.currentTx();

    Asset asset = chefDataAccess.findAsset(tx, tx.findBucket(getRepository()), name);
    if (asset == null) {
      return null;
    }
    if (asset.markAsDownloaded()) {
      tx.saveAsset(asset);
    }
    return chefDataAccess.toContent(asset, tx.requireBlob(asset.requireBlobRef()));
  }

  @Override
  protected void indicateVerified(final Context context, final Content content, final CacheInfo cacheInfo)
      throws IOException
  {
    setCacheInfo(content, cacheInfo);
  }

  @TransactionalTouchMetadata
  public void setCacheInfo(final Content content, final CacheInfo cacheInfo) throws IOException {
    StorageTx tx = UnitOfWork.currentTx();
    Asset asset = Content.findAsset(tx, tx.findBucket(getRepository()), content);
    if (asset == null) {
      log.debug(
          "Attempting to set cache info for non-existent Helm asset {}", content.getAttributes().require(Asset.class)
      );
      return;
    }
    log.debug("Updating cacheInfo of {} to {}", asset, cacheInfo);
    CacheInfo.applyToAsset(asset, cacheInfo);
    tx.saveAsset(asset);
  }

  @Override
  protected String getUrl(@Nonnull final Context context)
  {
    String url = context.getRequest().getPath().substring(1);
    AssetKind assetKind = context.getAttributes().require(AssetKind.class);
    switch (assetKind) {
      case COOKBOOKS_LIST:
      case COOKBOOKS_SEARCH:
        Parameters parameters = context.getRequest().getParameters();
        url += "?" + Joiner.on("&").withKeyValueSeparator("=").join(parameters);
        break;
      default:
        break;
    }

    return url;
  }
}
