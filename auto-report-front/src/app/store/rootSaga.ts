import { all } from 'redux-saga/effects';
import configSaga from './config/saga';

export default function* rootSaga() {
  yield all([configSaga()]);
}