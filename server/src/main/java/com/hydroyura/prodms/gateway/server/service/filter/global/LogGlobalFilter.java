package com.hydroyura.prodms.gateway.server.service.filter.global;

import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class LogGlobalFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestId = exchange.getRequest().getId();
        Long ts = Instant.now().getEpochSecond();
        String uri = exchange.getRequest().getURI().toString();
        String method = exchange.getRequest().getMethod().name();
        log.info("Got [{}] request with id = [{}] to uri = [{}] at ts = [{}]", method, requestId, uri, ts);
        return chain.filter(exchange);
    }


}
