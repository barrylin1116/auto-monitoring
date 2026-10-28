package com.twfhclife.autoreport.repository.opensearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twfhclife.autoreport.model.ConfigDictionary;
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


@Repository
@RequiredArgsConstructor
public class ConfigDictionaryRepository {

    public static final String INDEX = "config_dictionary";

    private final RestClient client;
    private final ObjectMapper mapper;

    
    public List<ConfigDictionary> findAll() throws IOException {
        Request request = new Request("POST", "/" + INDEX + "/_search");
        request.setJsonEntity(
                "{\"size\":1000,\"sort\":[{\"type.keyword\":\"asc\"},{\"sort\":\"asc\"}]}");
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
        List<ConfigDictionary> result = new ArrayList<>();
        for (JsonNode hit : hits) {
            result.add(mapper.treeToValue(hit.path("_source"), ConfigDictionary.class));
        }
        return result;
    }

    
    public void put(ConfigDictionary item) throws IOException {
        Request request = new Request("PUT", docPath(item.getType(), item.getCode()) + "?refresh=true");
        request.setJsonEntity(mapper.writeValueAsString(item));
        client.performRequest(request);
    }

    
    public boolean indexExists() {
        try {
            client.performRequest(new Request("HEAD", "/" + INDEX));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String docPath(String type, String code) {
        return "/" + INDEX + "/_doc/" + encode(type + ":" + code);
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}