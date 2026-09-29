# OSDU Legal Service for Azure

[![Release](https://img.shields.io/github/v/release/Azure/osdu-spi-legal)](https://github.com/Azure/osdu-spi-legal/releases)
[![Validate](https://github.com/Azure/osdu-spi-legal/actions/workflows/validate.yml/badge.svg?branch=main)](https://github.com/Azure/osdu-spi-legal/actions/workflows/validate.yml)
[![License: Apache 2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

> [!NOTE]
> Shared service code comes from the [OSDU community upstream](https://community.opengroup.org/osdu/platform/security-and-compliance/legal); the [OSDU documentation](https://osdu.pages.opengroup.org/platform/security-and-compliance/legal/) covers the API.

Legal manages legal tags: the named compliance rules (data type, origin country, contract, expiration) that a record of raw or source data must carry before it can be stored.

## At a glance

| | |
|---|---|
| API base path | `/api/legal/v1/` |
| Swagger UI | `/api/legal/v1/swagger` |
| Health | `:8081/actuator/health` |
| Depends on | Partition, Entitlements |
| Azure resources | Cosmos DB (legal tags), Storage (country configuration), Service Bus (`legaltags` topic), Redis |
| Deployed by | [OSDU SPI Stack](https://github.com/Azure/osdu-spi-stack) (`software/stacks/osdu/services/legal.yaml`) |

## Repository layout

[CONTRIBUTING.md](CONTRIBUTING.md) explains where each kind of change belongs.

| Path | Owner | Contents |
|---|---|---|
| `legal-core/` | OSDU upstream | Shared service code |
| `provider/legal-azure/` | This repository | Azure provider |
| `legal-acceptance-test/` | OSDU upstream | End-to-end suite run against a deployed environment |
| `testing/legal-test-azure/` | This repository | Legacy Azure integration tests |
| `.spi/service.yaml` | This repository | How CI deploys and tests the service on SPI Stack |

## Build

Requires Java 17 and Maven 3.6.3+. OSDU dependencies resolve from the public community registry through the settings file in `.mvn`:

```bash
mvn --settings .mvn/community-maven.settings.xml -P core,azure clean install
```

The runnable jar lands at `provider/legal-azure/target/legal-azure-*-spring-boot.jar`.

## Configuration

SPI Stack sets the service's environment from two places: the shared `osdu-config` ConfigMap and the service's own entry in [`services/legal.yaml`](https://github.com/Azure/osdu-spi-stack/blob/main/software/stacks/osdu/services/legal.yaml). Those files are the contract; the tables below list what Legal actually reads from them.

**Shared, from `osdu-config`:**

| Variable | Purpose |
|---|---|
| `AZURE_TENANT_ID` | Entra tenant |
| `AAD_CLIENT_ID` | Application ID that caller tokens are issued for |
| `KEYVAULT_URI` | Central Key Vault |
| `SERVER_PORT` | HTTP port (`8080`) |
| `APPINSIGHTS_KEY` | Telemetry |

**Specific to Legal**, from `services/legal.yaml`:

| Variable | Value on SPI Stack | Purpose |
|---|---|---|
| `SERVER_SERVLET_CONTEXTPATH` | `/api/legal/v1/` | API base path |
| `AZURE_ISTIOAUTH_ENABLED` | `true` | Trust the mesh's token validation |
| `AZURE_PAAS_WORKLOADIDENTITY_ISENABLED` | `true` | Authenticate to Azure with workload identity |
| `PARTITION_SERVICE_ENDPOINT` | `http://partition/api/partition/v1` | Per-partition resource lookup |
| `ENTITLEMENTS_SERVICE_ENDPOINT` | `http://entitlements/api/entitlements/v2` | Caller authorization |
| `ENTITLEMENTS_SERVICE_API_KEY` | `OBSOLETE` | Legacy API key passed to the Entitlements client; SPI Stack sets a placeholder, and the property has no default, so it must be set |
| `COSMOSDB_DATABASE` | `osdu-db` | Database inside each partition's Cosmos DB account |
| `AZURE_STORAGE_CONTAINER_NAME` | `legal-service-azure-configuration` | Container holding the country configuration |
| `SERVICEBUS_TOPIC_NAME` | `legaltags` | Topic for legal tag status changes |
| `LEGAL_SERVICE_REGION` | `us` | Region used in country validation |
| `REDIS_DATABASE` | `2` | Redis database index reserved for Legal |
| `FEATUREFLAG_LEGALTAGQUERYAPIFREETEXTALLFIELDS_ENABLED` | `true` | See [Service notes](#service-notes) |
| `JDK_JAVA_OPTIONS` | `--add-opens java.base/java.lang=ALL-UNNAMED` | See [Service notes](#service-notes) |

The service authenticates to Azure with workload identity, which injects `AZURE_CLIENT_ID` and a federated token; there are no client secrets. Per-partition resources (Cosmos DB, Storage, Service Bus) are resolved at request time through the Partition service. The Redis host comes from the Key Vault secret `redis-hostname`, over TLS on port `6380`.

## Test

| Suite | Where | Runs in CI | Run it yourself |
|---|---|---|---|
| Unit | `legal-core`, `provider/legal-azure` | Pull requests (Java Build) | `mvn ... install` from [Build](#build) |
| Acceptance | [`legal-acceptance-test`](legal-acceptance-test/README.md) | Pull requests, against SPI Stack (Deploy and Test) | `spi test legal` |
| Integration | `testing/legal-test-azure` | No | See below |

CI runs these on pull requests from this repository that change code. Documentation-only changes skip the build, and pull requests from forks build without deploying.

**Acceptance** proves a change on real infrastructure before it merges. It calls the deployed service through the gateway as a privileged test identity, and the bindings in `.spi/service.yaml` supply its host, partition, and token. Against an environment you are connected to:

```bash
spi test legal                   # the image and suite the environment is running
spi test legal --source .        # this checkout's suite and descriptor
```

**Integration** is the older Azure suite carried from upstream. It sits outside the root Maven build and expects a client secret for a test service principal and a Service Bus connection string, neither of which SPI Stack issues, so it does not run against SPI Stack today. Acceptance covers the same API surface.

To call the API by hand, `spi token` mints a bearer token:

```bash
curl -H "Authorization: Bearer $(spi token)" -H "data-partition-id: <partition>" \
  https://<gateway>/api/legal/v1/legaltags
```

## Deploy

For a pull request from this repository that changes code, CI publishes the service image and its test suite image, `osdu-spi-legal-acceptance`, to GHCR, and the Deploy and Test lane borrows an SPI Stack environment, runs the new image there, proves it with the acceptance suite, and restores the environment's own image. When that lane runs and passes, the change is proven on real infrastructure before it merges; the Validation Summary on the pull request shows whether it ran. This repository does not own infrastructure; SPI Stack does.

To try a build by hand on an environment you are connected to, pin it by digest and release the pin when done:

```bash
spi service pin legal --image ghcr.io/azure/osdu-spi-legal@sha256:<digest>
spi service reset legal
```

## Service notes

**Java 17 reflection.** Legal publishes tag status changes with Gson, which serializes an enum field by reflection. Java 17 blocks that unless `java.lang` is opened, so without `JDK_JAVA_OPTIONS` above every publish fails. Pass the same flag when running the jar yourself.

**Free-text query.** `provider/legal-azure/src/main/resources/application.properties` turns off free-text search across all legal tag fields. The core default and the acceptance suite expect it on, so SPI Stack sets the feature flag back to `true`.

## License

Copyright © Microsoft Corporation

Licensed under the [Apache License 2.0](LICENSE).
