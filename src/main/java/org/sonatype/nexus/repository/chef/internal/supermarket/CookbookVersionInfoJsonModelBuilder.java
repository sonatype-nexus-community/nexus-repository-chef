package org.sonatype.nexus.repository.chef.internal.supermarket;

import org.sonatype.nexus.repository.chef.internal.ChefAttributes;
import org.sonatype.nexus.repository.chef.internal.supermarket.model.CookbookVersionInfoJsonModel;
import org.sonatype.nexus.repository.chef.internal.util.AttributesHelper;
import org.sonatype.nexus.repository.chef.internal.util.ChefPathUtils;
import org.sonatype.nexus.repository.storage.Asset;

import java.util.Map;

public class CookbookVersionInfoJsonModelBuilder {

    private static final String AVERAGE_RATING = null;

    private String license;
    private Long tarBallFileSize;
    private String version;
    private String publishedAt;
    private String cookbookUrl;
    private String fileUrl;
    private Map<String, String> supportsMap;
    private Map<String, String> dependenciesMap;

    public CookbookVersionInfoJsonModelBuilder(Asset asset, String baseUrl) {
        addDepends(asset);
        addSupports(asset);
        updateBuilderMetadata(asset, baseUrl);
    }

    private void addSupports(Asset asset) {
        this.supportsMap = AttributesHelper.parseStringIntoMap(asset.formatAttributes().get(ChefAttributes.P_SUPPORTS, String.class));
    }

    private void addDepends(Asset asset) {
        this.dependenciesMap = AttributesHelper.parseStringIntoMap(asset.formatAttributes().get(ChefAttributes.P_DEPENDS, String.class));
    }

    private void updateBuilderMetadata(Asset asset, String baseUrl) {
        this.license = asset.formatAttributes().get(ChefAttributes.P_LICENCE, String.class);
        this.tarBallFileSize = Long.valueOf(asset.formatAttributes().get(ChefAttributes.P_TARBALL_FILE_SIZE, String.class, "0"));
        this.version = asset.formatAttributes().get(ChefAttributes.P_VERSION, String.class);
        this.publishedAt = asset.formatAttributes().get(ChefAttributes.P_UPDATED_AT, String.class);
        this.cookbookUrl = ChefPathUtils.buildSupermarketCookbookUrl(baseUrl, asset.formatAttributes().get(ChefAttributes.P_NAME, String.class));
        this.fileUrl = ChefPathUtils.buildSupermarketCookbookVersionDownloadUrl(
                baseUrl,
                asset.formatAttributes().get(ChefAttributes.P_NAME, String.class),
                asset.formatAttributes().get(ChefAttributes.P_VERSION, String.class));
    }

    public CookbookVersionInfoJsonModel build() {
        return new CookbookVersionInfoJsonModel(
                license,
                tarBallFileSize,
                version,
                publishedAt,
                AVERAGE_RATING,
                cookbookUrl,
                fileUrl,
                supportsMap,
                dependenciesMap
        );
    }
}
