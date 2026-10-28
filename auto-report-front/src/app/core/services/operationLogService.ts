import apiClient from './apiClient';
import type { PageResult } from './holidaySettingService';


export interface OperationLog {
  id?: string | null;
  systemType?: string | null;
  account?: string | null;
  operationTime?: string | null;
  functionScreen?: string | null;
  action?: string | null;
  actionResult?: string | null;
  actionContent?: string | null;
}


export interface OperationLogQuery {
  systemType?: string;
  account?: string;
  action?: string;
  actionResult?: string;
  ids?: string[];
}

export const fetchOperationLogs = async (
  query: OperationLogQuery = {},
  page = 0,
  size = 10
): Promise<PageResult<OperationLog>> => {
  const { ids, ...rest } = query;
  const { data } = await apiClient.get<PageResult<OperationLog>>('/api/operation-logs', {
    params: { page, size, ...rest, ...(ids && ids.length ? { ids: ids.join(',') } : {}) }
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