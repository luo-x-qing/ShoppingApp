import { describe, it, expect, vi, beforeEach } from 'vitest'
import { scenicApi } from '../../api/scenic.js'

describe('scenicApi', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('list() calls GET /scenic/list', async () => {
    const mockData = [{ id: 1, name: '景点A' }]
    globalThis.uni.request.mockImplementation(({ success }) =>
      success({ data: mockData })
    )

    const result = await scenicApi.list()
    expect(globalThis.uni.request).toHaveBeenCalledWith(
      expect.objectContaining({
        url: 'http://localhost:8080/scenic/list',
        method: 'GET',
      })
    )
    expect(result).toEqual(mockData)
  })

  it('getById() calls GET /scenic/{id}', async () => {
    globalThis.uni.request.mockImplementation(({ success }) =>
      success({ data: { id: 1, name: '景点A' } })
    )

    const result = await scenicApi.getById(1)
    expect(globalThis.uni.request).toHaveBeenCalledWith(
      expect.objectContaining({
        url: 'http://localhost:8080/scenic/1',
        method: 'GET',
      })
    )
    expect(result.name).toBe('景点A')
  })

  it('create() calls POST /scenic/add', async () => {
    const newScenic = { name: '新景点', province: '四川省' }
    globalThis.uni.request.mockImplementation(({ success }) =>
      success({ data: { id: 1, ...newScenic } })
    )

    const result = await scenicApi.create(newScenic)
    expect(globalThis.uni.request).toHaveBeenCalledWith(
      expect.objectContaining({
        url: 'http://localhost:8080/scenic/add',
        method: 'POST',
        data: newScenic,
      })
    )
    expect(result.id).toBe(1)
  })

  it('update() calls PUT /scenic/{id}', async () => {
    const updateData = { name: '更新名', province: '四川省' }
    globalThis.uni.request.mockImplementation(({ success }) =>
      success({ data: { id: 1, ...updateData } })
    )

    const result = await scenicApi.update(1, updateData)
    expect(globalThis.uni.request).toHaveBeenCalledWith(
      expect.objectContaining({
        url: 'http://localhost:8080/scenic/1',
        method: 'PUT',
        data: updateData,
      })
    )
    expect(result.name).toBe('更新名')
  })

  it('delete() calls DELETE /scenic/{id}', async () => {
    globalThis.uni.request.mockImplementation(({ success }) =>
      success({ data: null })
    )

    await scenicApi.delete(1)
    expect(globalThis.uni.request).toHaveBeenCalledWith(
      expect.objectContaining({
        url: 'http://localhost:8080/scenic/1',
        method: 'DELETE',
      })
    )
  })

  it('search() builds query string correctly', async () => {
    globalThis.uni.request.mockImplementation(({ success }) =>
      success({ data: [] })
    )

    await scenicApi.search('四川省', '成都市', '熊猫')
    expect(globalThis.uni.request).toHaveBeenCalledWith(
      expect.objectContaining({
        url: expect.stringContaining('/scenic/search'),
        method: 'GET',
      })
    )
    const callUrl = globalThis.uni.request.mock.calls[0][0].url
    expect(callUrl).toContain('province=')
    expect(callUrl).toContain('city=')
    expect(callUrl).toContain('name=')
  })

  it('search() with partial params', async () => {
    globalThis.uni.request.mockImplementation(({ success }) =>
      success({ data: [] })
    )

    await scenicApi.search('', '成都市', '')
    const callUrl = globalThis.uni.request.mock.calls[0][0].url
    expect(callUrl).toContain('city=%E6%88%90%E9%83%BD%E5%B8%82')
    expect(callUrl).not.toContain('province=')
    expect(callUrl).not.toContain('name=')
  })

  it('audit() calls PUT /scenic/{id}/audit', async () => {
    globalThis.uni.request.mockImplementation(({ success }) =>
      success({ data: { id: 1, auditStatus: 1 } })
    )

    const result = await scenicApi.audit(1, 1)
    expect(globalThis.uni.request).toHaveBeenCalledWith(
      expect.objectContaining({
        url: 'http://localhost:8080/scenic/1/audit',
        method: 'PUT',
      })
    )
  })

  it('auditList() calls GET /scenic/audit/list', async () => {
    globalThis.uni.request.mockImplementation(({ success }) =>
      success({ data: [] })
    )

    await scenicApi.auditList(0)
    expect(globalThis.uni.request).toHaveBeenCalledWith(
      expect.objectContaining({
        url: 'http://localhost:8080/scenic/audit/list?auditStatus=0',
        method: 'GET',
      })
    )
  })

  it('rejects on network error', async () => {
    globalThis.uni.request.mockImplementation(({ fail }) =>
      fail(new Error('Network error'))
    )

    await expect(scenicApi.list()).rejects.toThrow('Network error')
  })
})
