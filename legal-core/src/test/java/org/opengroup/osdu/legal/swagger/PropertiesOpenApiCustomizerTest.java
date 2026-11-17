package org.opengroup.osdu.legal.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.core.common.model.legal.AllowedLegaltagPropertyValues;
import org.opengroup.osdu.core.common.model.legal.Properties;
import org.opengroup.osdu.legal.countries.Country;
import org.opengroup.osdu.legal.countries.DefaultCountriesRepository;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.opengroup.osdu.legal.swagger.PropertiesOpenApiCustomizer.CONTRACT_ID_PROPERTY;
import static org.opengroup.osdu.legal.swagger.PropertiesOpenApiCustomizer.COUNTRY_OF_ORIGIN_PROPERTY;
import static org.opengroup.osdu.legal.swagger.PropertiesOpenApiCustomizer.CREATE_PROPERTIES_SCHEMA_NAME;
import static org.opengroup.osdu.legal.swagger.PropertiesOpenApiCustomizer.DATA_TYPE_PROPERTY;
import static org.opengroup.osdu.legal.swagger.PropertiesOpenApiCustomizer.EXPORT_CLASSIFICATION_PROPERTY;
import static org.opengroup.osdu.legal.swagger.PropertiesOpenApiCustomizer.ORIGINATOR_PROPERTY;
import static org.opengroup.osdu.legal.swagger.PropertiesOpenApiCustomizer.PERSONAL_DATA_PROPERTY;
import static org.opengroup.osdu.legal.swagger.PropertiesOpenApiCustomizer.PROPERTIES_SCHEMA_NAME;
import static org.opengroup.osdu.legal.swagger.PropertiesOpenApiCustomizer.SECURITY_CLASSIFICATION_PROPERTY;

public class PropertiesOpenApiCustomizerTest {

    @Test
    public void should_includeUsAndExcludeAa_when_buildingAllowedCountryCodes() {
        List<String> codes = PropertiesOpenApiCustomizer.allowedCountryCodes(
                new DefaultCountriesRepository().read());

        assertTrue(codes.contains("US"));
        assertTrue(codes.contains("CA"));
        assertFalse(codes.contains("AA"));
    }

    @Test
    public void should_setCountryOfOriginItemsEnum_onCreateProperties_only() {
        OpenAPI openAPI = openApiWithCreateProperties(
                COUNTRY_OF_ORIGIN_PROPERTY, new Schema<>().type("array").items(new StringSchema()).minItems(1),
                EXPORT_CLASSIFICATION_PROPERTY, new StringSchema().example("EAR99"));

        PropertiesOpenApiCustomizer.apply(openAPI);

        @SuppressWarnings("rawtypes")
        Schema createCountryOfOrigin = (Schema) createProperties(openAPI)
                .getProperties()
                .get(COUNTRY_OF_ORIGIN_PROPERTY);
        Schema<?> items = createCountryOfOrigin.getItems();

        assertNotNull(items.getEnum());
        assertTrue(items.getEnum().contains("US"));
        assertFalse(items.getEnum().contains("AA"));
        assertTrue(items.getPattern() == null || items.getPattern().isEmpty());
        assertTrue("array".equals(createCountryOfOrigin.getType())
                || (createCountryOfOrigin.getTypes() != null
                && createCountryOfOrigin.getTypes().contains("array")));
        assertEquals(1, createCountryOfOrigin.getMinItems());

        @SuppressWarnings("rawtypes")
        Schema sharedCountryOfOrigin = (Schema) openAPI.getComponents()
                .getSchemas()
                .get(PROPERTIES_SCHEMA_NAME)
                .getProperties()
                .get(COUNTRY_OF_ORIGIN_PROPERTY);
        assertNull(sharedCountryOfOrigin.getItems().getEnum());
    }

