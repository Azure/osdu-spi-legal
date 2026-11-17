package org.opengroup.osdu.legal.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import lombok.experimental.UtilityClass;
import org.opengroup.osdu.core.common.model.legal.AllowedLegaltagPropertyValues;
import org.opengroup.osdu.core.common.model.legal.DataTypeValues;
import org.opengroup.osdu.core.common.model.legal.Properties;
import org.opengroup.osdu.legal.countries.Country;
import org.opengroup.osdu.legal.countries.DefaultCountriesRepository;
import org.opengroup.osdu.legal.tags.dto.UpdateLegalTag;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * Request-only OpenAPI tightening for LegalTag property allowlists/patterns.
 * Applies to {@code CreateLegalTagProperties}; shared {@link Properties} stays
 * permissive so responses (including invalid tags and case-preserved values) remain
 * schema-valid for generated clients.
 *
 * <p>Requires {@link LegalTagRequestOpenApiCustomizer} to have created
 * {@code CreateLegalTagProperties} first as a single object schema (not {@code allOf}).
 */
@UtilityClass
final class PropertiesOpenApiCustomizer {

    static final String PROPERTIES_SCHEMA_NAME = Properties.class.getSimpleName();
    static final String CREATE_PROPERTIES_SCHEMA_NAME =
            LegalTagRequestOpenApiCustomizer.CREATE_PROPERTIES_SCHEMA_NAME;
    static final String COUNTRY_OF_ORIGIN_PROPERTY = "countryOfOrigin";
    static final String CONTRACT_ID_PROPERTY = "contractId";
    static final String ORIGINATOR_PROPERTY = "originator";
    static final String DATA_TYPE_PROPERTY = "dataType";
    static final String SECURITY_CLASSIFICATION_PROPERTY = "securityClassification";
    static final String PERSONAL_DATA_PROPERTY = "personalData";
    static final String EXPORT_CLASSIFICATION_PROPERTY = "exportClassification";

    /** Shared with {@link UpdateLegalTag#CONTRACT_ID_PATTERN} so create/update stay aligned. */
    static final String CONTRACT_ID_PATTERN = UpdateLegalTag.CONTRACT_ID_PATTERN;

    /**
     * No leading/trailing whitespace: {@code Properties#setOriginator} trims before validation,
     * so {@code "HD "} would otherwise be schema-valid then fail as {@code "HD"}.
     */
    static final String ORIGINATOR_PATTERN =
            "^[A-Za-z0-9.-]([-. A-Za-z0-9]*[A-Za-z0-9.-])?$";

    private static final Set<String> ALLOWED_RESIDENCY_RISKS = Set.of(
            Country.RESIDENCY_RISK.NO_RESTRICTION,
            Country.RESIDENCY_RISK.NOT_ASSIGNED,
            Country.RESIDENCY_RISK.CLIENT_CONSENT_REQUIRED);

    private static final List<String> EXPORT_CLASSIFICATIONS = List.of(
            AllowedLegaltagPropertyValues.ECCN_EAR99,
            AllowedLegaltagPropertyValues.ECCN_0A998,
            AllowedLegaltagPropertyValues.NOT_TECHNICAL_DATA,
            AllowedLegaltagPropertyValues.NO_LICENSE_REQUIRED);

    private static final List<String> PERSONAL_DATA_TYPES = List.of(
            AllowedLegaltagPropertyValues.PERSONALLY_IDENTIFIABLE,
            AllowedLegaltagPropertyValues.NO_PERSONAL_DATA);

    private static final List<String> SECURITY_CLASSIFICATIONS = List.of(
            AllowedLegaltagPropertyValues.PUBLIC,
            AllowedLegaltagPropertyValues.PRIVATE,
            AllowedLegaltagPropertyValues.CONFIDENTIAL);

    private static final List<String> DATA_TYPES = List.of(
            DataTypeValues.FIRST_PARTY_DATA,
            DataTypeValues.PUBLIC_DOMAIN_DATA,
            DataTypeValues.TRANSFERRED_DATA,
            DataTypeValues.SECOND_PARTY_DATA,
            DataTypeValues.THIRD_PARTY_DATA);

