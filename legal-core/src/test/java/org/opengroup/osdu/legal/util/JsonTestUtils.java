package org.opengroup.osdu.legal.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;

/** Object fixtures serialized through JSON so tests still exercise request deserializers. */
public final class JsonTestUtils {
    // Omit unset domain fields, but retain explicit null values in negative-test overrides.
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .setTimeZone(TimeZone.getDefault())
            .setDefaultPropertyInclusion(
                    JsonInclude.Value.construct(JsonInclude.Include.NON_NULL, JsonInclude.Include.ALWAYS));

    private JsonTestUtils() {}

    public static String json(Object body) {
        try {
            return MAPPER.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Cannot serialize test fixture", e);
        }
    }

    /** Copies a fixture and sets a field, including an explicit null value. */
    public static Map<String, Object> withField(Object body, String field, Object value) {
        Map<String, Object> result = MAPPER.convertValue(body,
                new TypeReference<LinkedHashMap<String, Object>>() {});
        result.put(field, value);
        return result;
    }
}
