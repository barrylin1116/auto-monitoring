import { call, put, takeLatest } from 'redux-saga/effects';
import { fetchConfigDictionaries } from 'app/core/services/configService';
import { FETCH_CONFIG_REQUEST, fetchConfigFailure, fetchConfigSuccess } from './actions';
import { ConfigDictionaries } from './types';

function* fetchConfigSaga() {
  try {
    const data: ConfigDictionaries = yield call(fetchConfigDictionaries);
    yield put(fetchConfigSuccess(data));
  } catch (e) {
    yield put(fetchConfigFailure(e instanceof Error ? e.message : '載入配置字典失敗'));
  }
}

export default function* configSaga() {
  yield takeLatest(FETCH_CONFIG_REQUEST, fetchConfigSaga);
}