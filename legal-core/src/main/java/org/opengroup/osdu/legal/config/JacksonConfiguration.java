package org.opengroup.osdu.legal.config;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.cfg.CoercionAction;
import tools.jackson.databind.cfg.CoercionInputShape;
import tools.jackson.databind.cfg.MapperBuilder;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Applies strict JSON type coercion as a baseline for request bodies.
 *
 * <p>LegalTag create/update/query DTOs also use {@link StrictStringDeserializer} /
 * {@link StrictIntDeserializer} / {@link CreateLegalTagPropertiesDeserializer} so rejection does
 * not depend on this ObjectMapper-wide config being wired into the MVC converter.
 */
@Configuration
public class JacksonConfiguration {

    @Bean
    public JsonMapperBuilderCustomizer strictStringCoercionCustomizer() {
        return JacksonConfiguration::applyStrictStringCoercion;
    }

    public static void applyStrictStringCoercion(MapperBuilder<?, ?> builder) {
        builder.disable(DeserializationFeature.ACCEPT_FLOAT_AS_INT);
        builder.withCoercionConfig(String.class, config -> config
                .setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Integer, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Float, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.EmptyArray, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.EmptyObject, CoercionAction.Fail));
        builder.withCoercionConfig(Integer.class, config -> config
                .setCoercion(CoercionInputShape.String, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Float, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail));
        builder.withCoercionConfig(Integer.TYPE, config -> config
                .setCoercion(CoercionInputShape.String, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Float, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail));
    }
}
