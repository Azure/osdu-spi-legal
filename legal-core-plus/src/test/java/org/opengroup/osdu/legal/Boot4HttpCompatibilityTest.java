package org.opengroup.osdu.legal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opengroup.osdu.core.common.http.HttpConfiguration;
import org.opengroup.osdu.core.common.model.legal.ServiceConfig;
import org.opengroup.osdu.core.common.model.legal.Properties;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.legal.config.WebConfiguration;
import org.opengroup.osdu.legal.middleware.GlobalExceptionMapper;
import org.opengroup.osdu.legal.security.SecurityConfiguration;
import org.opengroup.osdu.legal.tags.dto.UpdateLegalTag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = Boot4HttpCompatibilityTest.TestApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "server.servlet.context-path=/api/legal/v1",
      "management.server.port=0",
      "springdoc.api-docs.path=/api-docs",
      "LOG_PREFIX=legal-test"
    })
public class Boot4HttpCompatibilityTest {

  @Value("${local.server.port}")
  private int port;

  private final HttpClient client = HttpClient.newHttpClient();

  @Test
  public void should_preserveDateDeserializerAndPostBodyWithEitherTrailingSlash() throws Exception {
    for (String path : new String[] {"/legaltags", "/legaltags/"}) {
      HttpResponse<String> response = post(path, "{\"expirationDate\":\"2025-12-25\"}");
      assertEquals(response.body(), 200, response.statusCode());
      assertTrue(response.body(), response.body().contains("\"expirationDate\":\"2025-12-25\""));
    }
  }

  @Test
  public void should_rejectMalformedJson() throws Exception {
    assertEquals(400, post("/legaltags/", "{\"expirationDate\":").statusCode());
  }

  @Test
  public void should_requireCsrfProtectionWithoutBearerAuthentication() throws Exception {
    HttpResponse<String> response = client.send(
        HttpRequest.newBuilder(uri("/legaltags"))
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .POST(HttpRequest.BodyPublishers.ofString("{\"expirationDate\":\"2025-12-25\"}"))
            .build(),
        HttpResponse.BodyHandlers.ofString());

    assertTrue(
        "Expected CSRF protection to reject the request, got " + response.statusCode(),
        response.statusCode() == 401 || response.statusCode() == 403);
  }

  @Test
  public void should_useSharedDateDeserializerForBlankExpirationDate() throws Exception {
    HttpResponse<String> response = post("/legaltags/", "{\"expirationDate\":\"\"}");
    assertEquals(response.body(), 200, response.statusCode());
    assertTrue(response.body(), response.body().contains(
        "\"expirationDate\":\"" + Properties.DEFAULT_EXPIRATIONDATE + "\""));
  }

  @Test
  public void should_preserveMethodAuthorizationWithEitherTrailingSlash() throws Exception {
    for (String path : new String[] {"/legaltags/protected", "/legaltags/protected/"}) {
      HttpResponse<String> response = client.send(
          HttpRequest.newBuilder(uri(path)).GET().build(), HttpResponse.BodyHandlers.ofString());
      assertTrue("Expected unauthorized or forbidden, got " + response.statusCode(),
          response.statusCode() == 401 || response.statusCode() == 403);
    }
  }

  @Test
  public void should_keepSecurityFirewallInFrontOfTrailingSlashHandling() throws Exception {
    assertEquals(200, get("/legaltags/open").statusCode());
    assertEquals(200, get("/legaltags/open/").statusCode());
    for (String path : new String[] {
        "/legaltags/open;x=y", "/legaltags/open;x=y/", "/legaltags/x/%2e%2e/open",
        "/legaltags/x/%2e%2e/open/", "/legaltags/open%25", "/legaltags/open%25/"}) {
      HttpResponse<String> response = get(path);
      assertTrue(path + " reached the handler: " + response.statusCode(),
          response.statusCode() >= 400 && !response.body().contains("open-endpoint"));
    }
  }

  private HttpResponse<String> get(String path) throws Exception {
    return client.send(
        HttpRequest.newBuilder(uri(path)).GET().build(), HttpResponse.BodyHandlers.ofString());
  }

  @Test
  public void should_generateOpenApiForLegalModels() throws Exception {
    HttpResponse<String> response = client.send(
        HttpRequest.newBuilder(uri("/api-docs")).GET().build(),
        HttpResponse.BodyHandlers.ofString());
    assertEquals(response.body(), 200, response.statusCode());
    assertTrue(response.body(), response.body().contains("\"UpdateLegalTag\""));
    assertFalse(response.body().contains("##default"));
  }

  private HttpResponse<String> post(String path, String body) throws Exception {
    return client.send(
        HttpRequest.newBuilder(uri(path))
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build(),
        HttpResponse.BodyHandlers.ofString());
  }

  private URI uri(String path) {
    return URI.create("http://localhost:" + port + "/api/legal/v1" + path);
  }

  @SpringBootConfiguration
  @EnableAutoConfiguration
  @Import({WebConfiguration.class, SecurityConfiguration.class, ServiceConfig.class, HttpConfiguration.class,
      GlobalExceptionMapper.class, ProbeController.class})
  static class TestApplication {
    @Bean
    JaxRsDpsLog log() {
      return mock(JaxRsDpsLog.class);
    }
  }

  @RestController
  static class ProbeController {
    @PostMapping(value = "/legaltags", produces = MediaType.APPLICATION_JSON_VALUE)
    public UpdateLegalTag echo(@RequestBody UpdateLegalTag tag) {
      return tag;
    }

    @GetMapping("/legaltags/open")
    public String openEndpoint() {
      return "open-endpoint";
    }

    @GetMapping("/legaltags/protected")
    @PreAuthorize("hasAuthority('service.legal.admin')")
    public String protectedEndpoint() {
      return "authorized";
    }
  }
}
