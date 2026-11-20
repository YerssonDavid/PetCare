import { jwtDecode } from 'jwt-decode';
import { z } from 'zod';
import { ApiError, requestJson } from '../../shared/api/http';
import type { components } from '../../shared/api/generated';

type Credentials = components['schemas']['LoginRequest'];
type Transport = typeof requestJson;
const tokensSchema = z.object({ token: z.string().min(1), refreshToken: z.string().min(1) });
const claimsSchema = z.object({
  sub: z.string().min(1),
  role: z.enum(['USER', 'PROVIDER']),
  exp: z.number().int().positive(),
});
export type OwnerSession = { id: string; role: 'USER'; expiresAt: number };

/** A session is intentionally confined to this application instance, never browser storage. */
export function createAuthSession(transport: Transport = requestJson) {
  let tokens: z.infer<typeof tokensSchema> | null = null;
  let snapshot: OwnerSession | null = null;
  let generation = 0;
  let renewal: { generation: number; promise: Promise<string> } | null = null;
  const listeners = new Set<() => void>();
  const emit = () => listeners.forEach((listener) => listener());
  const assertGeneration = (expected: number) => {
    if (expected !== generation)
      throw new ApiError('La sesión cambió. Vuelve a iniciar sesión para continuar.', 401);
  };
  const clear = () => {
    generation++;
    tokens = null;
    snapshot = null;
    renewal = null;
    emit();
  };
  const store = (data: unknown) => {
    const parsed = tokensSchema.safeParse(data);
    if (!parsed.success)
      throw new ApiError('El servidor no devolvió los tokens de autenticación esperados.', 0);
    let claims: z.infer<typeof claimsSchema>;
    try {
      claims = claimsSchema.parse(jwtDecode(parsed.data.token));
    } catch {
      throw new ApiError('El token recibido no contiene una identidad y un rol válidos.', 401);
    }
    if (claims.role !== 'USER')
      throw new ApiError(
        'Esta versión de PetCare está disponible para propietarios. Inicia sesión con una cuenta de propietario.',
        403,
      );
    if (claims.exp * 1000 <= Date.now())
      throw new ApiError('El token recibido ya ha expirado. Vuelve a iniciar sesión.', 401);
    if (snapshot && snapshot.id !== claims.sub)
      throw new ApiError('La identidad de la cuenta cambió durante la renovación.', 401);
    tokens = parsed.data;
    snapshot = { id: claims.sub, role: claims.role, expiresAt: claims.exp * 1000 };
    emit();
    return parsed.data.token;
  };
  const refresh = (): Promise<string> => {
    if (renewal?.generation === generation) return renewal.promise;
    if (!tokens) return Promise.reject(new ApiError('Inicia sesión para continuar.', 401));
    const expected = generation;
    const refreshToken = tokens.refreshToken;
    const promise = (async () => {
      try {
        const data = await transport('/auth/refresh', {
          method: 'POST',
          body: JSON.stringify({ refreshToken }),
        });
        assertGeneration(expected);
        return store(data);
      } catch (error) {
        // A consumed refresh token is never retried, even after an ambiguous network failure.
        if (expected === generation) clear();
        throw error;
      } finally {
        if (renewal?.generation === expected) renewal = null;
      }
    })();
    renewal = { generation: expected, promise };
    return promise;
  };
  return {
    subscribe(listener: () => void) {
      listeners.add(listener);
      return () => {
        listeners.delete(listener);
      };
    },
    getSnapshot: () => snapshot,
    getGeneration: () => generation,
    getToken: () => tokens?.token ?? null,
    assertGeneration,
    clear,
    refresh,
    async accessToken() {
      if (!tokens || !snapshot) throw new ApiError('Inicia sesión para continuar.', 401);
      return snapshot.expiresAt - Date.now() < 20_000 ? refresh() : tokens.token;
    },
    async login(credentials: Credentials) {
      clear();
      const expected = generation;
      const data = await transport('/auth/login', {
        method: 'POST',
        body: JSON.stringify(credentials),
      });
      assertGeneration(expected);
      store(data);
    },
  };
}

export const authSession = createAuthSession();
