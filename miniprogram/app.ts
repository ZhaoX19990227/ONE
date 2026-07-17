import { USE_MOCK } from './config/index';
import { loginWithWechat } from './services/auth';

App<IAppOption>({
  globalData: {
    token: wx.getStorageSync('one_token') || '',
    selectedCity: wx.getStorageSync('one_city') || '上海'
  },

  onLaunch() {
    const accountInfo = wx.getAccountInfoSync();
    console.info('[ONE] launch', accountInfo.miniProgram.envVersion);
    if (!USE_MOCK && !this.globalData.token) {
      loginWithWechat().catch(() => console.warn('[ONE] silent login failed'));
    }
  }
});
