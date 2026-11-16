package org.opengroup.osdu.legal.tags.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.util.Json31;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class QueryLegalTagOpenApiOas31Test {

    @Test
    public void oas31_json_type_is_integer_for_queryLegalTagLimit() throws Exception {
        JsonNode limit = Json31.mapper().readTree(Json31.pretty(
                ModelConverters.getInstance(true).readAll(QueryLegalTag.class)
                        .get("QueryLegalTag").getProperties().get("limit")));

        assertEquals("integer", limit.get("type").asText());
    }
}
