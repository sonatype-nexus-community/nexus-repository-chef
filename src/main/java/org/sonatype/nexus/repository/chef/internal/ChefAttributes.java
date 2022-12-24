package org.sonatype.nexus.repository.chef.internal;

import org.sonatype.nexus.repository.chef.internal.metadata.CookbookMetadata;
import org.sonatype.nexus.repository.chef.internal.util.*;

import java.util.*;

// Attributes saved as metadata for components/assets
// We want all attributes used to populate supermarket endpoint JSON responses
public class ChefAttributes {

    public static final String P_NAME = "name";
    public static final String P_MAINTAINER = "maintainer";
    public static final String P_DESCRIPTION = "description";
    public static final String P_SOURCE_URL = "source_url";
    public static final String P_EXTERNAL_URL = "external_url";
    public static final String P_ISSUES_URL = "issues_url";
    public static final String P_VERSION = "version";
    public static final String P_LICENCE = "license";
    public static final String P_DEPENDS = "depends";
    public static final String P_SUPPORTS = "supports";

    // Following json fields needs calculation and can't be found in cookbook metadata file
    public static final String P_UPDATED_AT = "updated_at";
    public static final String P_TARBALL_FILE_SIZE = "tarball_file_size";

    private final Map<String, String> attributesMap;

    public ChefAttributes(CookbookMetadata cookbookMetadata, String updatedAt, String tarballFileSize) {
        this.attributesMap = new HashMap<>();
        attributesMap.put(P_NAME, AttributesHelper.standardizeAttributeValue(cookbookMetadata.getName()));
        attributesMap.put(P_MAINTAINER, AttributesHelper.standardizeAttributeValue(cookbookMetadata.getMaintainer()));
        attributesMap.put(P_DESCRIPTION, AttributesHelper.standardizeAttributeValue(cookbookMetadata.getDescription()));
        attributesMap.put(P_SOURCE_URL, AttributesHelper.standardizeAttributeValue(cookbookMetadata.getSourceUrl()));
        attributesMap.put(P_EXTERNAL_URL, AttributesHelper.standardizeAttributeValue(cookbookMetadata.getExternalUrl()));
        attributesMap.put(P_ISSUES_URL, AttributesHelper.standardizeAttributeValue(cookbookMetadata.getIssuesUrl()));
        attributesMap.put(P_VERSION, AttributesHelper.standardizeAttributeValue(cookbookMetadata.getVersion()));
        attributesMap.put(P_LICENCE, AttributesHelper.standardizeAttributeValue(cookbookMetadata.getLicence()));
        attributesMap.put(P_DEPENDS, AttributesHelper.standardizeAttributeValue(cookbookMetadata.getDepends()));
        attributesMap.put(P_SUPPORTS, AttributesHelper.standardizeAttributeValue(cookbookMetadata.getSupports()));
        attributesMap.put(P_UPDATED_AT, AttributesHelper.standardizeAttributeValue(updatedAt));
        attributesMap.put(P_TARBALL_FILE_SIZE, AttributesHelper.standardizeAttributeValue(tarballFileSize));
    }

    public Map<String, String> getAttributesMap() {
        return attributesMap;
    }

    public String getName() {
        return attributesMap.get(P_NAME);
    }

    public String getVersion() {
        return attributesMap.get(P_VERSION);
    }
}
