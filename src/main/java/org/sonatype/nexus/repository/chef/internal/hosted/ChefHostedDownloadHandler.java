package org.sonatype.nexus.repository.chef.internal.hosted;

import com.google.common.base.Preconditions;
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

import static org.sonatype.nexus.repository.chef.internal.util.ChefPathUtils.buildInternalCookbookInfoJsonPath;
import static org.sonatype.nexus.repository.chef.internal.util.ChefPathUtils.buildInternalCookbookVersionInfoJsonPath;
import static org.sonatype.nexus.repository.chef.internal.util.ChefPathUtils.buildInternalTarballPath;
import static org.sonatype.nexus.repository.chef.internal.util.ChefPathUtils.buildInternalUniverseJsonPath;

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
                return responseForTarball(hostedFacet.get(buildInternalTarballPath(context)));
            case UNIVERSE_JSON:
                log.trace("in handle, case UNIVERSE_JSON");
                return responseForMetadata(hostedFacet.get(buildInternalUniverseJsonPath()));
            case COOKBOOK_INFO:
                log.trace("in handle, case COOKBOOK_INFO");
                return responseForMetadata(hostedFacet.get(buildInternalCookbookInfoJsonPath(context)));
            case COOKBOOK_VERSION_INFO:
                log.trace("in handle, case COOKBOOK_VERSION_INFO");
                return responseForMetadata(hostedFacet.get(buildInternalCookbookVersionInfoJsonPath(context)));
            default:
                throw new IllegalStateException("Unexpected assetKind: " + assetKind);
        }
    }

    private Response responseForTarball(@Nullable final Content content) {
        if (content == null) {
            return HttpResponses.notFound();
        }

        // Try to set filename according to content attributes
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

    private Response responseForMetadata(@Nullable final Content content) {
        if (content == null) {
            return HttpResponses.notFound();
        }
        return HttpResponses.ok(content);
    }
}
