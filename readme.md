# Spring Cloud Functions - Azure Function - EventHub - Cosmos DB
#### This is Spring Boot application implemented using Spring Cloud Functions & deployed on Azure Function App. This code is to use for generic Auditlog. 
Auditlogs can be triggered from Http or logs pushed to EventHub are consumed by Function App and stored into Cosmos DB Container.

```shell
mvn archetype:generate --batch-mode -DarchetypeGroupId=com.microsoft.azure -DarchetypeArtifactId=azure-functions-archetype -DappName=azrarf-functionapp-data-audit-service -DresourceGroup=AZR-ARF-DMT-DEV -DappRegion=US_EAST -DgroupId=com.jnj.auditlog.func. -DartifactId=auditlog-spring-az-cloud-func
```

### Build & Run Azure Function
```shell
.\mvnw clean package
.\mvnw azure-functions:run
```
### local.settings.json
```shell
az functionapp config appsettings set \
	--name azrarf-functionapp-data-audit-service \
	--resource-group AZR-ARF-DMT-DEV \
	--settings "AzureWebJobsStorage=DefaultEndpointsProtocol=https;AccountName=azrarfdmtdevstorage;AccountKey=JDF7aGyJE4Kv+FvyxRK96/CqaOP3B/gNKwjHgG0B5YvDPwmrOVVVd2f/qmkupOiaj0/p8BlpbQ30dAgy2nDN6w==;EndpointSuffix=core.windows.net" "EventHubConnectionString=Endpoint=sb://data-audit-event-dev.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=Ozb2H1gSck+IaJ4ef/HH7w/JO2MiSY+p4vAE7g7sZyQ="  "CosmosDBConnectionString=AccountEndpoint=https://data-audit-dev.documents.azure.com:443/;AccountKey=jgnUDcrXnUQakBfPFh9QLGJSsq7jM2p7KfhYj2sjh7Dwhu1hzFVMOZWTm5N1xDiNvtFCB0mXSYDgS90eW8W1Bw==" \
	"FUNCTIONS_WORKER_RUNTIME=java"
	
az functionapp config appsettings set \
  --name azrarf-functionapp-data-audit-service \
  --resource-group AZR-ARF-DMT-DEV \
  --settings "cosmos.uri=https://data-audit-dev.documents.azure.com:443/" "cosmos.key=jgnUDcrXnUQakBfPFh9QLGJSsq7jM2p7KfhYj2sjh7Dwhu1hzFVMOZWTm5N1xDiNvtFCB0mXSYDgS90eW8W1Bw=="  "cosmos.container=auditlog cosmos.database=auditlogDB" "FUNCTIONS_WORKER_RUNTIME=java"
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
