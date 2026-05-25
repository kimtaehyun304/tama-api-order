package org.tama.tamaapi.config;

import io.micrometer.tracing.exporter.SpanExportingPredicate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TracingConfig {

    @Bean
    public SpanExportingPredicate spanExportingPredicate() {
        return span -> {
            String name = span.getName();

            return name == null ||
                    !name.startsWith("task scheduler.");
        };
    }

}