package org.sonatype.nexus.repository.chef.internal.supermarket.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.HashMap;
import java.util.Map;

public class UniverseJsonModel extends HashMap<String, UniverseJsonCookbookModel> {

    @JsonCreator
    public UniverseJsonModel(Map<String, UniverseJsonCookbookModel> cookbooks) {
        super(cookbooks);
    }

}
