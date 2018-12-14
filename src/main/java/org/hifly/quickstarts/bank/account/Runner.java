package org.hifly.quickstarts.bank.account;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.commandhandling.gateway.DefaultCommandGateway;
import org.axonframework.eventhandling.AnnotationEventHandlerAdapter;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.SimpleEventBus;
import org.axonframework.eventsourcing.EventSourcingRepository;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine;
import org.axonframework.messaging.Message;
import org.axonframework.messaging.unitofwork.DefaultUnitOfWork;
import org.axonframework.messaging.unitofwork.UnitOfWork;
import org.axonframework.modelling.command.AggregateAnnotationCommandHandler;
import org.axonframework.modelling.saga.AnnotatedSagaManager;
import org.axonframework.modelling.saga.repository.AnnotatedSagaRepository;
import org.axonframework.modelling.saga.repository.inmemory.InMemorySagaStore;
import org.hifly.quickstarts.bank.account.aggregator.AccountAggregate;
import org.hifly.quickstarts.bank.account.command.CreateAccountCommand;
import org.hifly.quickstarts.bank.account.command.DepositAmountCommand;
import org.hifly.quickstarts.bank.account.command.WithdrawalAmountCommand;
import org.hifly.quickstarts.bank.account.event.AccountClosedEvent;
import org.hifly.quickstarts.bank.account.handler.AccountEventHandler;
import org.hifly.quickstarts.bank.account.queryManager.QueryController;
import org.hifly.quickstarts.bank.account.saga.CloseAccountSaga;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import static org.axonframework.eventhandling.GenericEventMessage.asEventMessage;
import static org.axonframework.eventhandling.Segment.ROOT_SEGMENT;

public class Runner {

    private static List<Future<Integer>> futures = new ArrayList<>();

    public static void main(String[] args) {

        //basic event bus
        EventBus eventBus = SimpleEventBus.builder().build();

        //basic command bus
        CommandBus commandBus = SimpleCommandBus.builder().build();
        CommandGateway commandGateway = DefaultCommandGateway.builder()
                .commandBus(commandBus)
                .build();
        //event accounts inmemory
        EventStore eventStore = EmbeddedEventStore.builder()
                .storageEngine(new InMemoryEventStorageEngine())
                .build();
        //account aggregate
        EventSourcingRepository<AccountAggregate> repository = EventSourcingRepository.builder(AccountAggregate.class)
                .eventStore(eventStore)
                .build();
        AggregateAnnotationCommandHandler<AccountAggregate> aggregatorHandler =
                AggregateAnnotationCommandHandler.<AccountAggregate>builder()
                        .aggregateType(AccountAggregate.class)
                        .repository(repository)
                        .build();
        aggregatorHandler.subscribe(commandBus);

        //listen for events
        final AnnotationEventHandlerAdapter annotationEventListenerAdapter = new AnnotationEventHandlerAdapter(new AccountEventHandler());
        eventStore.subscribe(messages -> messages.forEach(e -> {
                    try {
                        annotationEventListenerAdapter.handle(e);
                    } catch (Exception e1) {
                        throw new RuntimeException(e1);

                    }
                }

        ));

        //support for saga
        AnnotatedSagaRepository<CloseAccountSaga> sagaRepository = AnnotatedSagaRepository.<CloseAccountSaga>builder()
                .sagaStore(new InMemorySagaStore())
                .sagaType(CloseAccountSaga.class)
                .build();

        Supplier<CloseAccountSaga> accountSagaSupplier = () -> new CloseAccountSaga(eventBus);

        AnnotatedSagaManager<CloseAccountSaga> sagaManager =
                AnnotatedSagaManager.<CloseAccountSaga>builder()
                        .sagaRepository(sagaRepository)
                        .sagaType(CloseAccountSaga.class)
                        .sagaFactory(accountSagaSupplier)
                        .build();

        eventBus.subscribe(messages -> messages.forEach(e -> {
                    try {
                        if (sagaManager.canHandle(e, null)) {
                            sagaManager.handle(e, ROOT_SEGMENT);
                        }
                    } catch (Exception e1) {
                        throw new RuntimeException(e1);

                    }
                }

        ));


        final String itemId = "A1";
        commandGateway.send(new CreateAccountCommand(itemId, "kermit the frog"));
        final String itemId2 = "A2";
        commandGateway.send(new CreateAccountCommand(itemId2, "john the law"));

        ExecutorService executorService1 = scheduleDeposit(commandGateway, itemId);
        ExecutorService executorService2 = scheduleWithdrawal(commandGateway, itemId);
        ExecutorService executorService3 = scheduleDeposit(commandGateway, itemId2);
        ExecutorService executorService4 = scheduleWithdrawal(commandGateway, itemId2);


        for (Future<Integer> fut : futures) {
            try {
                fut.get();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        EventMessage<AccountClosedEvent> message = asEventMessage(new AccountClosedEvent(itemId, "kermit the frog"));
        UnitOfWork<Message<AccountClosedEvent>> uow = DefaultUnitOfWork.startAndGet(message);
        try {
            eventBus.publish(message);
            uow.commit();
        } catch (Exception e) {
            uow.rollback(e);
        }

        executorService1.shutdown();
        executorService2.shutdown();
        executorService3.shutdown();
        executorService4.shutdown();

        QueryController queryController = new QueryController();
        queryController.printAccountsDetail();
    }


    public static ExecutorService scheduleDeposit(CommandGateway commandGateway, String itemId) {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        Double upper = 10000.0;
        Double lower = 1.0;

        for (int i = 0; i < 2; i++) {
            Future<Integer> future = executor.submit(() -> {
                        commandGateway.send(
                                new DepositAmountCommand(itemId,
                                        Math.floor((Math.random() * (upper - lower) + lower) * 100) / 100));
                        return 1;
                    }
            );
            futures.add(future);
        }
        return executor;
    }

    public static ExecutorService scheduleWithdrawal(CommandGateway commandGateway, String itemId) {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        Double upper = 10000.0;
        Double lower = 1.0;


        for (int i = 0; i < 2; i++) {
            Future<Integer> future = executor.submit(() -> {
                        commandGateway.send(
                                new WithdrawalAmountCommand(itemId,
                                        Math.floor((Math.random() * (upper - lower) + lower) * 100) / 100));
                        return 1;
                    }
            );
            futures.add(future);
        }
        return executor;
    }

}