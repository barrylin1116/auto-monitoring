package com.twfhclife.autodatastream.config;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class DroolsConfig {

    public static final String BIZ_MISF_LOGIN = "MISF_LOGIN";
    public static final String BIZ_ESERVICE_JD_LOGIN = "ESERVICE_JD_LOGIN";
    public static final String ESERVICE_JD_OPERATION = "ESERVICE_JD_OPERATION";
    public static final String LOGIN_RULES_KSESSION = "loginRulesKSession";

    @Bean
    public KieContainer kieContainer() {
        return KieServices.Factory.get().getKieClasspathContainer();
    }

    
    @Bean
    public Map<String, String> bizSessionNames() {
        Map<String, String> names = new HashMap<>();
        names.put(BIZ_MISF_LOGIN, LOGIN_RULES_KSESSION);
        names.put(BIZ_ESERVICE_JD_LOGIN, LOGIN_RULES_KSESSION);
        return names;
    }
}
