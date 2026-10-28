package com.twfhclife.autodatastream.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.Request;
import org.opensearch.client.Response;
import org.opensearch.client.ResponseException;
import org.opensearch.client.RestClient;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@Component
@RequiredArgsConstructor
public class TimeCheckerService {

    static final String HOLIDAY_INDEX = "holiday_setting";

    
    static final int NON_WORK_START_HOUR = 22;
    static final int NON_WORK_END_HOUR = 6;

    
    static final int MAX_CACHE_DATES = 370;

    private final RestClient client;

    
    private static final List<DateTimeFormatter> LOGIN_DATETIME_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

    
    private static final List<DateTimeFormatter> LOGIN_DATE_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"));

    
    private final Map<String, Boolean> nonWorkDayCache = new ConcurrentHashMap<>();

    
    public boolean isNonWorkTime(long timeMillis) {
        if (timeMillis <= 0) {
            return false;
        }
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = Instant.ofEpochMilli(timeMillis);
        return isNonWorkTimeAt(timeMillis, LocalDate.ofInstant(instant, zone));
    }

    
    public boolean isNonWorkTime(String loginTime) {
        ParsedLogin login = parseLogin(loginTime);
        if (login == null) {
            return false;
        }
        if (login.dateOnly) {
            return isNonWorkDay(login.date);
        }
        return isNonWorkTime(login.epochMillis);
    }

    
    public long toEpochMillis(String loginTime) {
        ParsedLogin login = parseLogin(loginTime);
        return login == null ? 0L : login.epochMillis;
    }

    
    public String toDayKey(String loginTime) {
        ParsedLogin login = parseLogin(loginTime);
        return login == null ? null : login.date.toString();
    }

    
    public boolean isNonWorkTime(long eventTimeMillis, String loginTime) {
        ParsedLogin login = parseLogin(loginTime);
        if (login == null) {
            return isNonWorkTime(eventTimeMillis);
        }
        if (login.dateOnly) {
            return isNonWorkTimeAt(eventTimeMillis, login.date);
        }
        return isNonWorkTime(login.epochMillis);
    }

    private boolean isNonWorkTimeAt(long timeMillis, LocalDate date) {
        if (isNonWorkDay(date)) {
            return true;
        }
        if (timeMillis <= 0) {
            return false;
        }
        int hour = Instant.ofEpochMilli(timeMillis).atZone(ZoneId.systemDefault()).getHour();
        return hour >= NON_WORK_START_HOUR || hour < NON_WORK_END_HOUR;
    }

    
    static ParsedLogin parseLogin(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String v = value.trim();
        if (v.length() == 8 && v.chars().allMatch(Character::isDigit)) {
            try {
                LocalDate d = LocalDate.of(
                        Integer.parseInt(v.substring(0, 4)),
                        Integer.parseInt(v.substring(4, 6)),
                        Integer.parseInt(v.substring(6, 8)));
                return ParsedLogin.dateOnly(d);
            } catch (DateTimeException e) {
                return null;
            }
        }
        for (DateTimeFormatter formatter : LOGIN_DATETIME_FORMATTERS) {
            try {
                return ParsedLogin.full(LocalDateTime.parse(v, formatter));
            } catch (DateTimeParseException ignored) {
                
            }
        }
        for (DateTimeFormatter formatter : LOGIN_DATE_FORMATTERS) {
            try {
                return ParsedLogin.dateOnly(LocalDate.parse(v, formatter));
            } catch (DateTimeParseException ignored) {
                
            }
        }
        return null;
    }

    
    static final class ParsedLogin {

        final boolean dateOnly;
        final LocalDate date;
        final long epochMillis;

        private ParsedLogin(boolean dateOnly, LocalDate date, long epochMillis) {
            this.dateOnly = dateOnly;
            this.date = date;
            this.epochMillis = epochMillis;
        }

        static ParsedLogin dateOnly(LocalDate date) {
            return new ParsedLogin(true, date,
                    date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
        }

        static ParsedLogin full(LocalDateTime dateTime) {
            long epochMillis = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            return new ParsedLogin(false, dateTime.toLocalDate(), epochMillis);
        }
    }

    
    public boolean isNonWorkDay(LocalDate date) {
        try {
            return resolveNonWorkDay(date);
        } catch (Exception e) {
            log.warn("非上班日判定失敗（暫以上班日降級，不更新快取）：{}", e.getMessage());
            return false;
        }
    }

    private boolean resolveNonWorkDay(LocalDate date) throws IOException {
        if (isWeekend(date)) {
            return true;
        }
        String key = date.toString();
        Boolean cached = nonWorkDayCache.get(key);
        if (cached != null) {
            return cached;
        }
        boolean holiday = isHolidayOrThrow(date);
        if (nonWorkDayCache.size() >= MAX_CACHE_DATES) {
            nonWorkDayCache.clear();
        }
        nonWorkDayCache.put(key, holiday);
        return holiday;
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    
    private boolean isHolidayOrThrow(LocalDate date) throws IOException {
        try {
            client.performRequest(new Request("GET", "/" + HOLIDAY_INDEX + "/_doc/" + date));
            return true;
        } catch (ResponseException e) {
            if (e.getResponse().getStatusLine().getStatusCode() == 404) {
                return false;
            }
            throw e;
        }
    }
}