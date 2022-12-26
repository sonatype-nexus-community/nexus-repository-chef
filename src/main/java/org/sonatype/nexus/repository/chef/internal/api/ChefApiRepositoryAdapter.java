package org.sonatype.nexus.repository.chef.internal.api;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.chef.internal.ChefFormat;
import org.sonatype.nexus.repository.rest.api.SimpleApiRepositoryAdapter;
import org.sonatype.nexus.repository.rest.api.model.AbstractApiRepository;
import org.sonatype.nexus.repository.routing.RoutingRuleStore;
import org.sonatype.nexus.repository.types.HostedType;

import javax.inject.Inject;
import javax.inject.Named;

@Named(ChefFormat.NAME)
public class ChefApiRepositoryAdapter
        extends SimpleApiRepositoryAdapter {
    @Inject
    public ChefApiRepositoryAdapter(final RoutingRuleStore routingRuleStore) {
        super(routingRuleStore);
    }

    @Override
    public AbstractApiRepository adapt(final Repository repository) {
        boolean online = repository.getConfiguration().isOnline();
        String name = repository.getName();
        String url = repository.getUrl();

        switch (repository.getType().toString()) {
            case HostedType.NAME:
                return new ChefHostedApiRepository(
                        name,
                        url,
                        online,
                        getHostedStorageAttributes(repository),
                        getCleanupPolicyAttributes(repository),
                        getComponentAttributes(repository));
        }
        return null;
    }
}
