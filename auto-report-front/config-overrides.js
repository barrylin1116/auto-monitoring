const {
  overrideDevServer
} = require('customize-cra');

const devServerConfig = () => config => {
  return {
    ...config,
    client: {
      overlay: false
    }
  };
};

module.exports = {
  devServer: overrideDevServer(devServerConfig())
};