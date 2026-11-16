package org.opengroup.osdu.legal.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.core.common.model.legal.Properties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.opengroup.osdu.legal.Constants.LEGAL_QUERY_API_UNION_OPERATOR;
import static org.opengroup.osdu.legal.swagger.LegalTagRequestOpenApiCustomizer.CREATE_PROPERTIES_SCHEMA_NAME;
import static org.opengroup.osdu.legal.swagger.LegalTagRequestOpenApiCustomizer.CREATE_SCHEMA_NAME;
import static org.opengroup.osdu.legal.swagger.LegalTagRequestOpenApiCustomizer.EXPIRATION_DATE_PROPERTY;
import static org.opengroup.osdu.legal.swagger.LegalTagRequestOpenApiCustomizer.OPERATOR_LIST_PROPERTY;
import static org.opengroup.osdu.legal.swagger.LegalTagRequestOpenApiCustomizer.PROPERTIES_FIELD;
import static org.opengroup.osdu.legal.swagger.LegalTagRequestOpenApiCustomizer.QUERY_LIST_PROPERTY;
import static org.opengroup.osdu.legal.swagger.LegalTagRequestOpenApiCustomizer.QUERY_SCHEMA_NAME;
import static org.opengroup.osdu.legal.swagger.LegalTagRequestOpenApiCustomizer.UPDATE_SCHEMA_NAME;

public class LegalTagRequestOpenApiCustomizerTest {

    @Test
    public void should_constrainCreateAndUpdateExpiration_withoutMutatingSharedProperties() {
        Schema<Object> sharedProperties = new ObjectSchema();
        Map<String, Schema> sharedProps = new HashMap<>();
        sharedProps.put("contractId", new StringSchema());
        sharedProps.put("countryOfOrigin", new Schema<>().type("array").items(new StringSchema()).minItems(1));
        sharedProps.put("dataType", new StringSchema());
        sharedProps.put(EXPIRATION_DATE_PROPERTY, new StringSchema().format("date").example("2099-12-25"));
        sharedProps.put("exportClassification", new StringSchema());
        sharedProps.put("originator", new StringSchema());
        sharedProps.put("personalData", new StringSchema());
        sharedProps.put("securityClassification", new StringSchema());
        Schema<?> sharedExtension = new Schema<>().type("object");
        sharedExtension.setAdditionalProperties(new Schema<>().type("object"));
        sharedProps.put(LegalTagRequestOpenApiCustomizer.EXTENSION_PROPERTIES_PROPERTY, sharedExtension);
        sharedProperties.setProperties(sharedProps);
        sharedProperties.setRequired(List.of(
                "contractId",
                "countryOfOrigin",
                "dataType",
                "expirationDate",
                "exportClassification",
                "originator",
                "personalData",
                "securityClassification"));

        Schema<Object> create = new ObjectSchema();
        Map<String, Schema> createProps = new HashMap<>();
        createProps.put(PROPERTIES_FIELD,
                new Schema<>().$ref("#/components/schemas/" + Properties.class.getSimpleName()));
        create.setProperties(createProps);

        Schema<Object> update = new ObjectSchema();
        Map<String, Schema> updateProps = new HashMap<>();
        updateProps.put(EXPIRATION_DATE_PROPERTY, new StringSchema().format("date").example("2099-12-25"));
        Schema<?> extensionProperties = new Schema<>().type("object");
        extensionProperties.setAdditionalProperties(new Schema<>().type("object"));
        updateProps.put(LegalTagRequestOpenApiCustomizer.EXTENSION_PROPERTIES_PROPERTY, extensionProperties);
        update.setProperties(updateProps);

        OpenAPI openAPI = new OpenAPI().components(new Components()
                .addSchemas(Properties.class.getSimpleName(), sharedProperties)
                .addSchemas(CREATE_SCHEMA_NAME, create)
                .addSchemas(UPDATE_SCHEMA_NAME, update));

        LegalTagRequestOpenApiCustomizer.apply(openAPI);
        PropertiesOpenApiCustomizer.apply(openAPI);

        Schema<?> createSchema = openAPI.getComponents().getSchemas().get(CREATE_SCHEMA_NAME);
        assertEquals("#/components/schemas/" + CREATE_PROPERTIES_SCHEMA_NAME,
                createSchema.getProperties().get(PROPERTIES_FIELD).get$ref());

        Schema<?> createProperties = openAPI.getComponents().getSchemas().get(CREATE_PROPERTIES_SCHEMA_NAME);
        assertNull(createProperties.getAllOf());
        assertEquals("object", createProperties.getType());
        assertEquals(Boolean.FALSE, createProperties.getAdditionalProperties());
        assertNotNull(createProperties.getProperties());
        assertEquals(List.of(
                        "contractId",
                        "countryOfOrigin",
                        "dataType",
                        "expirationDate",
                        "exportClassification",
                        "originator",
                        "personalData",
                        "securityClassification"),
                createProperties.getRequired());

        @SuppressWarnings("rawtypes")
        Schema createExpiration = (Schema) createProperties.getProperties().get(EXPIRATION_DATE_PROPERTY);
        assertGeneratorFriendlyExpiration(createExpiration);
        assertNull(createExpiration.getNullable());
        @SuppressWarnings("rawtypes")
        Schema dataType = (Schema) createProperties.getProperties()
                .get(PropertiesOpenApiCustomizer.DATA_TYPE_PROPERTY);
        assertNotNull(dataType.getEnum());
        @SuppressWarnings("rawtypes")
        Schema createExtension = (Schema) createProperties.getProperties()
                .get(LegalTagRequestOpenApiCustomizer.EXTENSION_PROPERTIES_PROPERTY);
        assertEquals(Boolean.TRUE, createExtension.getAdditionalProperties());

        Schema<?> updateSchema = openAPI.getComponents().getSchemas().get(UPDATE_SCHEMA_NAME);
        assertEquals(Boolean.FALSE, updateSchema.getAdditionalProperties());
        @SuppressWarnings("rawtypes")
        Schema updateExpiration = (Schema) updateSchema.getProperties().get(EXPIRATION_DATE_PROPERTY);
        assertGeneratorFriendlyExpiration(updateExpiration);
        assertEquals(Boolean.TRUE, updateExpiration.getNullable());
        assertTrue(updateExpiration.getTypes().contains("null"));
        @SuppressWarnings("rawtypes")
        Schema updateExtension = (Schema) updateSchema.getProperties()
                .get(LegalTagRequestOpenApiCustomizer.EXTENSION_PROPERTIES_PROPERTY);
        assertEquals(Boolean.TRUE, updateExtension.getAdditionalProperties());

        Schema<?> sharedSchema = openAPI.getComponents()
                .getSchemas()
                .get(Properties.class.getSimpleName());
        @SuppressWarnings("rawtypes")
        Schema sharedExpiration = (Schema) sharedSchema.getProperties().get(EXPIRATION_DATE_PROPERTY);
        assertTrue(sharedExpiration.getPattern() == null || sharedExpiration.getPattern().isEmpty());
        assertNull(sharedSchema.getAdditionalProperties());
    }

