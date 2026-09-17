package org.opengroup.osdu.legal.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.opengroup.osdu.core.common.model.http.RequestInfo;
import org.opengroup.osdu.core.common.model.legal.AllowedLegaltagPropertyValues;
import org.opengroup.osdu.legal.FeatureFlagController;
import org.opengroup.osdu.legal.api.LegalTagApi;
import org.opengroup.osdu.legal.countries.LegalTagCountriesService;
import org.opengroup.osdu.legal.logging.AuditLogger;
import org.opengroup.osdu.legal.tags.LegalTagService;
import org.opengroup.osdu.legal.tags.dto.InvalidTagsWithReason;
import org.opengroup.osdu.legal.tags.dto.LegalTagDto;
import org.opengroup.osdu.legal.tags.dto.LegalTagDtos;
import org.opengroup.osdu.legal.tags.dto.QueryLegalTag;
import org.opengroup.osdu.legal.tags.dto.ReadablePropertyValues;
import org.opengroup.osdu.legal.tags.dto.RequestLegalTags;
import org.opengroup.osdu.legal.tags.dto.UpdateLegalTag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

import jakarta.inject.Inject;
import jakarta.validation.ValidationException;

@RestController
public class LegalTagController implements LegalTagApi {

  private RequestInfo requestInfo;
  private LegalTagService legalTagService;
  @Inject private AllowedLegaltagPropertyValues allowedLegaltagPropertyValues;
  @Inject private LegalTagCountriesService legalTagCountriesService;
  @Inject private AuditLogger auditLogger;
  @Inject private FeatureFlagController featureFlagController;

  @Inject // injectMock only works on setter DI
  public void setRequestInfo(RequestInfo requestInfo) {
    this.requestInfo = requestInfo;
  }

  @Inject // injectMock only works on setter DI
  public void setLegalTagService(LegalTagService legalTagService) {
    this.legalTagService = legalTagService;
  }

  @Override
  public ResponseEntity<LegalTagDto> createLegalTag(LegalTagDto legalTag) {
    LegalTagDto output = legalTagService.create(legalTag, requestInfo.getTenantInfo().getName());
    return new ResponseEntity<>(output, HttpStatus.CREATED);
  }

  @Override
  public ResponseEntity<LegalTagDto> updateLegalTag(UpdateLegalTag legalTag) {
    LegalTagDto output = legalTagService.update(legalTag, requestInfo.getTenantInfo().getName());
    return new ResponseEntity<>(output, HttpStatus.OK);
  }

  @Override
  public ResponseEntity<LegalTagDtos> listLegalTags(boolean valid) {
    if (requestInfo.getTenantInfo() == null) {
      throw new ValidationException("No tenant supplied");
    }
    LegalTagDtos output = legalTagService.list(valid, requestInfo.getTenantInfo().getName());
    return new ResponseEntity<>(output, HttpStatus.OK);
  }

  @Override
  public ResponseEntity getLegalTag(String name) {
    LegalTagDto output = legalTagService.get(name, requestInfo.getTenantInfo().getName());

    if (output == null) return new ResponseEntity<>(createNotFoundBody(), HttpStatus.NOT_FOUND);
    else {
      return new ResponseEntity<>(output, HttpStatus.OK);
    }
  }

  @Override
  public ResponseEntity<HttpStatus> deleteLegalTag(String name) {
    if (legalTagService.delete(
        requestInfo.getTenantInfo().getProjectId(),
        name,
        requestInfo.getHeaders(),
        requestInfo.getTenantInfo().getName())) return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    else return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Override
  public ResponseEntity<LegalTagDtos> getLegalTags(RequestLegalTags requestedTags) {
    String[] names = requestedTags.getNames().stream().toArray(String[]::new);
    LegalTagDtos result = legalTagService.getBatch(names, requestInfo.getTenantInfo().getName());

    if (result == null || result.getLegalTags().size() != requestedTags.getNames().size()) {
      return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
    } else {
      return new ResponseEntity<>(result, HttpStatus.OK);
    }
  }

  @Override
  public ResponseEntity<InvalidTagsWithReason> validateLegalTags(RequestLegalTags requestedTags) {
    InvalidTagsWithReason result =
        legalTagService.validate(
            requestedTags.getNames().toArray(new String[0]), requestInfo.getTenantInfo().getName());

    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @Override
  public ResponseEntity<ReadablePropertyValues> getLegalTagProperties() {
    ReadablePropertyValues output = new ReadablePropertyValues();
    output.setCountriesOfOrigin(legalTagCountriesService.getValidCOOs());
    output.setOtherRelevantDataCountries(legalTagCountriesService.getValidORDCs());
    output.setExportClassificationControlNumbers(allowedLegaltagPropertyValues.getEccns());
    output.setPersonalDataTypes(allowedLegaltagPropertyValues.getPersonalDataType());
    output.setSecurityClassifications(allowedLegaltagPropertyValues.getSecurityClassifications());
    output.setDataTypes(allowedLegaltagPropertyValues.getDataTypes());
    auditLogger.readLegalPropertiesSuccess(Collections.singletonList(output.toString()));

    return new ResponseEntity<>(output, HttpStatus.OK);
  }

  @Override
  public ResponseEntity<LegalTagDtos> queryLegalTag(QueryLegalTag searchInput, boolean valid) {
    if (requestInfo.getTenantInfo() == null) {
      throw new ValidationException("No tenant supplied");
    }
    if (Boolean.FALSE.equals(featureFlagController.isLegalTagQueryApiFlagEnabled())) {
      return new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED);
    }
    LegalTagDtos output =
            legalTagService.queryLegalTag(searchInput, true, requestInfo.getTenantInfo().getName());
    return new ResponseEntity<>(output, HttpStatus.OK);
  }

  private String createNotFoundBody() {
    final Map<String, String> body = new HashMap<>();
    body.put("error", "Not found.");
    return new Gson().toJson(body);
  }
}
