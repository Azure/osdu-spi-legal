package org.opengroup.osdu.legal.provider.interfaces;

import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.legal.LegalTag;
import org.opengroup.osdu.legal.jobs.models.AboutToExpireLegalTags;

import org.opengroup.osdu.legal.jobs.models.AboutToExpireLegalTags;

import java.util.ArrayList;


public interface IAboutToExpireLegalTagPublisher {

	void publish(String projectId, DpsHeaders headers, AboutToExpireLegalTags tags) throws Exception;
}
