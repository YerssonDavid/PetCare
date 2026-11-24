const paths = {
  home: 'm3 10 9-7 9 7v10a1 1 0 0 1-1 1h-5v-7H9v7H4a1 1 0 0 1-1-1Z',
  paw: 'M8 14c-2 1-3 3-2 5 1 2 3 1 6 1s5 1 6-1c1-2 0-4-2-5l-2-2h-4ZM5 7a2 3 0 1 0 0 .1M10 4a2 3 0 1 0 0 .1M16 4a2 3 0 1 0 0 .1M21 8a2 3 0 1 0 0 .1',
  services: 'M9 3h6v6h6v6h-6v6H9v-6H3V9h6Z',
  calendar:
    'M8 2v4M16 2v4M3 10h18M5 4h14a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2ZM8 14h2m4 0h2m-8 4h2',
  arrow: 'M5 12h14m-6-6 6 6-6 6',
  back: 'M19 12H5m6-6-6 6 6 6',
  plus: 'M12 5v14M5 12h14',
  logout: 'M9 4H4v16h5m5-13 5 5-5 5m-5-5h13',
  sun: 'M12 3V1m0 22v-2M3 12H1m22 0h-2M5 5 3.5 3.5m17 17L19 19M5 19l-1.5 1.5m17-17L19 5M16 12a4 4 0 1 0-8 0 4 4 0 0 0 8 0',
  moon: 'M20.5 13A8.5 8.5 0 0 1 11 3.5 8.5 8.5 0 1 0 20.5 13Z',
  collapse: 'M4 3h16v18H4ZM9 3v18m8-12-3 3 3 3',
  expand: 'M4 3h16v18H4ZM9 3v18m5-12 3 3-3 3',
  menu: 'M4 6h16M4 12h16M4 18h16',
  logo: 'M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20zm0 8a8 8 0 1 0 0 16 8 8 0 0 0 0-16zM9 11c1.66 0 3 1.34 3 3s-1.34 3-3 3-3-1.34-3-3 1.34-3 3-3m3.5-.5c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5z',
  search: 'M10 17a7 7 0 1 0 0-14 7 7 0 0 0 0 14m5-2 6 6',
  clock: 'M12 8v5l3 2m6-3a9 9 0 1 0-18 0 9 9 0 0 0 18 0',
  phone: 'm5 3 4 4-2 3c1 3 4 6 7 7l3-2 4 4-3 3C10 22 2 14 2 6Z',
  check: 'm5 12 4 4L19 6',
  close: 'm6 6 12 12M6 18 18 6',
  bag: 'M4 7h16l1 14H3ZM8 7V5a4 4 0 0 1 8 0v2',
  heart:
    'M20.8 4.6a5.5 5.5 0 0 0-7.8 0l-1 1-1-1a5.5 5.5 0 0 0-7.8 7.8L12 21l8.8-8.6a5.5 5.5 0 0 0 0-7.8Z',
  user: 'M16 7a4 4 0 1 0-8 0 4 4 0 0 0 8 0M4 21v-2a8 8 0 0 1 16 0v2',
} as const;

export function Icon({ name, size = 20 }: { name: keyof typeof paths; size?: number }) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.7"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <path d={paths[name]} />
    </svg>
  );
}
