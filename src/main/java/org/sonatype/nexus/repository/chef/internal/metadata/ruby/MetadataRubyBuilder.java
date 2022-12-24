package org.sonatype.nexus.repository.chef.internal.metadata.ruby;

import org.sonatype.nexus.repository.chef.internal.metadata.util.InvalidMetadataException;

import java.util.*;

public class MetadataRubyBuilder {

    private String name;
    private String maintainer;
    private String description;
    private String sourceUrl;
    private String externalUrl;
    private String issuesUrl;
    private String version;
    private String licence;
    private final List<String> dependsList;
    private final List<String> supportList;

    public MetadataRubyBuilder() {
        this.dependsList = new ArrayList<>();
        this.supportList = new ArrayList<>();
    }

    public MetadataRubyModel buildMetadataRubyModel() {
        return new MetadataRubyModel(
                name,
                version,
                maintainer,
                description,
                sourceUrl,
                externalUrl,
                issuesUrl,
                licence,
                dependsList,
                supportList
        );
    }

    public void setName(String name) {
        if (isAlreadySet(this.name)) {
            throw new InvalidMetadataException("Metadata 'name' appears twice in metadata file");
        } else {
            this.name = name;
        }
    }

    public void setVersion(String version) {
        if (isAlreadySet(this.version)) {
            throw new InvalidMetadataException("Metadata 'version' appears twice in metadata file");
        } else {
            this.version = version;
        }
    }

    public void setMaintainer(String maintainer) {
        if (isAlreadySet(this.maintainer)) {
            throw new InvalidMetadataException("Metadata 'maintainer' appears twice in metadata file");
        } else {
            this.maintainer = maintainer;
        }
    }

    public void setDescription(String description) {
        if (isAlreadySet(this.description)) {
            throw new InvalidMetadataException("Metadata 'description' appears twice in metadata file");
        } else {
            this.description = description;
        }
    }

    public void setSourceUrl(String sourceUrl) {
        if (isAlreadySet(this.sourceUrl)) {
            throw new InvalidMetadataException("Metadata 'sourceUrl' appears twice in metadata file");
        } else {
            this.sourceUrl = sourceUrl;
        }
    }

    public void setExternalUrl(String externalUrl) {
        if (isAlreadySet(this.externalUrl)) {
            throw new InvalidMetadataException("Metadata 'externalUrl' appears twice in metadata file");
        } else {
            this.externalUrl = externalUrl;
        }
    }

    public void setIssuesUrl(String issuesUrl) {
        if (isAlreadySet(this.issuesUrl)) {
            throw new InvalidMetadataException("Metadata 'issuesUrl' appears twice in metadata file");
        } else {
            this.issuesUrl = issuesUrl;
        }
    }

    public void setLicence(String licence) {
        if (isAlreadySet(this.licence)) {
            throw new InvalidMetadataException("Metadata 'licence' appears twice in metadata file");
        } else {
            this.licence = licence;
        }
    }

    public void addDependency(String dependency) {
        this.dependsList.add(addVersionZeroIfNoVersionSpecified(dependency));
    }

    public void addSupports(String supports) {
        this.supportList.add(addVersionZeroIfNoVersionSpecified(supports));
    }

    // Helper function to add version constraint ">= 0.0.0" if no version dependency is specified
    private String addVersionZeroIfNoVersionSpecified(String string) {
        return (!string.matches(".+([0-9]+\\.)*[0-9]+$")) ? string.concat(", >= 0.0.0") : string;
    }

    private boolean isAlreadySet(String s) {
        return (s != null);
    }
}
