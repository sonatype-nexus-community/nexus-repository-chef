package org.sonatype.nexus.repository.chef.internal.metadata.json;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.*;

import java.io.*;
import java.util.*;

public class MetadataJsonModelCustomDeserializer extends StdDeserializer<MetadataJsonModel> {

    private static final long serialVersionUID = 1L;

    public MetadataJsonModelCustomDeserializer() {
        this(null);
    }

    public MetadataJsonModelCustomDeserializer(Class clazz) {
        super(clazz);
    }

    @Override
    public MetadataJsonModel deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        deserializationContext.setAttribute(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        JsonNode jsonNode = jsonParser.getCodec().readTree(jsonParser);
        JsonNode nameNode = jsonNode.get("name");
        JsonNode versionNode = jsonNode.get("version");
        JsonNode maintainerNode = jsonNode.get("maintainer");
        JsonNode descriptionNode = jsonNode.get("description");
        JsonNode sourceUrlNode = jsonNode.get("source_url");
        JsonNode externalUrlNode = jsonNode.get("external_url");
        JsonNode issuesUrlNode = jsonNode.get("issues_url");
        JsonNode licenceNode = jsonNode.get("license");
        JsonNode dependenciesNode = jsonNode.get("dependencies");
        JsonNode platformsNode = jsonNode.get("platforms");

        String name = nameNode != null ? nameNode.asText() : null;
        String version = versionNode != null ? versionNode.asText() : null;
        String maintainer = maintainerNode != null ? maintainerNode.asText() : null;
        String description = descriptionNode != null ? descriptionNode.asText() : null;
        String sourceUrl = sourceUrlNode != null ? sourceUrlNode.asText() : null;
        String externalUrl = externalUrlNode != null ? externalUrlNode.asText() : null;
        String issuesUrl = issuesUrlNode != null ? issuesUrlNode.asText() : null;
        String licence = licenceNode != null ? licenceNode.asText() : null;

        Map<String, String> dependencies = dependenciesNode != null ? parseIntoMap(jsonParser, dependenciesNode) : null;
        Map<String, String> platforms = platformsNode != null ? parseIntoMap(jsonParser, platformsNode) : null;

        return new MetadataJsonModel(
                name,
                version,
                maintainer,
                description,
                sourceUrl,
                externalUrl,
                issuesUrl,
                licence,
                dependencies,
                platforms
        );
    }

    private Map<String, String> parseIntoMap(JsonParser jsonParser, JsonNode node) throws JsonParseException {
        Map<String, String> map = new HashMap<>();
        Iterator<Map.Entry<String, JsonNode>> iterator = node.fields();
        while (iterator.hasNext()) {
            Map.Entry<String, JsonNode> entry = iterator.next();
            String key = entry.getKey();
            String value;
            if (entry.getValue().isTextual()) {
                value = entry.getValue().asText();
            } else if (entry.getValue().isEmpty()) {
                value = ">= 0.0.0"; // An empty value indicates the dependency / supported system metadata
            } else if (entry.getValue().isContainerNode()) {
                List<String> values = new ArrayList<>();
                Iterator<JsonNode> valueIterator = entry.getValue().elements();
                while (valueIterator.hasNext()) {
                    values.add(valueIterator.next().asText());
                }
                value = String.join(", ", values);
            } else {
                throw new JsonParseException(jsonParser, "Unknown type for child element: " + entry.getValue().toString());
            }
            map.put(key, value);
        }
        return map;
    }

}