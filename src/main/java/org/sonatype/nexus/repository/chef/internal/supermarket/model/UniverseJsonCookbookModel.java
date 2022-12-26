package org.sonatype.nexus.repository.chef.internal.supermarket.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.HashMap;
import java.util.Map;

public class UniverseJsonCookbookModel extends HashMap<String, UniverseJsonCookbookVersionModel> {

    @JsonCreator
    public UniverseJsonCookbookModel(Map<String, UniverseJsonCookbookVersionModel> versions) {
        super(versions);
    }

}
