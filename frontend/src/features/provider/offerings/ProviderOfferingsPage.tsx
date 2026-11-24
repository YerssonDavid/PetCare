import { useState, type FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { z } from 'zod';
import { useSession } from '../../auth/useSession';
import { api } from '../../../shared/api/client';
import type { components } from '../../../shared/api/generated';
import {
  categories,
  categoryLabels,
  offeringSchema,
  registerResponseSchema,
} from '../../../shared/api/schemas';
import { money } from '../../../shared/lib/format';
import {
  Button,
  Field,
  Loading,
  Notice,
  PageHeading,
  QueryError,
} from '../../../shared/ui/components';
import ui from '../../../shared/ui/ui.module.css';

function SlotForm({ offeringId }: { offeringId: string }) {
  const [success, setSuccess] = useState(false);
  const mutation = useMutation({
    mutationFn: (body: components['schemas']['CreateTimeSlotRequest']) =>
      api(`/services/${encodeURIComponent(offeringId)}/slots`, registerResponseSchema, {
        method: 'POST',
        body,
      }),
    onSuccess: () => setSuccess(true),
  });
  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSuccess(false);
    const form = new FormData(event.currentTarget);
    mutation.mutate({
      date: String(form.get('date')),
      startTime: String(form.get('startTime')),
      endTime: String(form.get('endTime')),
    });
  }
  return (
    <form className={ui.form} onSubmit={submit}>
      <h3>Crear franja horaria</h3>
      <p>Horario de Bogotá. La franja debe ser futura y terminar a más tardar a las 17:00.</p>
      {mutation.isError && <Notice>{mutation.error.message}</Notice>}
      {success && <Notice type="success">Franja creada correctamente.</Notice>}
      <Field id={`date-${offeringId}`} label="Fecha">
        <input id={`date-${offeringId}`} name="date" type="date" required />
      </Field>
      <Field id={`start-${offeringId}`} label="Hora de inicio">
        <input id={`start-${offeringId}`} name="startTime" type="time" required />
      </Field>
      <Field id={`end-${offeringId}`} label="Hora de finalización">
        <input id={`end-${offeringId}`} name="endTime" type="time" max="17:00" required />
      </Field>
      <Button disabled={mutation.isPending}>Crear franja</Button>
    </form>
  );
}

export function ProviderOfferingsPage() {
  const session = useSession();
  const client = useQueryClient();
  const [success, setSuccess] = useState(false);
  const query = useQuery({
    queryKey: ['provider', session?.id, 'offerings'],
    queryFn: ({ signal }) => api('/services', z.array(offeringSchema), { signal }),
  });
  const mutation = useMutation({
    mutationFn: (body: components['schemas']['PublishOfferingRequest']) =>
      api('/services', registerResponseSchema, { method: 'POST', body }),
    onSuccess: () => {
      setSuccess(true);
      void client.invalidateQueries({ queryKey: ['provider', session?.id, 'offerings'] });
    },
  });
  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSuccess(false);
    const form = new FormData(event.currentTarget);
    const category = categories.find((item) => item === form.get('category'));
    if (!category) return;
    mutation.mutate({
      name: String(form.get('name')).trim(),
      description: String(form.get('description')).trim(),
      conditions: String(form.get('conditions')).trim(),
      price: Number(form.get('price')),
      category,
      type: form.get('type') === 'PRODUCTO' ? 'PRODUCTO' : 'SERVICIO',
    });
  }
  const offerings = query.data?.filter((offering) => offering.providerId === session?.id);
  return (
    <>
      <PageHeading
        eyebrow="TU ESPACIO"
        title="Mis publicaciones"
        description="Publica tus servicios y productos y organiza sus horarios."
      />
      <section className={ui.card}>
        <h2>Nueva publicación</h2>
        <form className={ui.form} onSubmit={submit}>
          {mutation.isError && <Notice>{mutation.error.message}</Notice>}
          {success && <Notice type="success">Publicación creada correctamente.</Notice>}
          <Field id="offering-name" label="Nombre">
            <input id="offering-name" name="name" maxLength={100} required />
          </Field>
          <Field id="offering-description" label="Descripción">
            <textarea id="offering-description" name="description" maxLength={500} required />
          </Field>
          <Field id="offering-conditions" label="Condiciones (opcional)">
            <textarea id="offering-conditions" name="conditions" maxLength={500} />
          </Field>
          <Field id="offering-price" label="Precio">
            <input id="offering-price" name="price" type="number" min="0" step="0.01" required />
          </Field>
          <Field id="offering-category" label="Categoría">
            <select id="offering-category" name="category">
              {categories.map((category) => (
                <option key={category} value={category}>
                  {categoryLabels[category]}
                </option>
              ))}
            </select>
          </Field>
          <Field id="offering-type" label="Tipo">
            <select id="offering-type" name="type">
              <option value="SERVICIO">Servicio</option>
              <option value="PRODUCTO">Producto</option>
            </select>
          </Field>
          <Button disabled={mutation.isPending}>Publicar</Button>
        </form>
      </section>
      <section className={ui.stack}>
        <h2>Publicaciones existentes</h2>
        {query.isPending && <Loading />}
        {query.isError && <QueryError error={query.error} retry={() => void query.refetch()} />}
        {offerings?.length === 0 && <p>Aún no tienes publicaciones.</p>}
        {offerings?.map((offering) => (
          <article className={ui.card} key={offering.id}>
            <h3>{offering.name}</h3>
            <p>{offering.description}</p>
            <p>
              {money(offering.price)} · {offering.type === 'SERVICIO' ? 'Servicio' : 'Producto'}
            </p>
            <SlotForm offeringId={offering.id} />
          </article>
        ))}
      </section>
    </>
  );
}
