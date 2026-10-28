package com.twfhclife.autoreport.service;

import com.twfhclife.autoreport.model.HolidaySetting;
import com.twfhclife.autoreport.model.PageResult;
import com.twfhclife.autoreport.repository.opensearch.HolidaySettingRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HolidaySettingService {

    private final HolidaySettingRepository repository;

    
    public PageResult<HolidaySetting> findAll(int page, int size) throws IOException {
        Pair<List<HolidaySetting>, Long> result = repository.search(page * size, size);
        long total = result.getRight();
        int totalPages = size > 0 ? (int) Math.ceil((double) total / size) : 0;
        return new PageResult<>(result.getLeft(), total, totalPages, page, size);
    }

    public Optional<HolidaySetting> findByDate(String date) throws IOException {
        return repository.findByDate(date);
    }

    
    public HolidaySetting save(HolidaySetting setting) throws IOException {
        LocalDateTime now = LocalDateTime.now();
        HolidaySetting existing = repository.findByDate(setting.getHolidayDate()).orElse(null);
        if (existing != null) {
            existing.setRemark(setting.getRemark());
            existing.setUpdatedAt(now);
            repository.put(existing);
            return existing;
        }
        setting.setCreatedAt(now);
        setting.setUpdatedAt(now);
        repository.put(setting);
        return setting;
    }

    
    public void deleteByDate(String date) throws IOException {
        repository.deleteByDate(date);
    }
}
