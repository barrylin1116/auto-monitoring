import apiClient from './apiClient';

export interface HolidaySetting {
  id?: number;
  holidayDate: string;
  remark?: string | null;
  createdAt?: string;
  updatedAt?: string;
}

export interface HolidaySettingPayload {
  holidayDate: string;
  remark?: string | null;
}


export interface PageResult<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}


export const fetchHolidaySettings = async (
  page = 0,
  size = 10
): Promise<PageResult<HolidaySetting>> => {
  const { data } = await apiClient.get<PageResult<HolidaySetting>>('/api/holiday-settings', {
    params: { page, size }
  });
  return data || { content: [], totalElements: 0, totalPages: 0, number: page, size };
};


export const saveHolidaySetting = async (payload: HolidaySettingPayload): Promise<HolidaySetting> => {
  const { data } = await apiClient.post<HolidaySetting>('/api/holiday-settings', payload);
  return data;
};


export const deleteHolidaySetting = async (holidayDate: string): Promise<void> => {
  await apiClient.delete('/api/holiday-settings', { params: { date: holidayDate } });
};