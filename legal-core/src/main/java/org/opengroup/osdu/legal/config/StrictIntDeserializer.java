package org.opengroup.osdu.legal.config;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.DatabindException;

/**
 * Rejects non-integral JSON numbers for {@code int}/{@link Integer} fields (e.g. {@code 2.5},
 * {@code "10"}, {@code true}, {@code null}).
 */
public final class StrictIntDeserializer extends ValueDeserializer<Integer> {

    @Override
    public Integer deserialize(JsonParser parser, DeserializationContext context) {
        if (!parser.hasToken(JsonToken.VALUE_NUMBER_INT)) {
            return (Integer) context.handleUnexpectedToken(Integer.TYPE, parser);
        }
        // Jackson 3 surfaces int overflow as a stream-level InputCoercionException; keep it a binding error.
        if (parser.getNumberType() != JsonParser.NumberType.INT) {
            throw DatabindException.from(context, "Numeric value out of range of int");
        }
        return parser.getIntValue();
    }

    @Override
    public Integer getNullValue(DeserializationContext context) {
        throw DatabindException.from(context, "JSON null is not an integer");
    }
}
