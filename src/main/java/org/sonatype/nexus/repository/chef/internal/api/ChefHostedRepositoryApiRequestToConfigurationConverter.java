package org.sonatype.nexus.repository.chef.internal.api;

import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.rest.api.HostedRepositoryApiRequestToConfigurationConverter;

import javax.inject.Named;

@Named
public class ChefHostedRepositoryApiRequestToConfigurationConverter
        extends HostedRepositoryApiRequestToConfigurationConverter<ChefHostedApiRepositoryRequest> {
    @Override
    public Configuration convert(final ChefHostedApiRepositoryRequest request) {
        return super.convert(request);
    }
}
