package org.opengroup.osdu.legal.tags.validation;

import org.opengroup.osdu.core.common.model.legal.AllowedLegaltagPropertyValues;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.core.common.model.legal.validation.ExportClassificationValidator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExportClassificationValidatorTests {

    static ExportClassificationValidator sut;

    @BeforeAll
    public static void setupClass(){
        sut = new ExportClassificationValidator();
        sut.initialize(null);
    }

    @Test
    public void should_ReturnTrue_When_GivenValidEccnPredefinedValue(){
        assertTrue(sut.isValid(AllowedLegaltagPropertyValues.ECCN_EAR99, null));
    }
    @Test
    public void should_ReturnTrue_When_GivenValidEccn(){
        assertTrue(sut.isValid("EAR99", null));
    }
    @Test
    public void should_ReturnFalse_When_GivenEmptyEccn(){
        assertFalse(sut.isValid("", null));
    }
    @Test
    public void should_ReturnFalse_When_GivenNulEccn(){
        assertFalse(sut.isValid(null, null));
    }
    @Test
    public void should_ReturnFalse_When_GivenInvalidEccnChars(){
        assertFalse(sut.isValid("ref-eor", null));
    }
    @Test
    public void should_ReturnFalse_When_GivenInvalidEccnLength(){
        assertFalse(sut.isValid("EAR999999", null));
    }
    @Test
    public void should_ReturnTrue_When_GivenValidEccnInIrregularCase(){
        assertTrue(sut.isValid("eaR99", null));
    }
    @Test
    public void should_ReturnTrue_When_GivenNotTechnicalDataValue(){
        assertTrue(sut.isValid("Not - Technical Data", null));
    }
    @Test
    public void should_ReturnTrue_When_GivenNoLicenseRequiredValue(){
        assertTrue(sut.isValid("No License Required", null));
    }
}
