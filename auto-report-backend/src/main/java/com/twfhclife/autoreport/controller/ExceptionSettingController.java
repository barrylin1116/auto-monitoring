package com.twfhclife.autoreport.controller;

import com.twfhclife.autoreport.model.ExceptionSetting;
import com.twfhclife.autoreport.service.ExceptionSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;


@RestController
@RequestMapping("/api/exception-settings")
@RequiredArgsConstructor
public class ExceptionSettingController {

    private final ExceptionSettingService service;

    @GetMapping
    public List<ExceptionSetting> list(@RequestParam(required = false) String systemType) throws IOException {
        if (systemType != null && !systemType.isBlank()) {
            return service.findBySystemType(systemType);
        }
        return service.findAll();
    }

    @PostMapping
    public ExceptionSetting save(@RequestBody ExceptionSetting setting) throws IOException {
        return service.save(setting);
    }

    @DeleteMapping("/{systemType}/{eventType}")
    public void delete(@PathVariable String systemType, @PathVariable String eventType) throws IOException {
        service.delete(systemType, eventType);
    }
}