package org.hifly.quickstarts.bank.account;

import org.axonframework.commandhandling.AggregateAnnotationCommandHandler;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.commandhandling.gateway.DefaultCommandGateway;
import org.axonframework.eventhandling.AnnotationEventListenerAdapter;
import org.axonframework.eventsourcing.EventSourcingRepository;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine;
import org.hifly.quickstarts.bank.account.aggregator.AccountAggregate;
import org.hifly.quickstarts.bank.account.command.CreateAccountCommand;
import org.hifly.quickstarts.bank.account.command.DepositAmountCommand;
import org.hifly.quickstarts.bank.account.command.WithdrawalAmountCommand;
import org.hifly.quickstarts.bank.account.handler.AccountEventHandler;
import org.hifly.quickstarts.bank.account.queryManager.QueryController;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Runner {

    private static List<Future<Integer>> futures = new ArrayList<>();

    public static void main(String[] args) {
        //basic event bus
        CommandBus commandBus = new SimpleCommandBus();
        CommandGateway commandGateway = new DefaultCommandGateway(commandBus);

        //event accounts inmemory
        EventStore eventStore = new EmbeddedEventStore(new InMemoryEventStorageEngine());
        //account aggregate
        EventSourcingRepository<AccountAggregate> repository = new EventSourcingRepository<>(AccountAggregate.class, eventStore);
        AggregateAnnotationCommandHandler<AccountAggregate> aggregatorHandler = new AggregateAnnotationCommandHandler<>(AccountAggregate.class, repository);
        aggregatorHandler.subscribe(commandBus);

        //listen for events
        final AnnotationEventListenerAdapter annotationEventListenerAdapter = new AnnotationEventListenerAdapter(new AccountEventHandler());
        eventStore.subscribe(messages -> messages.forEach(e -> {
                    try {
                        annotationEventListenerAdapter.handle(e);
                    } catch (Exception e1) {
                        throw new RuntimeException(e1);

                    }
                }

        ));

        final String itemId = UUID.randomUUID().toString();
        commandGateway.send(new CreateAccountCommand(itemId, "kermit the frog"));
        final String itemId2 = UUID.randomUUID().toString();
        commandGateway.send(new CreateAccountCommand(itemId2, "john the law"));

        scheduleDeposit(commandGateway, itemId);
        scheduleWithdrawal(commandGateway, itemId);
        scheduleDeposit(commandGateway, itemId2);
        scheduleWithdrawal(commandGateway, itemId2);

        for (Future<Integer> fut : futures) {
            try {
                fut.get();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        QueryController queryController = new QueryController();
        queryController.printAccountsDetail();
    }

    public static void scheduleDeposit(CommandGateway commandGateway, String itemId) {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        Random r = new Random();

        for (int i = 0; i < 2; i++) {
            Future<Integer> future = executor.submit(() -> {
                        commandGateway.send(new DepositAmountCommand(itemId, r.nextDouble()));
                        return 1;
                    }
            );
            futures.add(future);
        }
    }

    public static void scheduleWithdrawal(CommandGateway commandGateway, String itemId) {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        Random r = new Random();

        for (int i = 0; i < 2; i++) {
            Future<Integer> future = executor.submit(() -> {
                        commandGateway.send(new WithdrawalAmountCommand(itemId, r.nextDouble()));
                        return 1;
                    }
            );
            futures.add(future);
        }
    }


}