package org.sonatype.nexus.repository.chef.internal.api;

import org.sonatype.nexus.repository.rest.api.RepositoriesApiResourceV1;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.Path;

@Named
@Singleton
@Path(ChefHostedApiRepositoryResourceV1.RESOURCE_URI)
public class ChefHostedApiRepositoryResourceV1
        extends ChefHostedRepositoryApiResource {
    static final String RESOURCE_URI = RepositoriesApiResourceV1.RESOURCE_URI + "/chef/hosted";
}

