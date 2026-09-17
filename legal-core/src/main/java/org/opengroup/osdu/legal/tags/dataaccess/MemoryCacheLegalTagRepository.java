package org.opengroup.osdu.legal.tags.dataaccess;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.opengroup.osdu.core.common.model.legal.ListLegalTagArgs;
import org.opengroup.osdu.core.common.model.legal.PersistenceException;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagRepository;
import org.opengroup.osdu.core.common.model.legal.LegalTag;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MemoryCacheLegalTagRepository extends LegalTagRepositoryWrapper {

    private final Cache<String, LegalTag> cache = CacheBuilder.newBuilder()
            .expireAfterAccess(15, TimeUnit.MINUTES)
            .maximumSize(500)
            .build();
    private final String tenantName;

    public MemoryCacheLegalTagRepository(ILegalTagRepository wrapped, String tenantName){
        super(wrapped);
        if(StringUtils.isBlank(tenantName))
            throw new IllegalArgumentException("tenantName must be supplied");
        this.tenantName = tenantName;
    }

    @Override
    public Long create(LegalTag legalTag) throws PersistenceException {
        Long output = super.create(legalTag);
        addLegalTagToCache(legalTag);
        return output;
    }
    @Override
    public Collection<LegalTag> get(long[] ids){
        List<LegalTag> output = new ArrayList<>();
        List<Long> idsNotInCache = getTagsFromCache(ids, output);
        getTagsFromStorage(output, idsNotInCache);
        return output;
    }
    @Override
    public Boolean delete(LegalTag legalTag) {
        Boolean output = super.delete(legalTag);
        if(output) {
            invalidateCache(legalTag);
        }
        return output;
    }
    @Override
    public LegalTag update(LegalTag newLegalTag){
        LegalTag output = super.update(newLegalTag);
        if(output != null) {
            invalidateCache(newLegalTag);
        }
        return output;
    }
    @Override
    public Collection<LegalTag> list(ListLegalTagArgs args){
        return super.list(args);
    }

    private void getTagsFromStorage(List<LegalTag> legalTags, List<Long> idsNotInCache) {
        if(idsNotInCache.size() > 0) {
            long[] castArray = idsNotInCache.stream().mapToLong(l -> l).toArray();
            Collection<LegalTag> tags = super.get(castArray);
            if (tags != null) {
                for (LegalTag tag : tags) {
                    addLegalTagToCache(tag);
                    legalTags.add(tag);
                }
            }
        }
    }
    private List<Long> getTagsFromCache(long[] ids, List<LegalTag> output) {
        List<Long> idsNotInCache = new ArrayList<>();
        for(long id : ids) {
            LegalTag tag = getLegalTagFromCache(id);
            if(tag != null){
                output.add(tag);
            }
            else{
                idsNotInCache.add(id);
            }
        }
        return idsNotInCache;
    }
    private void addLegalTagToCache(LegalTag legalTag) {
        cache.put(createKey(legalTag.getId()), legalTag);
    }
    private LegalTag getLegalTagFromCache(Long id) {
        return cache.getIfPresent(createKey(id));
    }
    private void invalidateCache(LegalTag legalTag) {
        cache.invalidate(createKey(legalTag.getId()));
    }
    private String createKey(Long id) {
        return String.format("lt.%s.%s", id.toString(), tenantName);
    }
}
