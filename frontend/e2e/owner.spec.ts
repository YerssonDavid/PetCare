import { expect, test, type Page } from '@playwright/test';

async function openNavigation(page: Page) {
  const toggle = page.getByRole('button', { name: 'Mostrar navegación', exact: true });
  if (await toggle.isVisible()) await toggle.click();
}

const ownerId = '11111111-1111-4111-8111-111111111111';
const petId = '22222222-2222-4222-8222-222222222222';
const offeringId = '33333333-3333-4333-8333-333333333333';
const slotId = '44444444-4444-4444-8444-444444444444';
const requestId = '55555555-5555-4555-8555-555555555555';
const offering = {
  id: offeringId,
  providerId: '66666666-6666-4666-8666-666666666666',
  name: 'Consulta veterinaria',
  description: 'Un chequeo completo para tu compañero.',
  conditions: 'Traer el carné de vacunación.',
  price: 65000,
  category: 'VETERINARIA',
  type: 'SERVICIO',
};
const slot = { id: slotId, date: '2099-12-20', startTime: '10:00:00', endTime: '11:00:00' };
const request = {
  id: requestId,
  petName: 'Luna',
  offeringName: offering.name,
  offeringType: 'SERVICIO',
  providerName: 'Clínica PetCare',
  date: slot.date,
  startTime: slot.startTime,
  endTime: slot.endTime,
  price: offering.price,
  status: 'PENDIENTE',
  requestedAt: '2026-11-19T10:00:00-05:00',
};

function loginResponse() {
  const token = (claims: object) =>
    `${Buffer.from('{}').toString('base64url')}.${Buffer.from(JSON.stringify(claims)).toString('base64url')}.test`;
  return {
    token: token({
      sub: ownerId,
      role: 'USER',
      jti: 'not-the-user',
      exp: Math.floor(Date.now() / 1000) + 3600,
    }),
    refreshToken: token({ sub: ownerId, exp: Math.floor(Date.now() / 1000) + 604800 }),
  };
}

test('owner journey, theme persistence and memory-only authentication', async ({
  page,
}, testInfo) => {
  const pets: { id: string; name: string; typePet: string; age: number; description: string }[] =
    [];
  const requests: (typeof request)[] = [];
  await page.emulateMedia({ colorScheme: 'light' });
  await page.route('**/api/**', async (route) => {
    const url = new URL(route.request().url());
    if (!url.pathname.startsWith('/api/')) return route.continue();
    const path = url.pathname.replace('/api', '');
    const method = route.request().method();
    const reply = (body: unknown, status = 200) => route.fulfill({ status, json: body });
    if (path === '/registry/owner') return reply({ id: ownerId, message: 'Cuenta creada' }, 201);
    if (path === '/auth/login') return reply(loginResponse());
    expect(route.request().headers().authorization).toMatch(/^Bearer /);
    if (path === '/pets') return reply(pets);
    if (path === '/registry/pet') {
      const body = route.request().postDataJSON() as Record<string, unknown>;
      expect(body).not.toHaveProperty('ownerId');
      pets.push({
        id: petId,
        name: String(body.name),
        typePet: String(body.typePet),
        age: Number(body.age),
        description: String(body.description),
      });
      return reply({ id: petId }, 201);
    }
    if (path === '/services') return reply([offering]);
    if (path === `/services/${offeringId}`)
      return reply({
        ...offering,
        providerName: 'Clínica PetCare',
        providerPhone: '3001234567',
        freeSlots: [slot],
      });
    if (path === `/services/${offeringId}/slots/free`) return reply(requests.length ? [] : [slot]);
    if (path === '/service-requests' && method === 'POST') {
      expect(route.request().postDataJSON()).toEqual({ petId, slotId });
      requests.push(request);
      return reply(request, 201);
    }
    if (path === '/service-requests/mine') return reply(requests);
    if (path === `/service-requests/${requestId}/history`)
      return reply({
        requestId,
        offeringName: offering.name,
        offeringDescription: offering.description,
        offeringType: 'SERVICIO',
        pet: pets[0],
        entries: [{ status: 'PENDIENTE', changedAt: request.requestedAt }],
      });
    return reply({ detail: `Ruta no prevista: ${method} ${path}` }, 404);
  });

  await page.goto('/registro');
  await page.screenshot({ path: testInfo.outputPath('registro-claro.png'), fullPage: true });
  await page.getByLabel('Nombre completo').fill('Ana Mosquera');
  await page.getByLabel('Correo electrónico').fill('ana@example.test');
  await page.getByLabel('Teléfono').fill('3001234567');
  await page.getByLabel('Contraseña', { exact: true }).fill('example-password');
  await page.getByRole('button', { name: 'Crear cuenta', exact: true }).click();
  await expect(page.getByRole('status')).toContainText('Tu cuenta está lista');
  await page.getByLabel('Correo electrónico').fill('ana@example.test');
  await page.getByLabel('Contraseña', { exact: true }).fill('example-password');
  await page.getByRole('button', { name: 'Iniciar sesión', exact: true }).click();
  await expect(page.getByRole('heading', { name: 'Su bienestar comienza' })).toBeVisible();

  await page.getByRole('button', { name: 'Activar modo oscuro' }).click();
  await expect(page.locator('html')).toHaveAttribute('data-theme', 'dark');
  await page.screenshot({ path: testInfo.outputPath('inicio-oscuro.png'), fullPage: true });
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true);
  await openNavigation(page);
  await page.getByRole('link', { name: 'Mis mascotas', exact: true }).click();
  await page.getByRole('button', { name: 'Agregar mascota' }).click();
  await page.getByLabel('Nombre', { exact: true }).fill('Luna');
  await page.getByLabel('Edad en años').fill('3');
  await page.getByRole('button', { name: 'Guardar mascota' }).click();
  await expect(page.getByRole('heading', { name: 'Luna', exact: true })).toBeVisible();
  await page.getByRole('link', { name: 'Encontrar cuidados' }).click();
  await page.screenshot({ path: testInfo.outputPath('catalogo-oscuro.png'), fullPage: true });
  await page.getByRole('link', { name: 'Ver detalle' }).click();
  await page.getByLabel('¿Para quién es este cuidado?').selectOption(petId);
  await page.getByRole('radio').check();
  await page.getByRole('button', { name: 'Solicitar servicio', exact: true }).click();
  await expect(page.getByRole('status').filter({ hasText: 'Solicitud creada' })).toBeVisible();
  await page.getByRole('link', { name: 'Ver historial' }).click();
  await expect(page.getByRole('heading', { name: 'Historial de solicitud' })).toBeVisible();
  await expect(page.getByText('Pendiente', { exact: true })).toBeVisible();
  expect(await page.evaluate(() => Object.keys(localStorage))).toEqual(['petcare_theme']);
  expect(await page.evaluate(() => Object.keys(sessionStorage))).toEqual([]);

  // Same origin in a new tab starts unauthenticated; reloading loses only authentication.
  const otherTab = await page.context().newPage();
  await otherTab.goto('/mascotas');
  await expect(otherTab).toHaveURL(/\/login\?/);
  await otherTab.close();
  await page.reload();
  await expect(page).toHaveURL(/\/login\?/);
  await expect(page.locator('html')).toHaveAttribute('data-theme', 'dark');
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true);
});

