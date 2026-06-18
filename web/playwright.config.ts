import { defineConfig, devices } from '@playwright/test'

/**
 * Testes end-to-end. Requer browsers instalados uma vez: `npx playwright install`.
 * Sobe o dev server automaticamente (porta 5173). Para fluxos autenticados, o
 * backend precisa estar rodando (proxy /api).
 */
export default defineConfig({
  testDir: './e2e',
  timeout: 30_000,
  fullyParallel: true,
  use: {
    baseURL: 'http://localhost:5173',
    trace: 'on-first-retry',
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
  webServer: {
    command: 'npm run dev',
    url: 'http://localhost:5173',
    reuseExistingServer: !process.env.CI,
    timeout: 120_000,
  },
})
