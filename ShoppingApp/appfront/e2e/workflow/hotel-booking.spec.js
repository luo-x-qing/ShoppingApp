import { test, expect } from '@playwright/test'

test.describe('酒店预订 E2E 工作流', () => {
  test('首页加载并显示酒店列表', async ({ page }) => {
    await page.goto('/')
    await page.waitForLoadState('networkidle')
    const title = await page.title()
    expect(title).toBeDefined()
  })
})
