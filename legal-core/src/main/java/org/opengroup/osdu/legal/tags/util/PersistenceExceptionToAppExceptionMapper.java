package org.opengroup.osdu.legal.tags.util;

import com.google.common.base.Strings;
import com.google.rpc.Code;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.legal.PersistenceException;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.springframework.stereotype.Component;

import java.util.function.Function;

import jakarta.inject.Inject;

@Component
public class PersistenceExceptionToAppExceptionMapper {

    @Inject
    private JaxRsDpsLog log;

    public <T, R> R run(Function<T, R> function, T input, String reason){
        try{
            return function.apply(input);
        }
        catch(PersistenceException ex) {
            String clientError = getClientError(ex);
            if(!Strings.isNullOrEmpty(clientError)) {
                log.error(reason, ex);
                throw new AppException(400, reason, clientError);
            }
            else if(notFound(ex)){
                log.error(reason, ex);
                throw new AppException(404, reason, "Not found.");
            }
            else {
                log.error(reason, ex);
                throw new AppException(500, reason, "Unexpected error. Please wait 30 seconds and try again.");
            }
        }
    }

    private boolean notFound(PersistenceException ex) {
        int code = ex.getCode();
        return code == Code.NOT_FOUND_VALUE;
    }

    private String getClientError(PersistenceException ex) {
        int code = ex.getCode();
        String output = null;
        switch(code) {
            case(Code.ALREADY_EXISTS_VALUE):
                output = "Already exists.";
                break;
            case (Code.INVALID_ARGUMENT_VALUE):
                output = "Invalid argument given.";
                break;
            default:
                break;
        }
        return output;
    }
}
