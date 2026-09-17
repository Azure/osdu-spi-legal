package org.opengroup.osdu.legal.tags.dto;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RequestLegalTagsTests {

    private static Validator validator;

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
    public void should_haveValidationErrors_when_requestingLegalTagsUsingInvalidName(){

        RequestLegalTags requestLegalTags = new RequestLegalTags();
        List<String> names = new ArrayList<>();
        names.add("12345+");

        requestLegalTags.setNames(names);
        Set<ConstraintViolation<RequestLegalTags>> violations = validator.validate(requestLegalTags);

        assertEquals(1, violations.size());
    }
}
