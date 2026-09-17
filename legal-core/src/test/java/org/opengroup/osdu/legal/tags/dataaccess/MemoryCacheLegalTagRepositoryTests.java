package org.opengroup.osdu.legal.tags.dataaccess;

import com.google.common.collect.Iterables;
import org.opengroup.osdu.core.common.model.legal.LegalTag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.core.common.model.legal.ListLegalTagArgs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

public class MemoryCacheLegalTagRepositoryTests {

    LegalTag legaltag = new LegalTag();
    LegalTagRepositoryWrapper wrapped = mock(LegalTagRepositoryWrapper.class);
    MemoryCacheLegalTagRepository sut = new MemoryCacheLegalTagRepository(wrapped, "my-tenant");

    @BeforeEach
    public void setupTest(){
        legaltag.setName("name");
        legaltag.setId(1L);
        when(wrapped.get(any())).thenReturn(Arrays.asList(legaltag));
    }

    @Test
    public void should_throwIllegalArgumentException_when_tenantNameIsBlank() {

        try {
            MemoryCacheLegalTagRepository sut1 = new MemoryCacheLegalTagRepository(wrapped, "");
            fail("Expected error");
        } catch(IllegalArgumentException illegalArgumentException){
            assertEquals("tenantName must be supplied", illegalArgumentException.getMessage());
        }

    }

    @Test
    public void should_returnLegalTagCollection_when_givenListLegalTagArgs(){
        ListLegalTagArgs listLegalTagArgs = new ListLegalTagArgs();
        listLegalTagArgs.setCursor("0");
        listLegalTagArgs.setLimit(100);
        listLegalTagArgs.setIsValid(true);
        Collection<LegalTag> expectedLegalTagCollection = Arrays.asList(legaltag);
        when(wrapped.list(listLegalTagArgs)).thenReturn(expectedLegalTagCollection);

        Collection<LegalTag> legalTagCollection = sut.list(listLegalTagArgs);

        assertEquals(expectedLegalTagCollection, legalTagCollection);
    }

    @Test
    public void should_returnWrappedObjectResult_when_callingCreate(){
        LegalTag c = new LegalTag();
        when(wrapped.create(c)).thenReturn(5L);

        long result = sut.create(c);

        assertEquals(5L, result);
    }

    @Test
    public void should_returnWrappedLegalTag_when_callingGetByKind_andThen_returnCached_WhenCallingItAgain(){
        long[] input = new long[]{1L};
        Collection<LegalTag> results = sut.get(input);
        LegalTag result= Iterables.get(results, 0);
        assertEquals(legaltag, result);
        verify(wrapped, times(1)).get(input);

        results = sut.get(input);
        result = Iterables.get(results, 0);
        assertEquals(legaltag, result);
        verify(wrapped, times(1)).get(input); //should not have called wrapped the second time
    }

    @Test
    public void should_notCache_when_getReturnsNull(){
        long[] input = new long[]{1L};
        when(wrapped.get(input)).thenReturn(null);

        Collection<LegalTag> results = sut.get(input);
        assertEquals(0, results.size());
        verify(wrapped, times(1)).get(input);

        results = sut.get(input);
        assertEquals(0, results.size());
        verify(wrapped, times(2)).get(input);
    }

    @Test
    public void should_notInvalidateCache_when_deleteIsNotSuccessful(){
        when(wrapped.delete(legaltag)).thenReturn(false);

        long[] input = new long[]{1L};
        Collection<LegalTag> results = sut.get(input);
        LegalTag result= Iterables.get(results, 0);
        sut.delete(result);

        results = sut.get(input);
        result= Iterables.get(results, 0);
        assertEquals(legaltag, result);
        verify(wrapped, times(1)).get(input); //should not have called wrapped as cahce was not invalidated
    }

    @Test
    public void should_invalidateCache_when_deleteIsNotSuccessful(){
        when(wrapped.delete(legaltag)).thenReturn(true);

        long[] input = new long[]{1L};
        Collection<LegalTag> results = sut.get(input);

        LegalTag result= Iterables.get(results, 0);
        sut.delete(result);

        results = sut.get(input);
        result= Iterables.get(results, 0);
        assertEquals(legaltag, result);
        verify(wrapped, times(2)).get(input); //should not have called wrapped as cahce was not invalidated
    }

    @Test
    public void should_invalidateCache_when_deleteIsSuccessful(){
        when(wrapped.update(legaltag)).thenReturn(legaltag);

        long[] input = new long[]{1L};
        sut.get(input);//this will add it to the cache
        sut.get(input);//this should use cache
        verify(wrapped, times(1)).get(input);
        LegalTag result = sut.update(legaltag);//this should invalidate cache
        sut.get(input);

        assertEquals(legaltag, result);
        verify(wrapped, times(2)).get(input);
    }

    @Test
    public void should_notInvalidateCache_when_deleteIsSuccessful(){
        when(wrapped.update(legaltag)).thenReturn(null);

        long[] input = new long[]{1L};
        sut.get(input);//this will add it to the cache
        sut.get(input);//this should use cache
        verify(wrapped, times(1)).get(input);
        LegalTag result = sut.update(legaltag);//this should not invalidate cache
        sut.get(input);

        assertEquals(null, result);
        verify(wrapped, times(1)).get(input);
    }
}
