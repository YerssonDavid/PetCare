import { expect, test } from '@playwright/test';

test('presents a public landing without API calls and connects sections and registration', async ({
  page,
  isMobile,
}, testInfo) => {
  const apiCalls: string[] = [];
  const errors: string[] = [];
  page.on('pageerror', (error) => errors.push(error.message));
  await page.route('**/api/**', (route) => {
    if (!new URL(route.request().url()).pathname.startsWith('/api/')) return route.continue();
    apiCalls.push(route.request().url());
    return route.abort();
  });
  await page.emulateMedia({ colorScheme: 'light' });
  await page.goto('/');
  await expect(page).toHaveURL(/\/$/);
  await expect(page.getByRole('heading', { level: 1 })).toHaveText(
    'El cuidado de tu mascota, en un solo lugar.',
  );
  await expect(
    page.getByRole('heading', { name: 'La tecnología también puede cuidar.' }),
  ).toBeVisible();
  await expect(page.getByRole('combobox')).toHaveCount(0);
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true);
  await page.screenshot({ path: testInfo.outputPath('landing-claro.png'), fullPage: true });

  await page.getByRole('button', { name: 'Activar modo oscuro' }).click();
  await expect(page.locator('html')).toHaveAttribute('data-theme', 'dark');
  await page.screenshot({ path: testInfo.outputPath('landing-oscuro.png'), fullPage: true });

  const navigation = page.getByRole('navigation', { name: 'Navegación pública' });
  if (isMobile) {
    await expect(navigation).toBeHidden();
    await page.getByRole('button', { name: 'Abrir menú' }).click();
    await navigation.getByRole('link', { name: 'El proyecto' }).focus();
    await page.keyboard.press('Escape');
    await expect(page.getByRole('button', { name: 'Abrir menú' })).toBeFocused();
    await page.getByRole('button', { name: 'Abrir menú' }).click();
  }
  await navigation.getByRole('link', { name: 'El proyecto' }).click();
  await expect(page).toHaveURL(/\/#proyecto$/);
  if (isMobile) await expect(navigation).toBeHidden();
  await page.getByRole('link', { name: 'Crear mi cuenta', exact: true }).first().click();
  await expect(page).toHaveURL(/\/registro$/);
  await expect(page.locator('html')).toHaveAttribute('data-theme', 'dark');
  await page.getByRole('link', { name: 'Volver a PetCare' }).click();
  await expect(page).toHaveURL(/\/$/);
  expect(apiCalls).toEqual([]);
  expect(errors).toEqual([]);
});

test('follows the system until a manual theme choice and keeps that choice across routes and reloads', async ({
  page,
}) => {
  await page.emulateMedia({ colorScheme: 'dark' });
  await page.goto('/');
  await expect(page.locator('html')).toHaveAttribute('data-theme', 'dark');
  await expect(page.getByRole('button', { name: 'Activar modo claro' })).toBeVisible();
  expect(await page.evaluate(() => localStorage.getItem('petcare_theme'))).toBeNull();

  await page.emulateMedia({ colorScheme: 'light' });
  await expect(page.locator('html')).toHaveAttribute('data-theme', 'light');
  const toggle = page.getByRole('button', { name: 'Activar modo oscuro' });
  await toggle.focus();
  await page.keyboard.press('Enter');
  await expect(page.locator('html')).toHaveAttribute('data-theme', 'dark');
  expect(await page.evaluate(() => localStorage.getItem('petcare_theme'))).toBe('dark');
  await page.emulateMedia({ colorScheme: 'dark' });
  await page.emulateMedia({ colorScheme: 'light' });
  await expect(page.locator('html')).toHaveAttribute('data-theme', 'dark');

  await page.getByRole('link', { name: 'Ya tengo una cuenta' }).click();
  await expect(page).toHaveURL(/\/login$/);
  await expect(page.getByRole('button', { name: 'Activar modo claro' })).toBeVisible();
  await page.getByRole('link', { name: 'Crear cuenta', exact: true }).click();
  await expect(page).toHaveURL(/\/registro$/);
  await page.reload();
  await expect(page.locator('html')).toHaveAttribute('data-theme', 'dark');
  await page.getByRole('button', { name: 'Activar modo claro' }).click();
  await expect(page.locator('html')).toHaveAttribute('data-theme', 'light');
  expect(await page.evaluate(() => localStorage.getItem('petcare_theme'))).toBe('light');
});

test('keeps manual theme navigation working when browser storage is unavailable', async ({
  page,
}) => {
  await page.addInitScript(() => {
    Object.defineProperty(window, 'localStorage', {
      get() {
        throw new DOMException('Storage unavailable', 'SecurityError');
      },
    });
  });
  await page.emulateMedia({ colorScheme: 'dark' });
  await page.goto('/');
  await expect(page.locator('html')).toHaveAttribute('data-theme', 'dark');
  await page.getByRole('button', { name: 'Activar modo claro' }).click();
  await expect(page.locator('html')).toHaveAttribute('data-theme', 'light');
  await page.getByRole('link', { name: 'Ya tengo una cuenta' }).click();
  await expect(page.getByRole('button', { name: 'Activar modo oscuro' })).toBeVisible();
});

test('adapts the public design to compact, tablet and desktop widths', async ({ page }) => {
  await page.goto('/');
  for (const width of [320, 768, 1024, 1440]) {
    await page.setViewportSize({ width, height: 900 });
    expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(
      true,
    );
    await expect(page.getByRole('heading', { level: 1 })).toBeVisible();
    await expect(page.getByRole('button', { name: /Activar modo/ })).toBeVisible();
  }
});
