package org.opengroup.osdu.legal.tags.util;

import com.google.rpc.Code;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.legal.PersistenceException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class PersistenceExceptionToAppExceptionMapperTests {

    @Mock
    private JaxRsDpsLog log;

    @InjectMocks
    private PersistenceExceptionToAppExceptionMapper sut;

    @Test
    public void shouldReturnFunctionsValue_whenRunningGivenFunction(){

        String result = sut.run((String s) -> "hi " + s, "world", "reason");

        assertEquals("hi world", result);
    }

    @Test
    public void shouldThrowAppException404_whenRunningFunction_throwsPersistenceExceptionNotFound(){

        try {
            sut.run((String s) -> {throw new PersistenceException(Code.NOT_FOUND_VALUE, "a", null); } ,"world", "reason");
            fail("Should have thrown exception");
        }
        catch(AppException e){
            assertEquals(404, e.getError().getCode());
            assertEquals("reason", e.getError().getReason());
            assertEquals("Not found.", e.getError().getMessage());
        }
    }

    @Test
    public void shouldThrowAppException400_whenRunningFunction_throwsPersistenceExceptionInvalidArgument(){

        try {
            sut.run((String s) -> {throw new PersistenceException(Code.INVALID_ARGUMENT_VALUE, "a", null); } ,"world", "reason");
            fail("Should have thrown exception");
        }
        catch(AppException e){
            assertEquals(400, e.getError().getCode());
            assertEquals("reason", e.getError().getReason());
            assertEquals("Invalid argument given.", e.getError().getMessage());
        }
    }

    @Test
    public void shouldThrowAppException400_whenRunningFunction_throwsPersistenceExceptionAlreadyExists(){

        try {
            sut.run((String s) -> {throw new PersistenceException(Code.ALREADY_EXISTS_VALUE, "a", null); } ,"world", "reason");
            fail("Should have thrown exception");
        }
        catch(AppException e){
            assertEquals(400, e.getError().getCode());
            assertEquals("reason", e.getError().getReason());
            assertEquals("Already exists.", e.getError().getMessage());
        }
    }

    @Test
    public void shouldThrowAppException500_whenRunningFunction_throwsDatastoreExceptAborted(){

        try {
            sut.run((String s) -> { throw new PersistenceException(Code.ABORTED_VALUE, "a", null); },"world", "reason");
            fail("Should have thrown exception");
        }
        catch(AppException e){
            assertEquals(500, e.getError().getCode());
            assertEquals("reason", e.getError().getReason());
            assertEquals("Unexpected error. Please wait 30 seconds and try again.", e.getError().getMessage());
        }
    }
}
