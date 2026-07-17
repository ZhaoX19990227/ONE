export type ThemeName = 'morning' | 'noon' | 'evening' | 'night'

export function currentTheme(date = new Date()): ThemeName {
  const hour = date.getHours()
  if (hour >= 6 && hour < 11) return 'morning'
  if (hour >= 11 && hour < 17) return 'noon'
  if (hour >= 17 && hour < 22) return 'evening'
  return 'night'
}

export function greeting(theme: ThemeName): string {
  return { morning: '早安，今天想吃点什么？', noon: '午间到站，把纠结交给 ONE。',
    evening: '晚风来了，选一口刚好的。', night: '夜深了，也可以留下一点生活。' }[theme]
}
