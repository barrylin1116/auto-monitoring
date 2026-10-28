package com.twfhclife.autoreport.repository.opensearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twfhclife.autoreport.model.ExceptionProcessRecord;
import lombok.RequiredArgsConstructor;
import org.opensearch.client.Request;
import org.opensearch.client.ResponseException;
import org.opensearch.client.RestClient;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


@Repository
@RequiredArgsConstructor
public class ExceptionProcessRecordRepository {

    static final String INDEX = "exception_process_record";

    private final RestClient client;
    private final ObjectMapper mapper;

    
    public void put(ExceptionProcessRecord record) throws IOException {
        Request request = new Request("PUT", "/" + INDEX + "/_doc/" + encode(record.getId()) + "?refresh=true");
        request.setJsonEntity(mapper.writeValueAsString(record));
        client.performRequest(request);
    }

    
    public List<ExceptionProcessRecord> findByEventId(String eventId) throws IOException {
        String body = "{\"size\":1000,\"query\":{\"term\":{\"eventId.keyword\":{\"value\":"
                + mapper.writeValueAsString(eventId) + "}}},"
                + "\"sort\":[{\"processedAt\":{\"order\":\"desc\"}}]}";
        Request request = new Request("POST", "/" + INDEX + "/_search");
        request.setJsonEntity(body);
        JsonNode hits;
        try {
            var response = client.performRequest(request);
            hits = mapper.readTree(response.getEntity().getContent()).path("hits").path("hits");
        } catch (ResponseException e) {
            if (e.getResponse().getStatusLine().getStatusCode() == 404) {
                return List.of();
            }
            throw e;
        }
        List<ExceptionProcessRecord> result = new ArrayList<>();
        for (JsonNode hit : hits) {
            ExceptionProcessRecord record =
                    mapper.treeToValue(hit.path("_source"), ExceptionProcessRecord.class);
            record.setId(hit.path("_id").asText());
            result.add(record);
        }
        return result;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
