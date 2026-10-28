package com.twfhclife.autodatastream.converter;

import com.twfhclife.autodatastream.config.DroolsConfig;
import com.twfhclife.autodatastream.handler.CommonLogRuleHandler;
import com.twfhclife.autodatastream.model.EserviceJdOperationInfo;
import com.twfhclife.autodatastream.model.LogEntry;
import org.springframework.stereotype.Component;


@Component
public class EserviceJdOperationConverter implements BizDocumentConverter {

    @Override
    public String bizName() {
        return DroolsConfig.ESERVICE_JD_OPERATION;
    }

    @Override
    public Object convert(LogEntry entry) {
        if (entry == null || !bizName().equals(entry.getBizName())) {
            return entry;
        }
        EserviceJdOperationInfo info = new EserviceJdOperationInfo();
        info.setAccount(entry.getAttributes().get(CommonLogRuleHandler.prefixOf(bizName()) + "account"));
        info.setAction(entry.getAttributes().get(CommonLogRuleHandler.prefixOf(bizName()) + "action"));
        info.setOperationTime(entry.getAttributes().get(CommonLogRuleHandler.prefixOf(bizName()) + "time"));
        info.setActionContent(entry.getAttributes().get(CommonLogRuleHandler.prefixOf(bizName()) + "action_content"));
        info.setActionResult(entry.getAttributes().get(CommonLogRuleHandler.prefixOf(bizName()) + "action_result"));
        info.setFunctionScreen(entry.getAttributes().get(CommonLogRuleHandler.prefixOf(bizName()) + "function_screen"));
        return info;
    }
}
