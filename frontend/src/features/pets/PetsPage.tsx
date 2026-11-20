import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useSearchParams } from 'react-router-dom';
import type { z } from 'zod';
import { useOwnerId } from '../../app/useOwnerId';
import { api } from '../../shared/api/client';
import type { components } from '../../shared/api/generated';
import { ownerKeys, petsQuery } from '../../shared/api/queries';
import { petLabels, petTypes, registerResponseSchema } from '../../shared/api/schemas';
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
import { petFormSchema } from './validation';
import ui from '../../shared/ui/ui.module.css';
import styles from './PetsPage.module.css';

function PetForm({ ownerId, close }: { ownerId: string; close: (saved?: boolean) => void }) {
  const queryClient = useQueryClient();
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<z.infer<typeof petFormSchema>>({
    resolver: zodResolver(petFormSchema),
    defaultValues: { name: '', typePet: 'PERRO', description: '' },
  });
  const mutation = useMutation({
    mutationFn: (body: components['schemas']['RegisterPetRequest']) =>
      api('/registry/pet', registerResponseSchema, { method: 'POST', body }),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ownerKeys.pets(ownerId) });
      close(true);
    },
  });
  return (
    <section className={`${ui.card} ${styles.formCard}`} aria-labelledby="pet-form-heading">
      <h2 id="pet-form-heading">Un nuevo compañero</h2>
      <p className={ui.muted}>Cuéntanos un poco sobre tu mascota.</p>
      <form
        className={ui.form}
        noValidate
        onSubmit={handleSubmit((values) => mutation.mutate(values))}
      >
        {mutation.isError && <Notice>{mutation.error.message}</Notice>}
        <div className={ui.formGrid}>
          <Field id="pet-name" label="Nombre" error={errors.name?.message}>
            <input
              id="pet-name"
              placeholder="Ej. Luna"
              maxLength={100}
              aria-invalid={!!errors.name}
              aria-describedby={errors.name ? 'pet-name-error' : undefined}
              {...register('name')}
            />
          </Field>
          <Field id="pet-type" label="Tipo de mascota" error={errors.typePet?.message}>
            <select id="pet-type" {...register('typePet')}>
              {petTypes.map((type) => (
                <option key={type} value={type}>
                  {petLabels[type]}
                </option>
              ))}
            </select>
          </Field>
        </div>
        <div className={ui.formGrid}>
          <Field id="pet-age" label="Edad en años" error={errors.age?.message}>
            <input
              id="pet-age"
              type="number"
              min="0"
              step="1"
              placeholder="Ej. 3"
              aria-invalid={!!errors.age}
              aria-describedby={errors.age ? 'pet-age-error' : undefined}
              {...register('age', { valueAsNumber: true })}
            />
          </Field>
        </div>
        <Field
          id="pet-description"
          label="Descripción (opcional)"
          error={errors.description?.message}
        >
          <textarea
            id="pet-description"
            maxLength={500}
            placeholder="Características, personalidad u observaciones importantes…"
            {...register('description')}
          />
        </Field>
        <div className={ui.row}>
          <Button disabled={mutation.isPending}>
            {mutation.isPending ? 'Guardando…' : 'Guardar mascota'}
          </Button>
          <Button
            type="button"
            variant="secondary"
            disabled={mutation.isPending}
            onClick={() => close()}
          >
            Cancelar
          </Button>
        </div>
      </form>
    </section>
  );
}

export function PetsPage() {
  const ownerId = useOwnerId();
  const [params, setParams] = useSearchParams();
  const pets = useQuery(petsQuery(ownerId));
  const adding = params.get('nueva') === '1';
  const saved = params.get('guardada') === '1';
  return (
    <>
      <PageHeading
        eyebrow="TU FAMILIA PETCARE"
        title="Mis mascotas"
        description="Cada compañero tiene su historia. Ten su información siempre a mano."
        action={
          !adding && (
            <Button onClick={() => setParams({ nueva: '1' })}>
              <Icon name="plus" size={17} />
              Agregar mascota
            </Button>
          )
        }
      />
      <div className={ui.stack}>
        {saved && <Notice type="success">Tu mascota se registró correctamente.</Notice>}
        {adding && (
          <PetForm
            ownerId={ownerId}
            close={(success) => setParams(success ? { guardada: '1' } : {})}
          />
        )}
        {pets.isPending ? (
          <Loading>Cargando tus mascotas…</Loading>
        ) : pets.isError ? (
          <QueryError error={pets.error} retry={() => void pets.refetch()} />
        ) : !pets.data.length ? (
          !adding && (
            <EmptyState
              title="Presenta a tu primer compañero"
              action={<ButtonLink to="/mascotas?nueva=1">Registrar mascota</ButtonLink>}
            >
              Registra tu mascota para solicitar servicios y productos para ella.
            </EmptyState>
          )
        ) : (
          <div className={ui.grid}>
            {pets.data.map((pet) => (
              <article key={pet.id} className={`${ui.card} ${styles.petCard}`}>
                <div className={ui.spread}>
                  <span className={styles.avatar}>
                    <Icon name="paw" size={32} />
                  </span>
                  <span className={ui.badge}>{petLabels[pet.typePet]}</span>
                </div>
                <div>
                  <h2>{pet.name}</h2>
                  <p>
                    {pet.age} {pet.age === 1 ? 'año' : 'años'} de compañía
                  </p>
                </div>
                <div className={styles.details}>
                  <p>{pet.description || 'Un compañero con mucho por descubrir.'}</p>
                </div>
                <ButtonLink to="/servicios" variant="secondary">
                  Encontrar cuidados <Icon name="arrow" size={15} />
                </ButtonLink>
              </article>
            ))}
          </div>
        )}
      </div>
    </>
  );
}
