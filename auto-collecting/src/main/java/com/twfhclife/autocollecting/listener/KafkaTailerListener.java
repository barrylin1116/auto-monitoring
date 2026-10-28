package com.twfhclife.autocollecting.listener;

import com.twfhclife.autocollecting.config.CollectProperties.LogSource;
import com.twfhclife.autocollecting.kafka.TopicResolver;
import com.twfhclife.autocollecting.model.LogEntry;
import com.twfhclife.autocollecting.otlp.OtelLogEmitter;
import com.twfhclife.autocollecting.otlp.OtlpJsonBuilder;
import com.twfhclife.autocollecting.parser.ConfigurableLogMapper;
import com.twfhclife.autocollecting.parser.LogLineParser;
import com.twfhclife.autocollecting.parser.LogLineParser.ParsedLog;
import com.twfhclife.autocollecting.processor.TelemetryProcessor;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

public class KafkaTailerListener extends TailerListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(KafkaTailerListener.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final TopicResolver topicResolver;
    private final String host;
    private final LogSource source;
    private final TelemetryProcessor telemetryProcessor;
    private final OtelLogEmitter otelLogEmitter;

    public KafkaTailerListener(KafkaTemplate<String, String> kafkaTemplate,
                               TopicResolver topicResolver, String host, LogSource source,
                               TelemetryProcessor telemetryProcessor,
                               OtelLogEmitter otelLogEmitter) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicResolver = topicResolver;
        this.host = host;
        this.source = source;
        this.telemetryProcessor = telemetryProcessor;
        this.otelLogEmitter = otelLogEmitter;
    }

    @Override
    public void handle(String line) {
        if (line == null || line.isEmpty()) {
            return;
        }
        try {
            ParsedLog parsed = LogLineParser.parse(line, source);
            if (parsed == null || parsed.getBizName() == null) {
                return;
            }

            LogEntry entry = new LogEntry();
            entry.setHost(host);
            entry.setSource(source.getName());
            entry.setFile(source.getPath());
            entry.setTimestamp(parsed.getRawTime());
            entry.setTimeMillis(parsed.getEpochMillis() > 0
                    ? parsed.getEpochMillis() : System.currentTimeMillis());
            entry.setLevel(parsed.getLevel() != null ? parsed.getLevel() : "UNKNOWN");
            entry.setThread(parsed.getThread() != null ? parsed.getThread() : "");
            entry.setLogger(parsed.getLogger() != null ? parsed.getLogger() : "");
            entry.setMessage(parsed.getMessage());
            entry.setBizName(parsed.getBizName());

            if (!telemetryProcessor.process(entry)) {
                return;
            }

            if (source.getBizName() != null && !source.getBizName().isBlank()) {
                entry.setBizName(source.getBizName());
            }

            Map<String, String> attributes;
            if (source.hasMapping()) {
                attributes = ConfigurableLogMapper.map(source, entry.getMessage(), entry.getBizName());
                if (attributes == null) {
                    return;
                }
            } else {
                attributes = new LinkedHashMap<>();
            }

            String request = OtlpJsonBuilder.build(entry, attributes);
            otelLogEmitter.emit(request);
            String topic = topicResolver.resolve(entry.getBizName());
            org.springframework.util.concurrent.ListenableFuture<org.springframework.kafka.support.SendResult<String, String>> future =
                    kafkaTemplate.send(topic, source.getName(), request);

            if (future == null) {
                log.error("Failed to enqueue log from [{}] to Kafka (send returned null)",
                        source.getName());
                return;
            }

            future.addCallback(
                    result -> {
                    },
                    error -> log.error("Failed to send log from [{}] to Kafka: {}",
                            source.getName(), error.getMessage()));

        } catch (Exception e) {
            log.error("Failed to handle log line from [{}]: {}", source.getName(), e.getMessage());
        }
    }

    @Override
    public void fileRotated() {
        log.info("Log file rotated, resume tailing: {}", source.getPath());
    }
}
