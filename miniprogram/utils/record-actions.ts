import { RecordView } from '../models/types'
import { request } from './request'
import { oneActionSheet, oneDialog } from './overlay'

interface RecordActionHost {
  selectComponent(selector: string): unknown
  showToast?(message: string): void
}

function repeat(record: RecordView) {
  const query = [
    `dimension=${record.type}`,
    `itemId=${record.itemId || 0}`,
    `title=${encodeURIComponent(record.title)}`,
    `brand=${encodeURIComponent(record.brandName || '')}`,
    `amount=${record.actualAmountFen === undefined ? '' : record.actualAmountFen / 100}`,
    'copy=1'
  ].join('&')
  wx.navigateTo({ url: `/pages/record/index?${query}` })
}

export async function showRecordActions(host: RecordActionHost, record: RecordView,
                                        onDeleted: () => Promise<void> | void) {
  const repeatable = record.type !== 'PRIVATE_HABIT'
  const items = repeatable ? ['再记一次', '删除这条记录'] : ['删除这条记录']
  const selected = await oneActionSheet(host, { title: '想对这条生活切片做什么？', items, dangerIndex: items.length - 1 })
  if (selected === undefined) return
  if (repeatable && selected === 0) { repeat(record); return }
  if (!(await oneDialog(host, {
    title: '删除这条生活切片？',
    content: '关联的口味记忆也会一起撤回，之后的推荐不会再参考它。',
    confirmText: '删除',
    danger: true
  }))) return
  try {
    await request<void>(`/records/${record.id}`, 'DELETE')
    wx.vibrateShort({ type: 'light' })
    await onDeleted()
    host.showToast?.('已经轻轻擦去')
  } catch (error) {
    host.showToast?.((error as Error).message)
  }
}
