import { combineReducers } from 'redux';
import { connectRouter } from 'connected-react-router';
import { History } from 'history';
import configReducer from './config/reducer';
import authReducer from './auth/reducer';

export const createRootReducer = (history: History) =>
  combineReducers({
    router: connectRouter(history),
    config: configReducer,
    auth: authReducer
  });

export type RootState = ReturnType<ReturnType<typeof createRootReducer>>;