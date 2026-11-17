package org.opengroup.osdu.legal.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import lombok.experimental.UtilityClass;
import org.opengroup.osdu.core.common.model.legal.Properties;
import org.opengroup.osdu.legal.tags.dto.CreateLegalTag;
import org.opengroup.osdu.legal.tags.dto.QueryLegalTag;
import org.opengroup.osdu.legal.tags.dto.UpdateLegalTag;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import static org.opengroup.osdu.legal.Constants.LEGAL_QUERY_API_ADD_OPERATOR;
import static org.opengroup.osdu.legal.Constants.LEGAL_QUERY_API_INTERSECTION_OPERATOR;
import static org.opengroup.osdu.legal.Constants.LEGAL_QUERY_API_UNION_OPERATOR;

/**
 * Request-only OpenAPI tightening for create/update LegalTag bodies.
 * Shared {@link Properties} stays unconstrained so responses may return invalid tags,
 * case-preserved enum values, and expiration dates such as {@code 9999-12-31}.
 *
 * <p>Creates {@code CreateLegalTagProperties} as a single object schema (properties copied
 * from {@link Properties}, then overridden). Do not use {@code allOf} with
 * {@code additionalProperties: false}: validators do not treat {@code allOf} member
 * properties as declared, so every field would be rejected.
 * {@link PropertiesOpenApiCustomizer} then adds allowlist enums/patterns to that schema.
 *
 * <p>Expiration stays {@code format: date} without a year-enumeration regex: those patterns
 * make Schemathesis/Hypothesis generation unsatisfiable ({@code filter_too_much}). Runtime
 * validation still enforces a future date (or the no-expiration sentinel).
 */
@UtilityClass
final class LegalTagRequestOpenApiCustomizer {

    static final String CREATE_SCHEMA_NAME = CreateLegalTag.class.getSimpleName();
    static final String CREATE_PROPERTIES_SCHEMA_NAME = "CreateLegalTagProperties";
    static final String UPDATE_SCHEMA_NAME = UpdateLegalTag.class.getSimpleName();
    static final String QUERY_SCHEMA_NAME = QueryLegalTag.class.getSimpleName();
    static final String PROPERTIES_FIELD = "properties";
    static final String EXPIRATION_DATE_PROPERTY = "expirationDate";
    static final String EXTENSION_PROPERTIES_PROPERTY = "extensionProperties";
    static final String QUERY_LIST_PROPERTY = "queryList";
    static final String OPERATOR_LIST_PROPERTY = "operatorList";
    static final List<String> QUERY_OPERATORS = List.of(
            LEGAL_QUERY_API_UNION_OPERATOR,
            LEGAL_QUERY_API_INTERSECTION_OPERATOR,
            LEGAL_QUERY_API_ADD_OPERATOR);

    static final String EXPIRATION_EXAMPLE = "2099-12-25";
    static final String EXPIRATION_DESCRIPTION_SUFFIX =
            " Must be a future date (UTC), or 9999-12-31 for no expiration.";

    static void apply(OpenAPI openApi) {
        applyCreate(openApi);
        applyUpdate(openApi);
        applyQuery(openApi);
    }

    private static void applyCreate(OpenAPI openApi) {
        Schema<?> create = openApi.getComponents().getSchemas().get(CREATE_SCHEMA_NAME);
        if (create == null || create.getProperties() == null) {
            return;
        }

        Schema<?> sharedProperties = openApi.getComponents().getSchemas()
                .get(Properties.class.getSimpleName());
        Schema<?> createProperties = new Schema<>();
        createProperties.setType("object");
        createProperties.setAdditionalProperties(Boolean.FALSE);
        createProperties.setProperties(copyProperties(sharedProperties));
        if (sharedProperties != null && sharedProperties.getRequired() != null) {
            createProperties.setRequired(List.copyOf(sharedProperties.getRequired()));
        }
        createProperties.getProperties().put(EXPIRATION_DATE_PROPERTY,
                futureExpirationSchema(propertyDescription(sharedProperties, EXPIRATION_DATE_PROPERTY), false));
        allowOpenExtensionProperties(createProperties);

        openApi.getComponents().addSchemas(CREATE_PROPERTIES_SCHEMA_NAME, createProperties);
        create.getProperties().put(PROPERTIES_FIELD,
                new Schema<>().$ref("#/components/schemas/" + CREATE_PROPERTIES_SCHEMA_NAME));
    }

