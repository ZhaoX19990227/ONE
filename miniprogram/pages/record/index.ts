import { Dimension } from '../../models/types'
import { request, uploadImage } from '../../utils/request'
import { currentTheme, ThemeName } from '../../utils/theme'

interface VisionCandidate { categoryId?: number; categoryName?: string; brandId?: number; brandName?: string; brandLogoUrl?: string; itemId?: number; itemName: string; confidence: number; evidence?: string; catalogMatched: boolean }
interface Recognition { id: number; imageUrl: string; status: 'NEED_CONFIRMATION' | 'FAILED' | 'CONFIRMED'; candidates: VisionCandidate[]; fallbackMessage?: string }

Page({
  data: {
    theme: 'noon' as ThemeName, dimension: 'MEAL' as Dimension, sessionId: 0, source: 'MANUAL',
    pageTitle: '记下这一口',
    imageUrl: '', mediaAssetId: 0, recognizing: false, recognition: undefined as Recognition | undefined,
    candidates: [] as VisionCandidate[], selectedIndex: -1, categoryId: 0, brandId: 0, itemId: 0,
    customBrandName: '', customItemName: '', amountYuan: '', note: '', rating: 0,
    tasteFeedback: 'JUST_RIGHT', sugarLevel: 'UNKNOWN', iceLevel: 'UNKNOWN', repurchaseIntent: 'UNSURE',
    saving: false, toast: ''
  },
  onLoad(query: Record<string, string>) {
    const dimension = (query.dimension || 'MEAL') as Dimension
    this.setData({ theme: currentTheme(), dimension, sessionId: Number(query.sessionId || 0),
      source: query.sessionId ? 'RECOMMENDATION' : 'MANUAL', itemId: Number(query.itemId || 0),
      customItemName: decodeURIComponent(query.title || ''), customBrandName: decodeURIComponent(query.brand || ''),
      amountYuan: query.amount || '', pageTitle: query.copy === '1' ? '再记这一口' : '记下这一口' })
    if (query.photo === '1') setTimeout(() => this.choosePhoto(), 250)
  },
  goBack() { wx.navigateBack() },
  choosePhoto() {
    wx.chooseMedia({ count: 1, mediaType: ['image'], sourceType: ['camera', 'album'], sizeType: ['compressed'],
      success: async ({ tempFiles }) => {
        const file = tempFiles[0]
        this.setData({ imageUrl: file.tempFilePath, recognizing: true, candidates: [], selectedIndex: -1 })
        try {
          const media = await uploadImage(file.tempFilePath)
          this.setData({ mediaAssetId: media.id, imageUrl: media.url, source: 'AI_PHOTO' })
          const recognition = await request<Recognition>('/recognitions', 'POST', { mediaAssetId: media.id, dimension: this.data.dimension })
          this.setData({ recognition, candidates: recognition.candidates })
          if (recognition.candidates.length) this.applyCandidate(0)
          else this.showToast(recognition.fallbackMessage || '没认准，自己写下它也很好')
        } catch (error) { this.showToast((error as Error).message) }
        finally { this.setData({ recognizing: false }) }
      }
    })
  },
  selectCandidate(event: WechatMiniprogram.TouchEvent) { this.applyCandidate(Number(event.currentTarget.dataset.index)) },
  applyCandidate(index: number) {
    const candidate = this.data.candidates[index]
    if (!candidate) return
    this.setData({ selectedIndex: index, categoryId: candidate.categoryId || 0, brandId: candidate.brandId || 0,
      itemId: candidate.itemId || 0, customBrandName: candidate.brandName || '', customItemName: candidate.itemName })
    wx.vibrateShort({ type: 'light' })
  },
  inputBrand(event: WechatMiniprogram.Input) { this.setData({ customBrandName: event.detail.value, brandId: 0, itemId: 0, selectedIndex: -1 }) },
  inputItem(event: WechatMiniprogram.Input) { this.setData({ customItemName: event.detail.value, itemId: 0, selectedIndex: -1 }) },
  inputAmount(event: WechatMiniprogram.Input) { this.setData({ amountYuan: event.detail.value }) },
  inputNote(event: WechatMiniprogram.Input) { this.setData({ note: event.detail.value }) },
  selectRating(event: WechatMiniprogram.TouchEvent) { this.setData({ rating: Number(event.currentTarget.dataset.value) }) },
  selectFeedback(event: WechatMiniprogram.TouchEvent) { this.setData({ tasteFeedback: event.currentTarget.dataset.value }) },
  selectSugar(event: WechatMiniprogram.TouchEvent) { this.setData({ sugarLevel: event.currentTarget.dataset.value }) },
  selectIce(event: WechatMiniprogram.TouchEvent) { this.setData({ iceLevel: event.currentTarget.dataset.value }) },
  selectRepurchase(event: WechatMiniprogram.TouchEvent) { this.setData({ repurchaseIntent: event.currentTarget.dataset.value }) },
  async save() {
    if (this.data.saving) return
    if (!this.data.itemId && !this.data.customItemName.trim()) { this.showToast('告诉 ONE 这是什么'); return }
    const amount = this.data.amountYuan ? Number(this.data.amountYuan) : undefined
    if (amount !== undefined && (!Number.isFinite(amount) || amount < 0 || amount > 100000)) { this.showToast('金额好像不太对'); return }
    this.setData({ saving: true })
    const amountFen = amount === undefined ? undefined : Math.round(amount * 100)
    const common = { occurredAt: new Date().toISOString(), decisionSessionId: this.data.sessionId || undefined,
      categoryId: this.data.categoryId || undefined, brandId: this.data.brandId || undefined,
      itemId: this.data.itemId || undefined, customBrandName: this.data.itemId ? undefined : this.data.customBrandName,
      customItemName: this.data.itemId ? undefined : this.data.customItemName, thumbnailUrl: this.data.imageUrl || undefined,
      money: amountFen === undefined ? undefined : { actualAmountFen: amountFen }, rating: this.data.rating || undefined,
      note: this.data.note.trim() || undefined,
      source: this.data.source, tasteFeedback: this.data.tasteFeedback, repurchaseIntent: this.data.repurchaseIntent }
    try {
      if (this.data.recognition && this.data.recognition.status !== 'CONFIRMED') {
        await request(`/recognitions/${this.data.recognition.id}/confirm`, 'POST', {
          categoryId: this.data.categoryId || undefined, brandId: this.data.brandId || undefined,
          itemId: this.data.itemId || undefined,
          customBrandName: this.data.itemId ? undefined : this.data.customBrandName,
          customItemName: this.data.itemId ? undefined : this.data.customItemName
        })
      }
      const path = this.data.dimension === 'MEAL' ? '/records/meals' : '/records/drinks'
      const payload = this.data.dimension === 'MEAL' ? common : { ...common, dimension: this.data.dimension,
        sugarLevel: this.data.sugarLevel, iceLevel: this.data.iceLevel, toppings: [] }
      await request(path, 'POST', payload)
      wx.vibrateShort({ type: 'medium' }); this.setData({ toast: '这一口，已经被 ONE 记住 ✨' })
      setTimeout(() => wx.navigateBack(), 1100)
    } catch (error) { this.showToast((error as Error).message) }
    finally { this.setData({ saving: false }) }
  },
  showToast(message: string) { this.setData({ toast: message }); setTimeout(() => this.setData({ toast: '' }), 2300) }
})
