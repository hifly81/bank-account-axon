package org.hifly.quickstarts.bank.account.saga;

import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.messaging.Message;
import org.axonframework.messaging.unitofwork.DefaultUnitOfWork;
import org.axonframework.messaging.unitofwork.UnitOfWork;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.hifly.quickstarts.bank.account.event.AccountClosedConfirmedEvent;
import org.hifly.quickstarts.bank.account.event.AccountClosedDeniedEvent;
import org.hifly.quickstarts.bank.account.event.AccountClosedEvent;
import org.hifly.quickstarts.bank.account.model.Account;
import org.hifly.quickstarts.bank.account.queryManager.AccountInMemoryView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.axonframework.eventhandling.GenericEventMessage.asEventMessage;
import static org.hifly.quickstarts.bank.account.queryManager.AccountInMemoryView.accounts;

public class CloseAccountSaga {

    private EventBus eventBus;

    private final Logger LOG = LoggerFactory.getLogger(CloseAccountSaga.class);

    public CloseAccountSaga() { }

    public CloseAccountSaga(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @StartSaga
    @SagaEventHandler(associationProperty = "accountId")
    public void handle(AccountClosedEvent event) {

        Account account = accounts.get(event.getAccountId());
        account.getBalance();

        if(account.getBalance() < 0) {

            EventMessage<AccountClosedDeniedEvent> message = asEventMessage(new AccountClosedDeniedEvent(event.getAccountId(), event.getCustomerName()));
            UnitOfWork<Message<AccountClosedDeniedEvent>> uow = DefaultUnitOfWork.startAndGet(message);
            try {
                eventBus.publish(message);
                uow.commit();
            } catch (Exception e) {
                uow.rollback(e);
            }

        }
        else {
            EventMessage<AccountClosedConfirmedEvent> message = asEventMessage(new AccountClosedConfirmedEvent(event.getAccountId(), event.getCustomerName()));
            UnitOfWork<Message<AccountClosedConfirmedEvent>> uow = DefaultUnitOfWork.startAndGet(message);
            try {
                eventBus.publish(message);
                uow.commit();
            } catch (Exception e) {
                uow.rollback(e);
            }
        }

    }

    @EndSaga
    @SagaEventHandler(associationProperty = "accountId")
    public void handle(AccountClosedConfirmedEvent event) {
        AccountInMemoryView.accounts.remove(event.getAccountId());
        LOG.info("account removed {}, customer {} ", event.getAccountId(), event.getCustomerName());
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "accountId")
    public void handle(AccountClosedDeniedEvent event) {
        LOG.info("account can't be removed {}, customer {} ", event.getAccountId(), event.getCustomerName());
    }



}
