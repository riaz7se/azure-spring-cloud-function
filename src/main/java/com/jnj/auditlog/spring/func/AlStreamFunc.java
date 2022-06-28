package com.jnj.auditlog.spring.func;

import com.jnj.auditlog.spring.model.AlEventData;
import com.jnj.auditlog.spring.repo.AlCosmosDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Slf4j
@Component
public class AlStreamFunc implements Function<Mono<AlEventData>, Flux<AlEventData>> {

    private AlCosmosDao alCosmosDao;

    @Autowired
    public AlStreamFunc(AlCosmosDao alCosmosDao) {
        this.alCosmosDao = alCosmosDao;
    }

    public Flux<AlEventData> apply(Mono<AlEventData> mono) {
        AlEventData alDbData = AlEventData.builder().build();
        mono.subscribe(auditLogData -> BeanUtils.copyProperties(auditLogData, alDbData));
        log.info("Project name Payload:::: ", alDbData.getPayload());
        Flux<AlEventData> alResults = alCosmosDao.getAlStream_byNestedJson(alDbData);
        log.info("Al Flux Results:::: ", alResults);
        return alResults;
    }
}