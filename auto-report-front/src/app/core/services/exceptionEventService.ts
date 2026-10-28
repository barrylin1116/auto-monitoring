import apiClient from './apiClient';
import type { PageResult } from './holidaySettingService';


export interface ExceptionEvent {
  id?: string | null;
  eventNo?: string | null;
  occurredAt?: number | null;
  eventType?: string | null;
  riskLevel?: string | null;
  systemType?: string | null;
  bizName?: string | null;
  account?: string | null;
  name?: string | null;
  channelBranch?: string | null;
  deviceInfo?: string | null;
  sourceIp?: string | null;
  description?: string | null;
  detailDescription?: string | null;
  status?: string | null;
  processedBy?: string | null;
  processMethod?: string | null;
  riskAssessment?: string | null;
  processRemark?: string | null;
  processedAt?: number | null;
  relatedLogIds?: string[] | null;
  attachments?: ExceptionAttachment[] | null;
}


export interface ExceptionAttachment {
  id?: string | null;
  eventId?: string | null;
  fileName?: string | null;
  contentType?: string | null;
  size?: number | null;
  uploadedBy?: string | null;
  uploadedAt?: number | null;
}


export interface ExceptionProcessRecord {
  id?: string | null;
  eventId?: string | null;
  action?: string | null;
  status?: string | null;
  processedBy?: string | null;
  processMethod?: string | null;
  riskAssessment?: string | null;
  processRemark?: string | null;
  processedAt?: number | null;
}


export interface ExceptionEventUpdatePayload {
  status?: string;
  processedBy?: string;
  processMethod?: string;
  riskAssessment?: string;
  processRemark?: string;
  processedAt?: number;
  action?: 'SAVE' | 'CLOSE';
}


export interface ExceptionEventQuery {
  systemType?: string;
  eventType?: string;
  riskLevel?: string;
  status?: string;
  account?: string;
}

export const fetchExceptionEvents = async (
  query: ExceptionEventQuery = {},
  page = 0,
  size = 10
): Promise<PageResult<ExceptionEvent>> => {
  const { data } = await apiClient.get<PageResult<ExceptionEvent>>('/api/exception-events', {
    params: { page, size, ...query }
  });
  return (
    data || {
      content: [],
      totalElements: 0,
      totalPages: 0,
      number: page,
      size
    }
  );
};


export const updateExceptionEvent = async (
  id: string,
  payload: ExceptionEventUpdatePayload
): Promise<ExceptionEvent> => {
  const { data } = await apiClient.put<ExceptionEvent>(`/api/exception-events/${id}`, payload);
  return data;
};


export const fetchExceptionEvent = async (id: string): Promise<ExceptionEvent> => {
  const { data } = await apiClient.get<ExceptionEvent>(`/api/exception-events/${id}`);
  return data;
};


export const fetchExceptionProcessRecords = async (
  id: string
): Promise<ExceptionProcessRecord[]> => {
  const { data } = await apiClient.get<ExceptionProcessRecord[]>(
    `/api/exception-events/${id}/process-records`
  );
  return data || [];
};


export type RelatedLogRecord = Record<string, unknown>;


export const fetchExceptionRelatedLogs = async (id: string): Promise<RelatedLogRecord[]> => {
  const { data } = await apiClient.get<RelatedLogRecord[]>(
    `/api/exception-events/${id}/related-logs`
  );
  return data || [];
};


export const uploadExceptionAttachments = async (
  eventId: string,
  files: File[],
  uploadedBy?: string
): Promise<ExceptionAttachment[]> => {
  const form = new FormData();
  files.forEach((file) => form.append('files', file));
  if (uploadedBy) form.append('uploadedBy', uploadedBy);
  const { data } = await apiClient.post<ExceptionAttachment[]>(
    `/api/exception-events/${eventId}/attachments`,
    form
  );
  return data || [];
};


export const deleteExceptionAttachment = async (attachmentId: string): Promise<void> => {
  await apiClient.delete(`/api/exception-events/attachments/${attachmentId}`);
};


export const exceptionAttachmentDownloadUrl = (attachmentId: string): string =>
  `/api/exception-events/attachments/${attachmentId}`;
