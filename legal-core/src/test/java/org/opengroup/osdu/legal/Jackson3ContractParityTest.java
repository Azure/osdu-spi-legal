/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.opengroup.osdu.legal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.sql.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.opengroup.osdu.core.common.http.HttpConfiguration;
import org.opengroup.osdu.core.common.model.legal.Properties;
import org.opengroup.osdu.core.common.model.legal.StatusChangedTag;
import org.opengroup.osdu.core.common.model.legal.StatusChangedTags;
import org.opengroup.osdu.legal.jobs.models.AboutToExpireLegalTag;
import org.opengroup.osdu.legal.jobs.models.AboutToExpireLegalTags;
import org.opengroup.osdu.legal.jobs.models.LegalTagJobResult;
import org.opengroup.osdu.legal.tags.dto.InvalidTagWithReason;
import org.opengroup.osdu.legal.tags.dto.InvalidTagsWithReason;
import org.opengroup.osdu.legal.tags.dto.LegalTagDto;
import org.opengroup.osdu.legal.tags.dto.LegalTagDtos;
import org.opengroup.osdu.legal.tags.dto.QueryLegalTag;
import org.opengroup.osdu.legal.tags.dto.ReadablePropertyValues;
import org.opengroup.osdu.legal.tags.dto.RequestLegalTags;
import org.opengroup.osdu.legal.tags.dto.UpdateLegalTag;
import tools.jackson.databind.json.JsonMapper;

/**
 * Writes and reads the legal API models with the Jackson 2 mapper the service used before (os-core-common 9.x
 * HttpConfiguration) and the Jackson 3 mapper it uses now, and requires the same JSON. Property order is not compared.
 */
public class Jackson3ContractParityTest {

    private static final Date EXPIRATION = Date.valueOf("2031-12-25");

    private final com.fasterxml.jackson.databind.ObjectMapper jackson2 = new com.fasterxml.jackson.databind.ObjectMapper()
            .registerModule(new JavaTimeModule())
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .setTimeZone(TimeZone.getDefault());

    private final JsonMapper jackson3 = new HttpConfiguration().jsonObjectMapper();

    static Stream<Arguments> responses() {
        Map<String, Object> extension = new LinkedHashMap<>();
        extension.put("key", "value");
        extension.put("nullValue", null);
        extension.put("nested", Map.of("a", 1));

        Properties properties = new Properties();
        properties.setCountryOfOrigin(List.of("US"));
        properties.setContractId("A1234");
        properties.setExpirationDate(EXPIRATION);
        properties.setOriginator("MyCompany");
        properties.setDataType("Public Domain Data");
        properties.setSecurityClassification("Public");
        properties.setPersonalData("No Personal Data");
        properties.setExportClassification("EAR99");
        properties.setExtensionProperties(extension);
        LegalTagDto dto = new LegalTagDto("tag", "<b>description</b>", properties);
        LegalTagDto bare = new LegalTagDto();
        bare.setDescription(null);

        ReadablePropertyValues values = new ReadablePropertyValues();
        values.setCountriesOfOrigin(Map.of("US", "United States"));
        values.setSecurityClassifications(Set.of("Public"));

        Map<String, Object> updateExtension = new HashMap<>(extension);
        UpdateLegalTag update = new UpdateLegalTag("tag", "A1234", "desc", EXPIRATION, updateExtension);

        AboutToExpireLegalTags aboutToExpire = new AboutToExpireLegalTags();
        aboutToExpire.getLegalTags().add(new AboutToExpireLegalTag("tag", EXPIRATION));
        StatusChangedTags changed = new StatusChangedTags();
        changed.getStatusChangedTags().add(new StatusChangedTag("tag", null));

        return Stream.of(
                Arguments.of(dto),
                Arguments.of(bare),
                Arguments.of(new LegalTagDtos(List.of(dto, bare))),
                Arguments.of(new InvalidTagsWithReason(List.of(new InvalidTagWithReason("tag", "expired")))),
                Arguments.of(new InvalidTagWithReason("tag", null)),
                Arguments.of(values),
                Arguments.of(new RequestLegalTags(List.of("tag"))),
                Arguments.of(new QueryLegalTag(List.of("a=b"), null, null, null, 0)),
                Arguments.of(update),
                Arguments.of(new UpdateLegalTag()),
                Arguments.of(new LegalTagJobResult(changed, aboutToExpire)));
    }

