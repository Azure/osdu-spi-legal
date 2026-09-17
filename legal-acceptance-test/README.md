# Legal acceptance tests

End-to-end acceptance tests for the OSDU Legal service. They run with JUnit 5 and depend on [`os-core-test`](https://community.opengroup.org/osdu/platform/system/lib/core/os-core-test) (`0.1.6`) for service URLs, authentication, HTTP clients, and entitlements role checks.

## Requirements

- Java 17
- Maven 3.8 or newer
- Access to an OSDU deployment with the Legal service enabled
- OpenID Connect client credentials or a bearer token for `PRIVILEGED_USER`

## How tests are structured

- **`LegalAcceptanceTests`** — base class extending `BaseAcceptanceTests`; wires `LegalTagsClient` for `ServiceType.LEGAL_V1` and `PRIVILEGED_USER`.
- **`LegalTagsClient`** (from `os-core-test`) — typed client for Legal Tags REST endpoints. Successful calls return `HttpResponse<T>`. HTTP error responses throw `ClientException` with an `AppError` body when present.
- **Assertions** — tests check `response.statusCode()`, `contentType()` (JSON responses), and `response.body()` directly. Expected failures use `expectClientError()` and assert on `ClientException.getStatusCode()` / `getError()`.
- **Cleanup** — `legalTagClient.teardown()` in `@AfterEach` deletes legal tags created during a test (tracked on successful `create`).

API tests live under `src/test/java/org/opengroup/osdu/legal/acceptancetests/`. Swagger/HTML endpoints are covered under `src/test/java/org/opengroup/osdu/legal/swagger/`. `/info` and feature-flag checks use `GetInfoApiAcceptanceTests` (`BaseGetInfoAcceptanceTests`).

For shared framework behaviour (env loading, tokens, retries, Allure, role verification), see the [os-core-test README](https://community.opengroup.org/osdu/platform/system/lib/core/os-core-test/-/blob/main/README.md).

### Configuration sources

Settings are read in this order (first match wins):

1. `.env` in the project working directory (when running `mvn test` from `legal-acceptance-test`)
2. `src/test/resources/.env` on the test classpath
3. JVM system properties (`-Dname=value`)
4. OS environment variables

Set `OS_CORE_TEST_IGNORE_WORKING_DIR_ENV=true` to skip the working-directory `.env` file and use only classpath, system properties, and OS environment variables.

### Required environment variables

| name                | value                         | description                                                                              | sensitive? | required |
|---------------------|-------------------------------|------------------------------------------------------------------------------------------|------------|----------|
| `HOST`              | ex `https://osdu.example.com` | OSDU API host; `os-core-test` appends service paths (Legal: `/api/legal/v1/`)           | no         | yes      |
| `DATA_PARTITION_ID` | ex `opendes`                  | Data partition used for API calls and test data                                          | no         | yes      |

### Authentication

Provide **either** OpenID Connect client credentials **or** a pre-configured bearer token for `PRIVILEGED_USER` (the only user type used by these tests).

**Option A — OIDC client credentials**

| name                                            | value                                              | description                                          | sensitive? | required |
|-------------------------------------------------|----------------------------------------------------|------------------------------------------------------|------------|----------|
| `TEST_OPENID_PROVIDER_URL`                      | ex `https://keycloak.example.com/auth/realms/osdu` | OpenID provider realm URL                            | yes        | yes      |
| `PRIVILEGED_USER_OPENID_PROVIDER_CLIENT_ID`     | `********`                                         | OAuth2 client ID for the privileged integration user | yes        | yes      |
| `PRIVILEGED_USER_OPENID_PROVIDER_CLIENT_SECRET` | `********`                                         | OAuth2 client secret                                 | yes        | yes      |
| `PRIVILEGED_USER_OPENID_PROVIDER_SCOPE`         | ex `openid` or `api://my-app/.default`             | OAuth2 scope (optional; defaults to `openid`)        | no         | no       |

**Option B — pre-configured bearer token**

| name                    | value      | description                                                    | sensitive? | required |
|-------------------------|------------|----------------------------------------------------------------|------------|----------|
| `PRIVILEGED_USER_TOKEN` | `********` | Bearer token for `PRIVILEGED_USER` (skips OIDC token exchange) | yes        | yes      |

### Optional environment variables

| name                         | value                             | description                                                                                  | required |
|------------------------------|-----------------------------------|----------------------------------------------------------------------------------------------|----------|
| `EXPOSE_FEATUREFLAG_ENABLED` | `true` / `false`                  | When `true`, `/info` tests assert feature flags listed in `GetInfoApiAcceptanceTests`        | no       |
| `USER_ROLES_CHECK_ENABLED`   | `true` / `false`                  | Enable JWT role verification against entitlements (default: driven by `required-roles.json`) | no       |
| `USER_REQUIRED_ROLES_CONFIG` | ex `/path/to/required-roles.json` | Override path to the required-roles configuration file                                       | no       |

### Entitlements configuration for integration accounts

Roles are defined in `src/test/resources/required-roles.json` (`enabled: true`). The integration account behind `PRIVILEGED_USER` must have:

| PRIVILEGED_USER           |
|---------------------------|
| users                     |
| service.entitlements.user |
| service.legal.admin       |
| service.legal.editor      |
| service.legal.user        | 

### Environment-specific tests

- **`CronJobUpdateAcceptanceTests`** — reads country codes from `TenantConfigTestingPurpose.json` and `SecondTenantConfigTestingPurpose.json` on the classpath. Skips when those resources yield no usable codes (tenant/country setup must match your deployment).
- **`QueryLegalTagsApiAcceptanceTests`** — requires the Legal query API to be enabled (`featureFlag.legalTagQueryApi.enabled`). Some scenarios accept HTTP `405` when the query endpoint is not exposed.

## Run tests

```bash
# Export the variables above, or place them in legal-acceptance-test/.env
cd legal-acceptance-test && mvn clean test
```

Run a single test class:

```bash
mvn test -Dtest=CreateLegalTagApiAcceptanceTests
```

## License

Copyright © Google LLC

Copyright © EPAM Systems

Copyright © ExxonMobil

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
