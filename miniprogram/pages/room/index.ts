import { GroupRoom } from '../../models/types'
import { request } from '../../utils/request'
import { currentTheme, ThemeName } from '../../utils/theme'
import { oneDialog } from '../../utils/overlay'

interface RoomData {
  theme: ThemeName
  code: string
  room?: GroupRoom
  loading: boolean
  toast: string
}

Page<RoomData, WechatMiniprogram.Page.CustomOption>({
  data: { theme: 'noon', code: '', room: undefined, loading: true, toast: '' },
  poller: 0 as unknown as number,
  onLoad(options: Record<string, string>) {
    this.setData({ code: (options.code || '').toUpperCase(), theme: currentTheme() })
    wx.showShareMenu({ menus: ['shareAppMessage'] })
    this.loadRoom()
  },
  onShow() {
    this.poller = setInterval(() => this.loadRoom(true), 5000) as unknown as number
  },
  onHide() { clearInterval(this.poller) },
  onUnload() { clearInterval(this.poller) },
  onPullDownRefresh() { this.loadRoom().finally(() => wx.stopPullDownRefresh()) },
  navigateBack() { wx.navigateBack({ fail: () => wx.switchTab({ url: '/pages/home/index' }) }) },
  async loadRoom(silent = false) {
    if (!this.data.code) return
    if (!silent) this.setData({ loading: true })
    try { this.setData({ room: await request<GroupRoom>(`/rooms/${this.data.code}`) }) }
    catch (error) { if (!silent) this.showToast((error as Error).message) }
    finally { if (!silent) this.setData({ loading: false }) }
  },
  async vote(event: WechatMiniprogram.TouchEvent) {
    if (this.data.room?.status !== 'OPEN') return
    const candidateId = Number(event.currentTarget.dataset.id)
    wx.vibrateShort({ type: 'light' })
    try {
      const room = await request<GroupRoom>(`/rooms/${this.data.code}/vote`, 'POST', { candidateId })
      this.setData({ room })
    } catch (error) { this.showToast((error as Error).message) }
  },
  async closeRoom() {
    const confirmed = await oneDialog(this, { title: '现在揭晓吗？', content: '会以当前票数选出大家的 ONE，结束后不能再投票。', confirmText: '揭晓' })
    if (!confirmed) return
    try {
      const room = await request<GroupRoom>(`/rooms/${this.data.code}/close`, 'POST')
      this.setData({ room })
      wx.vibrateShort({ type: 'medium' })
      this.showToast('答案出现了 ✨')
    } catch (error) { this.showToast((error as Error).message) }
  },
  shareRoom() { wx.showShareMenu({ menus: ['shareAppMessage'] }); this.showToast('点右上角，发到群里一起选') },
  onShareAppMessage() {
    return { title: this.data.room?.title || '来选出今晚的 ONE', path: `/pages/room/index?code=${this.data.code}` }
  },
  showToast(message: string) { this.setData({ toast: message }); setTimeout(() => this.setData({ toast: '' }), 2200) }
})
