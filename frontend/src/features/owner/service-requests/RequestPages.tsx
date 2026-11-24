import { useQuery } from '@tanstack/react-query';
import { Link, useParams, useSearchParams } from 'react-router-dom';
import { z } from 'zod';
import { useOwnerId } from '../../../app/useOwnerId';
import { historyQuery, requestsQuery } from '../../../shared/api/queries';
import { petLabels, statuses, statusLabels } from '../../../shared/api/schemas';
import { money, slotDate, time, timestamp } from '../../../shared/lib/format';
import {
  ButtonLink,
  EmptyState,
  Loading,
  Notice,
  PageHeading,
  QueryError,
} from '../../../shared/ui/components';
import { Icon } from '../../../shared/ui/Icon';
import { StatusBadge } from './StatusBadge';
import ui from '../../../shared/ui/ui.module.css';
import styles from './Requests.module.css';

export function RequestsPage() {
  const ownerId = useOwnerId();
  const [params, setParams] = useSearchParams();
  const selected = statuses.find((status) => status === params.get('estado')) || '';
  const query = useQuery({ ...requestsQuery(ownerId), refetchInterval: 30_000 });
  const requests = query.data?.filter((request) => !selected || request.status === selected);
  return (
    <>
      <PageHeading
        eyebrow="CADA CUIDADO, ORGANIZADO"
        title="Mis solicitudes"
        description="Consulta tus horarios, las respuestas del proveedor y el historial de cada solicitud."
        action={
          <ButtonLink to="/servicios" variant="secondary">
            Explorar catálogo <Icon name="arrow" size={16} />
          </ButtonLink>
        }
      />
      <div className={ui.stack}>
        {params.has('creada') && (
          <Notice type="success">
            Solicitud creada. Te mostraremos aquí la respuesta del proveedor.
          </Notice>
        )}
        <div className={`${ui.field} ${styles.filters}`}>
          <label htmlFor="request-status">Estado</label>
          <select
            id="request-status"
            value={selected}
            onChange={(event) =>
              setParams(event.target.value ? { estado: event.target.value } : {})
            }
          >
            <option value="">Todos los estados</option>
            {statuses.map((status) => (
              <option key={status} value={status}>
                {statusLabels[status]}
              </option>
            ))}
          </select>
        </div>
        {query.isPending ? (
          <Loading>Cargando tus solicitudes…</Loading>
        ) : query.isError ? (
          <QueryError error={query.error} retry={() => void query.refetch()} />
        ) : !requests?.length ? (
          <EmptyState
            title={
              selected ? 'Sin solicitudes en este estado' : 'Tu próximo cuidado está por comenzar'
            }
            action={<ButtonLink to="/servicios">Encontrar un cuidado</ButtonLink>}
          >
            {selected
              ? 'Elige otro estado para consultar el resto de tus solicitudes.'
              : 'Explora servicios y productos para tu mascota. Sus solicitudes aparecerán aquí.'}
          </EmptyState>
        ) : (
          <div className={ui.grid}>
            {requests.map((request) => (
              <article key={request.id} className={`${ui.card} ${styles.requestCard}`}>
                <div className={ui.spread}>
                  <span className={ui.eyebrow}>
                    {request.offeringType === 'PRODUCTO' ? 'Producto' : 'Servicio'}
                  </span>
                  <StatusBadge status={request.status} />
                </div>
                <div>
                  <h2>{request.offeringName}</h2>
                  <p>{request.providerName}</p>
                </div>
                <div className={styles.metadata}>
                  <span>
                    <Icon name="paw" size={17} />
                    {request.petName}
                  </span>
                  <span>
                    <Icon name="calendar" size={17} />
                    {slotDate(request.date)}
                  </span>
                  <span>
                    <Icon name="clock" size={17} />
                    {time(request.startTime)}–{time(request.endTime)} · Bogotá
                  </span>
                  {request.purchaseType && (
                    <span>
                      <Icon name="bag" size={17} />
                      {request.purchaseType === 'RETIRO' ? 'Retiro con el proveedor' : 'Entrega'}
                    </span>
                  )}
                </div>
                {request.rejectionReason && (
                  <Notice>Motivo de rechazo: {request.rejectionReason}</Notice>
                )}
                <div className={styles.requestFoot}>
                  <span className={ui.price}>{money(request.price)}</span>
                  <ButtonLink to={`/solicitudes/${request.id}/historial`} variant="secondary">
                    Ver historial <Icon name="arrow" size={16} />
                  </ButtonLink>
                </div>
              </article>
            ))}
          </div>
        )}
      </div>
    </>
  );
}

function HistoryDetail({ id }: { id: string }) {
  const ownerId = useOwnerId();
  const query = useQuery({ ...historyQuery(ownerId, id), refetchInterval: 30_000 });
  if (query.isPending) return <Loading>Consultando el historial…</Loading>;
  if (query.isError) return <QueryError error={query.error} retry={() => void query.refetch()} />;
  const history = query.data;
  return (
    <>
      <Link to="/solicitudes" className={ui.back}>
        <Icon name="back" size={17} />
        Volver a mis solicitudes
      </Link>
      <PageHeading
        eyebrow="EL RECORRIDO DE SU CUIDADO"
        title="Historial de solicitud"
        description="Cada actualización, en un solo lugar. Horarios en hora de Bogotá."
      />
      <div className={styles.history}>
        <section className={ui.card}>
          <span className={ui.eyebrow}>
            {history.offeringType === 'PRODUCTO' ? 'Producto' : 'Servicio'}
          </span>
          <h2>{history.offeringName}</h2>
          <p className={ui.muted}>{history.offeringDescription}</p>
          <hr className={ui.divider} />
          <span className={ui.eyebrow}>SU COMPAÑERO</span>
          <h3>{history.pet.name}</h3>
          <p className={ui.muted}>
            {petLabels[history.pet.typePet]} · {history.pet.age}{' '}
            {history.pet.age === 1 ? 'año' : 'años'}
          </p>
          {history.pet.description && <p className={ui.muted}>{history.pet.description}</p>}
        </section>
        <section className={ui.card}>
          <h2>Actualizaciones</h2>
          <p className={ui.muted}>De la más reciente a la más antigua.</p>
          {!history.entries.length ? (
            <p>Aún no hay cambios de estado.</p>
          ) : (
            <ol className={styles.timeline}>
              {history.entries.map((entry, index) => (
                <li key={`${entry.changedAt}-${index}`}>
                  <StatusBadge status={entry.status} />
                  <time dateTime={entry.changedAt}>{timestamp(entry.changedAt)}</time>
                  {entry.reason && (
                    <p>
                      <strong>Motivo:</strong> {entry.reason}
                    </p>
                  )}
                </li>
              ))}
            </ol>
          )}
        </section>
      </div>
    </>
  );
}

export function HistoryPage() {
  const { id } = useParams();
  if (!id || !z.uuid().safeParse(id).success)
    return (
      <EmptyState
        title="No encontramos esta solicitud"
        action={<ButtonLink to="/solicitudes">Volver a mis solicitudes</ButtonLink>}
      >
        El enlace no tiene un identificador válido.
      </EmptyState>
    );
  return <HistoryDetail id={id} />;
}
