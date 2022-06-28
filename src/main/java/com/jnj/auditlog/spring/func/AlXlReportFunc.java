package com.jnj.auditlog.spring.func;

import com.jnj.auditlog.spring.model.AlEventData;
import com.jnj.auditlog.spring.report.XlReportGen;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Slf4j
@Component
public class AlXlReportFunc implements Function<AlEventData, ByteArrayResource> {

    private XlReportGen alCosmosDao;

    @Autowired
    public AlXlReportFunc(XlReportGen alCosmosDao) {
        this.alCosmosDao = alCosmosDao;
    }

    public ByteArrayResource apply(AlEventData alReqData) {
        log.info("Project name Payload:::: ", alReqData.getPayload());
        return alCosmosDao.generateReport(alReqData);
    }
}