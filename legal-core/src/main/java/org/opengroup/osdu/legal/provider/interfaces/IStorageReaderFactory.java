package org.opengroup.osdu.legal.provider.interfaces;

import org.opengroup.osdu.core.common.model.tenant.TenantInfo;

public interface IStorageReaderFactory {

	IStorageReader getReader(TenantInfo tenant, String projectRegion);
}
