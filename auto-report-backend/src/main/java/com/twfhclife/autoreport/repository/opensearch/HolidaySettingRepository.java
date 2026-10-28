package com.twfhclife.autoreport.repository.opensearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twfhclife.autoreport.model.HolidaySetting;
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
import java.util.Optional;


@Repository
@RequiredArgsConstructor
public class HolidaySettingRepository {

    static final String INDEX = "holiday_setting";

    private final RestClient client;
    private final ObjectMapper mapper;

    
    public Pair<List<HolidaySetting>, Long> search(int from, int size) throws IOException {
        Request request = new Request("POST", "/" + INDEX + "/_search");
        request.setJsonEntity(String.format(
                "{\"from\":%d,\"size\":%d,\"sort\":[{\"holidayDate\":{\"order\":\"asc\"}}]}",
                from, size));
        Response response = client.performRequest(request);
        JsonNode hits = mapper.readTree(response.getEntity().getContent()).path("hits");
        long total = hits.path("total").path("value").asLong();
        List<HolidaySetting> content = new ArrayList<>();
        hits.path("hits").forEach(hit -> {
            try {
                content.add(mapper.treeToValue(hit.path("_source"), HolidaySetting.class));
            } catch (IOException e) {
                throw new IllegalStateException("解析節假日資料失敗", e);
            }
        });
        return Pair.of(content, total);
    }

    
    public Optional<HolidaySetting> findByDate(String date) throws IOException {
        try {
            Response response = client.performRequest(new Request("GET", docPath(date)));
            return Optional.of(mapper.treeToValue(
                    mapper.readTree(response.getEntity().getContent()).path("_source"),
                    HolidaySetting.class));
        } catch (ResponseException e) {
            if (e.getResponse().getStatusLine().getStatusCode() == 404) {
                return Optional.empty();
            }
            throw e;
        }
    }

    
    public void put(HolidaySetting setting) throws IOException {
        Request request = new Request("PUT", docPath(setting.getHolidayDate()) + "?refresh=true");
        request.setJsonEntity(mapper.writeValueAsString(setting));
        client.performRequest(request);
    }

    
    public void deleteByDate(String date) throws IOException {
        try {
            client.performRequest(new Request("DELETE", docPath(date) + "?refresh=true"));
        } catch (ResponseException e) {
            if (e.getResponse().getStatusLine().getStatusCode() != 404) {
                throw e;
            }
        }
    }

    private String docPath(String date) {
        return "/" + INDEX + "/_doc/" + date;
    }
}
