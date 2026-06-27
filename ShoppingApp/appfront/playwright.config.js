import { defineConfig } from '@playwright/test'

export default defineConfig({
  timeout: 30000,
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: [['html', { outputFolder: 'playwright-report' }]],
  projects: [
    {
      name: 'api',
      testDir: './e2e/api',
      use: {
        baseURL: process.env.API_URL || 'http://localhost:8080',
        extraHTTPHeaders: { 'Content-Type': 'application/json' },
      },
    },
    {
      name: 'workflow',
      testDir: './e2e/workflow',
      use: {
        baseURL: process.env.FRONTEND_URL || 'http://localhost:8081',
        headless: true,
        viewport: { width: 375, height: 812 },
      },
    },
  ],
})
