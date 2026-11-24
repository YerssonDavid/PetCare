import { afterEach, describe, expect, it, vi } from 'vitest';
import { isFutureSlot, slotDate, timestamp } from './format';

afterEach(() => vi.useRealTimers());
describe('Bogotá date handling', () => {
  it('compares slot start times in Bogotá instead of the browser timezone', () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date('2026-11-19T15:00:00Z'));
    expect(isFutureSlot('2026-11-19', '10:01:00')).toBe(true);
    expect(isFutureSlot('2026-11-19', '10:00:00')).toBe(false);
    expect(isFutureSlot('2026-11-19', '09:59:00')).toBe(false);
  });
  it('keeps the calendar day and interprets unzoned history timestamps in Bogotá', () => {
    expect(slotDate('2026-11-19')).toContain('19 de noviembre');
    expect(timestamp('2026-11-19T10:00:00')).toBe(timestamp('2026-11-19T15:00:00Z'));
  });
});
