import { z } from 'zod';

export const requestFormSchema = (product: boolean) =>
  z
    .object({
      petId: z.uuid('Selecciona una mascota.'),
      slotId: z.uuid('Selecciona una franja disponible.'),
      purchaseType: z.enum(['RETIRO', 'ENTREGA']).optional(),
    })
    .superRefine((values, context) => {
      if (product && !values.purchaseType)
        context.addIssue({
          code: 'custom',
          path: ['purchaseType'],
          message: 'Elige retiro o entrega.',
        });
      if (!product && values.purchaseType)
        context.addIssue({
          code: 'custom',
          path: ['purchaseType'],
          message: 'Esta opción solo corresponde a productos.',
        });
    });
