import { afterEach, describe, expect, it, vi } from 'vitest';
import { z } from 'zod';
import { authSession } from '../../features/auth/session';
import { deferred, tokenPair } from '../../test/tokens';
import { api } from './client';

const credentials = { email: 'owner@example.test', password: 'example-password' };
const json = (body: unknown, status = 200) =>
  new Response(JSON.stringify(body), { status, headers: { 'Content-Type': 'application/json' } });
afterEach(() => authSession.clear());

describe('authenticated HTTP client', () => {
  it('coordinates parallel 401s and retries with the rotated access token', async () => {
    const initial = tokenPair();
    const rotated = tokenPair({ suffix: 'rotated' });
    const fetchMock = vi.fn(async (url: string, options: RequestInit) => {
      if (url.endsWith('/auth/login')) return json(initial);
      if (url.endsWith('/auth/refresh')) return json(rotated);
      return (options.headers as Record<string, string>).Authorization === `Bearer ${initial.token}`
        ? json({ detail: 'Expirado' }, 401)
        : json([]);
    });
    vi.stubGlobal('fetch', fetchMock);
    await authSession.login(credentials);
    await Promise.all([
      api('/pets', z.array(z.unknown())),
      api('/service-requests/mine', z.array(z.unknown())),
    ]);
    expect(fetchMock.mock.calls.filter(([url]) => url.endsWith('/auth/refresh'))).toHaveLength(1);
    expect(authSession.getToken()).toBe(rotated.token);
  });

  it('reuses an already-rotated token for a delayed 401 without consuming refresh twice', async () => {
    const waiting = deferred<Response>();
    const initial = tokenPair();
    const rotated = tokenPair({ suffix: 'rotated' });
    let delayed = true;
    const fetchMock = vi.fn(async (url: string, options: RequestInit) => {
      if (url.endsWith('/auth/login')) return json(initial);
      if (url.endsWith('/auth/refresh')) return json(rotated);
      if (url.endsWith('/slow') && delayed) {
        delayed = false;
        return waiting.promise;
      }
      return (options.headers as Record<string, string>).Authorization === `Bearer ${initial.token}`
        ? json({}, 401)
        : json([]);
    });
    vi.stubGlobal('fetch', fetchMock);
    await authSession.login(credentials);
    const slow = api('/slow', z.array(z.unknown()));
    await api('/pets', z.array(z.unknown()));
    waiting.resolve(json({}, 401));
    await slow;
    expect(fetchMock.mock.calls.filter(([url]) => url.endsWith('/auth/refresh'))).toHaveLength(1);
  });

  it('does not refresh forbidden requests and retains the authenticated identity', async () => {
    const fetchMock = vi.fn(async (url: string) =>
      url.endsWith('/auth/login') ? json(tokenPair()) : json({ detail: 'No tienes permiso' }, 403),
    );
    vi.stubGlobal('fetch', fetchMock);
    await authSession.login(credentials);
    await expect(api('/pets', z.array(z.unknown()))).rejects.toMatchObject({ status: 403 });
    expect(fetchMock).toHaveBeenCalledTimes(2);
    expect(authSession.getSnapshot()).not.toBeNull();
  });

  it('stops after one retry and clears authentication on a second 401', async () => {
    const fetchMock = vi.fn(async (url: string) =>
      url.endsWith('/auth/login')
        ? json(tokenPair())
        : url.endsWith('/auth/refresh')
          ? json(tokenPair({ suffix: 'rotated' }))
          : json({}, 401),
    );
    vi.stubGlobal('fetch', fetchMock);
    await authSession.login(credentials);
    await expect(api('/pets', z.array(z.unknown()))).rejects.toMatchObject({ status: 401 });
    expect(fetchMock).toHaveBeenCalledTimes(4);
    expect(authSession.getSnapshot()).toBeNull();
  });

  it('discards a successful response from an account that has logged out', async () => {
    const waiting = deferred<Response>();
    vi.stubGlobal(
      'fetch',
      vi.fn(async (url: string) =>
        url.endsWith('/auth/login') ? json(tokenPair()) : waiting.promise,
      ),
    );
    await authSession.login(credentials);
    const pending = api('/pets', z.array(z.unknown()));
    await Promise.resolve();
    authSession.clear();
    waiting.resolve(json([]));
    await expect(pending).rejects.toMatchObject({ status: 401 });
  });
});
