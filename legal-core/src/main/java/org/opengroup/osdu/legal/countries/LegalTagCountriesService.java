package org.opengroup.osdu.legal.countries;

import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.springframework.stereotype.Service;
import org.opengroup.osdu.core.common.model.http.RequestInfo;
import org.opengroup.osdu.core.common.model.legal.ServiceConfig;
import org.springframework.web.context.annotation.RequestScope;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

@Service
@RequestScope
public class LegalTagCountriesService {

    private List<Country> cloudStorageCountries;
    private Map<String, String> validORDCs;

    @Inject
    private LegalTagCountriesTenantRepositories repositories;

    @Inject
    private RequestInfo requestInfo;

    @Inject
    private ServiceConfig serviceConfig;

    @Inject
    private DefaultCountriesRepository defaultCountriesRepository;

    @PostConstruct
    private void setup() {
        TenantInfo tenant = requestInfo.getTenantInfo();
        LegalTagCountriesRepository legalTagCountriesRepository = repositories.get(tenant, serviceConfig.getRegion());
        List<Country> sourceCountries = defaultCountriesRepository.read();
        List<Country> tenantCountries = legalTagCountriesRepository.read();
        List<Country> cloudStorageCountries = this.mergeCountriesRepositories(sourceCountries, tenantCountries);
        this.cloudStorageCountries = Collections.unmodifiableList(cloudStorageCountries);

        Map<String,String> ordcs = new HashMap<>();
        generateORDCsFromRepository(ordcs, sourceCountries);
        generateORDCsFromRepository(ordcs, tenantCountries);
        validORDCs = Collections.unmodifiableMap(ordcs);
    }

    public Map<String, String> getValidCOOs() {
        return generateCOOsFromRepository("none");
    }

    public Map<String, String> getValidCOOs(String dataType) {
        return generateCOOsFromRepository(dataType);
    }

    public Map<String, String> getValidORDCs() {
        return validORDCs;
    }

    private Map<String,String> generateCOOsFromRepository(String dataType) {
        Map<String,String> coos = new HashMap<>();
        for (Country country : cloudStorageCountries) {
            if (country.getResidencyRisk() != null &&
                    (country.getResidencyRisk().equals(Country.RESIDENCY_RISK.NO_RESTRICTION) ||
                            country.getResidencyRisk().equals(Country.RESIDENCY_RISK.NOT_ASSIGNED) ||
                            country.getResidencyRisk().equals(Country.RESIDENCY_RISK.CLIENT_CONSENT_REQUIRED))) {
                coos.put(country.getAlpha2(), country.getName());
            } else if (country.getTypesNotApplyDataResidency().contains(dataType)) {
                coos.put(country.getAlpha2(), country.getName());
            }
        }
        return Collections.unmodifiableMap(coos);
    }

    private List<Country> mergeCountriesRepositories(List<Country> sourceCountries, List<Country> tenantCountries) {
        for (Country tenantCountry : tenantCountries) {
            for (int i = 0; i < sourceCountries.size(); i++) {
                if (sourceCountries.get(i).isMatchByAlpha2(tenantCountry)) {
                    sourceCountries.set(i, tenantCountry);
                }
            }
        }
        return sourceCountries;
    }

    private void generateORDCsFromRepository(Map<String, String> coos, List<Country> countries) {
        for (Country country : countries) {
            if (country.getResidencyRisk() != null && !country.getResidencyRisk().equals(Country.RESIDENCY_RISK.EMBARGOED)) {
                coos.put(country.getAlpha2(), country.getName());
            } else {
                coos.remove(country.getAlpha2());
            }
        }
    }
}
