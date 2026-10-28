
import { useCallback, useEffect, useState } from 'react';
import { fetchCaptcha as fetchCaptchaImage } from 'app/core/services/loginService';

export const useLoginCaptcha = () => {
  const [captchaImg, setCaptchaImg] = useState('');
  const [captchaId, setCaptchaId] = useState('');
  const [captchaLoading, setCaptchaLoading] = useState(false);

  const fetchCaptcha = useCallback(() => {
    setCaptchaLoading(true);
    fetchCaptchaImage()
      .then((result) => {
        setCaptchaId(result.captchaId);
        setCaptchaImg(result.image);
      })
      .catch(() => {
        setCaptchaId('');
        setCaptchaImg('');
      })
      .finally(() => setCaptchaLoading(false));
  }, []);

  useEffect(() => {
    fetchCaptcha();
  }, [fetchCaptcha]);

  return {
    captchaImg,
    captchaId,
    captchaLoading,
    fetchCaptcha
  };
};
