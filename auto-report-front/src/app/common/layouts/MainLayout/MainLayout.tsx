import React, { useEffect, useState } from 'react';
import { Button, Layout, Menu, Typography } from 'antd';
import {
  AlertOutlined,
  AuditOutlined,
  CalendarOutlined,
  LoginOutlined,
  LogoutOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  UserOutlined,
  WarningOutlined
} from '@ant-design/icons';
import { useDispatch, useSelector } from 'react-redux';
import { useHistory, useLocation } from 'react-router-dom';
import { notifyConfirm } from 'app/common/utils/notify';
import { Path } from 'app/core/router/paths';
import { RootState } from 'app/store/rootReducer';
import { clearAuth } from 'app/store/auth/actions';
import { useAuth } from 'app/store/auth/hooks';
import { fetchConfigRequest } from 'app/store/config/actions';
import siderLogo from 'assets/img/icons/sider-logo.png';
import siderLogoCollapsed from 'assets/img/icons/sider-logo-collapsed.png';

const { Sider, Header, Content } = Layout;
const { Text } = Typography;

interface MenuEntry {
  key: string;
  label: string;
  path: string;
  icon?: React.ReactNode;
}

const MENU_ITEMS: MenuEntry[] = [
  { key: Path.EXCEPTION_EVENT, label: '異常事件管理', path: Path.EXCEPTION_EVENT, icon: <AlertOutlined /> },
  { key: Path.EXCEPTION_SETTING, label: '異常設定', path: Path.EXCEPTION_SETTING, icon: <WarningOutlined /> },
  { key: Path.HOLIDAY_SETTING, label: '節假日設定', path: Path.HOLIDAY_SETTING, icon: <CalendarOutlined /> },
  { key: Path.LOGIN_LOG_DETAIL, label: '登入記錄明細', path: Path.LOGIN_LOG_DETAIL, icon: <LoginOutlined /> },
  { key: Path.OPERATION_LOG_DETAIL, label: '操作記錄明細', path: Path.OPERATION_LOG_DETAIL, icon: <AuditOutlined /> }
];

const MainLayout: React.FC<{ children?: React.ReactNode }> = ({ children }) => {
  const history = useHistory();
  const location = useLocation();
  const dispatch = useDispatch();
  const configLoaded = useSelector((state: RootState) => state.config.loaded);
  const { username } = useAuth();
  const [collapsed, setCollapsed] = useState(false);

  useEffect(() => {
    if (!configLoaded) {
      dispatch(fetchConfigRequest());
    }
  }, [configLoaded, dispatch]);

  const activeKey = MENU_ITEMS.find((item) => item.path === location.pathname)?.key;

  const handleMenuClick = (key: string) => {
    const item = MENU_ITEMS.find((entry) => entry.key === key);
    if (item) {
      history.push(item.path);
    }
  };

  const handleLogout = () => {
    notifyConfirm('確定要登出嗎？', () => {
      dispatch(clearAuth());
      history.push(Path.LOGIN);
    });
  };

  return (
    <Layout className="main-layout">
      <Sider
        theme="light"
        width={260}
        collapsible
        collapsed={collapsed}
        onCollapse={setCollapsed}
        collapsedWidth={80}
        trigger={null}
        className="main-layout-sider"
      >
        <div className={`main-layout-logo${collapsed ? ' is-collapsed' : ''}`}>
          <img src={collapsed ? siderLogoCollapsed : siderLogo} alt="logo" />
          {!collapsed && (
            <Text strong className="main-layout-logo-text">
              自監控系統
            </Text>
          )}
        </div>
        <Menu
          mode="inline"
          className="main-sider-menu"
          selectedKeys={activeKey ? [activeKey] : []}
          items={MENU_ITEMS.map(({ key, label, icon }) => ({ key, label, icon }))}
          onClick={({ key }) => handleMenuClick(String(key))}
        />
      </Sider>

      <Layout>
        <Header className="main-layout-header">
          <Button
            type="text"
            icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
            onClick={() => setCollapsed((prev) => !prev)}
          />
          <Text strong className="main-layout-header-title">
            {activeKey ? MENU_ITEMS.find((item) => item.key === activeKey)?.label : '首頁'}
          </Text>
          <div className="main-layout-header-user">
            <span className="main-layout-header-username">
              <UserOutlined />
              <Text>{username || '未登入'}</Text>
            </span>
            <Button type="link" icon={<LogoutOutlined />} onClick={handleLogout}>
              登出
            </Button>
          </div>
        </Header>
        <Content className="main-layout-content">{children}</Content>
      </Layout>
    </Layout>
  );
};

export default MainLayout;