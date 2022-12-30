package org.sonatype.nexus.repository.chef.internal.util;

import org.sonatype.nexus.repository.view.Content;

public class ChefResponseContent {
    private final Content content;
    private final String pathToContent;

    public ChefResponseContent(Content content, String pathToContent) {
        this.content = content;
        this.pathToContent = pathToContent;
    }

    public Content getContent() {
        return content;
    }

    public String getPathToContent() {
        return pathToContent;
    }
}
