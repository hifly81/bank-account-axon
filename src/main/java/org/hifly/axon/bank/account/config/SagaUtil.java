package org.hifly.axon.bank.account.config;

import org.axonframework.modelling.saga.AnnotatedSagaManager;
import org.axonframework.modelling.saga.repository.AnnotatedSagaRepository;
import org.axonframework.modelling.saga.repository.inmemory.InMemorySagaStore;

import java.util.function.Supplier;

public class SagaUtil {


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
