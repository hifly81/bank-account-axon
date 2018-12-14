# Bank Account - Axon

Example of simple bank accounts commands/events using Axon Framework.

Events are aggregated using the AccountAggregate class.

An implementation of a saga pattern is realized using the CloaseAccountSaga class.

## Compile and execute test

```bash
  mvn clean install

```

## Run a sample deposit/withdrawal simulation

```bash
  mvn clean install && mvn exec:java

```
