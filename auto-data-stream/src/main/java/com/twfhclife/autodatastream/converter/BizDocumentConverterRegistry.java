package com.twfhclife.autodatastream.converter;

import com.twfhclife.autodatastream.model.LogEntry;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
public class BizDocumentConverterRegistry {

    private final Map<String, BizDocumentConverter> convertersByBizName;

    public BizDocumentConverterRegistry(List<BizDocumentConverter> converters) {
        Map<String, BizDocumentConverter> map = new HashMap<>();
        for (BizDocumentConverter converter : converters) {
            map.put(converter.bizName(), converter);
        }
        this.convertersByBizName = Map.copyOf(map);
    }

    
    public Object convert(LogEntry entry) {
        if (entry == null || entry.getBizName() == null) {
            return entry;
        }
        BizDocumentConverter converter = convertersByBizName.get(entry.getBizName());
        return converter == null ? entry : converter.convert(entry);
    }
}
