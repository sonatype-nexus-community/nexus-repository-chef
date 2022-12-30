package org.sonatype.nexus.repository.chef.internal.hosted;

import org.sonatype.nexus.repository.Facet;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.chef.internal.AssetKind;

import java.io.IOException;

@Facet.Exposed
public interface ChefContentFacet extends Facet {

    Content get(String path) throws IOException;

    void putMetadata(String path, Payload payload, AssetKind assetKind) throws IOException;

    void deleteMetadata(String path, AssetKind assetKind);

    Content put(Payload payload) throws IOException;
}