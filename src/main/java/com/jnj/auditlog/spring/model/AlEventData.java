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

    String appName;

    private String projectName;

    private String infoType;

    private String operation;

    private long tableId;  //TODO: add, test with list payload search query

//    private Map<String, Object> payload;
    private Object payload;

    private String updateTimestamp;

    private String updateBy;

    public AlEventData(String projectName) {
        this.projectName = projectName;
    }
}
