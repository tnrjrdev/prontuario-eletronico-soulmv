import { test, expect } from '@playwright/test'

test.describe('Smoke', () => {
  test('redireciona para login quando não autenticado', async ({ page }) => {
    await page.goto('/')
    await expect(page).toHaveURL(/\/login/)
    await expect(page.getByRole('heading', { name: /bem-vindo/i })).toBeVisible()
    await expect(page.getByRole('button', { name: /entrar no sistema/i })).toBeVisible()
  })

  test('exibe os campos da tela de login', async ({ page }) => {
    await page.goto('/login')
    await expect(page.getByPlaceholder('Ex: dr.joao')).toBeVisible()
    await expect(page.locator('input[type="password"]')).toBeVisible()
  })
})
