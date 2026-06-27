import { test, expect } from '@playwright/test'

test.describe('景点 & 路线 API E2E', () => {
  test.describe('景点 Scenic', () => {
    test('GET /scenic/list 返回景点列表', async ({ request }) => {
      const res = await request.get('/scenic/list')
      expect(res.ok()).toBeTruthy()
      const body = await res.json()
      expect(Array.isArray(body)).toBeTruthy()
    })

    test('GET /scenic/{id} 返回景点详情', async ({ request }) => {
      const list = await (await request.get('/scenic/list')).json()
      if (!list || list.length === 0) return
      const id = list[0].id
      const res = await request.get(`/scenic/${id}`)
      expect(res.ok()).toBeTruthy()
      const body = await res.json()
      expect(body.id).toBe(id)
    })

    test('GET /scenic/search 搜索景点', async ({ request }) => {
      const res = await request.get('/scenic/search?name=景点')
      if (!res.ok()) return
      const body = await res.json()
      expect(Array.isArray(body)).toBeTruthy()
    })

    test('Scenic CRUD 工作流', async ({ request }) => {
      const newScenic = {
        name: 'E2E 测试景点',
        province: '测试省',
        city: '测试市',
        description: '由 Playwright E2E 自动创建',
        score: 4.5,
        ticketPrice: 100,
      }
      const createRes = await (await request.post('/scenic/add', { data: newScenic })).json()
      expect(createRes.name).toBe('E2E 测试景点')
      const id = createRes.id

      const getRes = await (await request.get(`/scenic/${id}`)).json()
      expect(getRes.name).toBe('E2E 测试景点')

      const delRes = await request.delete(`/scenic/${id}`)
      expect(delRes.ok()).toBeTruthy()
    })
  })

  test.describe('旅游路线 TourRoute', () => {
    test('GET /tour-route/list 返回路线列表', async ({ request }) => {
      const res = await request.get('/tour-route/list')
      expect(res.ok()).toBeTruthy()
      const body = await res.json()
      expect(Array.isArray(body)).toBeTruthy()
    })

    test('GET /tour-route/{id} 返回路线详情', async ({ request }) => {
      const list = await (await request.get('/tour-route/list')).json()
      if (!list || list.length === 0) return
      const id = list[0].id
      const res = await request.get(`/tour-route/${id}`)
      expect(res.ok()).toBeTruthy()
      const body = await res.json()
      expect(body.id).toBe(id)
    })

    test('GET /tour-route/{id}/detail 含关联景点', async ({ request }) => {
      const list = await (await request.get('/tour-route/list')).json()
      if (!list || list.length === 0) return
      const id = list[0].id
      const res = await request.get(`/tour-route/${id}/detail`)
      expect(res.ok()).toBeTruthy()
    })
  })
})
