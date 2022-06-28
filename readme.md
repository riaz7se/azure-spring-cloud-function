# Spring Cloud Functions - Azure Function - EventHub - Cosmos DB
### This is Spring Boot application implemented using Spring Cloud Functions & deployed on Azure Function App. This code is to use for generic Auditlog. 
Auditlogs can be triggered from Http or logs pushed to EventHub are consumed by Function App and stored into Cosmos DB Container.

```shell
mvn archetype:generate --batch-mode \
  -DarchetypeGroupId=com.microsoft.azure -DarchetypeArtifactId=azure-functions-archetype \
  -DappName=<azure-function-app-name> -DresourceGroup=<azure-resource-group> -DappRegion=<azure-region> \
  -DgroupId=com.se.auditlog.func. -DartifactId=al-spring-az-cloud-func
```

### Build & Run Azure Function
```shell
.\mvnw clean package
.\mvnw azure-functions:run
```
### local.settings.json
```shell
az functionapp config appsettings set \
	--name <azure-function-app-name> \
	--resource-group <azure-resource-group> \
	--settings "AzureWebJobsStorage=DefaultEndpointsProtocol=https;AccountName=<azure-storage-name>;AccountKey=<account-key==>;EndpointSuffix=core.windows.net" "EventHubConnectionString=Endpoint=sb://my-audit-event.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=<shared-access-key>"  "CosmosDBConnectionString=AccountEndpoint=https://se-audit-dev.documents.azure.com:443/;AccountKey=<azzount-key==>" \
	"FUNCTIONS_WORKER_RUNTIME=java"
	
az functionapp config appsettings set \
  --name azrarf-functionapp-data-audit-service \
  --resource-group AZR-ARF-DMT-DEV \
  --settings "cosmos.uri=https://se-audit-dev.documents.azure.com:443/" "cosmos.key=<cosmos-db-key==>"  "cosmos.container=auditlog cosmos.database=auditlogDB" "FUNCTIONS_WORKER_RUNTIME=java"
```
### To get AuditLog Data as stream
- [GET,POST] http://localhost:7071/api/auditlog-stream

### To download Auditlog in Excel report
- [GET,POST] http://localhost:7071/api/auditlog-report

**** Above calls has Auth Level enabled, header x-functions-key has to be passed with function key  


### For loading all props to local.settings.json
```shell
func azure functionapp fetch-app-settings azrarf-functionapp-data-audit-service
```
