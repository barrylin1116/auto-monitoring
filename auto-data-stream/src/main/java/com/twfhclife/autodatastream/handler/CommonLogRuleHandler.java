package com.twfhclife.autodatastream.handler;

import com.twfhclife.autodatastream.model.ExceptionEvent;
import com.twfhclife.autodatastream.model.ExceptionSetting;
import com.twfhclife.autodatastream.model.LogEntry;
import com.twfhclife.autodatastream.repository.ConfigDictionaryRepository;
import com.twfhclife.autodatastream.repository.ExceptionEventWriter;
import com.twfhclife.autodatastream.repository.ExceptionSettingRepository;
import com.twfhclife.autodatastream.service.AccountService;
import com.twfhclife.autodatastream.service.LogRuleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class CommonLogRuleHandler {

    private static final Logger log = LoggerFactory.getLogger(CommonLogRuleHandler.class);

    private static final String EVENT_STATUS_PENDING = "PENDING";
    private static final String EVENT_STATUS_CLOSED = "CLOSED";

    private static final String RISK_DEFAULT = "MEDIUM";

    
    private static final String DICT_MATCHED_RULE = "matchedRule";
    
    private static final String DICT_MATCHED_RULE_DETAIL = "matchedRuleDetail";

    private final LogRuleService logRuleService;
    private final ExceptionSettingRepository exceptionSettingRepository;
    private final ExceptionEventWriter exceptionEventWriter;
    private final ConfigDictionaryRepository configDictionaryRepository;
    private final AccountService accountService;

    
    private final Map<String, Map<String, ExceptionSetting>> settingCache = new ConcurrentHashMap<>();

    
    private volatile String lastUpdatedAt;

    
    private volatile Map<String, String> matchedRuleLabels = Map.of();

    
    private volatile Map<String, String> matchedRuleDetails = Map.of();

    public CommonLogRuleHandler(LogRuleService logRuleService,
                                ExceptionSettingRepository exceptionSettingRepository,
                                ExceptionEventWriter exceptionEventWriter,
                                ConfigDictionaryRepository configDictionaryRepository,
                                AccountService accountService) {
        this.logRuleService = logRuleService;
        this.exceptionSettingRepository = exceptionSettingRepository;
        this.exceptionEventWriter = exceptionEventWriter;
        this.configDictionaryRepository = configDictionaryRepository;
        this.accountService = accountService;
    }

    
    public void handle(LogEntry entry) {
        List<ExceptionSetting> settings = findSettings(entry.getSystem());
        if (settings.isEmpty()) {
            log.info("No exception setting configured for system [{}], skip rule evaluation",
                    entry.getSystem());
            return;
        }

        List<String> allowed = allowedEventTypes(settings);
        if (allowed.isEmpty()) {
            log.info("No eventType configured in exception settings of system [{}], skip rule evaluation",
                    entry.getSystem());
            return;
        }

        logRuleService.evaluate(entry, allowed);

        List<String> matched = entry.getMatchedRules();
        if (matched == null || matched.isEmpty()) {
            log.info("Log [{}] from [{}] triggered no rules", entry.getBizName(), entry.getSource());
            return;
        }

        List<String> triggered = filterByEventTypes(matched, allowed);
        if (triggered.isEmpty()) {
            log.info("Log [{}] matched {} but none configured in eventType of exception settings [{}], skip",
                    entry.getBizName(), matched, entry.getSystem());
            return;
        }

        String account = extractAccount(entry);
        log.warn("Log [{}] from [{}/{}] triggered rules: {}",
                entry.getBizName(), entry.getSource(), account, triggered);

        for (String rule : triggered) {
            ExceptionSetting setting = settingFor(settings, rule);
            boolean processed = false;
            AutoProcessAction action = AutoProcessAction.of(setting);
            if (action != AutoProcessAction.NONE) {
                processed = accountService.apply(account, action.accountStatus()).success();
                if (!processed) {
                    log.warn("autoProcess [{}] ({}) is fail, status change to pending.", account, action);
                }
            }
            writeExceptionEvent(entry, setting, account, rule, processed);
        }
    }

    
    @Scheduled(fixedDelayString = "${app.exception-setting.refresh-interval-ms:60000}",
            initialDelayString = "${app.exception-setting.refresh-initial-delay-ms:0}")
    public synchronized void refreshSettings() {
        try {
            List<ExceptionSetting> changed = exceptionSettingRepository.findUpdatedAfter(lastUpdatedAt);
            String watermark = lastUpdatedAt;
            for (ExceptionSetting setting : changed) {
                String systemType = setting.getSystemType();
                String eventType = setting.getEventType();
                if (systemType == null || systemType.isBlank() || eventType == null || eventType.isBlank()) {
                    continue;
                }
                settingCache.computeIfAbsent(systemType, k -> new ConcurrentHashMap<>())
                        .put(eventType, setting);
                String updatedAt = setting.getUpdatedAt();
                if (updatedAt != null && (watermark == null || updatedAt.compareTo(watermark) > 0)) {
                    watermark = updatedAt;
                }
            }
            if (!changed.isEmpty()) {
                lastUpdatedAt = watermark;
                log.info("Refreshed {} exception setting(s), lastUpdatedAt={}", changed.size(), lastUpdatedAt);
            }
        } catch (Exception e) {
            log.warn("Failed to refresh exception settings: {}", e.getMessage());
        }
    }

    
    @Scheduled(fixedDelayString = "${app.dictionary.refresh-interval-ms:60000}",
            initialDelayString = "${app.dictionary.refresh-initial-delay-ms:0}")
    public synchronized void refreshDictionaries() {
        try {
            Map<String, String> labels = configDictionaryRepository.findByType(DICT_MATCHED_RULE);
            Map<String, String> details = configDictionaryRepository.findByType(DICT_MATCHED_RULE_DETAIL);
            matchedRuleLabels = labels;
            matchedRuleDetails = details;
            log.info("Refreshed dictionaries: matchedRule={}, matchedRuleDetail={}",
                    labels.size(), details.size());
        } catch (Exception e) {
            log.warn("Failed to refresh dictionaries: {}", e.getMessage());
        }
    }

    
    private static String extractAccount(LogEntry entry) {
        Map<String, String> attrs = entry.getAttributes();
        return attrs == null ? null : attrs.get(prefixOf(entry.getBizName()) + "account");
    }

    
    private static String extractName(LogEntry entry) {
        Map<String, String> attrs = entry.getAttributes();
        return attrs == null ? null : attrs.get(prefixOf(entry.getBizName()) + "name");
    }

    
    private static String extractChannelBranch(LogEntry entry) {
        Map<String, String> attrs = entry.getAttributes();
        return attrs == null ? null : attrs.get(prefixOf(entry.getBizName()) + "channel_branch");
    }

    
    private static String extractDeviceInfo(LogEntry entry) {
        Map<String, String> attrs = entry.getAttributes();
        return attrs == null ? null : attrs.get(prefixOf(entry.getBizName()) + "device_info");
    }

    
    private static String extractSourceIp(LogEntry entry) {
        Map<String, String> attrs = entry.getAttributes();
        return attrs == null ? null : attrs.get(prefixOf(entry.getBizName()) + "source_ip");
    }

    
    public static String prefixOf(String bizName) {
        if (bizName == null || bizName.isBlank()) {
            return "";
        }
        int last = bizName.lastIndexOf('_');
        if (last < 0) {
            return bizName.toLowerCase() + ".";
        }
        return bizName.substring(0, last).toLowerCase() + "." + bizName.substring(last + 1).toLowerCase() + ".";
    }

    
    private static String riskLevel(ExceptionSetting setting) {
        String risk = setting == null ? null : setting.getRiskLevel();
        return risk != null && !risk.isBlank() ? risk : RISK_DEFAULT;
    }

    
    private String describe(String rule) {
        String label = rule == null ? null : matchedRuleLabels.get(rule);
        return label != null ? label : rule;
    }

    
    private String describeDetail(String rule) {
        return rule == null ? null : matchedRuleDetails.get(rule);
    }

    
    private List<ExceptionSetting> findSettings(String system) {
        if (system == null || system.isBlank()) {
            return List.of();
        }
        Map<String, ExceptionSetting> byEventType = settingCache.get(system);
        if (byEventType == null) {
            return List.of();
        }
        return byEventType.values().stream()
                .filter(setting -> system.equals(setting.getSystemType()))
                .toList();
    }

    
    private static List<String> allowedEventTypes(List<ExceptionSetting> settings) {
        if (settings == null || settings.isEmpty()) {
            return List.of();
        }
        return settings.stream()
                .map(ExceptionSetting::getEventType)
                .filter(et -> et != null && !et.isBlank())
                .distinct()
                .toList();
    }

    
    private static ExceptionSetting settingFor(List<ExceptionSetting> settings, String rule) {
        return settings.stream()
                .filter(setting -> rule.equals(setting.getEventType()))
                .findFirst()
                .orElse(null);
    }

    
    private static List<String> filterByEventTypes(List<String> matched, List<String> allowed) {
        return matched.stream().filter(allowed::contains).toList();
    }

    
    private void writeExceptionEvent(LogEntry entry, ExceptionSetting setting, String account,
                                     String rule, boolean processed) {
        long occurredAt = entry.getTimeMillis() > 0 ? entry.getTimeMillis() : System.currentTimeMillis();
        long now = System.currentTimeMillis();
        String sourceIp = extractSourceIp(entry);
        boolean autoClose = isAutoClose(setting);
        String risk = riskLevel(setting);

        ExceptionEvent event = new ExceptionEvent();
        event.setEventType(rule);
        event.setRiskLevel(risk);
        event.setSystemType(entry.getSystem());
        event.setBizName(entry.getBizName());
        event.setAccount(account);
        event.setName(extractName(entry));
        event.setChannelBranch(extractChannelBranch(entry));
        event.setDeviceInfo(extractDeviceInfo(entry));
        event.setSourceIp(sourceIp);
        event.setDescription(describe(rule));
        event.setDetailDescription(describeDetail(rule));
        event.setStatus(processed && autoClose ? EVENT_STATUS_CLOSED : EVENT_STATUS_PENDING);
        event.setOccurredAt(occurredAt);
        event.setCreatedAt(now);
        List<String> relatedLogIds = entry.getRelatedLogIds(rule);
        event.setRelatedLogIds(relatedLogIds.isEmpty() ? null : relatedLogIds);
        try {
            exceptionEventWriter.write(event);
        } catch (Exception e) {
            log.warn("Failed to write exception event for rule [{}]: {}", rule, e.getMessage());
        }
    }

    
    private static boolean isAutoClose(ExceptionSetting setting) {
        return setting != null && "Y".equalsIgnoreCase(setting.getAutoClose());
    }

    
    private enum AutoProcessAction {
        NONE(null),
        LOCK("locked"),
        DISABLE("disabled");

        private final String accountStatus;

        AutoProcessAction(String accountStatus) {
            this.accountStatus = accountStatus;
        }

        String accountStatus() {
            return accountStatus;
        }

        static AutoProcessAction of(ExceptionSetting setting) {
            if (setting == null || setting.getAutoProcess() == null) {
                return NONE;
            }
            return switch (setting.getAutoProcess().trim().toUpperCase()) {
                case "LOCK" -> LOCK;
                case "DISABLE" -> DISABLE;
                default -> NONE;
            };
        }
    }

}
