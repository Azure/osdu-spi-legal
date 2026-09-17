# os-legal-azure

os-legal-azure is a [Spring Boot](https://spring.io/projects/spring-boot) service that hosts CRUD APIs that enable management of legal tags within the OSDU R2 ecosystem.

## Running Locally

### Requirements

In order to run this service locally, you will need the following:

- [Maven 3.8.0+](https://maven.apache.org/download.cgi)
- [Java 17](https://adoptopenjdk.net/)
- Infrastructure dependencies, deployable through the relevant [infrastructure template](https://dev.azure.com/slb-des-ext-collaboration/open-data-ecosystem/_git/infrastructure-templates?path=%2Finfra&version=GBmaster&_a=contents)
- While not a strict dependency, example commands in this document use [bash](https://www.gnu.org/software/bash/)
- Download the [application-insights-agent](https://github.com/microsoft/ApplicationInsights-Java/releases/tag/3.5.2) jar

### General Tips

**Environment Variable Management**
The following tools make environment variable configuration simpler
 - [direnv](https://direnv.net/) - for a shell/terminal environment
 - [EnvFile](https://plugins.jetbrains.com/plugin/7861-envfile) - for [Intellij IDEA](https://www.jetbrains.com/idea/)

**Lombok**
This project uses [Lombok](https://projectlombok.org/) for code generation. You may need to configure your IDE to take advantage of this tool.
 - [Intellij configuration](https://projectlombok.org/setup/intellij)
 - [VSCode configuration](https://projectlombok.org/setup/vscode)


### Environment Variables

In order to run the service locally, you will need to have the following environment variables defined.

**Note** The following command can be useful to pull secrets from keyvault:
```bash
az keyvault secret show --vault-name $KEY_VAULT_NAME --name $KEY_VAULT_SECRET_NAME --query value -otsv
```

**Required to run service**

| name | value | description                                                                                                        | sensitive? | source |
| ---  | ---   |--------------------------------------------------------------------------------------------------------------------|------------| ---    |
| `LOG_PREFIX` | `legal` | Logging prefix                                                                                                     | no         | - |
| `server.servlet.contextPath` | `/api/legal/v1/` | Servlet context path                                                                                               | no         | - |
| `legal_service_region` | `us` | Legal service region                                                                                               | no         | - |
| `entitlements_service_endpoint` | ex `https://foo-entitlements.azurewebsites.net` | Entitlements API endpoint                                                                                          | no         | output of infrastructure deployment |
| `entitlements_service_api_key` | `********` | The API key clients will need to use when calling the service                                                      | yes        | -- |
| `partition_service_endpoint` |  ex `https://foo-partition.azurewebsites.net` | Partition Service API endpoint                                                                                     | no         | output of infrastructure deployment |
| `azure.activedirectory.app-resource-id` | `********` | AAD client application ID                                                                                          | yes        | output of infrastructure deployment |
| `LEGAL_HOSTNAME` | `notused` | Possibly unused                                                                                                    | no         | - |
| `CRON_JOB_IP` | `10.0.0.1` | Possibly unused                                                                                                    | no         | - |
| `azure.activedirectory.session-stateless` | `true` | Flag run in stateless mode (needed by AAD dependency)                                                              | no         | -- |
| `aad_client_id` | `********` | AAD client application ID                                                                                          | yes        | output of infrastructure deployment |
| `azure.activedirectory.AppIdUri` | `api://${azure.activedirectory.client-id}` | URI for AAD Application                                                                                            | no         | -- |
| `cosmosdb_database` | ex `dev-osdu-r2-db` | Cosmos database for legal documents                                                                                | no         | output of infrastructure deployment |
| `azure.storage.container-name` | ex `legal-service-azure-configuration` | Storage container for legal documents                                                                              | no         | output of infrastructure deployment |
| `azure.storage.enable-https` | `true` | Spring configuration for Azure Storage                                                                             | no         | - |
| `servicebus_topic_name` | `legaltags` | Topic for async messaging                                                                                          | no         | output of infrastructure deployment |
| `KEYVAULT_URI` | ex `https://foo-keyvault.vault.azure.net/` | URI of KeyVault that holds application secrets                                                                     | no         | output of infrastructure deployment |
| `AZURE_CLIENT_ID` | `********` | Identity to run the service locally. This enables access to Azure resources. You only need this if running locally | yes        | keyvault secret: `$KEYVAULT_URI/secrets/app-dev-sp-username` |
| `AZURE_TENANT_ID` | `********` | AD tenant to authenticate users from                                                                               | yes        | keyvault secret: `$KEYVAULT_URI/secrets/app-dev-sp-tenant-id` |
| `AZURE_CLIENT_SECRET` | `********` | Secret for `$AZURE_CLIENT_ID`                                                                                      | yes        | keyvault secret: `$KEYVAULT_URI/secrets/app-dev-sp-password` |
| `appinsights_key` | `********` | API Key for App Insights                                                                                           | yes        | output of infrastructure deployment |
| `APPLICATIONINSIGHTS_CONNECTION_STRING` | `InstrumentationKey=********` | Connection String for App Insights. Instrumentation Key value can be obtained from Azure portal                    | yes        | keyvault secret: `$KEYVAULT_URI/secrets/appinsights-connection-string` |
| `azure_istioauth_enabled` | `true` | Flag to Disable AAD auth                                                                                           | no         | -- |

**Required to run integration tests**

| name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |
| `INTEGRATION_TESTER` | `********` | System identity to assume for API calls. Note: this user must have entitlements configured already | no | -- |
| `MY_TENANT` | ex `opendes` | OSDU tenant used for testing | no | -- |
| `AZURE_TESTER_SERVICEPRINCIPAL_SECRET` | `********` | Secret for `$INTEGRATION_TESTER` | yes | -- |
| `AZURE_AD_TENANT_ID` | `********` | AD tenant to authenticate users from | yes | -- |
| `AZURE_AD_APP_RESOURCE_ID` | `********` | AAD client application ID | yes | output of infrastructure deployment |
| `AZURE_LEGAL_STORAGE_ACCOUNT` | same as `storage_account` above | Storage account for integration tests| no | - |
| `AZURE_LEGAL_STORAGE_KEY` | `********` | Storage account key | yes | output of infrastructure deployment |
| `AZURE_LEGAL_SERVICEBUS` | `********` | Servicebus namespace connection string | yes | output of infrastructure deployment |
| `AZURE_LEGAL_TOPICNAME` | `legaltags` | Same as `servicebus_topic_name` above | no | - |
| `HOST_URL` | `http://localhost:8080/` | local service endpoint | no | - |
| `ENTITLEMENT_URL` | Same as `entitlements_service_endpoint` above | Endpoint for entitlements URL | no | - |
| `MY_SECOND_TENANT` | same as `opendes` | OSDU tenant used for testing | no | -- |
| `AZURE_LEGAL_STORAGE_ACCOUNT_SECOND` | same as `storage_account` above | Storage account for integration tests| no | - |


### Configure Maven

Check that maven is installed:
```bash
$ mvn --version
Apache Maven 3.8.0
Maven home: /usr/share/maven
Java version: 17.0.7
...
```

### Application Insights Agent
- [Download the jar from the link locally to your file system](https://github.com/microsoft/ApplicationInsights-Java/releases/tag/3.5.2)

### Build and run the application

After configuring your environment as specified above, you can follow these steps to build and run the application. These steps should be invoked from the *repository root.*

```bash
# build + test + install core service code
$ mvn clean install

# build + test + package azure service code
$ (cd provider/legal-azure/ && mvn clean package)

# run service
#
# Note: this assumes that the environment variables for running the service as outlined
#       above are already exported in your environment.
$ java -jar $(find provider/legal-azure/target/ -name '*-spring-boot.jar') --add-opens java.base/java.lang=ALL-UNNAMED --add-opens  java.base/java.lang.reflect=ALL-UNNAMED -javaagent:<<Absolute file path to application-insights-agent jar>> -DAPPINSIGHTS_LOGGING_ENABLED=true

# Alternately you can run using the Mavan Task
$ mvn spring-boot:run
```

### Test the application

After the service has started it should be accessible via a web browser by visiting [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html). If the request does not fail, you can then run the integration tests.

```bash
# build + install integration test core
$ (cd testing/legal-test-core/ && mvn clean install)

# build + run Azure integration tests.
#
# Note: this assumes that the environment variables for integration tests as outlined
#       above are already exported in your environment.
$ (cd testing/legal-test-azure/ && mvn clean test)
```

## Debugging

Jet Brains - the authors of Intellij IDEA, have written an [excellent guide](https://www.jetbrains.com/help/idea/debugging-your-first-java-application.html) on how to debug java programs.


## Deploying the Service

Service deployments into Azure are standardized to make the process the same for all services if using ADO and are closely related to the infrastructure deployed. The steps to deploy into Azure can be [found here](https://github.com/azure/osdu-infrastructure)

The default ADO pipeline is /devops/azure-pipeline.yml

### Manual Deployment Steps

__Environment Settings__

The following environment variables are necessary to properly deploy a service to an Azure OSDU Environment.

```bash
# Group Level Variables
export AZURE_TENANT_ID=""
export AZURE_SUBSCRIPTION_ID=""
export AZURE_SUBSCRIPTION_NAME=""
export AZURE_PRINCIPAL_ID=""
export AZURE_PRINCIPAL_SECRET=""
export AZURE_APP_ID=""
export AZURE_NO_ACCESS_ID=""
export AZURE_NO_ACCESS_SECRET=""
export AZURE_OTHER_APP_ID=""
export AZURE_BASENAME_21=""
export AZURE_BASENAME=""
export AZURE_BASE=""
export AZURE_INVALID_JWT=""
export AZURE_STORAGE_KEY=""

# Pipeline Level Variable
export AZURE_SERVICE="legal"
export AZURE_BUILD_SUBDIR="provider/legal-azure"
export AZURE_TEST_SUBDIR="testing/legal-test-azure"
export AZURE_OSDU_TENANT="opendes"
export AZURE_OSDU_SECOND_TENANT="opendes2"
export AZURE_SERVICE_BUS_TOPIC="legaltags"
export LEGAL_STORAGE_CONTAINER="legal-service-azure-configuration"


# Required for Azure Deployment
export AZURE_CLIENT_ID="${AZURE_PRINCIPAL_ID}"
export AZURE_CLIENT_SECRET="${AZURE_PRINCIPAL_SECRET}"
export AZURE_RESOURCE_GROUP="${AZURE_BASENAME}-osdu-r2-app-rg"
export AZURE_APPSERVICE_PLAN="${AZURE_BASENAME}-osdu-r2-sp"
export AZURE_APPSERVICE_NAME="${AZURE_BASENAME_21}-au-${AZURE_SERVICE}"

# Required for Testing
export HOST_URL="https://${AZURE_BASENAME_21}-au-legal.azurewebsites.net/"
export ENTITLEMENT_URL="https://${AZURE_BASENAME_21}-au-entitlements.azurewebsites.net/"
export MY_TENANT="${AZURE_OSDU_TENANT}"
export MY_SECOND_TENANT="${AZURE_OSDU_SECOND_TENANT}"
export AZURE_AD_TENANT_ID="${AZURE_TENANT_ID}"
export INTEGRATION_TESTER="${AZURE_PRINCIPAL_ID}"
export AZURE_TESTER_SERVICEPRINCIPAL_SECRET="${AZURE_PRINCIPAL_SECRET}"
export AZURE_AD_APP_RESOURCE_ID="${AZURE_APP_ID}"
export AZURE_LEGAL_STORAGE_ACCOUNT="${AZURE_BASE}sa"
export AZURE_LEGAL_STORAGE_ACCOUNT_SECOND="${AZURE_BASE}sa2"
export AZURE_LEGAL_STORAGE_KEY="${AZURE_STORAGE_KEY}"
export AZURE_LEGAL_SERVICEBUS="Endpoint=sb://${AZURE_BASENAME_21}sb.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=${AZURE_SERVICEBUS_KEY}"
export AZURE_LEGAL_TOPICNAME="${AZURE_SERVICE_BUS_TOPIC}"
```

__Azure Service Deployment__


1. Deploy the service using the Maven Plugin  _(azure_deploy)_

```bash
cd $AZURE_BUILD_SUBDIR
mvn azure-webapp:deploy \
  -DAZURE_TENANT_ID=$AZURE_TENANT_ID \
  -Dazure.appservice.subscription=$AZURE_SUBSCRIPTION_ID \
  -DAZURE_CLIENT_ID=$AZURE_CLIENT_ID \
  -DAZURE_CLIENT_SECRET=$AZURE_CLIENT_SECRET \
  -Dazure.appservice.resourcegroup=$AZURE_RESOURCE_GROUP \
  -Dazure.appservice.plan=$AZURE_APPSERVICE_PLAN \
  -Dazure.appservice.appname=$AZURE_APPSERVICE_NAME
```

2. Configure the Web App to start the SpringBoot Application _(azure_config)_

```bash
az login --service-principal -u $AZURE_CLIENT_ID -p $AZURE_CLIENT_SECRET --tenant $AZURE_TENANT_ID

# Set the JAR FILE as required
TARGET=$(find ./target/ -name '*-spring-boot.jar')
JAR_FILE=${TARGET##*/}

JAVA_COMMAND="java -jar /home/site/wwwroot/${JAR_FILE}"
JSON_TEMPLATE='{"appCommandLine":"%s"}'
JSON_FILE="config.json"
echo $(printf "$JSON_TEMPLATE" "$JAVA_COMMAND") > $JSON_FILE

az webapp config set --resource-group $AZURE_RESOURCE_GROUP --name $AZURE_APPSERVICE_NAME --generic-configurations @$JSON_FILE
```

3. Execute the Integration Tests against the Service Deployment _(azure_test)_

```bash
mvn clean test -f $AZURE_TEST_SUBDIR/pom.xml
```

## License
Copyright © Microsoft Corporation

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
