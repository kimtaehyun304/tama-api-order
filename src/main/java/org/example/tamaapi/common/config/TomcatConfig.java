package org.example.tamaapi.common.config;

import org.apache.coyote.ProtocolHandler;
import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

@Component
public class TomcatConfig
        implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        factory.addConnectorCustomizers(connector -> {
            ProtocolHandler handler = connector.getProtocolHandler();
            if (handler instanceof AbstractHttp11Protocol<?> protocol) {
                /*
                System.out.println("maxConnections = " + protocol.getMaxConnections());
                System.out.println("acceptCount = " + protocol.getAcceptCount());
                System.out.println("protocol.getMinSpareThreads() = " + protocol.getMinSpareThreads());
                System.out.println("protocol.getMaxThreads() = " + protocol.getMaxThreads());
                 */
            }
        });
    }
}
