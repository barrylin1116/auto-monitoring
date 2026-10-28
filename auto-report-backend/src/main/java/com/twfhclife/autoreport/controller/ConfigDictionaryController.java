package com.twfhclife.autoreport.controller;

import com.twfhclife.autoreport.model.ConfigDictionary;
import com.twfhclife.autoreport.service.ConfigDictionaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/config-dictionaries")
@RequiredArgsConstructor
public class ConfigDictionaryController {

    private final ConfigDictionaryService service;

    @GetMapping
    public Map<String, List<ConfigDictionary>> list() throws IOException {
        return service.groupedByType();
    }
}