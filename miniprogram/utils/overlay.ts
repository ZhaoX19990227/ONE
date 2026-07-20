export interface DialogOptions {
  title: string
  content?: string
  confirmText?: string
  cancelText?: string
  danger?: boolean
}

export interface ActionSheetOptions {
  title?: string
  items: string[]
  dangerIndex?: number
}

interface OverlayHost {
  selectComponent(selector: string): unknown
}

export async function oneDialog(host: OverlayHost, options: DialogOptions): Promise<boolean> {
  const dialog = host.selectComponent('#one-dialog') as { open(options: DialogOptions): Promise<boolean> } | undefined
  if (!dialog) return false
  return Boolean(await dialog.open(options))
}

export async function oneActionSheet(host: OverlayHost, options: ActionSheetOptions): Promise<number | undefined> {
  const sheet = host.selectComponent('#one-action-sheet') as { open(options: ActionSheetOptions): Promise<number | undefined> } | undefined
  if (!sheet) return undefined
  const result = await sheet.open(options)
  return typeof result === 'number' ? result : undefined
}
