import { mockActivities } from '../../mock/activities';

Page({
  data: {
    activeTab: 'upcoming',
    activity: mockActivities[0]
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 2 });
    }
  },

  setTab(event: WechatMiniprogram.TouchEvent) {
    this.setData({ activeTab: event.currentTarget.dataset.tab });
  },

  openDetail() {
    wx.navigateTo({ url: `/pages/detail/index?id=${this.data.activity.id}` });
  },

  checkIn() {
    wx.navigateTo({ url: `/pages/checkin/index?id=${this.data.activity.id}` });
  }
});
