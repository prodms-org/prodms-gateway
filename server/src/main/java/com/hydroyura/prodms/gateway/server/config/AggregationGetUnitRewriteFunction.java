package com.hydroyura.prodms.gateway.server.config;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hydroyura.prodms.archive.client.model.api.ApiRes;
import com.hydroyura.prodms.archive.client.model.res.GetUnitRes;
import com.hydroyura.prodms.files.server.api.res.GetUrlsLatestRes;
import com.hydroyura.prodms.gateway.server.mapper.GetUnitResToGetUnitDetailResMapper;
import com.hydroyura.prodms.gateway.server.model.res.GetUnitDetailedRes;
import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@SuppressWarnings("rawtypes")
@Slf4j
@RequiredArgsConstructor
public class AggregationGetUnitRewriteFunction implements RewriteFunction<JsonNode, ApiRes> {

    @Value("${microservices.urls.files}")
    private String filesUrl;

    private final GetUnitResToGetUnitDetailResMapper getUnitResToGetUnitDetailResMapper;

    //TODO: replace with bean
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final JavaType archiveResponseType = TypeFactory
        .defaultInstance()
        .constructParametricType(ApiRes.class, GetUnitRes.class);

    @PostConstruct
    void init() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @SneakyThrows
    @Override
    public Publisher<ApiRes> apply(ServerWebExchange serverWebExchange, JsonNode responseFromArchive) {
        Boolean isSuccessRequestToArchive = Optional
            .of(serverWebExchange.getResponse())
            .map(ServerHttpResponse::getStatusCode)
            .map(HttpStatusCode::is2xxSuccessful)
            .orElse(Boolean.FALSE);

        String number = extractNumberFromRequest(serverWebExchange);

        if (isSuccessRequestToArchive) {
            return Mono
                .zip(prepareCurrentResponse(responseFromArchive), fetchUrls(number))
                .map(tuple -> aggregate(tuple.getT1(), tuple.getT2()));
        } else {
            return prepareCurrentResponse(responseFromArchive);
        }
    }

    //TODO: handle error
    private String extractNumberFromRequest(ServerWebExchange serverWebExchange) {
        return Optional
            .ofNullable(serverWebExchange.getAttribute(ServerWebExchangeUtils.URI_TEMPLATE_VARIABLES_ATTRIBUTE))
            .map(Map.class::cast)
            .map(m -> String.valueOf(m.get("number")))
            .orElseThrow(() -> new RuntimeException("Handle it!"));
    }

    //TODO: handle error
    @SneakyThrows
    private Mono<ApiRes> prepareCurrentResponse(JsonNode responseFromArchive) {
        ApiRes<GetUnitRes> castedResponseFromArchive = objectMapper.readValue(
            responseFromArchive.traverse(),
            archiveResponseType
        );
        return Mono.just(castedResponseFromArchive);
    }

    // TODO: fetch exception
    private Mono<ApiRes<GetUrlsLatestRes>> fetchUrls(String number) {
        return WebClient.builder()
            .baseUrl(filesUrl)
            .build()
            .method(HttpMethod.GET)
            .uri("/api/v1/drawings/{number}", number)
            .contentType(MediaType.APPLICATION_JSON)
            .exchangeToMono(clientResponse ->
                clientResponse.bodyToMono(new ParameterizedTypeReference<ApiRes<GetUrlsLatestRes>>() {})
            );
    }

    private ApiRes<GetUnitDetailedRes> aggregate(ApiRes<GetUnitRes> archiveRes, ApiRes<GetUrlsLatestRes> filesRes) {
        GetUnitDetailedRes data = getUnitResToGetUnitDetailResMapper.convertWithUrls(archiveRes.getData(), filesRes.getData());
        ApiRes<GetUnitDetailedRes> completedRes = new ApiRes<>();
        completedRes.setData(data);
        completedRes.setId(archiveRes.getId());
        completedRes.setTimestamp(archiveRes.getTimestamp());
        completedRes.getErrors().addAll(archiveRes.getErrors());
        completedRes.getErrors().addAll(filesRes.getErrors());
        completedRes.getWarnings().addAll(archiveRes.getWarnings());
        completedRes.getWarnings().addAll(filesRes.getWarnings());
        return completedRes;
    }





}
