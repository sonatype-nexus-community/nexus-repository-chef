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

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.chef.internal.AssetKind;
import org.sonatype.nexus.repository.chef.internal.ChefAssetAttributePopulator;
import org.sonatype.nexus.repository.chef.internal.metadata.ChefAttributes;
import org.sonatype.nexus.repository.chef.internal.util.ChefAttributeParser;
import org.sonatype.nexus.repository.chef.internal.util.ChefDataAccess;
import org.sonatype.nexus.repository.chef.internal.util.TgzParser;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.Bucket;
import org.sonatype.nexus.repository.storage.Component;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.StorageTx;
import org.sonatype.nexus.repository.storage.TempBlob;
import org.sonatype.nexus.repository.transaction.TransactionalStoreBlob;
import org.sonatype.nexus.repository.transaction.TransactionalTouchBlob;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.transaction.UnitOfWork;

import com.google.common.base.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.repository.chef.internal.AssetKind.*;
import static org.sonatype.nexus.repository.chef.internal.util.ChefDataAccess.HASH_ALGORITHMS;
import static org.sonatype.nexus.repository.storage.AssetEntityAdapter.P_ASSET_KIND;

@Named
public class ChefHostedFacetImpl
    extends FacetSupport
    implements ChefHostedFacet
{
  private final ChefDataAccess chefDataAccess;

  private final TgzParser tgzParser;

  private final ChefAttributeParser chefAttributeParser;

  private final ChefAssetAttributePopulator chefAssetAttributePopulator;

  @Inject
  public ChefHostedFacetImpl(final ChefDataAccess chefDataAccess,
                             final TgzParser tgzParser,
                             final ChefAttributeParser chefAttributeParser,
                             final ChefAssetAttributePopulator chefAssetAttributePopulator)
  {
    this.chefDataAccess = checkNotNull(chefDataAccess);
    this.tgzParser = checkNotNull(tgzParser);
    this.chefAttributeParser = checkNotNull(chefAttributeParser);
    this.chefAssetAttributePopulator = checkNotNull(chefAssetAttributePopulator);
  }

  @Nullable
  @Override
  @TransactionalTouchBlob
  public Content get(final String path) {
    checkNotNull(path);
    StorageTx tx = UnitOfWork.currentTx();

    Asset asset = chefDataAccess.findAsset(tx, tx.findBucket(getRepository()), path);
    if (asset == null) {
      return null;
    }
    if (asset.markAsDownloaded()) {
      tx.saveAsset(asset);
    }

    return chefDataAccess.toContent(asset, tx.requireBlob(asset.requireBlobRef()));
  }

  @Override
  public void upload(final String path, final Payload payload, final AssetKind assetKind) throws IOException {
    checkNotNull(path);
    checkNotNull(payload);

    if (assetKind != COOKBOOK) {
      throw new IllegalArgumentException("Unsupported assetKind: " + assetKind);
    }

    try (TempBlob tempBlob = facet(StorageFacet.class).createTempBlob(payload, HASH_ALGORITHMS)) {
      storeCookbook(path, tempBlob, payload);
    }
  }

  @TransactionalStoreBlob
  protected Content storeCookbook(final String assetPath,
                                  final Supplier<InputStream> chartContent,
                                  final Payload payload) throws IOException
  {
    StorageTx tx = UnitOfWork.currentTx();
    Bucket bucket = tx.findBucket(getRepository());

    Asset asset = createChartAsset(assetPath, tx, bucket, chartContent.get());

    return chefDataAccess.saveAsset(tx, asset, chartContent, payload);
  }

  private Asset createChartAsset(final String assetPath,
                                 final StorageTx tx,
                                 final Bucket bucket,
                                 final InputStream inputStream) throws IOException
  {
    ChefAttributes cookbook;
    try (InputStream in = inputStream) {
      cookbook = chefAttributeParser.getAttributesFromInputStream(in);
    }

    return findOrCreateAssetAndComponent(assetPath, tx, bucket, cookbook);
  }

  private Asset findOrCreateAssetAndComponent(final String assetPath,
                                              final StorageTx tx,
                                              final Bucket bucket,
                                              final ChefAttributes cookbook)
  {
    Asset asset = chefDataAccess.findAsset(tx, bucket, assetPath);
    if (asset == null) {
      Component component = findOrCreateComponent(tx, bucket, cookbook);
      asset = tx.createAsset(bucket, component);
      asset.name(assetPath);
      asset.formatAttributes().set(P_ASSET_KIND, COOKBOOK.name());
    }

    chefAssetAttributePopulator.populate(asset.formatAttributes(), cookbook);

    return asset;
  }

  private Component findOrCreateComponent(final StorageTx tx,
                                          final Bucket bucket,
                                          final ChefAttributes chart)
  {
    Component component = chefDataAccess.findComponent(tx, getRepository(), chart.getName(), chart.getVersion());
    if (component == null) {
      component = tx.createComponent(bucket, getRepository().getFormat())
          .name(chart.getName())
          .version(chart.getVersion());
      tx.saveComponent(component);
    }
    return component;
  }
}
