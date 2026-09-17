package org.opengroup.osdu.legal.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HtmlEncodeAdapterTests {

    HtmlEncodeAdapter sut = new HtmlEncodeAdapter();

    @Test
    public void should_convertGivenString_To_aHtmlEncodedStringGoogleDate(){
       String result = sut.unmarshal("<>");
        assertEquals("&lt;&gt;", result);
    }

    @Test
    public void should_convertNull_To_aEmptyString(){
        String result = sut.unmarshal(null);
        assertEquals("", result);
    }

    @Test
    public void should_convertDate_To_aDateString(){
        String result = sut.unmarshal("hello");
        assertEquals("hello", result);
    }

    @Test
    public void should_marshal_success(){
        String result = sut.marshal("hello");
        assertEquals("hello", result);
    }

}
