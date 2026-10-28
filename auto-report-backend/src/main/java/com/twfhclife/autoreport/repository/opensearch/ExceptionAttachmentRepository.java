package com.twfhclife.autoreport.repository.opensearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twfhclife.autoreport.model.ExceptionAttachment;
import lombok.RequiredArgsConstructor;
import org.opensearch.client.Request;
import org.opensearch.client.Response;
import org.opensearch.client.ResponseException;
import org.opensearch.client.RestClient;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Repository
@RequiredArgsConstructor
public class ExceptionAttachmentRepository {

    static final String INDEX = "exception_attachment";

    private final RestClient client;
    private final ObjectMapper mapper;

    
    public void put(ExceptionAttachment attachment) throws IOException {
        Request request = new Request("PUT", "/" + INDEX + "/_doc/" + encode(attachment.getId()) + "?refresh=true");
        request.setJsonEntity(mapper.writeValueAsString(attachment));
        client.performRequest(request);
    }

    
    public Optional<ExceptionAttachment> findById(String id) throws IOException {
        try {
            Response response = client.performRequest(new Request("GET", "/" + INDEX + "/_doc/" + encode(id)));
            ExceptionAttachment attachment = mapper.treeToValue(
                    mapper.readTree(response.getEntity().getContent()).path("_source"), ExceptionAttachment.class);
            attachment.setId(id);
            return Optional.of(attachment);
        } catch (ResponseException e) {
            if (e.getResponse().getStatusLine().getStatusCode() == 404) {
                return Optional.empty();
            }
            throw e;
        }
    }

    
    public List<ExceptionAttachment> findByEventId(String eventId) throws IOException {
        String body = "{\"size\":1000,"
                + "\"_source\":{\"excludes\":[\"content\"]},"
                + "\"query\":{\"term\":{\"eventId.keyword\":{\"value\":" + mapper.writeValueAsString(eventId) + "}}},"
                + "\"sort\":[{\"uploadedAt\":{\"order\":\"desc\"}}]}";
        Request request = new Request("POST", "/" + INDEX + "/_search");
        request.setJsonEntity(body);
        JsonNode hits;
        try {
            hits = mapper.readTree(client.performRequest(request).getEntity().getContent())
                    .path("hits").path("hits");
        } catch (ResponseException e) {
            if (e.getResponse().getStatusLine().getStatusCode() == 404) {
                return List.of();
            }
            throw e;
        }
        List<ExceptionAttachment> result = new ArrayList<>();
        for (JsonNode hit : hits) {
            ExceptionAttachment attachment =
                    mapper.treeToValue(hit.path("_source"), ExceptionAttachment.class);
            attachment.setId(hit.path("_id").asText());
            result.add(attachment);
        }
        return result;
    }

    
    public void deleteById(String id) throws IOException {
        try {
            client.performRequest(new Request("DELETE", "/" + INDEX + "/_doc/" + encode(id) + "?refresh=true"));
        } catch (ResponseException e) {
            if (e.getResponse().getStatusLine().getStatusCode() == 404) {
                return;
            }
            throw e;
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
