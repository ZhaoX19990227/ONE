const app = () => getApp<IAppOption>()

export async function request<T>(path: string, method: WechatMiniprogram.RequestOption['method'] = 'GET', data?: WechatMiniprogram.IAnyObject | string | ArrayBuffer): Promise<T> {
  const token = await app().ensureLogin()
  return new Promise((resolve, reject) => wx.request({
    url: `${app().globalData.apiBase}${path}`, method, data,
    header: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
    success: (response) => response.statusCode >= 200 && response.statusCode < 300
      ? resolve(response.data as T)
      : reject(new Error((response.data as { message?: string })?.message || '请求失败')),
    fail: reject
  }))
}

export async function uploadImage(filePath: string): Promise<{ id: number; url: string; thumbnailUrl: string }> {
  const token = await app().ensureLogin()
  return new Promise((resolve, reject) => wx.uploadFile({
    url: `${app().globalData.apiBase}/media/images`, filePath, name: 'file',
    header: { Authorization: `Bearer ${token}` },
    success: (response) => response.statusCode >= 200 && response.statusCode < 300
      ? resolve(JSON.parse(response.data)) : reject(new Error('照片上传失败')),
    fail: reject
  }))
}
