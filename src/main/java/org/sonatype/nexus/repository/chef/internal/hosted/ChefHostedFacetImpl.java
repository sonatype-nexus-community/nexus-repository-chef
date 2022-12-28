package org.sonatype.nexus.repository.chef.internal.hosted;

import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.chef.internal.AssetKind;
import org.sonatype.nexus.repository.transaction.TransactionalStoreBlob;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Payload;

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
    public void rebuildMetadataJson(String path, Content content, AssetKind assetKind) throws IOException {
        log.trace(String.format("in rebuildMetadataJson, path=%s, AssetKind=%s", path, assetKind.name()));
        if (content != null) {
            content().putMetadata(path, content, assetKind);
        } else {
            content().deleteMetadata(path, assetKind);
        }
    }

    private ChefContentFacet content() {
        return getRepository().facet(ChefContentFacet.class);
    }
}
