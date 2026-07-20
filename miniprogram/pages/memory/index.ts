import { Dimension, PreferenceMemory } from '../../models/types'
import { request } from '../../utils/request'
import { currentTheme, ThemeName } from '../../utils/theme'
import { oneDialog } from '../../utils/overlay'

interface MemoryView extends PreferenceMemory { displayDate: string; suggestionText?: string }

Page({
  data: {
    theme: 'noon' as ThemeName,
    dimension: '' as '' | Dimension,
    filters: [
      { value: '', label: '全部' }, { value: 'MEAL', label: '吃饭' },
      { value: 'MILK_TEA', label: '奶茶' }, { value: 'COFFEE', label: '咖啡' }
    ],
    memories: [] as MemoryView[], loading: false, toast: ''
  },
  onLoad() { this.setData({ theme: currentTheme() }); this.load() },
  goBack() { wx.navigateBack() },
  selectFilter(event: WechatMiniprogram.TouchEvent) {
    this.setData({ dimension: event.currentTarget.dataset.value as '' | Dimension })
    this.load()
  },
  async load() {
    this.setData({ loading: true })
    try {
      const query = this.data.dimension ? `?dimension=${this.data.dimension}` : ''
      const values = await request<PreferenceMemory[]>(`/memories${query}`)
      this.setData({ memories: values.map(value => ({ ...value,
        displayDate: this.formatDate(value.sourceAt), suggestionText: this.suggestion(value.suggestedValue) })) })
    } catch (error) { this.showToast((error as Error).message) }
    finally { this.setData({ loading: false }) }
  },
  async forget(event: WechatMiniprogram.TouchEvent) {
    const id = Number(event.currentTarget.dataset.id)
    const confirmed = await oneDialog(this, { title: '让 ONE 忘记它？', content: '忘记后，下一次推荐不会再参考这条感受。', confirmText: '忘记', danger: true })
    if (!confirmed) return
    try {
      await request<void>(`/memories/${id}`, 'DELETE')
      wx.vibrateShort({ type: 'light' })
      this.setData({ memories: this.data.memories.filter(value => value.id !== id) })
      this.showToast('这条记忆已经忘记')
    } catch (error) { this.showToast((error as Error).message) }
  },
  formatDate(value: string) {
    const date = new Date(value)
    return `${date.getMonth() + 1}月${date.getDate()}日`
  },
  suggestion(value?: string) {
    if (!value) return undefined
    const labels: Record<string, string> = { NO_SUGAR: '下次建议无糖', LOW: '下次建议少少甜', THREE: '下次建议三分糖', FIVE: '下次建议五分糖' }
    return labels[value] || `下次建议 ${value}`
  },
  showToast(message: string) { this.setData({ toast: message }); setTimeout(() => this.setData({ toast: '' }), 2200) }
})
