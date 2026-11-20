import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { useOwnerId } from '../../app/useOwnerId';
import { petsQuery, requestsQuery } from '../../shared/api/queries';
import { petLabels } from '../../shared/api/schemas';
import { slotDate, time } from '../../shared/lib/format';
import { ButtonLink, EmptyState, Loading, QueryError } from '../../shared/ui/components';
import { Icon } from '../../shared/ui/Icon';
import { StatusBadge } from '../service-requests/StatusBadge';
import ui from '../../shared/ui/ui.module.css';
import styles from './DashboardPage.module.css';

export function DashboardPage() {
  const ownerId = useOwnerId();
  const pets = useQuery(petsQuery(ownerId));
  const requests = useQuery(requestsQuery(ownerId));
  const pending = requests.data?.filter((request) => request.status === 'PENDIENTE').length;
  const accepted = requests.data?.filter((request) => request.status === 'ACEPTADO').length;
  return (
    <>
      <section className={styles.hero}>
        <div>
          <span className={ui.eyebrow}>BIENVENIDO A TU ESPACIO</span>
          <h1>Su bienestar comienza con un pequeño cuidado.</h1>
          <p>
            Organiza el día a día de tus mascotas. Encuentra lo que necesitan y acompaña cada paso
            desde un solo lugar.
          </p>
          <ButtonLink to="/servicios">
            Explorar servicios <Icon name="arrow" size={17} />
          </ButtonLink>
        </div>
        <div className={styles.heroArt}>
          <Icon name="paw" size={100} />
        </div>
      </section>
      <div className={styles.stats}>
        <Link to="/mascotas" className={styles.stat}>
          <div>
            <strong>{pets.data?.length ?? '—'}</strong>
            <span>Mascotas registradas</span>
          </div>
          <div className={styles.statIcon}>
            <Icon name="paw" />
          </div>
        </Link>
        <Link to="/solicitudes?estado=PENDIENTE" className={styles.stat}>
          <div>
            <strong>{pending ?? '—'}</strong>
            <span>Solicitudes pendientes</span>
          </div>
          <div className={styles.statIcon}>
            <Icon name="clock" />
          </div>
        </Link>
        <Link to="/solicitudes?estado=ACEPTADO" className={styles.stat}>
          <div>
            <strong>{accepted ?? '—'}</strong>
            <span>Solicitudes aceptadas</span>
          </div>
          <div className={styles.statIcon}>
            <Icon name="check" />
          </div>
        </Link>
      </div>
      <div className={styles.sections}>
        <section className={ui.card}>
          <div className={ui.spread}>
            <h2>Mis compañeros</h2>
            <Link to="/mascotas" className={ui.muted}>
              Ver todos
            </Link>
          </div>
          {pets.isPending ? (
            <Loading />
          ) : pets.isError ? (
            <QueryError error={pets.error} retry={() => void pets.refetch()} />
          ) : !pets.data.length ? (
            <EmptyState
              title="Tu familia empieza aquí"
              action={
                <ButtonLink to="/mascotas?nueva=1" variant="secondary">
                  Registrar mascota
                </ButtonLink>
              }
            >
              Agrega a tu primer compañero para encontrar cuidados a su medida.
            </EmptyState>
          ) : (
            <div className={styles.list}>
              {pets.data.slice(0, 3).map((pet) => (
                <div key={pet.id} className={styles.listItem}>
                  <div className={styles.pet}>
                    <span className={styles.avatar}>
                      <Icon name="paw" />
                    </span>
                    <div>
                      <h3>{pet.name}</h3>
                      <p>
                        {petLabels[pet.typePet]} · {pet.age} {pet.age === 1 ? 'año' : 'años'}
                      </p>
                    </div>
                  </div>
                  <Icon name="heart" size={17} />
                </div>
              ))}
            </div>
          )}
        </section>
        <section className={ui.card}>
          <div className={ui.spread}>
            <h2>Últimas solicitudes</h2>
            <Link to="/solicitudes" className={ui.muted}>
              Ver todas
            </Link>
          </div>
          {requests.isPending ? (
            <Loading />
          ) : requests.isError ? (
            <QueryError error={requests.error} retry={() => void requests.refetch()} />
          ) : !requests.data.length ? (
            <EmptyState
              title="Un cuidado por descubrir"
              action={
                <ButtonLink to="/servicios" variant="secondary">
                  Ver catálogo
                </ButtonLink>
              }
            >
              Tus solicitudes y sus confirmaciones aparecerán aquí.
            </EmptyState>
          ) : (
            <div className={styles.list}>
              {requests.data.slice(0, 3).map((request) => (
                <div key={request.id} className={styles.listItem}>
                  <div>
                    <h3>
                      <Link to={`/solicitudes/${request.id}/historial`}>
                        {request.offeringName}
                      </Link>
                    </h3>
                    <p>
                      {request.petName} · {slotDate(request.date)} · {time(request.startTime)}
                    </p>
                  </div>
                  <StatusBadge status={request.status} />
                </div>
              ))}
            </div>
          )}
        </section>
      </div>
    </>
  );
}
