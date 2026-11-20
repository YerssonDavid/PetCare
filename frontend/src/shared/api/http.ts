export const API_BASE = (import.meta.env.VITE_API_BASE_URL || '/api').replace(/\/$/, '');

export class ApiError extends Error {
  constructor(
    message: string,
    public readonly status: number,
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

export async function requestJson(path: string, options: RequestInit = {}): Promise<unknown> {
  let response: Response;
  try {
    response = await fetch(`${API_BASE}${path}`, {
      ...options,
      headers: {
        Accept: 'application/json, application/problem+json',
        ...(options.body ? { 'Content-Type': 'application/json' } : {}),
        ...options.headers,
      },
    });
  } catch (error) {
    if (error instanceof Error && error.name === 'AbortError') throw error;
    throw new ApiError(
      'No pudimos conectar con PetCare. Comprueba tu conexión e inténtalo de nuevo.',
      0,
    );
  }
  const text = await response.text();
  let data: unknown = null;
  try {
    data = text ? JSON.parse(text) : null;
  } catch {
    if (response.ok)
      throw new ApiError('El servidor devolvió una respuesta que no pudimos interpretar.', 0);
  }
  if (!response.ok) {
    const problem = data && typeof data === 'object' ? (data as Record<string, unknown>) : {};
    const detail =
      typeof problem.detail === 'string'
        ? problem.detail
        : typeof problem.title === 'string'
          ? problem.title
          : null;
    throw new ApiError(
      detail ||
        (response.status === 403
          ? 'Tu cuenta no tiene permiso para realizar esta acción.'
          : `No pudimos completar la operación (${response.status}).`),
      response.status,
    );
  }
  return data;
}
