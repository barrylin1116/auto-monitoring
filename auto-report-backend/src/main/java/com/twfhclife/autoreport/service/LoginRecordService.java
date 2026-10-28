package com.twfhclife.autoreport.service;

import com.twfhclife.autoreport.model.LoginRecord;
import com.twfhclife.autoreport.model.PageResult;
import com.twfhclife.autoreport.repository.opensearch.LoginRecordRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoginRecordService {

    private final LoginRecordRepository repository;

    
    public List<String> listSystems() throws IOException {
        return repository.listSystems();
    }

    
    public PageResult<LoginRecord> findAll(String system, String account, String name,
                                           String loginResult, String loginTimeStart, String loginTimeEnd,
                                           int page, int size)
            throws IOException {
        Pair<List<LoginRecord>, Long> result =
                repository.search(system, account, name, loginResult, loginTimeStart, loginTimeEnd, page * size, size);
        long total = result.getRight();
        int totalPages = size > 0 ? (int) Math.ceil((double) total / size) : 0;
        return new PageResult<>(result.getLeft(), total, totalPages, page, size);
    }
}
