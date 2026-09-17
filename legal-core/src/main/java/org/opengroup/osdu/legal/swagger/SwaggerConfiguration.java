package org.opengroup.osdu.legal.swagger;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@Profile("!noswagger")
public class SwaggerConfiguration {

    @Autowired
    private SwaggerConfigurationProperties swaggerConfigurationProperties;

    @Bean
    public OpenAPI customOpenAPI() {

        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("Authorization")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");
        final String securitySchemeName = "Authorization";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(securitySchemeName);
        Components components = new Components().addSecuritySchemes(securitySchemeName, securityScheme);

        OpenAPI openAPI = new OpenAPI()
                .addSecurityItem(securityRequirement)
                .components(components)
                .info(apiInfo())
                .tags(tags());

        if(swaggerConfigurationProperties.isApiServerFullUrlEnabled())
            return openAPI;
        return openAPI
                .servers(Arrays.asList(new Server().url(swaggerConfigurationProperties.getApiServerUrl())));
    }

    private List<Tag> tags() {
        List<Tag> tags = new ArrayList<>();
        tags.add(new Tag().name("legaltag").description("LegalTags related endpoints"));
        tags.add(new Tag().name("legaltag-status-job").description("LegalTags status Job related endpoints"));
        tags.add(new Tag().name("health").description("Health related endpoints"));
        tags.add(new Tag().name("info").description("Version info endpoint"));
        return tags;
    }

    private Info apiInfo() {
        return new Info()
                .title(swaggerConfigurationProperties.getApiTitle())
                .description(swaggerConfigurationProperties.getApiDescription())
                .version(swaggerConfigurationProperties.getApiVersion())
                .license(new License().name(swaggerConfigurationProperties.getApiLicenseName()).url(swaggerConfigurationProperties.getApiLicenseUrl()))
                .contact(new Contact().name(swaggerConfigurationProperties.getApiContactName()).email(swaggerConfigurationProperties.getApiContactEmail()));
    }

    @Bean
    public OperationCustomizer operationCustomizer() {
        return (operation, handlerMethod) -> {
            Parameter dataPartitionId = new Parameter()
                    .name(DpsHeaders.DATA_PARTITION_ID)
                    .description("Tenant Id")
                    .in("header")
                    .required(true)
                    .schema(new StringSchema());
            return operation.addParametersItem(dataPartitionId);
        };
    }

}