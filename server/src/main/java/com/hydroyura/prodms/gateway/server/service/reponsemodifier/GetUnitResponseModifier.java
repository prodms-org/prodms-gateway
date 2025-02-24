package com.hydroyura.prodms.gateway.server.service.reponsemodifier;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.hydroyura.prodms.archive.client.model.api.ApiRes;
import java.util.Map;
import lombok.SneakyThrows;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Service
public class GetUnitResponseModifier implements RewriteFunction<ApiRes, ApiRes> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JavaType mapType = TypeFactory.
        defaultInstance()
        .constructMapType(Map.class, String.class, Object.class);

    private final WebClient httpClient = WebClient.builder()
        .baseUrl("http://localhost:8083")
        .build();

    @SneakyThrows // TODO: handle this ex
    @Override
    public Publisher<ApiRes> apply(ServerWebExchange serverWebExchange, ApiRes apiRes) {
        if (serverWebExchange.getResponse().getStatusCode().is2xxSuccessful()) {
            Map<String, Object> data = objectMapper.readValue(objectMapper.writeValueAsString(apiRes), mapType);
            String number = data.get("number").toString();

            httpClient.mutate().build()
                .get()
                .uri("/api/v1/number");

            return Mono.empty();
        } else {
            return Mono.just(apiRes);
        }
    }


}
