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

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Hidden;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.apache.commons.text.StringEscapeUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Hidden
@RestController
public class GlobalErrorHandler implements ErrorController {

  private static final Gson gson = new Gson();

  @RequestMapping("/error")
  public ResponseEntity<Object> handleError(final HttpServletRequest request,
                                                  final HttpServletResponse response) {

    Object exception = request.getAttribute("jakarta.servlet.error.exception");
    Object statusCode = request.getAttribute("jakarta.servlet.error.status_code");
    Object servletMessage = request.getAttribute("jakarta.servlet.error.message");
    if(exception instanceof AppException){
      AppException  appException = (AppException)exception;
      String message = appException.getError().getMessage();
          // need sanitize the user's input because inputs may contain EL expressions like ${expression}
          sanitization(appException);
      return new ResponseEntity<Object>(appException.getError(), HttpStatus.resolve(appException.getError().getCode()));
    }
    else if (statusCode != null) {
      String message = servletMessage != null ?  servletMessage + ", status: " + statusCode : "status: " + statusCode;
      return new ResponseEntity<Object>(gson.toJson(message), HttpStatus.resolve(Integer.parseInt(statusCode.toString())));
    }
    throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Server error", "An unknown error has occurred.");
  }

    /**
     * Need to sanitize only two fields: message and reason. No need to sanitize other AppError object's fields since
     * they are annotated with  @JsonIgnore thus they never will reach a client.
     */
    private static void sanitization(AppException appException) {
        sanitizeMessage(appException);
        sanitizeReason(appException);
    }

    private static void sanitizeMessage(AppException appException) {
        String message = appException.getError().getMessage();
        message = sanitize(message);
        appException.getError().setMessage(message);
    }

    private static void sanitizeReason(AppException appException) {
        String reason = appException.getError().getReason();
        reason = sanitize(reason);
        appException.getError().setReason(reason);
    }

    private static String sanitize(String message) {
        if (message != null) {
            message = StringEscapeUtils.escapeHtml4(message);
            message = message.replace("#{", "")
                .replace("${", "")
                .replace("}", "")
                .replace("'", "")
                .replace("\"", "");
        } else {
            message = "No message given in AppException";
        }
        return message;
    }
}
