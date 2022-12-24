package org.sonatype.nexus.repository.chef.internal.metadata.ruby;

import java.util.*;

public class MetadataRubyModel {

    private final String name;
    private final String version;
    private final String maintainer;
    private final String description;
    private final String sourceUrl;
    private final String externalUrl;
    private final String issuesUrl;
    private final String licence;
    private final List<String> dependsList;
    private final List<String> supportList;

    public MetadataRubyModel(String name, String version, String maintainer, String description, String sourceUrl, String externalUrl, String issuesUrl, String licence, List<String> dependsList, List<String> supportList) {
        this.name = name;
        this.version = version;
        this.maintainer = maintainer;
        this.description = description;
        this.sourceUrl = sourceUrl;
        this.externalUrl = externalUrl;
        this.issuesUrl = issuesUrl;
        this.licence = licence;
        this.dependsList = dependsList;
        this.supportList = supportList;
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

    public List<String> getDependsList() {
        return dependsList;
    }

    public List<String> getSupportList() {
        return supportList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetadataRubyModel rubyModel = (MetadataRubyModel) o;
        return Objects.equals(name, rubyModel.name) && Objects.equals(version, rubyModel.version) && Objects.equals(maintainer, rubyModel.maintainer) && Objects.equals(description, rubyModel.description) && Objects.equals(sourceUrl, rubyModel.sourceUrl) && Objects.equals(externalUrl, rubyModel.externalUrl) && Objects.equals(issuesUrl, rubyModel.issuesUrl) && Objects.equals(licence, rubyModel.licence) && Objects.equals(dependsList, rubyModel.dependsList) && Objects.equals(supportList, rubyModel.supportList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version, maintainer, description, sourceUrl, externalUrl, issuesUrl, licence, dependsList, supportList);
    }

    @Override
    public String toString() {
        return "MetadataRubyModel{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", maintainer='" + maintainer + '\'' +
                ", description='" + description + '\'' +
                ", sourceUrl='" + sourceUrl + '\'' +
                ", externalUrl='" + externalUrl + '\'' +
                ", issuesUrl='" + issuesUrl + '\'' +
                ", licence='" + licence + '\'' +
                ", dependsList=" + dependsList +
                ", supportList=" + supportList +
                '}';
    }
}
