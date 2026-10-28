package com.twfhclife.autodatastream.converter;

import com.twfhclife.autodatastream.config.DroolsConfig;
import com.twfhclife.autodatastream.handler.CommonLogRuleHandler;
import com.twfhclife.autodatastream.model.LogEntry;
import com.twfhclife.autodatastream.model.MisLoginInfo;
import org.springframework.stereotype.Component;


@Component
public class EserviceJdLoginConverter implements BizDocumentConverter {

    @Override
    public String bizName() {
        return DroolsConfig.BIZ_ESERVICE_JD_LOGIN;
    }

    @Override
    public Object convert(LogEntry entry) {
        if (entry == null || !bizName().equals(entry.getBizName())) {
            return entry;
        }
        MisLoginInfo info = new MisLoginInfo();
        info.setAccount(entry.getAttributes().get(CommonLogRuleHandler.prefixOf(bizName()) + "account"));
        info.setName(entry.getAttributes().get(CommonLogRuleHandler.prefixOf(bizName()) + "name"));
        info.setChannelBranch(entry.getAttributes().get(CommonLogRuleHandler.prefixOf(bizName()) + "channel_branch"));
        info.setRole(entry.getAttributes().get(CommonLogRuleHandler.prefixOf(bizName()) + "role"));
        info.setLoginTime(entry.getAttributes().get(CommonLogRuleHandler.prefixOf(bizName()) + "login_time"));
        info.setLogoutTime(entry.getAttributes().get(CommonLogRuleHandler.prefixOf(bizName()) + "logout_time"));
        info.setLoginResult(entry.getAttributes().get(CommonLogRuleHandler.prefixOf(bizName()) + "login_result"));
        info.setFailCount(toInteger(entry.getAttributes().get(CommonLogRuleHandler.prefixOf(bizName()) + "fail_count")));
        info.setSourceIp(entry.getAttributes().get(CommonLogRuleHandler.prefixOf(bizName()) + "source_ip"));
        info.setSourceArea(entry.getAttributes().get(CommonLogRuleHandler.prefixOf(bizName()) + "source_area"));
        info.setDeviceInfo(entry.getAttributes().get(CommonLogRuleHandler.prefixOf(bizName()) + "device_info"));
        info.setLoginMethod(entry.getAttributes().get(CommonLogRuleHandler.prefixOf(bizName()) + "login_method"));
        return info;
    }

    private static Integer toInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
