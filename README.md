# Bank Account - Axon

Example of an application that manages a bank account.<br>
This an example of an event-driven application, implementing patterns as event sourcing, cqrs, saga.

Events sent to the event bus and to the event bus are persisted to an Apache Kafka topic called "axon-test"..

Simple bank accounts commands/events are sent using Axon Framework; the events provided are:
  - create account
  - close account
  - deposit
  - withdrawal

Events are aggregated using the AccountAggregate class.

When a bank account is going to be closed, this is managed using a saga; an implementation of a saga pattern is realized using the CloseAccountSaga class.

The close account event is sent by a simulated external service.

## Prerequisites

Make sure you have an Apache Kafka broker running on localhost:9092 and a topic called "axon-test"

## Compile and execute test

```bash
  mvn clean install
```

## Run a sample deposit/withdrawal simulation

```bash
  mvn clean install && mvn exec:java -Dexec.mainClass="org.hifly.axon.bank.account.BankAccountApp"
```

```bash
2022-09-22 15:22:11 INFO  AccountEventHandler - account created A1, customer kermit the frog
2022-09-22 15:22:11 INFO  AccountEventHandler - account created A2, customer john the law
2022-09-22 15:22:16 INFO  AccountEventHandler - account A1, deposit 4320.46
2022-09-22 15:22:16 INFO  AccountEventHandler - account A2, deposit 976.57
2022-09-22 15:22:21 INFO  AccountEventHandler - account A1, withdrawal 9816.56 denied!!! - current balance 4320.46
2022-09-22 15:22:21 INFO  AccountEventHandler - account A2, withdrawal 1370.19 denied!!! - current balance 976.57
2022-09-22 15:22:23 INFO  QueryController - Account A1, customer kermit the frog, balance 4320.46
2022-09-22 15:22:23 INFO  QueryController - Account A2, customer john the law, balance 976.57
```

## Run a close account simulation

Open a new terminal session (don't stop the simulation main program) and run:
```bash
  mvn clean install && mvn exec:java -Dexec.mainClass="org.hifly.axon.bank.account.CloseAccountApp"
```

This will log in simulation main program:

```bash
2022-09-22 15:40:07 INFO  CloseAccountSaga - Received closed account event...
2022-09-22 15:40:07 INFO  CloseAccountSaga - account removed A1, customer kermit the frog 
```