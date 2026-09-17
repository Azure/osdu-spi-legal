package org.opengroup.osdu.legal.provider.interfaces;

public interface ILegalTagRepositoryFactory {

    ILegalTagRepository get(String tenantName);
}
