package org.opengroup.osdu.legal.countries;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class DefaultCountriesRepository implements LegalTagCountriesRepository {
    private static final String DEFAULT_COUNTRY_CODE_PATH = "DefaultCountryCode.json";

    @Override
    public List<Country> read() {
        CountriesReader countriesReader = new DefaultCountriesReader();
        return countriesReader.convertJsonIntoCountries(DEFAULT_COUNTRY_CODE_PATH);
    }

}
