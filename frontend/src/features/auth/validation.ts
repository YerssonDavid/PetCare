import { z } from 'zod';

export const loginSchema = z.object({
  email: z.string().trim().pipe(z.email('Ingresa un correo electrónico válido.')),
  password: z.string().min(1, 'Ingresa tu contraseña.'),
});
export const registerSchema = z.object({
  name: z.string().trim().min(1, 'Ingresa tu nombre.').max(100, 'Usa como máximo 100 caracteres.'),
  email: z.string().trim().max(150).pipe(z.email('Ingresa un correo electrónico válido.')),
  phone: z
    .string()
    .trim()
    .regex(/^\+?[0-9\s()-]{7,25}$/, 'Ingresa un teléfono válido de 7 a 25 caracteres.'),
  password: z
    .string()
    .min(8, 'Usa al menos 8 caracteres.')
    .max(72, 'Usa como máximo 72 caracteres.'),
});
