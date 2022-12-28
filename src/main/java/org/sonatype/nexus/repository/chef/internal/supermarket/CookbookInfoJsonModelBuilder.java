package org.sonatype.nexus.repository.chef.internal.supermarket;

import com.google.common.base.Preconditions;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.slf4j.Logger;
import org.sonatype.goodies.common.Loggers;
import org.sonatype.nexus.repository.chef.internal.ChefAttributes;
import org.sonatype.nexus.repository.chef.internal.supermarket.model.CookbookInfoJsonModel;

import org.sonatype.nexus.repository.chef.internal.util.ChefPathUtils;
import org.sonatype.nexus.repository.storage.Asset;

import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class CookbookInfoJsonModelBuilder {

    protected static final Logger log = Preconditions.checkNotNull(Loggers.getLogger(CookbookInfoJsonModelBuilder.class));

    private static final String CATEGORY = "Other";
    private static final String AVERAGE_RATING = null;
    private static final Boolean UP_FOR_ADOPTION = false;
    private static final Boolean DEPRECATED = false;

    private String name;

    private String maintainer;

    private String description;

    private ComparableVersion latestVersion;

    private String externalUrl;

    private String sourceUrl;

    private String issuesUrl;

    private String createdAt;

    private String updatedAt;

    private final List<ComparableVersion> versions;

    private final String baseUrl;

    public CookbookInfoJsonModelBuilder(String baseUrl) {
        this.versions = new ArrayList<>();
        this.baseUrl = baseUrl;
    }

    public void addVersion(Asset asset) {
        String cookbookVersion = asset.formatAttributes().get(ChefAttributes.P_VERSION, String.class);
        ComparableVersion comparableCookbookVersion = new ComparableVersion(cookbookVersion);
        if (versions.contains(comparableCookbookVersion)) {
            log.warn(String.format("Duplicate versions found for cookbook: version=%s", cookbookVersion));
        } else {
            log.trace(String.format("Putting new version in CookbookJsonModelBuilder: version=%s", cookbookVersion));
            versions.add(comparableCookbookVersion);
            if (latestVersion == null || comparableCookbookVersion.compareTo(latestVersion) > 0) {
                updateBuilderMetadata(asset);
            }
            if (updatedDateIsOlderThanCurrentCreatedAtDate(asset)) {
                updateBuilderCreatedAt(asset);
            }
        }
    }

    private void updateBuilderCreatedAt(Asset asset) {
        this.createdAt = asset.formatAttributes().get(ChefAttributes.P_UPDATED_AT, String.class, null);
    }

    private boolean updatedDateIsOlderThanCurrentCreatedAtDate(Asset asset) {
        String thisVersionUpdatedAt = asset.formatAttributes().get(ChefAttributes.P_UPDATED_AT, String.class, null);
        if (createdAt == null) {
            return true;
        } else if (thisVersionUpdatedAt == null) {
            return false;
        } else {
            return (thisVersionUpdatedAt.compareTo(createdAt) < 0);
        }
    }

    private void updateBuilderMetadata(Asset asset) {
        this.name = asset.formatAttributes().get(ChefAttributes.P_NAME, String.class);
        this.latestVersion = new ComparableVersion(asset.formatAttributes().get(ChefAttributes.P_VERSION, String.class));
        this.maintainer = asset.formatAttributes().get(ChefAttributes.P_MAINTAINER, String.class, null);
        this.description = asset.formatAttributes().get(ChefAttributes.P_DESCRIPTION, String.class, null);
        this.externalUrl = asset.formatAttributes().get(ChefAttributes.P_EXTERNAL_URL, String.class, null);
        this.sourceUrl = asset.formatAttributes().get(ChefAttributes.P_SOURCE_URL, String.class, null);
        this.issuesUrl = asset.formatAttributes().get(ChefAttributes.P_ISSUES_URL, String.class, null);
        this.updatedAt = asset.formatAttributes().get(ChefAttributes.P_UPDATED_AT, String.class, null);
    }

    public CookbookInfoJsonModel build() {
        return new CookbookInfoJsonModel(
                name,
                maintainer,
                description,
                CATEGORY,
                latestVersion.toString(),
                externalUrl,
                sourceUrl,
                issuesUrl,
                AVERAGE_RATING,
                createdAt,
                updatedAt,
                UP_FOR_ADOPTION,
                DEPRECATED,
                convertVersionsToUrlStrings()
        );
    }

    public boolean noVersionsFound() {
        return versions.isEmpty();
    }

    private List<String> convertVersionsToUrlStrings() {
        return versions.stream()
                .sorted(Comparator.reverseOrder())
                .map(this::convertVersionToUrl)
                .collect(Collectors.toList());
    }

    private String convertVersionToUrl(ComparableVersion version) {
        return ChefPathUtils.buildSupermarketCookbookVersionUrl(this.baseUrl, this.name, version.toString());
    }
}