test('keeps login values after failure and provides visible field validation', async ({ page }) => {
  await page.route('**/api/auth/login', (route) =>
    route.fulfill({ status: 401, json: { detail: 'Correo o contraseña incorrectos.' } }),
  );
  await page.goto('/login');
  await page.getByRole('button', { name: 'Iniciar sesión', exact: true }).click();
  await expect(page.getByText('Ingresa un correo electrónico válido.')).toBeVisible();
  await page.getByLabel('Correo electrónico').fill('ana@example.test');
  await page.getByLabel('Contraseña', { exact: true }).fill('incorrect-password');
  await page.getByRole('button', { name: 'Iniciar sesión', exact: true }).click();
  await expect(page.getByRole('alert')).toContainText('Correo o contraseña incorrectos.');
  await expect(page.getByLabel('Correo electrónico')).toHaveValue('ana@example.test');
  await expect(page.getByLabel('Contraseña', { exact: true })).toHaveValue('incorrect-password');
});

test('restores a protected link, handles a product conflict and clears authentication on logout', async ({
  page,
}) => {
  let conflicted = false;
  let created = false;
  const secondSlot = {
    ...slot,
    id: '77777777-7777-4777-8777-777777777777',
    startTime: '12:00:00',
    endTime: '13:00:00',
  };
  const product = { ...offering, name: 'Kit de cuidado', type: 'PRODUCTO' };
  const pet = { id: petId, name: 'Luna', typePet: 'GATO', age: 3 };
  await page.route('**/api/**', async (route) => {
    const path = new URL(route.request().url()).pathname;
    if (!path.startsWith('/api/')) return route.continue();
    const reply = (body: unknown, status = 200) => route.fulfill({ status, json: body });
    if (path === '/api/auth/login') return reply(loginResponse());
    if (path === '/api/pets') return reply([pet]);
    if (path === `/api/services/${offeringId}`)
      return reply({ ...product, providerName: 'Clínica PetCare', freeSlots: [slot, secondSlot] });
    if (path === `/api/services/${offeringId}/slots/free`)
      return reply(conflicted ? [secondSlot] : [slot, secondSlot]);
    if (path === '/api/service-requests') {
      const body = route.request().postDataJSON();
      expect(body).not.toHaveProperty('ownerId');
      expect(body.purchaseType).toBe('ENTREGA');
      if (!conflicted) {
        conflicted = true;
        return reply({ detail: 'La disponibilidad cambió. Selecciona otra franja.' }, 409);
      }
      expect(body.slotId).toBe(secondSlot.id);
      created = true;
      return reply(
        {
          ...request,
          offeringName: product.name,
          offeringType: 'PRODUCTO',
          purchaseType: 'ENTREGA',
        },
        201,
      );
    }
    if (path === '/api/service-requests/mine')
      return reply(
        created
          ? [
              {
                ...request,
                offeringName: product.name,
                offeringType: 'PRODUCTO',
                purchaseType: 'ENTREGA',
              },
            ]
          : [],
      );
    return reply({}, 404);
  });
  await page.goto(`/servicios/${offeringId}`);
  await expect(page).toHaveURL(/\/login\?redirect=/);
  await page.getByLabel('Correo electrónico').fill('ana@example.test');
  await page.getByLabel('Contraseña', { exact: true }).fill('example-password');
  await page.getByRole('button', { name: 'Iniciar sesión', exact: true }).click();
  await expect(page).toHaveURL(new RegExp(`/servicios/${offeringId}$`));
  await page.getByLabel('¿Para quién es este cuidado?').selectOption(petId);
  await page.getByLabel('¿Cómo quieres recibirlo?').selectOption('ENTREGA');
  await page.getByRole('radio').first().check();
  await page.getByRole('button', { name: 'Solicitar producto', exact: true }).click();
  await expect(page.getByRole('alert')).toContainText('La disponibilidad cambió');
  await expect(page.getByRole('radio')).toHaveCount(1);
  await page.getByRole('radio').check();
  await page.getByRole('button', { name: 'Solicitar producto', exact: true }).click();
  await expect(page.getByRole('status').filter({ hasText: 'Solicitud creada' })).toBeVisible();
  await openNavigation(page);
  await page.getByRole('button', { name: 'Cerrar sesión' }).click();
  await expect(page).toHaveURL(/\/login\?/);
  await expect(page.getByRole('heading', { name: 'Bienvenido nuevamente' })).toBeVisible();
});

