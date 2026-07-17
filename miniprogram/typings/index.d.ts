interface IAppOption {
  globalData: { token: string; apiBase: string }
  ensureLogin(): Promise<string>
}
