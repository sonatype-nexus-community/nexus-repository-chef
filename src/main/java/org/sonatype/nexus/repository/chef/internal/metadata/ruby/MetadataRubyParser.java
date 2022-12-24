package org.sonatype.nexus.repository.chef.internal.metadata.ruby;

import com.google.common.base.*;
import groovy.lang.Singleton;
import org.slf4j.*;
import org.sonatype.goodies.common.*;

import javax.inject.*;
import java.io.*;
import java.util.*;
import java.util.stream.*;

@Named
@Singleton
public class MetadataRubyParser {
    protected static final Logger log = Preconditions.checkNotNull(Loggers.getLogger(MetadataRubyParser.class));

    public static final String NAME = "name";
    public static final String VERSION = "version";
    public static final String MAINTAINER = "maintainer";
    public static final String DESCRIPTION = "description";
    public static final String SOURCE_URL = "source_url";
    public static final String EXTERNAL_URL = "external_url";
    public static final String ISSUES_URL = "issues_url";
    public static final String LICENCE = "license";
    public static final String DEPENDS = "depends";
    public static final String SUPPORTS = "supports";

    // To prevent infinite loops in case of malformed metadata files
    private static final Integer MAX_METADATA_ROWS_TO_PARSE = 1000;

    public MetadataRubyModel parseMetadataRubyInputStream(InputStream io) throws IOException {
        MetadataRubyBuilder metadataRubyBuilder = new MetadataRubyBuilder();
        StreamTokenizer tokenizer = createAndSetupNewTokenizer(io);
        int rowsParsed = 0;
        while (tokenizer.ttype != StreamTokenizer.TT_EOF && rowsParsed <= MAX_METADATA_ROWS_TO_PARSE) {
            if (tokenizer.ttype == StreamTokenizer.TT_WORD) {
                parseRow(tokenizer, metadataRubyBuilder);
            } else {
                tokenizer.nextToken();
            }
            rowsParsed++;
        }
        return metadataRubyBuilder.buildMetadataRubyModel();
    }

    private void parseRow(StreamTokenizer tokenizer, MetadataRubyBuilder metadataBuilder) throws IOException {
        // First token in each row will be the metadata type, e.g. name or maintainer
        String metadataType = tokenizer.sval.toLowerCase();

        // The rest of the tokens until EOL/EOF is the metadata value
        List<String> metadataValuesList = new ArrayList<>();
        for (int nextTokenType = tokenizer.nextToken();
             nextTokenType != StreamTokenizer.TT_EOL && nextTokenType != StreamTokenizer.TT_EOF;
             nextTokenType = tokenizer.nextToken()) {
            metadataValuesList.add(tokenizer.sval);
        }

        String metadataValue = formatMetadataValueListAsString(metadataValuesList);

        switch (metadataType) {
            case NAME:
                metadataBuilder.setName(metadataValue);
                break;
            case VERSION:
                metadataBuilder.setVersion(metadataValue);
                break;
            case MAINTAINER:
                metadataBuilder.setMaintainer(metadataValue);
                break;
            case DESCRIPTION:
                metadataBuilder.setDescription(metadataValue);
                break;
            case SOURCE_URL:
                metadataBuilder.setSourceUrl(metadataValue);
                break;
            case EXTERNAL_URL:
                metadataBuilder.setExternalUrl(metadataValue);
                break;
            case ISSUES_URL:
                metadataBuilder.setIssuesUrl(metadataValue);
                break;
            case LICENCE:
                metadataBuilder.setLicence(metadataValue);
                break;
            case DEPENDS:
                metadataBuilder.addDependency(metadataValue);
                break;
            case SUPPORTS:
                metadataBuilder.addSupports(metadataValue);
                break;
            default:
                log.info(String.format("Ignoring unknown / not relevant metadata, keyword= %s, value= %s", metadataType, metadataValue));
                break;
        }
    }

    private static String formatMetadataValueListAsString(List<String> list) {
        return list.stream()
                .map(MetadataRubyParser::addSpaceIfStringEndsWithComma)
                .collect(Collectors.joining());
    }

    private static String addSpaceIfStringEndsWithComma(String s) {
        return s.endsWith(",") ? s.concat(" ") : s;
    }

    private StreamTokenizer createAndSetupNewTokenizer(InputStream io) {
        StreamTokenizer tokenizer = new StreamTokenizer(new InputStreamReader(io));
        tokenizer.resetSyntax();
        tokenizer.wordChars('!', '~'); // we consider all common punctuation as part of words
        tokenizer.whitespaceChars(0, ' '); // control chars and regular space " " are parsed as whitespace
        tokenizer.commentChar('#'); // Ruby comment lines
        tokenizer.quoteChar('\''); // single quotes encapsulate one word token
        tokenizer.quoteChar('"'); // so does double quotes
        tokenizer.eolIsSignificant(true); // to be able to parse per row
        return tokenizer;
    }
}
