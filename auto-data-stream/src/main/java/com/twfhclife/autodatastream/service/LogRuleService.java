package com.twfhclife.autodatastream.service;

import com.twfhclife.autodatastream.model.LogEntry;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LogRuleService {

    private static final Logger log = LoggerFactory.getLogger(LogRuleService.class);

    
    private static final long RESET_IDLE_MILLIS = 24 * 60 * 60 * 1000L;

    private final KieContainer kieContainer;
    private final Map<String, String> bizSessionNames;
    private final TimeCheckerService timeCheckerService;
    private final Map<String, KieSession> ruleSessions = new ConcurrentHashMap<>();
    private final Map<String, Long> lastEventReceiveMillis = new ConcurrentHashMap<>();

public LogRuleService(Map<String, KieSession> ruleSessions,
                      TimeCheckerService timeCheckerService) {
        this.kieContainer = null;
        this.bizSessionNames = Collections.emptyMap();
        this.timeCheckerService = timeCheckerService;
        this.ruleSessions.putAll(ruleSessions);
        
        ruleSessions.values().forEach(s -> applyGlobals(s, null));
    }

    @Autowired
    public LogRuleService(KieContainer kieContainer,
                          Map<String, String> bizSessionNames,
                          TimeCheckerService timeCheckerService) {
        this.kieContainer = kieContainer;
        this.bizSessionNames = new HashMap<>(bizSessionNames);
        this.timeCheckerService = timeCheckerService;
    }

    public synchronized LogEntry evaluate(LogEntry entry) {
        return evaluate(entry, null);
    }

    
    public synchronized LogEntry evaluate(LogEntry entry, Collection<String> allowedRuleNames) {
        String bizName = entry.getBizName();
        if (bizName == null || !isRegistered(bizName)) {
            log.debug("No rule session registered for bizName [{}], skip evaluation", bizName);
            return entry;
        }

        long now = System.currentTimeMillis();
        if (isIdle(bizName, now)) {
            log.info("bizName [{}] idle for more than 15 minutes, resetting rule session", bizName);
            disposeSession(bizName);
        }

        KieSession session = getOrCreateSession(bizName);
        if (session == null) {
            log.warn("Unable to obtain rule session for bizName [{}], skip evaluation", bizName);
            return entry;
        }

        lastEventReceiveMillis.put(bizName, now);
        session.insert(entry);
        if (allowedRuleNames == null || allowedRuleNames.isEmpty()) {
            session.fireAllRules();
        } else {
            session.fireAllRules(match -> allowedRuleNames.contains(match.getRule().getName()));
        }
        return entry;
    }

    private boolean isRegistered(String bizName) {
        return bizSessionNames.containsKey(bizName) || ruleSessions.containsKey(bizName);
    }

    private boolean isIdle(String bizName, long now) {
        Long last = lastEventReceiveMillis.get(bizName);
        return last != null && now - last > RESET_IDLE_MILLIS;
    }

    private void disposeSession(String bizName) {
        KieSession session = ruleSessions.remove(bizName);
        if (session != null) {
            try {
                session.dispose();
            } catch (Exception e) {
                log.warn("Failed to dispose rule session for bizName [{}]", bizName, e);
            }
        }
    }

    private KieSession getOrCreateSession(String bizName) {
        KieSession session = ruleSessions.get(bizName);
        if (session != null) {
            return session;
        }
        String sessionName = bizSessionNames.get(bizName);
        if (sessionName == null || kieContainer == null) {
            return null;
        }
        KieSession created = kieContainer.newKieSession(sessionName);
        applyGlobals(created, bizName);
        ruleSessions.put(bizName, created);
        return created;
    }

    private void applyGlobals(KieSession session, String bizName) {
        if (timeCheckerService != null) {
            session.setGlobal("timeCheckerService", timeCheckerService);
        }
        String prefix = computeSystemPrefix(bizName);
        if (prefix != null) {
            session.setGlobal("systemPrefix", prefix);
        }
    }

    
    private String computeSystemPrefix(String bizName) {
        if (bizName == null || !bizName.endsWith("LOGIN")) {
            return null;
        }
        int idx = bizName.lastIndexOf("_LOGIN");
        if (idx <= 0) {
            return null;
        }
        return bizName.substring(0, idx).toLowerCase() + ".login.";
    }
}
