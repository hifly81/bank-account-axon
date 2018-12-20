package org.hifly.axon.bank.account.kafka.producer;

import java.util.Properties;

public class KafkaConfig {

    private static final String BROKER_LIST = "localhost:9092,localhost:9093,localhost:9094";

    public static Properties stringProducer() {
        Properties producerProperties = new Properties();
        producerProperties.put("bootstrap.servers", BROKER_LIST);
        producerProperties.put("max.block.ms", 15000);
        producerProperties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producerProperties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        return producerProperties;
    }
}
