import { useQuery } from '@tanstack/react-query';
import { useSearchParams } from 'react-router-dom';
import { useOwnerId } from '../../app/useOwnerId';
import { offeringsQuery } from '../../shared/api/queries';
import { categories, categoryLabels } from '../../shared/api/schemas';
import { money } from '../../shared/lib/format';
import {
  ButtonLink,
  EmptyState,
  Loading,
  PageHeading,
  QueryError,
} from '../../shared/ui/components';
import { Icon } from '../../shared/ui/Icon';
import { OfferingVisual } from './OfferingVisual';
import ui from '../../shared/ui/ui.module.css';
import styles from './Offerings.module.css';

export function CatalogPage() {
  const ownerId = useOwnerId();
  const [params, setParams] = useSearchParams();
  const selectedCategory = params.get('categoria') || '';
  const category = categories.find((item) => item === selectedCategory) || '';
  const search = params.get('q') || '';
  const kind = params.get('tipo') || '';
  const query = useQuery(offeringsQuery(ownerId, category));
  const offerings = query.data?.filter(
    (item) =>
      (!kind || item.type === kind) &&
      `${item.name} ${item.description}`
        .toLocaleLowerCase('es')
        .includes(search.trim().toLocaleLowerCase('es')),
  );
  const update = (key: string, value: string) => {
    setParams(
      (previous) => {
        const next = new URLSearchParams(previous);
        if (value) next.set(key, value);
        else next.delete(key);
        return next;
      },
      { replace: true },
    );
  };
  return (
    <>
      <PageHeading
        eyebrow="CUIDADOS PARA CADA ETAPA"
        title="Servicios y productos"
        description="Encuentra ese cuidado especial que tu mascota necesita."
      />
      <section className={styles.filters} aria-label="Filtrar catálogo">
        <div className={ui.field}>
          <label htmlFor="offering-search">Buscar</label>
          <div className={styles.search}>
            <Icon name="search" size={18} />
            <input
              id="offering-search"
              type="search"
              placeholder="Nombre o descripción"
              value={search}
              onChange={(event) => update('q', event.target.value)}
            />
          </div>
        </div>
        <div className={ui.field}>
          <label htmlFor="offering-category">Categoría</label>
          <select
            id="offering-category"
            value={category}
            onChange={(event) => update('categoria', event.target.value)}
          >
            <option value="">Todas las categorías</option>
            {categories.map((item) => (
              <option key={item} value={item}>
                {categoryLabels[item]}
              </option>
            ))}
          </select>
        </div>
        <div className={ui.field}>
          <label htmlFor="offering-kind">Tipo</label>
          <select
            id="offering-kind"
            value={kind}
            onChange={(event) => update('tipo', event.target.value)}
          >
            <option value="">Todos</option>
            <option value="SERVICIO">Servicios</option>
            <option value="PRODUCTO">Productos</option>
          </select>
        </div>
      </section>
      {query.isPending ? (
        <Loading>Buscando cuidados para tu mascota…</Loading>
      ) : query.isError ? (
        <QueryError error={query.error} retry={() => void query.refetch()} />
      ) : offerings && !offerings.length ? (
        <EmptyState title="No encontramos coincidencias">
          Prueba con otra categoría o cambia tu búsqueda. Si aún no hay publicaciones, vuelve a
          consultar más adelante.
        </EmptyState>
      ) : (
        <>
          <p className={styles.count} role="status">
            {offerings?.length}{' '}
            {offerings?.length === 1 ? 'opción disponible' : 'opciones disponibles'}
          </p>
          <div className={ui.grid}>
            {offerings?.map((offering) => (
              <article key={offering.id} className={`${ui.card} ${styles.offeringCard}`}>
                <OfferingVisual
                  imageUrl={offering.imageUrl}
                  product={offering.type === 'PRODUCTO'}
                />
                <div className={styles.cardBody}>
                  <span className={ui.eyebrow}>{categoryLabels[offering.category]}</span>
                  <h2>{offering.name}</h2>
                  <p>{offering.description}</p>
                  <div className={styles.cardFoot}>
                    <span className={ui.price}>{money(offering.price)}</span>
                    <ButtonLink to={`/servicios/${offering.id}`} variant="secondary">
                      Ver detalle <Icon name="arrow" size={16} />
                    </ButtonLink>
                  </div>
                </div>
              </article>
            ))}
          </div>
        </>
      )}
    </>
  );
}
