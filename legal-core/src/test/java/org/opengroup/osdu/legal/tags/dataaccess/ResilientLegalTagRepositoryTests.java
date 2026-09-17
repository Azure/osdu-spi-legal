package org.opengroup.osdu.legal.tags.dataaccess;

import com.google.common.collect.Iterables;
import com.google.rpc.Code;
import org.opengroup.osdu.core.common.model.legal.ListLegalTagArgs;
import org.opengroup.osdu.core.common.model.legal.PersistenceException;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagRepository;
import org.opengroup.osdu.core.common.model.legal.LegalTag;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ResilientLegalTagRepositoryTests {

    @Test
    public void should_ReturnWrappedObjectResult_When_CreateIsSuccessful(){
        ILegalTagRepository wrapped = mock(ILegalTagRepository.class);
        when(wrapped.create(any())).thenReturn(5005L);

        ResilientLegalTagRepository sut = new ResilientLegalTagRepository(wrapped, 10);
        Long result = sut.create(new LegalTag());

        assertEquals(5005L, (long)result);
    }

    @Test
    public void should_CallCreate3Times_When_ItThrowsDataStoreExceptionAborted() throws PersistenceException {
        CreateSut createSut = new CreateSut().init(Code.ABORTED_VALUE);
        ResilientLegalTagRepository sut = createSut.getSut();
        ILegalTagRepository wrapped = createSut.getWrapped();

        run(sut);

        verify(wrapped, times(3)).create(any());
    }

    @Test
    public void should_BreakCircuit_When_CreateThrowsConsecutiveErrors() throws PersistenceException {
        CreateSut createSut = new CreateSut().init(Code.CANCELLED_VALUE);
        ResilientLegalTagRepository sut = createSut.getSut();
        ILegalTagRepository wrapped = createSut.getWrapped();

        int max = 30;
        for(int i = 0; i < max; i++) {
            run(sut);
            verify(wrapped, times(i+1)).create(any());
        }
        run(sut, "CircuitBreaker 'ContractDatastore' is OPEN and does not permit further calls");
        verify(wrapped, times(max)).create(any()); //shouldn't call underlying method as circuit breaker is open
    }

    @Test
    public void should_NotBreakCircuit_When_CreateThrowsConsecutiveErrorsOnUnhandledErrorCode() throws PersistenceException {
        CreateSut createSut = new CreateSut().init(Code.ALREADY_EXISTS_VALUE);
        ResilientLegalTagRepository sut = createSut.getSut();
        ILegalTagRepository wrapped = createSut.getWrapped();

        int max = 30;
        for(int i = 0; i < max; i++) {
            run(sut);
            verify(wrapped, times(i+1)).create(any());
        }
        run(sut);
        verify(wrapped, times(max+1)).create(any()); //shouldn't call underlying method as circuit breaker is open
    }

    @Test
    public void should_CallCreate3Times_When_CreateThrowsDataStoreExceptionDeadLineExceeded() throws PersistenceException {
        CreateSut createSut = new CreateSut().init(Code.DEADLINE_EXCEEDED_VALUE);
        ResilientLegalTagRepository sut = createSut.getSut();
        ILegalTagRepository wrapped = createSut.getWrapped();

        run(sut);

        verify(wrapped, times(3)).create(any());
    }

    @Test
    public void should_CallCreate3Times_When_CreateThrowsDataStoreExceptionUnavailable() throws PersistenceException {
        CreateSut createSut = new CreateSut().init(Code.UNAVAILABLE_VALUE);
        ResilientLegalTagRepository sut = createSut.getSut();
        ILegalTagRepository wrapped = createSut.getWrapped();

        run(sut);

        verify(wrapped, times(3)).create(any());
    }

    @Test
    public void should_CallCreateOnce_When_CreateThrowsAPersistenceExceptionThatIsNotMapped() throws Exception {
        CreateSut createSut = new CreateSut().init(Code.ALREADY_EXISTS_VALUE);
        ResilientLegalTagRepository sut = createSut.getSut();
        ILegalTagRepository wrapped = createSut.getWrapped();

        run(sut);

        verify(wrapped, times(1)).create(any());
    }

    @Test
    public void should_ReturnWrappedObjectResult_When_GetIsSuccessful(){
        ILegalTagRepository wrapped = mock(ILegalTagRepository.class);
        LegalTag output = new LegalTag();
        output.setId(2L);
        long[] input = new long[]{1L};
        when(wrapped.get(input)).thenReturn(Arrays.asList(output));

        ResilientLegalTagRepository sut = new ResilientLegalTagRepository(wrapped, 10);
        Collection<LegalTag> result = sut.get(input);

        assertEquals(1, result.size());
        assertEquals(2L, (long)Iterables.get(result, 0).getId());
    }

    @Test
    public void should_CallGet3Times_When_GetThrowsDataStoreExceptionAborted() throws PersistenceException {
        CreateSut createSut = new CreateSut().init(Code.ABORTED_VALUE);
        ResilientLegalTagRepository sut = createSut.getSut();
        ILegalTagRepository wrapped = createSut.getWrapped();

        long[] input = new long[]{1L};
        try {
            sut.get(input);
        }catch(Exception ex){
            assertEquals("hi", ex.getMessage());
        }

        verify(wrapped, times(3)).get(input);
    }
    @Test
    public void should_ReturnWrappedObjectResult_When_DeleteIsSuccessful(){
        LegalTag c = new LegalTag();
        ILegalTagRepository wrapped = mock(ILegalTagRepository.class);
        when(wrapped.delete(c)).thenReturn(true);

        ResilientLegalTagRepository sut = new ResilientLegalTagRepository(wrapped, 10);
        Boolean result = sut.delete(c);

        assertTrue(result);
    }
    @Test
    public void should_CallDelete3Times_When_DeleteThrowsDataStoreExceptionAborted() throws PersistenceException {
        CreateSut createSut = new CreateSut().init(Code.ABORTED_VALUE);
        ResilientLegalTagRepository sut = createSut.getSut();
        ILegalTagRepository wrapped = createSut.getWrapped();
        LegalTag c = new LegalTag();
        try {
            sut.delete(c);
        }catch(Exception ex){
            assertEquals("hi", ex.getMessage());
        }

        verify(wrapped, times(3)).delete(c);
    }
    @Test
    public void should_ReturnWrappedObjectResult_When_UpdateIsSuccessful(){
        LegalTag expectedResult = new LegalTag();
        ILegalTagRepository wrapped = mock(ILegalTagRepository.class);
        when(wrapped.update(expectedResult)).thenReturn(expectedResult);

        ResilientLegalTagRepository sut = new ResilientLegalTagRepository(wrapped, 10);
        LegalTag result = sut.update(expectedResult);

        assertEquals(expectedResult, result);
    }
    @Test
    public void should_returnWrappedObjectResult_when_listIsSuccessful(){
        LegalTag c = new LegalTag();
        List<LegalTag> output = new ArrayList<>();
        ILegalTagRepository wrapped = mock(ILegalTagRepository.class);
        when(wrapped.list(any())).thenReturn(output);

        ResilientLegalTagRepository sut = new ResilientLegalTagRepository(wrapped, 10);
        Collection<LegalTag> result = sut.list(new ListLegalTagArgs());

        assertEquals(output, result);
    }
    @Test
    public void should_callList3Times_when_listThrowsPersistenceExceptionAborted() throws PersistenceException{
        CreateSut createSut = new CreateSut().init(Code.ABORTED_VALUE);
        ResilientLegalTagRepository sut = createSut.getSut();
        ILegalTagRepository wrapped = createSut.getWrapped();
        ListLegalTagArgs args = new ListLegalTagArgs();
        try {
            sut.list(args);
        }catch(Exception ex){
            assertEquals("hi", ex.getMessage());
        }

        verify(wrapped, times(3)).list(args);
    }

    private void run(ResilientLegalTagRepository sut) {
        run(sut, "hi");
    }
    private void run(ResilientLegalTagRepository sut, String expectedMsg) {
        try {
            sut.create(new LegalTag());
        }catch(Exception ex){
            assertEquals(expectedMsg, ex.getMessage());
        }
    }

    private class CreateSut {
        private ILegalTagRepository wrapped;
        private ResilientLegalTagRepository sut;

        public ILegalTagRepository getWrapped() {
            return wrapped;
        }

        public ResilientLegalTagRepository getSut() {
            return sut;
        }

        public CreateSut init(int code) {
            wrapped = mock(ILegalTagRepository.class);
            when(wrapped.create(any())).thenThrow(new PersistenceException(code, "hi", ""));
            when(wrapped.get(any())).thenThrow(new PersistenceException(code, "hi", ""));
            when(wrapped.delete(any())).thenThrow(new PersistenceException(code, "hi", ""));
            when(wrapped.list(any())).thenThrow(new PersistenceException(code, "hi", ""));

            sut = new ResilientLegalTagRepository(wrapped, 10);
            return this;
        }
    }
}
