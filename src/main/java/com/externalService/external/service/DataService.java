package com.externalService.external.service;

import com.externalService.external.http.HttpMethod;
import com.google.gson.Gson;
import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

import static com.externalService.external.http.HttpMethod.PATCH;
import static com.externalService.external.http.HttpMethod.POST;

public class DataService {


    private static final Logger LOGGER = LoggerFactory.getLogger(DataService.class);

    public static ResponseEntity<String> getRequest(String url) {

        Gson gson = new Gson();
        String json;
        try {
            HttpRequest request = buildRequest(url, null, HttpMethod.GET);
            HttpResponse<String> response = sendRequest(request);
            json = gson.toJson(response.body());


        } catch (Exception ex) {
            LOGGER.error("Connection failed: {}", ex.getMessage(), ex);
            return null;
        }
        return ResponseEntity.ok(json);    }

    public static ResponseEntity<String> postRequest(String url, String body) {

        Gson gson = new Gson();
        String json;
        try {
            HttpMethod method = url.contains("/patch") ? PATCH : POST;
            HttpRequest request = buildRequest(url, body, method);
            HttpResponse<String> response = sendRequest(request);
            json = gson.toJson(response.body());


        } catch (Exception ex) {
            LOGGER.error("Connection failed: {}", ex.getMessage(), ex);
            return null;
        }
        return ResponseEntity.ok(json);
    }


    private static HttpRequest buildRequest(String url, String body, HttpMethod method) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .header("Content-Type", "application/json")
                .uri(URI.create(url));

        if (method == HttpMethod.GET) {
            requestBuilder.GET();
        } else {
            requestBuilder.method(method.name(), HttpRequest.BodyPublishers.ofString(body));
        }

        return requestBuilder.build();
    }

    private static HttpResponse<String> sendRequest(HttpRequest request) {
        RetryPolicy<HttpResponse<String>> retryPolicy = getRetryPolicy();
        return Failsafe.with(retryPolicy).get(() -> {
            HttpResponse<String> response = getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new Exception(String.format("Non-success response requesting: %s with status code: %s , body: %s", request.uri(), response.statusCode(), response.body()));
            }
            return response;
        });
    }


    private static RetryPolicy<HttpResponse<String>> getRetryPolicy() {
        return RetryPolicy.<HttpResponse<String>>builder()
                .handle(Exception.class)
                .withDelay(Duration.ofSeconds(20))
                .withMaxRetries(2)
                .onRetry(e -> LOGGER.info("Retrying: {} attempt", e.getAttemptCount()))
                .onFailedAttempt(e -> LOGGER.warn("Failed attempt: {}", e.getAttemptCount()))
                .build();
    }

    public static HttpClient getHttpClient(){
        return HttpClient
                .newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }

}
