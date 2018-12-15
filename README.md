# Bank Account - Axon

Example of an application that manages a bank account.<br>
This an example of an event-driven application, implementing patterns as event sourcing, cqrs, saga.

Simple bank accounts commands/events are sent using Axon Framework; the events provided are:
  - create account
  - close account
  - deposit
  - withdrawal

Events are aggregated using the AccountAggregate class.

When a bank account is going to be cloased, this is managed usign a saga; an implementation of a saga pattern is realized using the CloseAccountSaga class.

## Compile and execute test

```bash
  mvn clean install

```

## Run a sample deposit/withdrawal simulation

```bash
  mvn clean install && mvn exec:java

```
