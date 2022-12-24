package org.sonatype.nexus.repository.chef.internal.metadata.ruby;

import org.sonatype.nexus.repository.chef.internal.metadata.CookbookMetadata;

import java.util.*;
import java.util.stream.*;

public class MetadataRubyMapper {

    public static CookbookMetadata convertToCookbookMetadata(MetadataRubyModel rubyModel) {
        return new CookbookMetadata(
                rubyModel.getName(),
                rubyModel.getVersion(),
                rubyModel.getMaintainer(),
                rubyModel.getDescription(),
                rubyModel.getSourceUrl(),
                rubyModel.getExternalUrl(),
                rubyModel.getIssuesUrl(),
                rubyModel.getLicence(),
                flattenAndFormatList(rubyModel.getDependsList()),
                flattenAndFormatList(rubyModel.getSupportList())
        );
    }

    private static String flattenAndFormatList(List<String> list) {
        return list.stream()
                .map(String::trim)
                .sorted()
                .collect(Collectors.joining("; "));
    }
}
