package com.twfhclife.autodatastream.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.opensearch.client.Request;
import org.opensearch.client.Response;
import org.opensearch.client.ResponseException;
import org.opensearch.client.RestClient;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Repository
@RequiredArgsConstructor
public class ConfigDictionaryRepository {

    static final String INDEX = "config_dictionary";

    private final RestClient client;

    private final ObjectMapper mapper = new ObjectMapper();

    
    public Map<String, String> findByType(String type) throws IOException {
        Request request = new Request("POST", "/" + INDEX + "/_search");
        request.setJsonEntity(buildQuery(type));
        try {
            Response response = client.performRequest(request);
            JsonNode hits = mapper.readTree(response.getEntity().getContent()).path("hits").path("hits");
            Map<String, String> result = new LinkedHashMap<>();
            for (JsonNode hit : hits) {
                JsonNode source = hit.path("_source");
                String code = source.path("code").asText(null);
                String label = source.path("label").asText(null);
                if (code != null) {
                    result.put(code, label);
                }
            }
            return result;
        } catch (ResponseException e) {
            if (e.getResponse().getStatusLine().getStatusCode() == 404) {
                return Map.of();
            }
            throw e;
        }
    }

    private String buildQuery(String type) throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("size", 1000);
        body.put("query", Map.of("term", Map.of("type.keyword", type)));
        body.put("_source", List.of("code", "label"));
        return mapper.writeValueAsString(body);
    }
}
