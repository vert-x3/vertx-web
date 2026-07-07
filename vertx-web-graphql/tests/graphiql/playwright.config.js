import { defineConfig } from '@playwright/test'
export default defineConfig({
  testDir: '.',
  use: {
    baseURL: 'http://localhost:8080',
    navigationOptions: { waitUntil: 'networkidle' },
  },
  expect: { timeout: 30000 },
})
