package org.opengroup.osdu.legal.countries;

import org.opengroup.osdu.core.common.model.legal.CountryCodes;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class PropertiesMock {
        public static Map<String,String> validOrdcs = Arrays.stream(CountryCodes.values()).filter(
            c -> c.getResidencyRisk() != CountryCodes.RESIDENCY_RISK.EMBARGOED)
            .collect(Collectors.toMap(CountryCodes::getAlpha2, CountryCodes::getName));

        public static Map<String,String> coos = Arrays.stream(CountryCodes.values()).filter(
            c -> c.getResidencyRisk() == CountryCodes.RESIDENCY_RISK.NO_RESTRICTION ||
                    c.getResidencyRisk() == CountryCodes.RESIDENCY_RISK.NOT_ASSIGNED ||
                    c.getResidencyRisk() == CountryCodes.RESIDENCY_RISK.CLIENT_CONSENT_REQUIRED)
            .collect(Collectors.toMap(CountryCodes::getAlpha2, CountryCodes::getName));
}
