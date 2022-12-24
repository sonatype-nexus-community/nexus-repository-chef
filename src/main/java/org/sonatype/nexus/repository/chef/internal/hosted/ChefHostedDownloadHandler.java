package org.sonatype.nexus.repository.chef.internal.hosted;

import com.google.common.base.Preconditions;
import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.sonatype.goodies.common.Loggers;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.chef.internal.AssetKind;
import org.sonatype.nexus.repository.http.HttpResponses;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Handler;
import org.sonatype.nexus.repository.view.Response;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.core.HttpHeaders;

import static org.sonatype.nexus.repository.chef.internal.util.ChefPathUtils.buildTarballPath;

@Named
@Singleton
public class ChefHostedDownloadHandler implements Handler {
    protected static final Logger log = Preconditions.checkNotNull(Loggers.getLogger(ChefHostedDownloadHandler.class));

    @Nonnull
    @Override
    public Response handle(@Nonnull final Context context) throws Exception {
        Repository repository = context.getRepository();
        ChefHostedFacet hostedFacet = repository.facet(ChefHostedFacet.class);
        AssetKind assetKind = context.getAttributes().require(AssetKind.class);
        switch (assetKind) {
            case COOKBOOK:
                log.trace("in handle, case COOKBOOK");
                return responseForTarball(hostedFacet.get(buildTarballPath(context)));
            case UNIVERSE_JSON:
                log.trace("in handle, case UNIVERSE_JSON");
                throw new NotImplementedException("UNIVERSE_JSON not yet implemented");
            case COOKBOOK_INFO:
                log.trace("in handle, case COOKBOOK_INFO");
                throw new NotImplementedException("COOKBOOK_INFO not yet implemented");
            case COOKBOOK_VERSION_INFO:
                log.trace("in handle, case COOKBOOK_VERSION_INFO");
                throw new NotImplementedException("COOKBOOK_VERSION_INFO not yet implemented");
            default:
                throw new IllegalStateException("Unexpected assetKind: " + assetKind);
        }
    }

    private Response responseForTarball(@Nullable final Content content) {
        if (content == null) {
            return HttpResponses.notFound();
        }
        String fileName;
        try {
            String fullPath = content.getAttributes().get("org.sonatype.nexus.repository.storage.Asset", Asset.class, null).name();
            log.trace(String.format("fullPath: %s", fullPath));
            fileName = fullPath.substring(fullPath.lastIndexOf('/') + 1);
            log.trace(String.format("Parsed filename for tarball: %s", fileName));
        } catch (Exception e) {
            fileName = "cookbook.tgz";
            log.warn(String.format("Could not parse tarball filename from path, using default filename: %s", fileName));
            log.warn(String.format("Exception: %s", e.getMessage()));
        }
        Response response = HttpResponses.ok(content);
        response.getHeaders().set(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=%s", fileName));
        return response;
    }
}
