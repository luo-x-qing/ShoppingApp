import { createSSRApp } from 'vue'
import App from './App.vue'
import http from './utils/http'
import RouteFloat from './components/RouteFloat.vue'

export function createApp() {
  const app = createSSRApp(App)
  
  // 全局挂载请求
  app.config.globalProperties.$http = http
  // 全局注册浮动组件
  app.component('RouteFloat', RouteFloat)

  return {
    app
  }
}