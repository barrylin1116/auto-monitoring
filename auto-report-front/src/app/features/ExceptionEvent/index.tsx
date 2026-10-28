import React, { useEffect, useMemo, useState } from 'react';
import {
  Button,
  Card,
  Col,
  ConfigProvider,
  DatePicker,
  Descriptions,
  Form,
  Input,
  Modal,
  Row,
  Select,
  Space,
  Spin,
  Table,
  Tag,
  Typography,
  Upload
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import type { UploadFile } from 'antd/es/upload/interface';
import { UploadOutlined } from '@ant-design/icons';
import type { Dayjs } from 'dayjs';
import { notifyConfirm, notifyError, notifySuccess } from 'app/common/utils/notify';
import { toLabels, toOptions } from 'app/core/services/configService';
import {
  ExceptionEvent,
  ExceptionEventQuery,
  ExceptionEventUpdatePayload,
  ExceptionProcessRecord,
  RelatedLogRecord,
  deleteExceptionAttachment,
  exceptionAttachmentDownloadUrl,
  fetchExceptionEvent,
  fetchExceptionProcessRecords,
  fetchExceptionEvents,
  fetchExceptionRelatedLogs,
  updateExceptionEvent,
  uploadExceptionAttachments
} from 'app/core/services/exceptionEventService';
import { useConfig } from 'app/store/config/hooks';
import { useAuth } from 'app/store/auth/hooks';

const PAGE_SIZE = 8;


const ATTACHMENT_MAX_SIZE = 10 * 1024 * 1024;

const TABLE_CHROME_HEIGHT = 560;
const TABLE_MIN_HEIGHT = 240;


const RELATED_LOG_ROW_HEIGHT = 47;
const RELATED_LOG_TABLE_HEIGHT = RELATED_LOG_ROW_HEIGHT * 8;


const RISK_COLORS: Record<string, string> = {
  LOW: '#52c41a',
  MEDIUM: '#fa8c16',
  HIGH: '#ff4d4f'
};


const STATUS_COLORS: Record<string, string> = {
  PENDING: 'orange',
  IN_PROGRESS: 'blue',
  TRACKING: 'gold',
  CLOSED: 'green'
};

const formatMillis = (m?: number | null) => {
  if (m == null || m <= 0) return '-';
  try {
    return new Date(m).toLocaleString();
  } catch {
    return String(m);
  }
};


const PROC_ACTION_LABELS: Record<string, string> = {
  SAVE: '存儲',
  CLOSE: '結案'
};


const PROCESS_METHOD_OPTIONS = [
  { value: 'OBSERVE', label: '觀察' },
  { value: 'LOCK_ACCOUNT', label: '鎖定帳號' },
  { value: 'DISABLE_ACCOUNT', label: '停用帳號' },
  { value: 'OTHER', label: '其他' }
];
const PROCESS_METHOD_LABELS: Record<string, string> = Object.fromEntries(
  PROCESS_METHOD_OPTIONS.map((option) => [option.value, option.label])
);


const RISK_ASSESSMENT_OPTIONS = [
  { value: 'HIGH', label: '高' },
  { value: 'MEDIUM', label: '中' },
  { value: 'LOW', label: '低' }
];
const RISK_ASSESSMENT_LABELS: Record<string, string> = Object.fromEntries(
  RISK_ASSESSMENT_OPTIONS.map((option) => [option.value, option.label])
);


const RELATED_LOG_COLUMNS: Record<string, { title: string; dataIndex: string }[]> = {
  MISF_LOGIN: [
    { title: '登入時間', dataIndex: 'loginTime' },
    { title: '帳號', dataIndex: 'account' },
    { title: '姓名', dataIndex: 'name' },
    { title: '登入結果', dataIndex: 'loginResult' },
  ],
  ESERVICE_JD_LOGIN: [
    { title: '登入時間', dataIndex: 'loginTime' },
    { title: '帳號', dataIndex: 'account' },
    { title: '姓名', dataIndex: 'name' },
    { title: '登入結果', dataIndex: 'loginResult' },
  ]
};


const collectRelatedColumns = (rows: RelatedLogRecord[]) => {
  const keys: string[] = [];
  rows.forEach((row) => {
    Object.keys(row).forEach((k) => {
      if (!keys.includes(k)) keys.push(k);
    });
  });
  return keys.map((k) => ({ title: k, dataIndex: k }));
};

interface ProcessFormValues {
  status?: string;
  processedBy?: string;
  processMethod?: string;
  riskAssessment?: string;
  processRemark?: string;
  processedAt?: Dayjs;
}

const ExceptionEventPage: React.FC = () => {
  const [form] = Form.useForm<ExceptionEventQuery>();
  const [processForm] = Form.useForm<ProcessFormValues>();
  const [list, setList] = useState<ExceptionEvent[]>([]);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(1);
  const [total, setTotal] = useState(0);
  const [query, setQuery] = useState<ExceptionEventQuery>({});
  const [detail, setDetail] = useState<ExceptionEvent | null>(null);
  const [detailLoading, setDetailLoading] = useState(false);
  const [relatedLogs, setRelatedLogs] = useState<RelatedLogRecord[]>([]);
  const [relatedLoading, setRelatedLoading] = useState(false);
  const [procRecords, setProcRecords] = useState<ExceptionProcessRecord[]>([]);
  const [procLoading, setProcLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [closing, setClosing] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [fileList, setFileList] = useState<UploadFile[]>([]);
  const [tableHeight, setTableHeight] = useState<number>(() =>
    Math.max(TABLE_MIN_HEIGHT, window.innerHeight - TABLE_CHROME_HEIGHT)
  );

  const { systemType, matchedRule, riskLevel, eventStatus } = useConfig();
  const { username } = useAuth();
  const systemTypeOptions = useMemo(() => toOptions(systemType), [systemType]);
  const systemTypeLabels = useMemo(() => toLabels(systemType), [systemType]);
  const eventTypeOptions = useMemo(() => toOptions(matchedRule), [matchedRule]);
  const eventTypeLabels = useMemo(() => toLabels(matchedRule), [matchedRule]);
  const riskLevelOptions = useMemo(() => toOptions(riskLevel), [riskLevel]);
  const riskLevelLabels = useMemo(() => toLabels(riskLevel), [riskLevel]);
  const statusOptions = useMemo(() => toOptions(eventStatus), [eventStatus]);
  const processStatusOptions = useMemo(
    () => statusOptions.filter((option) => option.value !== 'CLOSED'),
    [statusOptions]
  );
  const statusLabels = useMemo(() => toLabels(eventStatus), [eventStatus]);

  useEffect(() => {
    const onResize = () =>
      setTableHeight(Math.max(TABLE_MIN_HEIGHT, window.innerHeight - TABLE_CHROME_HEIGHT));
    window.addEventListener('resize', onResize);
    return () => window.removeEventListener('resize', onResize);
  }, []);

  const loadList = async (p: number, q: ExceptionEventQuery) => {
    setLoading(true);
    try {
      const result = await fetchExceptionEvents(q, p - 1, PAGE_SIZE);
      setList(result.content);
      setTotal(result.totalElements);
    } catch {
      notifyError('讀取異常事件失敗', '請確認後端服務是否啟動');
    } finally {
      setLoading(false);
    }
  };

  
  useEffect(() => {
    loadList(1, {});
    
  }, []);

  const handleSearch = (values: ExceptionEventQuery) => {
    setQuery(values);
    setPage(1);
    loadList(1, values);
  };

  const handleClear = () => {
    form.resetFields();
    setQuery({});
  };

  const loadRelatedLogs = async (id?: string | null) => {
    if (!id) {
      setRelatedLogs([]);
      return;
    }
    setRelatedLoading(true);
    try {
      setRelatedLogs(await fetchExceptionRelatedLogs(id));
    } catch {
      setRelatedLogs([]);
    } finally {
      setRelatedLoading(false);
    }
  };

  const loadProcessRecords = async (id?: string | null) => {
    if (!id) {
      setProcRecords([]);
      return;
    }
    setProcLoading(true);
    try {
      setProcRecords(await fetchExceptionProcessRecords(id));
    } catch {
      setProcRecords([]);
    } finally {
      setProcLoading(false);
    }
  };

  const handleView = async (record: ExceptionEvent) => {
    const id = record.id;
    if (!id) {
      setDetail(record);
      return;
    }
    
    setDetail(record);
    loadRelatedLogs(id);
    loadProcessRecords(id);
    setDetailLoading(true);
    try {
      const fresh = await fetchExceptionEvent(id);
      setDetail(fresh);
      
      processForm.resetFields();
      processForm.setFieldsValue({
        processedBy: username || undefined
      });
    } catch {
      notifyError('讀取異常事件詳情失敗', '請確認後端服務是否啟動');
    } finally {
      setDetailLoading(false);
    }
  };

  const handleCloseDetail = () => {
    setDetail(null);
    setDetailLoading(false);
    setRelatedLogs([]);
    setProcRecords([]);
    setFileList([]);
    processForm.resetFields();
  };

  useEffect(() => {
    if (detail) {
      setFileList(
        (detail.attachments ?? []).map((item) => ({
          uid: item.id || item.fileName || String(item.uploadedAt),
          name: item.fileName || '未命名檔案',
          status: 'done',
          url: item.id ? exceptionAttachmentDownloadUrl(item.id) : undefined
        }))
      );
    }
    
  }, [detail]);

  const toProcessPayload = (values: Partial<ProcessFormValues>): ExceptionEventUpdatePayload => ({
    status: values.status,
    processedBy: username || undefined,
    processMethod: values.processMethod,
    riskAssessment: values.riskAssessment,
    processRemark: values.processRemark,
    processedAt: values.processedAt ? values.processedAt.valueOf() : undefined
  });

  const handleUpload = async (files: File[]) => {
    const id = detail?.id;
    if (!id || files.length === 0) return;
    setUploading(true);
    try {
      const uploaded = await uploadExceptionAttachments(id, files, username || undefined);
      setDetail((prev) => (prev ? { ...prev, attachments: [...uploaded, ...(prev.attachments ?? [])] } : prev));
      notifySuccess('附件已上傳');
    } catch {
      notifyError('附件上傳失敗', '請確認後端服務是否啟動');
    } finally {
      setUploading(false);
    }
  };

  const handleRemoveAttachment = async (file: UploadFile) => {
    const id = typeof file.uid === 'string' ? file.uid : '';
    if (!id) {
      setFileList((prev) => prev.filter((item) => item.uid !== file.uid));
      return;
    }
    try {
      await deleteExceptionAttachment(id);
      setDetail((prev) =>
        prev
          ? { ...prev, attachments: (prev.attachments ?? []).filter((item) => item.id !== id) }
          : prev
      );
      notifySuccess('附件已刪除');
    } catch {
      notifyError('附件刪除失敗', '請確認後端服務是否啟動');
    }
  };

  const handleSave = async () => {
    const id = detail?.id;
    if (!id) return;
    let values: ProcessFormValues;
    try {
      values = await processForm.validateFields();
    } catch {
      return;
    }
    setSaving(true);
    try {
      await updateExceptionEvent(id, {
        ...toProcessPayload(values),
        action: 'SAVE'
      });
      notifySuccess('處理資訊已存儲');
      await loadList(page, query);
      handleCloseDetail();
    } catch {
      notifyError('存儲失敗', '請確認後端服務是否啟動');
    } finally {
      setSaving(false);
    }
  };

  const handleClose = async () => {
    const id = detail?.id;
    if (!id) return;
    let values: ProcessFormValues;
    try {
      values = await processForm.validateFields();
    } catch {
      return;
    }
    notifyConfirm('確定要結案嗎？結案後狀態將設為「已結案」。', async () => {
      setClosing(true);
      try {
        await updateExceptionEvent(id, {
          ...toProcessPayload(values),
          status: 'CLOSED',
          action: 'CLOSE'
        });
        notifySuccess('已結案');
        await loadList(page, query);
        handleCloseDetail();
      } catch {
        notifyError('結案失敗', '請確認後端服務是否啟動');
      } finally {
        setClosing(false);
      }
    });
  };

  const isClosed = detail?.status === 'CLOSED';

  const renderRisk = (value?: string | null) => {
    if (!value) return '-';
    return (
      <span style={{ color: RISK_COLORS[value], fontWeight: 600 }}>
        {riskLevelLabels[value] || value}
      </span>
    );
  };

  const renderStatus = (value?: string | null) => {
    if (!value) return '-';
    return <Tag color={STATUS_COLORS[value]}>{statusLabels[value] || value}</Tag>;
  };

  const columns: ColumnsType<ExceptionEvent> = [
    {
      title: '發生時間',
      dataIndex: 'occurredAt',
      key: 'occurredAt',
      width: 170,
      ellipsis: true,
      render: formatMillis
    },
    {
      title: '異常類型',
      dataIndex: 'eventType',
      key: 'eventType',
      width: 150,
      render: (v) => (v ? eventTypeLabels[v] || v : '-')
    },
    {
      title: '風險等級',
      dataIndex: 'riskLevel',
      key: 'riskLevel',
      width: 100,
      align: 'center',
      render: renderRisk
    },
    {
      title: '系統別',
      dataIndex: 'systemType',
      key: 'systemType',
      width: 120,
      render: (v) => (v ? systemTypeLabels[v] || v : '-')
    },
    { title: '帳號', dataIndex: 'account', key: 'account', width: 130, render: (v) => v || '-' },
    { title: '來源IP', dataIndex: 'sourceIp', key: 'sourceIp', width: 130, render: (v) => v || '-' },
    {
      title: '事件描述',
      dataIndex: 'description',
      key: 'description',
      width: 260,
      ellipsis: true,
      render: (v) => v || '-'
    },
    {
      title: '狀態',
      dataIndex: 'status',
      key: 'status',
      width: 110,
      align: 'center',
      render: renderStatus
    },
    {
      title: '操作',
      key: 'action',
      width: 90,
      align: 'center',
      render: (_, record) => (
        <Button type="link" onClick={() => handleView(record)}>
          查看
        </Button>
      )
    }
  ];

  
  const relatedColumns: ColumnsType<RelatedLogRecord> = useMemo(() => {
    const defined = detail?.bizName ? RELATED_LOG_COLUMNS[detail.bizName] : undefined;
    const specs = defined ?? collectRelatedColumns(relatedLogs);
    return specs.map(({ title, dataIndex }) => ({
      title,
      dataIndex,
      key: dataIndex,
      ellipsis: true,
      render: (v: unknown) => (v == null || v === '' ? '-' : String(v))
    }));
  }, [detail?.bizName, relatedLogs]);

  const procColumns: ColumnsType<ExceptionProcessRecord> = [
    {
      title: '處理時間',
      dataIndex: 'processedAt',
      key: 'processedAt',
      width: 170,
      ellipsis: true,
      render: formatMillis
    },
    {
      title: '處理動作',
      dataIndex: 'action',
      key: 'action',
      width: 90,
      render: (v) => (v ? PROC_ACTION_LABELS[v] || v : '-')
    },
    {
      title: '處理狀態',
      dataIndex: 'status',
      key: 'status',
      width: 110,
      align: 'center',
      render: renderStatus
    },
    {
      title: '處理人員',
      dataIndex: 'processedBy',
      key: 'processedBy',
      width: 120,
      render: (v) => v || '-'
    },
    {
      title: '處理方式',
      dataIndex: 'processMethod',
      key: 'processMethod',
      width: 120,
      render: (v) => (v ? PROCESS_METHOD_LABELS[v] || v : '-')
    },
    {
      title: '風險評估',
      dataIndex: 'riskAssessment',
      key: 'riskAssessment',
      width: 100,
      render: (v) => (v ? RISK_ASSESSMENT_LABELS[v] || v : '-')
    },
    {
      title: '處理說明',
      dataIndex: 'processRemark',
      key: 'processRemark',
      ellipsis: true,
      render: (v) => v || '-'
    }
  ];

  return (
    <ConfigProvider componentSize="large" theme={{ token: { colorPrimary: '#348d8f' } }}>
      <Card bordered={false} className="setting-query-card">
        <Form form={form} className="loginlog-query-form" onFinish={handleSearch}>
          <div className="query-form-row">
            <Form.Item label="系統別" name="systemType">
              <Select allowClear placeholder="全部" options={systemTypeOptions} />
            </Form.Item>
            <Form.Item label="異常類型" name="eventType">
              <Select allowClear placeholder="全部" options={eventTypeOptions} />
            </Form.Item>
          </div>
          <div className="query-form-row">
            <Form.Item label="風險等級" name="riskLevel">
              <Select allowClear placeholder="全部" options={riskLevelOptions} />
            </Form.Item>
            <Form.Item label="狀態" name="status">
              <Select allowClear placeholder="全部" options={statusOptions} />
            </Form.Item>
          </div>
          <div className="query-form-row">
            <Form.Item label="帳號" name="account">
              <Input allowClear placeholder="請輸入帳號" />
            </Form.Item>
          </div>
          <div className="query-form-actions">
            <Button type="primary" htmlType="submit">
              查詢
            </Button>
            <Button htmlType="button" onClick={handleClear}>
              清空
            </Button>
          </div>
        </Form>
      </Card>

      <Card bordered={false} className="setting-result-card" title="查詢結果">
        <Table
          className="setting-holiday-table"
          rowKey={(record, index) => record.id || `${record.account || ''}-${index ?? ''}`}
          loading={loading}
          dataSource={list}
          columns={columns}
          scroll={{ x: 1260, y: tableHeight }}
          pagination={{
            current: page,
            pageSize: PAGE_SIZE,
            total,
            showSizeChanger: false,
            showTotal: (t) => `共 ${t} 筆`,
            onChange: (p) => {
              setPage(p);
              loadList(p, query);
            }
          }}
        />
      </Card>

      <ConfigProvider componentSize="small" theme={{ token: { colorPrimary: '#348d8f' } }}>
        <Modal
          title="異常事件詳情"
          open={!!detail}
          onCancel={handleCloseDetail}
          forceRender
          width={1120}
        footer={
          <div style={{ textAlign: 'right' }}>
            <Button onClick={handleCloseDetail}>取消</Button>
            <Button
              type="primary"
              style={{ marginLeft: 8 }}
              loading={saving}
              disabled={isClosed}
              onClick={handleSave}
            >
              存儲
            </Button>
            <Button
              danger
              style={{ marginLeft: 8 }}
              loading={closing}
              disabled={isClosed}
              onClick={handleClose}
            >
              結案
            </Button>
          </div>
        }
      >
        <Card bordered={false} size="small" styles={{ header: { backgroundColor: '#e6f4f3' } }} title="事件資訊">
          <Spin spinning={detailLoading}>
            <Descriptions column={2} bordered size="small">
              <Descriptions.Item label="事件編號">{detail?.eventNo || '-'}</Descriptions.Item>
              <Descriptions.Item label="使用者帳號">{detail?.account || '-'}</Descriptions.Item>
              <Descriptions.Item label="異常類型">
                {detail?.eventType ? eventTypeLabels[detail.eventType] || detail.eventType : '-'}
              </Descriptions.Item>
              <Descriptions.Item label="使用者姓名">{detail?.name || '-'}</Descriptions.Item>
              <Descriptions.Item label="系統平臺">
                {detail?.systemType ? systemTypeLabels[detail.systemType] || detail.systemType : '-'}
              </Descriptions.Item>
              <Descriptions.Item label="通路分支">{detail?.channelBranch || '-'}</Descriptions.Item>
              <Descriptions.Item label="發生時間">{formatMillis(detail?.occurredAt)}</Descriptions.Item>
              <Descriptions.Item label="風險等級">{renderRisk(detail?.riskLevel)}</Descriptions.Item>
              <Descriptions.Item label="來源IP">{detail?.sourceIp || '-'}</Descriptions.Item>
              <Descriptions.Item label="狀態">{renderStatus(detail?.status)}</Descriptions.Item>
              <Descriptions.Item label="裝置訊息" span={2}>
                {detail?.deviceInfo || '-'}
              </Descriptions.Item>
            </Descriptions>
          </Spin>
        </Card>

        <Card bordered={false} size="small" styles={{ header: { backgroundColor: '#e6f4f3' } }} title="異常原因與詳細說明" style={{ marginTop: 8 }}>
          <Descriptions column={1} bordered size="small">
            <Descriptions.Item label="異常原因">{detail?.description || '-'}</Descriptions.Item>
            <Descriptions.Item label="詳細說明">{detail?.detailDescription || '-'}</Descriptions.Item>
          </Descriptions>
        </Card>

        <Card bordered={false} size="small" styles={{ header: { backgroundColor: '#e6f4f3' } }} title="操作記錄" style={{ marginTop: 8 }}>
          <Table
            className="setting-holiday-table"
            rowKey={(_, index) => `${detail?.id || ''}-${index ?? ''}`}
            size="small"
            loading={relatedLoading}
            dataSource={relatedLogs}
            columns={relatedColumns}
            scroll={{ x: 700, y: RELATED_LOG_TABLE_HEIGHT }}
            pagination={false}
            locale={{ emptyText: '尚無相關記錄' }}
          />
        </Card>

        {!isClosed && (
        <Card bordered={false} size="small" styles={{ header: { backgroundColor: '#e6f4f3' } }} title="處理資訊" style={{ marginTop: 8 }}>
            <Form form={processForm} layout="vertical" disabled={isClosed}>
            <Row gutter={16}>
              <Col span={12}>
                <Form.Item label="處理狀態" name="status" rules={[{ required: true, message: '請選擇處理狀態' }]}>
                  <Select allowClear placeholder="請選擇狀態" options={processStatusOptions} />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item label="處理人員" name="processedBy" rules={[{ required: true, message: '請輸入處理人員' }]} hidden>
                  <Input readOnly placeholder="請輸入處理人員" maxLength={50} />
                </Form.Item>
                <Form.Item label="處理人員">
                  <span>{username || '-'}</span>
                </Form.Item>
              </Col>
            </Row>
            <Row gutter={16}>
              <Col span={12}>
                <Form.Item label="處理方式" name="processMethod" rules={[{ required: true, message: '請選擇處理方式' }]}>
                  <Select allowClear placeholder="請選擇處理方式" options={PROCESS_METHOD_OPTIONS} />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item label="風險評估" name="riskAssessment" rules={[{ required: true, message: '請選擇風險評估' }]}>
                  <Select allowClear placeholder="請選擇風險評估" options={RISK_ASSESSMENT_OPTIONS} />
                </Form.Item>
              </Col>
            </Row>
              <Row gutter={16}>
                <Col span={12}>
                  <Form.Item label="處理時間" name="processedAt">
                    <DatePicker
                        showTime
                        format="YYYY-MM-DD HH:mm:ss"
                        style={{ width: '100%' }}
                        placeholder="請選擇處理時間"
                        getPopupContainer={(trigger) => trigger.parentElement ?? document.body}
                    />
                  </Form.Item>
                </Col>
                <Col span={12}>
                  <Form.Item label="附件">
                    <Space direction="vertical" style={{ width: '100%' }}>
                      <Upload
                        multiple
                        accept=".png,.jpeg,.jpg,.pdf,image/png,image/jpeg,application/pdf"
                        fileList={fileList}
                        beforeUpload={(file) => {
                          const allowed = ['png', 'jpeg', 'jpg', 'pdf'];
                          const ext = file.name.split('.').pop()?.toLowerCase() ?? '';
                          if (!allowed.includes(ext)) {
                            notifyError('檔案格式不支援', '僅允許 PNG、JPEG、PDF');
                            return Upload.LIST_IGNORE;
                          }
                          if (file.size > ATTACHMENT_MAX_SIZE) {
                            notifyError('檔案過大', '單檔上限 10MB');
                            return Upload.LIST_IGNORE;
                          }
                          handleUpload([file as unknown as File]);
                          return false;
                        }}
                        onRemove={handleRemoveAttachment}
                        onDownload={(file) => {
                          if (file.url) window.open(file.url);
                        }}
                      >
                        <Button icon={<UploadOutlined />} loading={uploading}>
                          選擇檔案（可多選）
                        </Button>
                      </Upload>
                      <Typography.Text type="secondary">支援多檔上傳，僅限 PNG / JPEG / PDF，單檔上限 10MB。</Typography.Text>
                    </Space>
                  </Form.Item>
                </Col>
              </Row>
              <Row gutter={16}>
                <Col span={24}>
                  <Form.Item
                    label="調查結果"
                    name="processRemark"
                    rules={[{ required: true, message: '請輸入調查結果' }]}
                  >
                    <Input.TextArea rows={3} maxLength={500} showCount placeholder="請輸入調查結果" />
                  </Form.Item>
                </Col>
              </Row>
          </Form>
        </Card>
        )}

        <Card bordered={false} size="small" styles={{ header: { backgroundColor: '#e6f4f3' } }} title="處理歷程" style={{ marginTop: 8 }}>
          <Table
            className="setting-holiday-table"
            rowKey={(record, index) => record.id || `${record.processedAt ?? ''}-${index ?? ''}`}
            size="small"
            loading={procLoading}
            dataSource={procRecords}
            columns={procColumns}
            scroll={{ x: 700 }}
            pagination={false}
            locale={{ emptyText: '尚無處理歷程' }}
          />
        </Card>
        </Modal>
      </ConfigProvider>
    </ConfigProvider>
  );
};

export default ExceptionEventPage;
