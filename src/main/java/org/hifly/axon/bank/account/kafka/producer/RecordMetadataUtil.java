package org.hifly.axon.bank.account.kafka.producer;

import org.apache.kafka.clients.producer.RecordMetadata;

public class RecordMetadataUtil {

    public static void prettyPrinter(RecordMetadata recordMetadata) {
        if(recordMetadata != null) {
            System.out.printf("Topic: %s - Partition: %d - Offset: %d\n",
                    recordMetadata.topic(),
                    recordMetadata.partition(),
                    recordMetadata.offset());
        }
    }
}
