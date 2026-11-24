import { describe, expect, it } from 'vitest';
import { requestFormSchema } from './validation';

const selection = {
  petId: '11111111-1111-4111-8111-111111111111',
  slotId: '22222222-2222-4222-8222-222222222222',
};
describe('service and product requests', () => {
  it('requires a purchase method for products but rejects it for services', () => {
    expect(requestFormSchema(true).safeParse(selection).success).toBe(false);
    expect(
      requestFormSchema(true).safeParse({ ...selection, purchaseType: 'ENTREGA' }).success,
    ).toBe(true);
    expect(requestFormSchema(false).safeParse(selection).success).toBe(true);
    expect(
      requestFormSchema(false).safeParse({ ...selection, purchaseType: 'RETIRO' }).success,
    ).toBe(false);
  });
});
