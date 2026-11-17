package org.opengroup.osdu.legal.config;

import tools.jackson.databind.DatabindException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.legal.tags.dto.CreateLegalTag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.opengroup.osdu.legal.util.JsonTestUtils.json;
import static org.opengroup.osdu.legal.util.JsonTestUtils.withField;
import static org.opengroup.osdu.legal.util.LegalTagRequestTestData.createBody;

public class JacksonConfigurationTests {

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        JsonMapper.Builder builder = JsonMapper.builder();
        JacksonConfiguration.applyStrictStringCoercion(builder);
        objectMapper = builder.build();
    }

    @Test
    public void should_rejectBooleanCoercionToString_onCreateLegalTagName() {
        assertThrows(DatabindException.class, () -> objectMapper.readValue(json(withField(createBody(), "name", false)), CreateLegalTag.class));
    }

    @Test
    public void should_rejectBooleanCoercion_whenAppliedViaBuilderCustomizer() {
        JsonMapper.Builder builder = JsonMapper.builder();
        new JacksonConfiguration().strictStringCoercionCustomizer().customize(builder);

        assertThrows(DatabindException.class, () -> builder.build().readValue(json(withField(createBody(), "name", false)), CreateLegalTag.class));
    }

    @Test
    public void should_acceptStringValues() throws Exception {
        CreateLegalTag request = createBody();
        request.setName("abc");
        CreateLegalTag created = objectMapper.readValue(json(request), CreateLegalTag.class);

        assertEquals("abc", created.getName());
    }
}
