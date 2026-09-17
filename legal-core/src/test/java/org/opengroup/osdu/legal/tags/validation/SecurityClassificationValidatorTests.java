package org.opengroup.osdu.legal.tags.validation;

import org.opengroup.osdu.core.common.model.legal.validation.SecurityClassificationValidator;
import org.opengroup.osdu.core.common.model.legal.AllowedLegaltagPropertyValues;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintValidatorContext;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SecurityClassificationValidatorTests {

    static SecurityClassificationValidator sut;

    @BeforeAll
    public static void setupClass(){
        sut = new SecurityClassificationValidator();
        sut.initialize(null);
    }

    @Test
    public void should_ReturnTrue_When_GivenValidSecurityClassification(){
        assertTrue(sut.isValid(AllowedLegaltagPropertyValues.PUBLIC, null));
    }
    @Test
    public void should_ReturnFalse_When_GivenValidSecretClassification(){
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
        when(context.buildConstraintViolationWithTemplate(any())).thenReturn(mock(ConstraintValidatorContext.ConstraintViolationBuilder.class));
        assertFalse(sut.isValid(AllowedLegaltagPropertyValues.SECRET, context));
    }
    @Test
    public void should_ReturnFalse_When_GivenEmptySecurityClassification(){
        assertFalse(sut.isValid("", null));
    }
    @Test
    public void should_ReturnFalse_When_GivenNullSecurityClassification(){
        assertFalse(sut.isValid(null, null));
    }
    @Test
    public void should_ReturnFalse_When_GivenInValidSecurityClassification(){
        assertFalse(sut.isValid("wrong", null));
    }
    @Test
    public void should_ReturnTrue_When_GivenValidSecurityClassificationInIrregularCase(){
        assertTrue(sut.isValid("puBLIC", null));
    }

}
