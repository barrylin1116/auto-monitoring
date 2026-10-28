import React, { useEffect, useState } from 'react';
import { Button, Card, ConfigProvider, DatePicker, Form, Input, Table } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';
import type { Dayjs } from 'dayjs';
import { notifyConfirm, notifyError, notifySuccess } from 'app/common/utils/notify';
import {
  HolidaySetting,
  deleteHolidaySetting,
  fetchHolidaySettings,
  saveHolidaySetting
} from 'app/core/services/holidaySettingService';

interface HolidayFormValues {
  holidayDate: Dayjs;
  remark?: string;
}


const PAGE_SIZE = 6;


const TABLE_CHROME_HEIGHT = 570;

const TABLE_MIN_HEIGHT = 240;

const HolidaySettingPage: React.FC = () => {
  const [form] = Form.useForm<HolidayFormValues>();
  const [list, setList] = useState<HolidaySetting[]>([]);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(1);
  const [total, setTotal] = useState(0);
  const [tableHeight, setTableHeight] = useState<number>(() =>
    Math.max(TABLE_MIN_HEIGHT, window.innerHeight - TABLE_CHROME_HEIGHT)
  );

  
  useEffect(() => {
    const onResize = () =>
      setTableHeight(Math.max(TABLE_MIN_HEIGHT, window.innerHeight - TABLE_CHROME_HEIGHT));
    window.addEventListener('resize', onResize);
    return () => window.removeEventListener('resize', onResize);
  }, []);

  
  const loadList = async (p: number, ps: number) => {
    setLoading(true);
    try {
      const result = await fetchHolidaySettings(p - 1, ps);
      setList(result.content);
      setTotal(result.totalElements);
      return result;
    } catch {
      notifyError('讀取節假日清單失敗', '請確認後端服務是否啟動');
      return null;
    } finally {
      setLoading(false);
    }
  };

  
  const refresh = async () => {
    const result = await loadList(page, PAGE_SIZE);
    if (result && result.content.length === 0 && page > 1) {
      const prev = page - 1;
      setPage(prev);
      await loadList(prev, PAGE_SIZE);
    }
  };

  useEffect(() => {
    loadList(1, PAGE_SIZE);
    
  }, []);

  
  const handleSet = async (values: HolidayFormValues) => {
    const date = values.holidayDate.format('YYYY-MM-DD');
    try {
      await saveHolidaySetting({ holidayDate: date, remark: values.remark });
      form.resetFields();
      const all = await fetchHolidaySettings(0, 10000);
      const idx = all.content.findIndex((item) => item.holidayDate === date);
      const target = idx >= 0 ? Math.floor(idx / PAGE_SIZE) + 1 : page;
      setPage(target);
      await loadList(target, PAGE_SIZE);
      notifySuccess('已設為節假日');
    } catch {
      notifyError('保存失敗', '請確認後端服務是否啟動');
    }
  };

  
  const handleDelete = async (holidayDate: string) => {
    try {
      await deleteHolidaySetting(holidayDate);
      notifySuccess('已取消節假日');
      await refresh();
    } catch {
      notifyError('刪除失敗', '請確認後端服務是否啟動');
    }
  };

  const columns: ColumnsType<HolidaySetting> = [
    {
      title: '節假日日期',
      dataIndex: 'holidayDate',
      key: 'holidayDate',
      width: 200,
      align: 'center'
    },
    {
      title: '備註',
      dataIndex: 'remark',
      key: 'remark',
      align: 'center',
      render: (remark?: string | null) => remark || '-'
    },
    {
      title: '操作',
      key: 'action',
      width: 100,
      align: 'center',
      render: (_, record) => (
        <Button
          type="primary"
          danger
          onClick={() => notifyConfirm('確定取消此節假日？', () => handleDelete(record.holidayDate))}
        >
          刪除
        </Button>
      )
    }
  ];

  return (
    <ConfigProvider componentSize="large" theme={{ token: { colorPrimary: '#348d8f' } }}>
      <Card bordered={false} className="setting-query-card">
        <Form
          form={form}
          layout="horizontal"
          className="setting-page-form"
          onFinish={handleSet}
        >
          <Form.Item
            label="節假日日期"
            name="holidayDate"
            rules={[{ required: true, message: '請選擇日期' }]}
          >
            <DatePicker
              placeholder="請選擇日期"
              style={{ width: '100%' }}
              disabledDate={(current) => current.isBefore(dayjs().add(1, 'day').startOf('day'))}
            />
          </Form.Item>

          <Form.Item label="備註" name="remark">
            <Input allowClear maxLength={100} placeholder="如：國慶日（可選）" />
          </Form.Item>

          <div className="setting-form-actions">
            <Button type="primary" htmlType="submit">
              設為節假日
            </Button>
          </div>
        </Form>
      </Card>

      <Card bordered={false} className="setting-result-card" title="查詢結果">
        <Table
          className="setting-holiday-table"
          rowKey={(record) => record.holidayDate}
          loading={loading}
          dataSource={list}
          columns={columns}
          scroll={{ y: tableHeight }}
          pagination={{
            current: page,
            pageSize: PAGE_SIZE,
            total,
            showSizeChanger: false,
            showTotal: (t) => `共 ${t} 筆`,
            onChange: (p) => {
              setPage(p);
              loadList(p, PAGE_SIZE);
            }
          }}
        />
      </Card>
    </ConfigProvider>
  );
};

export default HolidaySettingPage;