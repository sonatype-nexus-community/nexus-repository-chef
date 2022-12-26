package org.sonatype.nexus.repository.chef.internal.supermarket.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class CookbookVersionInfoJsonModel {

    @JsonProperty(value = "license")
    private String license;

    @JsonProperty(value = "tarball_file_size")
    private Long tarBallFileSize;

    @JsonProperty(value = "version")
    private String version;

    @JsonProperty(value = "published_at")
    private String publishedAt;

    @JsonProperty(value = "average_rating")
    private String averageRating;

    @JsonProperty(value = "cookbook")
    private String cookbookUrl;

    @JsonProperty(value = "file")
    private String fileUrl;

    @JsonProperty(value = "supports")
    private Map<String, String> supports;

    @JsonProperty(value = "dependencies")
    private Map<String, String> dependencies;

    public CookbookVersionInfoJsonModel(String license, Long tarBallFileSize, String version, String publishedAt, String averageRating, String cookbookUrl, String fileUrl, Map<String, String> supports, Map<String, String> dependencies) {
        this.license = license;
        this.tarBallFileSize = tarBallFileSize;
        this.version = version;
        this.publishedAt = publishedAt;
        this.averageRating = averageRating;
        this.cookbookUrl = cookbookUrl;
        this.fileUrl = fileUrl;
        this.supports = supports;
        this.dependencies = dependencies;
    }
}
