package org.opengroup.osdu.legal.config;

import tools.jackson.core.TokenStreamLocation;
import tools.jackson.databind.exc.UnrecognizedPropertyException;

import java.util.Collections;

/**
 * DTO-level rejection of extra JSON properties. Spring Boot disables
 * {@code FAIL_ON_UNKNOWN_PROPERTIES} on the MVC ObjectMapper, so class
 * {@code @JsonIgnoreProperties(ignoreUnknown = false)} is not enough.
 */
public final class RejectUnknownJsonProperties {

    private RejectUnknownJsonProperties() {
    }

    /**
     * Throws {@link UnrecognizedPropertyException} without requiring a live
     * {@code JsonParser}. {@code UnrecognizedPropertyException.from(null, ...)}
     * NPEs on {@code parser.currentLocation()}.
     */
    public static void reject(Object bean, String name) throws UnrecognizedPropertyException {
        Class<?> referringClass = bean.getClass();
        String message = String.format(
                "Unrecognized field \"%s\" (class %s), not marked as ignorable",
                name, referringClass.getName());
        UnrecognizedPropertyException exception = new UnrecognizedPropertyException(
                null, message, TokenStreamLocation.NA, referringClass, name, Collections.emptySet());
        exception.prependPath(bean, name);
        throw exception;
    }
}
