import { z } from 'zod';
import { authSession } from '../../features/auth/session';
import { ApiError, requestJson } from './http';

type Options = {
  method?: 'GET' | 'POST' | 'PATCH';
  body?: unknown;
  signal?: AbortSignal;
  public?: boolean;
};
export async function api<T>(
  path: string,
  schema: z.ZodType<T>,
  options: Options = {},
): Promise<T> {
  const generation = authSession.getGeneration();
  const token = options.public ? null : await authSession.accessToken();
  const send = (access: string | null) => {
    options.signal?.throwIfAborted();
    return requestJson(path, {
      method: options.method || 'GET',
      ...(options.body !== undefined ? { body: JSON.stringify(options.body) } : {}),
      signal: options.signal,
      ...(access ? { headers: { Authorization: `Bearer ${access}` } } : {}),
    });
  };
  let data: unknown;
  try {
    data = await send(token);
  } catch (error) {
    if (options.public || !(error instanceof ApiError) || error.status !== 401) throw error;
    authSession.assertGeneration(generation);
    // A delayed 401 may belong to the previous access token: use the already-rotated pair.
    const current = authSession.getToken();
    const fresh = current && current !== token ? current : await authSession.refresh();
    authSession.assertGeneration(generation);
    try {
      data = await send(fresh);
    } catch (retryError) {
      if (
        retryError instanceof ApiError &&
        retryError.status === 401 &&
        generation === authSession.getGeneration()
      )
        authSession.clear();
      throw retryError;
    }
  }
  if (!options.public) authSession.assertGeneration(generation);
  const parsed = schema.safeParse(data);
  if (!parsed.success)
    throw new ApiError(
      'La respuesta del servidor no tiene el formato esperado. Inténtalo de nuevo.',
      0,
    );
  return parsed.data;
}
