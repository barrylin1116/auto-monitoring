import apiClient from './apiClient';
import { getStoredToken } from 'app/core/auth/tokenStorage';
import { Path } from 'app/core/router/paths';
import { notifyError } from 'app/common/utils/notify';
import store, { history } from 'app/store';
import { clearAuth } from 'app/store/auth/actions';

const LOGIN_URL = '/api/auth/login';

apiClient.interceptors.request.use((config) => {
  const token = getStoredToken();
  if (token) {
    config.headers = config.headers ?? {};
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error?.response?.status;
    const url: string = error?.config?.url || '';
    if (status === 401 && !url.includes(LOGIN_URL)) {
      store.dispatch(clearAuth());
      notifyError('登入逾時，請重新登入');
      history.push(Path.LOGIN);
    }
    return Promise.reject(error);
  }
);
