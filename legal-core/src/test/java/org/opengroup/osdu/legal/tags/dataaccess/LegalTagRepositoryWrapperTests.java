package org.opengroup.osdu.legal.tags.dataaccess;

import org.opengroup.osdu.core.common.model.legal.ListLegalTagArgs;
import org.opengroup.osdu.core.common.model.legal.LegalTag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class LegalTagRepositoryWrapperTests {

    LegalTagRepositoryWrapper wrapped = mock(LegalTagRepositoryWrapper.class);
    MockRepoWrapper sut = new MockRepoWrapper(wrapped);

    @Test
    public void should_returnWrappedObjectResult_when_callingCreate(){
        LegalTag c = new LegalTag();
        when(wrapped.create(c)).thenReturn(5L);

        long result = sut.create(c);

        assertEquals(5L, result);
    }

    @Test
    public void should_returnWrappedObjectResult_when_callingUpdate(){
        LegalTag c = new LegalTag();
        when(wrapped.update(c)).thenReturn(c);

        LegalTag result = sut.update(c);

        assertEquals(c, result);
    }

    @Test
    public void should_returnWrappedObjectResult_when_delete(){
        LegalTag c = new LegalTag();
        when(wrapped.delete(c)).thenReturn(true);

        boolean result = sut.delete(c);

        assertTrue(result);
    }

    @Test
    public void should_returnWrappedObjectResult_when_list(){

        when(wrapped.list(any())).thenReturn(new ArrayList<>());

        Collection<LegalTag> result = sut.list(new ListLegalTagArgs());

        assertNotNull(result);
    }

    @Test
    public void should_returnWrappedObjectResult_when_callingGetByKind(){
        long[] input = new long[]{1L};
        LegalTag c = new LegalTag();
        Collection<LegalTag> output = Arrays.asList(c);
        when(wrapped.get(input)).thenReturn(output);

        Collection<LegalTag> result = sut.get(input);

        assertEquals(output, result);
    }

    class MockRepoWrapper extends LegalTagRepositoryWrapper {
        public MockRepoWrapper(LegalTagRepositoryWrapper wrapped){
            super(wrapped);
        }    }
}
