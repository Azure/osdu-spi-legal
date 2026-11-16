package org.opengroup.osdu.legal.controller;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.legal.AllowedLegaltagPropertyValues;
import org.opengroup.osdu.legal.FeatureFlagController;
import org.opengroup.osdu.legal.countries.LegalTagCountriesService;
import org.opengroup.osdu.legal.logging.AuditLogger;
import org.opengroup.osdu.legal.middleware.GlobalExceptionMapper;
import org.opengroup.osdu.legal.tags.LegalTagService;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.opengroup.osdu.legal.util.JsonTestUtils.json;
import static org.opengroup.osdu.legal.util.JsonTestUtils.withField;
import static org.opengroup.osdu.legal.util.LegalTagRequestTestData.createBody;
import static org.opengroup.osdu.legal.util.LegalTagRequestTestData.createProperties;
import static org.opengroup.osdu.legal.util.LegalTagRequestTestData.queryBody;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MVC conversion with a plain ObjectMapper so DTO {@code @JsonDeserialize} rules
 * reject schema-invalid types without relying on global coercion config.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LegalTagJacksonCoercionMvcTest {

    @Mock
    private LegalTagService legalTagService;
    @Mock
    private AllowedLegaltagPropertyValues allowedLegaltagPropertyValues;
    @Mock
    private LegalTagCountriesService legalTagCountriesService;
    @Mock
    private AuditLogger auditLogger;
    @Mock
    private FeatureFlagController featureFlagController;
    @Mock
    private JaxRsDpsLog jaxRsDpsLog;

    @InjectMocks
    private LegalTagController controller;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        GlobalExceptionMapper exceptionMapper = new GlobalExceptionMapper();
        ReflectionTestUtils.setField(exceptionMapper, "jaxRsDpsLogger", jaxRsDpsLog);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(exceptionMapper)
                .setMessageConverters(new JacksonJsonHttpMessageConverter(
                        JsonMapper.builder().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).build()))
                .build();
    }

    @ParameterizedTest
    @MethodSource("invalidNamesBodies")
    public void validate_should_return400_when_namesTypeIsInvalid(Map<String, Object> body) throws Exception {
        mockMvc.perform(post("/legaltags:validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @MethodSource("unknownPropertyBodies")
    public void create_should_return400_when_unknownPropertyPresent(Map<String, Object> body) throws Exception {
        mockMvc.perform(post("/legaltags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @MethodSource("invalidQueryBodies")
    public void query_should_return400_when_fieldTypeIsInvalid(Map<String, Object> body) throws Exception {
        mockMvc.perform(post("/legaltags:query")
                        .param("valid", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isBadRequest());
    }

    static Stream<Map<String, Object>> invalidNamesBodies() {
        return Stream.of(
                Map.of("names", List.of(false)),
                Map.of("names", Collections.singletonList(null)),
                withField(Map.of(), "names", null),
                Map.of("names", false),
                Map.of("names", Map.of()),
                Map.of("names", "abc"),
                Map.of("names", List.of("OSDU-Private-USA-EHC"), "x-schemathesis-unknown-property", 42));
    }

    static Stream<Map<String, Object>> unknownPropertyBodies() {
        return Stream.of(
                withField(createBody(), "properties", withField(createProperties(), "x-schemathesis-unknown-property", 42)),
                withField(createBody(), "x-schemathesis-unknown-property", 42));
    }

    static Stream<Map<String, Object>> invalidQueryBodies() {
        return Stream.of(
                withField(queryBody(), "limit", 2.5890419884777833e-42),
                withField(queryBody(), "limit", null),
                withField(queryBody(), "limit", false),
                withField(queryBody(), "operatorList", null),
                withField(queryBody(), "operatorList", false),
                withField(queryBody(), "operatorList", Map.of()),
                withField(queryBody(), "operatorList", Collections.singletonList(null)),
                withField(queryBody(), "queryList", null),
                withField(queryBody(), "queryList", false),
                withField(queryBody(), "queryList", Collections.singletonList(null)),
                withField(queryBody(), "sortBy", null),
                withField(queryBody(), "sortBy", false),
                withField(queryBody(), "sortOrder", null),
                withField(queryBody(), "sortOrder", false),
                withField(queryBody(), "x-schemathesis-unknown-property", 42));
    }
}
