package com.twfhclife.autodatastream.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twfhclife.autodatastream.model.ExceptionSetting;
import lombok.RequiredArgsConstructor;
import org.opensearch.client.Request;
import org.opensearch.client.Response;
import org.opensearch.client.RestClient;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Repository
@RequiredArgsConstructor
public class ExceptionSettingRepository {

    static final String INDEX = "exception_setting";

    private final RestClient client;

    private final ObjectMapper mapper = new ObjectMapper();

    
    public List<ExceptionSetting> findUpdatedAfter(String updatedAt) throws IOException {
        Request request = new Request("POST", "/" + INDEX + "/_search");
        request.setJsonEntity(buildIncrementalQuery(updatedAt));
        Response response = client.performRequest(request);
        JsonNode hits = mapper.readTree(response.getEntity().getContent()).path("hits").path("hits");
        List<ExceptionSetting> result = new ArrayList<>();
        for (JsonNode hit : hits) {
            result.add(mapper.treeToValue(hit.path("_source"), ExceptionSetting.class));
        }
        return result;
    }

    private String buildIncrementalQuery(String updatedAt) throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("size", 1000);
        if (updatedAt != null && !updatedAt.isBlank()) {
            body.put("query", Map.of("range", Map.of("updatedAt", Map.of("gt", updatedAt))));
        }
        body.put("sort", List.of(Map.of("updatedAt", Map.of("order", "asc"))));
        return mapper.writeValueAsString(body);
    }
}