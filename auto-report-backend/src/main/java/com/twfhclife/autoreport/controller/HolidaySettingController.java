package com.twfhclife.autoreport.controller;

import com.twfhclife.autoreport.model.HolidaySetting;
import com.twfhclife.autoreport.model.PageResult;
import com.twfhclife.autoreport.service.HolidaySettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;


@RestController
@RequestMapping("/api/holiday-settings")
@RequiredArgsConstructor
public class HolidaySettingController {

    private final HolidaySettingService service;

    @GetMapping(params = "!date")
    public PageResult<HolidaySetting> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws IOException {
        return service.findAll(page, size);
    }

    @GetMapping(params = "date")
    public ResponseEntity<HolidaySetting> getByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) throws IOException {
        return service.findByDate(date.toString())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.ok(null));
    }

    @PostMapping
    public HolidaySetting save(@RequestBody HolidaySetting setting) throws IOException {
        return service.save(setting);
    }

    @DeleteMapping(params = "date")
    public ResponseEntity<Void> delete(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) throws IOException {
        service.deleteByDate(date.toString());
        return ResponseEntity.noContent().build();
    }
}
