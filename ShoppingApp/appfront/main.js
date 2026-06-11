import { createSSRApp } from 'vue'
import App from './App.vue'
import http from './utils/http'

export function createApp() {
  const app = createSSRApp(App)
  
  // 全局挂载请求
  app.config.globalProperties.$http = http

  return {
    app
  }
}