Component({
  data: {
    selected: 0,
    items: [
      { pagePath: '/pages/home/index', text: '发现', icon: '⌂' },
      { pagePath: '/pages/publish/index', text: '发起', icon: '＋' },
      { pagePath: '/pages/trips/index', text: '行程', icon: '○' },
      { pagePath: '/pages/profile/index', text: '我的', icon: '◇' }
    ]
  },

  methods: {
    switchTab(event: WechatMiniprogram.TouchEvent) {
      const { path, index } = event.currentTarget.dataset;
      if (index === this.data.selected) return;
      wx.switchTab({ url: path });
    }
  }
});
