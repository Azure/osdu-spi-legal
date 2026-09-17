package org.opengroup.osdu.legal.countries;

import org.opengroup.osdu.legal.provider.interfaces.IStorageReader;

import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class LegalTagCountriesRepositoryImpl implements LegalTagCountriesRepository {

    private IStorageReader storageReader;

    public LegalTagCountriesRepositoryImpl(IStorageReader storageReader) {
        this.storageReader = storageReader;
    }

    @Override
    public List<Country> read() {
        byte[] content = storageReader.readAllBytes();
        String jsonString = new String(content, UTF_8);
        CountriesReader countriesReader = new CountriesReaderImpl();
        return countriesReader.convertJsonIntoCountries(jsonString);
    }
}
