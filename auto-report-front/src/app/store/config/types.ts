import { ConfigDictionaries, ConfigItem } from 'app/core/services/configService';

export interface ConfigState {
  bizName: ConfigItem[];
  systemType: ConfigItem[];
  processStatus: ConfigItem[];
  matchedRule: ConfigItem[];
  riskLevel: ConfigItem[];
  eventStatus: ConfigItem[];
  loaded: boolean;
  loading: boolean;
  error: string | null;
}

export type { ConfigDictionaries, ConfigItem };