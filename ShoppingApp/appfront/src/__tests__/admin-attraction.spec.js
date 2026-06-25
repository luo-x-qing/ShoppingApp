import { test, expect } from '@playwright/test'

const MOCK_ATTRACTIONS = [
  { id: 1, name: '宽窄巷子', province: '四川省', city: '成都市', photo: '/photos/1.jpg', createTime: '2026-01-01' },
  { id: 2, name: '武侯祠', province: '四川省', city: '成都市', photo: '/photos/2.jpg', createTime: '2026-01-02' },
  { id: 3, name: '大雁塔', province: '陕西省', city: '西安市', photo: '/photos/3.jpg', createTime: '2026-01-03' },
]

const MOCK_UPLOAD_RESPONSE = '/uploads/test.jpg'

test.describe('Admin Attraction Management Page', () => {
  test.beforeEach(async ({ page }) => {
    await page.route('**/api/attractions', async (route) => {
      if (route.request().method() === 'GET') {
        await route.fulfill({ contentType: 'application/json', body: JSON.stringify(MOCK_ATTRACTIONS) })
      } else if (route.request().method() === 'POST') {
        await route.fulfill({ contentType: 'application/json', body: JSON.stringify({ id: 4, ...route.request().postDataJSON() }) })
      } else {
        await route.continue()
      }
    })

    await page.route('**/api/upload', async (route) => {
      await route.fulfill({ contentType: 'text/plain', body: MOCK_UPLOAD_RESPONSE })
    })

    await page.route('**/api/attractions/*', async (route) => {
      const url = route.request().url()
      const method = route.request().method()

      if (method === 'GET') {
        const id = parseInt(url.split('/').pop())
        const attraction = MOCK_ATTRACTIONS.find(a => a.id === id)
        await route.fulfill({
          contentType: 'application/json',
          body: JSON.stringify(attraction || { error: 'not found' }),
        })
      } else if (method === 'PUT') {
        await route.fulfill({ contentType: 'application/json', body: JSON.stringify({ id: 1, ...route.request().postDataJSON() }) })
      } else if (method === 'DELETE') {
        await route.fulfill({ status: 200 })
      } else {
        await route.continue()
      }
    })

    await page.route('**/api/attractions/import-all', async (route) => {
      await route.fulfill({ contentType: 'application/json', body: JSON.stringify({ added: 50 }) })
    })

    await page.route('**/api/attractions/reset-and-renumber', async (route) => {
      await route.fulfill({ contentType: 'application/json', body: JSON.stringify({ message: '重置成功' }) })
    })

    await page.goto('/admin/attractions')
  })

  test('should display the page title and sidebar', async ({ page }) => {
    await expect(page.locator('h3')).toContainText('景点管理')
    const sidebar = page.locator('.list-group')
    await expect(sidebar).toContainText('景点管理')
    await expect(sidebar).toContainText('酒店管理')
    await expect(sidebar).toContainText('用户管理')
  })

  test('should load and display attractions in the table', async ({ page }) => {
    await page.waitForSelector('#attractionList tr td')
    const rows = page.locator('#attractionList tr')
    const rowCount = await rows.count()
    expect(rowCount).toBeGreaterThan(1)

    await expect(page.locator('#attractionList')).toContainText('宽窄巷子')
    await expect(page.locator('#attractionList')).toContainText('武侯祠')
    await expect(page.locator('#attractionList')).toContainText('大雁塔')
  })

  test('should display correct statistics', async ({ page }) => {
    await page.waitForSelector('#totalCount')
    const totalText = await page.locator('#totalCount').textContent()
    expect(totalText).toBe('3')

    const provinceText = await page.locator('#provinceCount').textContent()
    expect(provinceText).toBe('2')
  })

  test('should filter by province', async ({ page }) => {
    await page.waitForSelector('#filterProvince')
    await page.locator('#filterProvince').selectOption('四川省')
    await page.locator('button:has-text("搜索")').click()

    await page.waitForTimeout(500)
    const tableText = await page.locator('#attractionList').textContent()
    expect(tableText).toContain('宽窄巷子')
    expect(tableText).toContain('武侯祠')
    expect(tableText).not.toContain('大雁塔')
  })

  test('should search by keyword', async ({ page }) => {
    await page.waitForSelector('#searchKeyword')
    await page.locator('#searchKeyword').fill('大雁塔')
    await page.locator('button:has-text("搜索")').click()

    await page.waitForTimeout(500)
    const tableText = await page.locator('#attractionList').textContent()
    expect(tableText).toContain('大雁塔')
    expect(tableText).not.toContain('宽窄巷子')
  })

  test('should open add modal', async ({ page }) => {
    await page.locator('button:has-text("添加景点")').click()
    await expect(page.locator('#addModal')).toHaveClass(/show/)
    await expect(page.locator('#addModal .modal-title')).toContainText('添加景点')
  })

  test('should open edit modal with data', async ({ page }) => {
    await page.waitForSelector('button:has-text("修改")')
    await page.locator('button:has-text("修改")').first().click()
    await expect(page.locator('#editModal')).toHaveClass(/show/)

    const nameInput = page.locator('#editName')
    await expect(nameInput).toHaveValue('宽窄巷子')
  })

  test('should delete an attraction', async ({ page }) => {
    page.on('dialog', dialog => dialog.accept())
    await page.waitForSelector('button:has-text("删除")')
    await page.locator('button:has-text("删除")').first().click()

    await page.waitForTimeout(300)
    await expect(page.locator('#attractionList')).toContainText('宽窄巷子')
  })

  test('should import all attractions', async ({ page }) => {
    page.on('dialog', dialog => dialog.accept())

    await page.locator('button:has-text("一键导入全国景点")').click()
    await page.waitForTimeout(500)

    const totalText = await page.locator('#totalCount').textContent()
    expect(totalText).toBe('3')
  })

  test('should refresh the list', async ({ page }) => {
    await page.locator('button:has-text("刷新")').click()
    await page.waitForTimeout(500)
    await expect(page.locator('#attractionList')).toContainText('宽窄巷子')
  })
})
