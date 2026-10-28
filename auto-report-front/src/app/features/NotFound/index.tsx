import React from 'react';
import { Result, Button } from 'antd';
import { Link } from 'react-router-dom';
import { Path } from 'app/core/router/paths';

const NotFoundPage: React.FC = () => (
  <Result
    status="404"
    title="404"
    subTitle="抱歉，此頁面不存在。"
    extra={
      <Link to={Path.ROOT}>
        <Button type="primary">回首頁</Button>
      </Link>
    }
  />
);

export default NotFoundPage;