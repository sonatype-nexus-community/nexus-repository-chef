package org.sonatype.nexus.repository.chef.internal.metadata.json;

import java.util.*;

public class MetadataJsonModel {
    private final String name;

    private final String version;

    private final String maintainer;

    private final String description;

    private final String sourceUrl;

    private final String externalUrl;

    private final String issuesUrl;

    private final String licence;

    private final Map<String, String> dependencies;

    private final Map<String, String> platforms;

    public MetadataJsonModel(
            String name,
            String version,
            String maintainer,
            String description,
            String sourceUrl,
            String externalUrl,
            String issuesUrl,
            String licence,
            Map<String, String> dependencies,
            Map<String, String> platforms
    ) {
        this.name = name;
        this.version = version;
        this.maintainer = maintainer;
        this.description = description;
        this.sourceUrl = sourceUrl;
        this.externalUrl = externalUrl;
        this.issuesUrl = issuesUrl;
        this.licence = licence;
        this.dependencies = dependencies;
        this.platforms = platforms;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getMaintainer() {
        return maintainer;
    }

    public String getDescription() {
        return description;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public String getExternalUrl() {
        return externalUrl;
    }

    public String getIssuesUrl() {
        return issuesUrl;
    }

    public String getLicence() {
        return licence;
    }

    public Map<String, String> getDependencies() {
        return dependencies;
    }

    public Map<String, String> getPlatforms() {
        return platforms;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetadataJsonModel that = (MetadataJsonModel) o;
        return Objects.equals(name, that.name) && Objects.equals(version, that.version) && Objects.equals(maintainer, that.maintainer) && Objects.equals(description, that.description) && Objects.equals(sourceUrl, that.sourceUrl) && Objects.equals(externalUrl, that.externalUrl) && Objects.equals(issuesUrl, that.issuesUrl) && Objects.equals(licence, that.licence) && Objects.equals(dependencies, that.dependencies) && Objects.equals(platforms, that.platforms);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version, maintainer, description, sourceUrl, externalUrl, issuesUrl, licence, dependencies, platforms);
    }

    @Override
    public String toString() {
        return "MetadataJsonModel{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", maintainer='" + maintainer + '\'' +
                ", description='" + description + '\'' +
                ", sourceUrl='" + sourceUrl + '\'' +
                ", externalUrl='" + externalUrl + '\'' +
                ", issuesUrl='" + issuesUrl + '\'' +
                ", licence='" + licence + '\'' +
                ", dependencies=" + dependencies +
                ", platforms=" + platforms +
                '}';
    }
}
