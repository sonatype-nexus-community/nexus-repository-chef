package org.sonatype.nexus.repository.chef.internal;

import org.sonatype.nexus.repository.chef.internal.metadata.CookbookMetadataParser;
import org.sonatype.nexus.repository.view.payloads.TempBlob;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.inject.Inject;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

@Named
@Singleton
public class ChefAttributesExtractor {

    private final CookbookMetadataParser cookbookMetadataParser;

    @Inject
    public ChefAttributesExtractor(final CookbookMetadataParser cookbookMetadataParser) {
        this.cookbookMetadataParser = checkNotNull(cookbookMetadataParser);
    }

    public ChefAttributes getAttributes(TempBlob blob) throws IOException {
        String updatedAt = removeMillisFromIsoInstant(String.valueOf(blob.getBlob().getMetrics().getCreationTime()));
        String tarballSize = String.valueOf(blob.getBlob().getMetrics().getContentSize());

        return new ChefAttributes(
                cookbookMetadataParser.getMetadataFromTarballBlob(blob),
                updatedAt,
                tarballSize);
    }

    // Helper function to convert timestamp format to supermarket compatible
    private String removeMillisFromIsoInstant(String isoInstant) {
        return isoInstant.substring(0, isoInstant.lastIndexOf('.')).concat("Z");
    }

}
