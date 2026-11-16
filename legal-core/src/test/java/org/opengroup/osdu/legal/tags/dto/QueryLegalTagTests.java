package org.opengroup.osdu.legal.tags.dto;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.opengroup.osdu.legal.util.JsonTestUtils.json;
import static org.opengroup.osdu.legal.util.JsonTestUtils.withField;
import static org.opengroup.osdu.legal.util.LegalTagRequestTestData.queryBody;

public class QueryLegalTagTests {

    private final ObjectMapper objectMapper = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    @ParameterizedTest
    @MethodSource("invalidQueryBodies")
    public void should_rejectSchemaInvalidTypes_when_deserializingQueryBody(Map<String, Object> body) {
        assertThrows(DatabindException.class, () -> objectMapper.readValue(json(body), QueryLegalTag.class));
    }

    @Test
    public void should_acceptNumericLimit_when_deserializingQueryBody() throws Exception {
        QueryLegalTag query = objectMapper.readValue(
                json(queryBody()),
                QueryLegalTag.class);

        assertEquals(0, query.getLimit());
    }

    @Test
    public void should_acceptOmittedOptionalFields_when_deserializingQueryBody() throws Exception {
        QueryLegalTag request = new QueryLegalTag();
        request.setQueryList(List.of());
        QueryLegalTag query = objectMapper.readValue(json(request), QueryLegalTag.class);

        assertNull(query.getOperatorList());
        assertNull(query.getSortBy());
        assertNull(query.getSortOrder());
        assertEquals(0, query.getLimit());
    }

    static Stream<Map<String, Object>> invalidQueryBodies() {
        return Stream.of(
                withField(queryBody(), "limit", null),
                Map.of("queryList", List.of(), "limit", "10"),
                Map.of("queryList", List.of(), "limit", "j\u000bk"),
                Map.of("queryList", List.of(), "limit", 2.5),
                Map.of("queryList", List.of(), "limit", 2.5890419884777833e-42),
                Map.of("queryList", List.of(), "limit", false),
                Map.of("queryList", List.of(), "limit", List.of()),
                Map.of("queryList", List.of(), "limit", -4294967296L),
                withField(queryBody(), "sortOrder", null),
                withField(queryBody(), "sortBy", null),
                withField(queryBody(), "sortBy", false),
                withField(queryBody(), "sortBy", 1),
                withField(queryBody(), "sortOrder", false),
                withField(queryBody(), "operatorList", null),
                withField(queryBody(), "operatorList", false),
                withField(queryBody(), "operatorList", Map.of()),
                withField(queryBody(), "operatorList", "union"),
                withField(queryBody(), "operatorList", Collections.singletonList(null)),
                withField(queryBody(), "operatorList", List.of(false)),
                withField(queryBody(), "queryList", null),
                withField(queryBody(), "queryList", false),
                withField(queryBody(), "queryList", Map.of()),
                withField(queryBody(), "queryList", Collections.singletonList(null)),
                withField(queryBody(), "queryList", List.of(false)),
                withField(queryBody(), "x-schemathesis-unknown-property", 42));
    }
}
