//  Copyright © Microsoft Corporation
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.

package org.opengroup.osdu.legal.azure.tags.dataaccess;

import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.azure.cosmosdb.CosmosStore;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.legal.LegalTag;
import org.opengroup.osdu.core.common.model.legal.ListLegalTagArgs;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LegalTagRepositoryImplTest {

    private static final String dataPartitionId = "data-partition-id";

    @Mock
    private CosmosStore cosmosStore;

    @Mock
    private DpsHeaders headers;

    @InjectMocks
    private LegalTagRepositoryImpl sut;

    @Before
    public void init() {
        lenient().doReturn(dataPartitionId).when(headers).getPartitionId();
    }

    @Test(expected = AppException.class)
    public void testCreateLegalTagAlreadyExisting_throwsException() {
        long id = 1234;
        String strId = String.valueOf(id);
        LegalTag legalTag = getLegalTagWithId(id);
        Optional<LegalTag> optionalLegalTag = Optional.of(legalTag);
        doReturn(optionalLegalTag).when(cosmosStore).findItem(eq(dataPartitionId), any(), any(), eq(strId), eq(strId), any());
        try {
            sut.create(legalTag);
        } catch (AppException e) {
            int errorCode = 409;
            String errorMessage = "LegalTag already exists";
            validateAppException(e, errorCode, errorMessage);
            throw (e);
        }
    }

    @Test
    public void testCreateLegalTag_upsertItem_executesCorrectQuery() {
        long id = 1234;
        String strId = String.valueOf(id);
        LegalTag legalTag = getLegalTagWithId(id);
        long obtainedId = sut.create(legalTag);

        ArgumentCaptor<LegalTagDoc> arg = ArgumentCaptor.forClass(LegalTagDoc.class);
        verify(cosmosStore).createItem(eq(dataPartitionId), any(), any(), any(), arg.capture());

        assertEquals(arg.getValue().getId(), strId);
        assertEquals(obtainedId, id);
    }

    @Test
    public void testGetLegalTagCollections_whenIdsIsNull() {
        long[] ids = null;
        List<LegalTag> output = (List<LegalTag>) sut.get(ids);
        assertEquals(output.size(), 0);
    }

    @Test
    public void testGetLegalTagCollections_whenIdsIsNotNull() {
        long[] ids = {1234, 9876};
        String[] strIds = Arrays.stream(ids).mapToObj(String::valueOf).toArray(String[]::new);
        Optional[] legalTagDocs = new Optional[2];
        legalTagDocs[0] = Optional.of(new LegalTagDoc(strIds[0], getLegalTagWithId(ids[0])));
        legalTagDocs[1] = Optional.of(new LegalTagDoc(strIds[0], getLegalTagWithId(ids[1])));

        doReturn(legalTagDocs[0]).when(cosmosStore).findItem(eq(dataPartitionId), any(), any(), eq(strIds[0]), eq(strIds[0]), any());
        doReturn(legalTagDocs[1]).when(cosmosStore).findItem(eq(dataPartitionId), any(), any(), eq(strIds[1]), eq(strIds[1]), any());

        List<LegalTag> output = (List<LegalTag>) sut.get(ids);
        assertEquals(output.size(), 2);
        assertEquals(output.get(0).getId().longValue(), ids[0]);
        assertEquals(output.get(1).getId().longValue(), ids[1]);
    }

    @Test
    public void testDeleteLegalTag_whenLegalTagDoesNotExist() {
        long id = 1234;
        LegalTag legalTag = getLegalTagWithId(id);
        boolean status = sut.delete(legalTag);
        assertEquals(status, false);
    }

    @Test
    public void testDeleteLegalTag_whenLegalTagExists() {
        long id = 1234;
        String strId = String.valueOf(id);
        LegalTag legalTag = getLegalTagWithId(id);
        Optional<LegalTag> optionalLegalTag = Optional.of(legalTag);
        doReturn(optionalLegalTag).when(cosmosStore).findItem(eq(dataPartitionId), any(), any(), eq(strId), eq(strId), any());
        boolean status = sut.delete(legalTag);

        ArgumentCaptor<String> arg1 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> arg2 = ArgumentCaptor.forClass(String.class);
        verify(cosmosStore).deleteItem(eq(dataPartitionId), any(), any(), arg1.capture(), arg2.capture());

        assertEquals(status, true);
        assertEquals(arg1.getValue(), strId);
        assertEquals(arg2.getValue(), strId);
    }

    @Test
    public void testUpdateLegalTag_whenProvidedLegalTagIsNull() {
        LegalTag legalTag = sut.update(null);
        assertNull(legalTag);
    }

    @Test(expected = AppException.class)
    public void testUpdateLegalTag_whenValidItemDoesNotExist_throwsException() {
        long id = 1234;
        LegalTag legalTag = getLegalTagWithId(id);
        try {
            sut.update(legalTag);
        } catch (AppException e) {
            int errorCode = 404;
            String errorMessage = "Cannot update a LegalTag that does not exist";
            validateAppException(e, errorCode, errorMessage);
            throw (e);
        }
    }

    @Test
    public void testUpdateLegalTag_whenValidItemExists() {
        long id = 1234;
        String strId = String.valueOf(id);
        LegalTag legalTag = getLegalTagWithId(id);
        Optional<LegalTag> optionalLegalTag = Optional.of(legalTag);
        doReturn(optionalLegalTag).when(cosmosStore).findItem(eq(dataPartitionId), any(), any(), eq(strId), eq(strId), any());
        LegalTag obtainedLegalTag = sut.update(legalTag);

        ArgumentCaptor<LegalTagDoc> arg = ArgumentCaptor.forClass(LegalTagDoc.class);
        verify(cosmosStore).upsertItem(eq(dataPartitionId), any(), any(), any(), arg.capture());

        assertEquals(arg.getValue().getId(), strId);
        assertEquals(obtainedLegalTag.getId().longValue(), id);
    }

    @Test
    public void testListLegalTags_queryItems_executesCorrectQuery() {
        long[] ids = {1234, 9876};
        String[] strIds = Arrays.stream(ids).mapToObj(String::valueOf).toArray(String[]::new);
        List<LegalTagDoc> legalTagDocs = Arrays.asList(new LegalTagDoc(strIds[0], getLegalTagWithId(ids[0])), new LegalTagDoc(strIds[1], getLegalTagWithId(ids[1])));

        ArgumentCaptor<SqlQuerySpec> query = ArgumentCaptor.forClass(SqlQuerySpec.class);
        ArgumentCaptor<CosmosQueryRequestOptions> feedOptions = ArgumentCaptor.forClass(CosmosQueryRequestOptions.class);

        doReturn(legalTagDocs).when(cosmosStore).queryItems(eq(dataPartitionId), any(), any(), any(), any(), any());
        ListLegalTagArgs legalTagArgs = new ListLegalTagArgs();
        legalTagArgs.setIsValid(true);
        List<LegalTag> output = (List<LegalTag>) sut.list(legalTagArgs);

        assertEquals(output.size(), 2);
        assertEquals(output.get(0).getId().longValue(), ids[0]);
        assertEquals(output.get(1).getId().longValue(), ids[1]);

        verify(cosmosStore).queryItems(eq(dataPartitionId), any(), any(), query.capture(), feedOptions.capture(), any());
        assertEquals(query.getValue().getQueryText(), "SELECT * FROM c WHERE c.legalTag.isValid = @isValid");

        SqlParameter isValid = query.getValue().getParameters().get(0);

        assertEquals(isValid.getName(), "@isValid");
        assertEquals(isValid.getValue(Boolean.class), true);
    }

    private LegalTag getLegalTagWithId(long id) {
        LegalTag legalTag = new LegalTag();
        legalTag.setId(id);
        return legalTag;
    }

    private void validateAppException(AppException e, int errorCode, String errorMessage) {
        assertEquals(errorCode, e.getError().getCode());
        assertThat(e.getError().getMessage(), containsString(errorMessage));
    }
}
