package org.opengroup.osdu.legal.countries;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.opengroup.osdu.legal.provider.interfaces.IStorageReader;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LegalTagCountriesRepositoryImplTests {
    @Mock
    private IStorageReader storageReader;

    @InjectMocks
    private LegalTagCountriesRepositoryImpl sut;

    @BeforeEach
    public void setup() {
    }

    @Test
    public void should_returnEmptyCountries_given_empty_blob() {
        byte[] bytes = "".getBytes();
        when(this.storageReader.readAllBytes()).thenReturn(bytes);

        List<Country> countries = sut.read();
        assertTrue(countries.isEmpty());
    }

    @Test
    public void should_returnExpectingCountries_given_countriesJsonString() {

        String jsonString = generateJsonString();
        byte[] bytes = jsonString.getBytes();
        when(this.storageReader.readAllBytes()).thenReturn(bytes);
        List<Country> countries = sut.read();
        assertEquals(2, countries.size());
        assertEquals("AD", countries.get(0).getAlpha2());
        assertEquals("AE", countries.get(1).getAlpha2());

    }

    private String generateJsonString() {
        return "[{\n" +
                "    \"name\":\"Andorra\",\n" +
                "    \"alpha2\":\"AD\",\n" +
                "    \"numeric\":\"16\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\":\"United Arab Emirates\",\n" +
                "    \"alpha2\":\"AE\",\n" +
                "    \"numeric\":\"784\"\n" +
                "  }]";
    }

}
