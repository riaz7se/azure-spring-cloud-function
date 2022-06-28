package com.jnj.auditlog.spring.report;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jnj.auditlog.spring.model.AlEventData;
import com.jnj.auditlog.spring.model.TableTypePayload;
import com.jnj.auditlog.spring.repo.AlCosmosDao;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@Qualifier("excelReportServiceImpl")
public class XlReportGen {

    @Autowired
    private ObjectMapper objectMapper;

    private AlCosmosDao alCosmosDao;

    @Autowired
    public XlReportGen(AlCosmosDao alCosmosDao) {
        this.alCosmosDao = alCosmosDao;
    }

    /**
     * Working....
     *
     * @throws Exception
     */
    @SneakyThrows
    public ByteArrayResource generateReport(AlEventData alReqData) {

        log.info("Generate Report......");
        final List<AlEventData> alEventsList = alCosmosDao.getAlByAppNameAndPath(alReqData);
        log.info("Documents List......{}: ",alEventsList.size());

        if (alEventsList.size() > 0) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            Workbook workbook = new XSSFWorkbook();
            //craete 1st sheet
            createMetadataSheet(alEventsList, workbook);

            //create payload sheet
            createPayloadSheet(alEventsList, workbook);
            workbook.write(stream);
            workbook.close();

            byte[] xlByteArray = stream.toByteArray();
            log.info("Xl Byte Array::"+xlByteArray.length);
            return new ByteArrayResource(xlByteArray);
        }
        return new ByteArrayResource(new byte[0]);

    }


    private void createPayloadSheet(final List<AlEventData> alEventsList, Workbook workbook) throws JsonProcessingException {
        Sheet sheet = workbook.createSheet("Table");
        sheet.setColumnWidth(0,6000);
        Row rowHeader = sheet.createRow(0);

        String jsonStrForHeaderKeys = objectMapper.writeValueAsString(alEventsList.get(0).getPayload());
        TableTypePayload tableTypePayload_ForHeader = objectMapper.readValue(objectMapper.writeValueAsString(alEventsList.get(0).getPayload()), TableTypePayload.class);

        setHeaderCells(objectMapper.writeValueAsString(tableTypePayload_ForHeader), workbook, rowHeader);

        AtomicInteger ai = new AtomicInteger(1);
        alEventsList.stream().forEach(al -> {
            Row row = sheet.createRow(ai.getAndIncrement());
            AtomicInteger ci = new AtomicInteger(0);

            CellStyle cellStyle = workbook.createCellStyle();
            XSSFFont font = createFont((XSSFWorkbook) workbook, 11);
            cellStyle.setFont(font);
//            cellStyle.setWrapText(true);
            cellStyle.setAlignment(HorizontalAlignment.CENTER);

            try {
                Map<String, Object> payload = objectMapper.readValue(objectMapper.writeValueAsString(al.getPayload()), LinkedHashMap.class);
                if (payload.keySet().containsAll(TableTypePayload.KEYS)) {
                    TableTypePayload tableTypePayload = objectMapper.readValue(objectMapper.writeValueAsString(payload), TableTypePayload.class);
                    payload = objectMapper.readValue(objectMapper.writeValueAsString(tableTypePayload), Map.class);
                }
                payload.values().stream().forEach(jsonValues -> {
                    if (!(jsonValues instanceof Map)){
                        Cell cell1 = row.createCell(ci.getAndIncrement());
                        setColumnCell(cell1, cellStyle, jsonValues);
                    }
                });

            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void createMetadataSheet(final List<AlEventData> alEventsList, Workbook workbook) throws JsonProcessingException {
        Sheet sheet = workbook.createSheet("AuditLog");
        Row rowHeader = sheet.createRow(0);

        String jsonStrForHeaderKeys = objectMapper.writeValueAsString(alEventsList.get(0));
        setHeaderCells(jsonStrForHeaderKeys, workbook, rowHeader);

        AtomicInteger ai = new AtomicInteger(1);
        alEventsList.stream().forEach(al -> {
            Row row = sheet.createRow(ai.getAndIncrement());
            AtomicInteger ci = new AtomicInteger(0);

            CellStyle style = workbook.createCellStyle();
            style.setWrapText(true);

            try {
                Map<String, Object> alObjeMap = objectMapper.readValue(objectMapper.writeValueAsString(al), Map.class);
                alObjeMap.values().stream().forEach(jsonValues -> {
                    if (!(jsonValues instanceof Map)){
                        Cell cell1 = row.createCell(ci.getAndIncrement());
                        setColumnCell(cell1, style, jsonValues);
                    }
                });
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void setHeaderCells(String jsonStrForHeaderKeys, Workbook workbook, Row rowHeader) throws JsonProcessingException {
        CellStyle headerStyle = workbook.createCellStyle();
//        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
//        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        XSSFFont font = createFont((XSSFWorkbook) workbook, 11);
        font.setBold(true);
        headerStyle.setFont(font);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        Map<String, Object> keysAsHeaderMap = objectMapper.readValue(jsonStrForHeaderKeys, Map.class);
        Set<String> keysForHeader = keysAsHeaderMap.keySet();

        AtomicInteger cellIndex = new AtomicInteger(0);
        keysAsHeaderMap.entrySet().stream().forEach(entrySet -> {
            if (!(entrySet.getValue() instanceof Map)) {
                Cell headerCell = rowHeader.createCell(cellIndex.getAndIncrement());
                setHeader(headerCell, headerStyle, this.camelCaseToTitleCase(entrySet.getKey()));
            }
        });
    }

    private XSSFFont createFont(XSSFWorkbook workbook, int x) {
        XSSFFont font = workbook.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) x);
        return font;
    }


//    public Flux<AlEventData> createDocument(String projectName, Map<String, Object> nestedJsonPath) {
//        return Flux.generate(this::getWorkbookSheet, (workBook, sink) -> {
//                    sink.next(extracted(workBook););
//            return sheet;
//        }, this::closeWorkbook);
//    }

    private <S> void closeWorkbook(S s) {
    }

    private void setColumnCell(Cell currCell, CellStyle style, Object payloadValues) {
        String colValue = "";
        if (payloadValues instanceof Collection) {
            colValue = StringUtils.join(payloadValues, ",");
        } else {
            colValue = ObjectUtils.isEmpty(payloadValues) ? "" : String.valueOf(payloadValues);
        }

        currCell.setCellValue(colValue);
        currCell.setCellStyle(style);

    }

    private void setHeader(Cell headerCell, CellStyle headerStyle, String headerName) {
        headerCell.setCellValue(headerName);
        headerCell.setCellStyle(headerStyle);
    }

    public String camelCaseToTitleCase(String s){
        StringBuilder name = new StringBuilder();
        for (String w : s.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")) {
            name.append(StringUtils.capitalize(w)+" ");
        }
        return name.toString();
    }
}
