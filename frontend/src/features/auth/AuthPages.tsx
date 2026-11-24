import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useQueryClient } from '@tanstack/react-query';
import { Link, NavLink, Navigate, Outlet, useLocation, useNavigate } from 'react-router-dom';
import type { z } from 'zod';
import { ThemeToggle } from '../../app/ThemeToggle';
import { Icon } from '../../shared/ui/Icon';
import { Brand } from '../../shared/ui/Brand';
import { Button, Field, Notice } from '../../shared/ui/components';
import { api } from '../../shared/api/client';
import { registerResponseSchema } from '../../shared/api/schemas';
import type { components } from '../../shared/api/generated';
import { authSession } from './session';
import { useSession } from './useSession';
import { loginSchema, registerSchema } from './validation';
import ui from '../../shared/ui/ui.module.css';
import styles from './AuthPages.module.css';

function postLoginPath(search: string, role: 'USER' | 'PROVIDER' = 'USER') {
  const redirect = new URLSearchParams(search).get('redirect');
  if (role === 'PROVIDER') {
    return redirect && /^\/proveedor(?:[/?#]|$)/.test(redirect) && !redirect.includes('\\')
      ? redirect
      : '/proveedor';
  }
  return redirect &&
    /^\/(inicio|mascotas|servicios|solicitudes)(?:[/?#]|$)/.test(redirect) &&
    !redirect.includes('\\')
    ? redirect
    : '/inicio';
}

export function AuthLayout() {
  const session = useSession();
  const location = useLocation();
  if (session) return <Navigate to={postLoginPath(location.search, session.role)} replace />;
  return (
    <main id="main-content" tabIndex={-1} className={styles.layout}>
      <section className={styles.brandPanel}>
        <div>
          <Link to="/" className={styles.brandLink} aria-label="PetCare, página principal">
            <Brand />
          </Link>
          <h1>El cuidado de tu mascota, en un solo lugar.</h1>
          <p>
            Encuentra y gestiona servicios de cuidado para tus mascotas con información clara y
            confirmaciones organizadas.
          </p>
        </div>
        <span className={styles.academicProject}>PROYECTO ACADÉMICO</span>
      </section>
      <section className={styles.formPanel}>
        <div className={styles.themeRow}>
          <Link to="/" className={styles.homeLink}>
            <Icon name="back" size={16} />
            Volver a PetCare
          </Link>
          <ThemeToggle />
        </div>
        <div className={styles.formContent}>
          <nav className={styles.tabs} aria-label="Acceso a PetCare">
            <NavLink to="/login">Iniciar sesión</NavLink>
            <NavLink to="/registro">Crear cuenta</NavLink>
          </nav>
          <Outlet />
        </div>
      </section>
    </main>
  );
}

export function LoginPage() {
  const location = useLocation();
  const params = new URLSearchParams(location.search);
  const [error, setError] = useState('');
  const [visible, setVisible] = useState(false);
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<z.infer<typeof loginSchema>>({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: '', password: '' },
  });
  return (
    <>
      <h1>Bienvenido nuevamente</h1>
      <p className={styles.intro}>Ingresa con el correo y la contraseña de tu cuenta.</p>
      <form
        className={ui.form}
        noValidate
        onSubmit={handleSubmit(async (values) => {
          setError('');
          queryClient.clear();
          try {
            await authSession.login(values);
            navigate(postLoginPath(location.search, authSession.getSnapshot()?.role), {
              replace: true,
            });
          } catch (caught) {
            setError(caught instanceof Error ? caught.message : 'No pudimos iniciar sesión.');
          }
        })}
      >
        {params.get('registro') === 'completo' && (
          <Notice type="success">
            Tu cuenta está lista. Inicia sesión para registrar tu primera mascota.
          </Notice>
        )}
        {params.get('registro') === 'proveedor' && (
          <Notice type="success">
            Tu cuenta de proveedor está lista. Inicia sesión para continuar.
          </Notice>
        )}
        {error && <Notice>{error}</Notice>}
        <Field id="login-email" label="Correo electrónico" error={errors.email?.message}>
          <input
            id="login-email"
            type="email"
            autoComplete="username"
            placeholder="correo@ejemplo.com"
            aria-invalid={!!errors.email}
            aria-describedby={errors.email ? 'login-email-error' : undefined}
            {...register('email')}
          />
        </Field>
        <Field id="login-password" label="Contraseña" error={errors.password?.message}>
          <div className={styles.passwordWrap}>
            <input
              id="login-password"
              type={visible ? 'text' : 'password'}
              autoComplete="current-password"
              placeholder="Tu contraseña"
              aria-invalid={!!errors.password}
              aria-describedby={errors.password ? 'login-password-error' : undefined}
              {...register('password')}
            />
            <button
              type="button"
              className={styles.passwordToggle}
              aria-pressed={visible}
              onClick={() => setVisible(!visible)}
            >
              {visible ? 'Ocultar' : 'Mostrar'}
            </button>
          </div>
        </Field>
        <Button full disabled={isSubmitting}>
          {isSubmitting ? 'Iniciando sesión…' : 'Iniciar sesión'}
        </Button>
      </form>
      <p className={styles.footnote}>
        ¿Es tu primera visita? <Link to="/registro">Crea tu cuenta de propietario</Link>
      </p>
    </>
  );
}

export function RegisterPage() {
  const [error, setError] = useState('');
  const navigate = useNavigate();
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<z.infer<typeof registerSchema>>({ resolver: zodResolver(registerSchema) });
  return (
    <>
      <h1>Comienza una nueva historia</h1>
      <p className={styles.intro}>
        Crea tu cuenta de propietario y dale a tu mascota el cuidado que merece.
      </p>
      <form
        className={ui.form}
        noValidate
        onSubmit={handleSubmit(async (values) => {
          setError('');
          const body: components['schemas']['RegisterOwnerRequest'] = values;
          try {
            await api('/registry/owner', registerResponseSchema, {
              method: 'POST',
              body,
              public: true,
            });
            navigate('/login?registro=completo', { replace: true });
          } catch (caught) {
            setError(caught instanceof Error ? caught.message : 'No pudimos crear tu cuenta.');
          }
        })}
      >
        {error && <Notice>{error}</Notice>}
        <Field id="register-name" label="Nombre completo" error={errors.name?.message}>
          <input
            id="register-name"
            autoComplete="name"
            placeholder="¿Cómo te llamas?"
            maxLength={100}
            aria-invalid={!!errors.name}
            aria-describedby={errors.name ? 'register-name-error' : undefined}
            {...register('name')}
          />
        </Field>
        <Field id="register-email" label="Correo electrónico" error={errors.email?.message}>
          <input
            id="register-email"
            type="email"
            autoComplete="email"
            placeholder="correo@ejemplo.com"
            maxLength={150}
            aria-invalid={!!errors.email}
            aria-describedby={errors.email ? 'register-email-error' : undefined}
            {...register('email')}
          />
        </Field>
        <Field id="register-phone" label="Teléfono" error={errors.phone?.message}>
          <input
            id="register-phone"
            type="tel"
            autoComplete="tel"
            placeholder="Ej. 300 123 4567"
            aria-invalid={!!errors.phone}
            aria-describedby={errors.phone ? 'register-phone-error' : undefined}
            {...register('phone')}
          />
        </Field>
        <Field id="register-password" label="Contraseña" error={errors.password?.message}>
          <input
            id="register-password"
            type="password"
            autoComplete="new-password"
            placeholder="Mínimo 8 caracteres"
            maxLength={72}
            aria-invalid={!!errors.password}
            aria-describedby={errors.password ? 'register-password-error' : undefined}
            {...register('password')}
          />
        </Field>
        <Button full disabled={isSubmitting}>
          {isSubmitting ? 'Creando tu cuenta…' : 'Crear cuenta'}
        </Button>
      </form>
      <p className={styles.footnote}>
        Tu cuenta te permitirá gestionar tus mascotas y sus solicitudes.
      </p>
      <p className={styles.footnote}>
        ¿Ofreces servicios o productos?{' '}
        <Link to="/registro/proveedor">Regístrate como proveedor</Link>
      </p>
    </>
  );
}
