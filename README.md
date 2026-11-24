<p align="center">
  <img src="img/petCareLogo.png" alt="PetCare Logo" width="200"/>
</p>

<h1 align="center">PetCare 🐾</h1>
<p align="center">
  Aplicación web para encontrar y gestionar servicios de cuidado para mascotas.
</p>

## Descripción

Frontend de PetCare, proyecto académico de Análisis y Diseño de Sistemas. Consume una API externa; este repositorio contiene exclusivamente la aplicación cliente y su configuración de desarrollo y publicación.

La aplicación cuenta con espacios para propietarios (`USER`) y proveedores (`PROVIDER`):

- Registro e inicio de sesión.
- Panel de inicio con mascotas y solicitudes recientes.
- Consulta y registro de mascotas.
- Catálogo de servicios y productos, búsqueda local y filtro por categoría.
- Detalle, contacto del proveedor y selección de franjas.
- Solicitud de servicios o productos, con retiro o entrega para productos.
- Consulta de estados e historial, incluido el motivo de rechazo.
- Registro e inicio de sesión de proveedores.
- Publicación de servicios y productos y creación de franjas horarias.
- Consulta y respuesta de solicitudes recibidas, historial y servicios prestados.
- Landing pública con información del proyecto, funcionalidades y accesos a la cuenta.
- Tema automático según el sistema, con cambio directo claro/oscuro mediante sol y luna.
- Barra lateral contraíble a iconos en escritorio y navegación desplegable en móvil.

## Stack y requisitos

| Herramienta           | Versión              |
| --------------------- | -------------------- |
| Node.js LTS           | 24.22.3              |
| pnpm                  | 12.21.0              |
| React / React DOM     | 19.3.1               |
| TypeScript            | 5.9.3, modo estricto |
| Vite                  | 8.3.8                |
| React Router          | 7.18.6               |
| TanStack Query        | 5.108.1              |
| React Hook Form / Zod | 7.92.0 / 4.6.7       |

CSS Modules y variables semánticas gestionan los estilos. Las versiones de las dependencias directas están fijadas en `frontend/package.json` y las transitivas en `pnpm-lock.yaml`. TypeScript 5.9.3 satisface los requisitos declarados por `openapi-typescript` y `typescript-eslint`.

## Desarrollo local

Desde `frontend/`, activa Node 24.22.3 (el archivo `.nvmrc` permite utilizar `nvm install` y `nvm use`) e instala pnpm 12.21.0. Después:

```sh
pnpm install --frozen-lockfile
pnpm dev
```

Abre **http://localhost:5173**. El backend externo debe estar accesible en **http://localhost:8080** para utilizar los flujos reales. Sus endpoints se consultan mediante el proxy de desarrollo `/api`, sin cambiar el backend.

### Configuración

Los valores predeterminados funcionan sin archivo `.env`. `frontend/.env.example` documenta:

| Variable            | Valor predeterminado    | Función                                                                                      |
| ------------------- | ----------------------- | -------------------------------------------------------------------------------------------- |
| `VITE_API_BASE_URL` | `/api`                  | Prefijo utilizado por el cliente; puede ser una URL absoluta. Se incorpora durante el build. |
| `API_PROXY_TARGET`  | `http://localhost:8080` | Destino del proxy de Vite en desarrollo.                                                     |

Con una URL absoluta, la API debe permitir el origen del frontend mediante CORS. No se deben incluir secretos en variables `VITE_*`.

## Autenticación

- `POST /auth/login` devuelve `token` y `refreshToken`.
- Ambos tokens se conservan **solo en memoria**: recargar o cerrar la pestaña requiere volver a iniciar sesión. Cada pestaña es independiente.
- La cuenta se identifica con `sub`; el rol se lee de `role` del access token. `jti` identifica el token.
- El access token se envía como Bearer. La renovación envía `{ refreshToken }` a `POST /auth/refresh` y sustituye ambos tokens.
- Una única renovación coordina las peticiones concurrentes, con un solo reintento. Los refresh tokens consumidos no se reintentan, incluso ante un fallo de red ambiguo.
- Salir o perder la autenticación elimina las credenciales y la caché privada. Las respuestas pendientes no pueden restaurar una sesión cerrada.
- Únicamente se persiste la preferencia de tema, bajo `petcare_theme` en `localStorage`.

