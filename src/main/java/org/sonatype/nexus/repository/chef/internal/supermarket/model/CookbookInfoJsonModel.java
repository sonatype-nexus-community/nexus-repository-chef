package org.sonatype.nexus.repository.chef.internal.supermarket.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class CookbookInfoJsonModel {

    @JsonProperty(value = "name")
    private String name;

    @JsonProperty(value = "maintainer")
    private String maintainer;

    @JsonProperty(value = "description")
    private String description;

    @JsonProperty(value = "category")
    private String category;

    @JsonProperty(value = "latest_version")
    private String latestVersion;

    @JsonProperty(value = "external_url")
    private String externalUrl;

    @JsonProperty(value = "source_url")
    private String sourceUrl;

    @JsonProperty(value = "issues_url")
    private String issuesUrl;

    @JsonProperty(value = "average_rating")
    private String averageRating;

    @JsonProperty(value = "created_at")
    private String createdAt;

    @JsonProperty(value = "updated_at")
    private String updatedAt;

    @JsonProperty(value = "up_for_adoption")
    private Boolean upForAdoption;

    @JsonProperty(value = "deprecated")
    private Boolean deprecated;

    @JsonProperty(value = "versions")
    private List<String> versions;

    @JsonCreator
    public CookbookInfoJsonModel(String name, String maintainer, String description, String category, String latestVersion, String externalUrl, String sourceUrl, String issuesUrl, String averageRating, String createdAt, String updatedAt, Boolean upForAdoption, Boolean deprecated, List<String> versions) {
        this.name = name;
        this.maintainer = maintainer;
        this.description = description;
        this.category = category;
        this.latestVersion = latestVersion;
        this.externalUrl = externalUrl;
        this.sourceUrl = sourceUrl;
        this.issuesUrl = issuesUrl;
        this.averageRating = averageRating;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.upForAdoption = upForAdoption;
        this.deprecated = deprecated;
        this.versions = versions;
    }
}
