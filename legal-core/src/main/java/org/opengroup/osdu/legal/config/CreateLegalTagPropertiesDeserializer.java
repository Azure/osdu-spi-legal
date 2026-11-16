package org.opengroup.osdu.legal.config;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.JsonNode;
import org.opengroup.osdu.core.common.model.legal.Properties;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Deserializes create-request {@link Properties} without Jackson coercing non-strings to
 * strings (e.g. {@code "originator": false} → {@code "false"}). Explicit JSON null for
 * {@code extensionProperties} is also rejected (schema type is object, not nullable).
 *
 * <p>Unknown property names are rejected so create matches OpenAPI
 * {@code CreateLegalTagProperties.additionalProperties: false}. Shared {@link Properties}
 * keeps {@code @JsonIgnoreProperties(ignoreUnknown = true)} for response/persistence paths.
 */
public final class CreateLegalTagPropertiesDeserializer extends ValueDeserializer<Properties> {

    private static final String EXTENSION_PROPERTIES = "extensionProperties";
    private static final String COUNTRY_OF_ORIGIN = "countryOfOrigin";

    private static final List<String> STRING_PROPERTIES = List.of(
            "contractId",
            "originator",
            "dataType",
            "securityClassification",
            "personalData",
            "exportClassification",
            "expirationDate");

    /** Declared fields on {@link Properties} / CreateLegalTagProperties. */
    private static final Set<String> ALLOWED_PROPERTIES;

    static {
        Set<String> allowed = new HashSet<>(STRING_PROPERTIES);
        allowed.add(COUNTRY_OF_ORIGIN);
        allowed.add(EXTENSION_PROPERTIES);
        ALLOWED_PROPERTIES = Set.copyOf(allowed);
    }

    @Override
    public Properties deserialize(JsonParser parser, DeserializationContext context) {
        if (!parser.hasToken(JsonToken.START_OBJECT)) {
            return (Properties) context.handleUnexpectedToken(Properties.class, parser);
        }
        JsonNode node = context.readTree(parser);
        rejectUnknownProperties(parser, node);
        rejectInvalidPropertyTypes(parser, node);
        return context.readTreeAsValue(node, Properties.class);
    }

    @Override
    public Properties getNullValue(DeserializationContext context) {
        throw DatabindException.from(context, "JSON null is not an object");
    }

    private static void rejectUnknownProperties(JsonParser parser, JsonNode node) {
        for (String name : node.propertyNames()) {
            if (!ALLOWED_PROPERTIES.contains(name)) {
                throw DatabindException.from(parser, "Unrecognized field \"" + name + "\"");
            }
        }
    }

    private static void rejectInvalidPropertyTypes(JsonParser parser, JsonNode node) {
        for (String property : STRING_PROPERTIES) {
            if (!node.has(property)) {
                continue;
            }
            if (!node.get(property).isString()) {
                throw DatabindException.from(parser,
                        "JSON value is not a string for property '" + property + "'");
            }
        }
        if (node.has(COUNTRY_OF_ORIGIN)) {
            JsonNode countryOfOrigin = node.get(COUNTRY_OF_ORIGIN);
            if (!countryOfOrigin.isArray()) {
                throw DatabindException.from(parser,
                        "JSON value is not an array for property '" + COUNTRY_OF_ORIGIN + "'");
            }
            for (JsonNode item : countryOfOrigin) {
                if (!item.isString()) {
                    throw DatabindException.from(parser,
                            "JSON value is not a string for property '" + COUNTRY_OF_ORIGIN + "' items");
                }
            }
        }
        if (node.has(EXTENSION_PROPERTIES) && !node.get(EXTENSION_PROPERTIES).isObject()) {
            throw DatabindException.from(parser,
                    "JSON value is not an object for property '" + EXTENSION_PROPERTIES + "'");
        }
    }
}
