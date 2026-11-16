/*
 *  Copyright 2025 Ãƒâ€šÃ‚Â© Microsoft Corporation
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

import tools.jackson.databind.DatabindException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import tools.jackson.databind.ObjectMapper;
import org.opengroup.osdu.legal.tags.dto.UpdateLegalTag;
import static org.opengroup.osdu.legal.util.JsonTestUtils.json;
import static org.opengroup.osdu.legal.util.JsonTestUtils.withField;
import static org.opengroup.osdu.legal.util.LegalTagRequestTestData.updateBody;
import static org.opengroup.osdu.core.common.util.SerializationUtils.EXPIRATION_DATE_FORMAT;

import org.junit.jupiter.api.Test;
import java.text.SimpleDateFormat;
import static org.junit.jupiter.api.Assertions.*;

public class UpdateLegalTagTests {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String EXPIRATION_DATE_STRING = "2025-12-25";

    /**
     * Tests that the ExpirationDateDeserializer is correctly deserializing the expiration date
     * and @JsonFormat annotation correctly formats the expiration date during deserialization.
     */
    @Test
    public void testExpirationDate_Deserialization() throws Exception {
        UpdateLegalTag request = new UpdateLegalTag();
        request.setExpirationDate(java.sql.Date.valueOf(EXPIRATION_DATE_STRING));
        UpdateLegalTag tag = objectMapper.readValue(json(request), UpdateLegalTag.class);
        SimpleDateFormat sdf = new SimpleDateFormat(EXPIRATION_DATE_FORMAT);
        Date expectedDate = sdf.parse(EXPIRATION_DATE_STRING);

        assertNotNull(tag.getExpirationDate());
        assertEquals(expectedDate, tag.getExpirationDate());
    }

    @Test
    public void should_failValidation_when_nameIsMissingFromBody() throws Exception {
        UpdateLegalTag tag = objectMapper.readValue(json(new UpdateLegalTag()), UpdateLegalTag.class);

        assertNull(tag.getName());

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<UpdateLegalTag>> violations = validator.validate(tag);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> "name".equals(v.getPropertyPath().toString())));
    }

    @Test
    public void should_allowOmittedContractIdAndExpirationDate_when_validatingUpdateBody() throws Exception {
        UpdateLegalTag request = new UpdateLegalTag();
        request.setName("OSDU-Private-EHCData");
        UpdateLegalTag tag = objectMapper.readValue(json(request), UpdateLegalTag.class);

        assertNull(tag.getContractId());
        assertNull(tag.getExpirationDate());

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<UpdateLegalTag>> violations = validator.validate(tag);

        assertTrue(violations.stream().noneMatch(v -> "contractId".equals(v.getPropertyPath().toString())));
        assertTrue(violations.stream().noneMatch(v -> "expirationDate".equals(v.getPropertyPath().toString())));
    }

    @Test
    public void should_rejectNullExtensionProperties_when_deserializingUpdateBody() {
        assertThrows(DatabindException.class, () -> objectMapper.readValue(
                json(withField(updateBody(), "extensionProperties", null)),
                UpdateLegalTag.class));
    }

    @Test
    public void should_acceptNonObjectExtensionPropertyValues_when_deserializingUpdateBody() throws Exception {
        UpdateLegalTag request = updateBody();
        request.setExtensionProperties(Map.of(
                "x-schemathesis-additional", Arrays.asList(null, null), "flag", true));
        UpdateLegalTag tag = objectMapper.readValue(json(request), UpdateLegalTag.class);

        assertNotNull(tag.getExtensionProperties());
        assertTrue(tag.getExtensionProperties().containsKey("x-schemathesis-additional"));
        assertEquals(true, tag.getExtensionProperties().get("flag"));
    }

    @Test
    public void should_acceptOmittedExtensionProperties_when_deserializingUpdateBody() throws Exception {
        UpdateLegalTag tag = objectMapper.readValue(
                json(updateBody()),
                UpdateLegalTag.class);

        assertNull(tag.getExtensionProperties());
    }

    @Test
    public void should_rejectNullDescription_when_deserializingUpdateBody() {
        assertThrows(DatabindException.class, () -> objectMapper.readValue(
                json(withField(withField(updateBody(), "description", null), "extensionProperties", Map.of())),
                UpdateLegalTag.class));
    }

    @Test
    public void should_acceptOmittedDescription_when_deserializingUpdateBody() throws Exception {
        UpdateLegalTag tag = objectMapper.readValue(
                json(updateBody()),
                UpdateLegalTag.class);

        assertNull(tag.getDescription());
    }

    @Test
    public void should_rejectUnknownProperty_when_deserializingUpdateBody() {
        assertThrows(DatabindException.class, () -> objectMapper.readValue(
                json(withField(updateBody(), "x-schemathesis-unknown-property", 42)),
                UpdateLegalTag.class));
    }
}
