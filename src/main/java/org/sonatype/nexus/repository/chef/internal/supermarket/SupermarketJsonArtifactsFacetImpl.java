package org.sonatype.nexus.repository.chef.internal.supermarket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.sonatype.goodies.common.Loggers;
import org.sonatype.nexus.common.event.EventManager;
import org.sonatype.nexus.common.stateguard.Guarded;
import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.chef.internal.*;
import org.sonatype.nexus.repository.chef.internal.hosted.ChefHostedFacet;
import org.sonatype.nexus.repository.chef.internal.supermarket.model.UniverseJsonCookbookVersionModel;
import org.sonatype.nexus.repository.chef.internal.util.AttributesHelper;
import org.sonatype.nexus.repository.chef.internal.util.ChefPathUtils;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.AssetCreatedEvent;
import org.sonatype.nexus.repository.storage.AssetDeletedEvent;
import org.sonatype.nexus.repository.storage.AssetEvent;
import org.sonatype.nexus.repository.storage.AssetUpdatedEvent;
import org.sonatype.nexus.repository.storage.Query;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.StorageTx;
import org.sonatype.nexus.repository.transaction.TransactionalStoreBlob;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.ContentTypes;
import org.sonatype.nexus.repository.view.payloads.StringPayload;
import org.sonatype.nexus.transaction.UnitOfWork;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.sonatype.nexus.common.stateguard.StateGuardLifecycleSupport.State.STARTED;
import static org.sonatype.nexus.repository.storage.AssetEntityAdapter.P_ASSET_KIND;

@Named
public class SupermarketJsonArtifactsFacetImpl extends FacetSupport implements SupermarketJsonArtifactsFacet {

    protected static final Logger log = Preconditions.checkNotNull(Loggers.getLogger(SupermarketJsonArtifactsFacetImpl.class));

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String BASE_LOCATION_TYPE = "opscode";

    private final EventManager eventManager;

    @Inject
    public SupermarketJsonArtifactsFacetImpl(final EventManager eventManager) {
        this.eventManager = requireNonNull(eventManager);
    }

    @Subscribe
    @Guarded(by = STARTED)
    @AllowConcurrentEvents
    public void on(final AssetDeletedEvent deleted) {
        log.debug("on AssetDeletedEvent, event: " + deleted.toString());
        if (matchesRepository(deleted) && isEventRelevant(deleted)) {
            invalidateMetadata(deleted);
        }
    }

    @Subscribe
    @Guarded(by = STARTED)
    @AllowConcurrentEvents
    public void on(final AssetCreatedEvent created) {
        log.debug("on AssetCreatedEvent, event: " + created.toString());
        if (matchesRepository(created) && isEventRelevant(created)) {
            invalidateMetadata(created);
        }
    }

    @Subscribe
    @Guarded(by = STARTED)
    @AllowConcurrentEvents
    public void on(final AssetUpdatedEvent updated) {
        log.debug("on AssetUpdatedEvent, event: " + updated.toString());
        if (matchesRepository(updated) && isEventRelevant(updated)) {
            invalidateMetadata(updated);
        }
    }

    @Subscribe
    @Guarded(by = STARTED)
    public void on(final ChefHostedMetadataInvalidationEvent event) throws IOException {
        log.debug("on ChefHostedMetadataInvalidationEvent, event: " + event.toString());
        if (getRepository().getName().equals(event.getRepositoryName())) {
            ChefHostedFacet hostedFacet = getRepository().facet(ChefHostedFacet.class);
            UnitOfWork.begin(getRepository().facet(StorageFacet.class).txSupplier());
            try {
                String cookbookName = event.getCookbookName();
                String cookbookVersion = event.getCookbookVersion();
                hostedFacet.rebuildUniverseJson(buildUniverseJson());
                hostedFacet.rebuildCookbookInfoJson(
                        ChefPathUtils.buildInternalCookbookInfoJsonPath(cookbookName),
                        buildCookbookInfoJson(cookbookName));
                hostedFacet.rebuildCookbookVersionInfoJson(
                        ChefPathUtils.buildInternalCookbookVersionInfoJsonPath(cookbookName, cookbookVersion),
                        buildCookbookVersionInfoJson(cookbookName, cookbookVersion));
            } finally {
                UnitOfWork.end();
            }
        }
    }

