import React from 'react';
import { Modal } from 'antd';
import {
  CheckCircleFilled,
  CloseCircleFilled,
  ExclamationCircleFilled,
  InfoCircleFilled
} from '@ant-design/icons';

const GREEN = '#348d8f';
const NOTIFY_TITLE = '系統提示';


const renderContent = (text: string, detail?: string) =>
  detail ? (
    <div>
      <div>{text}</div>
      <div>{detail}</div>
    </div>
  ) : (
    text
  );




export const notifySuccess = (text: string, detail?: string) => {
  Modal.success({
    title: NOTIFY_TITLE,
    content: renderContent(text, detail),
    icon: <CheckCircleFilled style={{ color: GREEN }} />,
    okText: '確定',
    okButtonProps: { style: { backgroundColor: GREEN, borderColor: GREEN } },
    centered: true, width: 520
  });
};


export const notifyError = (text: string, detail?: string) => {
  Modal.error({
    title: NOTIFY_TITLE,
    content: renderContent(text, detail),
    icon: <CloseCircleFilled style={{ color: GREEN }} />,
    okText: '確定',
    okButtonProps: { style: { backgroundColor: GREEN, borderColor: GREEN } },
    centered: true, width: 520
  });
};


export const notifyInfo = (text: string, detail?: string) => {
  Modal.info({
    title: NOTIFY_TITLE,
    content: renderContent(text, detail),
    icon: <InfoCircleFilled style={{ color: GREEN }} />,
    okText: '確定',
    okButtonProps: { style: { backgroundColor: GREEN, borderColor: GREEN } },
    centered: true, width: 520
  });
};


export const notifyConfirm = (text: string, onOk: () => void | Promise<void>) => {
  Modal.confirm({
    title: NOTIFY_TITLE,
    content: text,
    icon: <ExclamationCircleFilled style={{ color: '#faad14' }} />,
    okText: '確定',
    cancelText: '取消',
    okButtonProps: { danger: true },
    centered: true, width: 520,
    onOk
  });
};