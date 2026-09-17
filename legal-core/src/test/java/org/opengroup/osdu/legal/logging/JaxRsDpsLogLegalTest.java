package org.opengroup.osdu.legal.logging;

import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.http.Request;
import org.junit.jupiter.api.extension.ExtendWith;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import org.opengroup.osdu.core.common.logging.ILogger;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.logging.audit.AuditPayload;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class JaxRsDpsLogLegalTest {

    @Mock
    private ILogger log;

    @Mock
    private DpsHeaders headers;

    @InjectMocks
    private JaxRsDpsLog sut;

    @BeforeEach
    public void setup() {
        Map<String, String> map = new HashMap<>();
        map.put("authorization", "12345");
        map.put("cor-id", "cor123");
        map.put(DpsHeaders.CORRELATION_ID, "cor123");
        map.put("account-id", "acc123");
        when(headers.getHeaders()).thenReturn(map);
        ReflectionTestUtils.setField(sut, "LOG_PREFIX", "legal");
    }

    @Test
    public void should_includeAllHeadersExceptAuth_when_writingALog(){
        sut.info("msg");

        ArgumentCaptor<Map> argument = ArgumentCaptor.forClass(Map.class);
        verify(log).info(any(), any(), argument.capture());
        assertEquals( "cor123", argument.getValue().get(DpsHeaders.CORRELATION_ID));
        assertFalse(argument.getValue().containsKey("authorization"));
    }

    @Test
    public void should_writeToAuditLogWithGivenPayload_on_auditRequests(){
        AuditPayload pl = new AuditPayload();
        sut.audit(pl);
        verify(log).audit(eq("legal.audit"), eq(pl), any());
    }
    @Test
    public void should_writeToRequestLogWithGivenHttpObj_on_requestLog(){
        Request http = Request.builder().build();
        sut.request(http);
        verify(log).request(eq("legal.request"), eq(http), any());
    }
    @Test
    public void should_writeToAppLogWithGivenMsg_on_errorLogrequest(){
        sut.error("error");
        verify(log).error(eq("legal.app"), eq("error"), any());
    }

}
