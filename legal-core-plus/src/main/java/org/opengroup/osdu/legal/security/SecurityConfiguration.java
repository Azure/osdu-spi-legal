/*
 *  Copyright 2020-2024 Google LLC
 *  Copyright 2020-2024 EPAM Systems, Inc
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
package org.opengroup.osdu.legal.security;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

  private static final RequestMatcher BEARER_AUTHENTICATED_REQUEST =
      request -> {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        return authorization != null
            && authorization.regionMatches(true, 0, "Bearer ", 0, "Bearer ".length());
      };

  private static final String[] AUTH_ALLOW_LIST = {
    "/",
    "/index.html",
    "/api-docs.yaml",
    "/api-docs/swagger-config",
    "/api-docs/**",
    "/swagger",
    "/swagger-ui.html",
    "/swagger-ui/**",
    "/legaltags",
    "/legaltags/**",
    "/legaltags:batchRetrieve",
    "/legaltags:validate",
    "/legaltags:properties",
    "/legaltags:query",
    "/jobs/updateLegalTagStatus",
    "/info",
    "/_ah/liveness_check",
    "/_ah/readiness_check",
    "/actuator/**",
    "/health/**"
  };

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.cors(AbstractHttpConfigurer::disable)
        // OSDU clients send bearer credentials explicitly; keep CSRF enabled for requests
        // using ambient browser credentials such as HTTP Basic.
        .csrf(csrf -> csrf.ignoringRequestMatchers(BEARER_AUTHENTICATED_REQUEST))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            authorize ->
                authorize.requestMatchers(AUTH_ALLOW_LIST).permitAll().anyRequest().denyAll())
        .httpBasic(withDefaults());
    return http.build();
  }
}
