package org.opengroup.osdu.legal.tags.dto;

import tools.jackson.databind.DatabindException;
import tools.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.opengroup.osdu.legal.util.JsonTestUtils.json;
import static org.opengroup.osdu.legal.util.JsonTestUtils.withField;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RequestLegalTagsTests {

    private static Validator validator;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    public static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void should_haveValidationError_when_requestingMoreThan25LegalTags(){

        RequestLegalTags requestLegalTags = new RequestLegalTags();
        List<String> names = new ArrayList<>();
        for(int i = 0; i < 26; i++){
            names.add("12345");
        }
        requestLegalTags.setNames(names);
        Set<ConstraintViolation<RequestLegalTags>> violations = validator.validate(requestLegalTags);

        assertEquals(1, violations.size());
    }

    @Test
    public void should_haveValidationError_when_nullASsignedToLegalTagNames(){

        RequestLegalTags requestLegalTags = new RequestLegalTags();
        requestLegalTags.setNames(null);
        Set<ConstraintViolation<RequestLegalTags>> violations = validator.validate(requestLegalTags);

        assertEquals(1, violations.size());
    }

    @Test
    public void should_haveNoValidationError_when_requesting20LegalTags(){

        RequestLegalTags requestLegalTags = new RequestLegalTags();
        List<String> names = new ArrayList<>();
        for(int i = 0; i < 20; i++){
            names.add("12345");
        }
        requestLegalTags.setNames(names);
        Set<ConstraintViolation<RequestLegalTags>> violations = validator.validate(requestLegalTags);

        assertEquals(0, violations.size());
    }

    @Test
    public void should_rejectNonStringItems_when_deserializingNamesArray() {
        assertThrows(DatabindException.class, () -> objectMapper.readValue(json(Map.of("names", List.of(false))), RequestLegalTags.class));
        assertThrows(DatabindException.class, () -> objectMapper.readValue(json(Map.of("names", List.of(123))), RequestLegalTags.class));
        assertThrows(DatabindException.class, () -> objectMapper.readValue(json(Map.of("names", List.of(Map.of()))), RequestLegalTags.class));
        assertThrows(DatabindException.class, () -> objectMapper.readValue(json(Map.of("names", Collections.singletonList(null))), RequestLegalTags.class));
        assertThrows(DatabindException.class, () -> objectMapper.readValue(json(withField(Map.of(), "names", null)), RequestLegalTags.class));
        assertThrows(DatabindException.class, () -> objectMapper.readValue(json(Map.of("names", false)), RequestLegalTags.class));
        assertThrows(DatabindException.class, () -> objectMapper.readValue(json(Map.of("names", Map.of())), RequestLegalTags.class));
        assertThrows(DatabindException.class, () -> objectMapper.readValue(json(Map.of("names", "abc")), RequestLegalTags.class));
    }

    @Test
    public void should_acceptStringItems_when_deserializingNamesArray() throws Exception {
        RequestLegalTags requestLegalTags = objectMapper.readValue(json(new RequestLegalTags(List.of("abc"))), RequestLegalTags.class);

        assertEquals(1, requestLegalTags.getNames().size());
        assertEquals("abc", requestLegalTags.getNames().get(0));
    }

    @Test
    public void should_haveValidationErrors_when_requestingLegalTagsUsingInvalidName(){

        RequestLegalTags requestLegalTags = new RequestLegalTags();
        List<String> names = new ArrayList<>();
        names.add("12345+");

        requestLegalTags.setNames(names);
        Set<ConstraintViolation<RequestLegalTags>> violations = validator.validate(requestLegalTags);

        assertEquals(1, violations.size());
    }

    @Test
    public void should_rejectUnknownProperty_when_deserializing() {
        assertThrows(DatabindException.class, () -> objectMapper.readValue(
                json(Map.of("names", List.of("OSDU-Private-USA-EHC"), "x-schemathesis-unknown-property", 42)),
                RequestLegalTags.class));
    }
}
