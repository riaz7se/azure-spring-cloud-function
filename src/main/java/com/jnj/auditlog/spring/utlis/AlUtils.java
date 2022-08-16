package com.jnj.auditlog.spring.utlis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jnj.auditlog.spring.model.AlContainerData;
import com.jnj.auditlog.spring.model.AlEventData;
import com.microsoft.azure.documentdb.Document;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.BeanUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.ObjectUtils;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
public class AlUtils {

    public static AlEventData containerToEntity(AlContainerData alDataContainer) {
        log.info("AlContainer:::: {} ", alDataContainer);
        AlEventData alEventData = new AlEventData();
        BeanUtils.copyProperties(alDataContainer, alEventData);

        log.info("AlEve::: {} ", alEventData);
        return alEventData;
    }


    public static AlContainerData entityToContainer(AlEventData alEventData) {
        AlContainerData alDataContainer = new AlContainerData();
        BeanUtils.copyProperties(alEventData, alDataContainer);
        alDataContainer.setUpdateBy(getUserNameFromContext());
        return alDataContainer;
    }

    public static AlEventData documentToEntity(Document d) throws JsonProcessingException {
        AlEventData alData = new AlEventData();
        alData.setAppName(d.getString(ContainerConstants.APPNAME));
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> payloadMap = objectMapper.readValue(JSONObject.valueToString(d.getObject("payload")), LinkedHashMap.class);
        alData.setPayload(payloadMap);
        alData.setInfoType(d.getString(ContainerConstants.INFO_TYPE));
        alData.setOperation(d.getString(ContainerConstants.OPERATION));
        alData.setProjectName(d.getString(ContainerConstants.PROJECT_NAME));

        return alData;
    }

    public static boolean isValidateJsonSearchPath(Map<String, Object> searchMap) {
        HashSet<String> validJsonKeys = new HashSet() {{
            add("appName");
            add("projectName");
            add("infoType");
            add("operation");
            add("updateBy");
            add("payload");
        }};
        Set<String> searchKeySet = searchMap.keySet();
        searchKeySet.removeAll(validJsonKeys);
        return searchKeySet.size() > 0 ? false : true;
    }

    public static String getUserNameFromContext() {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (ObjectUtils.isEmpty(auth) || ObjectUtils.isEmpty(auth.getPrincipal())) {
            throw new BadCredentialsException("Authentication or Principal is null");
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails)principal).getUsername();
        }
        return String.valueOf(principal);
//        return "test-user";
    }
    public static String camelCaseToTitleCase(String s){
        StringBuilder name = new StringBuilder();
        for (String w : s.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")) {
            name.append(StringUtils.capitalize(w)+" ");
        }
        return name.toString();
    }


}
