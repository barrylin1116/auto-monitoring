package com.twfhclife.autoreport.config;

import com.twfhclife.autoreport.model.ConfigDictionary;
import com.twfhclife.autoreport.repository.opensearch.ConfigDictionaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class ConfigDictionarySeeder implements ApplicationRunner {

    private final ConfigDictionaryRepository repository;

    @Override
    public void run(ApplicationArguments args) {
        try {
            if (!repository.indexExists()) {
                log.warn("配置字典索引不存在，略過種子資料寫入：{}", ConfigDictionaryRepository.INDEX);
                return;
            }
            Set<String> existing = new HashSet<>();
            for (ConfigDictionary item : repository.findAll()) {
                existing.add(key(item.getType(), item.getCode()));
            }
            int added = 0;
            for (ConfigDictionary item : defaults()) {
                if (existing.contains(key(item.getType(), item.getCode()))) {
                    continue;
                }
                repository.put(item);
                added++;
            }
            if (added > 0) {
                log.info("配置字典已補寫預設種子資料 {} 筆", added);
            }
        } catch (Exception e) {
            log.warn("配置字典種子資料寫入失敗（服務未啟動？）: {}", e.getMessage());
        }
    }

    private static String key(String type, String code) {
        return type + ":" + code;
    }

    private static List<ConfigDictionary> defaults() {
        return List.of(
                item("system_type", "MISF", "行動投保", 1),
                item("system_type", "ESERVICE_JD", "經代系統", 2),
                item("bizName", "MISF_LOGIN", "行動投保登錄", 1),
                item("processStatus", "SUCCESS", "成功", 1),
                item("processStatus", "FAILED", "失敗", 2),
                item("processStatus", "SKIPPED", "未處理", 3),
                item("matchedRule", "TOO_FREQUENT_LOGINS", "頻繁登入", 1),
                item("matchedRule", "TOO_MANY_DAILY_LOGINS", "單日登入次數異常", 2),
                item("matchedRule", "OFF_HOURS_LOGIN", "非上班時間登入", 3),
                item("matchedRuleDetail", "TOO_FREQUENT_LOGINS", "同一帳號於 10 分鐘內登入達 5 次（含）以上。", 1),
                item("matchedRuleDetail", "TOO_MANY_DAILY_LOGINS", "同一帳號單日登入次數達 20 次（含）以上。", 2),
                item("matchedRuleDetail", "OFF_HOURS_LOGIN", "於非上班時間（平日 22:00～06:00 或非上班日）登入。", 3),
                item("riskLevel", "LOW", "低", 1),
                item("riskLevel", "MEDIUM", "中", 2),
                item("riskLevel", "HIGH", "高", 3),
                item("eventStatus", "PENDING", "待處理", 1),
                item("eventStatus", "IN_PROGRESS", "處理中", 2),
                item("eventStatus", "TRACKING", "持續追蹤", 3),
                item("eventStatus", "CLOSED", "已結案", 4));
    }

    private static ConfigDictionary item(String type, String code, String label, int sort) {
        ConfigDictionary item = new ConfigDictionary();
        item.setType(type);
        item.setCode(code);
        item.setLabel(label);
        item.setSort(sort);
        return item;
    }
}