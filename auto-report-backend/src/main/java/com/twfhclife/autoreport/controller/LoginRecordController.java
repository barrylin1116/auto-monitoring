package com.twfhclife.autoreport.controller;

import com.twfhclife.autoreport.model.LoginRecord;
import com.twfhclife.autoreport.model.PageResult;
import com.twfhclife.autoreport.service.LoginRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;


@RestController
@RequestMapping("/api/login-records")
@RequiredArgsConstructor
public class LoginRecordController {

    private final LoginRecordService service;

    @GetMapping("/systems")
    public List<String> systems() throws IOException {
        return service.listSystems();
    }

    @GetMapping
    public PageResult<LoginRecord> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String system,
            @RequestParam(required = false) String account,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String loginResult,
            @RequestParam(required = false) String loginTimeStart,
            @RequestParam(required = false) String loginTimeEnd) throws IOException {
        return service.findAll(system, account, name, loginResult, loginTimeStart, loginTimeEnd, page, size);
    }
}
