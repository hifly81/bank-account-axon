package org.hifly.axon.bank.account;

import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.SimpleEventBus;
import org.axonframework.extensions.kafka.eventhandling.producer.KafkaPublisher;
import org.axonframework.messaging.Message;
import org.axonframework.messaging.unitofwork.DefaultUnitOfWork;
import org.axonframework.messaging.unitofwork.UnitOfWork;
import org.hifly.axon.bank.account.config.AxonKafkaConfig;
import org.hifly.axon.bank.account.event.AccountClosedEvent;

import static org.axonframework.eventhandling.GenericEventMessage.asEventMessage;

public class ExternalApp {

    public static void main(String[] args) {
        EventBus eventBus = SimpleEventBus.builder().build();
        //kafka
        KafkaPublisher<String, byte[]> kafkaPublisher = AxonKafkaConfig.createPublisher(eventBus, "axon");
        kafkaPublisher.start();

        EventMessage<AccountClosedEvent> message = asEventMessage(new AccountClosedEvent("A1", "kermit the frog"));
        UnitOfWork<Message<AccountClosedEvent>> uow = DefaultUnitOfWork.startAndGet(message);
        try {
            eventBus.publish(message);
            uow.commit();
        } catch (Exception e) {
            uow.rollback(e);
        }

        kafkaPublisher.shutDown();

    }
}
