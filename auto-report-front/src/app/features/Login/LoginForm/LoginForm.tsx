import React from 'react';
import { Form, Input, Button } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import CaptchaField from './CaptchaField';
import InputAccount from './InputAccount';
import { LoginFormValues } from '../types';

interface LoginFormProps {
  form: any;
  errors: Partial<Record<keyof LoginFormValues, string>>;
  captchaImg: string;
  captchaLoading: boolean | undefined;
  onRefreshCaptcha: () => void;
  onFinish?: (values: any) => void;
  onValuesChange?: (changedValues: any, allValues: any) => void;
}


const LoginForm: React.FC<LoginFormProps> = ({
  form,
  errors,
  captchaImg,
  captchaLoading,
  onRefreshCaptcha,
  onFinish,
  onValuesChange
}) => {
  return (
    <Form
      form={form}
      layout="vertical"
      requiredMark={false}
      onFinish={onFinish}
      onValuesChange={onValuesChange}
      initialValues={{
        memberId: '',
        passPhrase: '',
        captchaCode: ''
      }}
    >
      <Form.Item
        label="帳號"
        name="memberId"
        help={errors.memberId}
        validateStatus={errors.memberId ? 'error' : undefined}
      >
        <InputAccount
          name="memberId"
          placeholder="請輸入帳號"
          prefix={<UserOutlined style={{ color: '#ccc' }} />}
        />
      </Form.Item>

      <Form.Item
        label="密碼"
        name="passPhrase"
        help={errors.passPhrase}
        validateStatus={errors.passPhrase ? 'error' : undefined}
      >
        <Input.Password
          name="passPhrase"
          placeholder="輸入英文字母須區分大小寫"
          prefix={<LockOutlined style={{ color: '#ccc' }} />}
        />
      </Form.Item>

      <Form.Item
        label="圖形驗證碼"
        name="captchaCode"
        help={errors.captchaCode}
        validateStatus={errors.captchaCode ? 'error' : undefined}
      >
        <CaptchaField
          name="captchaCode"
          captchaImg={captchaImg}
          loading={captchaLoading}
          onRefresh={onRefreshCaptcha}
          autoFocus
        />
      </Form.Item>

      <Button type="primary" block size="large" htmlType="submit" style={{ marginTop: 12 }}>
        登入
      </Button>
    </Form>
  );
};

export default LoginForm;