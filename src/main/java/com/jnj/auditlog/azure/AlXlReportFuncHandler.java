package com.jnj.auditlog.azure;


import com.jnj.auditlog.spring.model.AlEventData;
import com.jnj.auditlog.spring.model.ErrorDetails;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.function.adapter.azure.FunctionInvoker;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.ObjectUtils;

import java.util.Optional;

@Slf4j
public class AlXlReportFuncHandler extends FunctionInvoker<AlEventData, ByteArrayResource> {

    @FunctionName("alXlReportFunc")
    public HttpResponseMessage execute(
            @HttpTrigger(name = "projectName", methods = {HttpMethod.GET, HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS, route = "auditlog-report")
            HttpRequestMessage<Optional<AlEventData>> request, ExecutionContext context) {

        if (Optional.ofNullable(request).isPresent()) {
            AlEventData requestAlData = request.getBody().orElseGet(() -> new AlEventData(request.getQueryParameters().getOrDefault("projectName", null)));
            return request
                    .createResponseBuilder(HttpStatus.OK)
                    .body(handleRequest(requestAlData, context).getByteArray())
                    .header("Content-Type","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
//                    .header("Content-Type", "application/vnd.ms-excel")
//                    .header("Content-Disposition", "attachment; filename=AuditLogs.xls")
                    .header("Content-Disposition", "inline; filename=AuditLogs.xlsx")
                    .build();

        }
        ErrorDetails errorDetails = ErrorDetails
                .builder()
                .message("Error while processing input request").build();
        return request
                .createResponseBuilder(HttpStatus.BAD_REQUEST)
                .body(errorDetails)
                .header("Content-Type", "application/json")
                .build();
    }
}