package com.twfhclife.autoreport.repository.opensearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.twfhclife.autoreport.model.OperationLog;
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
public class OperationLogRepository {

    static final String INDEX_SUFFIX = "_OPERATION";

    private final RestClient client;
    private final ObjectMapper mapper;

    
    public Pair<List<OperationLog>, Long> search(String systemType, String account, String action, String result,
                                                 List<String> ids, int from, int size)
            throws IOException {
        ObjectNode body = buildQuery(account, action, result, ids, from, size);
        JsonNode hits;
        try {
            hits = search(resolveIndex(systemType), body);
        } catch (ResponseException e) {
            if (e.getResponse().getStatusLine().getStatusCode() == 404) {
                return Pair.of(List.of(), 0L);
            }
            throw e;
        }
        long total = hits.path("total").path("value").asLong();
        List<OperationLog> content = new ArrayList<>();
        for (JsonNode hit : hits.path("hits")) {
            content.add(toRecord(systemType, hit.path("_id").asText(), hit.path("_source")));
        }
        return Pair.of(content, total);
    }

    
    private OperationLog toRecord(String systemType, String id, JsonNode source) {
        JsonNode attributes = source.path("attributes");
        String prefix = systemType.trim().toLowerCase() + ".operation.";
        OperationLog record = new OperationLog();
        record.setId(id);
        record.setSystemType(systemType);
        record.setAccount(first(source, "account", attributes, prefix + "account"));
        record.setOperationTime(first(source, "operationTime", attributes, prefix + "time"));
        record.setFunctionScreen(first(source, "functionScreen", attributes, prefix + "function_screen"));
        record.setAction(first(source, "action", attributes, prefix + "action"));
        record.setActionResult(first(source, "actionResult", attributes, prefix + "action_result"));
        record.setActionContent(first(source, "actionContent", attributes, prefix + "action_content"));
        return record;
    }

    
    private String first(JsonNode source, String sourceField, JsonNode attributes, String attrField) {
        String value = text(source.path(sourceField));
        if (value != null) {
            return value;
        }
        return text(attributes.path(attrField));
    }

    private String text(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        String value = node.asText(null);
        return value == null || value.isBlank() ? null : value;
    }

    private ObjectNode buildQuery(String account, String action, String result,
                                  List<String> ids, int from, int size) {
        ObjectNode root = mapper.createObjectNode();
        ArrayNode filters = root.putObject("query").putObject("bool").putArray("filter");
        addIds(filters, ids);
        addWildcard(filters, "account", account);
        addWildcard(filters, "action", action);
        addWildcard(filters, "actionResult", result);
        ObjectNode sortField = root.putArray("sort").addObject().putObject("timeMillis");
        sortField.put("order", "desc");
        sortField.put("missing", "_last");
        root.put("from", from);
        root.put("size", size);
        return root;
    }

    
    private void addIds(ArrayNode filters, List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        ArrayNode values = filters.addObject().putObject("ids").putArray("values");
        for (String id : ids) {
            if (id != null && !id.isBlank()) {
                values.add(id.trim());
            }
        }
        if (values.isEmpty()) {
            filters.remove(filters.size() - 1);
        }
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

    
    private String resolveIndex(String systemType) {
        return ("biz_" + systemType.trim() + INDEX_SUFFIX).toLowerCase();
    }

    private JsonNode search(String index, ObjectNode body) throws IOException {
        Request request = new Request("POST", "/" + index + "/_search");
        request.setJsonEntity(mapper.writeValueAsString(body));
        Response response = client.performRequest(request);
        return mapper.readTree(response.getEntity().getContent()).path("hits");
    }
}