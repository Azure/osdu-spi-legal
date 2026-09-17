package org.opengroup.osdu.legal.jobs.models;

import org.opengroup.osdu.core.common.model.legal.StatusChangedTags;

public class LegalTagJobResult {
  public StatusChangedTags statusChangedTags;
  public AboutToExpireLegalTags aboutToExpireLegalTags;

  public LegalTagJobResult(StatusChangedTags statusChangedTags, AboutToExpireLegalTags aboutToExpireLegalTags) {
    this.statusChangedTags = statusChangedTags;
    this.aboutToExpireLegalTags = aboutToExpireLegalTags;
  }
}
