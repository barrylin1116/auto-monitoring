import React from 'react';
import { Card, Empty, Typography } from 'antd';

const { Title, Paragraph } = Typography;

interface PagePlaceholderProps {
  title: string;
  description?: string;
}

const PagePlaceholder: React.FC<PagePlaceholderProps> = ({ title, description }) => (
  <Card bordered={false}>
    <Title level={4}>{title}</Title>
    <Paragraph type="secondary">{description ?? '此功能尚在建置中'}</Paragraph>
    <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} />
  </Card>
);

export default PagePlaceholder;