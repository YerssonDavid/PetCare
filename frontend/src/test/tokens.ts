export const ownerId = '11111111-1111-4111-8111-111111111111';
export function tokenPair({ sub = ownerId, role = 'USER', suffix = 'initial' } = {}) {
  const encode = (payload: object) => `${btoa('{}')}.${btoa(JSON.stringify(payload))}.${suffix}`;
  return {
    token: encode({ sub, role, jti: `token-${suffix}`, exp: Math.floor(Date.now() / 1000) + 3600 }),
    refreshToken: encode({
      sub,
      jti: `refresh-${suffix}`,
      exp: Math.floor(Date.now() / 1000) + 604800,
    }),
  };
}

export function deferred<T>() {
  let resolve!: (value: T) => void;
  let reject!: (reason: unknown) => void;
  const promise = new Promise<T>((accept, decline) => {
    resolve = accept;
    reject = decline;
  });
  return { promise, resolve, reject };
}
