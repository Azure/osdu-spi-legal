# API

## API Usage
Details of our APIs including how to create and retrieve LegalTags can be found [here.](https://community.opengroup.org/osdu/platform/security-and-compliance/legal/-/blob/5ecde60781c5ce0c92579e87c4b30fdf9219a9ab/docs/api/compliance_openapi.yaml)

### Permissions

| **_API_** | **_Minimum Permissions Required_** |
| --- | --- |
| Access LegalTag APIs| users.datalake.viewers |
| Create a LegalTag | users.datalake.editors |
| Update a LegalTag | users.datalake.editors |

### Headers

| **_Header_** | **_Description_** |
| --- | --- |
| data-partition-id (Required) | Specify the desired accessible partition id.|
| correlation-id (Optional) | Used to track a single request throughout all the services it passes through. This can be a GUID on the header with a key. If you are the service initiating the request, you should generate the id. Otherwise, you should just forward it on in the request. |

The Data Ecosystem stores data in different data partitions, depending on the access to those data partitions in the OSDU system. A user may have access to one or more data partitions.

## Creating a LegalTag

Any data being ingested needs a LegalTag associated with it.  You can create a LegalTag by using the POST LegalTag API e.g.

    POST /api/legal/v1/legaltags
    
<details><summary>Curl Post legaltags Example</summary>

```
curl --request POST \
  --url '/api/legal/v1/legaltags' \
  --header 'accept: application/json' \
  --header 'authorization: Bearer <JWT>' \
  --header 'content-type: application/json' \
  --header 'data-partition-id: opendes' \
  --data '{
        "name": "demo-legaltag",
        "description": "A legaltag used for demonstration purposes.",
        "properties": {
            "countryOfOrigin":["US"],
            "contractId":"No Contract Related",
            "expirationDate":"2099-01-01",
            "dataType":"Public Domain Data", 
            "originator":"OSDU",
            "securityClassification":"Public",
            "exportClassification":"EAR99",
            "personalData":"No Personal Data",
            "extensionProperties": {
                "anyCompanySpecificAttributes": "anyJsonTypeOfvalue"
            }
        }
}'
```

</details>
    
!!! Tip

     It is good practice for LegalTag names to be clear and descriptive of the properties it represents, so it would be easy to discover and to associate to the correct data with it. Also, the description field is a free form optional field to allow for you to add context to the LegalTag, making easier to understand and retrieve over time.

The "extensionProperties" field is an optional json object field and you may add any company specific attributes inside this field.

When creating LegalTags, the name is automatically prefixed with the data-partition-name that is assigned to the partition. So in the example above, if the given data-partition-name is **mypartition**, then the actual name of the LegalTag would be **mypartition-demo-legaltag**.

!!! Info "Legal Tag Names"

    The legalTag name needs to be between 3 and 100 characters and only alphanumeric characters and hyphens are allowed.

To help with LegalTag creation, it is advised to use the Get LegalTag Properties API to obtain the allowed properties before creating a legal tag. This returns the allowed values for many of the LegalTag properties.

## LegalTag Properties

Below are details of the properties you can supply when creating a LegalTag along with the values you can use. The allowed properties values can be data partition specific. Valid values associated with the property are shown. All values are mandatory unless otherwise stated.

You can get the data partition's specific allowed properties values by using LegalTag Properties API e.g.

    GET /api/legal/v1/legaltags:properties

<details><summary>Curl Get legaltags:properties Example</summary>
```
curl --request GET \
  --url '/api/legal/v1/legaltags:properties' \
  --header 'accept: application/json' \
  --header 'authorization: Bearer <JWT>' \
  --header 'content-type: application/json' \
  --header 'data-partition-id: opendes' \
```
</details>
    
<details><summary>Example 200 Response</summary>
```
    {
    	"countriesOfOrigin": {
    		"TT": "Trinidad and Tobago",
    		"TW": "Taiwan, Province of China",
    		"LR": "Liberia",
    		"DK": "Denmark",
    		"LT": "Lithuania",
    		"PY": "Paraguay",
    		"US": "United States",
    		...
    		...    		
    	},
    	"otherRelevantDataCountries": {
    		"PT": "Portugal",
    		"PW": "Palau",
    		"PY": "Paraguay",
    		"QA": "Qatar",
    		"AD": "Andorra",
    		"AE": "United Arab Emirates",
            ...
            ...
    	},
    	"securityClassifications": ["Private", "Public", "Confidential"],
    	"exportClassificationControlNumbers": ["No License Required", "Not - Technical Data", "EAR99", "0A998"],
    	"personalDataTypes": ["Personally Identifiable", "No Personal Data"]
    }
```

</details>  


### Country of Origin

Valid values: An array of [ISO Alpha-2 country codes](https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2). This is normally one value but can be more. This field is required.
 
!!! Note "Country of Origin"

    This is the country from where the data originally came, NOT from where the data was sent. The list of default allowed countries is defined [here](https://community.opengroup.org/osdu/platform/security-and-compliance/legal/-/blob/master/legal-core/src/main/resources/DefaultCountryCode.json?ref_type=heads). If ingesting Third Party Data, you can ingest data from any country that is not embargoed, if you have a valid contract associated with it that allows for this. This property is case sensitive.

### Contract Id

 Valid values: This should be the Contract Id associated with the data or 'Unknown' or 'No Contract Related'.
 
 The contract ID must be between 3 and 40 characters and only include alphanumeric values and hyphens.

!!! Note "ContractId"

    This is always required for any data types. This property is case sensitive.
 
### Expiration Date

 Valid values: Any date in the future in the format yyyy-MM-dd (e.g. 2099-12-25) or empty.
 
 When the provided contract ID is "Unknown" or "No Contract Related", then the _expiration date_ could be empty. However, if the date is provided, then it will be honored in validating the legal tag and the associated data, even when there's no contract is provided. As such, when the legal tag expires, the associated data will be soft-deleted from the Data Ecosystem.
 
!!! Note "Expiration Date"

    This sets the inclusive date when the LegalTag expires and the data it relates to is no longer usable in the Data Ecosystem. This normally is taken from the physical contracts _expiration date_ i.e. when you supply a contract ID. This is non mandatory field, but is required for certain types of data e.g. 3rd party. If the field is not set it will be autopopulated with the value 9999-12-31. This property is case sensitive.
   
### Originator

Valid values: Should be the name of the client or supplier.
   
!!! Note "Originator"

    This is always required. This property is case sensitive.
   
### Data type

| dataType | Data Residency Restriction |
| ----------- | -------- |
| "Public Domain Data" | "public data, no contract required" |
| "First Party Data" | "partition owner's data, no contract required" |
| "Second Party Data" | "client data, contract is required" |
| "Third Party Data" | "contract required" |
| "Transferred Data" | "EHC/Index data, no contract required" |

  
!!! Note "Data Types"

    The data types that are allowed are dependent on the data partitions. Vendor partitions may have different governing rules as opposed to standard partitions. To list the allowed data types for your data partition use the `GET /api/legal/v1/legaltags:properties` as described in [properties](#legaltag-properties).
                                                                                                                        
### Security classification

Valid values:

 - `Public`,
 - `Private`,
 - `Confidential`
   
!!! Warning

    This is the standard security classification for the data. We currently do not allow `Secret` data to be stored in the Data Ecosystem. This property is NOT case sensitive.
   
### Export classification

Valid values:

- `0A998`(0 as Zero),
- `EAR99`,
- `Not - Technical Data` (planned)
- `No License Required` (planned)
   
!!! Note

    We currently only allow data with the ECCN classification 'EAR99' and '0A998'(0 as Zero).
    This property is NOT case sensitive.
   
### Personal data

Valid values:

- `Personally Identifiable`,
- `No Personal Data`
   
!!! Warning "Sensitive Personal Information"

    We do not currently allow data that is _Sensitive Personal Information_ and this should __not__ be ingested.
    This property is NOT case sensitive.
 
  	 	
## Creating a Record

This relates to creating Records that are __NOT__ derivatives. See the [derivatives](#what-are-derivatives) section below for details on Record creation for derivative data.
 
Once you have a LegalTag created, you can assign it to as many Records as you like. However, it is the data managers' responsibility to assign accurate LegalTags to data.

When creating a Record, the following needs to be assigned for legal compliance:

* The LegalTag name associated with the Record
* The Alpha-2 country code of the original caller where the data is being ingested from 

Below is a full example of the payload needed when creating a Record. The *legal* section shows what is required.

<details><summary>Example JSON Payload for Creating a LegalTag</summary>
```
    [{
            "acl": {
                    "owners": [
                         "data.default.owners@{datapartition}.{domain}.org"
                    ],
                    "viewers": [
                        "data.default.viewers@{datapartition}.{domain}.org"
                    ]
            },
            "data": {
                    "count": 123456789
            },
            "id": "opendes:id:123456789",
            "kind": "opendes:welldb:wellbore:1.0.0",
            "legal" :{
                    "legaltags": [
                            "opendes-demo-legaltag"
                    ],
                    "otherRelevantDataCountries": ["US"] //the physical location of the person ingesting the data
            }
    }]
```
</details>

* legaltags - This section represents the names of the LegalTag(s) associated with the Record. This has to be supplied when the Record represents raw or source data (i,e, not derivative data).
* otherRelevantDataCountries - This is the Alpha-2 country codes for the country the data was ingested from and the country where the data is located in Data Ecosystem. otherRelevantDataCountries is not part of the LegalTag(s). It is part of the legal property of the record. The location of the data center, in which the record is stored is automatically added to the otherRelevantDataCountries list when the record is created. This location depends on the environment/region that the partition locates. 

You can get the list of all valid LegalTags using the Get LegalTags API method. You can use this to help assign only valid LegalTags to data when ingesting.

    GET /api/legal/v1/legaltags?valid=true
    
<details><summary>Curl GET legaltags Example</summary>
```
curl --request GET \
  --url '/api/legal/v1/legaltags?valid=true' \
  --header 'accept: application/json' \
  --header 'authorization: Bearer <JWT>' \
  --header 'content-type: application/json' \
  --header 'data-partition-id: opendes' \
```
</details>

<details><summary>Example 200 Response</summary>

```
    {
      "legalTags": [
        {
          "name": "osdu-thirdparty-public",
          "description": "",
          "properties": {
            "countryOfOrigin": [
              "US"
            ],
            "contractId": "A1234",
            "expirationDate": "2099-01-25",
            "originator": "OSDU",
            "dataType": "Third Party Data",
            "securityClassification": "Public",
            "personalData": "No Personal Data",
            "exportClassification": "EAR99"
          }
        },
        {
          "name": "osdu-welldb-public",
          "description": "",
          "properties": {
            "countryOfOrigin": [
              "US"
            ],
            "contractId": "AB123",
            "expirationDate": "2099-12-25",
            "originator": "OSDU",
            "dataType": "Public Domain Data",
            "securityClassification": "Public",
            "personalData": "No Personal Data",
            "exportClassification": "EAR99"
          }
        },
        ...
        ...
        ...
    }
```
</details>

## What are Derivatives?

In the context of OSDU, the term "derivative data" is data that has been derived from primary data sources.

Often when ingesting data into the Data Ecosystem, it is the raw data itself. In these scenarios, you associate a single LegalTag with this data.

However, in the case when the data to be ingested comes from multiple sources, it is the case of derivative data. For instance, what if you take multiple Records from the Data Ecosystem and create a whole new Record based on them all? Or what if you run an algorithm over your seismic data and create an attribute associated with this data you want to ingest?

At this point, you have derivative data (i.e., data derived from data). In these scenarios, you will need to assign LegalTags to this new data which is the union of the LegalTags associated to all the source data from which it was created.

For instance, I have Data A associated with LegalTag 1, and Data B associated with LegalTag 2. If I create Data C from Data A and Data B, then I need to associate both LegaltTag 1 and LegalTag 2 to Data C.

### Creating derivative Records
When creating Records that represent derivative data, the following must be assigned:

* The Record Id and version of all the Records that are the direct parents of the new derivative. This is added to the *ancestry* section  
* The Alpha-2 country code of where the derivative was created

Below is an example of the minimum number of fields required to ingest a derivative Record.

<details><summary>Record Example</summary>

```
        [{
                "acl": {
                        "owners": [ 
                            "data.default.owners@{datapartition}.{domain}.org" 
                        ],
                        "viewers": [ 
                            "data.default.viewers@{datapartition}.{domain}.org"
                        ]
                },
                "data": {
                        "count": 123456789
                },
                "id": "opendes:id:123456789",
                "kind": "opendes:welldb:wellbore:1.0.0",
                "legal" :{
                        "otherRelevantDataCountries": ["US"] //the physical location of where the derivative was created
                },
                "ancestry" :{
                       "parents": ["opendes:id:1:version", "opendes:id:2:version"] //the record ids and versions of the Records this derivative was created from
                }    
        }]
```
</details>

As shown the parent Records are provided as well as the ORDC of where the derivative was created.  The Record service takes responsibility for populating the full LegalTag and ORDC values based on the parents.


## Validating a LegalTag

The Storage service validates whether a Record is legally compliant during ingestion and consumption. Therefore, you can delegate the effort to the Record service as the request will fail if the Record is not compliant.

However, there may be times you want to validate LegalTags directly. 

You can validate a LegalTag by using the LegalTag validate API supplying the names of the LegalTags you wish to validate  e.g.
 
    POST /api/legal/v1/legaltags:validate
    
<details><summary>Curl Post legaltags:validate</summary>
```
curl --request POST \
  --url '/api/legal/v1/legaltags:validate' \
  --header 'accept: application/json' \
  --header 'authorization: Bearer <JWT>' \
  --header 'content-type: application/json' \
  --header 'data-partition-id: opendes' \
  --data '{
        "names": ["opendes-demo-legaltag"]
}'
```
</details>

If the LegalTag is valid, the response then looks something like this

<details><summary>Valid Response Example</summary>
```    
    {
        "invalidLegalTags": [] 
    }
```
</details>

If the LegalTag is invalid, the response then looks something like this

<details><summary>Invalid Response Example</summary>
```
    {
        "invalidLegalTags": [
            {"name":"opendes-demo-legaltag", "reason": "Contract expired"}
        ] 
    }
```
</details>

So if you just want to check that the given LegalTag(s) are currently valid, you only have to check if the returned 'invalidLegalTags' collection is empty.

Ingestion services forward the request to the LegalTag API using the same _SAuth_ token making the ingestion request. This checks both that a LegalTag exists and that the data has appropriate access to it.

## Updating a LegalTag

One of the main cases where a LegalTag can become invalid is if a contract expiration date passes. This makes both the LegalTag invalid and *all* data associated with that LegalTag including derivatives.

In these situations we can update LegalTags to make them valid again and so make the associated data accessible. Currently we only allow the update of the *description*, *contract ID*, *expiration date* and *extensionProperties* properties. 

    PUT /api/legal/v1/legaltags

<details><summary>Curl Put legaltags Example</summary>

```
curl --request PUT \
  --url '/api/legal/v1/legaltags' \
  --header 'accept: application/json' \
  --header 'authorization: Bearer <JWT>' \
  --header 'content-type: application/json' \
  --header 'data-partition-id: opendes' \
  --data '{
        "name": "opendes-demo-legaltag",
        "contractId": "AE12345",
        "expirationDate": "2099-12-21",
        "extensionProperties": {
            "anyCompanySpecificAttributes": "anyJsonTypeOfvalue"
        }
    }
}'
```

</details>

!!! Danger "Valid and Validate"

    There is a difference between "querying for valid or invalid LegalTag (List Legal Tag API )" and "checking if the legalTag is Valid (Validate Legal Tag API)".

The "List Legal tag" will check whether the tag is valid right now and "Validate Legal Tag" will check whether the tag would be valid after the next once-a-day update process.
Therefore, updating the LegalTag _Expiration Date_ will not make the record visible as valid in the Valid Tag List until its status has been updated. 
The Update process validates the LegalTags and then updates the status of the LegalTag from Invalid to Valid or vice versa. This update process runs only once in a day.

## Compliance on Consumption
As previously stated, the Records in the Storage service largely governs data compliance.  This means that if you use the Storage or Search core services, then compliance on consumption is handled on your behalf i.e. these services will not return Records that are no longer legally compliant.

However, there are use cases where you may not use these services all the time e.g. if you have your own operational data store.  In these cases you will need to check the LegalTags associated with your data are still valid before allowing consumption. For this, we have a PubSub topic that can be subscribed to.

!!! Warning "Compliance on Consumption"

    Currently, this topic can only be subscribed to if you deploy your service within the Data Ecosystem Google Project. 

This topic has the form
`projects/{googleProjectId}/topics/legaltags_changed`

This means you need to make a subscription to every data partition project you wish to receive the notifications on. 

!!! Info "Async Process"

    When new data partitions are added into the Data Ecosystem, it may take up to 24 hours for the topic to become available to subscribe to.

For more information on subscribing to PubSub topics, please use the Google documentation [here](https://cloud.google.com/pubsub/docs/subscriber).

## The LegalTag Changed Notification

After subscribing to the topic, you will receive notifications daily. These notifications will list all LegalTags that have changed, and whether the LegalTag has become compliant or non-compliant.

<details><summary>Details</summary>

```
    {
        "statusChangedTags": [ { 
                "changedTagName": "legaltag-name1",
                "changedTagStatus": "compliant"
            },
            {
                "changedTagName": "legaltag-name2",
                "changedTagStatus": "incompliant"
            } ]
    }
```
</details>

The above shows an example message sent to subscribers. It shows you receive an array of items. Each item has the LegalTag name that has changed and whether it has changed to be compliant or incompliant.  

If it has become incompliant, you must make sure associated data is no longer allowed to be consumed.

If it is marked compliant, data that was not allowed for consumption can now be consumed through your services.

## The LegalTag About to Expire notification
After subscribing to the topic, you will receive notifications daily.
These notifications will list all LegalTags that will expire soon.
The definition of "soon" is configurable by the deployer.

This topic has the form

    projects/{googleProjectId}/topics/about-to-expire-legal-tag	

Note: this feature is also behind a feature flag, meaning the deployer
must specifically enable the feature.

<details><summary>Expiration Details</summary>

```
    {
        "aboutToExpireLegalTags": [ 
            {
                "dataPartitionId":"osdu",
                "tagName":"osdu-public-usa-dataset-osduonaws-test-about-to-expire",
                "expirationDate":"Mar 15, 2024"
            }
        ]
    }
```

</details>

Time to expire should be configurable on deployment level. We should be able to provide list of all time periods before LegalTag expires. This will be achieved by introducing new environment variable `legaltag.expirationAlerts`. Format is comma separated values which consists of number and letter suffix, for example: 3d,2w,1m
Where suffix letter should represent this time periods:

* \#d - \# number of days
* \#w - \# number of weeks
* \#m - \# number of months

## Version info endpoint
For deployment available public `/info` endpoint, which provides build and git related information.

Example info response:
```json
{
    "groupId": "org.opengroup.osdu",
    "artifactId": "storage-gcp",
    "version": "0.10.0-SNAPSHOT",
    "buildTime": "2021-07-09T14:29:51.584Z",
    "branch": "feature/GONRG-2681_Build_info",
    "commitId": "7777",
    "commitMessage": "Added copyright to version info properties file",
    "connectedOuterServices": [
      {
        "name": "elasticSearch",
        "version":"..."
      },
      {
        "name": "postgresSql",
        "version":"..."
      },
      {
        "name": "redis",
        "version":"..."
      }
    ]
}
```
This endpoint takes information from files, generated by `spring-boot-maven-plugin`,
`git-commit-id-plugin` plugins. Need to specify paths for generated files to matching
properties:

- `version.info.buildPropertiesPath`
- `version.info.gitPropertiesPath`

## Legal Query

!!! Warning "New in M23"

    The `/legaltags:query` API was introduced in M23. This API can also be disabled server side via a [feature flag](/features/#legal-query-api) (`featureFlag.legalTagQueryApi.enabled`). If it is disabled you will receive a HTTP 405 Method Not Allowed.

This new API `POST /legaltags:query` takes a payload of `{"queryList": ["attribute=value"]}` and is implemented using contains (as partial match ignoring case).

This query like `/legaltags` API takes a parameter `valid` (boolean).
If `valid` parameter is true returns only valid LegalTags, if false returns only invalid LegalTags.
Default value is true for `valid` parameter.

### Query parameters

- queryList - A list of query strings. Currently multiple queries are supported. All values are treated as strings except for dates when using `between (start_date, end_date)` format. All queries are implemented as a case-insensitive iscontains, using `*` or `?` is not supported.

!!! Note "Boolean Values"

    If an attribute has a Boolean value querying on that attribute is supported and will match as string or boolean value.

### Expiration Date

For `expirationDate` you can do a string match (even partial) using `{"queryList": ["expirationDate=value"]}` or given a date range using `between`:

```
{"queryList": ["expirationDate between (2023-01-01, 2024-12-31)"]}
```

When using `between` the dates provided will not be included in query results. What you are searching for must be `between` the two dates (i.e. the above will provide results from 2023-01-02 thru 2024-12-30).

This also supports all attributes, including those in extensionProperties. All extension properties are based upon string matching only. This will even work on nested properties for example:

```
                "extensionProperties": {
                    "AgreementIdentifier": "dz-test-O",
                    "EffectiveDate": "2023-01-00T00:00:00",
                    "TerminationDate": "2099-12-31T00:00:00",
                    "AffiliateEnablementIndicator": true,
                    "AgreementParties": [
                        {
                            "AgreementPartyType": "EnabledAffiliate",
                            "AgreementParty": "Acme RDS"
                        }
                    ]
                }
```

Example search by name:

```
curl -X 'POST' \
  'https://site/api/legal/v1/legaltags:query?valid=true' \
  -H 'accept: */*' \
  -H 'data-partition-id: osdu' \
  -H 'Content-Type: application/json' \
  -d '{"queryList":["name=test"]}'
```

This will return the same kind of response getall tags would do (but just the matching tags):
<details><summary>Response example</summary>
```
{
  "legalTags": [
    {
      "name": "osdu-1703151379194",
      "description": "test for osdu-1703151379194",
      "properties": {
        "countryOfOrigin": [
          "US"
        ],
        "contractId": "A1234",
        "expirationDate": "2099-01-25",
        "originator": "MyCompany",
        "dataType": "Public Domain Data",
        "securityClassification": "Public",
        "personalData": "No Personal Data",
        "exportClassification": "EAR99"
      }
    }
}
```
</details>

### Operators
The `operatorList` provides a way to how to treat multiple queries. Currently **only** a single operator is supported.

- `union` - the default (if no operatorList provided), return the set of all tags from all queries removing the duplicates.
- `intersection` - return the common set of tags that are found in each of the queries.
- `add` - return the set of all tags from all queries keeping any duplicates.

!!! Warning "Intersection Requires more than one query"

    Intersection operator is only intended to be used with multiple queries. An intersection of a query with itself (a single query in the queryList) is an empty set (𝜙) and the result will always be an empty match.

<details><summary>Operator Payload example</summary>

```
{
    "queryList": ["expirationDate=value"]
    "operatorList": ["union"]
}
``` 
</details>

### Planned support for the following

- operatorList - additional operators to control how to join multiple queries together (logical operators)
- sortBy - Allows you to add one or more sorts on specific fields
- sortOrder - ascending or descending
- limit - The maximum number of results to return

<details><summary>Payload example</summary>
```
{
  "queryList": ["name=test"],
  "operatorList": ["union"],
  "sortBy": "name",
  "sortOrder": "ascending",
  "limit": 10
}
```
</details>

### Extension Properties
Extension properties can be searched just like any other legal tag property. For example:
`["AgreementIdentifier=search string"]`

### Free Text Query

Free text search is currently supported as part of M23. Free text query is implemented as a case-insensitive iscontains, using `*` or `?` is not supported.

* `"queryList":["string to search for"]`
* `"queryList":["any=string to search for"]`

Free text search will check the values of the following properties:

* name,
* description,
* contractId,
* originator,
* countryOfOrigin,
* extension properties

If [feature flag](/features/#legal-query-api-free-text) `featureFlag.legalTagQueryApiFreeTextAllFields.enabled` is enabled then it will also check these additional properties for a match:

* expirationDate,
* dataType,
* securityClassification,
* personalData,
* exportClassification

### Additional Examples

<details><summary>Curl Post legaltags Query Examples</summary>

```
curl -X 'POST' \
  'https://site/api/legal/v1/legaltags:query?valid=true' \
  -H 'accept: */*' \
  -H 'data-partition-id: osdu' \
  -H 'Content-Type: application/json' \
  -d '{"queryList":["AgreementIdentifier=test"]}'
```

```
curl -X 'POST' \
  'https://site/api/legal/v1/legaltags:query?valid=true' \
  -H 'accept: */*' \
  -H 'data-partition-id: osdu' \
  -H 'Content-Type: application/json' \
  -d '{"queryList":["description=foo"]}'
```

```
curl -X 'POST' \
  'https://site/api/legal/v1/legaltags:query?valid=true' \
  -H 'accept: */*' \
  -H 'data-partition-id: osdu' \
  -H 'Content-Type: application/json' \
  -d '{"queryList":["countryOfOrigin=US"]}'
```

```
curl -X 'POST' \
  'https://site/api/legal/v1/legaltags:query?valid=true' \
  -H 'accept: */*' \
  -H 'data-partition-id: osdu' \
  -H 'Content-Type: application/json' \
  -d '{"queryList":["AffiliateEnablementIndicator=True"]}'
```
</details>