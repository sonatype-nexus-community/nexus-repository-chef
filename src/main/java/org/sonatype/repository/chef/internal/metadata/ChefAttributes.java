package org.sonatype.repository.chef.internal.metadata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @since 0.0.1
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class ChefAttributes
{
  private String name;
  private String version;
  private String description;
  private String longDescription;

  public String getDescription() {
    return description;
  }

  public void setDescription(final String description) {
    this.description = description;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(final String version) {
    this.version = version;
  }

  public String getLongDescription() {
    return longDescription;
  }

  public void setLongDescription(final String longDescription) {
    this.longDescription = longDescription;
  }
}
