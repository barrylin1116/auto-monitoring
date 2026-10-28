package com.twfhclife.autoreport.repository.opensearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.twfhclife.autoreport.model.ExceptionEvent;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
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
public class ExceptionEventRepository {

    static final String INDEX = "exception_event";

    private final RestClient client;
    private final ObjectMapper mapper;

    
    public Pair<List<ExceptionEvent>, Long> search(String systemType, String eventType, String riskLevel,
                                                   String status, String account, int from, int size)
            throws IOException {
        ObjectNode body = buildQuery(systemType, eventType, riskLevel, status, account, from, size);
        JsonNode hits;
        try {
            hits = search(body);
        } catch (ResponseException e) {
            if (e.getResponse().getStatusLine().getStatusCode() == 404) {
                return Pair.of(List.of(), 0L);
            }
            throw e;
        }
        long total = hits.path("total").path("value").asLong();
        List<ExceptionEvent> content = new ArrayList<>();
        for (JsonNode hit : hits.path("hits")) {
            ExceptionEvent event = mapper.treeToValue(hit.path("_source"), ExceptionEvent.class);
            event.setId(hit.path("_id").asText());
            content.add(event);
        }
        return Pair.of(content, total);
    }

    
    public Optional<ExceptionEvent> findById(String id) throws IOException {
        try {
            Response response = client.performRequest(new Request("GET", "/" + INDEX + "/_doc/" + encode(id)));
            ExceptionEvent event = mapper.treeToValue(
                    mapper.readTree(response.getEntity().getContent()).path("_source"), ExceptionEvent.class);
            event.setId(id);
            return Optional.of(event);
        } catch (ResponseException e) {
            if (e.getResponse().getStatusLine().getStatusCode() == 404) {
                return Optional.empty();
            }
            throw e;
        }
    }

    
    public void updateProcessing(String id, String status, String processedBy,
                                 String processMethod, String riskAssessment,
                                 String processRemark, long processedAt) throws IOException {
        ObjectNode doc = mapper.createObjectNode();
        doc.put("status", status);
        doc.put("processedBy", processedBy);
        doc.put("processMethod", processMethod);
        doc.put("riskAssessment", riskAssessment);
        doc.put("processRemark", processRemark);
        doc.put("processedAt", processedAt);
        ObjectNode body = mapper.createObjectNode();
        body.set("doc", doc);
        Request request = new Request("POST", "/" + INDEX + "/_update/" + encode(id) + "?refresh=true");
        request.setJsonEntity(mapper.writeValueAsString(body));
        client.performRequest(request);
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private ObjectNode buildQuery(String systemType, String eventType, String riskLevel,
                                  String status, String account, int from, int size) {
        ObjectNode root = mapper.createObjectNode();
        ArrayNode filters = root.putObject("query").putObject("bool").putArray("filter");
        addTerm(filters, "systemType", systemType);
        addTerm(filters, "eventType", eventType);
        addTerm(filters, "riskLevel", riskLevel);
        addTerm(filters, "status", status);
        addWildcard(filters, "account", account);
        ObjectNode sortField = root.putArray("sort").addObject().putObject("occurredAt");
        sortField.put("order", "desc");
        root.put("from", from);
        root.put("size", size);
        return root;
    }

    private void addTerm(ArrayNode filters, String field, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        filters.addObject().putObject("term").putObject(field + ".keyword").put("value", value.trim());
    }

    private void addWildcard(ArrayNode filters, String field, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        String escaped = value.trim().replace("\\", "\\\\")
                .replace("*", "\\*").replace("?", "\\?");
        filters.addObject().putObject("wildcard").putObject(field + ".keyword")
                .put("value", "*" + escaped + "*")
                .put("case_insensitive", true);
    }

    private JsonNode search(ObjectNode body) throws IOException {
        Request request = new Request("POST", "/" + INDEX + "/_search");
        request.setJsonEntity(mapper.writeValueAsString(body));
        Response response = client.performRequest(request);
        return mapper.readTree(response.getEntity().getContent()).path("hits");
    }
}
