import { NavLink, Outlet, useLocation } from 'react-router-dom';
import { authSession } from '../features/auth/session';
import { Brand } from '../shared/ui/Brand';
import { Icon } from '../shared/ui/Icon';
import { ThemeSelect } from './ThemeSelect';
import styles from './Shell.module.css';

const navigation = [
  { path: '/inicio', label: 'Inicio', icon: 'home' },
  { path: '/mascotas', label: 'Mis mascotas', icon: 'paw' },
  { path: '/servicios', label: 'Servicios y productos', icon: 'services' },
  { path: '/solicitudes', label: 'Mis solicitudes', icon: 'calendar' },
] as const;

export function Shell({ ownerId }: { ownerId: string }) {
  const location = useLocation();
  const current = navigation.find((item) => location.pathname.startsWith(item.path));
  return (
    <div className={styles.shell}>
      <aside className={styles.sidebar}>
        <NavLink to="/inicio" className={styles.brandLink} aria-label="PetCare, inicio">
          <Brand />
        </NavLink>
        <span className={styles.navLabel}>MI ESPACIO</span>
        <nav className={styles.navigation} aria-label="Navegación principal">
          {navigation.map((item) => (
            <NavLink key={item.path} to={item.path}>
              <Icon name={item.icon} />
              <span>{item.label}</span>
            </NavLink>
          ))}
        </nav>
        <div className={styles.sidebarBottom}>
          <div className={styles.careNote}>
            <Icon name="heart" />
            <p>
              Pequeños cuidados.
              <br />
              <strong>Grandes compañeros.</strong>
            </p>
          </div>
          <button className={styles.logout} onClick={() => authSession.clear()}>
            <Icon name="logout" />
            Cerrar sesión
          </button>
          <span className={styles.sidebarFoot}>PetCare · Hecho para cuidarlos</span>
        </div>
      </aside>
      <div className={styles.workspace}>
        <header className={styles.header}>
          <div className={styles.breadcrumb}>
            Mi espacio <span>/</span> <strong>{current?.label || 'Inicio'}</strong>
          </div>
          <div className={styles.headerRight}>
            <ThemeSelect />
            <span className={styles.account}>
              <Icon name="user" size={17} />
              <span>Propietario</span>
            </span>
          </div>
        </header>
        <main id="main-content" tabIndex={-1} className={styles.main}>
          <Outlet context={{ ownerId }} />
        </main>
        <footer className={styles.footer}>
          <span>PetCare · El cuidado de tu mascota, en un solo lugar.</span>
          <span>Horarios de atención en hora de Bogotá</span>
        </footer>
      </div>
    </div>
  );
}
