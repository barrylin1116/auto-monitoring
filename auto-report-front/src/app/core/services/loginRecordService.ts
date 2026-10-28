import apiClient from './apiClient';
import type { PageResult } from './holidaySettingService';


export interface LoginRecord {
  account?: string | null;
  name?: string | null;
  channelBranch?: string | null;
  role?: string | null;
  loginTime?: string | null;
  logoutTime?: string | null;
  loginResult?: string | null;
  failCount?: number | null;
  sourceIp?: string | null;
  sourceArea?: string | null;
  deviceInfo?: string | null;
  loginMethod?: string | null;
}

function normalizeLoginResult(value?: string | null): string | undefined {
  if (!value) return value ?? undefined;
  const v = value.trim().toUpperCase();
  if (v === 'SUCCESS') return '成功';
  if (v === 'FAILED') return '失敗';
  return value;
}


export interface LoginRecordQuery {
  system?: string;
  account?: string;
  name?: string;
  loginResult?: string;
  loginTimeStart?: string;
  loginTimeEnd?: string;
}


export const fetchLoginSystems = async (): Promise<string[]> => {
  const { data } = await apiClient.get<string[]>('/api/login-records/systems');
  return data || [];
};


export const fetchLoginRecords = async (
  query: LoginRecordQuery = {},
  page = 0,
  size = 10
): Promise<PageResult<LoginRecord>> => {
  const { data } = await apiClient.get<PageResult<LoginRecord>>('/api/login-records', {
    params: { page, size, ...query }
  });
  if (data?.content) {
    data.content.forEach(r => { r.loginResult = normalizeLoginResult(r.loginResult); });
  }
  return data || { content: [], totalElements: 0, totalPages: 0, number: page, size };
};
