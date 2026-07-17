import { mockActivities } from '../../mock/activities';

Page({
  data: { keyword: '', results: mockActivities, focused: true },

  setKeyword(event: WechatMiniprogram.Input) {
    const keyword = event.detail.value.trim().toLowerCase();
    this.setData({
      keyword: event.detail.value,
      results: keyword ? mockActivities.filter((item) =>
        `${item.title}${item.description}${item.tags.join('')}`.toLowerCase().includes(keyword)) : mockActivities
    });
  },

  goBack() { wx.navigateBack(); },
  clear() { this.setData({ keyword: '', results: mockActivities }); },
  openDetail(event: WechatMiniprogram.TouchEvent) { wx.navigateTo({ url: `/pages/detail/index?id=${event.currentTarget.dataset.id}` }); }
});