    @ParameterizedTest
    @MethodSource("responses")
    public void should_writeSameJson(Object model) throws Exception {
        String expected = jackson2.writeValueAsString(model);
        String actual = jackson3.writeValueAsString(model);

        assertEquals(jackson2.readTree(expected), jackson2.readTree(actual), "jackson2: " + expected + "\njackson3: " + actual);
    }

    static Stream<Arguments> requests() {
        String properties = "{\"countryOfOrigin\":[\"US\"],\"contractId\":\"A1234\",\"expirationDate\":\"2031-12-25\","
                + "\"originator\":\"MyCompany\",\"dataType\":\"Public Domain Data\",\"securityClassification\":\"Public\","
                + "\"personalData\":\"No Personal Data\",\"exportClassification\":\"EAR99\","
                + "\"extensionProperties\":{\"k\":\"v\",\"n\":null},\"unknown\":1}";
        return Stream.of(
                Arguments.of(LegalTagDto.class, "{\"name\":\"tag\",\"description\":\"d\",\"properties\":" + properties + ",\"unknown\":true}"),
                Arguments.of(LegalTagDto.class, "{\"name\":\"tag\",\"description\":null,\"properties\":null}"),
                Arguments.of(UpdateLegalTag.class, "{\"name\":\"tag\",\"contractId\":\"c\",\"description\":\"d\",\"expirationDate\":\"2031-12-25\",\"extensionProperties\":{\"k\":\"v\",\"n\":null}}"),
                Arguments.of(UpdateLegalTag.class, "{\"name\":\"tag\"}"),
                Arguments.of(RequestLegalTags.class, "{\"names\":[\"a\",\"b\"]}"),
                Arguments.of(QueryLegalTag.class, "{\"queryList\":[\"a=b\"],\"operatorList\":[\"AND\"],\"sortBy\":\"name\",\"sortOrder\":\"ASC\",\"limit\":5}"),
                Arguments.of(QueryLegalTag.class, "{\"queryList\":[\"a=b\"]}"),
                Arguments.of(AboutToExpireLegalTag.class, "{\"tagName\":\"tag\",\"expirationDate\":\"2031-12-25\"}"),
                // Partial bodies: fields that are left out must keep their field initializers.
                Arguments.of(LegalTagDto.class, "{\"name\":\"tag\"}"),
                Arguments.of(LegalTagDto.class, "{}"),
                Arguments.of(UpdateLegalTag.class, "{}"),
                Arguments.of(RequestLegalTags.class, "{}"),
                Arguments.of(QueryLegalTag.class, "{}"),
                Arguments.of(LegalTagDtos.class, "{}"),
                Arguments.of(InvalidTagsWithReason.class, "{}"),
                Arguments.of(InvalidTagWithReason.class, "{\"name\":\"tag\"}"));
    }

    @ParameterizedTest
    @MethodSource("requests")
    public void should_readSameModel(Class<?> type, String json) throws Exception {
        String expected = jackson2.writeValueAsString(jackson2.readValue(json, type));
        String actual = jackson2.writeValueAsString(jackson3.readValue(json, type));

        assertEquals(jackson2.readTree(expected), jackson2.readTree(actual), "jackson2: " + expected + "\njackson3: " + actual);
    }

    // The shared deserializer maps a blank date to the default expiration date. Jackson 3 ignores the Jackson 2
    // @JsonDeserialize annotation, so this proves the models carry the Jackson 3 one.
    @Test
    public void should_useSharedDeserializerForBlankExpirationDate() {
        assertEquals(Properties.DEFAULT_EXPIRATIONDATE,
                jackson3.readValue("{\"expirationDate\":\"\"}", UpdateLegalTag.class).getExpirationDate());
        assertEquals(Properties.DEFAULT_EXPIRATIONDATE,
                jackson3.readValue("{\"expirationDate\":\"\"}", AboutToExpireLegalTag.class).getExpirationDate());
    }
}
