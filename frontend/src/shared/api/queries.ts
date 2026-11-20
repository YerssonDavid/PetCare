import { queryOptions } from '@tanstack/react-query';
import { z } from 'zod';
import { api } from './client';
import {
  historySchema,
  offeringDetailSchema,
  offeringSchema,
  petSchema,
  requestSchema,
  slotSchema,
} from './schemas';

export const ownerKeys = {
  all: (ownerId: string) => ['owner', ownerId] as const,
  pets: (ownerId: string) => [...ownerKeys.all(ownerId), 'pets'] as const,
  offerings: (ownerId: string) => [...ownerKeys.all(ownerId), 'offerings'] as const,
  detail: (ownerId: string, id: string) => [...ownerKeys.offerings(ownerId), 'detail', id] as const,
  slots: (ownerId: string, id: string) => [...ownerKeys.offerings(ownerId), 'slots', id] as const,
  requests: (ownerId: string) => [...ownerKeys.all(ownerId), 'requests'] as const,
  history: (ownerId: string, id: string) => [...ownerKeys.all(ownerId), 'history', id] as const,
};
export const petsQuery = (ownerId: string) =>
  queryOptions({
    queryKey: ownerKeys.pets(ownerId),
    queryFn: ({ signal }) => api('/pets', z.array(petSchema), { signal }),
  });
export const offeringsQuery = (ownerId: string, category = '') =>
  queryOptions({
    queryKey: [...ownerKeys.offerings(ownerId), 'list', category],
    queryFn: ({ signal }) =>
      api(
        `/services${category ? `?category=${encodeURIComponent(category)}` : ''}`,
        z.array(offeringSchema),
        { signal },
      ),
  });
export const offeringQuery = (ownerId: string, id: string) =>
  queryOptions({
    queryKey: ownerKeys.detail(ownerId, id),
    queryFn: ({ signal }) =>
      api(`/services/${encodeURIComponent(id)}`, offeringDetailSchema, { signal }),
  });
export const slotsQuery = (ownerId: string, id: string) =>
  queryOptions({
    queryKey: ownerKeys.slots(ownerId, id),
    queryFn: ({ signal }) =>
      api(`/services/${encodeURIComponent(id)}/slots/free`, z.array(slotSchema), { signal }),
  });
export const requestsQuery = (ownerId: string) =>
  queryOptions({
    queryKey: ownerKeys.requests(ownerId),
    queryFn: ({ signal }) => api('/service-requests/mine', z.array(requestSchema), { signal }),
  });
export const historyQuery = (ownerId: string, id: string) =>
  queryOptions({
    queryKey: ownerKeys.history(ownerId, id),
    queryFn: ({ signal }) =>
      api(`/service-requests/${encodeURIComponent(id)}/history`, historySchema, { signal }),
  });
