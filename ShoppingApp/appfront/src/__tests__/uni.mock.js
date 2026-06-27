import { vi } from 'vitest'

const mockStorage = {}

export function createUniMock() {
  const mock = {
    // --- Request ---
    request: vi.fn(),
    
    // --- Storage ---
    getStorageSync: vi.fn((key) => {
      const raw = mockStorage[key]
      try {
        return raw ? JSON.parse(raw) : (raw ?? [])
      } catch {
        return raw ?? []
      }
    }),
    setStorageSync: vi.fn((key, val) => {
      mockStorage[key] = typeof val === 'string' ? val : JSON.stringify(val)
    }),
    
    // --- Toast / Loading / Modal ---
    showToast: vi.fn(),
    showLoading: vi.fn(),
    hideLoading: vi.fn(),
    showModal: vi.fn(({ success }) => {
      if (success) success({ confirm: true, cancel: false })
    }),
    
    // --- Navigation ---
    navigateTo: vi.fn(),
    switchTab: vi.fn(),
    redirectTo: vi.fn(),
    reLaunch: vi.fn(),
    navigateBack: vi.fn(),
    
    // --- Location ---
    getLocation: vi.fn(({ success, fail }) => {
      if (success) {
        success({
          latitude: 26.0822,
          longitude: 119.2955,
          speed: 0,
          accuracy: 65
        })
      }
    }),
    
    // --- System Info ---
    getSystemInfoSync: vi.fn(() => ({
      windowWidth: 375,
      windowHeight: 667,
      pixelRatio: 2,
      platform: 'ios',
      language: 'zh-CN',
      model: 'iPhone X'
    })),
    
    // --- Others ---
    getStorageInfoSync: vi.fn(() => ({ keys: [], currentSize: 0, limitSize: 10 })),
    removeStorageSync: vi.fn(),
    
    // --- Helper to clear storage between tests ---
    __clearStorage() {
      Object.keys(mockStorage).forEach(k => delete mockStorage[k])
    },
    
    // --- Helper to preset storage ---
    __setStorage(key, val) {
      mockStorage[key] = typeof val === 'string' ? val : JSON.stringify(val)
    }
  }
  
  return mock
}

export function resetUniMock(mock) {
  Object.values(mock).forEach(val => {
    if (typeof val === 'function' && val.mockClear) {
      val.mockClear()
    }
  })
  mock.__clearStorage()
}
