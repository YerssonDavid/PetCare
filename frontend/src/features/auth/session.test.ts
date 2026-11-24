import { describe, expect, it, vi } from 'vitest';
import { ApiError } from '../../shared/api/http';
import { deferred, ownerId, tokenPair } from '../../test/tokens';
import { createAuthSession } from './session';

const credentials = { email: 'owner@example.test', password: 'example-password' };

describe('in-memory owner authentication', () => {
  it('uses sub for the account and role from the access token, never jti', async () => {
    const session = createAuthSession(vi.fn().mockResolvedValue(tokenPair()));
    await session.login(credentials);
    expect(session.getSnapshot()).toMatchObject({ id: ownerId, role: 'USER' });
    expect(session.getSnapshot()?.id).not.toBe('token-initial');
  });

  it('shares a single renewal and replaces the refresh token for its next use', async () => {
    const waiting = deferred<unknown>();
    const transport = vi
      .fn()
      .mockResolvedValueOnce(tokenPair())
      .mockReturnValueOnce(waiting.promise)
      .mockResolvedValueOnce(tokenPair({ suffix: 'third' }));
    const session = createAuthSession(transport);
    await session.login(credentials);
    const first = session.refresh();
    const second = session.refresh();
    expect(first).toBe(second);
    waiting.resolve(tokenPair({ suffix: 'rotated' }));
    await Promise.all([first, second]);
    await session.refresh();
    expect(transport).toHaveBeenCalledTimes(3);
    expect(JSON.parse(transport.mock.calls[2]?.[1].body as string)).toEqual({
      refreshToken: tokenPair({ suffix: 'rotated' }).refreshToken,
    });
  });

  it('does not restore authentication when logout happens during renewal', async () => {
    const waiting = deferred<unknown>();
    const transport = vi
      .fn()
      .mockResolvedValueOnce(tokenPair())
      .mockReturnValueOnce(waiting.promise);
    const session = createAuthSession(transport);
    await session.login(credentials);
    const pending = session.refresh();
    session.clear();
    waiting.resolve(tokenPair({ suffix: 'late' }));
    await expect(pending).rejects.toThrow('La sesión cambió');
    expect(session.getSnapshot()).toBeNull();
    expect(session.getToken()).toBeNull();
  });

  it('does not let a previous renewal overwrite a different account', async () => {
    const waiting = deferred<unknown>();
    const otherId = '22222222-2222-4222-8222-222222222222';
    const transport = vi
      .fn()
      .mockResolvedValueOnce(tokenPair())
      .mockReturnValueOnce(waiting.promise)
      .mockResolvedValueOnce(tokenPair({ sub: otherId, suffix: 'other' }));
    const session = createAuthSession(transport);
    await session.login(credentials);
    const pending = session.refresh();
    await session.login(credentials);
    waiting.resolve(tokenPair({ suffix: 'late' }));
    await expect(pending).rejects.toThrow();
    expect(session.getSnapshot()?.id).toBe(otherId);
  });

  it('clears authentication on an ambiguous refresh failure without retrying the token', async () => {
    const transport = vi
      .fn()
      .mockResolvedValueOnce(tokenPair())
      .mockRejectedValueOnce(new ApiError('Sin conexión', 0));
    const session = createAuthSession(transport);
    await session.login(credentials);
    await expect(session.refresh()).rejects.toThrow('Sin conexión');
    await expect(session.refresh()).rejects.toThrow('Inicia sesión');
    expect(transport).toHaveBeenCalledTimes(2);
    expect(session.getSnapshot()).toBeNull();
  });

  it('accepts provider accounts and rejects malformed login responses without retaining tokens', async () => {
    const session = createAuthSession(
      vi
        .fn()
        .mockResolvedValueOnce(tokenPair({ role: 'PROVIDER' }))
        .mockResolvedValueOnce({ token: 'invalid' }),
    );
    await session.login(credentials);
    expect(session.getSnapshot()?.role).toBe('PROVIDER');
    await expect(session.login(credentials)).rejects.toThrow('tokens');
    expect(session.getToken()).toBeNull();
  });
});
