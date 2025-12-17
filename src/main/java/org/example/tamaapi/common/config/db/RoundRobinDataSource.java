package org.example.tamaapi.common.config.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class RoundRobinDataSource extends AbstractRoutingDataSource {
    public static final String DATASOURCE_KEY_MASTER = "master";
    private final AtomicInteger atomicInteger = new AtomicInteger(0);

    private int slavesSize;

    //dataBaseProperty @AutoWired로 받으려고
    //이 클래스에 @Componenet 붙였더니 에러나서 이렇게 함
    public RoundRobinDataSource(int slavesSize) {
        this.slavesSize = slavesSize;
    }

    @Override
    protected Object determineCurrentLookupKey() {
        Object dataSource = DATASOURCE_KEY_MASTER;

        if (TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
            //MAX_VALUE 도달하면 0으로 자동으로 안돌아가서, 그전에 수동으로 바꿔줘야 함
            int index = atomicInteger.getAndUpdate(i -> i >= Integer.MAX_VALUE - 1000 ? 0 : i + 1);
            dataSource = "slave" + (index % slavesSize);
        }
        //log.info("Selected DB: {}",dataSource);
        return  dataSource;
    }
}