package org.opengroup.osdu.legal;

public class Constants {
    public static final String ABOUT_TO_EXPIRE_FEATURE_NAME = "featureFlag.aboutToExpireLegalTag.enabled";

    public static final String LEGAL_QUERY_API_FEATURE_NAME = "featureFlag.legalTagQueryApi.enabled";

    public static final String LEGAL_QUERY_API_FREE_TEXT_ALL_FIELDS_FEATURE_NAME = "featureFlag.legalTagQueryApiFreeTextAllFields.enabled";

    public static final String LEGAL_QUERY_API_BETWEEN_START = "(";
    public static final String LEGAL_QUERY_API_BETWEEN_END = ")";
    public static final String LEGAL_QUERY_API_ATTRIBUTE_SEPARATOR = "=";
    public static final String LEGAL_QUERY_API_QUERY_SEPARATOR = ",";
    public static final String LEGAL_QUERY_API_FREE_TEXT_ATTRIBUTE = "any";
    public static final String LEGAL_QUERY_API_UNION_OPERATOR = "union";
    public static final String LEGAL_QUERY_API_INTERSECTION_OPERATOR = "intersection";
    public static final String LEGAL_QUERY_API_ADD_OPERATOR = "add";

    private Constants() {
        // Private constructor to prevent instantiation
    }
}
