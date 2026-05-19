const GRAPHIQL_URL = 'http://localhost:8080/graphiql/?query=' + encodeURIComponent('{ hello }')

// Monaco editor renders non-breaking spaces (U+00A0) instead of regular spaces
function normalizeSpaces(text) {
  return text.replace(/ /g, ' ')
}

test('execute query and verify response', async () => {
  await page.goto(GRAPHIQL_URL, {waitUntil: 'networkidle0'})
  await page.waitForSelector('.graphiql-container', {timeout: 10000})

  const executeButton = await page.waitForSelector('.graphiql-execute-button', {timeout: 5000})
  await executeButton.click()

  const deadline = Date.now() + 10000
  let content = ''
  while (Date.now() < deadline) {
    content = normalizeSpaces(await page.evaluate(() => {
      const el = document.querySelector('.graphiql-response')
      return el ? el.textContent : ''
    }))
    if (content.includes('Hello World!')) break
    await new Promise(r => setTimeout(r, 500))
  }

  expect(content).toContain('"hello"')
  expect(content).toContain('Hello World!')
}, 30000)
