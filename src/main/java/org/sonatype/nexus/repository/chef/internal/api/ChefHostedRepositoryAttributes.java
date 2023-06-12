package org.sonatype.nexus.repository.chef.internal.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

public class ChefHostedRepositoryAttributes
{
    @ApiModelProperty(value = "Base url to use for Supermarket API responses. Leave blank to use hostname + repo name", example = "http://supermarket.internal")
    protected final String supermarketBaseUrl;

    @JsonCreator
    public ChefHostedRepositoryAttributes(@JsonProperty("supermarketBaseUrl") final String supermarketBaseUrl) {
        this.supermarketBaseUrl = supermarketBaseUrl;
    }

    public String getSupermarketBaseUrl() {
        return supermarketBaseUrl;
    }
}
