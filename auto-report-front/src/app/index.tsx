import React from 'react';
import { ConnectedRouter } from 'connected-react-router';
import { ConfigProvider } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import { history } from 'app/store';
import Routers from 'app/core/router';

const App: React.FC = () => (
  <ConfigProvider locale={zhCN} theme={{ token: { colorPrimary: '#348d8f' } }}>
    <ConnectedRouter history={history}>
      <Routers />
    </ConnectedRouter>
  </ConfigProvider>
);

export default App;