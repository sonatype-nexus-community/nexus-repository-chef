package org.sonatype.nexus.repository.chef.internal.hosted;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.sonatype.goodies.common.Loggers;
import org.sonatype.nexus.blobstore.api.Blob;
import org.sonatype.nexus.common.hash.HashAlgorithm;
import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.Format;
import org.sonatype.nexus.repository.chef.internal.ChefAttributesExtractor;
import org.sonatype.nexus.repository.chef.internal.ChefAttributes;
import org.sonatype.nexus.repository.chef.internal.ChefFormat;
import org.sonatype.nexus.repository.chef.internal.AssetKind;
import org.sonatype.nexus.repository.chef.internal.util.AttributesHelper;
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

@Named
public class ChefContentFacetImpl
        extends FacetSupport
        implements ChefContentFacet {
    protected static final Logger log = Preconditions.checkNotNull(Loggers.getLogger(ChefContentFacetImpl.class));

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
            return doPutContent(tempBlob, payload, AssetKind.COOKBOOK);
        }
    }

    @TransactionalStoreBlob
    protected Content doPutContent(final TempBlob tempBlob,
                                   final Payload payload,
                                   final AssetKind assetKind
    )
            throws IOException {
        StorageTx tx = UnitOfWork.currentTx();
        ChefAttributes cookbookAttributes = chefAttributesExtractor.getAttributes(tempBlob);

        final String path = ChefPathUtils.buildTarballPath(cookbookAttributes.getName(), cookbookAttributes.getVersion());

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

        try {
            asset.formatAttributes().clear();
            asset.formatAttributes().set(P_ASSET_KIND, AttributesHelper.standardizeAttributeValue(assetKind.name()));
            cookbookAttributes.getAttributesMap().forEach((key, value) -> asset.formatAttributes().set(key, value));
        } catch (Exception e) {
            log.warn("Error extracting format attributes for {}, skipping", path, e);
        }

        tx.saveAsset(asset);

        return toContent(asset, assetBlob.getBlob());
    }

    @TransactionalStoreMetadata
    public Asset getOrCreateAsset(final String path,
                                  final String name,
                                  final String version) {
        log.debug(String.format("in getOrCreateAsset. path=%s, name=%s, version=%s", path, name, version));
        final StorageTx tx = UnitOfWork.currentTx();
        final Bucket bucket = tx.findBucket(getRepository());

        Component component = findComponent(tx, name, version);
        if (component == null) {
            log.debug("findComponent returned null");
            component = tx.createComponent(bucket, format).name(name).version(version);
            tx.saveComponent(component);
        }

        Asset asset = findAsset(tx, path);
        if (asset == null) {
            log.debug("findAsset returned null");
            asset = tx.createAsset(bucket, component);
            asset.name(path);
        }

        asset.markAsDownloaded();

        return asset;
    }

    @Nullable
    private Asset findAsset(final StorageTx tx, final String path) {
        log.trace(String.format("in findAsset. path=%s", path));
        return tx.findAssetWithProperty(ChefAttributes.P_NAME, path, tx.findBucket(getRepository()));
    }

    @Nullable
    private Component findComponent(final StorageTx tx, final String name, final String version) {
        log.trace(String.format("in findComponent. name=%s, version=%s", name, version));
        Iterable<Component> components = tx.findComponents(Query.builder()
                        .where(ChefAttributes.P_NAME).eq(name)
                        .and(ChefAttributes.P_VERSION).eq(version)
                        .build(),
                singletonList(getRepository()));
        if (components.iterator().hasNext()) {
            return components.iterator().next();
        }
        return null;
    }

    private Content toContent(final Asset asset, final Blob blob) {
        final Content content = new Content(new BlobPayload(blob, asset.requireContentType()));
        Content.extractFromAsset(asset, hashAlgorithms, content.getAttributes());
        return content;
    }
}
