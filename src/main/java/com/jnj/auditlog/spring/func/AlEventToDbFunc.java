package com.jnj.auditlog.spring.func;

import com.jnj.auditlog.spring.model.AlEventData;
import com.jnj.auditlog.spring.repo.AlCosmosDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Slf4j
@Component
public class AlEventToDbFunc implements Function<AlEventData, String> {

    private AlCosmosDao alCosmosDao;

    @Autowired
    public AlEventToDbFunc(AlCosmosDao alCosmosDao) {
        this.alCosmosDao = alCosmosDao;
    }

    public String apply(AlEventData alEventData) {
        alCosmosDao.saveAlItem(alEventData);
        return "Audit Saved";
    }
}