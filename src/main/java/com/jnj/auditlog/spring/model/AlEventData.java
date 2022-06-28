package com.jnj.auditlog.spring.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AlEventData {

    private String appName;
    private String projectName;

    private String infoType;

    private String operation;

    private String appKey;

    private String requestId;

    private Map<String, Object> payload;

    private String updateTimestamp = String.valueOf(LocalDateTime.now());

    private String updateBy;

    public AlEventData(String projectName) {
        this.projectName = projectName;
    }
}
