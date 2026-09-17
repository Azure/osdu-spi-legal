package org.opengroup.osdu.legal.countries;

import org.opengroup.osdu.core.common.model.http.AppException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CountriesReaderImplTests {

    @Test
    public void should_returnEmptyCountries_given_emptyJsonString() {
        CountriesReader sut = new CountriesReaderImpl();
        String jsonString = "";
        List<Country> countries = sut.convertJsonIntoCountries(jsonString);
        assertTrue(countries.isEmpty());
    }

    @Test
    public void should_returnExpectingCountries_given_countriesJsonString() {
        CountriesReader sut = new CountriesReaderImpl();
        String jsonString = generateJsonString();
        List<Country> countries = sut.convertJsonIntoCountries(jsonString);
        assertEquals(2, countries.size());
        assertEquals("AD", countries.get(0).getAlpha2());
        assertEquals("AE", countries.get(1).getAlpha2());

    }

    @Test
    public void should_throwAppException_when_catchIOException() {
        CountriesReader sut = new CountriesReaderImpl();
        String jsonString = generateInvalidJsonString();
        try {
            sut.convertJsonIntoCountries(jsonString);
        } catch (AppException e) {
            assertEquals(500, e.getError().getCode());
            assertEquals("Internal Server Error", e.getError().getReason());
        }

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

    private String generateInvalidJsonString() {
        return "[\n" +
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
