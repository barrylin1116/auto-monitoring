import React, {useEffect, useMemo, useState} from 'react';
import { Button, Card, ConfigProvider, DatePicker, Form, Input, Select, Table } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import type { Dayjs } from 'dayjs';
import { notifyError, notifyInfo } from 'app/common/utils/notify';
import {
  LoginRecord,
  LoginRecordQuery,
  fetchLoginRecords
} from 'app/core/services/loginRecordService';
import {toOptions} from "../../../core/services/configService";
import {useConfig} from "../../../store/config/hooks";


interface LoginQueryForm extends LoginRecordQuery {
  loginTimeRange?: [Dayjs, Dayjs] | null;
}


const PAGE_SIZE = 8;


const TABLE_CHROME_HEIGHT = 560;

const TABLE_MIN_HEIGHT = 240;

const LOGIN_RESULT_OPTIONS = [
  { value: '成功', label: '成功' },
  { value: '失敗', label: '失敗' }
];


const renderResult = (result?: string | null) => {
  if (!result) return '-';
  const color = result.includes('成功') ? '#348d8f' : result.includes('失敗') ? '#ff4d4f' : undefined;
  return <span style={color ? { color, fontWeight: 600 } : undefined}>{result}</span>;
};



const LoginLogDetailPage: React.FC = () => {
  const [form] = Form.useForm<LoginQueryForm>();
  const [list, setList] = useState<LoginRecord[]>([]);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(1);
  const [total, setTotal] = useState(0);
  const [query, setQuery] = useState<LoginRecordQuery>({});
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

  const loadList = async (p: number, q: LoginRecordQuery) => {
    setLoading(true);
    try {
      const result = await fetchLoginRecords(q, p - 1, PAGE_SIZE);
      setList(result.content);
      setTotal(result.totalElements);
    } catch {
      notifyError('讀取登入記錄失敗', '請確認後端服務是否啟動');
    } finally {
      setLoading(false);
    }
  };

  
  const handleSearch = (values: LoginQueryForm) => {
    if (!values.system) {
      notifyInfo('請選擇系統別', '');
      return;
    }
    const range = values.loginTimeRange;
    const payload: LoginRecordQuery = {
      system: values.system,
      account: values.account,
      name: values.name,
      loginResult: values.loginResult,
      loginTimeStart: range && range[0] ? range[0].format('YYYY-MM-DD') : undefined,
      loginTimeEnd: range && range[1] ? range[1].format('YYYY-MM-DD') : undefined
    };
    setQuery(payload);
    setPage(1);
    loadList(1, payload);
  };

  
  const handleClear = () => {
    form.resetFields();
    setQuery({});
  };

  const columns: ColumnsType<LoginRecord> = [
    { title: '帳號', dataIndex: 'account', key: 'account', width: 130, render: (v) => v || '-' },
    {
      title: '姓名',
      dataIndex: 'name',
      key: 'name',
      width: 160,
      onCell: () => ({ style: { whiteSpace: 'nowrap' } }),
      render: (v) => v || '-'
    },
    {
      title: '通路/分支',
      dataIndex: 'channelBranch',
      key: 'channelBranch',
      width: 180,
      ellipsis: true,
      render: (v) => v || '-'
    },
    {
      title: '角色',
      dataIndex: 'role',
      key: 'role',
      width: 140,
      ellipsis: true,
      render: (v) => v || '-'
    },
    {
      title: '登入時間',
      dataIndex: 'loginTime',
      key: 'loginTime',
      width: 150,
      ellipsis: true,
      render: (v) => v || '-'
    },
    {
      title: '登出時間',
      dataIndex: 'logoutTime',
      key: 'logoutTime',
      width: 150,
      ellipsis: true,
      render: (v) => v || '-'
    },
    {
      title: '登入結果',
      dataIndex: 'loginResult',
      key: 'loginResult',
      width: 100,
      align: 'center',
      render: renderResult
    },
    {
      title: '失敗次數',
      dataIndex: 'failCount',
      key: 'failCount',
      width: 90,
      align: 'center',
      render: (v) => (v ?? 0) as number
    },
    { title: '來源IP', dataIndex: 'sourceIp', key: 'sourceIp', width: 130, render: (v) => v || '-' },
    { title: '來源地區', dataIndex: 'sourceArea', key: 'sourceArea', width: 110, render: (v) => v || '-' },
    {
      title: '裝置資訊',
      dataIndex: 'deviceInfo',
      key: 'deviceInfo',
      width: 220,
      ellipsis: true,
      render: (v) => v || '-'
    },
    {
      title: '登入方式',
      dataIndex: 'loginMethod',
      key: 'loginMethod',
      width: 100,
      align: 'center',
      render: (v) => v || '-'
    }
  ];

  return (
    <ConfigProvider componentSize="large" theme={{ token: { colorPrimary: '#348d8f' } }}>
      <Card bordered={false} className="setting-query-card">
        <Form form={form} className="loginlog-query-form" onFinish={handleSearch}>
          <div className="query-form-row">
            <Form.Item label="系統別" name="system" required>
              <Select allowClear placeholder="請選擇系統別" options={systemTypeOptions} />
            </Form.Item>
            <Form.Item label="帳號" name="account">
              <Input allowClear placeholder="請輸入帳號" />
            </Form.Item>
          </div>
          <div className="query-form-row">
            <Form.Item label="姓名" name="name">
              <Input allowClear placeholder="請輸入姓名" />
            </Form.Item>
            <Form.Item label="登入結果" name="loginResult">
              <Select allowClear placeholder="全部" options={LOGIN_RESULT_OPTIONS} />
            </Form.Item>
          </div>
          <div className="query-form-row">
            <Form.Item label="登入時間" name="loginTimeRange">
              <DatePicker.RangePicker
                style={{ width: '100%' }}
                format="YYYY-MM-DD"
                placeholder={['開始日期', '結束日期']}
              />
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
            `${record.account || ''}-${record.loginTime || ''}-${index ?? ''}`
          }
          loading={loading}
          dataSource={list}
          columns={columns}
          scroll={{ x: 1550, y: tableHeight }}
          pagination={{
            current: page,
            pageSize: PAGE_SIZE,
            total,
            showSizeChanger: false,
            showTotal: (t) => `共 ${t} 筆`,
            onChange: (p) => {
              if (!query.system) {
                notifyInfo('請選擇系統別', '系統別為必填查詢條件');
                return;
              }
              setPage(p);
              loadList(p, query);
            }
          }}
        />
      </Card>
    </ConfigProvider>
  );
};

export default LoginLogDetailPage;
