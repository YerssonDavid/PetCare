import { useEffect, useId, useState } from 'react';
import { Icon } from '../shared/ui/Icon';
import styles from '../shared/ui/ui.module.css';

type Theme = 'light' | 'dark' | 'system';
function initialTheme(): Theme {
  try {
    const stored = localStorage.getItem('petcare_theme');
    return stored === 'light' || stored === 'dark' ? stored : 'system';
  } catch {
    return 'system';
  }
}

export function ThemeSelect() {
  const [theme, setTheme] = useState<Theme>(initialTheme);
  const id = useId();
  useEffect(() => {
    const media = matchMedia('(prefers-color-scheme: dark)');
    const apply = () => {
      document.documentElement.dataset.theme =
        theme === 'system' ? (media.matches ? 'dark' : 'light') : theme;
    };
    apply();
    media.addEventListener('change', apply);
    return () => media.removeEventListener('change', apply);
  }, [theme]);
  return (
    <div className={styles.theme}>
      <Icon name={theme === 'dark' ? 'moon' : theme === 'light' ? 'sun' : 'monitor'} size={17} />
      <label htmlFor={id}>Tema</label>
      <select
        id={id}
        value={theme}
        onChange={(event) => {
          const next = event.target.value as Theme;
          setTheme(next);
          try {
            localStorage.setItem('petcare_theme', next);
          } catch {
            /* The theme still works when storage is unavailable. */
          }
        }}
      >
        <option value="system">Sistema</option>
        <option value="light">Claro</option>
        <option value="dark">Oscuro</option>
      </select>
    </div>
  );
}
