package com.twfhclife.autoreport.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.Request;
import org.opensearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class OpenSearchIndexInitializer implements ApplicationRunner {

    static final String EXCEPTION_SETTING_INDEX = "exception_setting";
    static final String HOLIDAY_SETTING_INDEX = "holiday_setting";
    static final String CONFIG_DICTIONARY_INDEX = "config_dictionary";
    static final String EXCEPTION_PROCESS_RECORD_INDEX = "exception_process_record";
    static final String EXCEPTION_EVENT_INDEX = "exception_event";
    static final String EXCEPTION_ATTACHMENT_INDEX = "exception_attachment";
    static final String BIZ_ESERVICE_JD_OPERATION_INDEX = "biz_eservice_jd_operation";

    private static final String EXCEPTION_SETTING_MAPPING = """
            {
              "mappings": {
                "properties": {
                  "systemType":  { "type": "keyword" },
                  "eventType":   { "type": "keyword" },
                  "autoProcess": { "type": "keyword" },
                  "riskLevel":   { "type": "keyword" },
                  "autoClose":   { "type": "keyword" },
                  "createdAt":   { "type": "date" },
                  "updatedAt":   { "type": "date" }
                }
              }
            }
            """;

    private static final String HOLIDAY_SETTING_MAPPING = """
            {
              "mappings": {
                "properties": {
                  "holidayDate": { "type": "date", "format": "yyyy-MM-dd" },
                  "remark":      { "type": "keyword" },
                  "createdAt":   { "type": "date" },
                  "updatedAt":   { "type": "date" }
                }
              }
            }
            """;

    private static final String CONFIG_DICTIONARY_MAPPING = """
            {
              "mappings": {
                "properties": {
                  "type":  { "type": "text", "fields": { "keyword": { "type": "keyword", "ignore_above": 256 } } },
                  "code":  { "type": "text", "fields": { "keyword": { "type": "keyword", "ignore_above": 256 } } },
                  "label": { "type": "text", "fields": { "keyword": { "type": "keyword", "ignore_above": 256 } } },
                  "sort":  { "type": "integer" }
                }
              }
            }
            """;

    private static final String OPERATION_LOG_MAPPING = """
            {
              "mappings": {
                "properties": {
                  "systemType":   { "type": "keyword" },
                  "account":      { "type": "keyword" },
                  "operatedAt":   { "type": "long" },
                  "functionName": { "type": "keyword" },
                  "action":       { "type": "keyword" },
                  "content":      { "type": "keyword" },
                  "result":       { "type": "keyword" }
                }
              }
            }
            """;

    private static final String EXCEPTION_EVENT_MAPPING = """
            {
              "mappings": {
                "properties": {
                  "eventNo":     { "type": "keyword" },
                  "occurredAt":  { "type": "long" },
                  "eventType":   { "type": "keyword" },
                  "riskLevel":   { "type": "keyword" },
                  "systemType":  { "type": "keyword" },
                  "bizName":     { "type": "keyword" },
                  "account":     { "type": "keyword" },
                  "name":        { "type": "keyword" },
                  "channelBranch": { "type": "keyword" },
                  "deviceInfo":  { "type": "keyword" },
                  "sourceIp":    { "type": "keyword" },
                  "description": { "type": "keyword" },
                  "detailDescription": { "type": "keyword" },
                  "status":      { "type": "keyword" },
                  "processedBy":   { "type": "keyword" },
                  "processMethod": { "type": "keyword" },
                  "riskAssessment": { "type": "keyword" },
                  "processRemark": { "type": "keyword" },
                  "processedAt":   { "type": "long" },
                  "relatedLogIds": { "type": "keyword" },
                  "createdAt":   { "type": "long" }
                }
              }
            }
            """;

    private static final String EXCEPTION_ATTACHMENT_MAPPING = """
            {
              "mappings": {
                "properties": {
                  "eventId":     { "type": "keyword" },
                  "fileName":    { "type": "keyword" },
                  "contentType": { "type": "keyword" },
                  "size":        { "type": "long" },
                  "content":     { "type": "binary" },
                  "uploadedBy":  { "type": "keyword" },
                  "uploadedAt":  { "type": "long" }
                }
              }
            }
            """;

    private static final String EXCEPTION_PROCESS_RECORD_MAPPING = """
            {
              "mappings": {
                "properties": {
                  "eventId":       { "type": "keyword" },
                  "action":        { "type": "keyword" },
                  "status":        { "type": "keyword" },
                  "processedBy":   { "type": "keyword" },
                  "processMethod": { "type": "keyword" },
                  "riskAssessment": { "type": "keyword" },
                  "processRemark": { "type": "keyword" },
                  "processedAt":   { "type": "long" }
                }
              }
            }
            """;

    private static final String BIZ_ESERVICE_JD_OPERATION_MAPPING = """
            {
              "mappings": {
                "properties": {
                  "account":        { "type": "keyword" },
                  "operationTime":  { "type": "keyword" },
                  "functionScreen": { "type": "keyword" },
                  "action":         { "type": "keyword" },
                  "actionResult":   { "type": "keyword" },
                  "actionContent":  { "type": "keyword" }
                }
              }
            }
            """;

    private final RestClient client;

    @Value("${app.opensearch.auto-create-index:true}")
    private boolean autoCreateIndex;

    @Override
    public void run(ApplicationArguments args) {
        if (!autoCreateIndex) {
            log.info("OpenSearch 索引自動建立已關閉（auto-create-index=false）");
            return;
        }
        try {
            createIndexIfAbsent(EXCEPTION_SETTING_INDEX, EXCEPTION_SETTING_MAPPING);
            createIndexIfAbsent(HOLIDAY_SETTING_INDEX, HOLIDAY_SETTING_MAPPING);
            createIndexIfAbsent(CONFIG_DICTIONARY_INDEX, CONFIG_DICTIONARY_MAPPING);
            createIndexIfAbsent(EXCEPTION_PROCESS_RECORD_INDEX, EXCEPTION_PROCESS_RECORD_MAPPING);
            createIndexIfAbsent(EXCEPTION_EVENT_INDEX, EXCEPTION_EVENT_MAPPING);
            createIndexIfAbsent(EXCEPTION_ATTACHMENT_INDEX, EXCEPTION_ATTACHMENT_MAPPING);
            createIndexIfAbsent(BIZ_ESERVICE_JD_OPERATION_INDEX, BIZ_ESERVICE_JD_OPERATION_MAPPING);
        } catch (Exception e) {
            log.warn("OpenSearch 索引初始化失敗（服務未啟動？）: {}", e.getMessage());
        }
    }

    private void createIndexIfAbsent(String index, String mapping) throws Exception {
        if (indexExists(index)) {
            log.info("OpenSearch 索引已存在: {}", index);
            return;
        }
        Request request = new Request("PUT", "/" + index);
        request.setJsonEntity(mapping);
        client.performRequest(request);
        log.info("OpenSearch 索引已建立: {}", index);
    }

    private boolean indexExists(String index) {
        try {
            client.performRequest(new Request("HEAD", "/" + index));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
