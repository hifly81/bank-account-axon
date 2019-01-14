package org.hifly.axon.bank.account.kafka.axon;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.extensions.kafka.eventhandling.KafkaMessageConverter;
import org.axonframework.extensions.kafka.eventhandling.consumer.*;
import org.axonframework.extensions.kafka.eventhandling.producer.DefaultProducerFactory;
import org.axonframework.extensions.kafka.eventhandling.producer.KafkaPublisher;
import org.axonframework.extensions.kafka.eventhandling.producer.ProducerFactory;
import org.axonframework.serialization.xml.XStreamSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.axonframework.extensions.kafka.eventhandling.producer.ConfirmationMode.WAIT_FOR_ACK;

public class AxonKafkaConfig {

    public static KafkaPublisher<String, byte[]> createPublisher(
            EventBus eventBus,
            String topicName) {
        ProducerFactory<String, byte[]> producerFactory = DefaultProducerFactory.<String, byte[]>builder()
                .closeTimeout(1000, MILLISECONDS)
                .configuration(kafkaConfig(ByteArraySerializer.class))
                .confirmationMode(WAIT_FOR_ACK)
                .build();

        KafkaPublisher<String, byte[]> publisher = KafkaPublisher.<String, byte[]>builder()
                .messageSource(eventBus)
                .producerFactory(producerFactory)
                .topic(topicName)
                .build();

        return publisher;
    }

    public static KafkaMessageSource createMessageSource(String topicName) {
        ConsumerFactory<String, byte[]> consumerFactory =
                new DefaultConsumerFactory<>(kafkaConfigConsumer( "consumer1", ByteArrayDeserializer.class));

        KafkaMessageConverter messageConverter = AgnosticKafkaMessageConverter.builder().serializer(XStreamSerializer.builder().build()).build();


        Fetcher fetcher = AsyncFetcher.<String, byte[]>builder()
                .consumerFactory(consumerFactory)
                .topic(topicName)
                .pollTimeout(300, TimeUnit.MILLISECONDS)
                .messageConverter(messageConverter)
                .build();
        KafkaMessageSource messageSource = new KafkaMessageSource(fetcher);

        return messageSource;
    }

    private static Map<String, Object> kafkaConfig(Class valueSerializer) {
        Map<String, Object> config = new HashMap<>();
        //FIXME broker address list
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092, localhost:9093, localhost:9094");
        config.put(ProducerConfig.RETRIES_CONFIG, 0);
        config.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        config.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        config.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);
        return config;
    }


    private static Map<String, Object> kafkaConfigConsumer(String groupName, Class valueDeserializer) {
        Map<String, Object> config = new HashMap<>();
        //FIXME broker address list
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092, localhost:9093, localhost:9094");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializer);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupName);
        return config;
    }

}
