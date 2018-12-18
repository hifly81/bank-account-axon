package org.hifly.axon.bank.account;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.common.Registration;
import org.axonframework.common.stream.BlockingStream;
import org.axonframework.config.Configuration;
import org.axonframework.config.DefaultConfigurer;
import org.axonframework.config.EventProcessingModule;
import org.axonframework.eventhandling.*;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine;
import org.axonframework.extensions.kafka.eventhandling.consumer.KafkaMessageSource;
import org.axonframework.extensions.kafka.eventhandling.producer.KafkaPublisher;
import org.axonframework.messaging.MessageHandlerInterceptor;
import org.axonframework.messaging.SubscribableMessageSource;
import org.axonframework.modelling.saga.AnnotatedSagaManager;
import org.axonframework.modelling.saga.repository.AnnotatedSagaRepository;
import org.axonframework.modelling.saga.repository.inmemory.InMemorySagaStore;
import org.hifly.axon.bank.account.aggregator.AccountAggregate;
import org.hifly.axon.bank.account.command.CreateAccountCommand;
import org.hifly.axon.bank.account.command.DepositAmountCommand;
import org.hifly.axon.bank.account.command.WithdrawalAmountCommand;
import org.hifly.axon.bank.account.config.AxonKafkaConfig;
import org.hifly.axon.bank.account.config.AxonUtil;
import org.hifly.axon.bank.account.event.AccountClosedEvent;
import org.hifly.axon.bank.account.handler.AccountEventHandler;
import org.hifly.axon.bank.account.queryManager.QueryController;
import org.hifly.axon.bank.account.saga.CloseAccountSaga;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.axonframework.eventhandling.GenericEventMessage.asEventMessage;

public class BankAccountConfigurationApiApp {

    private static final Logger LOG = LoggerFactory.getLogger(BankAccountConfigurationApiApp.class);

    private static List<Future<Integer>> futures = new ArrayList<>();

    public static void main(String[] args) {

        EventStore eventStore = EmbeddedEventStore.builder().storageEngine(new InMemoryEventStorageEngine()).build();
        AnnotatedSagaRepository<CloseAccountSaga> sagaRepository = AxonUtil.createAnnotatedSagaRepository(CloseAccountSaga.class);
        Supplier<CloseAccountSaga> accountSagaSupplier = () -> new CloseAccountSaga(eventStore);
        AnnotatedSagaManager<CloseAccountSaga> sagaManager = AxonUtil.createAnnotatedSagaManager(accountSagaSupplier, sagaRepository, CloseAccountSaga.class);

        KafkaMessageSource kafkaMessageSource = AxonKafkaConfig.createMessageSource("axon");

        Configuration configuration = DefaultConfigurer.defaultConfiguration()
                .configureEmbeddedEventStore(c -> new InMemoryEventStorageEngine())
                .configureAggregate(AccountAggregate.class)
                .eventProcessing(ep -> ep
                        .registerEventHandler(cf -> new AccountEventHandler())
                        .registerSaga(CloseAccountSaga.class,
                                sc -> sc.configureSagaStore(c -> new InMemorySagaStore())
                                        .configureRepository(c -> sagaRepository)
                                        .configureSagaManager(c -> sagaManager))
                        .byDefaultAssignTo("close-account")
                        .registerTrackingEventProcessor("close-account", c -> kafkaMessageSource)
                )
                .buildConfiguration();

        runSimulation(configuration);

    }

    private static void runSimulation(
            Configuration configuration) {

        LOG.info("----->>>Starting simulation<<<-----");

        sendCommands(configuration);
        //receiveExternalEvents(configuration.eventBus());
        queries();


    }

    private static void sendCommands(Configuration configuration) {

        final String itemId = "A1";
        configuration.commandGateway().send(new CreateAccountCommand(itemId, "kermit the frog"));
        final String itemId2 = "A2";
        configuration.commandGateway().send(new CreateAccountCommand(itemId2, "john the law"));

        try {
            ExecutorService executorService1 = scheduleDeposit(configuration.commandGateway(), itemId);
            Thread.sleep(1000);
            ExecutorService executorService2 = scheduleWithdrawal(configuration.commandGateway(), itemId);
            Thread.sleep(1000);
            ExecutorService executorService3 = scheduleDeposit(configuration.commandGateway(), itemId2);
            Thread.sleep(1000);
            ExecutorService executorService4 = scheduleWithdrawal(configuration.commandGateway(), itemId2);
            Thread.sleep(1000);

            configuration.eventBus().publish(asEventMessage(new AccountClosedEvent("A1", "kermit the frog")));

            executorService1.shutdown();
            executorService2.shutdown();
            executorService3.shutdown();
            executorService4.shutdown();

        } catch (Exception ex) {
            ex.printStackTrace();
        }


        for (Future<Integer> fut : futures) {
            try {
                fut.get();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }


    }

    private static ExecutorService scheduleDeposit(CommandGateway commandGateway, String itemId) {
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

    private static ExecutorService scheduleWithdrawal(CommandGateway commandGateway, String itemId) {
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

    private static void queries() {
        ExecutorService executor = Executors.newFixedThreadPool(1);
        QueryController queryController = new QueryController();

        executor.submit(() -> {
                    while (true) {
                        queryController.printAccountsDetail();
                        Thread.sleep(10000);
                    }
                }
        );
    }

    private static void receiveExternalEvents(EventBus eventBus) {
        ExecutorService executor = Executors.newFixedThreadPool(1);
        KafkaMessageSource kafkaMessageSource = AxonKafkaConfig.createMessageSource("axon");
        BlockingStream<TrackedEventMessage<?>> stream = kafkaMessageSource.openStream(null);

        LOG.info("----->>>Starting receiveExternalEvents<<<-----");

        executor.submit(() -> {
            while (true) {
                while (stream.hasNextAvailable(10, TimeUnit.SECONDS)) {
                    TrackedEventMessage<?> actual = stream.nextAvailable();
                    if (actual.getPayload() instanceof AccountClosedEvent) {
                        AccountClosedEvent accountClosedEvent = (AccountClosedEvent) actual.getPayload();
                        LOG.info("----->>>received AccountClosedEvent, account:" + accountClosedEvent.getAccountId() + "<<<-----");
                        EventMessage<AccountClosedEvent> message = asEventMessage(accountClosedEvent);
                        eventBus.publish(message);
                    }
                }
                Thread.sleep(10000);
            }
        });


    }
}
