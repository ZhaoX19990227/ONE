import { currentTheme, greeting, ThemeName } from '../../utils/theme'
import { request } from '../../utils/request'
import { Candidate, Dimension, Recommendation, RecordView } from '../../models/types'

interface HomeData {
  theme: ThemeName; greeting: string; dimension: Dimension; records: RecordView[];
  loading: boolean; recommendation?: Recommendation; selected?: Candidate; spinning: boolean;
  wheelStyle: string; toast: string; deerBurst: boolean
}

Page<HomeData, WechatMiniprogram.Page.CustomOption>({
  data: { theme: 'noon', greeting: '', dimension: 'MEAL', records: [], loading: false,
    recommendation: undefined, selected: undefined, spinning: false, wheelStyle: '', toast: '', deerBurst: false },
  onShow() {
    const theme = currentTheme()
    this.setData({ theme, greeting: greeting(theme) })
    const tab = this.getTabBar?.()
    if (tab) tab.setData({ selected: 0 })
    this.loadToday()
  },
  onPullDownRefresh() { this.loadToday().finally(() => wx.stopPullDownRefresh()) },
  async loadToday() {
    try { this.setData({ records: await request<RecordView[]>('/records/today') }) } catch (_) { /* 首页保留空态 */ }
  },
  selectDimension(event: WechatMiniprogram.TouchEvent) {
    this.setData({ dimension: event.currentTarget.dataset.value as Dimension, recommendation: undefined, selected: undefined })
  },
  selectDrink(event: WechatMiniprogram.TouchEvent) {
    this.setData({ dimension: event.currentTarget.dataset.value as Dimension })
  },
  async recommend(event: WechatMiniprogram.TouchEvent) {
    const mode = event.currentTarget.dataset.mode as 'SPIN' | 'SMART'
    if (this.data.dimension === 'PRIVATE_HABIT') { await this.recordDeer(); return }
    this.setData({ loading: true, recommendation: undefined, selected: undefined })
    try {
      const result = await request<Recommendation>('/recommendations', 'POST', { dimension: this.data.dimension, mode })
      this.setData({ recommendation: result })
      if (mode === 'SPIN') this.startSpin(result)
    } catch (error) { this.showToast((error as Error).message) }
    finally { this.setData({ loading: false }) }
  },
  startSpin(result: Recommendation) {
    if (!result.candidates.length) return
    const winnerIndex = Math.floor(Math.random() * result.candidates.length)
    const degrees = 1440 + (360 - winnerIndex * (360 / result.candidates.length))
    this.setData({ spinning: true, wheelStyle: `transform:rotate(${degrees}deg)` })
    setTimeout(() => {
      wx.vibrateShort({ type: 'light' })
      this.chooseCandidate(result.candidates[winnerIndex])
      this.setData({ spinning: false })
    }, 1600)
  },
  tapCandidate(event: WechatMiniprogram.TouchEvent) {
    const candidate = this.data.recommendation?.candidates[Number(event.currentTarget.dataset.index)]
    if (candidate) this.chooseCandidate(candidate)
  },
  async chooseCandidate(candidate: Candidate) {
    const session = this.data.recommendation
    if (!session) return
    try {
      const result = await request<Recommendation>(`/recommendations/${session.sessionId}/candidates/${candidate.id}/choose`, 'POST')
      this.setData({ recommendation: result, selected: result.candidates.find(value => value.id === candidate.id) })
    } catch (error) { this.showToast((error as Error).message) }
  },
  recordSelected() {
    const session = this.data.recommendation; const candidate = this.data.selected
    if (!session || !candidate) return
    const query = [`dimension=${session.dimension}`, `sessionId=${session.sessionId}`, `itemId=${candidate.itemId}`,
      `title=${encodeURIComponent(candidate.itemName)}`, `brand=${encodeURIComponent(candidate.brandName || '')}`].join('&')
    wx.navigateTo({ url: `/pages/record/index?${query}` })
  },
  photoRecord() {
    if (this.data.dimension === 'PRIVATE_HABIT') { this.recordDeer(); return }
    wx.navigateTo({ url: `/pages/record/index?dimension=${this.data.dimension}&photo=1` })
  },
  async recordDeer() {
    this.setData({ loading: true })
    try {
      const result = await request<RecordView>('/records/deer', 'POST', { occurredAt: new Date().toISOString(), bodyFeeling: 'NOT_RECORDED' })
      wx.vibrateShort({ type: 'light' })
      this.setData({ deerBurst: true, toast: result.deerMessage || '今日同频成功 ✨' })
      setTimeout(() => this.setData({ deerBurst: false, toast: '' }), 2300)
      await this.loadToday()
    } catch (error) { this.showToast((error as Error).message) }
    finally { this.setData({ loading: false }) }
  },
  closeRecommendation() { this.setData({ recommendation: undefined, selected: undefined, wheelStyle: '' }) },
  showToast(message: string) { this.setData({ toast: message }); setTimeout(() => this.setData({ toast: '' }), 2200) }
})
