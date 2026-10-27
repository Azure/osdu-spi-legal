/*
 *  Copyright 2020-2025 Google LLC
 *  Copyright 2020-2025 EPAM Systems, Inc
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

package org.opengroup.osdu.legal.middleware;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.RequestMethod;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GlobalErrorHandlerTest {

    @Mock
    HttpServletRequest request;

    @ParameterizedTest
    @EnumSource(RequestMethod.class)
    void shouldPreserveErrorStatusForEveryHttpMethod(RequestMethod method) throws Exception {
        MockMvcBuilders.standaloneSetup(new GlobalErrorHandler())
            .addDispatcherServletCustomizer(servlet -> servlet.setDispatchTraceRequest(true))
            .build()
            .perform(MockMvcRequestBuilders.request(HttpMethod.valueOf(method.name()), "/error")
                .requestAttr("jakarta.servlet.error.status_code", 400))
            .andExpect(status().isBadRequest());
    }

    @Test
    void givenMessageWhenExceptionIsThrownThenMessageSanitiseTriggered() {
        // given
        String originalMessage = "original${}Message";
        String expectedMessage = "originalMessage";
        AppException appException = new AppException(200, "reason", originalMessage);
        appException.getError().setMessage(originalMessage);
        when(request.getAttribute("jakarta.servlet.error.exception")).thenReturn(appException);
        // when
        GlobalErrorHandler globalErrorHandler = new GlobalErrorHandler();
        globalErrorHandler.handleError(request, null);
        // then
        assertEquals(expectedMessage, appException.getError().getMessage());
    }

    @Test
    void givenReasonWhenExceptionIsThrownThenReasonSanitiseTriggered() {
        // given
        String originalReason = "original${}Reason";
        String expectedReason = "originalReason";
        AppException appException = new AppException(200, originalReason, "message");
        appException.getError().setReason(originalReason);
        when(request.getAttribute("jakarta.servlet.error.exception")).thenReturn(appException);
        // when
        GlobalErrorHandler globalErrorHandler = new GlobalErrorHandler();
        globalErrorHandler.handleError(request, null);
        // then
        assertEquals(expectedReason, appException.getError().getReason());
    }

}
