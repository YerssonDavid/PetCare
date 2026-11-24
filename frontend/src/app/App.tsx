import { Component, useEffect, useState, type ErrorInfo, type ReactNode } from 'react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { BrowserRouter, Link, Navigate, Route, Routes, useLocation } from 'react-router-dom';
import { AuthLayout, LoginPage, RegisterPage } from '../features/auth/AuthPages';
import { authSession } from '../features/auth/session';
import { useSession } from '../features/auth/useSession';
import { DashboardPage } from '../features/owner/dashboard/DashboardPage';
import { PetsPage } from '../features/owner/pets/PetsPage';
import { CatalogPage } from '../features/owner/offerings/CatalogPage';
import { OfferingPage } from '../features/owner/offerings/OfferingPage';
import { RequestsPage, HistoryPage } from '../features/owner/service-requests/RequestPages';
import { ApiError } from '../shared/api/http';
import { Button, EmptyState } from '../shared/ui/components';
import { Shell } from './Shell';
import { ThemeProvider } from './ThemeProvider';
import { LandingPage } from '../features/landing/LandingPage';
import { ProviderRegisterPage } from '../features/provider/registration/ProviderRegisterPage';
import { ProviderOfferingsPage } from '../features/provider/offerings/ProviderOfferingsPage';
import {
  ProviderDashboardPage,
  ProviderHistoryPage,
  ProviderRequestsPage,
} from '../features/provider/requests/ProviderRequestsPage';

class ErrorBoundary extends Component<{ children: ReactNode }, { failed: boolean }> {
  state = { failed: false };
  static getDerivedStateFromError() {
    return { failed: true };
  }
  componentDidCatch(error: Error, info: ErrorInfo) {
    if (import.meta.env.DEV) console.error(error, info.componentStack);
  }
  render() {
    return this.state.failed ? (
      <main style={{ maxWidth: 640, padding: '4rem 1rem', margin: 'auto' }}>
        <EmptyState
          title="Algo no salió como esperábamos"
          action={<Button onClick={() => window.location.reload()}>Volver a abrir PetCare</Button>}
        >
          Recarga la aplicación para volver a intentarlo.
        </EmptyState>
      </main>
    ) : (
      this.props.children
    );
  }
}

function ProtectedShell({ role }: { role: 'USER' | 'PROVIDER' }) {
  const session = useSession();
  const location = useLocation();
  if (!session)
    return (
      <Navigate
        to={`/login?redirect=${encodeURIComponent(location.pathname + location.search)}`}
        replace
      />
    );
  if (session.role !== role)
    return <Navigate to={session.role === 'PROVIDER' ? '/proveedor' : '/inicio'} replace />;
  return <Shell ownerId={session.id} role={session.role} />;
}

function RouteFocus() {
  const { pathname } = useLocation();
  useEffect(() => {
    const frame = requestAnimationFrame(() => {
      document.querySelector<HTMLElement>('#main-content')?.focus({ preventScroll: true });
      window.scrollTo({ top: 0 });
    });
    return () => cancelAnimationFrame(frame);
  }, [pathname]);
  return null;
}

export function App() {
  const [queryClient] = useState(
    () =>
      new QueryClient({
        defaultOptions: {
          queries: {
            staleTime: 30_000,
            retry: (failures, error) =>
              failures < 1 && error instanceof ApiError && error.status >= 500,
          },
          mutations: { retry: false },
        },
      }),
  );
  useEffect(() => {
    let previousOwner = authSession.getSnapshot()?.id;
    return authSession.subscribe(() => {
      const owner = authSession.getSnapshot()?.id;
      if (owner !== previousOwner) queryClient.clear();
      previousOwner = owner;
    });
  }, [queryClient]);
  return (
    <ErrorBoundary>
      <ThemeProvider>
        <QueryClientProvider client={queryClient}>
          <BrowserRouter>
            <a href="#main-content" className="skip-link">
              Saltar al contenido
            </a>
            <RouteFocus />
            <Routes>
              <Route element={<AuthLayout />}>
                <Route path="/login" element={<LoginPage />} />
                <Route path="/registro" element={<RegisterPage />} />
                <Route path="/registro/proveedor" element={<ProviderRegisterPage />} />
              </Route>
              <Route element={<ProtectedShell role="USER" />}>
                <Route path="/inicio" element={<DashboardPage />} />
                <Route path="/mascotas" element={<PetsPage />} />
                <Route path="/servicios" element={<CatalogPage />} />
                <Route path="/servicios/:id" element={<OfferingPage />} />
                <Route path="/solicitudes" element={<RequestsPage />} />
                <Route path="/solicitudes/:id/historial" element={<HistoryPage />} />
              </Route>
              <Route element={<ProtectedShell role="PROVIDER" />}>
                <Route path="/proveedor" element={<ProviderDashboardPage />} />
                <Route path="/proveedor/publicaciones" element={<ProviderOfferingsPage />} />
                <Route path="/proveedor/solicitudes" element={<ProviderRequestsPage />} />
                <Route
                  path="/proveedor/solicitudes/:id/historial"
                  element={<ProviderHistoryPage />}
                />
                <Route path="/proveedor/prestados" element={<ProviderRequestsPage provided />} />
              </Route>
              <Route path="/" element={<LandingPage />} />
              <Route
                path="*"
                element={
                  <main
                    id="main-content"
                    tabIndex={-1}
                    style={{ maxWidth: 700, padding: '4rem 1rem', margin: 'auto' }}
                  >
                    <EmptyState
                      title="Esta página no está disponible"
                      action={<Link to="/">Volver a PetCare</Link>}
                    >
                      Comprueba la dirección o vuelve a tu espacio de PetCare.
                    </EmptyState>
                  </main>
                }
              />
            </Routes>
          </BrowserRouter>
        </QueryClientProvider>
      </ThemeProvider>
    </ErrorBoundary>
  );
}
