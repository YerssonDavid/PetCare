import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { z } from 'zod';
import { useOwnerId } from '../../app/useOwnerId';
import { api } from '../../shared/api/client';
import type { components } from '../../shared/api/generated';
import { ApiError } from '../../shared/api/http';
import { offeringQuery, ownerKeys, petsQuery, slotsQuery } from '../../shared/api/queries';
import { categoryLabels, requestSchema } from '../../shared/api/schemas';
import { isFutureSlot, money, slotDate, time } from '../../shared/lib/format';
import {
  Button,
  ButtonLink,
  EmptyState,
  Field,
  Loading,
  Notice,
  PageHeading,
  QueryError,
} from '../../shared/ui/components';
import { Icon } from '../../shared/ui/Icon';
import { OfferingVisual } from './OfferingVisual';
import { requestFormSchema } from './validation';
import ui from '../../shared/ui/ui.module.css';
import styles from './Offerings.module.css';

function RequestForm({
  ownerId,
  offeringId,
  product,
  price,
}: {
  ownerId: string;
  offeringId: string;
  product: boolean;
  price: number;
}) {
  const pets = useQuery(petsQuery(ownerId));
  const slots = useQuery({
    ...slotsQuery(ownerId, offeringId),
    staleTime: 0,
    refetchInterval: 30_000,
  });
  const client = useQueryClient();
  const navigate = useNavigate();
  const schema = requestFormSchema(product);
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors },
  } = useForm<z.infer<typeof schema>>({
    resolver: zodResolver(schema),
    defaultValues: { petId: '', slotId: '', ...(product ? { purchaseType: 'RETIRO' } : {}) },
  });
  const mutation = useMutation({
    mutationFn: (body: components['schemas']['CreateServiceRequestRequest']) =>
      api('/service-requests', requestSchema, { method: 'POST', body }),
    onSuccess: (response) => {
      void client.invalidateQueries({ queryKey: ownerKeys.requests(ownerId) });
      void client.invalidateQueries({ queryKey: ownerKeys.offerings(ownerId) });
      navigate(`/solicitudes?creada=${encodeURIComponent(response.id)}`);
    },
    onError: (error) => {
      if (error instanceof ApiError && error.status === 409) {
        void client.invalidateQueries({ queryKey: ownerKeys.offerings(ownerId) });
        void client.invalidateQueries({ queryKey: ownerKeys.requests(ownerId) });
      }
    },
  });
  if (pets.isPending || slots.isPending)
    return <Loading>Consultando mascotas y disponibilidad…</Loading>;
  if (pets.isError) return <QueryError error={pets.error} retry={() => void pets.refetch()} />;
  if (slots.isError) return <QueryError error={slots.error} retry={() => void slots.refetch()} />;
  if (!pets.data.length)
    return (
      <EmptyState
        title="Primero, tu compañero"
        action={<ButtonLink to="/mascotas?nueva=1">Registrar mascota</ButtonLink>}
      >
        Necesitas registrar una mascota para crear una solicitud.
      </EmptyState>
    );
  const available = slots.data.filter((slot) => isFutureSlot(slot.date, slot.startTime));
  if (!available.length)
    return (
      <EmptyState
        title="Aún no hay franjas disponibles"
        action={
          <Button variant="secondary" onClick={() => void slots.refetch()}>
            Consultar de nuevo
          </Button>
        }
      >
        Vuelve más adelante para encontrar un horario para tu mascota.
      </EmptyState>
    );
  return (
    <form
      className={ui.form}
      noValidate
      onSubmit={handleSubmit((values) => {
        if (
          !available.some(
            (slot) => slot.id === values.slotId && isFutureSlot(slot.date, slot.startTime),
          )
        ) {
          setError('slotId', { message: 'Esa franja ya no está disponible. Selecciona otra.' });
          void slots.refetch();
          return;
        }
        const body: components['schemas']['CreateServiceRequestRequest'] = {
          petId: values.petId,
          slotId: values.slotId,
          ...(product && values.purchaseType ? { purchaseType: values.purchaseType } : {}),
        };
        mutation.mutate(body);
      })}
    >
      {mutation.isError && <Notice>{mutation.error.message}</Notice>}
      <Field id="request-pet" label="¿Para quién es este cuidado?" error={errors.petId?.message}>
        <select
          id="request-pet"
          aria-invalid={!!errors.petId}
          aria-describedby={errors.petId ? 'request-pet-error' : undefined}
          {...register('petId')}
        >
          <option value="">Selecciona tu mascota</option>
          {pets.data.map((pet) => (
            <option key={pet.id} value={pet.id}>
              {pet.name}
            </option>
          ))}
        </select>
      </Field>
      {product && (
        <Field
          id="request-purchase"
          label="¿Cómo quieres recibirlo?"
          error={errors.purchaseType?.message}
        >
          <select id="request-purchase" {...register('purchaseType')}>
            <option value="RETIRO">Retiro con el proveedor</option>
            <option value="ENTREGA">Entrega</option>
          </select>
        </Field>
      )}
      <fieldset aria-describedby={errors.slotId ? 'request-slot-error' : 'slot-timezone'}>
        <legend>{product ? 'Horario de retiro o entrega' : 'Elige una franja'}</legend>
        <p id="slot-timezone" className={ui.muted}>
          Todos los horarios corresponden a Bogotá.
        </p>
        <div className={styles.slots}>
          {available.map((slot) => (
            <label key={slot.id} className={styles.slot}>
              <input
                type="radio"
                value={slot.id}
                aria-invalid={!!errors.slotId}
                {...register('slotId')}
              />
              <span>
                <strong>{slotDate(slot.date)}</strong>
                <small>
                  {time(slot.startTime)}–{time(slot.endTime)}
                </small>
              </span>
            </label>
          ))}
        </div>
        {errors.slotId && (
          <p id="request-slot-error" className={ui.fieldError}>
            {errors.slotId.message}
          </p>
        )}
      </fieldset>
      <div className={styles.summary}>
        <div className={ui.spread}>
          <span>Total</span>
          <strong className={ui.price}>{money(price)}</strong>
        </div>
        <p>La solicitud quedará pendiente de respuesta del proveedor.</p>
      </div>
      <Button full disabled={mutation.isPending}>
        {mutation.isPending
          ? 'Enviando solicitud…'
          : product
            ? 'Solicitar producto'
            : 'Solicitar servicio'}
      </Button>
    </form>
  );
}

