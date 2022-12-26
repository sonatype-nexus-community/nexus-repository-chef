package org.sonatype.nexus.repository.chef.internal.util;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.sonatype.goodies.common.Loggers;

import java.util.*;

public class AttributesHelper {

    protected static final Logger log = Preconditions.checkNotNull(Loggers.getLogger(AttributesHelper.class));

    /*
    Small helper function to trim whitespace from attributes as well as storing empty
    strings as null.
     */
    public static String standardizeAttributeValue(String s) {
        return (s == null || s.trim().equals("")) ? null : s.trim();
    }

    // Parse the dependencies / supports string into a Map
    public static Map<String, String> parseStringIntoMap(String string) {
        Map<String, String> map = new HashMap<>();
        if (string != null && !string.trim().isEmpty()) {
            log.debug(String.format("string to parse as dependecy/supports: %s", string));
            Arrays.stream(string.split(";")).map(String::trim).forEach(stringItem -> {
                String[] parts = stringItem.split(",");
                switch (parts.length) {
                    case 1:
                        map.put(parts[0].trim(), ">= 0.0.0");
                        break;
                    case 2:
                        map.put(parts[0].trim(), parts[1].trim());
                        break;
                    default:
                        log.warn(String.format("String malformed: %s", string));
                }
            });
        }
        return map;
    }

}
