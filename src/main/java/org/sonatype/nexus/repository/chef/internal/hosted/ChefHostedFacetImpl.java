package org.sonatype.nexus.repository.chef.internal.hosted;

import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.chef.internal.AssetKind;
import org.sonatype.nexus.repository.transaction.TransactionalStoreBlob;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.chef.internal.util.ChefPathUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;

@Named
public class ChefHostedFacetImpl extends FacetSupport implements ChefHostedFacet {

    @Inject
    public ChefHostedFacetImpl() {
    }

    @Override
    @TransactionalStoreBlob
    public void upload(final Payload payload) throws IOException {
        content().put(payload);
    }

    @Override
    public Content get(final String path) throws IOException {
        return content().get(path);
    }

    @Override
    @TransactionalStoreBlob
    public void rebuildUniverseJson(Content content) throws IOException {
        log.trace("in rebuildUniverseJson");
        content().putMetadata(ChefPathUtils.buildInternalUniverseJsonPath(), content, AssetKind.UNIVERSE_JSON);
    }

    @Override
    @TransactionalStoreBlob
    public void rebuildCookbookInfoJson(String path, Content content) throws IOException {
        log.trace("in rebuildCookbookInfoJson");
        content().putMetadata(path, content, AssetKind.COOKBOOK_INFO);
    }

    @Override
    @TransactionalStoreBlob
    public void rebuildCookbookVersionInfoJson(String path, Content content) throws IOException {
        log.trace("in rebuildCookbookVersionInfoJson");
        content().putMetadata(path, content, AssetKind.COOKBOOK_VERSION_INFO);
    }

    private ChefContentFacet content() {
        return getRepository().facet(ChefContentFacet.class);
    }
}
