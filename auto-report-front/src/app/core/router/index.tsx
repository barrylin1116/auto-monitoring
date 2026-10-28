import React from 'react';
import { Route, Switch } from 'react-router-dom';
import { Path } from './paths';
import LoginPage from 'app/features/Login';
import HomePage from 'app/features/Home';
import NotFoundPage from 'app/features/NotFound';
import MainLayout from 'app/common/layouts/MainLayout';
import ExceptionEventPage from 'app/features/ExceptionEvent';
import ExceptionSettingPage from 'app/features/Setting/ExceptionSetting';
import HolidaySettingPage from 'app/features/Setting/HolidaySetting';
import LoginLogDetailPage from 'app/features/Setting/LoginLogDetail';
import OperationLogDetailPage from 'app/features/Setting/OperationLogDetail';

const Routers: React.FC = () => (
  <Switch>
    {}
    <Route path={Path.LOGIN} component={LoginPage} />

    {}
    <Route
      render={() => (
        <MainLayout>
          <Switch>
            <Route path={Path.EXCEPTION_EVENT} component={ExceptionEventPage} />
            <Route path={Path.EXCEPTION_SETTING} component={ExceptionSettingPage} />
            <Route path={Path.HOLIDAY_SETTING} component={HolidaySettingPage} />
            <Route path={Path.LOGIN_LOG_DETAIL} component={LoginLogDetailPage} />
            <Route path={Path.OPERATION_LOG_DETAIL} component={OperationLogDetailPage} />
            <Route path={Path.ROOT} exact component={HomePage} />
            <Route component={NotFoundPage} />
          </Switch>
        </MainLayout>
      )}
    />
  </Switch>
);

export default Routers;