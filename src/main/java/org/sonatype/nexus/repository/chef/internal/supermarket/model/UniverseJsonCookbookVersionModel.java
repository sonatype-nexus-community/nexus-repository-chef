package org.sonatype.nexus.repository.chef.internal.supermarket.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class UniverseJsonCookbookVersionModel {

    @JsonProperty(value = "location_type")
    private String locationType;

    @JsonProperty(value = "location_path")
    private String locationPath;

    @JsonProperty(value = "download_url")
    private String downloadUrl;

    @JsonProperty(value = "dependencies")
    private Map<String, String> dependencies;

    @JsonCreator
    public UniverseJsonCookbookVersionModel(String locationType, String locationPath, String downloadUrl, Map<String, String> dependencies) {
        this.locationType = locationType;
        this.locationPath = locationPath;
        this.downloadUrl = downloadUrl;
        this.dependencies = dependencies;
    }

    public String getLocationType() {
        return locationType;
    }

    public void setLocationType(String locationType) {
        this.locationType = locationType;
    }

    public String getLocationPath() {
        return locationPath;
    }

    public void setLocationPath(String locationPath) {
        this.locationPath = locationPath;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public Map<String, String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(Map<String, String> dependencies) {
        this.dependencies = dependencies;
    }

    @Override
    public String toString() {
        return "UniverseJsonCookbookVersionModel{" +
                "locationType='" + locationType + '\'' +
                ", locationPath='" + locationPath + '\'' +
                ", downloadUrl='" + downloadUrl + '\'' +
                ", dependencies=" + dependencies +
                '}';
    }
}
