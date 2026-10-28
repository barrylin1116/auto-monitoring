import { ConfigDictionaries } from './types';

export const FETCH_CONFIG_REQUEST = 'config/FETCH_CONFIG_REQUEST';
export const FETCH_CONFIG_SUCCESS = 'config/FETCH_CONFIG_SUCCESS';
export const FETCH_CONFIG_FAILURE = 'config/FETCH_CONFIG_FAILURE';

export interface FetchConfigRequestAction {
  type: typeof FETCH_CONFIG_REQUEST;
}

export interface FetchConfigSuccessAction {
  type: typeof FETCH_CONFIG_SUCCESS;
  payload: ConfigDictionaries;
}

export interface FetchConfigFailureAction {
  type: typeof FETCH_CONFIG_FAILURE;
  payload: string;
}

export type ConfigAction =
  | FetchConfigRequestAction
  | FetchConfigSuccessAction
  | FetchConfigFailureAction;

export const fetchConfigRequest = (): FetchConfigRequestAction => ({
  type: FETCH_CONFIG_REQUEST
});

export const fetchConfigSuccess = (payload: ConfigDictionaries): FetchConfigSuccessAction => ({
  type: FETCH_CONFIG_SUCCESS,
  payload
});

export const fetchConfigFailure = (payload: string): FetchConfigFailureAction => ({
  type: FETCH_CONFIG_FAILURE,
  payload
});