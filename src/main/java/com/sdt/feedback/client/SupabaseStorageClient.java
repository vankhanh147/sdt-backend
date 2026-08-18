package com.sdt.feedback.client;

import com.sdt.feedback.config.SupabaseStorageProperties;
import com.sdt.feedback.exception.StorageOperationException;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Component
public class SupabaseStorageClient {

    private static final String AUTHORIZATION = "Authorization";
    private static final String API_KEY = "apikey";

    private final RestClient restClient;
    private final SupabaseStorageProperties properties;

    public SupabaseStorageClient(
            RestClient supabaseStorageRestClient,
            SupabaseStorageProperties properties
    ) {
        this.restClient = supabaseStorageRestClient;
        this.properties = properties;
    }

    public void upload(String storagePath, byte[] content, String contentType) {
        try {
            restClient.post()
                    .uri(objectUri(false, storagePath))
                    .headers(this::addAuthentication)
                    .header("x-upsert", "false")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(content)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            throw new StorageOperationException(
                    "Unable to upload attachment to storage",
                    exception
            );
        }
    }

    public byte[] download(String storagePath) {
        try {
            byte[] content = restClient.get()
                    .uri(objectUri(true, storagePath))
                    .headers(this::addAuthentication)
                    .retrieve()
                    .body(byte[].class);
            if (content == null) {
                throw new StorageOperationException(
                        "Storage returned an empty attachment response",
                        null
                );
            }
            return content;
        } catch (StorageOperationException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new StorageOperationException(
                    "Unable to download attachment from storage",
                    exception
            );
        }
    }

    public void delete(String storagePath) {
        deleteAll(List.of(storagePath));
    }

    public void deleteAll(List<String> storagePaths) {
        if (storagePaths.isEmpty()) {
            return;
        }
        try {
            restClient.method(HttpMethod.DELETE)
                    .uri(bucketObjectUri())
                    .headers(this::addAuthentication)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("prefixes", storagePaths))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            throw new StorageOperationException(
                    "Unable to delete attachment from storage",
                    exception
            );
        }
    }

    private URI bucketObjectUri() {
        return UriComponentsBuilder
                .fromUriString(projectBaseUrl())
                .pathSegment("storage", "v1", "object", properties.bucket())
                .build()
                .encode()
                .toUri();
    }

    private URI objectUri(boolean authenticatedDownload, String storagePath) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(projectBaseUrl())
                .pathSegment("storage", "v1", "object");
        if (authenticatedDownload) {
            builder.pathSegment("authenticated");
        }
        builder.pathSegment(properties.bucket());
        builder.pathSegment(storagePath.split("/"));
        return builder.build().encode().toUri();
    }

    private String projectBaseUrl() {
        String url = properties.url().replaceFirst("/+$", "");
        return url.replaceFirst("/rest/v1$", "");
    }

    private void addAuthentication(org.springframework.http.HttpHeaders headers) {
        headers.setBearerAuth(properties.serviceRoleKey());
        headers.set(API_KEY, properties.serviceRoleKey());
    }
}
