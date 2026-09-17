package org.opengroup.osdu.legal.countries;

import jakarta.inject.Inject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.legal.provider.interfaces.IStorageReader;
import org.opengroup.osdu.legal.provider.interfaces.IStorageReaderFactory;
import org.springframework.stereotype.Repository;

@Repository
@Log
public class LegalTagCountriesTenantRepositories {

    @Inject
    private IStorageReaderFactory storageReaderFactory;

    private final Map<String, LegalTagCountriesRepository> countriesTenantRepositories = new ConcurrentHashMap<>();

    LegalTagCountriesRepository get(TenantInfo tenant, String projectRegion){
        String tenantName = tenant.getName();
        if(StringUtils.isBlank(tenantName))
            throw invalidTenantGivenException(tenantName);
        if(!countriesTenantRepositories.containsKey(tenantName)){
            addRepository(tenant, projectRegion);
        }
        return countriesTenantRepositories.get(tenantName);
    }

    private void addRepository(TenantInfo tenant, String projectRegion) {
        IStorageReader storageReader = storageReaderFactory.getReader(tenant, projectRegion);
        LegalTagCountriesRepository repo = new LegalTagCountriesRepositoryImpl(storageReader);
        countriesTenantRepositories.put(tenant.getName(), repo);
    }

    private AppException invalidTenantGivenException(String tenantName){
        log.warning(String.format("Requested tenantname '%s' does not exist in list of tenants", tenantName));
        return new AppException(403, "Forbidden", String.format("You do not have access to the %s, value given %s",
                DpsHeaders.DATA_PARTITION_ID, tenantName));
    }
}
