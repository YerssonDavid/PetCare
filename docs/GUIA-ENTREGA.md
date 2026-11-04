# Guía de ejecución y revisión de PetCare

## 1. Inicio completo

```bash
podman compose up -d --build
```

Abrir:

```text
http://localhost:5173
```

Ver servicios:

```bash
podman ps
```

Debe mostrar `petcare-db`, `petcare-backend` y `petcare-frontend`.

## 2. Recorrido demo

Entrar con `demo@petcare.test` y cualquier contraseña de cuatro o más caracteres. Este acceso es únicamente demostrativo y permite recorrer la interfaz sin crear una cuenta real.

## 3. Recorrido con persistencia

En **Crear cuenta**, registrar un propietario. Luego, desde **Mis mascotas**, agregar una mascota. El frontend envía los datos a:

- `POST /registry/owner`
- `POST /registry/pet`
- `DELETE /registry/pet?petId=...&ownerId=...`

## 4. Desarrollo rápido

Los cambios de `frontend/index.html`, `frontend/app.js` y `frontend/styles.css` se reflejan al recargar el navegador porque la carpeta está montada como volumen de desarrollo. Para cambios del backend:

```bash
podman compose up -d --build backend
```

## 5. Evidencias para la universidad

Capturar:

1. Pantalla de login.
2. Registro de propietario.
3. Panel principal.
4. Mis mascotas.
5. Agregar mascota.
6. Mascota registrada.
7. Eliminación de mascota.
8. Servicios preparados.
9. Solicitudes preparadas.
10. `podman ps` con los tres servicios activos.

## 6. Límites actuales

El backend todavía no implementa autenticación JWT, proveedores, catálogo real, disponibilidad, reservas ni historial. Esas pantallas forman parte del prototipo visual y del trabajo futuro del MVP; no deben documentarse como funcionalidades ya persistentes.

## 7. Seguridad básica

No publicar archivos `.env`, claves reales ni datos personales. Los puertos se exponen en `127.0.0.1` y el modo demo no es autenticación de producción.
