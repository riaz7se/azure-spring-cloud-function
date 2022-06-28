package com.jnj.auditlog.azure;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.OutputBinding;
import com.microsoft.azure.functions.annotation.Cardinality;
import com.microsoft.azure.functions.annotation.CosmosDBOutput;
import com.microsoft.azure.functions.annotation.EventHubTrigger;
import com.microsoft.azure.functions.annotation.FunctionName;

public class AlEventHubTriggerCosmosBindFunc {

    @FunctionName("EventHub-Trigger-Cosmos-Bind")
    public void processAuditLogData(
            @EventHubTrigger(
                    name = "eventHubInput",
                    eventHubName = "data-audit-event", // blank because the value is included in the connection string
                    cardinality = Cardinality.ONE,
                    connection = "EventHubConnectionString")
            Object auditLogEventData,
            @CosmosDBOutput(
                    name = "auditLogOutput",
                    databaseName = "auditlogDB",
                    collectionName = "auditlog",
                    connectionStringSetting = "CosmosDBConnectionString")
            OutputBinding<Object> document,
            final ExecutionContext context) {

        context.getLogger().info("Event hub message received: " + auditLogEventData.toString());

        //enrich or modify auditLogEventData here

        document.setValue(auditLogEventData);
    }
}
