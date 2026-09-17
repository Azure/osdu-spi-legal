package org.opengroup.osdu.legal.acceptanceTests;

import org.opengroup.osdu.legal.util.AcceptanceBaseTest;
import org.opengroup.osdu.legal.util.LegalTagUtils;
import org.junit.Test;

public abstract class DeleteLegalTagApiAcceptanceTests extends AcceptanceBaseTest {

    protected String name;

    @Test
    public void should_return204_when_deletingAContractThatDoesNotExist() throws Exception{
        name = LegalTagUtils.createRandomNameTenant();
        validateAccess(204);
    }

    @Test
    public void should_return204_when_deletingAContractThatDoesExist() throws Exception{
        name = LegalTagUtils.createRandomNameTenant();
        legalTagUtils.getResult(legalTagUtils.create(name), 201, String.class );
        validateAccess(204);
    }

    @Test
    public void should_return400_when_deletingAContractWithAnInvalidName() throws Exception{
        name = "invalid*name";
        send("", 400);
    }

    @Override
    protected String getApi() {
        return "legaltags/" + name;
    }

    @Override
    protected String getHttpMethod() {
        return "DELETE";
    }
}
