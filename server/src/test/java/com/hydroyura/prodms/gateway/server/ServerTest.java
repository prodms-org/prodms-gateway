package com.hydroyura.prodms.gateway.server;

import static com.hydroyura.prodms.gateway.server.TestUtils.ERROR_MSG_ARCHIVE_UNIT_NOT_FOUND;
import static com.hydroyura.prodms.gateway.server.TestUtils.ERROR_MSG_FILES_UNIT_NOT_FOUND;
import static com.hydroyura.prodms.gateway.server.TestUtils.UNIT_NUMBER_1;
import static com.hydroyura.prodms.gateway.server.TestUtils.URI_ARCHIVE_GET_UNIT;
import static com.hydroyura.prodms.gateway.server.TestUtils.URI_FILES_GET_URLS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.hydroyura.prodms.archive.client.model.enums.UnitStatus;
import com.hydroyura.prodms.archive.client.model.enums.UnitType;
import com.hydroyura.prodms.archive.client.model.res.GetUnitRes;
import com.hydroyura.prodms.common.model.api.ApiRes;
import com.hydroyura.prodms.files.server.api.enums.DrawingType;
import com.hydroyura.prodms.files.server.api.res.GetUrlsLatestRes;
import com.hydroyura.prodms.gateway.server.model.res.GetUnitDetailedRes;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ServerTest {

    static ClientAndServer archiveMockServer;
    static ClientAndServer filesMockServer;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private Properties properties;

    @Autowired
    private RouteLocator locator;

    @BeforeAll
    static void startServers() {
        archiveMockServer = ClientAndServer.startClientAndServer(8089);
        filesMockServer = ClientAndServer.startClientAndServer(8088);
    }

    @AfterAll
    static void stopServers() {
        archiveMockServer.stop();
        filesMockServer.stop();
    }

    @DynamicPropertySource
    static void tuneProperties(DynamicPropertyRegistry registry) {
        registry.add("microservices.urls.archive", () -> "http://localhost:" + archiveMockServer.getLocalPort());
    }

    @Test
    void test_GetUnit_SUCCESS() throws Exception {
        // given
        archiveMockServer
            .when(request()
                .withPath(URI_ARCHIVE_GET_UNIT.formatted(UNIT_NUMBER_1))
                .withMethod(HttpMethod.GET.name())
                .withContentType(MediaType.APPLICATION_JSON))
            .respond(HttpResponse
                .response()
                .withContentType(MediaType.APPLICATION_JSON)
                .withBody(archiveResponseOK())
                .withStatusCode(HttpStatus.OK.value())
            );

        filesMockServer
            .when(request()
                .withPath(URI_FILES_GET_URLS.formatted(UNIT_NUMBER_1))
                .withMethod(HttpMethod.GET.name())
                .withContentType(MediaType.APPLICATION_JSON))
            .respond(HttpResponse
                .response()
                .withContentType(MediaType.APPLICATION_JSON)
                .withBody(filesResponseOK())
                .withStatusCode(HttpStatus.OK.value())
            );


        // when
        var response = webTestClient
            .get()
            .uri(URI_ARCHIVE_GET_UNIT.formatted(UNIT_NUMBER_1))
            .header("Content-Type", MediaType.APPLICATION_JSON.toString())
            .exchange()
            .expectBody(JsonNode.class)
            .returnResult();


        // then
        ApiRes<GetUnitDetailedRes> body = objectMapper.readValue(
            response.getResponseBody().traverse(),
            TypeFactory.defaultInstance().constructParametricType(
                ApiRes.class,
                GetUnitDetailedRes.class)
        );
        assertEquals(HttpStatusCode.valueOf(200), response.getStatus());
        assertEquals(UNIT_NUMBER_1, body.getData().getNumber());
        assertEquals(3, body.getData().getUrls().size());
    }

    @Test
    void test_GetUnit_SUCCES_WITHOUT_DRAWINGS() throws Exception {
        // given
        archiveMockServer
            .when(request()
                .withPath(URI_ARCHIVE_GET_UNIT.formatted(UNIT_NUMBER_1))
                .withMethod(HttpMethod.GET.name())
                .withContentType(MediaType.APPLICATION_JSON))
            .respond(HttpResponse
                .response()
                .withContentType(MediaType.APPLICATION_JSON)
                .withBody(archiveResponseOK())
                .withStatusCode(HttpStatus.OK.value())
            );

        filesMockServer
            .when(request()
                .withPath(URI_FILES_GET_URLS.formatted(UNIT_NUMBER_1))
                .withMethod(HttpMethod.GET.name())
                .withContentType(MediaType.APPLICATION_JSON))
            .respond(HttpResponse
                .response()
                .withContentType(MediaType.APPLICATION_JSON)
                .withBody(filesResponseNOT_FOUND())
                .withStatusCode(HttpStatus.NOT_FOUND.value())
            );


        // when
        var response = webTestClient
            .get()
            .uri(URI_ARCHIVE_GET_UNIT.formatted(UNIT_NUMBER_1))
            .header("Content-Type", MediaType.APPLICATION_JSON.toString())
            .exchange()
            .expectBody(JsonNode.class)
            .returnResult();


        // then
        ApiRes<GetUnitDetailedRes> body = objectMapper.readValue(
            response.getResponseBody().traverse(),
            TypeFactory.defaultInstance().constructParametricType(
                ApiRes.class,
                GetUnitDetailedRes.class)
        );
        assertEquals(HttpStatusCode.valueOf(200), response.getStatus());
        assertTrue(body.getErrors().contains(ERROR_MSG_FILES_UNIT_NOT_FOUND.formatted(UNIT_NUMBER_1)));
        assertTrue(body.getData().getUrls().isEmpty());

    }


    @Test
    void test_GetUnit_NOT_FOUND_IN_ARCHIVE() throws Exception {
        // given
        archiveMockServer
            .when(request()
                .withPath(URI_ARCHIVE_GET_UNIT.formatted(UNIT_NUMBER_1))
                .withMethod(HttpMethod.GET.name())
                .withContentType(MediaType.APPLICATION_JSON))
            .respond(HttpResponse
                .response()
                .withContentType(MediaType.APPLICATION_JSON)
                .withBody(archiveResponseNOT_FOUND())
                .withStatusCode(HttpStatus.NOT_FOUND.value())
            );

        // when
        var response = webTestClient
            .get()
            .uri(URI_ARCHIVE_GET_UNIT.formatted(UNIT_NUMBER_1))
            .header("Content-Type", MediaType.APPLICATION_JSON.toString())
            .exchange()
            .expectBody(JsonNode.class)
            .returnResult();


        // then
        ApiRes<GetUnitDetailedRes> body = objectMapper.readValue(
            response.getResponseBody().traverse(),
            TypeFactory.defaultInstance().constructParametricType(
                ApiRes.class,
                GetUnitDetailedRes.class)
        );
        assertEquals(HttpStatusCode.valueOf(200), response.getStatus());
        assertTrue(body.getErrors().contains(ERROR_MSG_FILES_UNIT_NOT_FOUND.formatted(UNIT_NUMBER_1)));
        assertTrue(body.getData().getUrls().isEmpty());

    }


    @SneakyThrows
    private String archiveResponseOK() {
        GetUnitRes getUnitRes = new GetUnitRes();
        getUnitRes.setNumber(UNIT_NUMBER_1);
        getUnitRes.setVersion(1);
        getUnitRes.setType(UnitType.PART);
        getUnitRes.setStatus(UnitStatus.DESIGN);

        ApiRes<GetUnitRes> res = new ApiRes<>();
        res.setData(getUnitRes);
        res.setId(UUID.randomUUID());

        return objectMapper.writeValueAsString(res);
    }

    @SneakyThrows
    private String archiveResponseNOT_FOUND() {
        ApiRes<GetUnitRes> res = new ApiRes<>();
        res.setId(UUID.randomUUID());
        res.setErrors(List.of(ERROR_MSG_ARCHIVE_UNIT_NOT_FOUND.formatted(UNIT_NUMBER_1)));
        return objectMapper.writeValueAsString(res);
    }

    @SneakyThrows
    private String filesResponseOK() {
        GetUrlsLatestRes getUrlsLatestRes = new GetUrlsLatestRes();
        getUrlsLatestRes.setDrawings(Map.of(
            DrawingType.ASSEMBLY, "http://asem",
            DrawingType.SIMPLE, "http://simple",
            DrawingType.OVERALL, "http://overall"
        ));

        ApiRes<GetUrlsLatestRes> res = new ApiRes<>();
        res.setData(getUrlsLatestRes);
        res.setId(UUID.randomUUID());

        return objectMapper.writeValueAsString(res);
    }

    @SneakyThrows
    private String filesResponseNOT_FOUND() {
        ApiRes<GetUrlsLatestRes> res = new ApiRes<>();
        res.setId(UUID.randomUUID());
        res.setErrors(List.of(ERROR_MSG_FILES_UNIT_NOT_FOUND.formatted(UNIT_NUMBER_1)));
        return objectMapper.writeValueAsString(res);
    }

}