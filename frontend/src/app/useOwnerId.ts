import { useOutletContext } from 'react-router-dom';

export function useOwnerId() {
  return useOutletContext<{ ownerId: string }>().ownerId;
}
