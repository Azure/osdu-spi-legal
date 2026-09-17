package org.opengroup.osdu.legal.tags.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.opengroup.osdu.legal.countries.LegalTagCountriesService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class OtherRelevantDataCountriesValidatorTests {

    @Mock
    private LegalTagCountriesService legalTagCountriesService;

    @InjectMocks
    private OtherRelevantDataCountriesValidator sut;

    @BeforeEach
    public void setupClass(){
        Map<String, String> ordc = new HashMap<String, String>() {{
            put("US", "usa");
            put("GB", "United Kingdom");
            put("BV", "Bouvet Island");
        }};
        when(legalTagCountriesService.getValidORDCs()).thenReturn(ordc);
    }

    @Test
    public void should_ReturnTrue_When_GivenValidIsoAlpha2Code(){
        assertTrue(sut.isValid(Arrays.asList(new String[]{"US"}), null));
    }

    @Test
    public void should_ReturnTrue_When_GivenEmptyIsoAlpha2Code(){
        assertTrue(sut.isValid(Arrays.asList(new String[]{}), null));
    }

    @Test
    public void should_ReturnTrue_When_GivenNullIsoAlpha2Code(){
        assertTrue(sut.isValid(null, null));
    }

    @Test
    public void should_ReturnTrue_When_GivenMultipleValidIsoAlpha2Code(){
        assertTrue(sut.isValid(Arrays.asList(new String[]{"US", "GB"}), null));
    }

    @Test
    public void should_ReturnTrue_When_AnyCountryIsRestricted(){
        assertTrue(sut.isValid(Arrays.asList(new String[]{"GB", "BV"}), null));
    }

    @Test
    public void should_ReturnFalse_When_GivenInvalidIsoAlpha2Code(){
        assertFalse(sut.isValid(Arrays.asList(new String[]{"g2"}), null));
    }

    @Test
    public void should_ReturnFalse_When_GivenMultipleValidButOneInvalidIsoAlpha2Code(){
        assertFalse(sut.isValid(Arrays.asList(new String[]{"US", "GB", "g2"}), null));
    }

    @Test
    public void should_ReturnFalse_When_AnyCountryIsEmbargoed(){
        assertFalse(sut.isValid(Arrays.asList(new String[]{"GB", "SY"}), null));
    }
    @Test
    public void should_ReturnFalse_When_AnyCountryIsEmpty(){
        assertFalse(sut.isValid(Arrays.asList(new String[]{"GB", ""}), null));
    }
    @Test
    public void should_ReturnFalse_When_AnyCountryIsNull(){
        assertFalse(sut.isValid(Arrays.asList(new String[]{"GB", null}), null));
    }
}
