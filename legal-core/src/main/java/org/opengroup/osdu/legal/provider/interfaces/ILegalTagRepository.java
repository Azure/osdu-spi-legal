package org.opengroup.osdu.legal.provider.interfaces;

import org.opengroup.osdu.core.common.model.legal.ListLegalTagArgs;
import org.opengroup.osdu.core.common.model.legal.LegalTag;

import java.util.Collection;

public interface ILegalTagRepository {
    String LEGAL_TAGS_ENTITYNAME = "LegalTag";
    String LEGAL_TAGS_HISTORIC_ENTITYNAME = "LegalTagHistoric";
    String COUNTRY_OF_ORIGIN = "COO";
    String CONTRACT_ID = "contractId";
    String CREATION_DT = "created";
    String IS_VALID = "isValid";
    String EXPIRATION_DATE = "expirationDate";
    String ORIGINATOR = "originator";
    String DATA_TYPE = "dataType";
    String SECURITY_CLASSIFICATION = "securityClassification";
    String EXPORT_CLASSIFICATION = "exportClassification";
    String PERSONAL_DATA = "personalData";
    String EXTENSION_PROPERTIES = "extensionProperties";
    String DESCRIPTION = "description";
    String NAME = "name";

    Long create(LegalTag legalTag);
    Collection<LegalTag> get(long[] ids);
    Boolean delete(LegalTag legalTag);
    LegalTag update(LegalTag newLegalTag);
    Collection<LegalTag> list(ListLegalTagArgs args);
}