test('keeps navigation usable when collapsed and preserves it across owner routes', async ({
  page,
  isMobile,
}, testInfo) => {
  await page.route('**/api/**', (route) => {
    const path = new URL(route.request().url()).pathname;
    if (!path.startsWith('/api/')) return route.continue();
    if (path === '/api/auth/login') return route.fulfill({ json: loginResponse() });
    if (path === '/api/pets' || path === '/api/service-requests/mine') {
      return route.fulfill({ json: [] });
    }
    return route.fulfill({ status: 404, json: {} });
  });
  await page.goto('/login');
  await page.getByLabel('Correo electrónico').fill('ana@example.test');
  await page.getByLabel('Contraseña', { exact: true }).fill('example-password');
  await page.getByRole('button', { name: 'Iniciar sesión', exact: true }).click();
  await expect(page).toHaveURL(/\/inicio$/);

  const navigation = page.getByRole('navigation', { name: 'Navegación principal' });
  if (isMobile) {
    await expect(navigation).toBeHidden();
    await openNavigation(page);
    await expect(navigation).toBeVisible();
    await navigation.getByRole('link', { name: 'Mis mascotas' }).focus();
    await page.keyboard.press('Escape');
    await expect(page.getByRole('button', { name: 'Mostrar navegación' })).toBeFocused();
    await expect(navigation).toBeHidden();
    await openNavigation(page);
  } else {
    const collapse = page.getByRole('button', { name: 'Contraer barra lateral' });
    await expect(collapse).toBeVisible();
    await collapse.click();
    await expect(page.getByRole('button', { name: 'Expandir barra lateral' })).toHaveAttribute(
      'aria-expanded',
      'false',
    );
    await expect
      .poll(() =>
        page.locator('#owner-sidebar').evaluate((element) => element.getBoundingClientRect().width),
      )
      .toBeLessThan(100);
    await expect(navigation.getByRole('link', { name: 'Mis mascotas' })).toBeVisible();
  }

  await navigation.getByRole('link', { name: 'Mis mascotas' }).click();
  await expect(page).toHaveURL(/\/mascotas$/);
  if (isMobile) {
    await expect(navigation).toBeHidden();
    await openNavigation(page);
  } else {
    const expand = page.getByRole('button', { name: 'Expandir barra lateral' });
    await expect(expand).toBeVisible();
    await page.screenshot({ path: testInfo.outputPath('barra-contraida.png'), fullPage: true });
    await expand.focus();
    await page.keyboard.press('Enter');
    await expect(page.getByRole('button', { name: 'Contraer barra lateral' })).toHaveAttribute(
      'aria-expanded',
      'true',
    );
    await expect
      .poll(() =>
        page.locator('#owner-sidebar').evaluate((element) => element.getBoundingClientRect().width),
      )
      .toBeGreaterThan(200);
  }
  await expect(navigation.getByRole('link', { name: 'Mis mascotas' })).toHaveAttribute(
    'aria-current',
    'page',
  );
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true);

  await navigation.getByRole('link', { name: 'Inicio', exact: true }).click();
  await page.getByRole('link', { name: 'PetCare, página principal' }).click();
  await expect(page).toHaveURL(/\/$/);
  await expect(page.getByRole('link', { name: 'Ir a mi espacio' }).first()).toBeVisible();
  await page.getByRole('link', { name: 'Ir a mi espacio' }).first().click();
  await expect(page).toHaveURL(/\/inicio$/);
});
