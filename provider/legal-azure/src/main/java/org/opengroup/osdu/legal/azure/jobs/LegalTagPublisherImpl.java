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

import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.legal.StatusChangedTags;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagPublisher;
import org.springframework.stereotype.Component;

@Component
public class LegalTagPublisherImpl extends AbstractPublisher implements ILegalTagPublisher {

    protected final static String LEGAL_TAGS_CHANGE_EVENT_SUBJECT = "legaltagschanged";
    protected final static String LEGAL_TAGS_CHANGE_EVENT_TYPE = "legaltagschanged";
    protected final static String LEGAL_TAGS_CHANGE_EVENT_DATA_VERSION = "1.0";
    protected final static String LEGAL_TAGS_CHANGE_EVENT_DEBUG_MESSAGE = "Legal publishes tag changed event";

    public LegalTagPublisherImpl() {
        super(LEGAL_TAGS_CHANGE_EVENT_SUBJECT, LEGAL_TAGS_CHANGE_EVENT_TYPE, LEGAL_TAGS_CHANGE_EVENT_DATA_VERSION, LEGAL_TAGS_CHANGE_EVENT_DEBUG_MESSAGE);
    }

    @Override
    public void publish(String projectId, DpsHeaders headers, StatusChangedTags tags) {
        publishToServiceBus(headers, tags);
        publishToEventGrid(headers, tags);
    }
}