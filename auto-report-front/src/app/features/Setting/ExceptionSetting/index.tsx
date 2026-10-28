import React, { useEffect, useMemo, useState } from 'react';
import { Button, Card, ConfigProvider, Form, Modal, Radio, Select, Table } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { notifyConfirm, notifyError, notifySuccess } from 'app/common/utils/notify';
import { toLabels, toOptions } from 'app/core/services/configService';
import {
  AutoClose,
  AutoProcess,
  ExceptionSetting,
  ExceptionSettingPayload,
  RiskLevel,
  deleteExceptionSetting,
  fetchExceptionSettings,
  saveExceptionSetting
} from 'app/core/services/exceptionSettingService';
import { useConfig } from 'app/store/config/hooks';

export const AUTO_PROCESS_OPTIONS = [
  { value: 'NONE', label: '無' },
  { value: 'LOCK', label: '鎖定' },
  { value: 'DISABLE', label: '停用' }
];

const AUTO_PROCESS_LABELS: Record<string, string> = {
  NONE: '無',
  LOCK: '鎖定',
  DISABLE: '停用'
};

export const RISK_LEVEL_OPTIONS = [
  { value: 'LOW', label: '低' },
  { value: 'MEDIUM', label: '中' },
  { value: 'HIGH', label: '高' }
];

const RISK_LEVEL_LABELS: Record<string, string> = {
  LOW: '低',
  MEDIUM: '中',
  HIGH: '高'
};

export const AUTO_CLOSE_OPTIONS = [
  { value: 'Y', label: '是' },
  { value: 'N', label: '否' }
];

const AUTO_CLOSE_LABELS: Record<string, string> = {
  Y: '是',
  N: '否'
};


const TABLE_CHROME_HEIGHT = 520;

const TABLE_MIN_HEIGHT = 280;

interface ExceptionSettingValues {
  systemType?: string;
  eventType?: string;
  autoProcess: AutoProcess;
  riskLevel?: RiskLevel;
  autoClose?: AutoClose;
}

interface ExceptionSettingQuery {
  systemType?: string;
  autoProcess?: AutoProcess;
  eventType?: string;
  riskLevel?: RiskLevel;
}

