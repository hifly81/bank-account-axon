package org.hifly.axon.bank.account;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.hifly.axon.bank.account.kafka.producer.BaseProducer;
import org.hifly.axon.bank.account.kafka.producer.RecordMetadataUtil;

public class CloseAccountApp {

    public static void main (String [] args) throws Exception {
        BaseProducer baseProducer = new BaseProducer();
        baseProducer.start(null);
        bunchOfSynchMessages("axon-test", baseProducer);
    }

    public static void bunchOfSynchMessages(String topic, BaseProducer baseProducer) {
        RecordMetadata lastRecord = null;
        for (int i= 0; i < 1; i++ ) {
            String toSend = "<org.hifly.axon.bank.account.event.AccountClosedEvent><accountId>A1</accountId><customerName>kermit the frog</customerName></org.hifly.axon.bank.account.event.AccountClosedEvent>";
            ProducerRecord producerRecord = new ProducerRecord<>(topic, toSend);
            producerRecord.headers().add("message-type" , new String("org.hifly.axon.bank.account.event.AccountClosedEvent").getBytes());
            lastRecord = baseProducer.produceSync(producerRecord);
        }
        RecordMetadataUtil.prettyPrinter(lastRecord);
        
    }
}
