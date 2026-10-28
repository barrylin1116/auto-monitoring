package com.twfhclife.autodatastream.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twfhclife.autodatastream.converter.BizDocumentConverterRegistry;
import com.twfhclife.autodatastream.handler.CommonLogRuleHandler;
import com.twfhclife.autodatastream.model.LogEntry;
import com.twfhclife.autodatastream.repository.OpenSearchLogWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;


@Component
public class LogConsumerListener {

    private static final Logger log = LoggerFactory.getLogger(LogConsumerListener.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final MessageIdDeduplicator deduplicator;
    private final BizDocumentConverterRegistry converter;
    private final OpenSearchLogWriter openSearchWriter;
    private final CommonLogRuleHandler commonLogRuleHandler;

    public LogConsumerListener(MessageIdDeduplicator deduplicator,
                               BizDocumentConverterRegistry converter,
                               OpenSearchLogWriter openSearchWriter,
                               CommonLogRuleHandler commonLogRuleHandler) {
        this.deduplicator = deduplicator;
        this.converter = converter;
        this.openSearchWriter = openSearchWriter;
        this.commonLogRuleHandler = commonLogRuleHandler;
    }

    @KafkaListener(topicPattern = "${app.topics.pattern}", groupId = "${spring.kafka.consumer.group-id}")
    public void onLogMessage(@Payload String message, Acknowledgment acknowledgment) {
        try {
            LogEntry entry = OtelLogDecoder.decode(message, objectMapper);
            if (entry == null) {
                log.warn("Skipped blank/unparseable log message");
                return;
            }
            if (deduplicator.isDuplicate(entry.getId())) {
                log.debug("Skipped duplicate log message id [{}]", entry.getId());
                return;
            }
            writeToOpenSearch(entry);
            commonLogRuleHandler.handle(entry);
        } catch (Exception e) {
            log.error("Failed to process log message [{}]", message, e);
        } finally {
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
        }
    }

    
    private void writeToOpenSearch(LogEntry entry) {
        if (entry.getBizName() == null || entry.getBizName().isBlank()) {
            log.debug("Blank bizName, skip OpenSearch write");
            return;
        }
        try {
            openSearchWriter.write(entry.getBizName(), entry.getId(), converter.convert(entry));
        } catch (Exception e) {
            log.error("Failed to write log entry to OpenSearch index [biz_{}]",
                    entry.getBizName(), e);
        }
    }
}
