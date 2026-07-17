import { API_BASE } from './config/index'

App<IAppOption>({
  globalData: { token: '', apiBase: API_BASE },
  onLaunch() {
    this.globalData.token = wx.getStorageSync<string>('one_token') || ''
    this.ensureLogin().catch(() => undefined)
  },
  ensureLogin(): Promise<string> {
    if (this.globalData.token) return Promise.resolve(this.globalData.token)
    return new Promise((resolve, reject) => {
      wx.login({
        success: ({ code }) => wx.request({
          url: `${this.globalData.apiBase}/auth/wechat`, method: 'POST', data: { code },
          success: (response) => {
            const data = response.data as { token?: string }
            if (!data.token) { reject(new Error('登录失败')); return }
            this.globalData.token = data.token
            wx.setStorageSync('one_token', data.token)
            resolve(data.token)
          },
          fail: reject
        }),
        fail: reject
      })
    })
  }
})
