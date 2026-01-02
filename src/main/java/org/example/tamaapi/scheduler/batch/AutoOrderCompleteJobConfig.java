package org.example.tamaapi.scheduler.batch;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tamaapi.command.order.OrderService;
import org.example.tamaapi.domain.order.OrderStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.orm.JpaNativeQueryProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class AutoOrderCompleteJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory emf;
    private final OrderService orderService;

    private static final int chunkSize = 1000;

    @Bean
    public JpaPagingItemReader<Long> orderIdReader()  {
        //업데이트가 실시간으로 이뤄지므로, 페이징이 앞으로 당겨지는 문제 해결을 위해
        JpaPagingItemReader<Long> reader = new JpaPagingItemReader<>() {
            @Override
            public int getPage() {
                return 0;
            }
        };

        reader.setName("orderIdReader");
        reader.setEntityManagerFactory(emf);
        reader.setPageSize(chunkSize);

        JpaNativeQueryProvider<Long> queryProvider =
                new JpaNativeQueryProvider<>();
        queryProvider.setSqlQuery("SELECT o.order_id FROM orders o WHERE o.updated_at >= now() - interval 80 day and o.status = :DELIVERED");
        queryProvider.setEntityClass(Long.class);

        reader.setParameterValues(Map.of(
                "DELIVERED", OrderStatus.DELIVERED.name()
        ));
        reader.setQueryProvider(queryProvider);
        return reader;
    }

    @Bean
    public ItemWriter<Long> orderUpdateWriter() {
        return chunk -> {
            if (chunk.isEmpty() || chunk.getItems().isEmpty()) {
                log.debug("reader 데이터가 비어서 배치를 생략합니다.");
                return;
            };
            orderService.updateOrderStatusToCompleted((List<Long>) chunk.getItems());
        };
    }

    @Bean
    public Step completeOrderStep(JpaPagingItemReader<Long> orderIdReader,
                                  ItemWriter<Long> orderUpdateWriter) {
        return new StepBuilder("completeOrderStep", jobRepository)
                .<Long, Long>chunk(chunkSize, transactionManager)
                .reader(orderIdReader)
                .writer(orderUpdateWriter)
                .transactionAttribute(
                        new DefaultTransactionAttribute(TransactionDefinition.PROPAGATION_REQUIRED) {{
                            setReadOnly(false);
                        }}
                )
                .faultTolerant()
                .retry(Exception.class)
                .retryLimit(3)
                .build();
    }

    @Bean
    public Job completeOrderJob(Step completeOrderStep) {
        return new JobBuilder("completeOrderJob", jobRepository)
                .start(completeOrderStep)
                .build();
    }
}
