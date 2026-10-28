import apiClient from './apiClient';

export type LoginErrorType = 'captcha' | 'credentials' | 'other';

export interface LoginResult {
  success: boolean;
  message: string;
  token?: string;
  errorType?: LoginErrorType;
}

interface LoginApiResponse {
  token: string;
  username: string;
  expiresIn: number;
}

interface CaptchaApiResponse {
  captchaId: string;
  captchaBase64: string;
}

export interface CaptchaResult {
  captchaId: string;
  image: string;
}


export const fetchCaptcha = async (): Promise<CaptchaResult> => {
  const { data } = await apiClient.get<CaptchaApiResponse>('/api/auth/captcha');
  const base64 = data?.captchaBase64 || '';
  return {
    captchaId: data?.captchaId || '',
    image: base64 ? `data:image/png;base64,${base64}` : ''
  };
};


export const login = async (
  memberId: string,
  passPhrase: string,
  captchaId: string,
  captchaCode: string
): Promise<LoginResult> => {
  try {
    const { data } = await apiClient.post<LoginApiResponse>('/api/auth/login', {
      username: memberId,
      password: passPhrase,
      captchaId,
      captchaCode
    });
    return { success: true, message: '登入成功', token: data.token };
  } catch (error: any) {
    const status = error?.response?.status;
    if (status === 400) {
      return { success: false, message: '驗證碼錯誤', errorType: 'captcha' };
    }
    if (status === 401) {
      return { success: false, message: '帳號或密碼錯誤', errorType: 'credentials' };
    }
    if (status === 403) {
      return {
        success: false,
        message: error?.response?.data?.message || '帳號已鎖定',
        errorType: 'other'
      };
    }
    return { success: false, message: '登入失敗，請稍後再試', errorType: 'other' };
  }
};
