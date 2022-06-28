package com.jnj.auditlog.azure;


import com.jnj.auditlog.spring.model.AlEventData;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import org.springframework.cloud.function.adapter.azure.FunctionInvoker;
import org.springframework.util.ObjectUtils;
import reactor.core.publisher.Flux;

import java.util.Optional;

/**
 * Al Reteriver Function
 */
public class AlEventToDbFuncHandler extends FunctionInvoker<AlEventData, Flux<AlEventData>> {

    @FunctionName("alEventToDb")
    public HttpResponseMessage execute(
            @HttpTrigger(name = "projectName", methods = {HttpMethod.GET, HttpMethod.POST}, authLevel = AuthorizationLevel.FUNCTION, route = "auditlog-save")
            HttpRequestMessage<Optional<AlEventData>> request, ExecutionContext context) {

        if (Optional.ofNullable(request).isPresent()) {
            AlEventData requestAlData = request.getBody().orElseGet(() -> new AlEventData(request.getQueryParameters().getOrDefault("projectName", null)));
            if (ObjectUtils.isEmpty(requestAlData.getUpdateBy())) {
                return request
                        .createResponseBuilder(HttpStatus.UNAUTHORIZED)
                        .build();
            }
            return request
                    .createResponseBuilder(HttpStatus.OK)
                    .body(handleRequest(requestAlData, context))
                    .header("Content-Type", "text/event-stream")
                    .build();
        }
        return request
                .createResponseBuilder(HttpStatus.BAD_REQUEST)
                .header("Content-Type", "application/json")
                .build();
    }
}