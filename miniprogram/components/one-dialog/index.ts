interface DialogOptions {
  title: string
  content?: string
  confirmText?: string
  cancelText?: string
  danger?: boolean
}

export {}

const resolvers = new WeakMap<object, (confirmed: boolean) => void>()

Component({
  data: {
    visible: false,
    title: '',
    content: '',
    confirmText: '确认',
    cancelText: '取消',
    danger: false
  },
  lifetimes: {
    detached() {
      const resolver = resolvers.get(this)
      if (resolver) resolver(false)
      resolvers.delete(this)
    }
  },
  methods: {
    open(options: DialogOptions): Promise<boolean> {
      const pending = resolvers.get(this)
      if (pending) pending(false)
      this.setData({
        visible: true,
        title: options.title,
        content: options.content || '',
        confirmText: options.confirmText || '确认',
        cancelText: options.cancelText || '取消',
        danger: Boolean(options.danger)
      })
      return new Promise(resolve => { resolvers.set(this, resolve) })
    },
    cancel() { this.finish(false) },
    confirm() { this.finish(true) },
    swallow() { /* 阻止面板点击穿透 */ },
    finish(confirmed: boolean) {
      this.setData({ visible: false })
      const current = resolvers.get(this)
      resolvers.delete(this)
      if (current) current(confirmed)
    }
  }
})
