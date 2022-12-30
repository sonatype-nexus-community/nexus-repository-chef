package org.sonatype.nexus.repository.chef.internal.hosted;

import org.sonatype.nexus.blobstore.api.Blob;
import org.sonatype.nexus.common.hash.HashAlgorithm;
import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.Format;
import org.sonatype.nexus.repository.chef.internal.ChefAttributesExtractor;
import org.sonatype.nexus.repository.chef.internal.ChefAttributes;
import org.sonatype.nexus.repository.chef.internal.ChefFormat;
import org.sonatype.nexus.repository.chef.internal.AssetKind;
import org.sonatype.nexus.repository.chef.internal.util.ChefWritePolicySelector;
import org.sonatype.nexus.repository.chef.internal.util.ChefPathUtils;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.StorageTx;
import org.sonatype.nexus.repository.storage.Component;
import org.sonatype.nexus.repository.storage.Bucket;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.AssetBlob;
import org.sonatype.nexus.repository.storage.Query;
import org.sonatype.nexus.repository.transaction.TransactionalStoreBlob;
import org.sonatype.nexus.repository.transaction.TransactionalStoreMetadata;
import org.sonatype.nexus.repository.transaction.TransactionalTouchBlob;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.payloads.BlobPayload;
import org.sonatype.nexus.repository.view.payloads.TempBlob;
import org.sonatype.nexus.transaction.UnitOfWork;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Collections.singletonList;
import static org.sonatype.nexus.common.hash.HashAlgorithm.MD5;
import static org.sonatype.nexus.common.hash.HashAlgorithm.SHA1;
import static org.sonatype.nexus.common.hash.HashAlgorithm.SHA256;
import static org.sonatype.nexus.repository.storage.AssetEntityAdapter.P_ASSET_KIND;
import static org.sonatype.nexus.repository.storage.ComponentEntityAdapter.P_VERSION;
import static org.sonatype.nexus.repository.storage.ComponentEntityAdapter.P_NAME;

