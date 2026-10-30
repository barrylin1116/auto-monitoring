import { ConfigProvider, Form } from 'antd';
import { notifyError } from 'app/common/utils/notify';
import React, { useState } from 'react';
import { useDispatch } from 'react-redux';
import { useHistory } from 'react-router-dom';
import { loginSuccess } from 'app/store/auth/actions';
import { fetchConfigRequest } from 'app/store/config/actions';
import LoginForm from './LoginForm';
import { useLoginCaptcha } from './useLoginCaptcha';
import { Path } from 'app/core/router/paths';
import { login } from 'app/core/services/loginService';

import homeImg from 'assets/img/pic/Home.jpg';
import userIcon from 'assets/img/pic/icon_friends-green.png';
import loginLogo from 'assets/img/icons/my-logo.png';

import * as Yup from 'yup';
import { LoginFormValues } from './types';
import { useYupValidator } from 'app/core/hooks/useYupValidator';
import { debounce } from 'lodash';

const schema = Yup.object({
  memberId: Yup.string().required('請輸入帳號'),
  passPhrase: Yup.string().required('請輸入密碼'),
  captchaCode: Yup.string().required('請輸入驗證碼')
});

const fieldValidateStrategy = {
  memberId: { onChange: true, onBlur: true },
  passPhrase: { onChange: true, onBlur: true },
  captchaCode: { onChange: true, onBlur: false }
};

const globalTheme = {
  token: {
    colorPrimary: '#348d8f',
    controlHeight: 50,
    borderRadius: 8,
    colorBgTextHover: '#cde3e3',
    fontFamily: '"Helvetica Neue", Helvetica, Arial, sans-serif',
    fontSize: 19.2
  },
  components: {
    Button: {
      colorPrimary: '#348d8f',
      defaultColor: '#348d8f',
      defaultBorderColor: '#348d8f',
      defaultBg: '#ffffff',
      defaultHoverColor: '#348d8f',
      defaultHoverBorderColor: '#348d8f',
      defaultHoverBg: '#cde3e3',
      defaultActiveColor: '#348d8f',
      defaultActiveBorderColor: '#348d8f',
      defaultActiveBg: '#2c787a',
      controlHeight: 50,
      borderRadius: 8
    },
    Input: {
      controlHeight: 50
    },
    Select: {
      controlHeight: 50
    }
  }
};


const LoginMain: React.FC = () => {
  const [form] = Form.useForm<LoginFormValues>();
  const { validateField, validateForm } = useYupValidator(schema);
  const [errors, setErrors] = useState<Partial<Record<keyof LoginFormValues, string>>>({});
  const history = useHistory();
  const dispatch = useDispatch();

  const { captchaImg, captchaId, captchaLoading, fetchCaptcha } = useLoginCaptcha();

  const handleFinish = async (values: LoginFormValues) => {
    const { isValid, errors: formErrors } = await validateForm(values);

    if (!isValid) {
      setErrors(formErrors);
      return;
    }

    const { memberId, passPhrase, captchaCode } = values;
    const result = await login(memberId, passPhrase, captchaId, captchaCode);

    if (result.success) {
      dispatch(loginSuccess(memberId, result.token || ''));
      dispatch(fetchConfigRequest());
      history.push(Path.EXCEPTION_EVENT);
      return;
    }

    
    fetchCaptcha();
    if (result.errorType === 'captcha') {
      setErrors((prev) => ({ ...prev, captchaCode: '驗證碼錯誤' }));
    } else {
      notifyError(result.message);
    }
  };

  const validateFieldDebouncedFn = async (
    field: keyof LoginFormValues,
    value: any
  ) => {
    const error = await validateField(field, value);
    setErrors((prev) => ({ ...prev, [field]: error }));
  };

  const debouncedValidateField = debounce(validateFieldDebouncedFn, 500);

  const handleValuesChange = async (changed: Partial<LoginFormValues>, all: LoginFormValues) => {
    const [field, value] = Object.entries(changed)[0] as [
      keyof LoginFormValues,
      LoginFormValues[keyof LoginFormValues]
    ];

    if (fieldValidateStrategy[field]?.onChange) {
      debouncedValidateField(field, value);
    }
  };

  return (
    <ConfigProvider theme={globalTheme}>
      <div className="login-page">
        {}
        <header className="login-page-header">
          <div className="login-page-header-inner">
            <div className="login-page-header-brand">
              <img src={loginLogo} alt="logo-台銀人壽" className="login-page-logo" />
              <div className="login-page-header-title">自監控系統</div>
            </div>
          </div>
        </header>

        {}
        <div className="login-page-content">
          <div className="login-page-container">
            {}
            <div className="login-page-hero login-hero-lg">
              <img src={homeImg} alt="home" className="login-page-hero-img" />
            </div>

            {}
            <div className="login-page-form-col">
              <div className="login-form-wrapper nc-bg-tertiary">
                <div className="login-page-form-title-block">
                  <h4 className="login-page-form-title nc-text-green-300">使用者登入</h4>
                  <img src={userIcon} alt="user-icon" className="login-page-form-icon" />
                </div>
                <div className="login-page-form-scroll">
                  <LoginForm
                    form={form}
                    errors={errors}
                    captchaImg={captchaImg}
                    captchaLoading={captchaLoading}
                    onRefreshCaptcha={fetchCaptcha}
                    onFinish={handleFinish}
                    onValuesChange={handleValuesChange}
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </ConfigProvider>
  );
};

export default LoginMain;