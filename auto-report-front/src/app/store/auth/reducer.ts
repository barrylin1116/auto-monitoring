import {
  clearStoredAuth,
  getStoredToken,
  getStoredUsername,
  saveAuth
} from 'app/core/auth/tokenStorage';
import { AuthAction, CLEAR_AUTH, LOGIN_SUCCESS } from './actions';
import { AuthState } from './types';

const initialState: AuthState = {
  username: getStoredUsername(),
  token: getStoredToken()
};

const authReducer = (state = initialState, action: AuthAction): AuthState => {
  switch (action.type) {
    case LOGIN_SUCCESS:
      saveAuth(action.payload.username, action.payload.token);
      return { ...state, username: action.payload.username, token: action.payload.token };
    case CLEAR_AUTH:
      clearStoredAuth();
      return { ...state, username: '', token: '' };
    default:
      return state;
  }
};

export default authReducer;
