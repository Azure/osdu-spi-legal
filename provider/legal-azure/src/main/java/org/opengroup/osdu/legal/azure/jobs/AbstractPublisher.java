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

package org.opengroup.osdu.legal.azure.jobs;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.microsoft.azure.eventgrid.models.EventGridEvent;
import com.microsoft.azure.servicebus.Message;
import org.joda.time.DateTime;
import org.opengroup.osdu.azure.eventgrid.EventGridTopicStore;
import org.opengroup.osdu.azure.servicebus.ITopicClientFactory;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.legal.azure.di.EventGridConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

@Component
abstract public class AbstractPublisher {
    private final String eventSubject;
    private final String eventType;
    private final String eventDataVersion;
    private final String eventDebugMessage;

    @Autowired
    private EventGridConfig eventGridConfig;

    @Autowired
    private EventGridTopicStore eventGridTopicStore;

    @Inject
    private ITopicClientFactory topicClientFactory;

    @Inject
    private JaxRsDpsLog logger;

    @Inject
    @Named("SERVICE_BUS_TOPIC")
    private String serviceBusTopic;

    AbstractPublisher(String subject, String type, String dataVersion, String debugMessage) {
        eventSubject = subject;
        eventType = type;
        eventDataVersion = dataVersion;
        eventDebugMessage = debugMessage;
    }

    protected void publishToServiceBus(DpsHeaders headers, Object tags) {
        Message message = createMessage(headers, tags);
        try {
            logger.debug("Legal publishes message " + headers.getCorrelationId());
            topicClientFactory.getClient(headers.getPartitionId(), serviceBusTopic).send(message);
        } catch (Exception exception) {
            logger.error(exception.getMessage(), exception);
        }
    }

    protected void publishToEventGrid(DpsHeaders headers, Object tags) {
        if (eventGridConfig.isPublishingToEventGridEnabled() && tags != null) {
            HashMap<String, Object> data = new HashMap<>();
            List<EventGridEvent> eventsList = new ArrayList<>();
            data.put("data", tags);
            data.put(DpsHeaders.DATA_PARTITION_ID, headers.getPartitionId());
            data.put(DpsHeaders.CORRELATION_ID, headers.getCorrelationId());
            data.put(DpsHeaders.USER_EMAIL, headers.getUserEmail());
            String messageId = UUID.randomUUID().toString();

            try {
                eventsList.add(new EventGridEvent(
                        messageId,
                        eventSubject,
                        data,
                        eventType,
                        DateTime.now(),
                        eventDataVersion
                ));
                logger.debug(eventDebugMessage + ": " + data.get(DpsHeaders.CORRELATION_ID));
                eventGridTopicStore.publishToEventGridTopic(headers.getPartitionId(), eventGridConfig.getTopicName(), eventsList);
            } catch (Exception exception) {
                logger.error(exception.getMessage(), exception);
            }
        }
    }

    private Message createMessage(DpsHeaders headers, Object tags) {
        Gson gson = new Gson();
        Message message = new Message();
        Map<String, Object> properties = new HashMap<>();

        // properties
        properties.put(DpsHeaders.DATA_PARTITION_ID, headers.getPartitionId());
        headers.addCorrelationIdIfMissing();
        properties.put(DpsHeaders.CORRELATION_ID, headers.getCorrelationId());
        properties.put(DpsHeaders.USER_EMAIL, headers.getUserEmail());

        message.setProperties(properties);

        // add all to body {"message": {"data":[], "id":...}}
        JsonObject jo = new JsonObject();
        jo.add("data", gson.toJsonTree(tags));
        jo.addProperty(DpsHeaders.DATA_PARTITION_ID, headers.getPartitionId());
        jo.addProperty(DpsHeaders.CORRELATION_ID, headers.getCorrelationId());
        jo.addProperty(DpsHeaders.USER_EMAIL, headers.getUserEmail());
        JsonObject jomsg = new JsonObject();
        jomsg.add("message", jo);

        message.setBody(jomsg.toString().getBytes(StandardCharsets.UTF_8));
        message.setContentType("application/json");

        return message;
    }
}
