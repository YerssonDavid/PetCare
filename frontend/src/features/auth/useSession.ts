import { useSyncExternalStore } from 'react';
import { authSession } from './session';

export function useSession() {
  return useSyncExternalStore(
    authSession.subscribe,
    authSession.getSnapshot,
    authSession.getSnapshot,
  );
}
