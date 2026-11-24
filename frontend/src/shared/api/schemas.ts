import { z } from 'zod';

export const petTypes = [
  'PERRO',
  'GATO',
  'AVE',
  'CONEJO',
  'HAMSTER',
  'PEZ',
  'REPTIL',
  'OTRO',
] as const;
export const categories = ['VETERINARIA', 'PELUQUERIA', 'PASEO', 'CUIDADO_TEMPORAL'] as const;
export const statuses = ['PENDIENTE', 'ACEPTADO', 'RECHAZADO'] as const;
export const categoryLabels: Record<(typeof categories)[number], string> = {
  VETERINARIA: 'Veterinaria',
  PELUQUERIA: 'Peluquería',
  PASEO: 'Paseos',
  CUIDADO_TEMPORAL: 'Cuidado temporal',
};
export const petLabels: Record<(typeof petTypes)[number], string> = {
  PERRO: 'Perro',
  GATO: 'Gato',
  AVE: 'Ave',
  CONEJO: 'Conejo',
  HAMSTER: 'Hámster',
  PEZ: 'Pez',
  REPTIL: 'Reptil',
  OTRO: 'Otro',
};
export const statusLabels: Record<(typeof statuses)[number], string> = {
  PENDIENTE: 'Pendiente',
  ACEPTADO: 'Aceptada',
  RECHAZADO: 'Rechazada',
};
const optionalText = z.string().nullish();
export const registerResponseSchema = z.object({ id: z.uuid(), message: optionalText });
export const petSchema = z.object({
  id: z.uuid(),
  name: z.string(),
  typePet: z.enum(petTypes),
  age: z.number().int().nonnegative(),
  description: optionalText,
});
export const slotSchema = z.object({
  id: z.uuid(),
  date: z.iso.date(),
  startTime: z.string(),
  endTime: z.string(),
});
export const offeringSchema = z.object({
  id: z.uuid(),
  providerId: z.uuid(),
  name: z.string(),
  description: z.string(),
  conditions: optionalText,
  price: z.number().nonnegative(),
  category: z.enum(categories),
  type: z.enum(['SERVICIO', 'PRODUCTO']),
  imageUrl: optionalText,
});
export const offeringDetailSchema = offeringSchema.extend({
  providerName: z.string(),
  providerPhone: optionalText,
  freeSlots: z.array(slotSchema),
});
export const requestSchema = z.object({
  id: z.uuid(),
  petName: z.string(),
  offeringName: z.string(),
  offeringType: z.enum(['SERVICIO', 'PRODUCTO']),
  providerName: z.string(),
  date: z.iso.date(),
  startTime: z.string(),
  endTime: z.string(),
  price: z.number().nonnegative(),
  status: z.enum(statuses),
  purchaseType: z.enum(['RETIRO', 'ENTREGA']).nullish(),
  rejectionReason: optionalText,
  requestedAt: z.string(),
});
export const historySchema = z.object({
  requestId: z.uuid(),
  offeringName: z.string(),
  offeringDescription: z.string(),
  offeringType: z.enum(['SERVICIO', 'PRODUCTO']),
  pet: petSchema,
  entries: z.array(
    z.object({ status: z.enum(statuses), reason: optionalText, changedAt: z.string() }),
  ),
});
export const receivedRequestSchema = z.object({
  id: z.uuid(),
  pet: petSchema,
  ownerEmail: z.string(),
  offeringName: z.string(),
  offeringDescription: z.string(),
  offeringType: z.enum(['SERVICIO', 'PRODUCTO']),
  date: z.iso.date(),
  startTime: z.string(),
  endTime: z.string(),
  price: z.number().nonnegative(),
  status: z.enum(statuses),
  purchaseType: z.enum(['RETIRO', 'ENTREGA']).nullish(),
  rejectionReason: optionalText,
  requestedAt: z.string(),
});
export type Pet = z.infer<typeof petSchema>;
export type Offering = z.infer<typeof offeringSchema>;
export type ServiceRequest = z.infer<typeof requestSchema>;
