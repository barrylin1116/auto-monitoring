package com.twfhclife.autoreport.controller;

import com.twfhclife.autoreport.model.ExceptionEvent;
import com.twfhclife.autoreport.model.ExceptionAttachment;
import com.twfhclife.autoreport.model.ExceptionProcessRecord;
import com.twfhclife.autoreport.model.ExceptionEventUpdateRequest;
import com.twfhclife.autoreport.model.PageResult;
import com.twfhclife.autoreport.service.ExceptionEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/exception-events")
@RequiredArgsConstructor
public class ExceptionEventController {

    private final ExceptionEventService service;

    @GetMapping
    public PageResult<ExceptionEvent> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String systemType,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String account) throws IOException {
        return service.findAll(systemType, eventType, riskLevel, status, account, page, size);
    }

    @PutMapping("/{id}")
    public ExceptionEvent update(@PathVariable String id,
                                 @RequestBody ExceptionEventUpdateRequest request) throws IOException {
        return service.update(id, request);
    }

    
    @GetMapping("/{id}")
    public ExceptionEvent get(@PathVariable String id) throws IOException {
        return service.findById(id);
    }

    @GetMapping("/{id}/process-records")
    public List<ExceptionProcessRecord> processRecords(@PathVariable String id) throws IOException {
        return service.findProcessRecords(id);
    }

    
    @GetMapping("/{id}/related-logs")
    public List<Map<String, Object>> relatedLogs(@PathVariable String id) throws IOException {
        return service.findRelatedLogs(id);
    }

    
    @GetMapping("/{id}/attachments")
    public List<ExceptionAttachment> attachments(@PathVariable String id) throws IOException {
        return service.findAttachments(id);
    }

    
    @PostMapping("/{id}/attachments")
    public List<ExceptionAttachment> uploadAttachments(@PathVariable String id,
                                                       @RequestParam("files") List<MultipartFile> files,
                                                       @RequestParam(required = false) String uploadedBy)
            throws IOException {
        List<ExceptionAttachment> uploaded = new java.util.ArrayList<>();
        for (MultipartFile file : files) {
            uploaded.add(service.uploadAttachment(id, file, uploadedBy));
        }
        return uploaded;
    }

    
    @GetMapping("/attachments/{attachmentId}")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable String attachmentId) throws IOException {
        ExceptionAttachment attachment = service.findAttachment(attachmentId);
        byte[] bytes = Base64.getDecoder().decode(attachment.getContent());
        String fileName = attachment.getFileName() == null ? attachment.getId() : attachment.getFileName();
        String encodedName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        String contentType = attachment.getContentType() == null
                ? MediaType.APPLICATION_OCTET_STREAM_VALUE : attachment.getContentType();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedName)
                .body(new ByteArrayResource(bytes));
    }

    
    @DeleteMapping("/attachments/{attachmentId}")
    public void deleteAttachment(@PathVariable String attachmentId) throws IOException {
        service.deleteAttachment(attachmentId);
    }
}
