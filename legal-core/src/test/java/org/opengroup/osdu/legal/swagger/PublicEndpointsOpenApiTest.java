package org.opengroup.osdu.legal.swagger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.core.common.info.VersionInfoBuilder;
import org.opengroup.osdu.legal.controller.HealthCheckController;
import org.opengroup.osdu.legal.controller.InfoController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(
    classes = PublicEndpointsOpenApiTest.Application.class,
    properties = {
        "springdoc.api-docs.path=/api-docs",
        "swagger.api-server-url=/api/legal/v1"
    })
@MockitoBean(types = VersionInfoBuilder.class)
class PublicEndpointsOpenApiTest {

  @Autowired
  private WebApplicationContext context;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
  }

  @Test
  void shouldDescribeInfoAndHealthAsPublicWithoutTenantHeaders() throws Exception {
    ResultActions specification = mockMvc.perform(get("/api-docs"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.security[0].Authorization").isArray());
    for (String path : new String[] {"/info", "/_ah/liveness_check", "/_ah/readiness_check"}) {
      specification
          .andExpect(jsonPath("$.paths['" + path + "'].get.security").isEmpty())
          .andExpect(jsonPath("$.paths['" + path + "'].get.parameters").doesNotExist());
    }
  }

  @Test
  void shouldDocumentHealthChecksWithoutResponseBody() throws Exception {
    ResultActions specification = mockMvc.perform(get("/api-docs")).andExpect(status().isOk());
    for (String path : new String[] {"/_ah/liveness_check", "/_ah/readiness_check"}) {
      specification
          .andExpect(jsonPath("$.paths['" + path + "'].get.responses['200'].content").doesNotExist());
      mockMvc.perform(get(path)).andExpect(status().isOk());
    }
  }

  @SpringBootConfiguration
  @EnableAutoConfiguration
  @Import({
      InfoController.class, HealthCheckController.class,
      SwaggerConfiguration.class, SwaggerConfigurationProperties.class
  })
  static class Application {
  }
}
