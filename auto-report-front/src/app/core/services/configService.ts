import apiClient from './apiClient';

export interface ConfigItem {
  code: string;
  label: string;
  sort?: number;
}

export type ConfigDictionaries = Record<string, ConfigItem[]>;

export const fetchConfigDictionaries = async (): Promise<ConfigDictionaries> => {
  const { data } = await apiClient.get<ConfigDictionaries>('/api/config-dictionaries');
  return data || {};
};

export const toOptions = (items: ConfigItem[] = []) =>
  items.map((item) => ({ value: item.code, label: item.label }));

export const toLabels = (items: ConfigItem[] = []) =>
  items.reduce<Record<string, string>>((acc, item) => {
    acc[item.code] = item.label;
    return acc;
  }, {});