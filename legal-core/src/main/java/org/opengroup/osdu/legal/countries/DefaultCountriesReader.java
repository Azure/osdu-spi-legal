package org.opengroup.osdu.legal.countries;

import com.google.gson.stream.JsonReader;

import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class DefaultCountriesReader extends CountriesReader {
    public JsonReader generateJsonReader(String inputString) throws IOException {
        ClassPathResource cpr = new ClassPathResource(inputString);
        InputStream inputStream = cpr.getInputStream();
        Reader reader = new InputStreamReader(inputStream);
        return new JsonReader(reader);
    }
}
