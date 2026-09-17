package org.opengroup.osdu.legal.azure.di;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventGridConfig {
    private final boolean publishToEventGridEnabled;

    // The Event Grid Event can be a maximum of 1MB. The batch size manipulation will impact the costing.
    private final Integer eventGridBatchSize;

    private final String topicName;

    public EventGridConfig(@Value("#{new Boolean('${azure.publishToEventGrid}')}") boolean publish,
                           @Value("#{new Integer('${azure.eventGridBatchSize}')}") int batchSize,
                           @Value("${azure.eventGrid.topicName}") String topicName) {
        if (publish) {
            if ((topicName.isEmpty() || batchSize <= 0)) {
                throw new RuntimeException("Missing EventGrid Configuration");
            }
        }
        this.publishToEventGridEnabled = publish;
        this.eventGridBatchSize = batchSize;
        this.topicName = topicName;
    }

    public boolean isPublishingToEventGridEnabled() {
        return publishToEventGridEnabled;
    }

    public String getTopicName() {
        return topicName;
    }

    public int getEventGridBatchSize() {
        return eventGridBatchSize;
    }
}
