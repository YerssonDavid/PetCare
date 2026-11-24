import { useState, type FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, useParams } from 'react-router-dom';
import { z } from 'zod';
import { useSession } from '../../auth/useSession';
import { api } from '../../../shared/api/client';
import {
  historySchema,
  receivedRequestSchema,
  requestSchema,
  statusLabels,
} from '../../../shared/api/schemas';
import { slotDate, time, timestamp } from '../../../shared/lib/format';
import {
  Button,
  Field,
  Loading,
  Notice,
  PageHeading,
  QueryError,
} from '../../../shared/ui/components';
import ui from '../../../shared/ui/ui.module.css';

function RequestActions({ id }: { id: string }) {
  const client = useQueryClient();
  const [rejecting, setRejecting] = useState(false);
  const mutation = useMutation({
    mutationFn: ({ action, reason }: { action: 'accept' | 'reject'; reason?: string }) =>
      api(`/service-requests/${encodeURIComponent(id)}/${action}`, requestSchema, {
        method: 'PATCH',
        ...(action === 'reject' ? { body: { reason } } : {}),
      }),
    onSuccess: () => {
      setRejecting(false);
      void client.invalidateQueries({ queryKey: ['provider'] });
    },
    onError: () => void client.invalidateQueries({ queryKey: ['provider'] }),
  });
  function reject(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const reason = String(new FormData(event.currentTarget).get('reason')).trim();
    if (reason) mutation.mutate({ action: 'reject', reason });
  }
  return (
    <div className={ui.stack}>
      {mutation.isError && <Notice>{mutation.error.message}</Notice>}
      <div className={ui.row}>
        <Button disabled={mutation.isPending} onClick={() => mutation.mutate({ action: 'accept' })}>
          Aceptar
        </Button>
        <Button
          variant="secondary"
          disabled={mutation.isPending}
          onClick={() => setRejecting(!rejecting)}
        >
          Rechazar
        </Button>
      </div>
      {rejecting && (
        <form className={ui.form} onSubmit={reject}>
          <Field id={`reason-${id}`} label="Motivo del rechazo">
            <textarea id={`reason-${id}`} name="reason" maxLength={500} required />
          </Field>
          <Button disabled={mutation.isPending}>Confirmar rechazo</Button>
        </form>
      )}
    </div>
  );
}

export function ProviderRequestsPage({ provided = false }: { provided?: boolean }) {
  const session = useSession();
  const [status, setStatus] = useState('');
  const query = useQuery({
    queryKey: ['provider', session?.id, provided ? 'provided' : 'received', status],
    queryFn: ({ signal }) => {
      const path = provided ? '/service-requests/provided' : '/service-requests/received';
      const filter = !provided && status ? `?status=${encodeURIComponent(status)}` : '';
      return api(`${path}${filter}`, z.array(receivedRequestSchema), { signal });
    },
    refetchInterval: 30_000,
  });
  return (
    <>
      <PageHeading
        eyebrow="TU ESPACIO"
        title={provided ? 'Servicios prestados' : 'Solicitudes recibidas'}
        description={
          provided
            ? 'Consulta los servicios aceptados cuya hora de inicio ya llegó.'
            : 'Revisa y responde las solicitudes de tus clientes.'
        }
      />
      {!provided && (
        <Field id="provider-status" label="Filtrar por estado">
          <select
            id="provider-status"
            value={status}
            onChange={(event) => setStatus(event.target.value)}
          >
            <option value="">Todos</option>
            <option value="PENDIENTE">Pendientes</option>
            <option value="ACEPTADO">Aceptadas</option>
            <option value="RECHAZADO">Rechazadas</option>
          </select>
        </Field>
      )}
      {query.isPending && <Loading />}
      {query.isError && <QueryError error={query.error} retry={() => void query.refetch()} />}
      {query.data?.length === 0 && <p>No hay solicitudes para mostrar.</p>}
      <div className={ui.grid}>
        {query.data?.map((request) => (
          <article className={`${ui.card} ${ui.stack}`} key={request.id}>
            <h2>{request.offeringName}</h2>
            <p>{request.offeringDescription}</p>
            <p>Estado: {statusLabels[request.status]}</p>
            <p>
              Mascota: {request.pet.name} · Cliente: {request.ownerEmail}
            </p>
            <p>
              {slotDate(request.date)} · {time(request.startTime)}–{time(request.endTime)} · Bogotá
            </p>
            {request.purchaseType && (
              <p>{request.purchaseType === 'RETIRO' ? 'Retiro' : 'Entrega'}</p>
            )}
            {request.rejectionReason && <p>Motivo: {request.rejectionReason}</p>}
            <Link to={`/proveedor/solicitudes/${request.id}/historial`}>Ver historial</Link>
            {!provided && request.status === 'PENDIENTE' && <RequestActions id={request.id} />}
          </article>
        ))}
      </div>
    </>
  );
}

export function ProviderHistoryPage() {
  const { id = '' } = useParams();
  const session = useSession();
  const valid = z.uuid().safeParse(id).success;
  const query = useQuery({
    queryKey: ['provider', session?.id, 'history', id],
    queryFn: ({ signal }) =>
      api(`/service-requests/${encodeURIComponent(id)}/history`, historySchema, { signal }),
    enabled: valid,
  });
  if (!valid) return <p>El identificador de la solicitud no es válido.</p>;
  if (query.isPending) return <Loading />;
  if (query.isError) return <QueryError error={query.error} retry={() => void query.refetch()} />;
  return (
    <>
      <PageHeading
        eyebrow="TU ESPACIO"
        title="Historial de solicitud"
        description={query.data.offeringName}
      />
      <Link to="/proveedor/solicitudes">Volver a solicitudes</Link>
      <section className={ui.card}>
        <h2>{query.data.pet.name}</h2>
        <p>{query.data.offeringDescription}</p>
        <h3>Actualizaciones</h3>
        <ol>
          {query.data.entries.map((entry, index) => (
            <li key={`${entry.changedAt}-${index}`}>
              {statusLabels[entry.status]} · {timestamp(entry.changedAt)}
              {entry.reason && <p>Motivo: {entry.reason}</p>}
            </li>
          ))}
        </ol>
      </section>
    </>
  );
}

export function ProviderDashboardPage() {
  return (
    <>
      <PageHeading
        eyebrow="BIENVENIDO"
        title="Tu espacio de proveedor"
        description="Organiza tus publicaciones, horarios y solicitudes."
      />
      <div className={ui.grid}>
        <Link className={ui.card} to="/proveedor/publicaciones">
          Mis publicaciones y horarios
        </Link>
        <Link className={ui.card} to="/proveedor/solicitudes">
          Solicitudes recibidas
        </Link>
        <Link className={ui.card} to="/proveedor/prestados">
          Servicios prestados
        </Link>
      </div>
    </>
  );
}