    static void apply(OpenAPI openApi) {
        Schema<?> createProperties = openApi.getComponents().getSchemas()
                .get(CREATE_PROPERTIES_SCHEMA_NAME);
        if (createProperties == null) {
            return;
        }

        createProperties.setAdditionalProperties(Boolean.FALSE);
        if (createProperties.getProperties() == null) {
            createProperties.setProperties(new LinkedHashMap<>());
        }

        Schema<?> sharedProperties = openApi.getComponents().getSchemas().get(PROPERTIES_SCHEMA_NAME);
        applyConstraints(createProperties, sharedProperties);
    }

    private static void applyConstraints(Schema<?> target, Schema<?> descriptionSource) {
        Schema<?> countryOfOrigin = descriptionSource != null
                && descriptionSource.getProperties() != null
                ? descriptionSource.getProperties().get(COUNTRY_OF_ORIGIN_PROPERTY)
                : null;
        Schema<?> countryItems = new StringSchema()
                ._enum(allowedCountryCodes(new DefaultCountriesRepository().read()))
                .example("US");
        // Use ArraySchema so OAS 3.1 emits type: array (Schema.type("array") can omit it).
        ArraySchema countryOverride = new ArraySchema().items(countryItems);
        countryOverride.setExample(List.of("US"));
        countryOverride.setMinItems(countryOfOrigin != null && countryOfOrigin.getMinItems() != null
                ? countryOfOrigin.getMinItems()
                : 1);
        if (countryOfOrigin != null && countryOfOrigin.getDescription() != null) {
            countryOverride.setDescription(countryOfOrigin.getDescription());
        }
        target.getProperties().put(COUNTRY_OF_ORIGIN_PROPERTY, countryOverride);

        replaceWithEnum(target, descriptionSource, EXPORT_CLASSIFICATION_PROPERTY, EXPORT_CLASSIFICATIONS,
                AllowedLegaltagPropertyValues.ECCN_EAR99);
        replaceWithEnum(target, descriptionSource, PERSONAL_DATA_PROPERTY, PERSONAL_DATA_TYPES,
                AllowedLegaltagPropertyValues.NO_PERSONAL_DATA);
        replaceWithEnum(target, descriptionSource, SECURITY_CLASSIFICATION_PROPERTY, SECURITY_CLASSIFICATIONS,
                AllowedLegaltagPropertyValues.PRIVATE);
        replaceWithEnum(target, descriptionSource, DATA_TYPE_PROPERTY, DATA_TYPES,
                DataTypeValues.THIRD_PARTY_DATA);

        replaceWithPattern(target, descriptionSource, CONTRACT_ID_PROPERTY, CONTRACT_ID_PATTERN, 3, 40, "A1234");
        replaceWithPattern(target, descriptionSource, ORIGINATOR_PROPERTY, ORIGINATOR_PATTERN, 3, 60, "Equinor");
    }

    private static void replaceWithEnum(Schema<?> target, Schema<?> descriptionSource,
                                        String propertyName, List<String> values, String example) {
        Schema<?> constrained = new StringSchema()._enum(values).example(example);
        copyDescription(descriptionSource, propertyName, constrained);
        target.getProperties().put(propertyName, constrained);
    }

    private static void replaceWithPattern(Schema<?> target, Schema<?> descriptionSource,
                                           String propertyName, String pattern, int minLength,
                                           int maxLength, String example) {
        Schema<?> constrained = new StringSchema()
                .pattern(pattern)
                .minLength(minLength)
                .maxLength(maxLength)
                .example(example);
        copyDescription(descriptionSource, propertyName, constrained);
        target.getProperties().put(propertyName, constrained);
    }

    private static void copyDescription(Schema<?> descriptionSource, String propertyName,
                                        Schema<?> to) {
        if (descriptionSource == null || descriptionSource.getProperties() == null) {
            return;
        }
        Schema<?> from = descriptionSource.getProperties().get(propertyName);
        if (from != null && from.getDescription() != null) {
            to.setDescription(from.getDescription());
        }
    }

    static List<String> allowedCountryCodes(List<Country> countries) {
        return countries.stream()
                .filter(country -> ALLOWED_RESIDENCY_RISKS.contains(country.getResidencyRisk()))
                .map(Country::getAlpha2)
                .distinct()
                .sorted()
                .toList();
    }
}
