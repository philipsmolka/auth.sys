package it.frs.auth.sys.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Configuration class for {@link WebClient}.
 * <p>
 * Defines a WebClient bean with a base URL and a logging filter that
 * logs every outgoing request URL.
 */
@Configuration
@Slf4j
public class WebClientConfig {

    /**
     * Creates and configures a {@link WebClient} instance.
     * <p>
     * The WebClient is configured with:
     * <ul>
     *     <li>Base URL: {@code http://ip-api.com}</li>
     *     <li>Request logging filter to log each outgoing request URL</li>
     * </ul>
     *
     * @return a configured WebClient instance
     */
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl("http://ip-api.com")
                .filter(ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
                    log.info("Calling: {}", clientRequest.url());
                    return Mono.just(clientRequest);
                }))
                .build();
    }
}
