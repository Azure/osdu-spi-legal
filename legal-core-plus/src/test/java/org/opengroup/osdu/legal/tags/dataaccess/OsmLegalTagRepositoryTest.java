/*
 *  Copyright 2026 EPAM Systems, Inc
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.opengroup.osdu.legal.tags.dataaccess;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.opengroup.osdu.core.common.model.legal.LegalTag;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.osm.core.model.Destination;
import org.opengroup.osdu.core.osm.core.service.Context;
import org.opengroup.osdu.core.osm.core.translate.ExceptionClassifier;

public class OsmLegalTagRepositoryTest {

  private Context context;
  private OsmLegalTagRepository repository;

  @Before
  public void setUp() {
    context = mock(Context.class);
    TenantInfo tenantInfo = new TenantInfo();
    tenantInfo.setName("osdu");
    tenantInfo.setDataPartitionId("osdu");
    repository = new OsmLegalTagRepository(context, tenantInfo, mock(ExceptionClassifier.class));
  }

  @Test
  public void get_shouldReadAllIdsInOneLookup() {
    long[] ids = {1L, 2L, 3L};
    when(context.getByIds(eq(LegalTag.class), any(Destination.class), aryEq(ids)))
        .thenReturn(Arrays.asList(legalTag(1L), legalTag(2L)));

    Collection<LegalTag> result = repository.get(ids);

    assertEquals(2, result.size());
    verify(context).getByIds(eq(LegalTag.class), any(Destination.class), aryEq(ids));
    verify(context, never()).getOne(any());
  }

  @Test
  public void get_shouldNotQueryForNullOrEmptyIds() {
    assertTrue(repository.get(null).isEmpty());
    assertTrue(repository.get(new long[0]).isEmpty());
    verifyNoInteractions(context);
  }

  private static LegalTag legalTag(long id) {
    LegalTag legalTag = new LegalTag();
    legalTag.setId(id);
    legalTag.setName("osdu-tag-" + id);
    return legalTag;
  }
}
