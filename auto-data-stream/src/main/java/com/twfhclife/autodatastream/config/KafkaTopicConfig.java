package com.twfhclife.autodatastream.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;


@Configuration
@EnableKafka
public class KafkaTopicConfig {

    private static final String TOPIC_SUFFIX = "_collecting_logs";

    @Bean
    public NewTopic misfLoginCollectingLogsTopic() {
        return TopicBuilder.name(DroolsConfig.BIZ_MISF_LOGIN + TOPIC_SUFFIX)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
