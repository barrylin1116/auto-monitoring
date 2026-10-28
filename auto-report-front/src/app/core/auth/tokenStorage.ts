const USERNAME_KEY = 'auth.username';
const TOKEN_KEY = 'auth.token';

export const getStoredUsername = (): string => {
  try {
    return window.localStorage.getItem(USERNAME_KEY) || '';
  } catch {
    return '';
  }
};

export const getStoredToken = (): string => {
  try {
    return window.localStorage.getItem(TOKEN_KEY) || '';
  } catch {
    return '';
  }
};

export const saveAuth = (username: string, token: string) => {
  try {
    window.localStorage.setItem(USERNAME_KEY, username);
    window.localStorage.setItem(TOKEN_KEY, token);
  } catch {
    
  }
};

export const clearStoredAuth = () => {
  try {
    window.localStorage.removeItem(USERNAME_KEY);
    window.localStorage.removeItem(TOKEN_KEY);
  } catch {
    
  }
};
