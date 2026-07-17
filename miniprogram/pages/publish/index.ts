import { publishActivity } from '../../services/activity';

Page({
  data: {
    typeOptions: ['羽毛球', '桌游', '游戏开黑', 'Citywalk', '咖啡闲聊'],
    typeValues: ['BADMINTON', 'BOARD_GAME', 'GAMING', 'CITY_WALK', 'COFFEE'],
    typeIndex: 0,
    mode: 'OFFLINE',
    title: '',
    description: '',
    date: '',
    minDate: '',
    time: '20:00',
    district: '',
    address: '',
    capacity: 6,
    depositYuan: 20,
    approvalRequired: false,
    publishing: false
  },

  onLoad() {
    const now = new Date();
    const tomorrow = new Date(now.getFullYear(), now.getMonth(), now.getDate() + 1);
    this.setData({ date: this.formatDate(tomorrow), minDate: this.formatDate(now) });
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 1 });
    }
  },

  setField(event: WechatMiniprogram.Input) {
    const field = event.currentTarget.dataset.field;
    this.setData({ [field]: event.detail.value });
  },

  selectType(event: WechatMiniprogram.PickerChange) {
    this.setData({ typeIndex: Number(event.detail.value) });
    wx.vibrateShort({ type: 'light' });
  },

  selectDate(event: WechatMiniprogram.PickerChange) {
    this.setData({ date: String(event.detail.value) });
  },

  selectTime(event: WechatMiniprogram.PickerChange) {
    this.setData({ time: String(event.detail.value) });
  },

  selectCapacity(event: WechatMiniprogram.SliderChange) {
    this.setData({ capacity: event.detail.value });
    wx.vibrateShort({ type: 'light' });
  },

  moveDeposit(event: WechatMiniprogram.SliderChanging) {
    this.setData({ depositYuan: event.detail.value });
    if (event.detail.value % 10 === 0) wx.vibrateShort({ type: 'light' });
  },

  setMode(event: WechatMiniprogram.TouchEvent) {
    this.setData({ mode: event.currentTarget.dataset.mode });
    wx.vibrateShort({ type: 'light' });
  },

  toggleApproval(event: WechatMiniprogram.SwitchChange) {
    this.setData({ approvalRequired: event.detail.value });
  },

  async publish() {
    if (!this.data.title.trim() || !this.data.description.trim()) {
      wx.showToast({ title: '再多说一点这场活动吧', icon: 'none' });
      return;
    }
    if (this.data.mode === 'OFFLINE' && !this.data.district.trim()) {
      wx.showToast({ title: '请选择活动所在区域', icon: 'none' });
      return;
    }
    this.setData({ publishing: true });
    try {
      const startAt = new Date(`${this.data.date}T${this.data.time}:00+08:00`);
      await publishActivity({
        type: this.data.typeValues[this.data.typeIndex],
        mode: this.data.mode,
        title: this.data.title.trim(),
        description: this.data.description.trim(),
        cityCode: this.data.mode === 'OFFLINE' ? '310100' : undefined,
        cityName: this.data.mode === 'OFFLINE' ? '上海' : '线上',
        district: this.data.district,
        address: this.data.address,
        startAt: startAt.toISOString(),
        endAt: new Date(startAt.getTime() + 2 * 60 * 60 * 1000).toISOString(),
        enrollDeadline: new Date(startAt.getTime() - 60 * 60 * 1000).toISOString(),
        capacity: this.data.capacity,
        feeFen: 0,
        depositFen: this.data.depositYuan * 100,
        approvalRequired: this.data.approvalRequired,
        tags: ['新活动'],
        attributes: {}
      });
      wx.vibrateShort({ type: 'medium' });
      wx.showToast({ title: '信号已发出', icon: 'success' });
      setTimeout(() => wx.switchTab({ url: '/pages/trips/index' }), 800);
    } catch (error) {
      wx.showToast({ title: '发布失败，请检查时间和网络', icon: 'none' });
    } finally {
      this.setData({ publishing: false });
    }
  },

  formatDate(date: Date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
});
