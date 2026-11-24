import { expect, test } from '@playwright/test';

const providerId = '11111111-1111-4111-8111-111111111111';
const offeringId = '22222222-2222-4222-8222-222222222222';
const requestId = '33333333-3333-4333-8333-333333333333';
const petId = '44444444-4444-4444-8444-444444444444';
const offering = {
  id: offeringId,
  providerId,
  name: 'Consulta',
  description: 'Consulta general',
  price: 10000,
  category: 'VETERINARIA',
  type: 'SERVICIO',
};
const pet = { id: petId, name: 'Luna', typePet: 'PERRO', age: 3 };
const received = {
  id: requestId,
  pet,
  ownerEmail: 'owner@example.com',
  offeringName: 'Consulta',
  offeringDescription: 'Consulta general',
  offeringType: 'SERVICIO',
  date: '2099-12-20',
  startTime: '10:00:00',
  endTime: '11:00:00',
  price: 10000,
  status: 'PENDIENTE',
  requestedAt: '2026-11-19T10:00:00',
};

test('provider can register, publish, create a slot and respond to a request', async ({ page }) => {
  let published = false;
  let accepted = false;
  await page.route('**/api/**', async (route) => {
    const path = new URL(route.request().url()).pathname.replace('/api', '');
    const method = route.request().method();
    const reply = (body: unknown, status = 200) => route.fulfill({ status, json: body });
    if (path === '/registry/provider') {
      expect(route.request().postDataJSON()).toMatchObject({
        nameCommercial: 'Clínica',
        typeProvider: 'CLINICA',
        experienceYears: 4,
      });
      return reply({ id: providerId }, 201);
    }
    if (path === '/auth/login') {
      const claims = Buffer.from(
        JSON.stringify({
          sub: providerId,
          role: 'PROVIDER',
          exp: Math.floor(Date.now() / 1000) + 3600,
        }),
      ).toString('base64url');
      return reply({ token: `e30.${claims}.signature`, refreshToken: 'refresh' });
    }
    expect(route.request().headers().authorization).toMatch(/^Bearer /);
    if (path === '/services' && method === 'GET') return reply(published ? [offering] : []);
    if (path === '/services' && method === 'POST') {
      expect(route.request().postDataJSON()).toMatchObject({
        name: 'Consulta',
        price: 10000,
        type: 'SERVICIO',
      });
      published = true;
      return reply({ id: offeringId }, 201);
    }
    if (path === `/services/${offeringId}/slots`) {
      expect(route.request().postDataJSON()).toEqual({
        date: '2099-12-20',
        startTime: '10:00',
        endTime: '11:00',
      });
      return reply({ id: offeringId }, 201);
    }
    if (path === '/service-requests/received')
      return reply([{ ...received, status: accepted ? 'ACEPTADO' : 'PENDIENTE' }]);
    if (path === `/service-requests/${requestId}/accept`) {
      expect(method).toBe('PATCH');
      accepted = true;
      return reply({
        id: requestId,
        petName: pet.name,
        offeringName: 'Consulta',
        offeringType: 'SERVICIO',
        providerName: 'Clínica',
        date: received.date,
        startTime: received.startTime,
        endTime: received.endTime,
        price: 10000,
        status: 'ACEPTADO',
        requestedAt: received.requestedAt,
      });
    }
    return reply({ detail: `Ruta no prevista: ${method} ${path}` }, 404);
  });

  await page.goto('/registro/proveedor');
  await page.getByLabel('Nombre comercial').fill('Clínica');
  await page.getByLabel('Tipo de proveedor').selectOption('CLINICA');
  await page.getByLabel('Correo electrónico').fill('vet@example.com');
  await page.getByLabel('Teléfono').fill('3001234567');
  await page.getByLabel('Contraseña').fill('password123');
  await page.getByLabel('Ciudad').fill('Bogotá');
  await page.getByLabel('Años de experiencia').fill('4');
  await page.getByRole('button', { name: 'Crear cuenta de proveedor' }).click();
  await expect(page).toHaveURL(/\/login\?registro=proveedor/);
  await page.getByLabel('Correo electrónico').fill('vet@example.com');
  await page.getByLabel('Contraseña').fill('password123');
  await page.getByRole('button', { name: 'Iniciar sesión', exact: true }).click();
  await expect(page).toHaveURL(/\/proveedor$/);
  await page.getByRole('link', { name: 'Mis publicaciones y horarios' }).click();
  await page.getByLabel('Nombre', { exact: true }).fill('Consulta');
  await page.getByLabel('Descripción', { exact: true }).fill('Consulta general');
  await page.getByLabel('Precio').fill('10000');
  await page.getByRole('button', { name: 'Publicar', exact: true }).click();
  await expect(page.getByText('Publicación creada correctamente.')).toBeVisible();
  await page.getByLabel('Fecha', { exact: true }).fill('2099-12-20');
  await page.getByLabel('Hora de inicio').fill('10:00');
  await page.getByLabel('Hora de finalización').fill('11:00');
  await page.getByRole('button', { name: 'Crear franja' }).click();
  await expect(page.getByText('Franja creada correctamente.')).toBeVisible();
  const toggle = page.getByRole('button', { name: 'Mostrar navegación' });
  if (await toggle.isVisible()) await toggle.click();
  await page.getByRole('link', { name: 'Solicitudes recibidas', exact: true }).click();
  await page.getByRole('button', { name: 'Aceptar', exact: true }).click();
  await expect(page.getByText('Estado: Aceptada')).toBeVisible();
});
