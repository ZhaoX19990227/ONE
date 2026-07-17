import { Activity } from '../../models/activity';
import { getActivity, joinActivity } from '../../services/activity';

Page({
  data: {
    activity: null as Activity | null,
    feeYuan: '0',
    joining: false,
    loaded: false
  },

  onLoad(query: Record<string, string>) {
    this.loadActivity(Number(query.id || 1));
  },

  async loadActivity(id: number) {
    try {
      const activity = await getActivity(id);
      this.setData({ activity, feeYuan: (activity.depositFen / 100).toFixed(0), loaded: true });
    } catch (error) {
      wx.showToast({ title: '这场活动已经离开了', icon: 'none' });
      setTimeout(() => wx.navigateBack(), 1000);
    }
  },

  goBack() { wx.navigateBack(); },

  share() {
    wx.showShareMenu({ menus: ['shareAppMessage', 'shareTimeline'] });
    wx.showToast({ title: '点击右上角分享给同频的人', icon: 'none' });
  },

  async join() {
    const activity = this.data.activity;
    if (!activity || this.data.joining || activity.isJoined) return;
    this.setData({ joining: true });
    try {
      await joinActivity(activity.id);
      this.setData({
        'activity.isJoined': true,
        'activity.joinedCount': activity.joinedCount + 1,
        joining: false
      });
      wx.vibrateShort({ type: 'medium' });
      wx.showToast({ title: '已加入这场相遇', icon: 'success' });
    } catch (error) {
      this.setData({ joining: false });
      wx.showToast({ title: '报名没有成功，请重试', icon: 'none' });
    }
  },

  checkIn() {
    wx.navigateTo({ url: `/pages/checkin/index?id=${this.data.activity?.id || 1}` });
  },

  onShareAppMessage() {
    return { title: this.data.activity?.title || '来 ONE 一起做件有趣的事', path: `/pages/detail/index?id=${this.data.activity?.id || 1}` };
  }
});
