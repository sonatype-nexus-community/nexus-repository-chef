package org.sonatype.nexus.repository.chef.internal.metadata;

import java.util.*;

// class containing model of cookbook metadata we use for supermarket endpoints
public class CookbookMetadata {

    private final String name;
    private final String version;
    private final String maintainer;
    private final String description;
    private final String sourceUrl;
    private final String externalUrl;
    private final String issuesUrl;
    private final String licence;
    private final String depends;
    private final String supports;

    public CookbookMetadata(String name,
                            String version,
                            String maintainer,
                            String description,
                            String sourceUrl,
                            String externalUrl,
                            String issuesUrl,
                            String licence,
                            String depends,
                            String supports
    ) {
        this.name = name;
        this.version = version;
        this.maintainer = maintainer;
        this.description = description;
        this.sourceUrl = sourceUrl;
        this.externalUrl = externalUrl;
        this.issuesUrl = issuesUrl;
        this.licence = licence;
        this.depends = depends;
        this.supports = supports;
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

    public String getDepends() {
        return depends;
    }

    public String getSupports() {
        return supports;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CookbookMetadata that = (CookbookMetadata) o;
        return Objects.equals(name, that.name) && Objects.equals(version, that.version) && Objects.equals(maintainer, that.maintainer) && Objects.equals(description, that.description) && Objects.equals(sourceUrl, that.sourceUrl) && Objects.equals(externalUrl, that.externalUrl) && Objects.equals(issuesUrl, that.issuesUrl) && Objects.equals(licence, that.licence) && Objects.equals(depends, that.depends) && Objects.equals(supports, that.supports);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version, maintainer, description, sourceUrl, externalUrl, issuesUrl, licence, depends, supports);
    }

    @Override
    public String toString() {
        return "CookbookMetadata{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", maintainer='" + maintainer + '\'' +
                ", description='" + description + '\'' +
                ", sourceUrl='" + sourceUrl + '\'' +
                ", externalUrl='" + externalUrl + '\'' +
                ", issuesUrl='" + issuesUrl + '\'' +
                ", licence='" + licence + '\'' +
                ", depends='" + depends + '\'' +
                ", supports='" + supports + '\'' +
                '}';
    }
}
