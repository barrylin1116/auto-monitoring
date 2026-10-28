package com.twfhclife.autoreport.service;

import com.twfhclife.autoreport.model.ConfigDictionary;
import com.twfhclife.autoreport.repository.opensearch.ConfigDictionaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ConfigDictionaryService {

    private final ConfigDictionaryRepository repository;

    public List<ConfigDictionary> findAll() throws IOException {
        return repository.findAll();
    }

    
    public Map<String, List<ConfigDictionary>> groupedByType() throws IOException {
        Map<String, List<ConfigDictionary>> grouped = new LinkedHashMap<>();
        for (ConfigDictionary item : repository.findAll()) {
            grouped.computeIfAbsent(item.getType(), key -> new java.util.ArrayList<>()).add(item);
        }
        return grouped;
    }
}