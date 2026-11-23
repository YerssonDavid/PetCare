import { useRef, useState } from 'react';
import { Link, NavLink, Outlet, useLocation } from 'react-router-dom';
import { authSession } from '../features/auth/session';
import { Brand } from '../shared/ui/Brand';
import { Icon } from '../shared/ui/Icon';
import { ThemeToggle } from './ThemeToggle';
import ui from '../shared/ui/ui.module.css';
import styles from './Shell.module.css';

const navigation = [
  { path: '/inicio', label: 'Inicio', icon: 'home' },
  { path: '/mascotas', label: 'Mis mascotas', icon: 'paw' },
  { path: '/servicios', label: 'Servicios y productos', icon: 'services' },
  { path: '/solicitudes', label: 'Mis solicitudes', icon: 'calendar' },
] as const;

export function Shell({ ownerId }: { ownerId: string }) {
  const [collapsed, setCollapsed] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);
  const mobileToggle = useRef<HTMLButtonElement>(null);
  const location = useLocation();
  const current = navigation.find((item) => location.pathname.startsWith(item.path));
  const collapseLabel = collapsed ? 'Expandir barra lateral' : 'Contraer barra lateral';
  return (
    <div className={styles.shell} data-collapsed={collapsed}>
      <aside
        id="owner-sidebar"
        className={styles.sidebar}
        data-mobile-open={mobileOpen}
        onKeyDown={(event) => {
          if (event.key === 'Escape' && mobileOpen) {
            setMobileOpen(false);
            mobileToggle.current?.focus();
          }
        }}
      >
        <div className={styles.sidebarHeading}>
          <Link to="/" className={styles.brandLink} aria-label="PetCare, página principal">
            <Brand compact={collapsed} />
          </Link>
          <button
            type="button"
            className={ui.iconButton}
            aria-label={collapseLabel}
            title={collapseLabel}
            aria-expanded={!collapsed}
            aria-controls="owner-navigation"
            onClick={() => setCollapsed((value) => !value)}
          >
            <Icon name={collapsed ? 'expand' : 'collapse'} size={18} />
          </button>
        </div>
        <div id="owner-navigation" className={styles.sidebarContent}>
          <span className={styles.navLabel}>MI ESPACIO</span>
          <nav className={styles.navigation} aria-label="Navegación principal">
            {navigation.map((item) => (
              <NavLink
                key={item.path}
                to={item.path}
                aria-label={item.label}
                title={item.label}
                onClick={() => setMobileOpen(false)}
              >
                <Icon name={item.icon} />
                <span className={styles.navText}>{item.label}</span>
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
            <button
              className={styles.logout}
              aria-label="Cerrar sesión"
              title="Cerrar sesión"
              onClick={() => authSession.clear()}
            >
              <Icon name="logout" />
              <span className={styles.navText}>Cerrar sesión</span>
            </button>
            <span className={styles.sidebarFoot}>PetCare · Hecho para cuidarlos</span>
          </div>
        </div>
      </aside>
      <header className={styles.header}>
        <div className={styles.mobileBrand}>
          <button
            ref={mobileToggle}
            type="button"
            className={ui.iconButton}
            aria-label={mobileOpen ? 'Ocultar navegación' : 'Mostrar navegación'}
            aria-expanded={mobileOpen}
            aria-controls="owner-sidebar"
            onClick={() => setMobileOpen((value) => !value)}
          >
            <Icon name={mobileOpen ? 'close' : 'menu'} />
          </button>
          <Link to="/" className={styles.brandLink} aria-label="PetCare, página principal">
            <Brand />
          </Link>
        </div>
        <div className={styles.breadcrumb}>
          Mi espacio <span>/</span> <strong>{current?.label || 'Inicio'}</strong>
        </div>
        <div className={styles.headerRight}>
          <ThemeToggle />
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
  );
}
