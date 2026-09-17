package org.opengroup.osdu.legal.countries;

import com.google.gson.stream.JsonReader;

import java.io.StringReader;

public class CountriesReaderImpl extends CountriesReader {
    public JsonReader generateJsonReader(String inputString) {
        return new JsonReader(new StringReader(inputString));
    }
}
