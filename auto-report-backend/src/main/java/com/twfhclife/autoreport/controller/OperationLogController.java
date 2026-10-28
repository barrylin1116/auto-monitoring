package com.twfhclife.autoreport.controller;

import com.twfhclife.autoreport.model.OperationLog;
import com.twfhclife.autoreport.model.PageResult;
import com.twfhclife.autoreport.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;


@RestController
@RequestMapping("/api/operation-logs")
@RequiredArgsConstructor
public class OperationLogController {

    private final OperationLogService service;

    @GetMapping
    public PageResult<OperationLog> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String systemType,
            @RequestParam(required = false) String account,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String actionResult,
            @RequestParam(required = false) List<String> ids) throws IOException {
        return service.findAll(systemType, account, action, actionResult, ids, page, size);
    }
}