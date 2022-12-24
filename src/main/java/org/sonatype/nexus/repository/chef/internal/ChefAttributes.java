package org.sonatype.nexus.repository.chef.internal;

import java.util.*;

// Attributes saved as metadata for components/assets
// We want all attributes used to populate supermarket endpoint JSON responses
// TODO Not yet implemented: Rest of needed attributes
public class ChefAttributes {

    public static final String P_NAME = "name";
    public static final String P_VERSION = "version";
    private final Map<String, String> attributesMap;

    public ChefAttributes(final String cookbookName, final String cookbookVersion) {
        this.attributesMap = new HashMap<>();
        attributesMap.put(P_NAME, cookbookName);
        attributesMap.put(P_VERSION, cookbookVersion);
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
