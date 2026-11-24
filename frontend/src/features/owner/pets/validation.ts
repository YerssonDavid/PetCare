import { z } from 'zod';
import { petTypes } from '../../../shared/api/schemas';

export const petFormSchema = z.object({
  name: z
    .string()
    .trim()
    .min(1, 'Ingresa el nombre de tu mascota.')
    .max(100, 'Usa como máximo 100 caracteres.'),
  typePet: z.enum(petTypes),
  age: z
    .number({ error: 'Ingresa la edad en años.' })
    .int('Ingresa un número entero.')
    .min(0, 'La edad no puede ser negativa.'),
  description: z.string().trim().max(500, 'Usa como máximo 500 caracteres.'),
});
