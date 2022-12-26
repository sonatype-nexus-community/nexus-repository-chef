package org.sonatype.nexus.repository.chef.internal.hosted;

import com.google.common.collect.Lists;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.chef.internal.ChefFormat;
import org.sonatype.nexus.repository.chef.internal.util.ChefResponseContent;
import org.sonatype.nexus.repository.chef.internal.util.ChefPathUtils;
import org.sonatype.nexus.repository.rest.UploadDefinitionExtension;
import org.sonatype.nexus.repository.security.ContentPermissionChecker;
import org.sonatype.nexus.repository.security.VariableResolverAdapter;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.upload.UploadHandlerSupport;
import org.sonatype.nexus.repository.upload.UploadDefinition;
import org.sonatype.nexus.repository.upload.UploadResponse;
import org.sonatype.nexus.repository.upload.ComponentUpload;
import org.sonatype.nexus.repository.upload.AssetUpload;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.PartPayload;
import org.sonatype.nexus.transaction.UnitOfWork;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

@Named(ChefFormat.NAME)
@Singleton
public class ChefHostedUploadHandler extends UploadHandlerSupport {

    private final ContentPermissionChecker contentPermissionChecker;

    private final VariableResolverAdapter variableResolverAdapter;

    private UploadDefinition definition;

    @Inject
    public ChefHostedUploadHandler(final ContentPermissionChecker contentPermissionChecker,
                                   @Named("simple") final VariableResolverAdapter variableResolverAdapter,
                                   final Set<UploadDefinitionExtension> uploadDefinitionExtensions) {
        super(uploadDefinitionExtensions);
        this.contentPermissionChecker = contentPermissionChecker;
        this.variableResolverAdapter = variableResolverAdapter;
    }

    @Override
    public UploadResponse handle(final Repository repository, final ComponentUpload upload) throws IOException {
        //Data holders for populating the UploadResponse
        List<PartPayload> payloads = new ArrayList<>();

        // We don't yet know the final path to save the tarball at, as it depends on metadata inside the tarball
        // Thus we test permissions on a fixed placeholder path
        final String placeholderPath = ChefPathUtils.buildInternalPlaceholderPathForPermissionCheck();

        for (AssetUpload asset : upload.getAssetUploads()) {
            ensurePermitted(repository.getName(), ChefFormat.NAME, placeholderPath, emptyMap());
            payloads.add(asset.getPayload());
        }

        List<ChefResponseContent> chefResponseContents = getResponseContents(repository, payloads);
        List<Content> contentList = chefResponseContents.stream().map(ChefResponseContent::getContent).collect(Collectors.toList());
        List<String> pathList = chefResponseContents.stream().map(ChefResponseContent::getPathToContent).collect(Collectors.toList());

        return new UploadResponse(contentList, pathList);
    }

    private List<ChefResponseContent> getResponseContents(final Repository repository, final List<PartPayload> payloads)
            throws IOException {
        ChefContentFacet facet = repository.facet(ChefContentFacet.class);
        List<ChefResponseContent> responseContents = Lists.newArrayList();
        UnitOfWork.begin(repository.facet(StorageFacet.class).txSupplier());
        try {
            for (PartPayload payload : payloads) {
                Content content = facet.put(payload);
                String path = getPathFromUploadedContent(content);
                responseContents.add(new ChefResponseContent(content, path));
            }
        } finally {
            UnitOfWork.end();
        }
        return responseContents;
    }

    // For now this is convoluted and not really needed - it will be useful when path is created from metadata inside tarball
    private String getPathFromUploadedContent(Content content) {
        Object asset = content.getAttributes().get(Asset.class.getName());
        if (asset instanceof Asset && ((Asset) asset).name() != null) {
            return ((Asset) asset).name();
        } else {
            return "";
        }
    }

    @Override
    public UploadDefinition getDefinition() {
        if (definition == null) {
            definition = getDefinition(ChefFormat.NAME, false,
                    emptyList(),
                    emptyList(),
                    null);
        }
        return definition;
    }

    @Override
    public VariableResolverAdapter getVariableResolverAdapter() {
        return variableResolverAdapter;
    }

    @Override
    public ContentPermissionChecker contentPermissionChecker() {
        return contentPermissionChecker;
    }

    @Override
    public boolean supportsExportImport() {
        return true;
    }
}