    @Test
    public void should_setExportClassificationEnum_onCreateProperties_and_disallowAdditionalProperties() {
        OpenAPI openAPI = openApiWithCreateProperties(
                EXPORT_CLASSIFICATION_PROPERTY, new StringSchema().example("EAR99"));

        PropertiesOpenApiCustomizer.apply(openAPI);

        Schema<?> create = createProperties(openAPI);
        assertEquals(Boolean.FALSE, create.getAdditionalProperties());
        assertNull(create.getAllOf());

        @SuppressWarnings("rawtypes")
        Schema exportClassification = (Schema) create.getProperties().get(EXPORT_CLASSIFICATION_PROPERTY);

        assertNotNull(exportClassification.getEnum());
        assertTrue(exportClassification.getEnum().contains(AllowedLegaltagPropertyValues.ECCN_EAR99));
        assertTrue(exportClassification.getEnum().contains(AllowedLegaltagPropertyValues.ECCN_0A998));
        assertTrue(exportClassification.getEnum().contains(AllowedLegaltagPropertyValues.NOT_TECHNICAL_DATA));
        assertTrue(exportClassification.getEnum().contains(AllowedLegaltagPropertyValues.NO_LICENSE_REQUIRED));
        assertFalse(exportClassification.getEnum().contains(""));

        Schema<?> sharedProperties = openAPI.getComponents().getSchemas().get(PROPERTIES_SCHEMA_NAME);
        assertNull(sharedProperties.getAdditionalProperties());
        @SuppressWarnings("rawtypes")
        Schema sharedExport = (Schema) sharedProperties.getProperties().get(EXPORT_CLASSIFICATION_PROPERTY);
        assertNull(sharedExport.getEnum());
    }

    @Test
    public void should_setPersonalDataEnum_and_contractIdPattern_onCreateProperties_only() {
        OpenAPI openAPI = openApiWithCreateProperties(
                PERSONAL_DATA_PROPERTY, new StringSchema().example("No Personal Data"),
                CONTRACT_ID_PROPERTY, new StringSchema().example("A1234"),
                ORIGINATOR_PROPERTY, new StringSchema().example("Equinor"),
                SECURITY_CLASSIFICATION_PROPERTY, new StringSchema().example("Private"),
                DATA_TYPE_PROPERTY, new StringSchema().example("Third Party Data"));

        PropertiesOpenApiCustomizer.apply(openAPI);

        Schema<?> create = createProperties(openAPI);

        @SuppressWarnings("rawtypes")
        Schema personalData = (Schema) create.getProperties().get(PERSONAL_DATA_PROPERTY);
        assertNotNull(personalData.getEnum());
        assertTrue(personalData.getEnum().contains(AllowedLegaltagPropertyValues.NO_PERSONAL_DATA));
        assertTrue(personalData.getEnum().contains(AllowedLegaltagPropertyValues.PERSONALLY_IDENTIFIABLE));
        assertFalse(personalData.getEnum().contains(""));

        @SuppressWarnings("rawtypes")
        Schema contractId = (Schema) create.getProperties().get(CONTRACT_ID_PROPERTY);
        assertEquals(PropertiesOpenApiCustomizer.CONTRACT_ID_PATTERN, contractId.getPattern());
        assertEquals(3, contractId.getMinLength());
        assertEquals(40, contractId.getMaxLength());

        @SuppressWarnings("rawtypes")
        Schema originator = (Schema) create.getProperties().get(ORIGINATOR_PROPERTY);
        assertEquals(PropertiesOpenApiCustomizer.ORIGINATOR_PATTERN, originator.getPattern());
        assertEquals(3, originator.getMinLength());
        assertEquals(60, originator.getMaxLength());

        @SuppressWarnings("rawtypes")
        Schema security = (Schema) create.getProperties().get(SECURITY_CLASSIFICATION_PROPERTY);
        assertTrue(security.getEnum().contains(AllowedLegaltagPropertyValues.PUBLIC));
        assertFalse(security.getEnum().contains(AllowedLegaltagPropertyValues.SECRET));

        Schema<?> sharedProperties = openAPI.getComponents().getSchemas().get(PROPERTIES_SCHEMA_NAME);
        assertNull(((Schema) sharedProperties.getProperties().get(PERSONAL_DATA_PROPERTY)).getEnum());
        assertNull(((Schema) sharedProperties.getProperties().get(CONTRACT_ID_PROPERTY)).getPattern());
        assertNull(((Schema) sharedProperties.getProperties().get(DATA_TYPE_PROPERTY)).getEnum());
    }

