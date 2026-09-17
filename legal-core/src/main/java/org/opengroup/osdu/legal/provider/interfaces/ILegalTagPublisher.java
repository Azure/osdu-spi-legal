package org.opengroup.osdu.legal.provider.interfaces;

import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.legal.StatusChangedTags;

public interface ILegalTagPublisher {

	void publish(String projectId, DpsHeaders headers, StatusChangedTags tags) throws Exception;
}