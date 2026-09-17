# OSDU Legal (Compliance) Service

## Introduction

This document covers how to remain compliant at the different stages of the data lifecycle inside the Data Ecosystem.
 
1. When ingesting data
2. Whilst the data is inside the Data Ecosystem 
3. When consuming data

The clients' interaction revolves around ingestion and consumption, so this is when you need to use what is contained in this guide. Point 2 should be mostly handled on the clients’ behalf; however, it is still important to understand that this is happening as it has ramifications on when and how data can be consumed.

Data compliance is largely governed through the Records in the storage service. Though there is an independent legal service and LegalTags entity, these offer no compliance by themselves.

Records have a Legal section in their schema and this is where the compliance is enforced. However, clients must still make sure they are using the Record service correctly to remain compliant.

Further details can be found in the [Creating a Record](./api.md#creating-a-record) section.

## What is a LegalTag?
A LegalTag is the entity that represents the legal status of data in the Data Ecosystem. It is a collection of *properties* that governs how the data can be consumed and ingested. 

A legal tag is required for data ingestion. Therefore, creation of a legal tag is a necessary first step if there isn't a legal tag already exists for use with the ingested data. The LegalTag name needs to be assigned to the LegalTag during creation, and is used for reference. The name is the unique identifier for the LegalTag that is used to access it.

When data is ingested, it is assigned the LegalTag *name*. This name is checked for a corresponding valid LegalTag in the system.  A valid LegalTag means it exists and has not expired. If a LegalTag is invalid, the data is rejected. For instance, we may not allow ingestion of data from a certain country, or we may not allow consumption of data that has an expired contract.

In the same manner, the ingested data will be invalidated (soft-deleted) when the legal tag expires, as it would no longer be compliant.

Upon ingestion of data, it's the responsiblity of the process ingesting that data to use the appropriate tag for business rules and processes..

Further details can be found in the [Creating a Legal Tag](./api.md#creating-a-legaltag).
