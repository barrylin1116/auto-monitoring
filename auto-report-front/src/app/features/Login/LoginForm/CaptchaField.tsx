import React, { useEffect, useRef } from 'react';
import { Input, Spin, Typography, InputRef } from 'antd';
import { ReloadOutlined } from '@ant-design/icons';

interface CaptchaFieldProps {
  name?: string;
  value?: string;
  onChange?: (e: any) => void;
  onBlur?: () => void;
  captchaImg: string;
  onRefresh?: () => void;
  loading?: boolean;
  autoFocus?: boolean;
  error?: string;
}


const CaptchaField: React.FC<CaptchaFieldProps> = ({
  name = 'captcha',
  value,
  onChange,
  onBlur,
  captchaImg,
  onRefresh,
  loading = false,
  autoFocus = false,
  error
}) => {
  const inputRef = useRef<InputRef>(null);

  useEffect(() => {
    if (autoFocus && inputRef.current) {
      inputRef.current.focus();
    }
  }, [autoFocus]);

  return (
    <div className="captcha-field-wrapper">
      <div style={{ display: 'flex', flexWrap: 'wrap', gap: 12, alignItems: 'center' }}>
        <Input
          ref={inputRef}
          name={name}
          value={value}
          onChange={onChange}
          onBlur={onBlur}
          placeholder="請輸入驗證碼"
          status={error ? 'error' : undefined}
          style={{ flex: '1 1 120px' }}
        />

        <div style={{ display: 'flex', flexDirection: 'row', gap: 8, alignItems: 'center' }}>
          <div style={{ position: 'relative' }}>
            {loading && (
              <div
                style={{
                  position: 'absolute',
                  top: 0,
                  left: 0,
                  width: '100%',
                  height: '100%',
                  display: 'flex',
                  justifyContent: 'center',
                  alignItems: 'center',
                  background: 'rgba(255,255,255,0.6)',
                  borderRadius: 4,
                  zIndex: 2
                }}
              >
                <Spin size="small" />
              </div>
            )}
            <img
              src={captchaImg}
              alt="captcha"
              style={{
                width: 120,
                height: 40,
                borderRadius: 4,
                cursor: onRefresh ? 'pointer' : 'default',
                objectFit: 'contain',
                opacity: loading ? 0.5 : 1
              }}
              onClick={onRefresh}
            />
          </div>

          <ReloadOutlined
            onClick={onRefresh}
            style={{ fontSize: 20, cursor: 'pointer', color: '#1890ff' }}
          />
        </div>
      </div>

      {error && (
        <Typography.Text
          type="danger"
          style={{ fontSize: 12, display: 'block', marginTop: 4 }}
        >
          {error}
        </Typography.Text>
      )}
    </div>
  );
};

export default CaptchaField;