Component({
  data: { selected: 0, list: [
    { pagePath: '/pages/home/index', text: '转一转', symbol: '↻' },
    { pagePath: '/pages/calendar/index', text: '日历', symbol: '□' },
    { pagePath: '/pages/profile/index', text: '我的', symbol: '○' }
  ] },
  methods: { switchTab(event: WechatMiniprogram.TouchEvent) {
    const index = Number(event.currentTarget.dataset.index)
    wx.switchTab({ url: this.data.list[index].pagePath })
  } }
})
