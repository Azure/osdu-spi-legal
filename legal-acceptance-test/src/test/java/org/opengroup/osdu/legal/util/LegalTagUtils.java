package org.opengroup.osdu.legal.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class LegalTagUtils {

    private static final Gson GSON = new Gson();

    public static String createRandomNameTenant(String tenantId) {
        return tenantId + "-gae-integration-test-" + System.currentTimeMillis();
    }

    public static List<String> readAlpha2CodesFromResource(String resourceName) throws IOException {
        InputStream inputStream = LegalTagUtils.class.getResourceAsStream("/" + resourceName);
        if (inputStream == null) {
            throw new IOException("Resource not found: " + resourceName);
        }
        JsonArray countries = GSON.fromJson(
            new InputStreamReader(inputStream, StandardCharsets.UTF_8), JsonArray.class);
        List<String> alpha2Codes = new ArrayList<>();
        for (JsonElement element : countries) {
            JsonObject country = element.getAsJsonObject();
            if (country.has("alpha2")) {
                alpha2Codes.add(country.get("alpha2").getAsString());
            }
        }
        return alpha2Codes;
    }


}