La ruta `/` muestra la landing pública sin consultar la API. Los paneles de propietario y proveedor están en `/inicio` y `/proveedor`, respectivamente, y requieren el rol correspondiente. El tema sigue el sistema mientras no exista una elección manual; al pulsar la luna o el sol, se guarda únicamente `dark` o `light`. La barra lateral conserva su estado en memoria al navegar por el panel.

El registro devuelve una confirmación y conduce al login. El listado de publicaciones propias utiliza `GET /services` y filtra por `providerId`, ya que la API no dispone de un endpoint específico para ese listado.

## Contratos y fechas

Los DTOs de petición proceden de los tipos generados desde OpenAPI, y Zod valida las respuestas utilizadas por las pantallas. Para regenerar los tipos con la API disponible:

```sh
pnpm api:generate
```

El archivo generado se versiona, por lo que el backend no es necesario para instalar dependencias o compilar. Si cambia la ubicación de la API, adapta la URL del comando `api:generate`.

Las franjas representan fecha y hora locales de **America/Bogota**; no se interpretan como la zona del navegador. El servidor es la autoridad para conflictos de disponibilidad y permisos. Un conflicto `409` actualiza la información afectada.

## Comprobaciones

Desde `frontend/`:

```sh
pnpm typecheck
pnpm lint
pnpm format:check
pnpm test
pnpm build
```

Las pruebas de autenticación cubren rotación concurrente, respuestas `401` tardías, logout durante renovación y aislamiento de cuentas. Las pruebas de navegador usan respuestas de API simuladas: no crean ni modifican datos en el backend.

Para ejecutar el recorrido de propietario en escritorio y móvil:

```sh
pnpm exec playwright install --with-deps chromium
pnpm test:e2e
```

## Publicación del frontend

`pnpm build` genera los archivos estáticos en `frontend/dist`. El servidor que los publique debe resolver las rutas de la SPA hacia `index.html` y conectar `/api` con el backend externo, o utilizar una URL absoluta en `VITE_API_BASE_URL` al compilar.

El Dockerfile compila el frontend y publica los archivos con Nginx. Ejecuta desde la raíz del repositorio:

```sh
docker build -f frontend/Dockerfile -t petcare-frontend .
docker run --rm --add-host=host.docker.internal:host-gateway \
  -p 5173:80 \
  -e API_UPSTREAM=http://host.docker.internal:8080 \
  petcare-frontend
```

`API_UPSTREAM` debe ser el origen de la API, sin barra final. El contenedor no incluye el backend. Para una URL de API absoluta, usa `--build-arg VITE_API_BASE_URL=https://api.example.com` al construir la imagen.

## Estructura

```text
frontend/src/
├── app/                   # Rutas, providers, layout y tema
├── features/
│   ├── auth/              # Registro, login y autenticación en memoria
│   ├── landing/           # Presentación pública del proyecto
│   ├── owner/             # Panel, mascotas, catálogo y solicitudes del propietario
│   └── provider/          # Registro, publicaciones, franjas y solicitudes del proveedor
├── shared/
│   ├── api/               # HTTP, contratos, validación y consultas
│   ├── lib/               # Fechas y formatos
│   └── ui/                # Componentes compartidos
└── test/
```

## Referencias visuales

La interfaz parte de las vistas previas disponibles del [prototipo de Figma](https://www.figma.com/design/IpxDgmM1Q6iQG99oQIRaSh/PetCare-%E2%80%93-Prototipo?node-id=41-9996) y de la referencia de login existente. El modo oscuro extiende la misma identidad mediante variables CSS. La fidelidad visual se revisará pantalla por pantalla con el diseño completo.

`frontend/figma-login.html` y su CSS se conservan como referencia estática del prototipo previo.

## 📄 Licencia

Este proyecto está bajo la licencia MIT. Ver [LICENSE](LICENSE).
