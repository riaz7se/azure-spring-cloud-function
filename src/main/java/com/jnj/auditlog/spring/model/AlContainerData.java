package com.jnj.auditlog.spring.model;

import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.GeneratedValue;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Container(containerName = "auditlog", partitionKeyPath = "/projectName")
@JsonIgnoreProperties(ignoreUnknown = true)
public class AlContainerData {

    public static final String PARTITION_KEY = "/projectName";

    @PartitionKey
    private String projectName;

    @Id
    @GeneratedValue
    private String auditId;

    private String appName;

    private String infoType;

    private String operation;

    private String appKey;

    private String requestId;

    private Map<String, Object> payload;

    private String updateTimestamp = String.valueOf(LocalDateTime.now());

    private String updateBy;

}
