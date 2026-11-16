package org.opengroup.osdu.legal.config;

import tools.jackson.databind.exc.UnrecognizedPropertyException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RejectUnknownJsonPropertiesTests {

    @Test
    public void should_throwUnrecognizedPropertyException_withoutNullPointer() {
        Object bean = new Object();
        UnrecognizedPropertyException exception = assertThrows(
                UnrecognizedPropertyException.class,
                () -> RejectUnknownJsonProperties.reject(bean, "isValid"));

        assertEquals("isValid", exception.getPropertyName());
        assertTrue(exception.getMessage().contains("isValid"));
        assertTrue(exception.getMessage().contains("not marked as ignorable"));
    }
}
