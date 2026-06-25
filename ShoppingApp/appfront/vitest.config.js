import { defineConfig } from 'vitest/config'

export default defineConfig({
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: ['./src/__tests__/setup.js'],
    exclude: ['node_modules/**', 'src/__tests__/admin-attraction.spec.js'],
  },
})