function OfferingDetail({ id }: { id: string }) {
  const ownerId = useOwnerId();
  const query = useQuery(offeringQuery(ownerId, id));
  if (query.isPending) return <Loading>Cargando el detalle…</Loading>;
  if (query.isError) return <QueryError error={query.error} retry={() => void query.refetch()} />;
  const offering = query.data;
  const product = offering.type === 'PRODUCTO';
  const telephone = offering.providerPhone?.replace(/[^+\d]/g, '');
  return (
    <>
      <Link className={ui.back} to="/servicios">
        <Icon name="back" size={17} />
        Volver al catálogo
      </Link>
      <PageHeading
        eyebrow={categoryLabels[offering.category]}
        title={offering.name}
        description={
          product
            ? 'Un producto pensado para su bienestar.'
            : 'Un cuidado especial, en buenas manos.'
        }
      />
      <div className={styles.detailGrid}>
        <article className={ui.card}>
          <OfferingVisual imageUrl={offering.imageUrl} product={product} large />
          <h2>Acerca de {product ? 'este producto' : 'este servicio'}</h2>
          <p className={styles.description}>{offering.description}</p>
          <hr className={ui.divider} />
          <h3>Condiciones y recomendaciones</h3>
          <p className={styles.description}>
            {offering.conditions || 'El proveedor no ha indicado condiciones adicionales.'}
          </p>
          <div className={styles.provider}>
            <span className={ui.eyebrow}>TU PROVEEDOR</span>
            <strong>{offering.providerName}</strong>
            {telephone && (
              <p>
                <a href={`tel:${telephone}`}>
                  <Icon name="phone" size={16} />
                  {offering.providerPhone}
                </a>
              </p>
            )}
          </div>
        </article>
        <section className={ui.card} aria-labelledby="request-heading">
          <div className={ui.spread}>
            <h2 id="request-heading">Organiza su próximo cuidado</h2>
            <span className={ui.badge}>{product ? 'Producto' : 'Servicio'}</span>
          </div>
          <p className={ui.muted}>Selecciona tu mascota y el horario que mejor les funcione.</p>
          <RequestForm
            key={id}
            ownerId={ownerId}
            offeringId={id}
            product={product}
            price={offering.price}
          />
        </section>
      </div>
    </>
  );
}

export function OfferingPage() {
  const { id } = useParams();
  if (!id || !z.uuid().safeParse(id).success)
    return (
      <EmptyState
        title="No encontramos esta publicación"
        action={<ButtonLink to="/servicios">Volver al catálogo</ButtonLink>}
      >
        El enlace no tiene un identificador válido.
      </EmptyState>
    );
  return <OfferingDetail id={id} />;
}
