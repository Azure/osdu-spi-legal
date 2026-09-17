/*
 *  Copyright 2025 © Microsoft Corporation
 *
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

package org.opengroup.osdu.legal.tags.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opengroup.osdu.legal.tags.dto.UpdateLegalTag;
import static org.opengroup.osdu.core.common.util.SerializationUtils.EXPIRATION_DATE_FORMAT;

import org.junit.jupiter.api.Test;
import  java.util.Date;
import java.text.SimpleDateFormat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UpdateLegalTagTests {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String EXPIRATION_DATE_STRING = "2025-12-25";

    /**
     * Tests that the ExpirationDateDeserializer is correctly deserializing the expiration date
     * and @JsonFormat annotation correctly formats the expiration date during deserialization.
     */
    @Test
    public void testExpirationDate_Deserialization() throws Exception {
        String json = "{\"expirationDate\":\"2025-12-25\"}";
        UpdateLegalTag tag = objectMapper.readValue(json, UpdateLegalTag.class);
        SimpleDateFormat sdf = new SimpleDateFormat(EXPIRATION_DATE_FORMAT);
        Date expectedDate = sdf.parse(EXPIRATION_DATE_STRING);

        assertNotNull(tag.getExpirationDate());
        assertEquals(expectedDate, tag.getExpirationDate());
    }
}
