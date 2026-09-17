# Ingestion Workflow

![API Security - High level](https://storage.googleapis.com/devportal-live-public/IngestionSequenceDiagram.png)

The above diagram shows the typical sequence of events of a data ingestion. The important points to highlight are as follow:

* It is the ingestor's responsibility to create a LegalTag. LegalTag validation happens at this point.
* The Storage service validates the LegalTag for the data being ingested. 
* Only after validating a LegalTag exists can we ingest data.  No data should be stored at any point in the Data Ecosystem that does not have a valid LegalTag.