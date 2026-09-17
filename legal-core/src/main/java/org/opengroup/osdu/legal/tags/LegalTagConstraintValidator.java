package org.opengroup.osdu.legal.tags;

import jakarta.inject.Inject;
import jakarta.validation.Configuration;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.security.AccessController;
import java.util.Set;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.legal.RulesetProvider;
import org.opengroup.osdu.legal.countries.LegalTagCountriesService;
import org.opengroup.osdu.core.common.model.legal.LegalTag;
import org.opengroup.osdu.core.common.model.legal.validation.DataTypeValidator;
import org.opengroup.osdu.legal.tags.validation.OtherRelevantDataCountriesValidator;
import org.opengroup.osdu.core.common.model.legal.validation.PropertiesValidator;
import org.opengroup.osdu.core.common.model.http.RequestInfo;
import org.springframework.stereotype.Service;
import org.hibernate.validator.internal.util.privilegedactions.NewInstance;

import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;

@Service
public class LegalTagConstraintValidator implements ConstraintValidatorFactory {

    @Inject
    protected RequestInfo requestInfo;

    @Inject
    protected RulesetProvider ruleSetProvider;

    @Inject
    private LegalTagCountriesService legalTagCountriesService;

    @Override
    public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {

        if (key == DataTypeValidator.class) {
            DataTypeValidator dtv = new DataTypeValidator(requestInfo);
            dtv.setRequestInfo(requestInfo);
            return (T)dtv;
        }
        else if(key == PropertiesValidator.class) {
            PropertiesValidator validator = new PropertiesValidator(ruleSetProvider.get());
            return (T)validator;
        }
        else if(key == OtherRelevantDataCountriesValidator.class) {
            OtherRelevantDataCountriesValidator validator = new OtherRelevantDataCountriesValidator(legalTagCountriesService);
            return (T)validator;
        }
        else {
            return run(NewInstance.action(key, "ConstraintValidator"));
        }
    }

    @Override
    public void releaseInstance(ConstraintValidator<?, ?> instance) {
        // do nothing
    }

    private <T> T run(PrivilegedAction<T> action) {
        return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
    }

    public Validator getValidator(){
        Configuration<?> config = Validation.byDefaultProvider().configure();
        config.constraintValidatorFactory(this);
        ValidatorFactory factory = config.buildValidatorFactory();
        return factory.getValidator();
    }

    public String getErrors(LegalTag tag){
        List<String> violationMessages = new ArrayList<>();
        Set<ConstraintViolation<LegalTag>> violations = getValidator().validate(tag);
        if (violations.isEmpty()) return null;
        else{
            for (ConstraintViolation<LegalTag> violation : violations) {
                violationMessages.add(violation.getMessage());
            }
        }
        return violationMessages.toString().replace("[", "").replace("]", "");
    }
    public <T> void isValidThrows(T tag){
        Set<ConstraintViolation<T>> violations = getValidator().validate(tag);
        if(!violations.isEmpty()){
            throw new ConstraintViolationException("Invalid LegalTag.", violations);
        }
    }
    public void setHeaders(DpsHeaders headers){
        requestInfo.setHeaders(headers);
    }
}