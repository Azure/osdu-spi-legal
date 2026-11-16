package org.opengroup.osdu.legal.util;

import java.util.Arrays;
import java.util.Objects;
import java.io.IOException;
import java.util.List;

import static org.opengroup.osdu.core.test.util.TestFileUtil.readTestDataFile;

public class LegalTagUtils {

    private record Country(String alpha2) {}

    public static String createRandomNameTenant(String tenantId) {
        return tenantId + "-gae-integration-test-" + System.currentTimeMillis();
    }

    public static List<String> readAlpha2CodesFromResource(String resourceName) throws IOException {
        Country[] countries = readTestDataFile(resourceName, Country[].class);
        return Arrays.stream(countries).map(Country::alpha2).filter(Objects::nonNull).toList();
    }


}
