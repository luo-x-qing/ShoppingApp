import { vi } from 'vitest'

globalThis.uni = {
  request: vi.fn(),
  showToast: vi.fn(),
  showModal: vi.fn(),
  navigateTo: vi.fn(),
  getStorageSync: vi.fn(),
  setStorageSync: vi.fn(),
  getSystemInfoSync: vi.fn(() => ({
    windowWidth: 375,
    windowHeight: 667,
  })),
}