    @Test
    public void should_documentQueryOperatorsWithoutCaseSensitiveEnum_and_rejectBlankQueryItems() {
        Schema<Object> query = new ObjectSchema();
        Map<String, Schema> queryProps = new HashMap<>();
        queryProps.put(QUERY_LIST_PROPERTY, new Schema<>().type("array").items(new StringSchema()));
        queryProps.put(OPERATOR_LIST_PROPERTY, new Schema<>().type("array").items(new StringSchema()));
        query.setProperties(queryProps);

        OpenAPI openAPI = new OpenAPI().components(new Components().addSchemas(QUERY_SCHEMA_NAME, query));

        LegalTagRequestOpenApiCustomizer.apply(openAPI);

        Schema<?> querySchema = openAPI.getComponents().getSchemas().get(QUERY_SCHEMA_NAME);
        Schema<?> queryItems = querySchema.getProperties().get(QUERY_LIST_PROPERTY).getItems();
        assertEquals(1, queryItems.getMinLength());

        @SuppressWarnings("rawtypes")
        Schema operatorItems = querySchema.getProperties().get(OPERATOR_LIST_PROPERTY).getItems();
        assertNull(operatorItems.getEnum());
        assertEquals(LEGAL_QUERY_API_UNION_OPERATOR, operatorItems.getExample());
        assertNotNull(operatorItems.getDescription());
        assertTrue(operatorItems.getDescription().toLowerCase().contains("case-insensitive"));
    }

    private static void assertGeneratorFriendlyExpiration(Schema<?> expiration) {
        assertEquals("date", expiration.getFormat());
        assertNull(expiration.getPattern());
        assertEquals(LegalTagRequestOpenApiCustomizer.EXPIRATION_EXAMPLE, expiration.getExample());
        assertNotNull(expiration.getDescription());
        assertTrue(expiration.getDescription().contains("future date"));
    }
}
