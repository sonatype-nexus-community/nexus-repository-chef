package org.sonatype.nexus.repository.chef.internal;

import org.sonatype.nexus.repository.view.payloads.TempBlob;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.inject.Inject;

import java.io.IOException;
import java.util.*;

@Named
@Singleton
public class ChefAttributesExtractor {
    @Inject
    public ChefAttributesExtractor() {
    }

    public ChefAttributes getAttributes(TempBlob blob) throws IOException {
        // TODO not implemented in this commit: parsing of blob
        //  Parsing implemented in separate commit for readability and reviewability
        //  we put random values as placeholder for now to avoid name collisions
        String[] uuidParts = UUID.randomUUID().toString().split("-");
        return new ChefAttributes(uuidParts[0], uuidParts[1]);
    }
}
