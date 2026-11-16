package org.opengroup.osdu.legal.config;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.DatabindException;
import java.util.Map;

/**
 * Deserializes a JSON object into {@code Map<String, Object>} and rejects JSON null.
 * Omitted optional fields remain unset; an explicit {@code null} is not an object.
 */
public final class StrictObjectMapDeserializer extends ValueDeserializer<Map<String, Object>> {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    @Override
    public Map<String, Object> deserialize(JsonParser parser, DeserializationContext context) {
        if (!parser.hasToken(JsonToken.START_OBJECT)) {
            return (Map<String, Object>) context.handleUnexpectedToken(Map.class, parser);
        }
        return context.readValue(parser, MAP_TYPE);
    }

    @Override
    public Map<String, Object> getNullValue(DeserializationContext context) {
        throw DatabindException.from(context, "JSON null is not an object");
    }
}
