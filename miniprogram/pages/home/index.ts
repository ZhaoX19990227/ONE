import { currentTheme, greeting, ThemeName } from '../../utils/theme'
import { request } from '../../utils/request'
import { Candidate, Dimension, Recommendation, RecordView } from '../../models/types'
import { ProfileView } from '../../models/types'
import { showRecordActions } from '../../utils/record-actions'

interface HomeData {
  theme: ThemeName; greeting: string; dimension: Dimension; records: RecordView[];
  loading: boolean; recommendation?: Recommendation; selected?: Candidate; spinning: boolean;
  wheelStyle: string; wheelBackground: string; wheelSegments: Array<{ label: string; style: string }>;
  toast: string; deerBurst: boolean; todayAmountFen: number;
  memoryEnabled: boolean; privateHabitEnabled: boolean
}

Page<HomeData, WechatMiniprogram.Page.CustomOption>({
  data: { theme: 'noon', greeting: '', dimension: 'MEAL', records: [], loading: false,
    recommendation: undefined, selected: undefined, spinning: false, wheelStyle: 'transform:rotate(0deg)',
    wheelBackground: '', wheelSegments: [], toast: '', deerBurst: false,
    todayAmountFen: 0, memoryEnabled: true, privateHabitEnabled: true },
  onShow() {
    const theme = currentTheme()
    this.setData({ theme, greeting: greeting(theme) })
    const tab = this.getTabBar?.()
    if (tab) tab.setData({ selected: 0 })
    Promise.all([this.loadToday(), this.loadPreferences()])
  },
  onPullDownRefresh() { Promise.all([this.loadToday(), this.loadPreferences()]).finally(() => wx.stopPullDownRefresh()) },
  async loadToday() {
    try {
      const values = await request<RecordView[]>('/records/today')
      const records = this.data.privateHabitEnabled ? values : values.filter(value => value.type !== 'PRIVATE_HABIT')
      this.setData({ records, todayAmountFen: records.reduce((sum, value) => sum + (value.actualAmountFen || 0), 0) })
    } catch (_) { /* 首页保留空态 */ }
  },
  async loadPreferences() {
    try {
      const profile = await request<ProfileView>('/me')
      const update: Partial<HomeData> = { memoryEnabled: profile.aiEnabled, privateHabitEnabled: profile.privateHabitEnabled }
      if (!profile.privateHabitEnabled && this.data.dimension === 'PRIVATE_HABIT') update.dimension = 'MEAL'
      if (!profile.privateHabitEnabled) {
        update.records = this.data.records.filter(value => value.type !== 'PRIVATE_HABIT')
        update.todayAmountFen = update.records.reduce((sum, value) => sum + (value.actualAmountFen || 0), 0)
      }
      this.setData(update)
    } catch (_) { /* 推荐仍可使用默认设置 */ }
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
      this.presentRecommendation(result, mode === 'SPIN')
    } catch (error) { this.showToast((error as Error).message) }
    finally { this.setData({ loading: false }) }
  },
  startSpin(result: Recommendation) {
    if (!result.candidates.length) return
    const winnerIndex = Math.max(0, result.candidates.findIndex(value => value.id === result.winnerCandidateId))
    const slice = 360 / result.candidates.length
    const targetCenter = winnerIndex * slice + slice / 2
    const degrees = 1800 + (360 - targetCenter)
    this.setData({ spinning: true, selected: undefined, wheelStyle: 'transform:rotate(0deg)' })
    setTimeout(() => this.setData({ wheelStyle: `transform:rotate(${degrees}deg)` }), 40)
    setTimeout(() => {
      wx.vibrateShort({ type: 'light' })
      this.chooseCandidate(result.candidates[winnerIndex])
      this.setData({ spinning: false })
    }, 2880)
  },
  presentRecommendation(result: Recommendation, autoSpin = false) {
    const colors = ['#F8A4A8', '#F3D0CE', '#7FC2C9', '#D6E8E6', '#E8B9BE', '#F0DED5', '#96C9CD', '#F7C4B9']
    const slice = 360 / Math.max(1, result.candidates.length)
    const wheelBackground = `conic-gradient(${result.candidates.map((_, index) =>
      `${colors[index % colors.length]} ${index * slice}deg ${(index + 1) * slice}deg`).join(',')})`
    const wheelSegments = result.candidates.map((candidate, index) => ({
      label: candidate.brandShortName || candidate.itemName.slice(0, 4),
      style: `transform:rotate(${index * slice + slice / 2}deg) translateY(-112rpx) rotate(${-index * slice - slice / 2}deg)`
    }))
    this.setData({ recommendation: result, selected: undefined, wheelBackground, wheelSegments,
      wheelStyle: 'transform:rotate(0deg)' })
    if (autoSpin) this.startSpin(result)
  },
  async refreshRecommendation() {
    const session = this.data.recommendation
    if (!session || this.data.spinning) return
    this.setData({ loading: true })
    try {
      const result = await request<Recommendation>(`/recommendations/${session.sessionId}/refresh`, 'POST')
      this.presentRecommendation(result, result.mode === 'SPIN')
    } catch (error) { this.showToast((error as Error).message) }
    finally { this.setData({ loading: false }) }
  },
  dismissCandidate(event: WechatMiniprogram.TouchEvent) {
    if (this.data.spinning) return
    const candidate = this.data.recommendation?.candidates[Number(event.currentTarget.dataset.index)]
    if (!candidate) return
    const reasons = this.data.dimension === 'MEAL' ? ['今天不想吃这个', '最近吃过了', '这次有点贵']
      : ['今天不想喝这个', '最近喝过了', '这次有点贵']
    wx.showActionSheet({ itemList: reasons, success: async ({ tapIndex }) => {
      const session = this.data.recommendation
      if (!session) return
      this.setData({ loading: true })
      try {
        const result = await request<Recommendation>(`/recommendations/${session.sessionId}/candidates/${candidate.id}/dismiss`, 'POST', { reason: reasons[tapIndex] })
        this.presentRecommendation(result, result.mode === 'SPIN')
        this.showToast('收到，这两周会少出现一点')
      } catch (error) { this.showToast((error as Error).message) }
      finally { this.setData({ loading: false }) }
    } })
  },
  async createRoom() {
    const recommendation = this.data.recommendation
    if (!recommendation || recommendation.candidates.length < 2 || this.data.spinning) return
    this.setData({ loading: true })
    try {
      const room = await request<{ shareCode: string }>('/rooms', 'POST', {
        title: recommendation.dimension === 'MEAL' ? '今晚吃什么？' : '今天喝什么？',
        dimension: recommendation.dimension,
        itemIds: recommendation.candidates.map(value => value.itemId)
      })
      this.closeRecommendation()
      wx.navigateTo({ url: `/pages/room/index?code=${room.shareCode}` })
    } catch (error) { this.showToast((error as Error).message) }
    finally { this.setData({ loading: false }) }
  },
  tapCandidate(event: WechatMiniprogram.TouchEvent) {
    if (this.data.spinning) return
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
    this.closeRecommendation()
    wx.navigateTo({ url: `/pages/record/index?${query}` })
  },
  photoRecord() {
    if (this.data.dimension === 'PRIVATE_HABIT') { this.recordDeer(); return }
    this.closeRecommendation()
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
  closeRecommendation() { this.setData({ recommendation: undefined, selected: undefined, wheelStyle: 'transform:rotate(0deg)', wheelSegments: [] }) },
  recordActions(event: WechatMiniprogram.TouchEvent) {
    const record = this.data.records[Number(event.currentTarget.dataset.index)]
    if (record) showRecordActions(record, () => this.loadToday())
  },
  showToast(message: string) { this.setData({ toast: message }); setTimeout(() => this.setData({ toast: '' }), 2200) }
})
