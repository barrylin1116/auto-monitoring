package com.twfhclife.autodatastream.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.opensearch.client.Request;
import org.opensearch.client.ResponseException;
import org.opensearch.client.RestClient;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


@Component
@RequiredArgsConstructor
public class OpenSearchLogWriter {

    private final RestClient client;
    private final ObjectMapper mapper = new ObjectMapper();

    
    private final Set<String> ensured = ConcurrentHashMap.newKeySet();

    
    public void write(String bizName, String id, Object document) throws IOException {
        writeToIndex(("biz_" + bizName).toLowerCase(Locale.ROOT), id, document);
    }

    
    public void writeToIndex(String index, Object document) throws IOException {
        writeToIndex(index, null, document);
    }

    
    public void writeToIndex(String index, String id, Object document) throws IOException {
        ensureIndex(index);
        Request request;
        if (id == null || id.isBlank()) {
            request = new Request("POST", "/" + index + "/_doc");
        } else {
            request = new Request("PUT", "/" + index + "/_doc/" + encodeId(id));
        }
        request.setJsonEntity(mapper.writeValueAsString(document));
        client.performRequest(request);
    }

    private static String encodeId(String id) {
        return URLEncoder.encode(id, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private void ensureIndex(String index) throws IOException {
        if (ensured.contains(index)) {
            return;
        }
        try {
            client.performRequest(new Request("HEAD", "/" + index));
        } catch (ResponseException e) {
            if (e.getResponse().getStatusLine().getStatusCode() != 404) {
                throw e;
            }
            Request create = new Request("PUT", "/" + index);
            create.setJsonEntity("{\"mappings\":{}}");
            try {
                client.performRequest(create);
            } catch (ResponseException race) {
                
                if (race.getResponse().getStatusLine().getStatusCode() != 400) {
                    throw race;
                }
            }
        }
        ensured.add(index);
    }
}
