//  Copyright © Microsoft Corporation
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.

package org.opengroup.osdu.legal.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.BlobUrlParts;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import org.opengroup.osdu.azure.util.AzureServicePrincipal;

public class AzureLegalTagUtils extends LegalTagUtils {
    private static final String FILE_NAME = "Legal_COO.json";
    private static final String CONTAINER_NAME_AZURE = "legal-service-azure-configuration";
    private static String clientSecret = System.getProperty("AZURE_TESTER_SERVICEPRINCIPAL_SECRET", System.getenv("AZURE_TESTER_SERVICEPRINCIPAL_SECRET"));
    private static String clientId = System.getProperty("INTEGRATION_TESTER", System.getenv("INTEGRATION_TESTER"));
    private static String tenantId = System.getProperty("AZURE_AD_TENANT_ID", System.getenv("AZURE_AD_TENANT_ID"));
    private static String storageAccount = System.getProperty("AZURE_LEGAL_STORAGE_ACCOUNT", System.getenv("AZURE_LEGAL_STORAGE_ACCOUNT")).toLowerCase();
    private static String app_resource_id = System.getProperty("AZURE_AD_APP_RESOURCE_ID", System.getenv("AZURE_AD_APP_RESOURCE_ID"));

    @Override
    public synchronized void uploadTenantTestingConfigFile() {
        try {
            String blobPath = generateBlobPath(storageAccount, CONTAINER_NAME_AZURE, FILE_NAME);
            BlobUrlParts parts = BlobUrlParts.parse(blobPath);
            BlobContainerClient blobContainerClient = getBlobContainerClient(parts.getAccountName(), parts.getBlobContainerName());
            if (!blobContainerClient.exists()) {
                createContainer(parts.getBlobContainerName());
            }
            BlockBlobClient blockBlobClient = blobContainerClient.getBlobClient(parts.getBlobName()).getBlockBlobClient();
            if (!blockBlobClient.exists()) {
                String content = readTestFile("TenantConfigTestingPurpose.json");
                try (ByteArrayInputStream dataStream = new ByteArrayInputStream(content.getBytes())) {
                    blockBlobClient.upload(dataStream, content.length());
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new AssertionError(String.format("Error: Could not create test %s file blob", parts.getBlobName()), e);
                }
            }
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

    @Override
    public List<String> readCOOCountries(String storageAccount, String defaultCOOFileName) throws IOException {

        List<String> countries = new ArrayList<>();

        String blobPath = String.format("https://%s.blob.core.windows.net/%s/%s", storageAccount.toLowerCase(), CONTAINER_NAME_AZURE, FILE_NAME);
        BlobUrlParts parts = BlobUrlParts.parse(blobPath);
        ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
                .clientSecret(clientSecret)
                .clientId(clientId)
                .tenantId(tenantId)
                .build();
        BlobContainerClient blobContainerClient = new BlobContainerClientBuilder()
                .endpoint(String.format("https://%s.blob.core.windows.net", parts.getAccountName()))
                .credential(clientSecretCredential)
                .containerName(parts.getBlobContainerName())
                .buildClient();

        BlockBlobClient blockBlobClient = blobContainerClient.getBlobClient(parts.getBlobName()).getBlockBlobClient();
        if (blobContainerClient.exists()) {
            try {
                if (blockBlobClient.exists()) {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    blockBlobClient.download(outputStream);
                    ByteArrayInputStream stream = new ByteArrayInputStream(outputStream.toByteArray());
                    int byteData;
                    StringBuffer sb = new StringBuffer();
                    while ((byteData = stream.read()) != -1) {
                        sb.append((char) byteData);
                    }

                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode root = objectMapper.readTree(sb.toString());

                    if(root.isArray()) {
                        for (JsonNode node : root) {
                            countries.add(node.path("alpha2").toString().replaceAll("\"", ""));
                        }
                    }
                    stream.close();
                } else {
                    String content = readTestFile(defaultCOOFileName);
                    ByteArrayInputStream newStream = new ByteArrayInputStream(content.getBytes());
                    newStream.reset();
                    blockBlobClient.upload(newStream, content.length(), true);

                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode root = objectMapper.readTree(content);

                    if(root.isArray()) {
                        for (JsonNode node : root) {
                            countries.add(node.path("alpha2").toString().replaceAll("\"", ""));
                        }
                    }
                }
            } catch (Exception e) {
                throw new AssertionError(String.format("Error: Could not create test %s file blob", parts.getBlobName()), e);
            }
        }

        return countries;
    }

    private static String generateContainerPath(String accountName, String containerName) {
        return String.format("https://%s.blob.core.windows.net/%s", accountName, containerName);
    }

    public void createContainer(String containerName)
    {
        String containerPath = generateContainerPath(storageAccount, containerName);
        BlobUrlParts parts = BlobUrlParts.parse(containerPath);
        BlobContainerClient blobContainerClient = getBlobContainerClient(parts.getAccountName(), parts.getBlobContainerName());
        if(!blobContainerClient.exists()){
            blobContainerClient.create();

        }
    }

    private static String generateBlobPath(String accountName, String containerName, String blobName) {
        return String.format("https://%s.blob.core.windows.net/%s/%s", accountName, containerName, blobName);
    }

    private BlobContainerClient getBlobContainerClient(String accountName, String containerName) {
        ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
                .clientSecret(clientSecret)
                .clientId(clientId)
                .tenantId(tenantId)
                .build();
        BlobContainerClient blobContainerClient = new BlobContainerClientBuilder()
                .endpoint(getBlobAccountUrl(accountName))
                .credential(clientSecretCredential)
                .containerName(containerName)
                .buildClient();
        return blobContainerClient;
    }

    private static String getBlobAccountUrl(String accountName) {
        return String.format("https://%s.blob.core.windows.net", accountName);
    }

    @Override
    public synchronized String accessToken() throws Exception {
        if (Strings.isNullOrEmpty(token)) {
            token = new AzureServicePrincipal().getIdToken(clientId, clientSecret, tenantId, app_resource_id);
        }
        return "Bearer " + token;
    }

    public static String getAzureServiceBusConnectionString() {
		return System.getProperty("AZURE_LEGAL_SERVICEBUS", System.getenv("AZURE_LEGAL_SERVICEBUS"));
    }
    
	public static String getAzureServiceBusTopicName() {
	    return System.getProperty("AZURE_LEGAL_TOPICNAME", System.getenv("AZURE_LEGAL_TOPICNAME"));
    }
}
