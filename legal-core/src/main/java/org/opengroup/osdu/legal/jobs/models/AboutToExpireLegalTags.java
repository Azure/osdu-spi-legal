package org.opengroup.osdu.legal.jobs.models;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AboutToExpireLegalTags {
    private List<AboutToExpireLegalTag> legalTags = new ArrayList<>();
}
