package org.opengroup.osdu.legal.util;

import org.apache.commons.text.StringEscapeUtils;
import com.google.common.base.Strings;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

public class HtmlEncodeAdapter extends XmlAdapter<String, String> {

    @Override
    public String unmarshal(String rawString) {
        if(Strings.isNullOrEmpty(rawString))
            return "";

        return StringEscapeUtils.escapeHtml4(rawString);
    }

    @Override
    public String marshal(String encodedString) {
        return encodedString;
    }
}
