package org.opengroup.osdu.legal.tags.dto;

import org.junit.jupiter.api.Test;
import org.opengroup.osdu.core.common.model.legal.LegalTag;
import org.opengroup.osdu.legal.tags.LegalTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LegalTagDtoTests {

    @Test
    public void should_appendWarningToDescription_when_clientConsentRequiredOnCoo() {
        LegalTag lt = LegalTestUtils.createValidLegalTag("abc");
        lt.getProperties().getCountryOfOrigin().add("MY");
        LegalTagDto dto = LegalTagDto.convertTo(lt);

        assertTrue(dto.getDescription().startsWith("One or more Country of Origin for these Legal tag require consent of client or a license."));
    }

    @Test
    public void should_notAppendWarningToDescription_when_clientConsentNotRequiredOnCoo() {
        LegalTag lt = LegalTestUtils.createValidLegalTag("abc");
        LegalTagDto dto = LegalTagDto.convertTo(lt);

        assertFalse(dto.getDescription().startsWith("One or more Country of Origin for these Legal tag require consent of client or a license."));
    }

    @Test
    public void should_initialize_successfully() {

        LegalTagDto legalTagDto = new LegalTagDto("Test LegalTag", "Test legal tag description", LegalTestUtils.createValidProperties());

        assertNotNull(legalTagDto);
        assertNotNull(legalTagDto.getName());
        assertNotNull(legalTagDto.getDescription());
        assertNotNull(legalTagDto.getProperties());
    }

    @Test
    public void should_compare_successfully() {

        LegalTagDto legalTagDto1 = new LegalTagDto("Test LegalTag", "Test legal tag description", LegalTestUtils.createValidProperties());
        LegalTagDto legalTagDto2 = new LegalTagDto("Test LegalTag", "Test legal tag description", LegalTestUtils.createValidProperties());

        assertTrue(legalTagDto1.equals(legalTagDto2));
        assertEquals(legalTagDto1.hashCode(),legalTagDto2.hashCode());
        assertEquals(legalTagDto1.toString(),legalTagDto2.toString());
    }

    @Test
    public void should_returnNullLegalTagDto_whenLegalTagIsNull() {
        assertNull(LegalTagDto.convertTo(null));
    }

    @Test
    public void should_returnNullLegalTag_whenLegalTagDtoIsNull() {
        assertNull(LegalTagDto.convertFrom(null));
    }
}
