package com.rzodeczko.paymentservice.infrastructure.configuration;

import com.rzodeczko.paymentservice.infrastructure.configuration.properties.TPayProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.Executors;

/**
 * Spring configuration for infrastructure-level beans used by outbound HTTP
 * integrations.
 */
@Configuration
@EnableConfigurationProperties({TPayProperties.class})
public class BeanConfiguration {

    /**
     * Configures a shared {@code RestClientCustomizer} backed by JDK HttpClient
     * with connection/read timeouts and a virtual-thread executor.
     *
     * @return customizer that applies a request factory to all RestClient builders
     */
    @Bean
    public RestClientCustomizer restClientCustomizer() {
        HttpClient httpClient = HttpClient
                .newBuilder()
                .connectTimeout(Duration.ofMillis(2000))
                .executor(Executors.newVirtualThreadPerTaskExecutor())
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMillis(5000));

        return builder -> builder.requestFactory(requestFactory);
    }
}
