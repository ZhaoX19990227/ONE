type Petal = { x: number; y: number; vx: number; vy: number; size: number; color: string; rotation: number; vr: number };

Page({
  data: { success: false },
  petals: [] as Petal[],
  timer: 0 as unknown as number,

  onReady() {
    setTimeout(() => this.startCelebration(), 350);
  },

  onUnload() {
    if (this.timer) clearTimeout(this.timer);
  },

  startCelebration() {
    const colors = ['rgba(248,164,168,.72)', 'rgba(127,194,201,.68)', 'rgba(228,199,200,.78)', 'rgba(255,255,255,.86)'];
    this.petals = Array.from({ length: 34 }, (_, index) => ({
      x: 110 + Math.random() * 530,
      y: -80 - Math.random() * 280,
      vx: -1.5 + Math.random() * 3,
      vy: 2 + Math.random() * 3.5,
      size: 7 + Math.random() * 13,
      color: colors[index % colors.length],
      rotation: Math.random() * Math.PI,
      vr: -.08 + Math.random() * .16
    }));
    wx.vibrateShort({ type: 'heavy' });
    this.setData({ success: true });
    this.animate();
  },

  animate() {
    const context = wx.createCanvasContext('petalCanvas', this);
    this.petals.forEach((petal) => {
      petal.x += petal.vx;
      petal.y += petal.vy;
      petal.rotation += petal.vr;
      context.save();
      context.translate(petal.x, petal.y);
      context.rotate(petal.rotation);
      context.setFillStyle(petal.color);
      const width = petal.size * 0.55;
      const height = petal.size;
      context.beginPath();
      context.moveTo(0, -height);
      context.bezierCurveTo(width, -height * 0.45, width, height * 0.45, 0, height);
      context.bezierCurveTo(-width, height * 0.45, -width, -height * 0.45, 0, -height);
      context.fill();
      context.restore();
    });
    context.draw();
    if (this.petals.some((petal) => petal.y < 1000)) {
      this.timer = setTimeout(() => this.animate(), 32) as unknown as number;
    }
  },

  finish() {
    wx.switchTab({ url: '/pages/trips/index' });
  }
});
