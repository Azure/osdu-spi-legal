package org.opengroup.osdu.legal.util;

import java.sql.Date;
import java.util.List;
import org.opengroup.osdu.core.common.model.legal.Properties;
import org.opengroup.osdu.legal.tags.dto.CreateLegalTag;
import org.opengroup.osdu.legal.tags.dto.QueryLegalTag;
import org.opengroup.osdu.legal.tags.dto.UpdateLegalTag;

/** Fresh request fixtures; use JsonTestUtils.withField for invalid types and explicit nulls. */
public final class LegalTagRequestTestData {
    private LegalTagRequestTestData() {}

    public static Properties createProperties() {
        Properties properties = new Properties();
        properties.setContractId("A1234");
        properties.setCountryOfOrigin(List.of("US"));
        properties.setDataType("Third Party Data");
        properties.setExpirationDate(Date.valueOf("2099-12-25"));
        properties.setExportClassification("EAR99");
        properties.setOriginator("Equinor");
        properties.setPersonalData("No Personal Data");
        properties.setSecurityClassification("Private");
        return properties;
    }

    public static CreateLegalTag createBody() {
        return new CreateLegalTag("OSDU-Private-EHCData", "", createProperties());
    }

    public static QueryLegalTag queryBody() {
        return new QueryLegalTag(List.of(), List.of(), "", "", 0);
    }

    public static UpdateLegalTag updateBody() {
        return new UpdateLegalTag("OSDU-Private-EHCData", "A1234", null,
                Date.valueOf("2099-12-25"), null);
    }
}
