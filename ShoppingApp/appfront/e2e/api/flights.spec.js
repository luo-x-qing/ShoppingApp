import { test, expect } from '@playwright/test'

test.describe('机票 API E2E', () => {
  test('GET /api/flights/search 搜索航班', async ({ request }) => {
    const res = await request.get('/api/flights/search?dep=PEK&arr=SHA&date=2026-07-15')
    expect(res.ok()).toBeTruthy()
    const body = await res.json()
    expect(body.success).toBeDefined()
  })

  test.describe('FlightOrder CRUD 工作流', () => {
    let createdId

    test('POST /api/flight-orders 创建订单', async ({ request }) => {
      const order = {
        flightNumber: 'CA1234',
        departCity: '北京',
        arriveCity: '上海',
        departTime: '2026-07-15T08:00:00',
        arriveTime: '2026-07-15T10:30:00',
        price: 850,
        username: 'e2etest',
        passengerName: '张三',
        passengerIdCard: '110101199001011234',
        contactPhone: '13800138000',
        status: '待支付',
      }
      const res = await request.post('/api/flight-orders', { data: order })
      expect(res.ok()).toBeTruthy()
      const body = await res.json()
      expect(body.code).toBe(200)
      expect(body.data.flightNumber).toBe('CA1234')
      createdId = body.data.id
    })

    test('GET /api/flight-orders?username= 查询订单', async ({ request }) => {
      const res = await request.get('/api/flight-orders?username=e2etest')
      expect(res.ok()).toBeTruthy()
      const body = await res.json()
      expect(body.code).toBe(200)
      expect(Array.isArray(body.data)).toBeTruthy()
    })

    test('PUT /api/flight-orders/{id}/pay 支付订单', async ({ request }) => {
      if (!createdId) return
      const res = await request.put(`/api/flight-orders/${createdId}/pay`)
      expect(res.ok()).toBeTruthy()
      const body = await res.json()
      expect(body.data.status).toBe('已支付')
    })

    test('DELETE /api/flight-orders/{id} 删除订单', async ({ request }) => {
      if (!createdId) return
      const res = await request.delete(`/api/flight-orders/${createdId}`)
      expect(res.ok()).toBeTruthy()
    })
  })
})
