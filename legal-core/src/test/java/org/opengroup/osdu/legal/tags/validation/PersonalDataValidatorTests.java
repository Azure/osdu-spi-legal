package org.opengroup.osdu.legal.tags.validation;

import org.opengroup.osdu.core.common.model.legal.AllowedLegaltagPropertyValues;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.core.common.model.legal.validation.PersonalDataValidator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PersonalDataValidatorTests {

    static PersonalDataValidator sut;

    @BeforeAll
    public static void setupClass(){
        sut = new PersonalDataValidator();
        sut.initialize(null);
    }

    @Test
    public void should_ReturnTrue_When_GivenValidPersonalData(){
        assertTrue(sut.isValid(AllowedLegaltagPropertyValues.PERSONALLY_IDENTIFIABLE, null));
    }
    @Test
    public void should_ReturnFalse_When_GivenEmptyPersonalData(){
        assertFalse(sut.isValid("", null));
    }
    @Test
    public void should_ReturnFalse_When_GivenNullPersonalData(){
        assertFalse(sut.isValid(null, null));
    }
    @Test
    public void should_ReturnFalse_When_GivenInValidPersonalData(){
        assertFalse(sut.isValid("bad", null));
    }
    @Test
    public void should_ReturnTrue_When_GivenValidPersonalDataInIrregularCase(){
        assertTrue(sut.isValid("nO peRsonAL dATA", null));
    }

}
