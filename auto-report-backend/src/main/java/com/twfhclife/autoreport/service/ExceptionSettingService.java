package com.twfhclife.autoreport.service;

import com.twfhclife.autoreport.model.ExceptionSetting;
import com.twfhclife.autoreport.repository.opensearch.ExceptionSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExceptionSettingService {

    private final ExceptionSettingRepository repository;

    public List<ExceptionSetting> findAll() throws IOException {
        return repository.findAll();
    }

    public List<ExceptionSetting> findBySystemType(String systemType) throws IOException {
        return repository.findBySystemType(systemType);
    }

    
    public ExceptionSetting save(ExceptionSetting setting) throws IOException {
        LocalDateTime now = LocalDateTime.now();
        Optional<ExceptionSetting> existing = repository.findBySystemTypeAndEventType(
                setting.getSystemType(), setting.getEventType());
        if (existing.isPresent()) {
            ExceptionSetting current = existing.get();
            current.setAutoProcess(setting.getAutoProcess());
            current.setRiskLevel(setting.getRiskLevel());
            current.setAutoClose(setting.getAutoClose());
            current.setUpdatedAt(now);
            repository.put(current);
            return current;
        }
        setting.setCreatedAt(now);
        setting.setUpdatedAt(now);
        repository.put(setting);
        return setting;
    }

    
    public void delete(String systemType) throws IOException {
        repository.deleteBySystemType(systemType);
    }

    
    public void delete(String systemType, String eventType) throws IOException {
        repository.deleteBySystemTypeAndEventType(systemType, eventType);
    }
}
