package org.sonatype.nexus.repository.chef.internal.metadata.json;

import org.sonatype.nexus.repository.chef.internal.metadata.CookbookMetadata;

import java.util.*;
import java.util.stream.*;

public class MetadataJsonMapper {
    public static CookbookMetadata convertToCookbookMetadata(MetadataJsonModel jsonModel) {
        return new CookbookMetadata(
                jsonModel.getName(),
                jsonModel.getVersion(),
                jsonModel.getMaintainer(),
                jsonModel.getDescription(),
                jsonModel.getSourceUrl(),
                jsonModel.getExternalUrl(),
                jsonModel.getIssuesUrl(),
                jsonModel.getLicence(),
                flattenMapToString(jsonModel.getDependencies()),
                flattenMapToString(jsonModel.getPlatforms())
        );
    }

    private static String flattenMapToString(Map<String, String> map) {
        return map
                .entrySet()
                .stream()
                .map(k -> k.getKey().concat(", ").concat(k.getValue()))
                .map(String::trim)
                .sorted()
                .collect(Collectors.joining("; "));
    }
}
