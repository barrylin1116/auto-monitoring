import React, { useState } from 'react';
import { Input, InputProps } from 'antd';
import { EyeInvisibleOutlined, EyeOutlined } from '@ant-design/icons';


const InputAccount: React.FC<InputProps> = ({ value, onChange, ...rest }) => {
  const [visible, setVisible] = useState(false);

  return (
    <Input
      {...rest}
      value={value}
      onChange={onChange}
      type="text"
      data-mask={visible ? 'false' : 'true'}
      suffix={
        visible
          ? (
            <EyeOutlined onClick={() => setVisible(false)} />
            )
          : (
            <EyeInvisibleOutlined onClick={() => setVisible(true)} />
            )
      }
    />
  );
};

export default InputAccount;