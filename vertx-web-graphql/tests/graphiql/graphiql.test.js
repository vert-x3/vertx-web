import { test, expect } from '@playwright/test'

test('execute query and verify response', async ({ page }) => {
  await page.goto('/graphiql/?query=' + encodeURIComponent('{ hello }'), { waitUntil: 'networkidle' })
  await page.waitForSelector('.graphiql-container', { timeout: 10000 })

  await page.locator('.graphiql-execute-button').click()

  await expect(page.locator('.graphiql-response')).toContainText('"hello"')
  await expect(page.locator('.graphiql-response')).toContainText('Hello World!')
})
