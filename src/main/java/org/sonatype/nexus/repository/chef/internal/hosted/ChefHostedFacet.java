package org.sonatype.nexus.repository.chef.internal.hosted;

import org.sonatype.nexus.repository.Facet;
import org.sonatype.nexus.repository.chef.internal.AssetKind;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Payload;

import javax.annotation.Nullable;
import java.io.IOException;

@Facet.Exposed
public interface ChefHostedFacet
        extends Facet {

    String getSupermarketBaseUrl();

    void upload(Payload payload) throws IOException;

    @Nullable
    Content get(String path) throws IOException;

    void rebuildMetadataJson(String path, Content content, AssetKind assetKind) throws IOException;
}
