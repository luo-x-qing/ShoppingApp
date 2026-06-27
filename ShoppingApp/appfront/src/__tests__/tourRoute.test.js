import { describe, it, expect, vi, beforeEach } from 'vitest'
import { tourRouteApi } from '../../api/tourRoute.js'

describe('tourRouteApi', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('list() calls GET /tour-route/list', async () => {
    const mockData = [{ id: 1, name: '路线A', days: 3 }]
    globalThis.uni.request.mockImplementation(({ success }) =>
      success({ data: mockData })
    )

    const result = await tourRouteApi.list()
    expect(globalThis.uni.request).toHaveBeenCalledWith(
      expect.objectContaining({
        url: 'http://localhost:8080/tour-route/list',
        method: 'GET',
      })
    )
    expect(result).toEqual(mockData)
  })

  it('getById() calls GET /tour-route/{id}', async () => {
    globalThis.uni.request.mockImplementation(({ success }) =>
      success({ data: { id: 1, name: '路线A' } })
    )

    const result = await tourRouteApi.getById(1)
    expect(globalThis.uni.request).toHaveBeenCalledWith(
      expect.objectContaining({
        url: 'http://localhost:8080/tour-route/1',
        method: 'GET',
      })
    )
    expect(result.name).toBe('路线A')
  })

  it('getDetail() calls GET /tour-route/{id}/detail', async () => {
    globalThis.uni.request.mockImplementation(({ success }) =>
      success({ data: { id: 1, name: '路线A', routeScenics: [] } })
    )

    const result = await tourRouteApi.getDetail(1)
    expect(globalThis.uni.request).toHaveBeenCalledWith(
      expect.objectContaining({
        url: 'http://localhost:8080/tour-route/1/detail',
        method: 'GET',
      })
    )
    expect(result.routeScenics).toEqual([])
  })

  it('create() calls POST /tour-route/add', async () => {
    const newRoute = { name: '新路线', days: 3, price: 1000 }
    globalThis.uni.request.mockImplementation(({ success }) =>
      success({ data: { id: 1, ...newRoute } })
    )

    const result = await tourRouteApi.create(newRoute)
    expect(globalThis.uni.request).toHaveBeenCalledWith(
      expect.objectContaining({
        url: 'http://localhost:8080/tour-route/add',
        method: 'POST',
        data: newRoute,
      })
    )
    expect(result.id).toBe(1)
  })

  it('update() calls PUT /tour-route/{id}', async () => {
    const updateData = { name: '更新路线', days: 5 }
    globalThis.uni.request.mockImplementation(({ success }) =>
      success({ data: { id: 1, ...updateData } })
    )

    const result = await tourRouteApi.update(1, updateData)
    expect(globalThis.uni.request).toHaveBeenCalledWith(
      expect.objectContaining({
        url: 'http://localhost:8080/tour-route/1',
        method: 'PUT',
        data: updateData,
      })
    )
    expect(result.name).toBe('更新路线')
  })

  it('delete() calls DELETE /tour-route/{id}', async () => {
    globalThis.uni.request.mockImplementation(({ success }) =>
      success({ data: null })
    )

    await tourRouteApi.delete(1)
    expect(globalThis.uni.request).toHaveBeenCalledWith(
      expect.objectContaining({
        url: 'http://localhost:8080/tour-route/1',
        method: 'DELETE',
      })
    )
  })

  it('rejects on network error', async () => {
    globalThis.uni.request.mockImplementation(({ fail }) =>
      fail(new Error('Network error'))
    )

    await expect(tourRouteApi.list()).rejects.toThrow('Network error')
  })
})