    private static void applyUpdate(OpenAPI openApi) {
        Schema<?> update = openApi.getComponents().getSchemas().get(UPDATE_SCHEMA_NAME);
        if (update == null || update.getProperties() == null) {
            return;
        }
        update.setAdditionalProperties(Boolean.FALSE);
        update.getProperties().put(EXPIRATION_DATE_PROPERTY,
                futureExpirationSchema(propertyDescription(update, EXPIRATION_DATE_PROPERTY), true));
        allowOpenExtensionProperties(update);
    }

    private static void applyQuery(OpenAPI openApi) {
        Schema<?> query = openApi.getComponents().getSchemas().get(QUERY_SCHEMA_NAME);
        if (query == null || query.getProperties() == null) {
            return;
        }

        Schema<?> queryList = query.getProperties().get(QUERY_LIST_PROPERTY);
        if (queryList != null) {
            Schema<?> items = queryList.getItems();
            if (items == null) {
                items = new StringSchema();
                queryList.setItems(items);
            }
            items.setMinLength(1);
        }

        Schema<?> operatorList = query.getProperties().get(OPERATOR_LIST_PROPERTY);
        if (operatorList != null) {
            // Match runtime: no case-sensitive enum; only the first element is applied via
            // case-insensitive substring match against union / intersection / add.
            StringSchema items = new StringSchema();
            items.setExample(LEGAL_QUERY_API_UNION_OPERATOR);
            items.setDescription(
                    "First list element only. Case-insensitive substring match for "
                            + String.join(", ", QUERY_OPERATORS) + ".");
            operatorList.setItems(items);
            if (operatorList.getDescription() == null || operatorList.getDescription().isBlank()) {
                operatorList.setDescription(
                        "Logical operators for joining queryList conditions. Only the first "
                                + "element is used (case-insensitive substring match).");
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Map<String, Schema> copyProperties(Schema<?> source) {
        Map<String, Schema> copy = new LinkedHashMap<>();
        if (source == null || source.getProperties() == null) {
            return copy;
        }
        copy.putAll(source.getProperties());
        return copy;
    }

    /**
     * @param nullable update may send JSON null to leave the stored date unchanged;
     *                 create stays a non-null date string
     */
    private static Schema<?> futureExpirationSchema(String description, boolean nullable) {
        StringSchema schema = new StringSchema();
        schema.setFormat("date");
        schema.setExample(EXPIRATION_EXAMPLE);
        schema.setDescription(withExpirationConstraint(description));
        if (nullable) {
            // OAS 3.1: null is a type, not the 3.0 nullable keyword.
            schema.setTypes(new LinkedHashSet<>(List.of("string", "null")));
            schema.setNullable(true);
        }
        return schema;
    }

    static String withExpirationConstraint(String description) {
        if (description == null || description.isBlank()) {
            return "The date the LegalTag expires." + EXPIRATION_DESCRIPTION_SUFFIX;
        }
        if (description.contains("future date")) {
            return description;
        }
        return description + EXPIRATION_DESCRIPTION_SUFFIX;
    }

    private static String propertyDescription(Schema<?> schema, String propertyName) {
        if (schema == null || schema.getProperties() == null) {
            return null;
        }
        Schema<?> existing = schema.getProperties().get(propertyName);
        return existing != null ? existing.getDescription() : null;
    }

    /** Match Properties / runtime: extension values may be string, boolean, array, or object. */
    private static void allowOpenExtensionProperties(Schema<?> parent) {
        Schema<?> extensionProperties = parent.getProperties().get(EXTENSION_PROPERTIES_PROPERTY);
        if (extensionProperties != null) {
            extensionProperties.setAdditionalProperties(Boolean.TRUE);
        }
    }
}
