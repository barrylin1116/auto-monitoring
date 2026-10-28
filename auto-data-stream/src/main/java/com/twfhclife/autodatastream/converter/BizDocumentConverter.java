package com.twfhclife.autodatastream.converter;

import com.twfhclife.autodatastream.model.LogEntry;


public interface BizDocumentConverter {

    
    String bizName();

    
    Object convert(LogEntry entry);
}
