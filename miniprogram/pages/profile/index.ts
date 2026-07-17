import { currentTheme, ThemeName } from '../../utils/theme'
import { request, uploadImage } from '../../utils/request'

interface Profile { nickname:string;avatarUrl?:string;heightCm?:number;weightGram?:number;gayRole?:'ONE'|'ZERO'|'VERS'|'SIDE';monthlyBudgetFen?:number;aiEnabled:boolean;privateHabitEnabled:boolean;mealPreferences:string;drinkPreferences:string;dietaryRestrictions:string }

Page({
  data: { theme: 'noon' as ThemeName, nickname: 'ONE群友', avatarUrl: '', heightCm: '', weightKg: '', gayRole: '',
    monthlyBudgetYuan: '', aiEnabled: true, privateHabitEnabled: true,
    mealOptions: ['清淡', '重口', '粉面', '米饭', '火锅', '快捷'].map(name => ({ name, selected: false })), selectedMealTags: [] as string[],
    drinkOptions: ['奶香', '果茶', '纯茶', '黑咖', '奶咖', '少少甜'].map(name => ({ name, selected: false })), selectedDrinkTags: [] as string[], saving: false, toast: '' },
  onShow() {
    const tab = this.getTabBar?.(); if (tab) tab.setData({ selected: 2 })
    this.setData({ theme: currentTheme() }); this.load()
  },
  async load() {
    try {
      const profile = await request<Profile>('/me')
      const meal = this.parsePreferences(profile.mealPreferences); const drink = this.parsePreferences(profile.drinkPreferences)
      this.setData({ nickname: profile.nickname, avatarUrl: profile.avatarUrl || '', heightCm: profile.heightCm ? String(profile.heightCm) : '',
        weightKg: profile.weightGram ? String(profile.weightGram / 1000) : '', gayRole: profile.gayRole || '',
        monthlyBudgetYuan: profile.monthlyBudgetFen ? String(profile.monthlyBudgetFen / 100) : '', aiEnabled: profile.aiEnabled,
        privateHabitEnabled: profile.privateHabitEnabled, selectedMealTags: meal.tags || [], selectedDrinkTags: drink.tags || [],
        mealOptions: this.data.mealOptions.map(value => ({ ...value, selected: (meal.tags || []).includes(value.name) })),
        drinkOptions: this.data.drinkOptions.map(value => ({ ...value, selected: (drink.tags || []).includes(value.name) })) })
    } catch (_) { /* 继续显示默认资料 */ }
  },
  parsePreferences(value: string): {tags?:string[]} { try { return JSON.parse(value || '{}') } catch (_) { return {} } },
  async chooseAvatar(event: WechatMiniprogram.CustomEvent) {
    const path = (event.detail as { avatarUrl: string }).avatarUrl
    try { const media = await uploadImage(path); this.setData({ avatarUrl: media.url }) } catch (error) { this.showToast((error as Error).message) }
  },
  inputNickname(event: WechatMiniprogram.Input) { this.setData({ nickname: event.detail.value }) },
  inputHeight(event: WechatMiniprogram.Input) { this.setData({ heightCm: event.detail.value }) },
  inputWeight(event: WechatMiniprogram.Input) { this.setData({ weightKg: event.detail.value }) },
  inputBudget(event: WechatMiniprogram.Input) { this.setData({ monthlyBudgetYuan: event.detail.value }) },
  selectRole(event: WechatMiniprogram.TouchEvent) { this.setData({ gayRole: event.currentTarget.dataset.value }) },
  toggleMeal(event: WechatMiniprogram.TouchEvent) { this.toggleTag('selectedMealTags', 'mealOptions', event.currentTarget.dataset.value) },
  toggleDrink(event: WechatMiniprogram.TouchEvent) { this.toggleTag('selectedDrinkTags', 'drinkOptions', event.currentTarget.dataset.value) },
  toggleTag(field: 'selectedMealTags'|'selectedDrinkTags', optionField:'mealOptions'|'drinkOptions', value:string) { const tags = [...this.data[field]]; const index=tags.indexOf(value); index>=0?tags.splice(index,1):tags.push(value); this.setData({[field]:tags,[optionField]:this.data[optionField].map(item=>({...item,selected:tags.includes(item.name)}))}) },
  toggleAi(event: WechatMiniprogram.SwitchChange) { this.setData({ aiEnabled: event.detail.value }) },
  toggleHabit(event: WechatMiniprogram.SwitchChange) { this.setData({ privateHabitEnabled: event.detail.value }) },
  async save() {
    if (!this.data.nickname.trim()) { this.showToast('给自己留一个昵称'); return }
    this.setData({ saving: true })
    try {
      await request('/me', 'PUT', { nickname: this.data.nickname, avatarUrl: this.data.avatarUrl || undefined,
        heightCm: this.data.heightCm ? Number(this.data.heightCm) : undefined,
        weightGram: this.data.weightKg ? Math.round(Number(this.data.weightKg) * 1000) : undefined,
        gayRole: this.data.gayRole || undefined, monthlyBudgetFen: this.data.monthlyBudgetYuan ? Math.round(Number(this.data.monthlyBudgetYuan) * 100) : undefined,
        aiEnabled: this.data.aiEnabled, privateHabitEnabled: this.data.privateHabitEnabled,
        mealPreferences: { tags: this.data.selectedMealTags }, drinkPreferences: { tags: this.data.selectedDrinkTags }, dietaryRestrictions: [] })
      wx.vibrateShort({ type: 'light' }); this.showToast('资料已经轻轻收好')
    } catch (error) { this.showToast((error as Error).message) }
    finally { this.setData({ saving: false }) }
  },
  showToast(message:string) { this.setData({ toast: message }); setTimeout(() => this.setData({ toast: '' }), 2200) }
})
