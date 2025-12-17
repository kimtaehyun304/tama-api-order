package org.example.tamaapi.common.config.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class RoutingDataSource extends AbstractRoutingDataSource {
    public static final String DATASOURCE_KEY_MASTER = "master";

    @Override
    protected Object determineCurrentLookupKey() {
        Object dataSource = DATASOURCE_KEY_MASTER;

        boolean currentTransactionReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
        System.out.println("currentTransactionReadOnly = " + currentTransactionReadOnly);
        //System.out.println("readOnly = " + TransactionSynchronizationManager.isCurrentTransactionReadOnly());
        if (TransactionSynchronizationManager.isCurrentTransactionReadOnly())
            dataSource = "slave0";

        log.info("Selected DB: {}", dataSource);
        return dataSource;
    }
}