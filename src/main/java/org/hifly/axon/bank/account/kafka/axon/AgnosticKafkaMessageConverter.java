package org.hifly.axon.bank.account.kafka.axon;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.axonframework.common.AxonConfigurationException;
import org.axonframework.common.BuilderUtils;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.GenericDomainEventMessage;
import org.axonframework.eventhandling.GenericEventMessage;
import org.axonframework.eventhandling.async.SequencingPolicy;
import org.axonframework.eventhandling.async.SequentialPerAggregatePolicy;
import org.axonframework.extensions.kafka.eventhandling.HeaderUtils;
import org.axonframework.extensions.kafka.eventhandling.KafkaMessageConverter;
import org.axonframework.messaging.MetaData;
import org.axonframework.serialization.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiFunction;

public class AgnosticKafkaMessageConverter implements KafkaMessageConverter<String, byte[]> {

    private static final Logger logger = LoggerFactory.getLogger(KafkaMessageConverter.class);
    private final Serializer serializer;
    private final SequencingPolicy<? super EventMessage<?>> sequencingPolicy;
    private final BiFunction<String, Object, RecordHeader> headerValueMapper;

    protected AgnosticKafkaMessageConverter(AgnosticKafkaMessageConverter.Builder builder) {
        builder.validate();
        this.serializer = builder.serializer;
        this.sequencingPolicy = builder.sequencingPolicy;
        this.headerValueMapper = builder.headerValueMapper;
    }

    public static AgnosticKafkaMessageConverter.Builder builder() {
        return new AgnosticKafkaMessageConverter.Builder();
    }

    public ProducerRecord<String, byte[]> createKafkaMessage(EventMessage<?> eventMessage, String topic) {
        SerializedObject<byte[]> serializedObject = eventMessage.serializePayload(this.serializer, byte[].class);
        byte[] payload = (byte[]) serializedObject.getData();
        return new ProducerRecord(topic, (Integer) null, (Long) null, this.key(eventMessage), payload, HeaderUtils.toHeaders(eventMessage, serializedObject, this.headerValueMapper));
    }

    private String key(EventMessage<?> eventMessage) {
        Object identifier = this.sequencingPolicy.getSequenceIdentifierFor(eventMessage);
        return identifier != null ? identifier.toString() : null;
    }

    public Optional<EventMessage<?>> readKafkaMessage(ConsumerRecord<String, byte[]> consumerRecord) {
        try {
            Headers headers = consumerRecord.headers();
            if(HeaderUtils.value(headers,"axon-message-type" ) == null) {
                if(HeaderUtils.value(headers,"message-type" ) == null) {
                    throw new IllegalStateException("Can't parse a message without a message-type");
                } else {
                    headers.add("axon-message-type", HeaderUtils.valueAsString(headers, "message-type").getBytes());
                }
            }
            byte[] messageBody = (byte[]) consumerRecord.value();
            SerializedMessage<?> message = this.extractSerializedMessage(headers, messageBody);
            Optional<EventMessage<?>> optionalEventMessage =  this.buildMessage(headers, message);
            return optionalEventMessage;

        } catch (Exception var5) {
            logger.trace("Error converting {} to axon", consumerRecord, var5);
        }

        return Optional.empty();
    }

    private Optional<EventMessage<?>> buildMessage(Headers headers, SerializedMessage<?> message) {
        if(HeaderUtils.value(headers,"axon-message-timestamp" ) == null) {
            headers.add("axon-message-timestamp", Instant.now().toString().getBytes());
        }
        long timestamp = HeaderUtils.valueAsLong(headers, "axon-message-timestamp");
        if( headers.lastHeader("axon-message-aggregate-id") != null) {
            return this.domainEvent(headers, message, timestamp);
        } else {
            Optional<EventMessage<?>> eventMessage = this.event(message, timestamp);
            return  eventMessage;
        }

    }

    private SerializedMessage<?> extractSerializedMessage(Headers headers, byte[] messageBody) {
        SimpleSerializedObject<byte[]> serializedObject = new SimpleSerializedObject(messageBody, byte[].class, HeaderUtils.valueAsString(headers, "axon-message-type"), HeaderUtils.valueAsString(headers, "axon-message-revision", (String) null));
        return new SerializedMessage(HeaderUtils.valueAsString(headers, "axon-message-id"), new LazyDeserializingObject(serializedObject, this.serializer), new LazyDeserializingObject(MetaData.from(HeaderUtils.extractAxonMetadata(headers))));
    }

    private boolean isAxonMessage(Headers headers) {
        return HeaderUtils.keys(headers).containsAll(Arrays.asList("axon-message-id", "axon-message-type"));
    }

    private Optional<EventMessage<?>> domainEvent(Headers headers, SerializedMessage<?> message, long timestamp) {
        return Optional.of(new GenericDomainEventMessage(HeaderUtils.valueAsString(headers, "axon-message-aggregate-type"), HeaderUtils.valueAsString(headers, "axon-message-aggregate-id"), HeaderUtils.valueAsLong(headers, "axon-message-aggregate-seq"), message, () -> {
            return Instant.ofEpochMilli(timestamp);
        }));
    }

    private Optional<EventMessage<?>> event(SerializedMessage<?> message, long timestamp) {
        return Optional.of(new GenericEventMessage(message, () -> {
            return Instant.ofEpochMilli(timestamp);
        }));
    }

    public static class Builder {
        private Serializer serializer;
        private SequencingPolicy<? super EventMessage<?>> sequencingPolicy = SequentialPerAggregatePolicy.instance();
        private BiFunction<String, Object, RecordHeader> headerValueMapper = HeaderUtils.byteMapper();

        public Builder() {
        }

        public AgnosticKafkaMessageConverter.Builder serializer(Serializer serializer) {
            BuilderUtils.assertNonNull(serializer, "Serializer may not be null");
            this.serializer = serializer;
            return this;
        }

        public AgnosticKafkaMessageConverter.Builder sequencingPolicy(SequencingPolicy<? super EventMessage<?>> sequencingPolicy) {
            BuilderUtils.assertNonNull(sequencingPolicy, "SequencingPolicy may not be null");
            this.sequencingPolicy = sequencingPolicy;
            return this;
        }

        public AgnosticKafkaMessageConverter.Builder headerValueMapper(BiFunction<String, Object, RecordHeader> headerValueMapper) {
            BuilderUtils.assertNonNull(headerValueMapper, "{} may not be null");
            this.headerValueMapper = headerValueMapper;
            return this;
        }

        public AgnosticKafkaMessageConverter build() {
            return new AgnosticKafkaMessageConverter(this);
        }

        protected void validate() throws AxonConfigurationException {
            BuilderUtils.assertNonNull(this.serializer, "The Serializer is a hard requirement and should be provided");
        }
    }


}
