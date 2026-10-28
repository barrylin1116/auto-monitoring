package com.twfhclife.autoreport.service;

import com.twfhclife.autoreport.model.OperationLog;
import com.twfhclife.autoreport.model.PageResult;
import com.twfhclife.autoreport.repository.opensearch.OperationLogRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final OperationLogRepository repository;

    
    public PageResult<OperationLog> findAll(String systemType, String account, String action, String result,
                                            List<String> ids, int page, int size) throws IOException {
        Pair<List<OperationLog>, Long> data =
                repository.search(systemType, account, action, result, ids, page * size, size);
        long total = data.getRight();
        int totalPages = size > 0 ? (int) Math.ceil((double) total / size) : 0;
        return new PageResult<>(data.getLeft(), total, totalPages, page, size);
    }
}