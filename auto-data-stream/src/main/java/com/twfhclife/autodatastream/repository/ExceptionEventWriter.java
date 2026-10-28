package com.twfhclife.autodatastream.repository;

import com.twfhclife.autodatastream.model.ExceptionEvent;
import com.twfhclife.autodatastream.service.EventNoGenerator;
import lombok.RequiredArgsConstructor;
import org.opensearch.client.Request;
import org.opensearch.client.ResponseException;
import org.opensearch.client.RestClient;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;


@Repository
@RequiredArgsConstructor
public class ExceptionEventWriter {

    public static final String INDEX = "exception_event";

    private static final String MAPPING = """
            {
              "mappings": {
                "properties": {
                  "eventNo":     { "type": "keyword" },
                  "occurredAt":  { "type": "long" },
                  "eventType":   { "type": "keyword" },
                  "riskLevel":   { "type": "keyword" },
                  "systemType":  { "type": "keyword" },
                  "bizName":     { "type": "keyword" },
                  "account":     { "type": "keyword" },
                  "name":        { "type": "keyword" },
                  "channelBranch": { "type": "keyword" },
                  "deviceInfo":  { "type": "keyword" },
                  "sourceIp":    { "type": "keyword" },
                  "description": { "type": "keyword" },
                  "detailDescription": { "type": "keyword" },
                  "status":      { "type": "keyword" },
                  "processedBy":   { "type": "keyword" },
                  "processRemark": { "type": "keyword" },
                  "processedAt":   { "type": "long" },
                  "relatedLogIds": { "type": "keyword" },
                  "createdAt":   { "type": "long" }
                }
              }
            }
            """;

    private final RestClient client;
    private final OpenSearchLogWriter logWriter;
    private final EventNoGenerator eventNoGenerator;

    
    private final AtomicBoolean indexEnsured = new AtomicBoolean(false);

    public void write(ExceptionEvent event) throws IOException {
        if (event.getEventNo() == null || event.getEventNo().isBlank()) {
            event.setEventNo(eventNoGenerator.next());
        }
        ensureIndex();
        logWriter.writeToIndex(INDEX, event);
    }

    private void ensureIndex() throws IOException {
        if (indexEnsured.get()) {
            return;
        }
        try {
            client.performRequest(new Request("HEAD", "/" + INDEX));
        } catch (ResponseException e) {
            if (e.getResponse().getStatusLine().getStatusCode() != 404) {
                throw e;
            }
            Request create = new Request("PUT", "/" + INDEX);
            create.setJsonEntity(MAPPING);
            try {
                client.performRequest(create);
            } catch (ResponseException race) {
                
                if (race.getResponse().getStatusLine().getStatusCode() != 400) {
                    throw race;
                }
            }
        }
        indexEnsured.set(true);
    }
}
