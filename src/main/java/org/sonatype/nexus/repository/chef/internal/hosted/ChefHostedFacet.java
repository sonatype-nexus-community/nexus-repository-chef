package org.sonatype.nexus.repository.chef.internal.hosted;

import org.sonatype.nexus.repository.Facet;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Payload;

import javax.annotation.Nullable;
import java.io.IOException;

@Facet.Exposed
public interface ChefHostedFacet
        extends Facet {
    void upload(Payload payload) throws IOException;

    @Nullable
    Content get(String path) throws IOException;

}
