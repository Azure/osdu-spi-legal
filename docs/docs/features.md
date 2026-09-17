# Feature Flags

In M23 three feature flags were added to legal service to optionally control the behavior:

## Expire Legal Tag
`featureFlag.aboutToExpireLegalTag.enabled` see [about to expire](/api#the-legaltag-about-to-expire-notification) for more details.

##  Legal Query API
The feature flag `featureFlag.legalTagQueryApi.enabled` enables or disables this [API](api.md#legal-query).

## Legal Query API Free Text 
The feature flag `featureFlag.legalTagQueryApiFreeTextAllFields.enabled` when disabled excludes the following from the query match:

- expirationDate,
- dataType,
- securityClassification,
- personalData,
- exportClassification

See [Legal Query Free Text](api.md#free-text-query) for more details.