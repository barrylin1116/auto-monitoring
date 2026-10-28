package com.twfhclife.autoreport.service;

import com.twfhclife.autoreport.model.ExceptionEvent;
import com.twfhclife.autoreport.model.ExceptionAttachment;
import com.twfhclife.autoreport.model.ExceptionProcessRecord;
import com.twfhclife.autoreport.model.ExceptionEventUpdateRequest;
import com.twfhclife.autoreport.model.PageResult;
import com.twfhclife.autoreport.repository.opensearch.BizLogRepository;
import com.twfhclife.autoreport.repository.opensearch.ExceptionAttachmentRepository;
import com.twfhclife.autoreport.repository.opensearch.ExceptionEventRepository;
import com.twfhclife.autoreport.repository.opensearch.ExceptionProcessRecordRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExceptionEventService {

    private static final String ACTION_SAVE = "SAVE";
    private static final String ACTION_CLOSE = "CLOSE";
    private static final String STATUS_CLOSED = "CLOSED";

    
    private static final long MAX_ATTACHMENT_SIZE = 10L * 1024 * 1024;
    
    private static final Set<String> ALLOWED_ATTACHMENT_EXTENSIONS =
            Set.of("png", "jpeg", "jpg", "pdf");

    private final ExceptionEventRepository repository;
    private final ExceptionProcessRecordRepository processRecordRepository;
    private final ExceptionAttachmentRepository attachmentRepository;
    private final BizLogRepository bizLogRepository;

    
    public List<Map<String, Object>> findRelatedLogs(String id) throws IOException {
        ExceptionEvent event = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "異常事件不存在：" + id));
        return bizLogRepository.findByIds(event.getBizName(), event.getRelatedLogIds());
    }

    
    public ExceptionEvent findById(String id) throws IOException {
        ExceptionEvent event = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "異常事件不存在：" + id));
        event.setAttachments(attachmentRepository.findByEventId(id));
        return event;
    }

    
    public PageResult<ExceptionEvent> findAll(String systemType, String eventType, String riskLevel,
                                              String status, String account, int page, int size)
            throws IOException {
        Pair<List<ExceptionEvent>, Long> data =
                repository.search(systemType, eventType, riskLevel, status, account, page * size, size);
        long total = data.getRight();
        int totalPages = size > 0 ? (int) Math.ceil((double) total / size) : 0;
        return new PageResult<>(data.getLeft(), total, totalPages, page, size);
    }

    
    public List<ExceptionProcessRecord> findProcessRecords(String id) throws IOException {
        return processRecordRepository.findByEventId(id);
    }

    
    public List<ExceptionAttachment> findAttachments(String eventId) throws IOException {
        return attachmentRepository.findByEventId(eventId);
    }

    
    public ExceptionAttachment findAttachment(String attachmentId) throws IOException {
        return attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "附件不存在：" + attachmentId));
    }

    
    public ExceptionAttachment uploadAttachment(String eventId, MultipartFile file, String uploadedBy)
            throws IOException {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "上傳檔案不得為空");
        }
        if (file.getSize() > MAX_ATTACHMENT_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "單檔上限 10MB");
        }
        String ext = extensionOf(file.getOriginalFilename());
        if (!ALLOWED_ATTACHMENT_EXTENSIONS.contains(ext)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "僅允許 PNG、JPEG、PDF");
        }
        String id = UUID.randomUUID().toString();
        ExceptionAttachment attachment = ExceptionAttachment.builder()
                .id(id)
                .eventId(eventId)
                .fileName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .size(file.getSize())
                .content(Base64.getEncoder().encodeToString(file.getBytes()))
                .uploadedBy(uploadedBy)
                .uploadedAt(System.currentTimeMillis())
                .build();
        attachmentRepository.put(attachment);
        return attachment;
    }

    
    public void deleteAttachment(String attachmentId) throws IOException {
        attachmentRepository.deleteById(attachmentId);
    }

    private static String extensionOf(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase();
    }

    
    public ExceptionEvent update(String id, ExceptionEventUpdateRequest request) throws IOException {
        ExceptionEvent existing = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "找不到異常事件：" + id));
        String status = request.getStatus() == null || request.getStatus().isBlank()
                ? existing.getStatus() : request.getStatus();
        String processedBy = request.getProcessedBy();
        String processMethod = request.getProcessMethod();
        String riskAssessment = request.getRiskAssessment();
        String processRemark = request.getProcessRemark();
        long processedAt = request.getProcessedAt() != null
                ? request.getProcessedAt() : System.currentTimeMillis();

        repository.updateProcessing(id, status, processedBy, processMethod, riskAssessment, processRemark, processedAt);

        String action = request.getAction();
        if (action == null || action.isBlank()) {
            action = STATUS_CLOSED.equals(status) ? ACTION_CLOSE : ACTION_SAVE;
        }
        processRecordRepository.put(ExceptionProcessRecord.builder()
                .id(UUID.randomUUID().toString())
                .eventId(id)
                .action(action)
                .status(status)
                .processedBy(processedBy)
                .processMethod(processMethod)
                .riskAssessment(riskAssessment)
                .processRemark(processRemark)
                .processedAt(processedAt)
                .build());

        existing.setStatus(status);
        existing.setProcessedBy(processedBy);
        existing.setProcessMethod(processMethod);
        existing.setRiskAssessment(riskAssessment);
        existing.setProcessRemark(processRemark);
        existing.setProcessedAt(processedAt);
        return existing;
    }
}
