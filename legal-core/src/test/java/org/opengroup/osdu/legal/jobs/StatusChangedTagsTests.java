package org.opengroup.osdu.legal.jobs;

import org.junit.jupiter.api.Test;
import org.opengroup.osdu.core.common.model.legal.StatusChangedTag;
import org.opengroup.osdu.core.common.model.legal.StatusChangedTags;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StatusChangedTagsTests {

    @Test
    public void should_print_object_whenFormattedToString(){
        StatusChangedTags statusChangedTags = new StatusChangedTags();
        statusChangedTags.getStatusChangedTags().add(new StatusChangedTag(null, null));
        String s = String.format("%s", statusChangedTags);

        assertEquals("StatusChangedTags(statusChangedTags=[StatusChangedTag(changedTagName=null, changedTagStatus=null)])", s);
    }
}
