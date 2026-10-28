package com.twfhclife.autoreport.repository.opensearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twfhclife.autoreport.model.ExceptionSetting;
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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;


@Repository
@RequiredArgsConstructor
public class ExceptionSettingRepository {

    static final String INDEX = "exception_setting";

    private final RestClient client;
    private final ObjectMapper mapper;

    
    public List<ExceptionSetting> findAll() throws IOException {
        Request request = new Request("POST", "/" + INDEX + "/_search");
        request.setJsonEntity("{\"size\":1000}");
        Response response = client.performRequest(request);
        JsonNode hits = mapper.readTree(response.getEntity().getContent()).path("hits").path("hits");
        List<ExceptionSetting> result = new ArrayList<>();
        for (JsonNode hit : hits) {
            result.add(toSetting(hit.path("_source")));
        }
        result.sort(Comparator
                .comparing(ExceptionSetting::getSystemType, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(ExceptionSetting::getEventType, Comparator.nullsLast(Comparator.naturalOrder())));
        return result;
    }

    
    public Optional<ExceptionSetting> findBySystemTypeAndEventType(String systemType, String eventType) throws IOException {
        try {
            Response response = client.performRequest(new Request("GET", docPath(systemType, eventType)));
            return Optional.of(toSetting(mapper.readTree(response.getEntity().getContent()).path("_source")));
        } catch (ResponseException e) {
            if (e.getResponse().getStatusLine().getStatusCode() == 404) {
                return Optional.empty();
            }
            throw e;
        }
    }

    
    public List<ExceptionSetting> findBySystemType(String systemType) throws IOException {
        List<ExceptionSetting> all = findAll();
        List<ExceptionSetting> result = new ArrayList<>();
        for (ExceptionSetting setting : all) {
            if (systemType != null && systemType.equals(setting.getSystemType())) {
                result.add(setting);
            }
        }
        return result;
    }

    
    public void put(ExceptionSetting setting) throws IOException {
        Request request = new Request("PUT", docPath(setting.getSystemType(), setting.getEventType()) + "?refresh=true");
        request.setJsonEntity(mapper.writeValueAsString(setting));
        client.performRequest(request);
    }

    
    public void deleteBySystemTypeAndEventType(String systemType, String eventType) throws IOException {
        try {
            client.performRequest(new Request("DELETE", docPath(systemType, eventType) + "?refresh=true"));
        } catch (ResponseException e) {
            if (e.getResponse().getStatusLine().getStatusCode() != 404) {
                throw e;
            }
        }
    }

    
    public void deleteBySystemType(String systemType) throws IOException {
        List<ExceptionSetting> list = findBySystemType(systemType);
        for (ExceptionSetting setting : list) {
            deleteBySystemTypeAndEventType(setting.getSystemType(), setting.getEventType());
        }
    }

    private ExceptionSetting toSetting(JsonNode source) throws IOException {
        return mapper.treeToValue(source, ExceptionSetting.class);
    }

    private String docPath(String systemType, String eventType) {
        return "/" + INDEX + "/_doc/" + encode(systemType + "_" + eventType);
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
