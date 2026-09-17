package org.opengroup.osdu.legal.jobs;

import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.legal.StatusChangedTag;
import org.opengroup.osdu.core.common.model.legal.StatusChangedTags;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagPublisher;
import org.opengroup.osdu.legal.provider.interfaces.IAboutToExpireLegalTagPublisher;
import org.opengroup.osdu.legal.tags.LegalTagConstraintValidator;
import org.opengroup.osdu.legal.tags.LegalTagService;
import org.opengroup.osdu.core.common.model.legal.LegalTag;
import org.opengroup.osdu.core.common.model.legal.Properties;
import org.opengroup.osdu.legal.jobs.models.LegalTagJobResult;
import org.opengroup.osdu.legal.jobs.models.AboutToExpireLegalTags;
import org.opengroup.osdu.legal.jobs.models.AboutToExpireLegalTag;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.legal.FeatureFlagController;
import org.opengroup.osdu.legal.config.LegalTagConfig;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

import jakarta.inject.Inject;

@Component
public class LegalTagStatusJob {
    @Inject
    private LegalTagConstraintValidator validator;
    @Inject
    private LegalTagService legalTagService;
    @Inject
    private ILegalTagPublisher legalTagPublisher;
    @Inject
    private IAboutToExpireLegalTagPublisher aboutToExpireLegalTagPublisher;
    @Inject
    private JaxRsDpsLog log;
    @Inject
    private FeatureFlagController featureFlagController;

    @Autowired
    private Clock clock;

    @Autowired
    private LegalTagConfig config;

    public LegalTagJobResult run(String projectId, DpsHeaders headers, String tenantName) throws Exception {
        LegalTagJobResult legalTagJobResult = new LegalTagJobResult(new StatusChangedTags(), new AboutToExpireLegalTags());
        validator.setHeaders(headers);
        legalTagJobResult = checkAndUpdateLegalTagStatus(true, tenantName, legalTagJobResult);
        legalTagJobResult = checkAndUpdateLegalTagStatus(false, tenantName, legalTagJobResult);

        publishLegalTagStatusUpdateEvents(!legalTagJobResult.statusChangedTags.getStatusChangedTags().isEmpty(), projectId, headers, legalTagJobResult.statusChangedTags);
        if (featureFlagController.isAboutToExpireFeatureFlagEnabled()) {
            publishAboutToExpireLegalTagEvents(!legalTagJobResult.aboutToExpireLegalTags.getLegalTags().isEmpty(), projectId, headers, legalTagJobResult.aboutToExpireLegalTags);
        }

        return legalTagJobResult;
    }

    private LegalTagJobResult checkAndUpdateLegalTagStatus(Boolean isCurrentlyValid, String tenantName, LegalTagJobResult legalTagJobResult) {
        Collection<LegalTag> validLegalTags = legalTagService.listLegalTag(isCurrentlyValid, tenantName);
        for (LegalTag tag : validLegalTags) {
            String errors = validator.getErrors(tag);
            Boolean hasErrors = errors != null;

            if (featureFlagController.isAboutToExpireFeatureFlagEnabled() && isCurrentlyValid) {
                checkAboutToExpireLegalTag(tag, legalTagJobResult.aboutToExpireLegalTags);
            }

            if (isCurrentlyValid.equals(hasErrors)) {
                log.info(String.format("Changing state of %s from %s to %s. Errors found in legaltag %s", tag.getName(),
                        isCurrentlyValid ? "Valid" : "Invalid",
                        hasErrors ? "Invalid" : "Valid", errors));
                legalTagJobResult.statusChangedTags.getStatusChangedTags().add(new StatusChangedTag(tag.getName(), hasErrors ? LegalTagCompliance.incompliant : LegalTagCompliance.compliant));
                legalTagService.updateStatus(tag.getName(), !isCurrentlyValid, tenantName);
            }
        }
        return legalTagJobResult;
    }

    private void checkAboutToExpireLegalTag(LegalTag tag, AboutToExpireLegalTags aboutToExpireLegalTags) {
        Properties properties = tag.getProperties();
        Date expirationDate = properties.getExpirationDate();
        List<LocalDate> aboutToExpireDates = getAboutToExpireDates(expirationDate);
        LocalDate today = LocalDate.now(clock);

        for (LocalDate aboutToExpireDate : aboutToExpireDates) {
            // when cron runs less frequent than once per a day it could not match when legal tag be about to expire!
            Boolean isAboutToExpire = aboutToExpireDate.equals(today);
            if (isAboutToExpire) {
                log.info(String.format("Found legal tag about to expire: %s at %s", tag.getName(), expirationDate));
                aboutToExpireLegalTags.getLegalTags().add(new AboutToExpireLegalTag(tag.getName(), properties.getExpirationDate()));
                return;
            }
        }
    }

    private List<LocalDate> getAboutToExpireDates(Date expirationDate) throws AppException {
        ArrayList aboutToExpireDates = new ArrayList<>();

        List<String> expirationAlertsList = Arrays.asList(config.getExpirationAlerts().split(","));
        for (String expiration : expirationAlertsList) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(expirationDate);

            try {
                if (expiration.endsWith("d")) {
                    int numberOfDays = Integer.parseInt(expiration.replace("d", ""));
                    cal.add(Calendar.DAY_OF_YEAR, - numberOfDays);
                } else if (expiration.endsWith("w")) {
                    int numberOfWeeks = Integer.parseInt(expiration.replace("w", ""));
                    cal.add(Calendar.DAY_OF_YEAR, - 7 * numberOfWeeks);
                } else if (expiration.endsWith("m")) {
                    int numberOfMonths = Integer.parseInt(expiration.replace("m", ""));
                    cal.add(Calendar.MONTH, - numberOfMonths);                
                } else if (expiration.endsWith("y")) {
                    int numberOfYears = Integer.parseInt(expiration.replace("y", ""));
                    cal.add(Calendar.YEAR, - numberOfYears);
                } else {
                    log.error(String.format("Invalid legal tag about to expire time value: %s", expiration));
                    throw new AppException(500, "Server error", String.format("Invalid legal tag about to expire time value: %s", expiration));
                }
            } catch (NumberFormatException e) {
                log.error(String.format("Invalid legal tag about to expire time value: %s", expiration));
                throw new AppException(500, "Server error", String.format("Invalid legal tag about to expire time value: %s", expiration));
            }
            
            aboutToExpireDates.add(cal.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        }

        return aboutToExpireDates;
    }

    private void publishLegalTagStatusUpdateEvents(boolean hasStatusChanges, String projectId, DpsHeaders headers, StatusChangedTags statusChangedTags) throws Exception {
        if (hasStatusChanges) {
            legalTagPublisher.publish(projectId, headers, statusChangedTags);
        }
    }

    private void publishAboutToExpireLegalTagEvents(boolean hasTags, String projectId, DpsHeaders headers, AboutToExpireLegalTags aboutToExpireLegalTags) throws Exception {
        if (hasTags) {
            aboutToExpireLegalTagPublisher.publish(projectId, headers, aboutToExpireLegalTags);
        }
    }
}
