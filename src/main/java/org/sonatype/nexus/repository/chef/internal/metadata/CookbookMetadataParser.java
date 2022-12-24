package org.sonatype.nexus.repository.chef.internal.metadata;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.*;
import com.google.common.base.*;
import org.slf4j.*;
import org.sonatype.goodies.common.*;
import org.sonatype.nexus.repository.chef.internal.metadata.json.MetadataJsonMapper;
import org.sonatype.nexus.repository.chef.internal.metadata.json.MetadataJsonModel;
import org.sonatype.nexus.repository.chef.internal.metadata.json.MetadataJsonModelCustomDeserializer;
import org.sonatype.nexus.repository.chef.internal.metadata.ruby.MetadataRubyMapper;
import org.sonatype.nexus.repository.chef.internal.metadata.ruby.MetadataRubyModel;
import org.sonatype.nexus.repository.chef.internal.metadata.ruby.MetadataRubyParser;
import org.sonatype.nexus.repository.chef.internal.metadata.util.TgzParser;
import org.sonatype.nexus.repository.view.payloads.*;

import javax.inject.*;
import java.io.*;

import static com.google.common.base.Preconditions.*;
import static java.util.Optional.*;

@Named
@Singleton
public class CookbookMetadataParser {
    protected static final Logger log = Preconditions.checkNotNull(Loggers.getLogger(CookbookMetadataParser.class));

    private final TgzParser tgzParser;
    private final MetadataRubyParser metadataRubyParser;
    private final ObjectMapper objectMapper;

    private static final String METADATA_RB = "/metadata.rb";
    private static final String METADATA_JSON = "/metadata.json";

    @Inject
    public CookbookMetadataParser(final TgzParser tgzParser,
                                  final MetadataRubyParser metadataRubyParser,
                                  final ObjectMapper objectMapper) {
        this.tgzParser = checkNotNull(tgzParser);
        this.metadataRubyParser = checkNotNull(metadataRubyParser);
        this.objectMapper = checkNotNull(objectMapper);
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(MetadataJsonModel.class, new MetadataJsonModelCustomDeserializer());
        this.objectMapper.registerModule(simpleModule);
    }

    public CookbookMetadata getMetadataFromTarballBlob(TempBlob blob) throws IOException {
        /* A supermarket cookbook can have both metadata.rb and metadata.json files, with
           some metadata only present in one of the files. Thus, we need to try to parse
           both files, and if both are present, combine their data.
           If the data differs, we use the metadata.json data, as the Ruby metadata file
           can in some cases contain ruby code.
         */

        InputStream rubyMetadataFileInputStream = tgzParser.getFileFromInputStream(blob.get(), METADATA_RB);
        InputStream jsonMetadataFileInputStream = tgzParser.getFileFromInputStream(blob.get(), METADATA_JSON);

        if (streamIsNotEmpty(rubyMetadataFileInputStream)) {
            CookbookMetadata rubyMetadata = parseMetadataRb(rubyMetadataFileInputStream);
            if (streamIsNotEmpty(jsonMetadataFileInputStream)) {
                CookbookMetadata jsonMetadata = parseMetadataJson(jsonMetadataFileInputStream);
                return mergeCookbookMetadataFileContents(rubyMetadata, jsonMetadata);
            } else {
                assureCookbookMetadataContainsRequiredFields(rubyMetadata);
                log.debug(String.format("Returning CookbookMetadata from metadata.rb: %s", rubyMetadata));
                return rubyMetadata;
            }
        } else if (streamIsNotEmpty(jsonMetadataFileInputStream)) {
            CookbookMetadata jsonMetadata = parseMetadataJson(jsonMetadataFileInputStream);
            assureCookbookMetadataContainsRequiredFields(jsonMetadata);
            log.debug(String.format("Returning CookbookMetadata from metadata.json: %s", jsonMetadata));
            return jsonMetadata;
        } else {
            throw new IOException("No metadata file found in archive");
        }
    }

