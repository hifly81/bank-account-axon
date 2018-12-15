package org.hifly.quickstarts.bank.account.config;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.commandhandling.gateway.DefaultCommandGateway;
import org.axonframework.eventhandling.AnnotationEventHandlerAdapter;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.SimpleEventBus;
import org.axonframework.eventsourcing.EventSourcingRepository;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine;
import org.axonframework.modelling.command.AggregateAnnotationCommandHandler;
import org.axonframework.modelling.saga.AnnotatedSagaManager;
import org.axonframework.modelling.saga.repository.AnnotatedSagaRepository;
import org.axonframework.modelling.saga.repository.inmemory.InMemorySagaStore;
import org.hifly.quickstarts.bank.account.aggregator.AccountAggregate;
import org.hifly.quickstarts.bank.account.handler.AccountEventHandler;
import org.hifly.quickstarts.bank.account.saga.CloseAccountSaga;

import java.util.function.Supplier;

public class AxonConfig {

    public static EventBus createEventBus() {
        EventBus eventBus = SimpleEventBus.builder().build();
        return eventBus;
    }

    public static CommandBus createCommandBus() {
        CommandBus commandBus = SimpleCommandBus.builder().build();
        return commandBus;
    }

    public static CommandGateway createCommandGateway(CommandBus commandBus) {
        CommandGateway commandGateway = DefaultCommandGateway.builder()
                .commandBus(commandBus == null? createCommandBus(): commandBus)
                .build();
        return commandGateway;
    }

    public static EventStore createEventStore() {
        //in memory
        EventStore eventStore = EmbeddedEventStore.builder()
                .storageEngine(new InMemoryEventStorageEngine())
                .build();
        return eventStore;
    }

    public static <T> EventSourcingRepository<T> createEventSourceRepository(EventStore eventStore, Class<T> clazz) {
        EventSourcingRepository<T> repository = EventSourcingRepository.builder(clazz)
                .eventStore(eventStore)
                .build();
        return repository;
    }

    public static <T> AggregateAnnotationCommandHandler<T> createAggregatorHandler(EventSourcingRepository repository, Class<T> clazz) {
        AggregateAnnotationCommandHandler<T> aggregatorHandler =
                AggregateAnnotationCommandHandler.<T>builder()
                        .aggregateType(clazz)
                        .repository(repository)
                        .build();

        return aggregatorHandler;
    }

    public static <T> AnnotationEventHandlerAdapter createAnnotationEventHandler(T t) {
        return new AnnotationEventHandlerAdapter(t);
    }

    public static <T> AnnotatedSagaRepository<T> createAnnotatedSagaRepository(Class<T> clazz) {
        //in memory
        AnnotatedSagaRepository<T> sagaRepository = AnnotatedSagaRepository.<T>builder()
                .sagaStore(new InMemorySagaStore())
                .sagaType(clazz)
                .build();

        return sagaRepository;
    }

    public static <T> AnnotatedSagaManager<T> createAnnotatedSagaManager(
            Supplier<T> sagaSupplier,
            AnnotatedSagaRepository<T> annotatedSagaRepository,
            Class<T> clazz) {
        AnnotatedSagaManager<T> sagaManager =
                AnnotatedSagaManager.<T>builder()
                        .sagaRepository(annotatedSagaRepository)
                        .sagaType(clazz)
                        .sagaFactory(sagaSupplier)
                        .build();
        return sagaManager;
    }






}
