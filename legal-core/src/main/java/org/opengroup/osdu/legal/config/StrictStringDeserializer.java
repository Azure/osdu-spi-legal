package org.opengroup.osdu.legal.config;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.DatabindException;

/**
 * Rejects non-string JSON tokens for {@link String} fields (e.g. {@code false}, {@code 123},
 * {@code null}). Omitted optional fields remain unset; an explicit JSON null is not a string.
 */
public final class StrictStringDeserializer extends ValueDeserializer<String> {

    @Override
    public String deserialize(JsonParser parser, DeserializationContext context) {
        if (!parser.hasToken(JsonToken.VALUE_STRING)) {
            return (String) context.handleUnexpectedToken(String.class, parser);
        }
        return parser.getString();
    }

    @Override
    public String getNullValue(DeserializationContext context) {
        throw DatabindException.from(context, "JSON null is not a string");
    }
}
