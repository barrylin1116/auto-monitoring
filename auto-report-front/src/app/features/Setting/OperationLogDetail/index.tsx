import React, { useEffect, useMemo, useState } from 'react';
import { Button, Card, ConfigProvider, Form, Input, Select, Table } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { notifyError } from 'app/common/utils/notify';
import { toOptions } from 'app/core/services/configService';
import { OperationLog, OperationLogQuery, fetchOperationLogs } from 'app/core/services/operationLogService';
import { useConfig } from 'app/store/config/hooks';

const PAGE_SIZE = 8;

const TABLE_CHROME_HEIGHT = 560;
const TABLE_MIN_HEIGHT = 240;

const RESULT_OPTIONS = [
  { value: 'SUCCESS', label: '成功' },
  { value: 'FAILED', label: '失敗' }
];

const RESULT_LABELS: Record<string, string> = {
  SUCCESS: '成功',
  FAILED: '失敗'
};


const renderResult = (result?: string | null) => {
  if (!result) return '-';
  const label = RESULT_LABELS[result] || result;
  const color = result === 'SUCCESS' ? '#348d8f' : result === 'FAILED' ? '#ff4d4f' : undefined;
  return <span style={color ? { color, fontWeight: 600 } : undefined}>{label}</span>;
};

const OperationLogDetailPage: React.FC = () => {
  const [form] = Form.useForm<OperationLogQuery>();
  const [list, setList] = useState<OperationLog[]>([]);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(1);
  const [total, setTotal] = useState(0);
  const [query, setQuery] = useState<OperationLogQuery>({});
  const [tableHeight, setTableHeight] = useState<number>(() =>
    Math.max(TABLE_MIN_HEIGHT, window.innerHeight - TABLE_CHROME_HEIGHT)
  );

  const { systemType } = useConfig();
  const systemTypeOptions = useMemo(() => toOptions(systemType), [systemType]);

  useEffect(() => {
    const onResize = () =>
      setTableHeight(Math.max(TABLE_MIN_HEIGHT, window.innerHeight - TABLE_CHROME_HEIGHT));
    window.addEventListener('resize', onResize);
    return () => window.removeEventListener('resize', onResize);
  }, []);

  const loadList = async (p: number, q: OperationLogQuery) => {
    setLoading(true);
    try {
      const result = await fetchOperationLogs(q, p - 1, PAGE_SIZE);
      setList(result.content);
      setTotal(result.totalElements);
    } catch {
      notifyError('讀取操作記錄失敗', '請確認後端服務是否啟動');
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (values: OperationLogQuery) => {
    setQuery(values);
    setPage(1);
    loadList(1, values);
  };

  const handleClear = () => {
    form.resetFields();
    setQuery({});
  };

  const columns: ColumnsType<OperationLog> = [
    { title: '帳號', dataIndex: 'account', key: 'account', width: 150, render: (v) => v || '-' },
    {
      title: '操作時間',
      dataIndex: 'operationTime',
      key: 'operationTime',
      width: 180,
      ellipsis: true,
      render: (v) => v || '-'
    },
    {
      title: '功能/畫面',
      dataIndex: 'functionScreen',
      key: 'functionScreen',
      width: 220,
      ellipsis: true,
      render: (v) => v || '-'
    },
    {
      title: '操作動作',
      dataIndex: 'action',
      key: 'action',
      width: 120,
      render: (v) => v || '-'
    },
    {
      title: '操作結果',
      dataIndex: 'actionResult',
      key: 'actionResult',
      width: 110,
      align: 'center',
      render: renderResult
    },
    {
      title: '操作内容',
      dataIndex: 'actionContent',
      key: 'actionContent',
      ellipsis: true,
      render: (v) => v || '-'
    }
  ];

  return (
    <ConfigProvider componentSize="large" theme={{ token: { colorPrimary: '#348d8f' } }}>
      <Card bordered={false} className="setting-query-card">
        <Form form={form} className="loginlog-query-form" onFinish={handleSearch}>
          <div className="query-form-row">
            <Form.Item label="系統別" name="systemType" rules={[{ required: true, message: '請選擇系統別' }]}>
              <Select placeholder="請選擇" options={systemTypeOptions} />
            </Form.Item>
            <Form.Item label="帳號" name="account">
              <Input allowClear placeholder="請輸入帳號" />
            </Form.Item>
          </div>
          <div className="query-form-row">
            <Form.Item label="操作動作" name="action">
              <Input allowClear placeholder="請輸入操作動作" />
            </Form.Item>
            <Form.Item label="操作結果" name="actionResult">
              <Select allowClear placeholder="全部" options={RESULT_OPTIONS} />
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
          rowKey={(record, index) =>
            record.id ||
            `${record.systemType || ''}-${record.account || ''}-${record.operationTime ?? ''}-${index ?? ''}`
          }
          loading={loading}
          dataSource={list}
          columns={columns}
          scroll={{ x: 1020, y: tableHeight }}
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
    </ConfigProvider>
  );
};

export default OperationLogDetailPage;
