# OSDU Legal Service

Official documentation is located at [https://osdu.pages.opengroup.org/platform/security-and-compliance/legal/](https://osdu.pages.opengroup.org/platform/security-and-compliance/legal/)

## Prerequisites

- [JDK 25](https://adoptium.net/)
- [Maven 3.9.16+](https://maven.apache.org/download.cgi)

Community / core-plus modules compile and run on Java 25. For core-plus local setup, see [legal-core-plus/README.md](./legal-core-plus/README.md).

## os-legal-aws

The AWS provider has been removed from this repository.

## os-legal-gcp

The GCP provider has been moved from this repository to [here](https://community.opengroup.org/osdu/platform/deployment-and-operations/base-containers-gcp/gc-osdu-services).

## Running integration tests
Integration tests are located in a separate project for each cloud in the ```testing``` directory under the project root directory.

### Open API 3.0 - Swagger
- Swagger UI : https://host/context-path/swagger (will redirect to https://host/context-path/swagger-ui/index.html)
- api-docs (JSON) : https://host/context-path/api-docs
- api-docs (YAML) : https://host/context-path/api-docs.yaml

All the Swagger and OpenAPI related common properties are managed here [swagger.properties](./legal-core/src/main/resources/swagger.properties)

#### Server Url(full path vs relative path) configuration
- `api.server.fullUrl.enabled=true` It will generate full server url in the OpenAPI swagger
- `api.server.fullUrl.enabled=false` It will generate only the contextPath only
- default value is false
[Reference]:(https://springdoc.org/faq.html#_how_is_server_url_generated) 

## License
Copyright 2017-2019, Schlumberger

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at 

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
