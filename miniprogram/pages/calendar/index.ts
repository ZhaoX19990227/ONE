import { currentTheme, ThemeName } from '../../utils/theme'
import { request } from '../../utils/request'
import { RecordView } from '../../models/types'

interface DayCell { date: string; day?: number; empty?: boolean; totalCount: number; mealCount: number; milkTeaCount: number; coffeeCount: number; privateHabitCount: number; amountFen: number; coverUrl?: string; moodText?: string }
interface MonthView { month: string; activeDays: number; totalCount: number; totalAmountFen: number; days: DayCell[] }
interface Summary { activeDays: number; totalCount: number; totalAmountFen: number; insight: string; dimensions: Array<{dimension:string;count:number;amountFen:number}>; topBrands: Array<{brandName:string;count:number;amountFen:number;color?:string}> }

Page({
  data: { theme: 'noon' as ThemeName, year: 0, month: 0, cells: [] as DayCell[], activeDays: 0,
    totalAmountFen: 0, selectedDate: '', dayRecords: [] as RecordView[], summary: undefined as Summary | undefined,
    posterPath: '', loading: false },
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
      const [calendar, summary] = await Promise.all([
        request<MonthView>(`/calendar/month?month=${monthText}`),
        request<Summary>(`/analytics/summary?from=${from}&to=${to}`)
      ])
      const weekday = new Date(this.data.year, this.data.month - 1, 1).getDay()
      const placeholders: DayCell[] = Array.from({ length: weekday }, () => ({ date: '', empty: true, totalCount: 0, mealCount: 0, milkTeaCount: 0, coffeeCount: 0, privateHabitCount: 0, amountFen: 0 }))
      this.setData({ cells: placeholders.concat(calendar.days.map((day, index) => ({ ...day, day: index + 1 }))),
        activeDays: calendar.activeDays, totalAmountFen: calendar.totalAmountFen, summary })
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
      success: ({ tempFilePath }) => { this.setData({ posterPath: tempFilePath }); wx.previewImage({ urls: [tempFilePath] }) } }, this))
  },
  roundRect(ctx: WechatMiniprogram.CanvasContext, x:number,y:number,w:number,h:number,r:number) { ctx.beginPath(); ctx.moveTo(x+r,y); ctx.arcTo(x+w,y,x+w,y+h,r); ctx.arcTo(x+w,y+h,x,y+h,r); ctx.arcTo(x,y+h,x,y,r); ctx.arcTo(x,y,x+w,y,r); ctx.closePath() },
  wrapText(ctx: WechatMiniprogram.CanvasContext, text:string,x:number,y:number,max:number,line:number) { let current=''; let row=0; for(const char of text){ if(ctx.measureText(current+char).width>max){ctx.fillText(current,x,y+row*line);current=char;row++}else current+=char} if(current)ctx.fillText(current,x,y+row*line) },
  onShareAppMessage() { return { title: `${this.data.month}月，我在 ONE 留下了 ${this.data.activeDays} 天生活切片`, path: '/pages/home/index', imageUrl: this.data.posterPath || undefined } }
})