    private CookbookMetadata parseMetadataRb(InputStream io) throws IOException {
        MetadataRubyModel rubyMetadata = metadataRubyParser.parseMetadataRubyInputStream(io);
        io.close();
        return MetadataRubyMapper.convertToCookbookMetadata(rubyMetadata);
    }

    private CookbookMetadata parseMetadataJson(InputStream io) throws IOException {
        MetadataJsonModel jsonMetadata = this.objectMapper.configure(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY, false).readValue(io, MetadataJsonModel.class);
        io.close();
        return MetadataJsonMapper.convertToCookbookMetadata(jsonMetadata);
    }

    private CookbookMetadata mergeCookbookMetadataFileContents(CookbookMetadata rubyMetadata, CookbookMetadata jsonMetadata) throws IOException {
        log.info("Both metadata.rb file and metadata.json file found, merging them.");
        CookbookMetadata cookbookMetadata = new CookbookMetadata(
                getMetadataValueFromTwoSources(rubyMetadata.getName(), jsonMetadata.getName()),
                getMetadataValueFromTwoSources(rubyMetadata.getVersion(), jsonMetadata.getVersion()),
                getMetadataValueFromTwoSources(rubyMetadata.getMaintainer(), jsonMetadata.getMaintainer()),
                getMetadataValueFromTwoSources(rubyMetadata.getDescription(), jsonMetadata.getDescription()),
                getMetadataValueFromTwoSources(rubyMetadata.getSourceUrl(), jsonMetadata.getSourceUrl()),
                getMetadataValueFromTwoSources(rubyMetadata.getExternalUrl(), jsonMetadata.getExternalUrl()),
                getMetadataValueFromTwoSources(rubyMetadata.getIssuesUrl(), jsonMetadata.getIssuesUrl()),
                getMetadataValueFromTwoSources(rubyMetadata.getLicence(), jsonMetadata.getLicence()),
                getMetadataValueFromTwoSources(rubyMetadata.getDepends(), jsonMetadata.getDepends()),
                getMetadataValueFromTwoSources(rubyMetadata.getSupports(), jsonMetadata.getSupports())
        );
        log.info(String.format("Successfully merged metadata files into CookbookMetadata: %s", cookbookMetadata));
        assureCookbookMetadataContainsRequiredFields(cookbookMetadata);
        return cookbookMetadata;
    }

    // TODO This helper method needs a better name
    private String getMetadataValueFromTwoSources(String fromRuby, String fromJson) {
        if (fromRuby == null) {
            log.debug(String.format("Value from metadata.rb is null, using value from metadata.json. metadata.rb=null, metadata.json=%s",
                    ofNullable(fromJson).orElse("null")));
            return fromJson;
        } else if (fromJson == null) {
            log.debug(String.format("Value from metadata.json is null, using value from metadata.rb. metadata.rb=%s, metadata.json=null",
                    ofNullable(fromRuby).orElse("null")));
            return fromRuby;
        } else if (fromRuby.equals(fromJson)) {
            log.debug(String.format("Values from metadata.rb and metadata.json are equal. value=%s", fromRuby));
            return fromRuby;
        } else {
            log.warn(String.format("Different metadata values found for same metadata type: metadata.rb=%s, metadata.json=%s. Using %s.", fromRuby, fromJson, fromJson));
            return fromJson;
        }
    }

    private void assureCookbookMetadataContainsRequiredFields(CookbookMetadata cookbookMetadata) throws IOException {
        if (isNullOrEmptyString(cookbookMetadata.getName()) || isNullOrEmptyString(cookbookMetadata.getVersion())) {
            throw new IOException(String.format(
                    "Empty/null values not allowed in cookbook name or version, name=%s, version=%s",
                    ofNullable(cookbookMetadata.getName()).orElse("null"),
                    ofNullable(cookbookMetadata.getVersion()).orElse("null")
            ));
        }
    }

    private boolean isNullOrEmptyString(String s) {
        return (s == null || s.trim().isEmpty());
    }

    private boolean streamIsNotEmpty(InputStream io) {
        return io != null;
    }


}