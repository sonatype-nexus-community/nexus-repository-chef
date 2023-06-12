package org.sonatype.nexus.repository.chef.internal.hosted;

import com.google.common.annotations.VisibleForTesting;
import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.chef.internal.AssetKind;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.config.ConfigurationFacet;
import org.sonatype.nexus.repository.transaction.TransactionalStoreBlob;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Payload;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.groups.Default;
import java.io.IOException;

@Named
public class ChefHostedFacetImpl extends FacetSupport implements ChefHostedFacet {

    @Inject
    public ChefHostedFacetImpl() {
    }

    @VisibleForTesting
    static final String CONFIG_KEY = "chef";

    @VisibleForTesting
    static class Config
    {
        public String supermarketBaseUrl;
    }

    private Config config;

    @Override
    protected void doValidate(final Configuration configuration) throws Exception {
        facet(ConfigurationFacet.class).validateSection(
                configuration,
                CONFIG_KEY,
                Config.class,
                Default.class,
                getRepository().getType().getValidationGroup());
    }

    @Override
    protected void doConfigure(final Configuration configuration) throws Exception {
        config = facet(ConfigurationFacet.class).readSection(configuration, CONFIG_KEY, Config.class);
    }

    @Override
    protected void doDestroy() throws Exception {
        config = null;
    }

    @Override
    public String getSupermarketBaseUrl() {
        return config.supermarketBaseUrl;
    }

    @Override
    @TransactionalStoreBlob
    public void upload(final Payload payload) throws IOException {
        content().put(payload);
    }

    @Override
    public Content get(final String path) throws IOException {
        return content().get(path);
    }

    @Override
    @TransactionalStoreBlob
    public void rebuildMetadataJson(String path, Content content, AssetKind assetKind) throws IOException {
        log.trace(String.format("in rebuildMetadataJson, path=%s, AssetKind=%s", path, assetKind.name()));
        if (content != null) {
            content().putMetadata(path, content, assetKind);
        } else {
            content().deleteMetadata(path, assetKind);
        }
    }

    private ChefContentFacet content() {
        return getRepository().facet(ChefContentFacet.class);
    }
}
