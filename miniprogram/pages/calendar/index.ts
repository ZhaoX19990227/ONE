import { currentTheme, ThemeName } from '../../utils/theme'
import { request } from '../../utils/request'
import { ProfileView, RecordView } from '../../models/types'
import { showRecordActions } from '../../utils/record-actions'

interface DayCell { date: string; day?: number; empty?: boolean; totalCount: number; mealCount: number; milkTeaCount: number; coffeeCount: number; privateHabitCount: number; amountFen: number; coverUrl?: string; moodText?: string }
interface MonthView { month: string; activeDays: number; totalCount: number; totalAmountFen: number; days: DayCell[] }
interface Summary { activeDays: number; totalCount: number; totalAmountFen: number; insight: string; dimensions: Array<{dimension:string;count:number;amountFen:number}>; topBrands: Array<{brandName:string;dimension:string;count:number;amountFen:number;logoUrl?:string;color?:string}> }
interface Weekly { from:string;to:string;activeDays:number;totalCount:number;totalAmountFen:number;currentRhythmDays:number;longestRhythmDays:number;dimensions:Array<{dimension:string;count:number;amountFen:number}>;gentleMessage:string;easterEgg?:string }

Page({
  data: { theme: 'noon' as ThemeName, year: 0, month: 0, cells: [] as DayCell[], activeDays: 0,
    totalAmountFen: 0, selectedDate: '', dayRecords: [] as RecordView[], summary: undefined as Summary | undefined,
    dimensionCards: [] as Array<{dimension:string;label:string;count:number;amountFen:number}>,
    topBrands: [] as Array<Summary['topBrands'][number] & {shortName:string}>, monthlyBudgetFen: 0, budgetPercent: 0, budgetText: '',
    weekly: undefined as Weekly | undefined, posterPath: '', posterVisible: false, posterTitle: '', loading: false },
  onShow() {
    const tab = this.getTabBar?.(); if (tab) tab.setData({ selected: 1 })
    this.setData({ theme: currentTheme() })
    if (!this.data.year) { const now = new Date(); this.setData({ year: now.getFullYear(), month: now.getMonth() + 1 }) }
    this.loadMonth()
  },
  previousMonth() { this.shiftMonth(-1) },
  nextMonth() { this.shiftMonth(1) },
  shiftMonth(delta: number) {
    const date = new Date(this.data.year, this.data.month - 1 + delta, 1)
    this.setData({ year: date.getFullYear(), month: date.getMonth() + 1, selectedDate: '', dayRecords: [] })
    this.loadMonth()
  },
  async loadMonth() {
    this.setData({ loading: true })
    const monthText = `${this.data.year}-${String(this.data.month).padStart(2, '0')}`
    const from = `${monthText}-01`; const last = new Date(this.data.year, this.data.month, 0).getDate()
    const to = `${monthText}-${String(last).padStart(2, '0')}`
    try {
      const [calendar, summary, profile, weekly] = await Promise.all([
        request<MonthView>(`/calendar/month?month=${monthText}`),
        request<Summary>(`/analytics/summary?from=${from}&to=${to}`),
        request<ProfileView>('/me'),
        request<Weekly>('/analytics/weekly')
      ])
      const weekday = new Date(this.data.year, this.data.month - 1, 1).getDay()
      const placeholders: DayCell[] = Array.from({ length: weekday }, () => ({ date: '', empty: true, totalCount: 0, mealCount: 0, milkTeaCount: 0, coffeeCount: 0, privateHabitCount: 0, amountFen: 0 }))
      const labels: Record<string, string> = { MEAL: '好好吃饭', MILK_TEA: '奶茶茶饮', COFFEE: '咖啡时间' }
      const dimensions = summary.dimensions.filter(value => value.dimension !== 'PRIVATE_HABIT')
        .map(value => ({ ...value, label: labels[value.dimension] || value.dimension }))
      const budget = profile.monthlyBudgetFen || 0
      const budgetPercent = budget ? Math.min(100, Math.round(calendar.totalAmountFen / budget * 100)) : 0
      const remaining = budget - calendar.totalAmountFen
      const budgetText = !budget ? '在「我的」设置预算后，这里会出现温柔提醒' : remaining >= 0
        ? `还留有 ¥${(remaining / 100).toFixed(0)} 的自在空间` : `比参考预算多 ¥${(-remaining / 100).toFixed(0)}，只是提醒，不评判`
      this.setData({ cells: placeholders.concat(calendar.days.map((day, index) => ({ ...day, day: index + 1 }))),
        activeDays: calendar.activeDays, totalAmountFen: calendar.totalAmountFen, summary,
        dimensionCards: dimensions, topBrands: summary.topBrands.slice(0, 5).map(value => ({ ...value, shortName: value.brandName.substring(0, 1) })), monthlyBudgetFen: budget, budgetPercent, budgetText, weekly })
    } catch (error) { wx.showToast({ title: (error as Error).message, icon: 'none' }) }
    finally { this.setData({ loading: false }) }
  },
  async selectDay(event: WechatMiniprogram.TouchEvent) {
    const date = event.currentTarget.dataset.date as string
    if (!date) return
    this.setData({ selectedDate: date })
    try { this.setData({ dayRecords: await request<RecordView[]>(`/records?date=${date}`) }) } catch (_) { this.setData({ dayRecords: [] }) }
  },
  generatePoster() {
    const summary = this.data.summary; if (!summary) return
    const context = wx.createCanvasContext('poster', this)
    const width = 620; const height = 900
    context.setFillStyle('#FCF9F8'); context.fillRect(0, 0, width, height)
    const gradient = context.createLinearGradient(0, 0, width, height); gradient.addColorStop(0, '#F7D8D8'); gradient.addColorStop(1, '#DDEDEE')
    context.setFillStyle(gradient); context.fillRect(0, 0, width, 250)
    context.setFillStyle('#2D2A26'); context.setFontSize(42); context.fillText('ONE', 48, 82)
    context.setFontSize(22); context.fillText(`${this.data.year}.${String(this.data.month).padStart(2, '0')} 生活切片`, 48, 125)
    context.setFontSize(50); context.fillText(`${summary.activeDays}`, 48, 215)
    context.setFontSize(19); context.setFillStyle('#7B716F'); context.fillText('有记录的日子', 120, 210)
    context.setFillStyle('#FFFFFF'); this.roundRect(context, 34, 282, 552, 470, 28); context.fill()
    context.setFillStyle('#2D2A26'); context.setFontSize(27); context.fillText('本月吃喝', 68, 340)
    const shareable = summary.dimensions.filter(value => value.dimension !== 'PRIVATE_HABIT')
    shareable.forEach((value, index) => {
      const labels: Record<string,string> = { MEAL: '好好吃饭', MILK_TEA: '奶茶茶饮', COFFEE: '咖啡时间' }
      context.setFillStyle(index === 0 ? '#F8A4A8' : index === 1 ? '#7FC2C9' : '#9B8071')
      context.fillRect(68, 385 + index * 70, 12, 36); context.setFillStyle('#2D2A26'); context.setFontSize(23)
      context.fillText(`${labels[value.dimension]}  ${value.count} 次`, 98, 412 + index * 70)
    })
    context.setFillStyle('#9A8E8A'); context.setFontSize(20); this.wrapText(context, summary.insight, 68, 625, 470, 34)
    context.setFillStyle('#2D2A26'); context.setFontSize(28); context.fillText(`¥ ${(summary.totalAmountFen / 100).toFixed(2)}`, 68, 715)
    context.setFillStyle('#9A8E8A'); context.setFontSize(18); context.fillText('只统计吃饭、奶茶与咖啡 · 私密节奏不会分享', 48, 820)
    context.setFillStyle('#F8A4A8'); context.setFontSize(18); context.fillText('让每一口，慢慢变成懂你的记忆。', 48, 855)
    context.draw(false, () => wx.canvasToTempFilePath({ canvasId: 'poster', width, height, destWidth: 1240, destHeight: 1800,
      success: ({ tempFilePath }) => this.setData({ posterPath: tempFilePath, posterVisible: true, posterTitle: `你的 ${this.data.month} 月生活切片` }) }, this))
  },
  generateWeeklyPoster() {
    const weekly = this.data.weekly as Weekly | undefined; if (!weekly) return
    const context = wx.createCanvasContext('poster', this); const width = 620; const height = 900
    context.setFillStyle('#FCF9F8'); context.fillRect(0, 0, width, height)
    const gradient = context.createLinearGradient(0, 0, width, 350); gradient.addColorStop(0, '#F6D2D3'); gradient.addColorStop(1, '#D9EBEA')
    context.setFillStyle(gradient); context.fillRect(0, 0, width, 330)
    context.setFillStyle('#2D2A26'); context.setFontSize(44); context.fillText('ONE · THIS WEEK', 46, 76)
    context.setFontSize(21); context.fillText(`${weekly.from} — ${weekly.to}`, 46, 120)
    context.setFontSize(76); context.fillText(`${weekly.activeDays}`, 46, 235)
    context.setFontSize(20); context.fillText('天留下生活切片', 132, 227)
    context.setFillStyle('#FFFFFF'); this.roundRect(context, 34, 372, 552, 350, 30); context.fill()
    context.setFillStyle('#2D2A26'); context.setFontSize(27); context.fillText(`${weekly.totalCount} 次真实记录 · ¥${(weekly.totalAmountFen / 100).toFixed(2)}`, 68, 432)
    context.setFillStyle('#9A8E8A'); context.setFontSize(21); this.wrapText(context, weekly.gentleMessage, 68, 500, 465, 38)
    if (weekly.easterEgg) { context.setFillStyle('#F8A4A8'); context.setFontSize(23); this.wrapText(context, weekly.easterEgg, 68, 630, 465, 36) }
    context.setFillStyle('#9A8E8A'); context.setFontSize(18); context.fillText('连续不是任务，只是偶然被看见的生活节奏。', 46, 812)
    context.setFillStyle('#7FC2C9'); context.fillText('个人私密记录不会出现在分享文案里。', 46, 850)
    context.draw(false, () => wx.canvasToTempFilePath({ canvasId: 'poster', width, height, destWidth: 1240, destHeight: 1800,
      success: ({ tempFilePath }) => this.setData({ posterPath: tempFilePath, posterVisible: true, posterTitle: '这周的 ONE' }) }, this))
  },
  closePoster() { this.setData({ posterVisible: false }) },
  savePoster() {
    if (!this.data.posterPath) return
    wx.saveImageToPhotosAlbum({ filePath: this.data.posterPath,
      success: () => wx.showToast({ title: '报告已保存', icon: 'success' }),
      fail: error => {
        if ((error.errMsg || '').includes('auth deny')) {
          wx.showModal({ title: '需要相册权限', content: '在设置中允许保存到相册，就能留下这张月报。',
            confirmText: '去设置', success: result => { if (result.confirm) wx.openSetting() } })
        } else wx.showToast({ title: '保存失败，请再试一次', icon: 'none' })
      } })
  },
  recordActions(event: WechatMiniprogram.TouchEvent) {
    const record = this.data.dayRecords[Number(event.currentTarget.dataset.index)]
    if (record) showRecordActions(record, () => this.selectDayByDate(this.data.selectedDate))
  },
  async selectDayByDate(date: string) {
    if (!date) return
    try { this.setData({ dayRecords: await request<RecordView[]>(`/records?date=${date}`) }); await this.loadMonth() }
    catch (_) { this.setData({ dayRecords: [] }) }
  },
  roundRect(ctx: WechatMiniprogram.CanvasContext, x:number,y:number,w:number,h:number,r:number) { ctx.beginPath(); ctx.moveTo(x+r,y); ctx.arcTo(x+w,y,x+w,y+h,r); ctx.arcTo(x+w,y+h,x,y+h,r); ctx.arcTo(x,y+h,x,y,r); ctx.arcTo(x,y,x+w,y,r); ctx.closePath() },
  wrapText(ctx: WechatMiniprogram.CanvasContext, text:string,x:number,y:number,max:number,line:number) { let current=''; let row=0; for(const char of text){ if(ctx.measureText(current+char).width>max){ctx.fillText(current,x,y+row*line);current=char;row++}else current+=char} if(current)ctx.fillText(current,x,y+row*line) },
  onShareAppMessage() { return { title: `${this.data.month}月，我在 ONE 留下了 ${this.data.activeDays} 天生活切片`, path: '/pages/home/index', imageUrl: this.data.posterPath || undefined } }
})
