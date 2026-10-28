export interface AuthPayload {
  username: string;
  token: string;
}

export const LOGIN_SUCCESS = 'auth/LOGIN_SUCCESS';
export const CLEAR_AUTH = 'auth/CLEAR_AUTH';

export interface LoginSuccessAction {
  type: typeof LOGIN_SUCCESS;
  payload: AuthPayload;
}

export interface ClearAuthAction {
  type: typeof CLEAR_AUTH;
}

export type AuthAction = LoginSuccessAction | ClearAuthAction;

export const loginSuccess = (username: string, token: string): LoginSuccessAction => ({
  type: LOGIN_SUCCESS,
  payload: { username, token }
});

export const clearAuth = (): ClearAuthAction => ({
  type: CLEAR_AUTH
});
