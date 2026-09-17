package org.opengroup.osdu.legal.tags.dataaccess;

import org.opengroup.osdu.core.common.model.legal.ListLegalTagArgs;
import org.opengroup.osdu.core.common.model.legal.PersistenceException;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagRepository;
import org.opengroup.osdu.core.common.model.legal.LegalTag;

import java.util.Collection;

public abstract class LegalTagRepositoryWrapper implements ILegalTagRepository {

    private final ILegalTagRepository wrapped;

    public LegalTagRepositoryWrapper(ILegalTagRepository wrapped){
        this.wrapped = wrapped;
    }

    @Override
    public Long create(LegalTag legalTag) throws PersistenceException {
        return wrapped.create(legalTag);
    }

    @Override
    public Collection<LegalTag> get(long[] ids) throws PersistenceException {
        return wrapped.get(ids);
    }
    @Override
    public Boolean delete(LegalTag legalTag) throws PersistenceException {
        return wrapped.delete(legalTag);
    }
    @Override
    public LegalTag update(LegalTag newLegalTag) throws PersistenceException {
        return wrapped.update(newLegalTag);
    }
    @Override
    public Collection<LegalTag> list(ListLegalTagArgs args) throws PersistenceException {
        return wrapped.list(args);
    }
}
