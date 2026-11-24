const dateFormat = new Intl.DateTimeFormat('es-CO', {
  day: 'numeric',
  month: 'long',
  year: 'numeric',
  timeZone: 'America/Bogota',
});
export const money = (value: number) =>
  new Intl.NumberFormat('es-CO', {
    style: 'currency',
    currency: 'COP',
    maximumFractionDigits: 0,
  }).format(value);
// A slot is a Bogotá wall-clock date and time, not UTC or the browser's local time.
export const slotDate = (date: string) => dateFormat.format(new Date(`${date}T12:00:00-05:00`));
export const time = (value: string) => value.slice(0, 5);
export const timestamp = (value: string) => {
  // LocalDateTime responses have no offset; interpret them in the backend's Bogotá zone.
  const zoned = /(?:Z|[+-]\d{2}:?\d{2})$/i.test(value) ? value : `${value}-05:00`;
  const date = new Date(zoned);
  return Number.isNaN(date.getTime())
    ? 'Fecha no disponible'
    : new Intl.DateTimeFormat('es-CO', {
        dateStyle: 'medium',
        timeStyle: 'short',
        timeZone: 'America/Bogota',
      }).format(date);
};
export const isFutureSlot = (date: string, startTime: string) =>
  new Date(`${date}T${startTime}-05:00`).getTime() > Date.now();
