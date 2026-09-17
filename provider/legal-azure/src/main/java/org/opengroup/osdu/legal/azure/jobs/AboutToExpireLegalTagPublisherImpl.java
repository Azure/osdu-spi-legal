package org.opengroup.osdu.legal.azure.jobs;

import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.legal.provider.interfaces.IAboutToExpireLegalTagPublisher;
import org.opengroup.osdu.legal.jobs.models.AboutToExpireLegalTags;

import org.springframework.stereotype.Service;

@Service
public class AboutToExpireLegalTagPublisherImpl extends AbstractPublisher implements IAboutToExpireLegalTagPublisher {
    protected final static String LEGAL_TAGS_CHANGE_EVENT_SUBJECT = "legaltagclosetoexpiretopic";
    protected final static String LEGAL_TAGS_CHANGE_EVENT_TYPE = "legaltagclosetoexpiretopic";
    protected final static String LEGAL_TAGS_CHANGE_EVENT_DATA_VERSION = "1.0";
    protected final static String LEGAL_TAGS_CHANGE_EVENT_DEBUG_MESSAGE = "Legal publishes about to expire tag event";

    public AboutToExpireLegalTagPublisherImpl() {
        super(LEGAL_TAGS_CHANGE_EVENT_SUBJECT, LEGAL_TAGS_CHANGE_EVENT_TYPE, LEGAL_TAGS_CHANGE_EVENT_DATA_VERSION, LEGAL_TAGS_CHANGE_EVENT_DEBUG_MESSAGE);
    }

    @Override
    public void publish(String projectId, DpsHeaders headers, AboutToExpireLegalTags aboutToExpireLegalTags) {
        publishToServiceBus(headers, aboutToExpireLegalTags);
        publishToEventGrid(headers, aboutToExpireLegalTags);
    }
}
