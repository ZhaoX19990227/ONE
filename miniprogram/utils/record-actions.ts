import { RecordView } from '../models/types'
import { request } from './request'

function actionSheet(itemList: string[]): Promise<number | undefined> {
  return new Promise(resolve => wx.showActionSheet({
    itemList,
    success: result => resolve(result.tapIndex),
    fail: () => resolve(undefined)
  }))
}

function confirmDelete(): Promise<boolean> {
  return new Promise(resolve => wx.showModal({
    title: '删除这条生活切片？',
    content: '关联的口味记忆也会一起撤回，之后的推荐不会再参考它。',
    confirmText: '删除',
    confirmColor: '#D9919E',
    success: result => resolve(result.confirm),
    fail: () => resolve(false)
  }))
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

export async function showRecordActions(record: RecordView, onDeleted: () => Promise<void> | void) {
  const repeatable = record.type !== 'PRIVATE_HABIT'
  const selected = await actionSheet(repeatable ? ['再记一次', '删除这条记录'] : ['删除这条记录'])
  if (selected === undefined) return
  if (repeatable && selected === 0) { repeat(record); return }
  if (!(await confirmDelete())) return
  try {
    await request<void>(`/records/${record.id}`, 'DELETE')
    wx.vibrateShort({ type: 'light' })
    await onDeleted()
    wx.showToast({ title: '已经轻轻擦去', icon: 'none' })
  } catch (error) {
    wx.showToast({ title: (error as Error).message, icon: 'none' })
  }
}
