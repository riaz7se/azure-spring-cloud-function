package com.jnj.auditlog.spring.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TableTypePayload {

    public final static Set<String> KEYS = new HashSet() {{
        add("tableName");
        add("dbColumnName");
    }};

    private String tableName;
    private int changedRowId;
    private String action;
    private String changedValue;
    private String dbColumnName;
    private String actionPerformedBy;
    private String actionPerformedByRole;
    private String actionPerformedOn;

}
