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

package org.opengroup.osdu.legal.azure;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.eventgrid.models.EventGridEvent;
import com.microsoft.azure.servicebus.Message;
import com.microsoft.azure.servicebus.MessageBody;
import com.microsoft.azure.servicebus.TopicClient;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.azure.eventgrid.EventGridTopicStore;
import org.opengroup.osdu.azure.servicebus.ITopicClientFactory;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.legal.azure.di.EventGridConfig;
import org.opengroup.osdu.legal.azure.jobs.AboutToExpireLegalTagPublisherImpl;
import org.opengroup.osdu.legal.jobs.models.AboutToExpireLegalTags;
import retrofit2.Response;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

@RunWith(MockitoJUnitRunner.class)
public class AboutToExpireLegalTagPublisherImplTest {

    private final static String EVENT_SUBJECT = "legaltagclosetoexpiretopic";
    private static final String CORRELATION_ID = "correlation-id";
    private static final String USER_EMAIL = "user@email.com";
    private static final String PARTITION_ID = "partition-id";

    @Mock
    private JaxRsDpsLog logger;

    @Mock
    private ITopicClientFactory topicClientFactory;

    @Mock
    private EventGridTopicStore eventGridTopicStore;

    @Mock
    private EventGridConfig eventGridConfig;

    @Mock
    private TopicClient topicClient;

    @Mock
    private DpsHeaders headers;

    @InjectMocks
    private AboutToExpireLegalTagPublisherImpl sut;

    @Before
    public void init() throws ServiceBusException, InterruptedException {
        doReturn(CORRELATION_ID).when(headers).getCorrelationId();
        doReturn(USER_EMAIL).when(headers).getUserEmail();
        doReturn(PARTITION_ID).when(headers).getPartitionId();
        doReturn(topicClient).when(topicClientFactory).getClient(eq(PARTITION_ID), any());
    }

    @Test
    public void shouldPublishToServiceBus() throws Exception {
        AboutToExpireLegalTags aboutToExpireLegalTags = new AboutToExpireLegalTags();

        ArgumentCaptor<Message> messageArgumentCaptor = ArgumentCaptor.forClass(Message.class);
        ArgumentCaptor<String> debugLogArgumentCaptor = ArgumentCaptor.forClass(String.class);

        sut.publish("project-id", headers, aboutToExpireLegalTags);

        verify(logger).debug(debugLogArgumentCaptor.capture());
        verify(topicClient).send(messageArgumentCaptor.capture());

        Map<String, Object> properties = messageArgumentCaptor.getValue().getProperties();
        MessageBody messageBody = messageArgumentCaptor.getValue().getMessageBody();
        Gson gson = new Gson();
        String messageKey = "message";
        String dataKey = "data";
        JsonObject jsonObjectMessage = gson.fromJson(new String(messageBody.getBinaryData().get(0)), JsonObject.class);
        JsonObject jsonObject = (JsonObject) jsonObjectMessage.get(messageKey);

        assertEquals("Legal publishes message " + CORRELATION_ID, debugLogArgumentCaptor.getValue());
        assertEquals(PARTITION_ID, properties.get(DpsHeaders.DATA_PARTITION_ID));
        assertEquals(CORRELATION_ID, properties.get(DpsHeaders.CORRELATION_ID));
        assertEquals(USER_EMAIL, properties.get(DpsHeaders.USER_EMAIL));
        assertEquals(PARTITION_ID, jsonObject.get(DpsHeaders.DATA_PARTITION_ID).getAsString());
        assertEquals(CORRELATION_ID, jsonObject.get(DpsHeaders.CORRELATION_ID).getAsString());
        assertEquals(USER_EMAIL, jsonObject.get(DpsHeaders.USER_EMAIL).getAsString());
        assertEquals(gson.toJsonTree(aboutToExpireLegalTags), jsonObject.get(dataKey));
    }

