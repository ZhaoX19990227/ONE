import { API_BASE_URL } from '../config/index';

export async function loginWithWechat(): Promise<string> {
  const loginResult = await new Promise<WechatMiniprogram.LoginSuccessCallbackResult>((resolve, reject) => {
    wx.login({ success: resolve, fail: reject });
  });
  const response = await new Promise<WechatMiniprogram.RequestSuccessCallbackResult>((resolve, reject) => {
    wx.request({
      url: `${API_BASE_URL}/auth/wechat`,
      method: 'POST',
      header: { 'content-type': 'application/json' },
      data: { code: loginResult.code },
      success: resolve,
      fail: reject
    });
  });
  if (response.statusCode < 200 || response.statusCode >= 300) {
    throw new Error('微信登录失败');
  }
  const token = (response.data as { token: string }).token;
  getApp<IAppOption>().globalData.token = token;
  wx.setStorageSync('one_token', token);
  return token;
}
