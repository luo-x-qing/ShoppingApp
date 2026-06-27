import { defineConfig } from '@playwright/test'

export default defineConfig({
  testDir: './src/__tests__',
  timeout: 30000,
  use: {
    baseURL: 'http://localhost:8080',
    headless: true,
  },
})