const ExceptionSettingPage: React.FC = () => {
  const [form] = Form.useForm<ExceptionSettingValues>();
  const [queryForm] = Form.useForm<ExceptionSettingQuery>();
  const [list, setList] = useState<ExceptionSetting[]>([]);
  const [query, setQuery] = useState<ExceptionSettingQuery>({});
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingSystemType, setEditingSystemType] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);
  const [tableHeight, setTableHeight] = useState<number>(() =>
    Math.max(TABLE_MIN_HEIGHT, window.innerHeight - TABLE_CHROME_HEIGHT)
  );

  const { systemType, matchedRule } = useConfig();
  const systemTypeOptions = useMemo(() => toOptions(systemType), [systemType]);
  const systemTypeLabels = useMemo(() => toLabels(systemType), [systemType]);
  const eventTypeOptions = useMemo(() => toOptions(matchedRule), [matchedRule]);
  const eventTypeLabels = useMemo(() => toLabels(matchedRule), [matchedRule]);

  
  const filteredList = useMemo(
    () =>
      list.filter((item) => {
        if (query.systemType && item.systemType !== query.systemType) {
          return false;
        }
        if (query.autoProcess && item.autoProcess !== query.autoProcess) {
          return false;
        }
        if (query.riskLevel && item.riskLevel !== query.riskLevel) {
          return false;
        }
        if (query.eventType && item.eventType !== query.eventType) {
          return false;
        }
        return true;
      }),
    [list, query]
  );

  
  useEffect(() => {
    const onResize = () =>
      setTableHeight(Math.max(TABLE_MIN_HEIGHT, window.innerHeight - TABLE_CHROME_HEIGHT));
    window.addEventListener('resize', onResize);
    return () => window.removeEventListener('resize', onResize);
  }, []);

  
  const loadList = async () => {
    setLoading(true);
    try {
      const settings = await fetchExceptionSettings();
      setList(settings);
    } catch {
      notifyError('讀取異常設定清單失敗', '請確認後端服務是否啟動');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadList();
    
  }, []);

  
  const handleOpenCreate = () => {
    setEditingSystemType(null);
    form.resetFields();
    form.setFieldsValue({ autoProcess: 'NONE', riskLevel: 'MEDIUM', autoClose: 'N' });
    setModalOpen(true);
  };

  
  const handleOpenEdit = (record: ExceptionSetting) => {
    setEditingSystemType(record.systemType);
    form.setFieldsValue({
      systemType: record.systemType,
      eventType: record.eventType,
      autoProcess: record.autoProcess ?? 'NONE',
      riskLevel: record.riskLevel ?? 'MEDIUM',
      autoClose: record.autoClose ?? 'N'
    });
    setModalOpen(true);
  };

  
  const handleDelete = async (systemType: string, eventType: string) => {
    try {
      await deleteExceptionSetting(systemType, eventType);
      notifySuccess('異常設定已刪除');
      await loadList();
    } catch {
      notifyError('刪除失敗', '請確認後端服務是否啟動');
    }
  };

  const handleCloseModal = () => {
    setModalOpen(false);
  };

  const handleSearch = (values: ExceptionSettingQuery) => {
    setQuery(values);
  };

  const handleClearQuery = () => {
    queryForm.resetFields();
    setQuery({});
  };

  
  const handleValuesChange = async (changed: Partial<ExceptionSettingValues>) => {
    if (!('systemType' in changed)) {
      return;
    }
    const systemType = changed.systemType;
    if (!systemType) {
      form.setFieldsValue({ eventType: undefined, autoProcess: 'NONE', riskLevel: 'MEDIUM', autoClose: 'N' });
    }
  };

  const handleSave = async (values: ExceptionSettingValues) => {
    if (!values.systemType || !values.eventType) {
      return;
    }
    const payload: ExceptionSettingPayload = {
      systemType: values.systemType,
      eventType: values.eventType,
      autoProcess: values.autoProcess,
      riskLevel: values.riskLevel ?? 'MEDIUM',
      autoClose: values.autoClose ?? 'N'
    };
    setSaving(true);
    try {
      await saveExceptionSetting(payload);
      notifySuccess('異常設定已儲存');
      setModalOpen(false);
      await loadList();
    } catch {
      notifyError('儲存失敗', '請確認後端服務是否啟動');
    } finally {
      setSaving(false);
    }
  };

  const columns: ColumnsType<ExceptionSetting> = [
    {
      title: '系統別',
      dataIndex: 'systemType',
      key: 'systemType',
      width: 180,
      render: (v) => (v ? systemTypeLabels[v] || v : '-')
    },
    {
      title: '事件類型',
      dataIndex: 'eventType',
      key: 'eventType',
      width: 220,
      render: (eventType?: string) => (eventType ? eventTypeLabels[eventType] || eventType : '-')
    },
    {
      title: '自動處理',
      dataIndex: 'autoProcess',
      key: 'autoProcess',
      width: 120,
      align: 'center',
      render: (v) => (v ? AUTO_PROCESS_LABELS[v] || v : '-')
    },
    {
      title: '風險等級',
      dataIndex: 'riskLevel',
      key: 'riskLevel',
      width: 120,
      align: 'center',
      render: (v) => (v ? RISK_LEVEL_LABELS[v] || v : '-')
    },
    {
      title: '自動結案',
      dataIndex: 'autoClose',
      key: 'autoClose',
      width: 120,
      align: 'center',
      render: (v) => (v ? AUTO_CLOSE_LABELS[v] || v : '-')
    },
    {
      title: '更新時間',
      dataIndex: 'updatedAt',
      key: 'updatedAt',
      width: 200,
      render: (v) => v || '-'
    },
    {
      title: '操作',
      key: 'action',
      width: 160,
      align: 'center',
render: (_, record) => (
        <div style={{ display: 'flex', gap: 8, justifyContent: 'center' }}>
          <Button type="primary" onClick={() => handleOpenEdit(record)}>
            修改
          </Button>
          <Button
            type="primary"
            danger
            onClick={() =>
              notifyConfirm('確定刪除此異常設定？', () => handleDelete(record.systemType, record.eventType))
            }
          >
            刪除
          </Button>
        </div>
      )
    }
  ];

  return (
    <ConfigProvider componentSize="large" theme={{ token: { colorPrimary: '#348d8f' } }}>
      <Card bordered={false} className="setting-query-card">
        <Form
          form={queryForm}
          className="loginlog-query-form"
          onFinish={handleSearch}
        >
          <div className="query-form-row">
            <Form.Item label="系統別" name="systemType">
              <Select allowClear placeholder="全部" options={systemTypeOptions} />
            </Form.Item>
            <Form.Item label="自動處理" name="autoProcess">
              <Select allowClear placeholder="全部" options={AUTO_PROCESS_OPTIONS} />
            </Form.Item>
          </div>
          <div className="query-form-row">
            <Form.Item label="風險等級" name="riskLevel">
              <Select allowClear placeholder="全部" options={RISK_LEVEL_OPTIONS} />
            </Form.Item>
            <Form.Item label="事件類型" name="eventType">
              <Select allowClear placeholder="全部" options={eventTypeOptions} />
            </Form.Item>
          </div>
          <div className="query-form-actions">
            <Button type="primary" htmlType="submit">
              查詢
            </Button>
            <Button htmlType="button" onClick={handleClearQuery}>
              清空
            </Button>
          </div>
        </Form>
      </Card>

      <Card
        bordered={false}
        className="setting-result-card"
        title="查詢結果"
        extra={
          <Button type="primary" onClick={handleOpenCreate}>
            新增
          </Button>
        }
      >
        <Table
          className="setting-holiday-table"
          rowKey={(record) => `${record.systemType}_${record.eventType}`}
          loading={loading}
          dataSource={filteredList}
          columns={columns}
          scroll={{ y: tableHeight }}
          pagination={false}
        />
      </Card>

      <Modal
        title={editingSystemType ? '修改異常設定' : '新增異常設定'}
        open={modalOpen}
        onCancel={handleCloseModal}
        forceRender
        width={880}
        footer={
          <div style={{ textAlign: 'right' }}>
            <Button onClick={handleCloseModal}>取消</Button>
            <Button
              type="primary"
              style={{ marginLeft: 8 }}
              loading={saving}
              onClick={() => form.submit()}
            >
              儲存
            </Button>
          </div>
        }
      >
        <Form
          form={form}
          layout="horizontal"
          className="setting-page-form setting-page-form--two-col"
          onFinish={handleSave}
          onValuesChange={handleValuesChange}
          initialValues={{ autoProcess: 'NONE', riskLevel: 'MEDIUM', autoClose: 'N' }}
        >
          <div className="setting-form-grid">
            <Form.Item
              label="系統別"
              name="systemType"
              rules={[{ required: true, message: '請選擇系統別' }]}
              preserve={true}
            >
              <Select
                allowClear
                placeholder="請選擇系統別"
                options={systemTypeOptions}
                disabled={!!editingSystemType}
              />
            </Form.Item>

            <Form.Item
              label="預設風險等級"
              name="riskLevel"
              rules={[{ required: true, message: '請選擇預設風險等級' }]}
            >
              <Select placeholder="請選擇預設風險等級" options={RISK_LEVEL_OPTIONS} />
            </Form.Item>

            <Form.Item
              label="事件類型"
              name="eventType"
              className="setting-form-grid-span"
              rules={[{ required: true, message: '請選擇事件類型' }]}
              preserve={true}
            >
              <Select
                allowClear
                placeholder="請選擇事件類型"
                options={eventTypeOptions}
                className="event-type-select"
                disabled={!!editingSystemType}
              />
            </Form.Item>

            <Form.Item
              label="自動處理"
              name="autoProcess"
              rules={[{ required: true, message: '請選擇自動處理' }]}
            >
              <Radio.Group options={AUTO_PROCESS_OPTIONS} />
            </Form.Item>

            <Form.Item
              label="是否自動結案"
              name="autoClose"
              rules={[{ required: true, message: '請選擇是否自動結案' }]}
            >
              <Radio.Group options={AUTO_CLOSE_OPTIONS} />
            </Form.Item>
          </div>
        </Form>
      </Modal>
    </ConfigProvider>
  );
};

export default ExceptionSettingPage;
