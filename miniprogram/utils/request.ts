import { API_BASE_URL } from '../config/index';

export function request<T>(options: WechatMiniprogram.RequestOption): Promise<T> {
  const app = getApp<IAppOption>();
  return new Promise((resolve, reject) => {
    wx.request({
      ...options,
      url: `${API_BASE_URL}${options.url}`,
      header: {
        'content-type': 'application/json',
        ...(app.globalData.token ? { Authorization: `Bearer ${app.globalData.token}` } : {}),
        ...(options.header || {})
      },
      success(response) {
        if (response.statusCode >= 200 && response.statusCode < 300) {
          resolve(response.data as T);
          return;
        }
        reject(response.data);
      },
      fail: reject
    });
  });
}
