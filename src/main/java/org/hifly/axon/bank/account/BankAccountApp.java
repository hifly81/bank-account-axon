package org.hifly.axon.bank.account;

import org.axonframework.config.Configuration;
import org.axonframework.config.DefaultConfigurer;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine;
import org.axonframework.extensions.kafka.eventhandling.consumer.KafkaMessageSource;
import org.axonframework.extensions.kafka.eventhandling.producer.KafkaPublisher;
import org.axonframework.modelling.saga.AnnotatedSagaManager;
import org.axonframework.modelling.saga.repository.AnnotatedSagaRepository;
import org.axonframework.modelling.saga.repository.inmemory.InMemorySagaStore;
import org.hifly.axon.bank.account.aggregator.AccountAggregate;
import org.hifly.axon.bank.account.command.CreateAccountCommand;
import org.hifly.axon.bank.account.command.DepositAmountCommand;
import org.hifly.axon.bank.account.command.WithdrawalAmountCommand;
import org.hifly.axon.bank.account.config.SagaUtil;
import org.hifly.axon.bank.account.handler.AccountEventHandler;
import org.hifly.axon.bank.account.kafka.axon.AxonKafkaConfig;
import org.hifly.axon.bank.account.queryManager.QueryController;
import org.hifly.axon.bank.account.saga.CloseAccountSaga;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class BankAccountApp {

    private static final Logger LOG = LoggerFactory.getLogger(BankAccountApp.class);

    public static void main(String[] args) throws Exception {

        EventStore eventStore = EmbeddedEventStore.builder().storageEngine(new InMemoryEventStorageEngine()).build();
        AnnotatedSagaRepository<CloseAccountSaga> sagaRepository = SagaUtil.createAnnotatedSagaRepository(CloseAccountSaga.class);
        Supplier<CloseAccountSaga> accountSagaSupplier = () -> new CloseAccountSaga(eventStore);
        AnnotatedSagaManager<CloseAccountSaga> sagaManager = SagaUtil.createAnnotatedSagaManager(accountSagaSupplier, sagaRepository, CloseAccountSaga.class);

        KafkaMessageSource kafkaMessageSource = AxonKafkaConfig.createMessageSource("axon-test");
        KafkaPublisher<String, byte[]> kafkaEventStorePublisher = AxonKafkaConfig.createPublisher(eventStore, "axon-test");

        Configuration configuration = DefaultConfigurer.defaultConfiguration()
                .configureEventStore(configuration1 -> eventStore)
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

        configuration.start();
        kafkaEventStorePublisher.start();

        runSimulation(configuration);

    }

    private static void runSimulation(
            Configuration configuration) throws Exception {

        LOG.info("----->>>Starting simulation<<<-----");

        sendCommands(configuration);
        queries();


    }

    private static void sendCommands(Configuration configuration) throws Exception {

        Double upper = 10000.0;
        Double lower = 1.0;

        final String itemId = "A1";
        configuration.commandGateway().send(new CreateAccountCommand(itemId, "kermit the frog"));
        final String itemId2 = "A2";
        configuration.commandGateway().send(new CreateAccountCommand(itemId2, "john the law"));

        Thread.sleep(5000);

        configuration.commandGateway().send(
                new DepositAmountCommand(itemId,
                        Math.floor((Math.random() * (upper - lower) + lower) * 100) / 100));
        configuration.commandGateway().send(
                new DepositAmountCommand(itemId2,
                        Math.floor((Math.random() * (upper - lower) + lower) * 100) / 100));

        Thread.sleep(5000);

        configuration.commandGateway().send(
                new WithdrawalAmountCommand(itemId,
                        Math.floor((Math.random() * (upper - lower) + lower) * 100) / 100));

        configuration.commandGateway().send(
                new WithdrawalAmountCommand(itemId2,
                        Math.floor((Math.random() * (upper - lower) + lower) * 100) / 100));

        Thread.sleep(2000);

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

}
