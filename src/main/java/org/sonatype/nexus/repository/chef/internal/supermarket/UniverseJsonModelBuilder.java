package org.sonatype.nexus.repository.chef.internal.supermarket;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.sonatype.goodies.common.Loggers;
import org.sonatype.nexus.repository.chef.internal.supermarket.model.UniverseJsonCookbookModel;
import org.sonatype.nexus.repository.chef.internal.supermarket.model.UniverseJsonCookbookVersionModel;
import org.sonatype.nexus.repository.chef.internal.supermarket.model.UniverseJsonModel;

import java.util.HashMap;
import java.util.Map;

public class UniverseJsonModelBuilder {
    protected static final Logger log = Preconditions.checkNotNull(Loggers.getLogger(UniverseJsonModelBuilder.class));

    private Map<String, UniverseJsonCookbookModel> cookbooks;

    public UniverseJsonModelBuilder() {
        cookbooks = new HashMap<>();
    }

    public void addCookbook(String cookbookName, String cookbookVersion, UniverseJsonCookbookVersionModel versionModel) {
        log.trace(String.format("Trying to put cookbook in UniverseJsonBuilder: name=%s, version=%s, versionModel=%s", cookbookName, cookbookVersion, versionModel));

        if (cookbooks.containsKey(cookbookName) && cookbooks.get(cookbookName) != null) {
            UniverseJsonCookbookModel cookbookVersions = cookbooks.get(cookbookName);
            if (cookbookVersions.containsKey(cookbookVersion) && cookbookVersions.get(cookbookVersion) != null) {
                log.warn(String.format("Duplicate cookbooks with same name and version found, name=%s, version=%s", cookbookName, cookbookVersion));
            } else {
                log.trace(String.format("Putting new version of existing cookbook in UniverseJsonBuilder: name=%s, version=%s", cookbookName, cookbookVersion));
                cookbookVersions.put(cookbookVersion, versionModel);
            }
        } else {
            HashMap<String, UniverseJsonCookbookVersionModel> versionsMap = new HashMap<>();
            versionsMap.put(cookbookVersion, versionModel);
            UniverseJsonCookbookModel cookbookVersions = new UniverseJsonCookbookModel(versionsMap);
            log.trace(String.format("Putting cookbook in UniverseJsonBuilder: name=%s, version=%s", cookbookName, cookbookVersion));
            cookbooks.put(cookbookName, cookbookVersions);
        }
    }

    public UniverseJsonModel build() {
        return new UniverseJsonModel(cookbooks);
    }

}
