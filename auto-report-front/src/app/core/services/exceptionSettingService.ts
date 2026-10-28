import apiClient from './apiClient';

export type AutoProcess = 'NONE' | 'LOCK' | 'DISABLE';

export type RiskLevel = 'LOW' | 'MEDIUM' | 'HIGH';

export type AutoClose = 'Y' | 'N';

export interface ExceptionSetting {
  id?: number;
  systemType: string;
  eventType: string;
  autoProcess: AutoProcess;
  riskLevel?: RiskLevel;
  autoClose?: AutoClose;
  createdAt?: string;
  updatedAt?: string;
}

export interface ExceptionSettingPayload {
  systemType: string;
  eventType: string;
  autoProcess: AutoProcess;
  riskLevel?: RiskLevel;
  autoClose?: AutoClose;
}


export const fetchExceptionSettings = async (systemType?: string): Promise<ExceptionSetting[]> => {
  const { data } = await apiClient.get<ExceptionSetting[]>('/api/exception-settings', {
    params: systemType ? { systemType } : undefined
  });
  return data || [];
};


export const saveExceptionSetting = async (payload: ExceptionSettingPayload): Promise<ExceptionSetting> => {
  const { data } = await apiClient.post<ExceptionSetting>('/api/exception-settings', payload);
  return data;
};


export const deleteExceptionSetting = async (systemType: string, eventType: string): Promise<void> => {
  await apiClient.delete(`/api/exception-settings/${encodeURIComponent(systemType)}/${encodeURIComponent(eventType)}`);
};