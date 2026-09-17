package org.opengroup.osdu.legal.countries;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class Country {
    private final String name;
    private final String alpha2;
    private final int numeric;
    private final String residencyRisk;
    private final List<String> typesNotApplyDataResidency;

    public List<String> getTypesNotApplyDataResidency() {
        return this.typesNotApplyDataResidency == null ? new ArrayList<>() : this.typesNotApplyDataResidency;
    }

    public static class RESIDENCY_RISK {
        public static final String NO_RESTRICTION = "No restriction";
        public static final String NOT_ASSIGNED = "Not assigned";
        public static final String EMBARGOED = "Embargoed";
        public static final String DEFAULT = "Default";
        public static final String CLIENT_CONSENT_REQUIRED = "Client consent required";
        
        private RESIDENCY_RISK() {}
    }

    public boolean isMatchByAlpha2(Country other) {
        return this.getAlpha2().equalsIgnoreCase(other.getAlpha2());
    }
}
