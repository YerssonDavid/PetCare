import type { ButtonHTMLAttributes, ReactNode } from 'react';
import { Link } from 'react-router-dom';
import { Icon } from './Icon';
import styles from './ui.module.css';

export function Button({
  variant = 'primary',
  full,
  className = '',
  ...props
}: ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: 'primary' | 'secondary';
  full?: boolean;
}) {
  return (
    <button
      className={`${styles.button} ${styles[variant]} ${full ? styles.full : ''} ${className}`}
      {...props}
    />
  );
}
export function ButtonLink({
  to,
  children,
  variant = 'primary',
}: {
  to: string;
  children: ReactNode;
  variant?: 'primary' | 'secondary';
}) {
  return (
    <Link className={`${styles.button} ${styles[variant]}`} to={to}>
      {children}
    </Link>
  );
}
export function Notice({
  children,
  type = 'error',
}: {
  children: ReactNode;
  type?: 'error' | 'success';
}) {
  return (
    <div
      className={`${styles.notice} ${styles[type]}`}
      role={type === 'error' ? 'alert' : 'status'}
    >
      {children}
    </div>
  );
}
export function Field({
  id,
  label,
  error,
  children,
}: {
  id: string;
  label: string;
  error?: string;
  children: ReactNode;
}) {
  return (
    <div className={styles.field}>
      <label htmlFor={id}>{label}</label>
      {children}
      {error && (
        <p id={`${id}-error`} className={styles.fieldError}>
          {error}
        </p>
      )}
    </div>
  );
}
export function Loading({ children = 'Cargando información…' }: { children?: ReactNode }) {
  return (
    <div className={styles.loading} role="status">
      <span className={styles.spinner} />
      {children}
    </div>
  );
}
export function EmptyState({
  title,
  children,
  action,
}: {
  title: string;
  children: ReactNode;
  action?: ReactNode;
}) {
  return (
    <div className={styles.empty}>
      <span className={styles.emptyIcon}>
        <Icon name="paw" size={28} />
      </span>
      <h2>{title}</h2>
      <p>{children}</p>
      {action}
    </div>
  );
}
export function PageHeading({
  eyebrow,
  title,
  description,
  action,
}: {
  eyebrow: string;
  title: string;
  description: string;
  action?: ReactNode;
}) {
  return (
    <div className={styles.pageHeading}>
      <div>
        <span className={styles.eyebrow}>{eyebrow}</span>
        <h1>{title}</h1>
        <p>{description}</p>
      </div>
      {action}
    </div>
  );
}
export function QueryError({ error, retry }: { error: Error; retry: () => void }) {
  return (
    <div className={styles.stack}>
      <Notice>{error.message}</Notice>
      <div>
        <Button variant="secondary" onClick={retry}>
          Volver a intentar
        </Button>
      </div>
    </div>
  );
}
