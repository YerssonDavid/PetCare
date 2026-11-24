import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { api } from '../../../shared/api/client';
import type { components } from '../../../shared/api/generated';
import { registerResponseSchema } from '../../../shared/api/schemas';
import { Button, Field, Notice } from '../../../shared/ui/components';
import ui from '../../../shared/ui/ui.module.css';

export function ProviderRegisterPage() {
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);
  const navigate = useNavigate();

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const body: components['schemas']['RegisterProviderRequest'] = {
      nameCommercial: String(form.get('nameCommercial')).trim(),
      typeProvider: form.get('typeProvider') === 'CLINICA' ? 'CLINICA' : 'INDEPENDIENTE',
      email: String(form.get('email')).trim(),
      phone: String(form.get('phone')).trim(),
      password: String(form.get('password')),
      city: String(form.get('city')).trim(),
      address: String(form.get('address')).trim(),
      experienceYears: Number(form.get('experienceYears')),
      description: String(form.get('description')).trim(),
    };
    setError('');
    setSaving(true);
    try {
      await api('/registry/provider', registerResponseSchema, {
        method: 'POST',
        body,
        public: true,
      });
      navigate('/login?registro=proveedor', { replace: true });
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : 'No pudimos crear tu cuenta.');
    } finally {
      setSaving(false);
    }
  }

  return (
    <>
      <h1>Crea tu cuenta de proveedor</h1>
      <p>Comparte tus servicios y productos con propietarios de mascotas.</p>
      <form className={ui.form} onSubmit={submit}>
        {error && <Notice>{error}</Notice>}
        <Field id="provider-name" label="Nombre comercial">
          <input id="provider-name" name="nameCommercial" maxLength={100} required />
        </Field>
        <Field id="provider-type" label="Tipo de proveedor">
          <select id="provider-type" name="typeProvider">
            <option value="INDEPENDIENTE">Independiente</option>
            <option value="CLINICA">Clínica</option>
          </select>
        </Field>
        <Field id="provider-email" label="Correo electrónico">
          <input id="provider-email" name="email" type="email" maxLength={150} required />
        </Field>
        <Field id="provider-phone" label="Teléfono">
          <input
            id="provider-phone"
            name="phone"
            type="tel"
            pattern="\+?[0-9\s()\-]{7,25}"
            required
          />
        </Field>
        <Field id="provider-password" label="Contraseña">
          <input
            id="provider-password"
            name="password"
            type="password"
            minLength={8}
            maxLength={72}
            autoComplete="new-password"
            required
          />
        </Field>
        <Field id="provider-city" label="Ciudad">
          <input id="provider-city" name="city" maxLength={100} required />
        </Field>
        <Field id="provider-address" label="Dirección (opcional)">
          <input id="provider-address" name="address" maxLength={200} />
        </Field>
        <Field id="provider-experience" label="Años de experiencia">
          <input
            id="provider-experience"
            name="experienceYears"
            type="number"
            min="0"
            max="80"
            required
          />
        </Field>
        <Field id="provider-description" label="Descripción (opcional)">
          <textarea id="provider-description" name="description" maxLength={500} />
        </Field>
        <Button full disabled={saving}>
          {saving ? 'Creando cuenta…' : 'Crear cuenta de proveedor'}
        </Button>
      </form>
      <p>
        <Link to="/registro">Registrarme como propietario</Link>
      </p>
    </>
  );
}
