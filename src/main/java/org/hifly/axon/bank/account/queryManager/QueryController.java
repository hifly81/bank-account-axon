package org.hifly.axon.bank.account.queryManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hifly.axon.bank.account.queryManager.AccountInMemoryView.accounts;

public class QueryController {

    private final Logger LOG = LoggerFactory.getLogger(QueryController.class);

    public void printAccountsDetail() {
       accounts.forEach((k,v) -> LOG.info("Account {}, customer {}, balance {}", v.getId(), v.getCustomerName(), Math.floor(v.getBalance()*100)/100));
    }
}
