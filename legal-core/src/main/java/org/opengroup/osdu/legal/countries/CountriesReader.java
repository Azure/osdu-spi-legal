package org.opengroup.osdu.legal.countries;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.opengroup.osdu.core.common.model.http.AppException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class CountriesReader {

    public List<Country> convertJsonIntoCountries(String jsonString) {
        List<Country> countries = new ArrayList<>();
        if (jsonString.isEmpty()) return countries;
        try {
            Gson gson = new Gson();
            JsonReader reader = generateJsonReader(jsonString);
            reader.beginArray();
            while (reader.hasNext()) {
                Country data = gson.fromJson(reader, Country.class);
                countries.add(data);
            }
            reader.endArray();
            reader.close();
        } catch (Exception e) {
            throw AppException.countryCodeLoadingError();
        }
        return countries;
    }

    abstract JsonReader generateJsonReader(String inputString) throws IOException;

}
