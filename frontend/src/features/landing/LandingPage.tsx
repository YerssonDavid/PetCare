import { useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import { ThemeToggle } from '../../app/ThemeToggle';
import { Brand } from '../../shared/ui/Brand';
import { ButtonLink } from '../../shared/ui/components';
import { Icon } from '../../shared/ui/Icon';
import { useSession } from '../auth/useSession';
import { PetCompanions } from './PetCompanions';
import ui from '../../shared/ui/ui.module.css';
import styles from './LandingPage.module.css';

const features = [
  {
    icon: 'paw',
    title: 'Su propio espacio',
    description: 'Registra a tus mascotas y ten su información a mano para cada nuevo cuidado.',
  },
  {
    icon: 'services',
    title: 'Cuidados a su medida',
    description: 'Explora servicios y productos. Consulta sus detalles y conoce al proveedor.',
  },
  {
    icon: 'calendar',
    title: 'Organízate con calma',
    description: 'Consulta las franjas disponibles y envía la solicitud que tu compañero necesita.',
  },
  {
    icon: 'heart',
    title: 'Cada paso, más claro',
    description: 'Sigue el estado de tus solicitudes y consulta su historial desde un mismo lugar.',
  },
] as const;

const steps = [
  { title: 'Crea tu cuenta', description: 'Un primer paso para organizar todo su cuidado.' },
  {
    title: 'Presenta a tu compañero',
    description: 'Registra su nombre, tipo y los detalles que lo hacen único.',
  },
  {
    title: 'Encuentra su próximo cuidado',
    description: 'Explora, envía una solicitud y sigue su respuesta.',
  },
];

export function LandingPage() {
  const session = useSession();
  const [menuOpen, setMenuOpen] = useState(false);
  const menuToggle = useRef<HTMLButtonElement>(null);
  const destination = session
    ? session.role === 'PROVIDER'
      ? '/proveedor'
      : '/inicio'
    : '/registro';
  const action = session ? 'Ir a mi espacio' : 'Crear mi cuenta';

  return (
    <div className={styles.page}>
      <header className={styles.header}>
        <div className={styles.headerInner}>
          <Link to="/" className={styles.brandLink} aria-label="PetCare, página principal">
            <Brand />
          </Link>
          <nav
            id="landing-navigation"
            aria-label="Navegación pública"
            className={styles.navigation}
            data-open={menuOpen}
            onKeyDown={(event) => {
              if (event.key === 'Escape') {
                setMenuOpen(false);
                menuToggle.current?.focus();
              }
            }}
          >
            <a href="#funcionalidades" onClick={() => setMenuOpen(false)}>
              Qué puedes hacer
            </a>
            <a href="#como-funciona" onClick={() => setMenuOpen(false)}>
              Cómo funciona
            </a>
            <a href="#proyecto" onClick={() => setMenuOpen(false)}>
              El proyecto
            </a>
            <div className={styles.mobileAccount}>
              {session ? (
                <Link to="/inicio">Ir a mi espacio</Link>
              ) : (
                <>
                  <Link to="/login">Iniciar sesión</Link>
                  <Link to="/registro">Crear cuenta</Link>
                </>
              )}
            </div>
          </nav>
          <div className={styles.headerActions}>
            <ThemeToggle />
            <div className={styles.desktopAccount}>
              {!session && (
                <Link to="/login" className={styles.loginLink}>
                  Iniciar sesión
                </Link>
              )}
              <ButtonLink to={destination}>{session ? action : 'Crear cuenta'}</ButtonLink>
            </div>
            <button
              ref={menuToggle}
              type="button"
              className={`${ui.iconButton} ${styles.menuToggle}`}
              aria-label={menuOpen ? 'Cerrar menú' : 'Abrir menú'}
              aria-expanded={menuOpen}
              aria-controls="landing-navigation"
              onClick={() => setMenuOpen((value) => !value)}
            >
              <Icon name={menuOpen ? 'close' : 'menu'} />
            </button>
          </div>
        </div>
      </header>

      <main id="main-content" tabIndex={-1}>
        <section className={`${styles.container} ${styles.hero}`} aria-labelledby="landing-title">
          <div className={styles.heroCopy}>
            <span className={styles.pill}>
              <Icon name="paw" size={16} /> CUIDADO QUE CONECTA
            </span>
            <h1 id="landing-title">
              El cuidado de tu mascota, <span>en un solo lugar.</span>
            </h1>
            <p>
              Ellos hacen tu vida más bonita. PetCare te ayuda a organizar la suya: encuentra
              servicios, descubre productos y acompaña cada paso de su cuidado.
            </p>
            <div className={styles.actions}>
              <ButtonLink to={destination}>
                {action}
                <Icon name="arrow" size={18} />
              </ButtonLink>
              {!session && (
                <ButtonLink to="/login" variant="secondary">
                  Ya tengo una cuenta
                </ButtonLink>
              )}
            </div>
            <span className={styles.heroNote}>
              <Icon name="heart" size={16} /> Pensado para ti y para quien siempre te acompaña.
            </span>
          </div>
          <div className={styles.heroVisual}>
            <div className={styles.illustration}>
              <PetCompanions />
            </div>
            <div className={styles.careTag}>
              <span className={styles.tagIcon}>
                <Icon name="heart" size={19} />
              </span>
              <span>
                Pequeños cuidados.<strong>Grandes compañeros.</strong>
              </span>
            </div>
            <div className={styles.visualCaption}>
              <Icon name="paw" size={18} /> Más cuidado. Más momentos juntos.
            </div>
          </div>
        </section>

        <section
          id="funcionalidades"
          className={styles.featureSection}
          aria-labelledby="features-title"
        >
          <div className={styles.container}>
            <div className={styles.sectionHeading}>
              <div>
                <span className={ui.eyebrow}>TU COMPAÑERO, EN EL CENTRO</span>
                <h2 id="features-title">
                  Menos vueltas.
                  <br />
                  Más tiempo juntos.
                </h2>
              </div>
              <p>
                Desde su información hasta su próxima solicitud. Lo que necesitas para organizar su
                bienestar, conectado.
              </p>
            </div>
            <div className={styles.featureGrid}>
              {features.map((feature, index) => (
                <article key={feature.title} className={styles.featureCard}>
                  <div className={styles.featureTop}>
                    <span className={styles.featureIcon}>
                      <Icon name={feature.icon} size={23} />
                    </span>
                    <span className={styles.featureNumber}>0{index + 1}</span>
                  </div>
                  <h3>{feature.title}</h3>
                  <p>{feature.description}</p>
                </article>
              ))}
            </div>
          </div>
        </section>

        <section
          id="como-funciona"
          className={`${styles.container} ${styles.stepsSection}`}
          aria-labelledby="steps-title"
        >
          <div className={styles.centerHeading}>
            <span className={ui.eyebrow}>ASÍ DE SENCILLO</span>
            <h2 id="steps-title">Su próximo cuidado empieza aquí.</h2>
            <p>Tres pasos para tener todo un poco más cerca.</p>
          </div>
          <ol className={styles.steps}>
            {steps.map((step, index) => (
              <li key={step.title}>
                <span className={styles.stepNumber} aria-hidden="true">
                  0{index + 1}
                </span>
                <h3>{step.title}</h3>
                <p>{step.description}</p>
              </li>
            ))}
          </ol>
        </section>

        <section
          id="proyecto"
          className={`${styles.container} ${styles.projectSection}`}
          aria-labelledby="project-title"
        >
          <div className={styles.projectCopy}>
            <span className={styles.projectEyebrow}>CON PROPÓSITO, CON CARIÑO</span>
            <h2 id="project-title">La tecnología también puede cuidar.</h2>
            <p>
              PetCare nace como un proyecto académico de Análisis y Diseño de Sistemas. Nuestro
              propósito es acercar a los propietarios de mascotas a los servicios y productos que
              sus compañeros necesitan.
            </p>
            <span className={styles.academicNote}>
              Un proyecto pensado desde el cuidado y construido para conectar.
            </span>
          </div>
          <div className={styles.projectValues}>
            <div>
              <Icon name="services" />
              <span>
                <strong>Conectar</strong>Información de servicios y productos en un mismo espacio.
              </span>
            </div>
            <div>
              <Icon name="calendar" />
              <span>
                <strong>Organizar</strong>Disponibilidad y solicitudes fáciles de consultar.
              </span>
            </div>
            <div>
              <Icon name="heart" />
              <span>
                <strong>Acompañar</strong>Seguimiento de cada solicitud, de principio a fin.
              </span>
            </div>
          </div>
        </section>

        <section
          className={`${styles.container} ${styles.closing}`}
          aria-labelledby="closing-title"
        >
          <span className={styles.closingIcon}>
            <Icon name="paw" size={28} />
          </span>
          <h2 id="closing-title">Su bienestar comienza contigo.</h2>
          <p>Nosotros te ayudamos a tener su cuidado un poco más organizado.</p>
          <ButtonLink to={destination}>
            {action}
            <Icon name="arrow" size={18} />
          </ButtonLink>
        </section>
      </main>

      <footer className={styles.footer}>
        <div className={`${styles.container} ${styles.footerInner}`}>
          <div>
            <Brand />
            <p>El cuidado de tu mascota, en un solo lugar.</p>
          </div>
          <nav aria-label="Navegación del pie de página">
            <a href="#funcionalidades">Funcionalidades</a>
            <a href="#como-funciona">Cómo funciona</a>
            <a href="#proyecto">El proyecto</a>
            <a href="https://github.com/YerssonDavid/PetCare">GitHub</a>
          </nav>
        </div>
        <div className={`${styles.container} ${styles.footerBottom}`}>
          <span>© {new Date().getFullYear()} PetCare</span>
          <span>Proyecto académico</span>
        </div>
      </footer>
    </div>
  );
}