    @TransactionalStoreBlob
    public Content buildCookbookVersionInfoJson(String cookbookName, String cookbookVersion) throws JsonProcessingException {
        StorageTx tx = UnitOfWork.currentTx();

        CookbookVersionInfoJsonModelBuilder builder = null;
        String baseNexusUrl = getRepository().getUrl();

        // Find the artifact corresponding to this cookbook name and version
        for (Asset asset : tx.findAssets(
                Query.builder()
                        .where(String.format("attributes.%s.%s", ChefFormat.NAME, P_ASSET_KIND))
                        .eq(AssetKind.COOKBOOK.name())
                        .and(String.format("attributes.%s.%s", ChefFormat.NAME, ChefAttributes.P_NAME))
                        .eq(cookbookName)
                        .and(String.format("attributes.%s.%s", ChefFormat.NAME, ChefAttributes.P_VERSION))
                        .eq(cookbookVersion)
                        .build(),
                singletonList(getRepository()))) {
            log.debug(String.format("Asset is a cookbook with this name (%s) and this version (%s)", cookbookName, cookbookVersion));
            if (builder == null) {
                builder = new CookbookVersionInfoJsonModelBuilder(asset, baseNexusUrl);
            } else {
                log.warn(String.format("Several cookbooks found with the same name=%s and version=%s", cookbookName, cookbookVersion));
            }
        }
        log.debug("Returning cookbook info json:\n\n" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(builder.build()) + "\n\n");
        return new Content(new StringPayload(mapper.writeValueAsString(builder.build()),
                ContentTypes.APPLICATION_JSON));
    }

    @TransactionalStoreBlob
    public Content buildUniverseJson() throws JsonProcessingException {
        String baseNexusUrl = getRepository().getUrl();
        String locationPath = baseNexusUrl.concat("/api/v1");
        UniverseJsonModelBuilder builder = new UniverseJsonModelBuilder();

        StorageTx tx = UnitOfWork.currentTx();

        // Iterate over all cookbooks stored in the repo, adding them to the builder
        for (Asset asset : tx.findAssets(
                Query.builder()
                        .where(String.format("attributes.%s.%s", ChefFormat.NAME, P_ASSET_KIND))
                        .eq(AssetKind.COOKBOOK.name())
                        .build(),
                singletonList(getRepository()))) {
            log.trace(String.format("Found asset kind=%s, formatAttributes=%s, asset=%s", asset.formatAttributes().get(P_ASSET_KIND, String.class), asset.formatAttributes().toString(), asset));
            String cookbookName = asset.formatAttributes().get(ChefAttributes.P_NAME, String.class);
            String cookbookVersion = asset.formatAttributes().get(ChefAttributes.P_VERSION, String.class);
            Map<String, String> dependencies = AttributesHelper.parseStringIntoMap(asset.formatAttributes().get(ChefAttributes.P_DEPENDS, String.class, null));
            String downloadPath = ChefPathUtils.buildSupermarketCookbookVersionDownloadUrl(baseNexusUrl, cookbookName, cookbookVersion);
            UniverseJsonCookbookVersionModel versionModel = new UniverseJsonCookbookVersionModel(
                    BASE_LOCATION_TYPE,
                    locationPath,
                    downloadPath,
                    dependencies
            );
            builder.addCookbook(cookbookName, cookbookVersion, versionModel);
        }
        log.trace("Returning universe json:\n\n" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(builder.build()) + "\n\n");
        return new Content(new StringPayload(mapper.writeValueAsString(builder.build()),
                ContentTypes.APPLICATION_JSON));
    }

    @TransactionalStoreBlob
    public Content buildCookbookInfoJson(String cookbookName) throws JsonProcessingException {
        CookbookInfoJsonModelBuilder builder = new CookbookInfoJsonModelBuilder(getRepository().getUrl());
        StorageTx tx = UnitOfWork.currentTx();

        // Iterate over all versions of this cookbook
        for (Asset asset : tx.findAssets(
                Query.builder()
                        .where(String.format("attributes.%s.%s", ChefFormat.NAME, ChefAttributes.P_NAME))
                        .eq(cookbookName)
                        .and(String.format("attributes.%s.%s", ChefFormat.NAME, P_ASSET_KIND))
                        .eq(AssetKind.COOKBOOK.name())
                        .build(),
                singletonList(getRepository()))) {
            log.trace(String.format("Found asset kind=%s, formatAttributes=%s, asset=%s", asset.formatAttributes().get(P_ASSET_KIND, String.class), asset.formatAttributes().toString(), asset));
            builder.addVersion(asset);
        }
        log.trace("Returning cookbook info json:\n\n" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(builder.build()) + "\n\n");
        return new Content(new StringPayload(mapper.writeValueAsString(builder.build()),
                ContentTypes.APPLICATION_JSON));
    }

    private void invalidateMetadata(final AssetEvent assetEvent) {
        log.debug("in invalidateMetadata, event: " + assetEvent.toString());
        Asset asset = assetEvent.getAsset();
        String cookbookName = asset.formatAttributes().require(ChefAttributes.P_NAME, String.class);
        String cookbookVersion = asset.formatAttributes().require(ChefAttributes.P_VERSION, String.class);
        eventManager.post(new ChefHostedMetadataInvalidationEvent(getRepository().getName(), cookbookName, cookbookVersion));
    }

    private boolean isEventRelevant(final AssetEvent event) {
        return AssetKind.COOKBOOK.name().equals(event.getAsset().formatAttributes().get(P_ASSET_KIND, String.class));
    }

    private boolean matchesRepository(final AssetEvent assetEvent) {
        return assetEvent.isLocal() && getRepository().getName().equals(assetEvent.getRepositoryName());
    }
}
