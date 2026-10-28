package com.twfhclife.autocollecting.service;

import com.twfhclife.autocollecting.config.CollectProperties;
import com.twfhclife.autocollecting.config.CollectProperties.LogSource;
import com.twfhclife.autocollecting.config.OtelProperties;
import com.twfhclife.autocollecting.config.TelemetryProperties;
import com.twfhclife.autocollecting.kafka.TopicResolver;
import com.twfhclife.autocollecting.listener.KafkaTailerListener;
import com.twfhclife.autocollecting.otlp.OtelLogEmitter;
import com.twfhclife.autocollecting.processor.TelemetryProcessor;
import org.apache.commons.io.input.Tailer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@EnableConfigurationProperties({CollectProperties.class, TelemetryProperties.class, OtelProperties.class})
public class LogTailerService implements InitializingBean, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(LogTailerService.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final TopicResolver topicResolver;
    private final CollectProperties properties;
    private final TelemetryProcessor telemetryProcessor;
    private final OtelLogEmitter otelLogEmitter;
    private final List<Tailer> tailers = new CopyOnWriteArrayList<>();

    public LogTailerService(KafkaTemplate<String, String> kafkaTemplate,
                            TopicResolver topicResolver,
                            CollectProperties properties,
                            TelemetryProcessor telemetryProcessor,
                            OtelLogEmitter otelLogEmitter) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicResolver = topicResolver;
        this.properties = properties;
        this.telemetryProcessor = telemetryProcessor;
        this.otelLogEmitter = otelLogEmitter;
    }

    @Override
    public void afterPropertiesSet() {
        String host = resolveHost();
        if (properties.getSources().isEmpty()) {
            log.warn("No log sources configured (app.collect.sources), nothing to collect");
            return;
        }
        for (LogSource source : properties.getSources()) {
            startTailer(source, host);
        }
    }

    private void startTailer(LogSource source, String host) {
        File file = new File(source.getPath());
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            log.error("Failed to create log directory for [{}]: {}", source.getName(), parent);
            return;
        }
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    log.warn("Log file already exists: {}", file.getPath());
                }
            } catch (Exception e) {
                log.error("Failed to create log file [{}]: {}", file.getPath(), e.getMessage());
                return;
            }
        }

        KafkaTailerListener listener =
                new KafkaTailerListener(kafkaTemplate, topicResolver, host, source,
                        telemetryProcessor, otelLogEmitter);
        Tailer tailer = new Tailer(file, listener,
                properties.getTailDelayMs(), !source.isFromBeginning(), false);
        Thread thread = new Thread(tailer, "log-tailer-" + source.getName());
        thread.start();
        tailers.add(tailer);
        log.info("Started tailing [{}] -> Kafka topics (bizName-routed), key={}",
                file.getPath(), source.getName());
    }

    private static String resolveHost() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }

    @Override
    public void destroy() {
        for (Tailer tailer : tailers) {
            try {
                tailer.stop();
            } catch (Exception e) {
                log.warn("Failed to stop tailer: {}", e.getMessage());
            }
        }
        tailers.clear();
        log.info("Stopped all log tailers");
    }
}