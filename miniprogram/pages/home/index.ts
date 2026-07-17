import { listActivities } from '../../services/activity';
import { Activity } from '../../models/activity';

type ActivityCard = Activity & { feeYuan: string; progressText: string };

Page({
  data: {
    city: '上海',
    activeFilter: '推荐',
    filters: ['推荐', '今晚', '周末', '线上', '离我近'],
    activities: [] as ActivityCard[],
    loading: true,
    citySheetVisible: false,
    cities: ['上海', '杭州', '北京', '成都']
  },

  onLoad() {
    this.setData({ city: getApp<IAppOption>().globalData.selectedCity });
    this.loadActivities();
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 0 });
    }
  },

  async onPullDownRefresh() {
    await this.loadActivities();
    wx.stopPullDownRefresh();
  },

  async loadActivities() {
    try {
      const activities = await listActivities();
      this.setData({
        activities: activities.map((item) => ({
          ...item,
          feeYuan: (item.depositFen / 100).toFixed(0),
          progressText: `${item.joinedCount}/${item.capacity}`
        })),
        loading: false
      });
    } catch (error) {
      this.setData({ loading: false });
      wx.showToast({ title: '暂时没有接住信号', icon: 'none' });
    }
  },

  selectFilter(event: WechatMiniprogram.TouchEvent) {
    this.setData({ activeFilter: event.currentTarget.dataset.filter });
    wx.vibrateShort({ type: 'light' });
  },

  openDetail(event: WechatMiniprogram.TouchEvent) {
    wx.navigateTo({ url: `/pages/detail/index?id=${event.currentTarget.dataset.id}` });
  },

  openCitySheet() {
    this.setData({ citySheetVisible: true });
  },

  closeCitySheet() {
    this.setData({ citySheetVisible: false });
  },

  selectCity(event: WechatMiniprogram.TouchEvent) {
    const city = event.currentTarget.dataset.city;
    getApp<IAppOption>().globalData.selectedCity = city;
    wx.setStorageSync('one_city', city);
    this.setData({ city, citySheetVisible: false });
  },

  openSearch() {
    wx.navigateTo({ url: '/pages/search/index' });
  },

  openMessages() {
    wx.showToast({ title: '你暂时没有未读消息', icon: 'none' });
  }
});