    @Test
    public void shouldLogError_whenPublishToServiceBusFails() throws Exception {
        AboutToExpireLegalTags aboutToExpireLegalTags = new AboutToExpireLegalTags();

        ArgumentCaptor<Message> messageArgumentCaptor = ArgumentCaptor.forClass(Message.class);
        ArgumentCaptor<String> debugLogArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Exception> exceptionArgumentCaptor = ArgumentCaptor.forClass(Exception.class);
        ArgumentCaptor<String> errorLogArgumentCaptor = ArgumentCaptor.forClass(String.class);

        var serviceBusExceptionMessage = "com.microsoft.azure.servicebus.amqp.AmqpException: The connection was inactive for more than the allowed 300000 milliseconds";
        ServiceBusException serviceBusException = new ServiceBusException(true, serviceBusExceptionMessage);
        doThrow(serviceBusException).when(topicClient).send(any());

        sut.publish("project-id", headers, aboutToExpireLegalTags);

        verify(logger).debug(debugLogArgumentCaptor.capture());
        verify(topicClient).send(messageArgumentCaptor.capture());
        verify(logger).error(errorLogArgumentCaptor.capture(), exceptionArgumentCaptor.capture());

        Map<String, Object> properties = messageArgumentCaptor.getValue().getProperties();
        MessageBody messageBody = messageArgumentCaptor.getValue().getMessageBody();
        Gson gson = new Gson();
        String messageKey = "message";
        String dataKey = "data";
        JsonObject jsonObjectMessage = gson.fromJson(new String(messageBody.getBinaryData().get(0)), JsonObject.class);
        JsonObject jsonObject = (JsonObject) jsonObjectMessage.get(messageKey);

        assertEquals("Legal publishes message " + CORRELATION_ID, debugLogArgumentCaptor.getValue());
        assertEquals(serviceBusExceptionMessage, errorLogArgumentCaptor.getValue());
        assertEquals(serviceBusException, exceptionArgumentCaptor.getValue());
        assertEquals(PARTITION_ID, properties.get(DpsHeaders.DATA_PARTITION_ID));
        assertEquals(CORRELATION_ID, properties.get(DpsHeaders.CORRELATION_ID));
        assertEquals(USER_EMAIL, properties.get(DpsHeaders.USER_EMAIL));
        assertEquals(PARTITION_ID, jsonObject.get(DpsHeaders.DATA_PARTITION_ID).getAsString());
        assertEquals(CORRELATION_ID, jsonObject.get(DpsHeaders.CORRELATION_ID).getAsString());
        assertEquals(USER_EMAIL, jsonObject.get(DpsHeaders.USER_EMAIL).getAsString());
        assertEquals(gson.toJsonTree(aboutToExpireLegalTags), jsonObject.get(dataKey));
    }

    @Test
    public void shouldPublishToEventGrid_whenFlagIsSet() throws ServiceBusException, InterruptedException {
        AboutToExpireLegalTags aboutToExpireLegalTags = new AboutToExpireLegalTags();

        ArgumentCaptor<String> partitionNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> topicNameArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<List<EventGridEvent>> listEventGridEventArgumentCaptor = ArgumentCaptor.forClass(List.class);
        doNothing().when(this.eventGridTopicStore).publishToEventGridTopic(
                partitionNameCaptor.capture(), topicNameArgumentCaptor.capture(), listEventGridEventArgumentCaptor.capture()
        );
        when(this.eventGridConfig.isPublishingToEventGridEnabled()).thenReturn(true);
        when(this.eventGridConfig.getTopicName()).thenReturn(EVENT_SUBJECT);

        sut.publish("project-id", headers, aboutToExpireLegalTags);

        verify(this.eventGridTopicStore, times(1)).publishToEventGridTopic(any(), any(), anyList());

        assertEquals(1, listEventGridEventArgumentCaptor.getValue().size());
        assertEquals(EVENT_SUBJECT, topicNameArgumentCaptor.getValue());
        assertEquals(PARTITION_ID, partitionNameCaptor.getValue());
    }

    @Test
    public void shouldLogError_whenPublishToEventGridFails() {
        AboutToExpireLegalTags aboutToExpireLegalTags = new AboutToExpireLegalTags();

        ArgumentCaptor<String> partitionNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> topicNameArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<List<EventGridEvent>> listEventGridEventArgumentCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Exception> exceptionArgumentCaptor = ArgumentCaptor.forClass(Exception.class);
        ArgumentCaptor<String> errorLogArgumentCaptor = ArgumentCaptor.forClass(String.class);

        when(this.eventGridConfig.isPublishingToEventGridEnabled()).thenReturn(true);
        when(this.eventGridConfig.getTopicName()).thenReturn(EVENT_SUBJECT);

        var cloudExceptionMessage = "Cloud Exception occurred";
        MediaType mediaType = null;
        Response<ResponseBody> errorResponse = Response.error(503, ResponseBody.create(mediaType, "Service Unavailable"));
        CloudException cloudException = new CloudException(cloudExceptionMessage, errorResponse);
        doThrow(cloudException).when(eventGridTopicStore).publishToEventGridTopic(partitionNameCaptor.capture(), topicNameArgumentCaptor.capture(), listEventGridEventArgumentCaptor.capture());

        sut.publish("project-id", headers, aboutToExpireLegalTags);

        verify(this.eventGridTopicStore, times(1)).publishToEventGridTopic(any(), any(), anyList());
        verify(logger).error(errorLogArgumentCaptor.capture(), exceptionArgumentCaptor.capture());

        assertEquals(1, listEventGridEventArgumentCaptor.getValue().size());
        assertEquals(EVENT_SUBJECT, topicNameArgumentCaptor.getValue());
        assertEquals( PARTITION_ID, partitionNameCaptor.getValue());
        assertEquals(cloudExceptionMessage, errorLogArgumentCaptor.getValue());
        assertEquals(cloudException, exceptionArgumentCaptor.getValue());
    }

    @Test
    public void shouldNotPublishToEventGrid_whenFlagStatusChangedTagsIsNull() {
        AboutToExpireLegalTags aboutToExpireLegalTags = null;
        when(this.eventGridConfig.isPublishingToEventGridEnabled()).thenReturn(true);

        sut.publish("project-id", headers, aboutToExpireLegalTags);

        verify(this.eventGridTopicStore, never()).publishToEventGridTopic(any(), any(), anyList());
    }
}