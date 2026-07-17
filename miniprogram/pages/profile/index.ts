Page({
  data: {
    tags: ['羽毛球 · 初级', '桌游 · 有人教学更好', 'Citywalk', '可开麦'],
    records: [
      { value: '12', label: '完成活动' },
      { value: '1', label: '提前取消' },
      { value: '0', label: '爽约' }
    ]
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 3 });
    }
  },

  editProfile() {
    wx.showToast({ title: '资料编辑将在账号接入后开放', icon: 'none' });
  },

  openSetting(event: WechatMiniprogram.TouchEvent) {
    wx.showToast({ title: `${event.currentTarget.dataset.name}功能已预留`, icon: 'none' });
  }
});