    @Test
    public void should_excludeEmbargoedCountries_fromAllowedCodes() {
        List<Country> countries = List.of(
                new Country("United States", "US", 840, Country.RESIDENCY_RISK.NO_RESTRICTION, List.of()),
                new Country("Embargoed Land", "ZZ", 999, Country.RESIDENCY_RISK.EMBARGOED, List.of()),
                new Country("Fake", "AA", 0, Country.RESIDENCY_RISK.DEFAULT, List.of()));

        List<String> codes = PropertiesOpenApiCustomizer.allowedCountryCodes(countries);

        assertTrue(codes.contains("US"));
        assertFalse(codes.contains("ZZ"));
        assertFalse(codes.contains("AA"));
    }

    @Test
    public void should_notConstrainExpirationDate_onSharedPropertiesSchema() {
        StringSchema original = new StringSchema();
        original.setFormat("date");
        original.setExample("2099-12-25");
        OpenAPI openAPI = openApiWithCreateProperties("expirationDate", original);

        PropertiesOpenApiCustomizer.apply(openAPI);

        @SuppressWarnings("rawtypes")
        Schema expiration = (Schema) openAPI.getComponents()
                .getSchemas()
                .get(PROPERTIES_SCHEMA_NAME)
                .getProperties()
                .get("expirationDate");

        assertEquals(original, expiration);
        assertTrue(expiration.getPattern() == null || expiration.getPattern().isEmpty());
    }

    @Test
    public void should_preserveExistingExpirationOverride_when_applyingConstraints() {
        OpenAPI openAPI = openApiWithCreateProperties(
                DATA_TYPE_PROPERTY, new StringSchema().example("Third Party Data"));
        Schema<?> expiration = new StringSchema().format("date").example("2099-12-25");
        createProperties(openAPI).getProperties().put("expirationDate", expiration);

        PropertiesOpenApiCustomizer.apply(openAPI);

        assertEquals(expiration, createProperties(openAPI).getProperties().get("expirationDate"));
        @SuppressWarnings("rawtypes")
        Schema dataType = (Schema) createProperties(openAPI).getProperties().get(DATA_TYPE_PROPERTY);
        assertNotNull(dataType.getEnum());
    }

    @SuppressWarnings("rawtypes")
    private static Schema createProperties(OpenAPI openAPI) {
        return (Schema) openAPI.getComponents().getSchemas().get(CREATE_PROPERTIES_SCHEMA_NAME);
    }

    private static OpenAPI openApiWithCreateProperties(Object... nameAndSchemaPairs) {
        Schema<Object> properties = new ObjectSchema();
        Map<String, Schema> props = new HashMap<>();
        for (int i = 0; i < nameAndSchemaPairs.length; i += 2) {
            props.put((String) nameAndSchemaPairs[i], (Schema) nameAndSchemaPairs[i + 1]);
        }
        properties.setProperties(props);

        Schema<?> createProperties = new Schema<>();
        createProperties.setType("object");
        createProperties.setAdditionalProperties(Boolean.FALSE);
        createProperties.setProperties(new LinkedHashMap<>(props));

        return new OpenAPI().components(new Components()
                .addSchemas(PROPERTIES_SCHEMA_NAME, properties)
                .addSchemas(CREATE_PROPERTIES_SCHEMA_NAME, createProperties));
    }
}
