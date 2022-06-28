package com.jnj.auditlog.spring.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class AuditLogData {

    private String appName;

    private String infoType;

    private String operation;

    private String appKey;

    private Map<String, Object> payload;

    public AuditLogData(String appName) {
        this.appName = appName;
    }

    private String updateBy;

    private String updateTimestamp = String.valueOf(LocalDateTime.now());

}
