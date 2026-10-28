package com.twfhclife.autoreport.repository.opensearch;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.opensearch.client.Request;
import org.opensearch.client.ResponseException;
import org.opensearch.client.RestClient;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


@Repository
@RequiredArgsConstructor
public class BizLogRepository {

    private final RestClient client;
    private final ObjectMapper mapper;

    
    public List<Map<String, Object>> findByIds(String bizName, List<String> ids) throws IOException {
        if (bizName == null || bizName.isBlank() || ids == null || ids.isEmpty()) {
            return List.of();
        }
        String index = ("biz_" + bizName).toLowerCase(Locale.ROOT);
        ArrayNode values = mapper.createArrayNode();
        for (String id : ids) {
            if (id != null && !id.isBlank()) {
                values.add(id.trim());
            }
        }
        if (values.isEmpty()) {
            return List.of();
        }
        ObjectNode root = mapper.createObjectNode();
        root.put("size", values.size());
        root.putObject("query").putObject("ids").set("values", values);

        JsonNode hits;
        try {
            Request request = new Request("POST", "/" + index + "/_search");
            request.setJsonEntity(mapper.writeValueAsString(root));
            hits = mapper.readTree(client.performRequest(request).getEntity().getContent())
                    .path("hits").path("hits");
        } catch (ResponseException e) {
            if (e.getResponse().getStatusLine().getStatusCode() == 404) {
                return List.of();
            }
            throw e;
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (JsonNode hit : hits) {
            Map<String, Object> doc = mapper.convertValue(
                    hit.path("_source"), new TypeReference<LinkedHashMap<String, Object>>() {
                    });
            result.add(doc);
        }
        return result;
    }
}
