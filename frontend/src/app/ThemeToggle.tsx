import { Icon } from '../shared/ui/Icon';
import { useTheme } from './theme-context';
import styles from '../shared/ui/ui.module.css';

export function ThemeToggle() {
  const { theme, toggleTheme } = useTheme();
  const label = theme === 'dark' ? 'Activar modo claro' : 'Activar modo oscuro';
  return (
    <button
      type="button"
      className={styles.iconButton}
      aria-label={label}
      title={label}
      onClick={toggleTheme}
    >
      <Icon name={theme === 'dark' ? 'sun' : 'moon'} />
    </button>
  );
}
