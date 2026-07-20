interface ActionSheetOptions {
  title?: string
  items: string[]
  dangerIndex?: number
}

export {}

const resolvers = new WeakMap<object, (index: number | undefined) => void>()

Component({
  data: { visible: false, title: '', items: [] as string[], dangerIndex: -1 },
  lifetimes: {
    detached() {
      const resolver = resolvers.get(this)
      if (resolver) resolver(undefined)
      resolvers.delete(this)
    }
  },
  methods: {
    open(options: ActionSheetOptions): Promise<number | undefined> {
      const pending = resolvers.get(this)
      if (pending) pending(undefined)
      this.setData({ visible: true, title: options.title || '', items: options.items, dangerIndex: options.dangerIndex ?? -1 })
      return new Promise(resolve => { resolvers.set(this, resolve) })
    },
    choose(event: WechatMiniprogram.TouchEvent) { this.finish(Number(event.currentTarget.dataset.index)) },
    cancel() { this.finish(undefined) },
    swallow() { /* 阻止面板点击穿透 */ },
    finish(index: number | undefined) {
      this.setData({ visible: false })
      const current = resolvers.get(this)
      resolvers.delete(this)
      if (current) current(index)
    }
  }
})
