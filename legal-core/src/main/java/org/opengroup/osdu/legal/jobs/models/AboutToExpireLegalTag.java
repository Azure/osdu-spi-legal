package org.opengroup.osdu.legal.jobs.models;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.opengroup.osdu.core.common.model.legal.serialization.ExpirationDateDeserializer;

import java.sql.Date;
import lombok.Data;

import static org.opengroup.osdu.core.common.util.SerializationUtils.EXPIRATION_DATE_FORMAT;

@Data
public class AboutToExpireLegalTag {

    private String tagName;

    @JsonDeserialize(using = ExpirationDateDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = EXPIRATION_DATE_FORMAT)
    private Date expirationDate;

    AboutToExpireLegalTag(){
    }
    public AboutToExpireLegalTag(String tagName, Date expirationDate) {
        this.tagName = tagName;
        this.expirationDate = expirationDate;
    }
}
