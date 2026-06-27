import { test, expect } from '@playwright/test'

test.describe('酒店 API E2E', () => {
  test('GET /api/hotels 返回酒店列表', async ({ request }) => {
    const res = await request.get('/api/hotels')
    expect(res.ok()).toBeTruthy()
    const body = await res.json()
    expect(body.code).toBe(200)
    expect(Array.isArray(body.data)).toBeTruthy()
  })

  test('GET /api/hotels/{id} 返回酒店详情', async ({ request }) => {
    const list = await (await request.get('/api/hotels')).json()
    if (!list.data || list.data.length === 0) return
    const id = list.data[0].id
    const res = await request.get(`/api/hotels/${id}`)
    expect(res.ok()).toBeTruthy()
    const body = await res.json()
    expect(body.code).toBe(200)
    expect(body.data.id).toBe(id)
  })

  test('GET /api/hotels/{id}/detail 返回完整信息', async ({ request }) => {
    const list = await (await request.get('/api/hotels')).json()
    if (!list.data || list.data.length === 0) return
    const id = list.data[0].id
    const res = await request.get(`/api/hotels/${id}/detail`)
    expect(res.ok()).toBeTruthy()
    const body = await res.json()
    expect(body.data.hotel).toBeDefined()
    expect(body.data.roomTypes).toBeDefined()
  })

  test('GET /api/hotels/search 按关键词搜索', async ({ request }) => {
    const res = await request.get('/api/hotels/search?keyword=酒店')
    expect(res.ok()).toBeTruthy()
    const body = await res.json()
    expect(body.code).toBe(200)
    expect(Array.isArray(body.data)).toBeTruthy()
  })

  test('GET /api/hotels/category/{category} 按分类查询', async ({ request }) => {
    const res = await request.get('/api/hotels/category/经济')
    expect(res.ok()).toBeTruthy()
    const body = await res.json()
    expect(body.code).toBe(200)
  })

  test('GET /api/hotels/sort/price 按价格排序', async ({ request }) => {
    const res = await request.get('/api/hotels/sort/price')
    expect(res.ok()).toBeTruthy()
    const body = await res.json()
    if (body.data && body.data.length > 1) {
      for (let i = 1; i < body.data.length; i++) {
        expect(body.data[i].price).toBeGreaterThanOrEqual(body.data[i - 1].price)
      }
    }
  })

  test('GET /api/hotels/sort/rating 按评分排序', async ({ request }) => {
    const res = await request.get('/api/hotels/sort/rating')
    expect(res.ok()).toBeTruthy()
    const body = await res.json()
    if (body.data && body.data.length > 1) {
      for (let i = 1; i < body.data.length; i++) {
        expect(body.data[i].avgRating).toBeGreaterThanOrEqual(body.data[i - 1].avgRating)
      }
    }
  })

  test('GET /api/hotels/{hotelId}/room-types 返回房型', async ({ request }) => {
    const list = await (await request.get('/api/hotels')).json()
    if (!list.data || list.data.length === 0) return
    const id = list.data[0].id
    const res = await request.get(`/api/hotels/${id}/room-types`)
    expect(res.ok()).toBeTruthy()
    const body = await res.json()
    expect(body.code).toBe(200)
  })
})