@Named
public class ChefContentFacetImpl
        extends FacetSupport
        implements ChefContentFacet {
    private static final List<HashAlgorithm> hashAlgorithms = Arrays.asList(MD5, SHA1, SHA256);

    private final Format format;

    private final ChefAttributesExtractor chefAttributesExtractor;

    @Inject
    public ChefContentFacetImpl(@Named(ChefFormat.NAME) final Format format,
                                final ChefAttributesExtractor chefAttributesExtractor) {
        this.format = checkNotNull(format);
        this.chefAttributesExtractor = checkNotNull(chefAttributesExtractor);
    }

    @Override
    protected void doInit(final Configuration configuration) throws Exception {
        super.doInit(configuration);
        getRepository().facet(StorageFacet.class).registerWritePolicySelector(new ChefWritePolicySelector());
    }

    @Nullable
    @Override
    @TransactionalTouchBlob
    public Content get(final String path) throws IOException {
        StorageTx tx = UnitOfWork.currentTx();

        final Asset asset = findAsset(tx, path);
        if (asset == null) {
            return null;
        }

        final Blob blob = tx.requireBlob(asset.requireBlobRef());
        return toContent(asset, blob);
    }

    @Override
    public Content put(final Payload payload) throws IOException {
        StorageFacet storageFacet = facet(StorageFacet.class);
        try (TempBlob tempBlob = storageFacet.createTempBlob(payload, hashAlgorithms)) {
            return doPutCookbook(tempBlob, payload);
        }
    }

    @Override
    public void putMetadata(final String path, final Payload payload, final AssetKind assetKind) throws IOException {
        StorageFacet storageFacet = facet(StorageFacet.class);
        try (TempBlob tempBlob = storageFacet.createTempBlob(payload, hashAlgorithms)) {
            switch (assetKind) {
                case UNIVERSE_JSON:
                case COOKBOOK_VERSION_INFO:
                case COOKBOOK_INFO:
                    doPutMetadata(path, tempBlob, payload, assetKind);
                    break;
                default:
                    throw new IllegalStateException("Unexpected asset kind: " + assetKind);
            }
        }
    }

    @Override
    public void deleteMetadata(final String path, final AssetKind assetKind) {
        switch (assetKind) {
            case UNIVERSE_JSON:
            case COOKBOOK_VERSION_INFO:
            case COOKBOOK_INFO:
                doDeleteMetadata(path, assetKind);
                break;
            default:
                throw new IllegalStateException("Unexpected asset kind: " + assetKind);
        }
    }

    @TransactionalStoreBlob
    protected void doDeleteMetadata(final String path, final AssetKind assetKind) {
        log.trace("in doDeleteMetadata");
        StorageTx tx = UnitOfWork.currentTx();
        Asset asset = findAsset(tx, path);
        if (asset != null) {
            String foundAssetKind = asset.formatAttributes().get(P_ASSET_KIND, String.class, "");
            if (foundAssetKind.equals(assetKind.name())) {
                tx.deleteAsset(asset);
                log.debug(String.format("Deleted metadata with path=%s and AssetKind=%s", path, assetKind.name()));
            } else {
                log.warn(String.format("Tried to delete metadata with path=%s and AssetKind=%s, but its actual AssetKind was %s", path, assetKind.name(), foundAssetKind));
            }
        } else {
            log.warn(String.format("Tried to delete metadata with path=%s and AssetKind=%s, but it was not found", path, assetKind.name()));
        }
    }

    @TransactionalStoreBlob
    protected void doPutMetadata(final String path,
                                 final TempBlob tempBlob,
                                 final Payload payload,
                                 final AssetKind assetKind)
            throws IOException {
        log.trace("in doPutMetadata");
        StorageTx tx = UnitOfWork.currentTx();
        Asset asset = getOrCreateAsset(path);
        asset.formatAttributes().set(P_ASSET_KIND, assetKind.name());

        if (payload instanceof Content) {
            Content.applyToAsset(asset, Content.maintainLastModified(asset, ((Content) payload).getAttributes()));
        }

        tx.setBlob(
                asset,
                path,
                tempBlob,
                null,
                payload.getContentType(),
                false
        );

        tx.saveAsset(asset);
    }

    @TransactionalStoreBlob
    protected Content doPutCookbook(final TempBlob tempBlob, final Payload payload) throws IOException {
        StorageTx tx = UnitOfWork.currentTx();
        ChefAttributes cookbookAttributes = chefAttributesExtractor.getAttributes(tempBlob);
        final String path = ChefPathUtils.buildInternalTarballPath(cookbookAttributes.getName(), cookbookAttributes.getVersion());

        Asset asset = getOrCreateAsset(path, cookbookAttributes.getName(), cookbookAttributes.getVersion());

        if (payload instanceof Content) {
            Content.applyToAsset(asset, Content.maintainLastModified(asset, ((Content) payload).getAttributes()));
        }

        AssetBlob assetBlob = tx.setBlob(
                asset,
                path,
                tempBlob,
                null,
                payload.getContentType(),
                false
        );

        asset.formatAttributes().clear();
        asset.formatAttributes().set(P_ASSET_KIND, AssetKind.COOKBOOK.name());
        cookbookAttributes.getAttributesMap().forEach((key, value) -> asset.formatAttributes().set(key, value));

        tx.saveAsset(asset);

        return toContent(asset, assetBlob.getBlob());
    }

    @TransactionalStoreMetadata
    public Asset getOrCreateAsset(final String path,
                                  final String name,
                                  final String version) {
        log.trace(String.format("in getOrCreateAsset. path=%s, name=%s, version=%s", path, name, version));
        final StorageTx tx = UnitOfWork.currentTx();
        final Bucket bucket = tx.findBucket(getRepository());

        Component component = findComponent(tx, name, version);
        if (component == null) {
            log.trace("findComponent returned null");
            component = tx.createComponent(bucket, format).name(name).version(version);
            tx.saveComponent(component);
        }

        Asset asset = findAsset(tx, path);
        if (asset == null) {
            log.trace("findAsset returned null");
            asset = tx.createAsset(bucket, component);
            asset.name(path);
        }

        asset.markAsDownloaded();

        return asset;
    }

    @TransactionalStoreMetadata
    public Asset getOrCreateAsset(final String path) {
        final StorageTx tx = UnitOfWork.currentTx();
        final Bucket bucket = tx.findBucket(getRepository());

        Asset asset = findAsset(tx, path);
        if (asset == null) {
            log.trace("findAsset returned null");
            asset = tx.createAsset(bucket, format);
            asset.name(path);
        }

        asset.markAsDownloaded();

        return asset;
    }


    @Nullable
    private Asset findAsset(final StorageTx tx, final String path) {
        log.trace(String.format("in findAsset. path=%s", path));
        return tx.findAssetWithProperty(P_NAME, path, tx.findBucket(getRepository()));
    }

    @Nullable
    private Component findComponent(final StorageTx tx, final String name, final String version) {
        log.trace(String.format("in findComponent. name=%s, version=%s", name, version));
        Iterable<Component> components = tx.findComponents(Query.builder()
                        .where(P_NAME).eq(name)
                        .and(P_VERSION).eq(version)
                        .build(),
                singletonList(getRepository()));
        if (components.iterator().hasNext()) {
            log.trace("in findComponent. Found component.");
            return components.iterator().next();
        }
        log.trace("in findComponent. Did not find component.");
        return null;
    }

    private Content toContent(final Asset asset, final Blob blob) {
        final Content content = new Content(new BlobPayload(blob, asset.requireContentType()));
        Content.extractFromAsset(asset, hashAlgorithms, content.getAttributes());
        return content;
    }
}
