// Copyright © Amazon Web Services
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.legal.tags.dataaccess;

import org.opengroup.osdu.core.common.provider.interfaces.ITenantFactory;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagRepository;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagRepositoryFactory;
import org.springframework.stereotype.Repository;

import jakarta.inject.Inject;

@Repository
public class LegalTagRepositoryFactoryImpl implements ILegalTagRepositoryFactory {
    @Inject
    protected ITenantFactory tenantFactory;

    @Inject
    protected ILegalTagRepository legalTagRepository;

    @Override
    public ILegalTagRepository get(String tenantName){
        if(tenantName == null || !tenantFactory.exists(tenantName))
            throw invalidTenantGivenException(tenantName);
        return legalTagRepository;
    }

    protected AppException invalidTenantGivenException(String tenantName){
        return new AppException(403, "Forbidden",
                String.format("Invalid tenant %s", tenantName));
    }
}