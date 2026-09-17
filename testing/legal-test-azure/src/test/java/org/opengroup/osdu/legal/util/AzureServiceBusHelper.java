// Copyright © Microsoft Corporation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.legal.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.microsoft.azure.servicebus.*;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class AzureServiceBusHelper {
    public static boolean checkLegalTagNameSent(IMessage message, String name) throws Exception {
        Gson gson = new Gson();
        if(message == null)
            return false;
        String body = new String(message.getBody(), StandardCharsets.UTF_8);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(body);
        String data = root.path("message").path("data").toString();
        return data.equals("{\"statusChangedTags\":[{\"changedTagName\":\"" + name + "\",\"changedTagStatus\":\"incompliant\"}]}");
    }

    public static IMessage getMessage(String connectionString, String topicName, String subscriptionName, int waitSeconds) throws Exception {
        ConnectionStringBuilder connectionStringBuilder =
                new ConnectionStringBuilder(connectionString, topicName+"/subscriptions/" + subscriptionName);
        IMessageReceiver subscriptionClient = ClientFactory.createMessageReceiverFromConnectionStringBuilder(
                connectionStringBuilder, ReceiveMode.PEEKLOCK);
        List<IMessage> messages = new ArrayList<>();

        while (true) {
            IMessage receivedMessage = subscriptionClient.receive(Duration.ofSeconds(waitSeconds));
            if (receivedMessage != null) {
                messages.add(receivedMessage);
                subscriptionClient.complete(receivedMessage.getLockToken());
            } else {
                // No more messages to receive.
                subscriptionClient.close();
                break;
            }
        }
        return messages.size() > 0 ? messages.get(0) : null;
    }
}
