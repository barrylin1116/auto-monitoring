package com.twfhclife.autoreport.repository.opensearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.twfhclife.autoreport.model.LoginRecord;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.opensearch.client.Request;
import org.opensearch.client.Response;
import org.opensearch.client.ResponseException;
import org.opensearch.client.RestClient;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Repository
@RequiredArgsConstructor
public class LoginRecordRepository {

    
    static final String INDEX_SUFFIX = "_LOGIN";

    private final RestClient client;
    private final ObjectMapper mapper;

    
    public List<String> listSystems() throws IOException {
        Request request = new Request("POST", "/config_dictionary/_search");
        ObjectNode body = mapper.createObjectNode();
        body.put("size", 1000);
        body.putObject("query").putObject("term").putObject("type.keyword").put("value", "system_type");
        request.setJsonEntity(mapper.writeValueAsString(body));
        JsonNode hits;
        try {
            Response response = client.performRequest(request);
            hits = mapper.readTree(response.getEntity().getContent()).path("hits").path("hits");
        } catch (ResponseException e) {
            if (e.getResponse().getStatusLine().getStatusCode() == 404) {
                return List.of();
            }
            throw e;
        }
        List<String> systems = new ArrayList<>();
        for (JsonNode hit : hits) {
            String code = hit.path("_source").path("code").asText("");
            if (!code.isBlank()) {
                systems.add(code);
            }
        }
        systems.sort(String::compareTo);
        return systems;
    }

    
    public Pair<List<LoginRecord>, Long> search(String system, String account, String name,
                                                String loginResult, String loginTimeStart, String loginTimeEnd,
                                                int from, int size)
            throws IOException {
        String index = resolveIndex(system);
        ObjectNode body = buildQuery(account, name, loginResult, loginTimeStart, loginTimeEnd, from, size);
        JsonNode hits;
        try {
            hits = search(index, body);
        } catch (ResponseException e) {
            int status = e.getResponse().getStatusLine().getStatusCode();
            if (status == 404) {
                return Pair.of(List.of(), 0L);
            }
            if (status != 400) {
                throw e;
            }
            
            body.remove("sort");
            ArrayNode sort = body.putArray("sort");
            sort.addObject().put("_doc", "desc");
            hits = search(index, body);
        }
        long total = hits.path("total").path("value").asLong();
        List<LoginRecord> content = new ArrayList<>();
        for (JsonNode hit : hits.path("hits")) {
            content.add(mapper.treeToValue(hit.path("_source"), LoginRecord.class));
        }
        return Pair.of(content, total);
    }

    
    private String resolveIndex(String system) {
        return ("biz_" + system.trim() + INDEX_SUFFIX).toLowerCase();
    }

    private ObjectNode buildQuery(String account, String name, String loginResult,
                                  String loginTimeStart, String loginTimeEnd, int from, int size) {
        ObjectNode root = mapper.createObjectNode();
        ArrayNode filters = root.putObject("query").putObject("bool").putArray("filter");
        addWildcard(filters, "帳號.keyword", account);
        addWildcard(filters, "姓名.keyword", name);
        if (loginResult != null && !loginResult.isBlank()) {
            filters.addObject().putObject("term")
                    .putObject("登入結果.keyword").put("value", loginResult);
        }
        addLoginTimeRange(filters, loginTimeStart, loginTimeEnd);
        ObjectNode sortField = root.putArray("sort").addObject()
                .putObject("登入時間.keyword");
        sortField.put("order", "desc");
        sortField.put("missing", "_last");
        root.put("from", from);
        root.put("size", size);
        return root;
    }

    private void addWildcard(ArrayNode filters, String field, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        String escaped = value.trim().replace("\\", "\\\\")
                .replace("*", "\\*").replace("?", "\\?");
        filters.addObject().putObject("wildcard").putObject(field)
                .put("value", "*" + escaped + "*")
                .put("case_insensitive", true);
    }

    private void addLoginTimeRange(ArrayNode filters, String start, String end) {
        boolean hasStart = start != null && !start.isBlank();
        boolean hasEnd = end != null && !end.isBlank();
        if (!hasStart && !hasEnd) {
            return;
        }
        ObjectNode range = filters.addObject().putObject("range").putObject("登入時間.keyword");
        if (hasStart) {
            range.put("gte", normalizeStart(start.trim()));
        }
        if (hasEnd) {
            range.put("lte", normalizeEnd(end.trim()));
        }
    }

    private String normalizeStart(String value) {
        return value.length() == 10 ? value + " 00:00:00" : value;
    }

    private String normalizeEnd(String value) {
        return value.length() == 10 ? value + " 23:59:59" : value;
    }

    private JsonNode search(String index, ObjectNode body) throws IOException {
        Request request = new Request("POST", "/" + index + "/_search");
        request.setJsonEntity(mapper.writeValueAsString(body));
        Response response = client.performRequest(request);
        return mapper.readTree(response.getEntity().getContent()).path("hits");
    }
}
