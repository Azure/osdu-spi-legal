package org.opengroup.osdu.legal.tags.dto;

import tools.jackson.databind.DatabindException;
import tools.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.opengroup.osdu.legal.util.JsonTestUtils.json;
import static org.opengroup.osdu.legal.util.JsonTestUtils.withField;
import static org.opengroup.osdu.legal.util.LegalTagRequestTestData.createBody;
import static org.opengroup.osdu.legal.util.LegalTagRequestTestData.createProperties;

public class CreateLegalTagTests {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void should_rejectNullExtensionProperties_when_deserializingCreateBody() {
        assertThrows(DatabindException.class, () -> objectMapper.readValue(
                json(withField(createBody(), "properties", withField(createProperties(), "extensionProperties", null))),
                CreateLegalTag.class));
    }

    @Test
    public void should_acceptOmittedExtensionProperties_when_deserializingCreateBody() throws Exception {
        CreateLegalTag created = objectMapper.readValue(
                json(createBody()),
                CreateLegalTag.class);

        assertEquals("OSDU-Private-EHCData", created.getName());
        assertNotNull(created.getProperties());
        assertNull(created.getProperties().getExtensionProperties());
    }

    @Test
    public void should_rejectNullCountryOfOrigin_when_deserializingCreateBody() {
        assertThrows(DatabindException.class, () -> objectMapper.readValue(
                json(withField(createBody(), "properties", withField(createProperties(), "countryOfOrigin", null))),
                CreateLegalTag.class));
    }

    @Test
    public void should_rejectBooleanOriginator_when_deserializingCreateBody() {
        assertThrows(DatabindException.class, () -> objectMapper.readValue(
                json(withField(createBody(), "properties", withField(createProperties(), "originator", false))),
                CreateLegalTag.class));
    }

    @Test
    public void should_rejectNullDescription_when_deserializingCreateBody() {
        assertThrows(DatabindException.class, () -> objectMapper.readValue(
                json(withField(createBody(), "description", null)),
                CreateLegalTag.class));
    }

    @Test
    public void should_acceptExtensionPropertiesObject_when_deserializingCreateBody() throws Exception {
        CreateLegalTag request = createBody();
        request.getProperties().setExtensionProperties(
                Map.of("flag", true, "items", Arrays.asList(null, null)));
        CreateLegalTag created = objectMapper.readValue(json(request), CreateLegalTag.class);

        assertNotNull(created.getProperties().getExtensionProperties());
        assertEquals(true, created.getProperties().getExtensionProperties().get("flag"));
    }

    @Test
    public void should_rejectUnknownPropertyOnProperties_when_deserializingCreateBody() {
        assertThrows(DatabindException.class, () -> objectMapper.readValue(
                json(withField(createBody(), "properties", withField(createProperties(), "x-schemathesis-unknown-property", 42))),
                CreateLegalTag.class));
    }

    @Test
    public void should_rejectUnknownPropertyOnBody_when_deserializingCreateBody() {
        assertThrows(DatabindException.class, () -> objectMapper.readValue(
                json(withField(createBody(), "x-schemathesis-unknown-property", 42)),
                CreateLegalTag.class));
    }

    @Test
    public void should_ignoreReadOnlyLegalTagFields_when_deserializingCreateBody() throws Exception {
        CreateLegalTag created = objectMapper.readValue(
                json(withField(withField(createBody(), "isValid", true), "id", 11234567890L)),
                CreateLegalTag.class);

        assertEquals("OSDU-Private-EHCData", created.getName());
        assertNotNull(created.getProperties());
    }
}
