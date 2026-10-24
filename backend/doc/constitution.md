---
name: constitution petcare
description: Descripción general del proyecto, objetivos generales y reglas.
---

# **Descripción General**

**PetCare** es una aplicación web que centraliza la búsqueda, solicitud y gestión de servicios para mascotas en cuatro categorías: **veterinaria, peluquería, paseo y cuidado temporal**. Conecta a dos tipos de usuarios:

- **Propietarios de mascotas**, que buscan, comparan y solicitan servicios.
- **Proveedores**, como clínicas veterinarias o cuidadores independientes, que publican sus servicios y gestionan las solicitudes que reciben.

## Problema que resuelve

Las seis entrevistas mostraron que el problema no es la falta de proveedores, sino que la información está dispersa entre redes sociales, chats de WhatsApp, memoria y cuadernos.

- Los **propietarios** repiten las mismas preguntas a varios proveedores, encuentran información desactualizada y no tienen una confirmación escrita ni un historial de lo contratado.
- Los **proveedores** controlan sus cupos de memoria, se les cruzan citas y tienen malentendidos por falta de un registro de lo acordado.
- La decisión del propietario depende más de la **confianza** que del precio.

## Propuesta de valor

Un solo lugar donde el propietario encuentra servicios con información clara y disponibilidad visible, solicita una cita para una mascota específica y consulta su estado e historial. El proveedor, por su parte, publica servicios, define su disponibilidad y acepta o rechaza solicitudes, y el sistema impide cruces de fecha y solicitudes duplicadas.

## Alcance del MVP

- Registro e inicio de sesión de propietarios y proveedores, con funciones según el rol.
- Registro de una o varias mascotas por propietario.
- Búsqueda de servicios por categoría y consulta de su detalle.
- Solicitud con mascota, fecha y hora, confirmación escrita y prevención de duplicados.
- Gestión de disponibilidad por franjas, con bloqueo de cruces.
- Aceptación o rechazo de solicitudes con motivo, y consulta del estado en "Mis Citas".

### Fuera del alcance del MVP

- Reseñas y calificaciones
- Imágenes reales
- Notificaciones en tiempo real
- Pagos en línea
- Administrador o verificación de proveedores
- Correo o SMS

## Metodología

El proyecto combina **Design Thinking** para entender al usuario (entrevistas, mapas de empatía, user personas, POV y Customer Journey Map) con **Scrum** para construir la solución de forma incremental. Se organiza en sprints con seguimiento en GitHub Projects y un equipo de tres personas:

| Rol | Responsabilidad |
|---|---|
| Analista y diseñadora UX | Investigación con usuarios y diseño de la interfaz |
| Desarrollador | Frontend y backend |
| Arquitecto de software | Arquitectura y backend |

## Arquitectura

La arquitectura se documenta con el **modelo C4**:

- **C1 — Contexto:** los dos tipos de usuario y el sistema PetCare.
- **C2 — Contenedores:**
    - SPA desplegada en Vercel.
    - API REST en Java con Spring Boot y autenticación JWT mediante Spring Security.
    - Base de datos PostgreSQL.
- **C3 — Componentes:** la API se organiza en módulos por dominio (Identidad y acceso, Catálogo de servicios, Mascotas, Solicitudes y Disponibilidad), cada uno con arquitectura hexagonal.
- **Despliegue (diagrama complementario):** la API y la base de datos corren en Docker sobre una VM Always Free de OCI, con Cloudflare al frente para DNS y HTTPS.

---
### Responsabilidades

- Autenticar a los usuarios y autorizar cada operación según su rol.
- Aplicar las reglas de negocio: impedir solicitudes duplicadas, impedir cruces de fecha y garantizar que cada usuario solo acceda a sus propios datos.
- Validar la información antes de almacenarla.
- Persistir y consultar la información en PostgreSQL.

## 2. Stack tecnológico

| Elemento | Tecnología | Propósito |
|---|---|---|
| Lenguaje | Java 25 (LTS) | Base del desarrollo del servidor |
| Framework | Spring Boot 4 | API REST y configuración de la aplicación |
| Seguridad | Spring Security con JWT | Autenticación sin estado y autorización por rol |
| Contraseñas | Hash seguro (p. ej. BCrypt) | Almacenamiento protegido de credenciales (RNF-05) |
| Validación | Bean Validation | Validación de datos de entrada (RNF-03) |
| Persistencia | Spring Data JPA / Hibernate | Acceso a datos desde los adaptadores de salida |
| Base de datos | PostgreSQL | Almacenamiento de la información del sistema |
| Errores | Problem Details (RFC 9457) | Respuestas de error uniformes y comprensibles (RNF-07) |
| Pruebas | JUnit y Mockito | Pruebas unitarias con simulación de dependencias |
| Empaquetado | Docker | Imagen de la API para el despliegue |

## 3. Arquitectura

### 3.1 Arquitectura hexagonal

La API sigue una **arquitectura hexagonal (puertos y adaptadores)**, que separa la lógica de negocio de los detalles técnicos en tres capas:

| Capa | Contenido | Depende de |
|---|---|---|
| Dominio | Entidades y reglas de negocio | Nada |
| Aplicación | Casos de uso y puertos (interfaces de entrada y salida) | Dominio |
| Infraestructura | Adaptadores de entrada (controladores REST) y de salida (repositorios JPA) | Aplicación |

El dominio no conoce Spring, HTTP ni la base de datos. Esto permite probar las reglas de negocio de forma aislada y cambiar la infraestructura sin modificarlas (RNF-11).

### 3.2 Organización por módulos de dominio

En este documento, un **módulo** es un **paquete de primer nivel que agrupa todo lo relacionado con un área del negocio**. No es una clase ni un módulo de Java (JPMS). Cada módulo contiene sus propias capas de dominio, aplicación e infraestructura, de modo que todo lo relacionado con, por ejemplo, las solicitudes vive en un solo lugar.

| Módulo | Responsabilidad | Requisitos |
|---|---|---|
| `identidad` | Registro, inicio de sesión, emisión de JWT y roles | RF-01, RF-02 |
| `mascotas` | Registro y consulta de las mascotas del usuario | RF-03 |
| `catalogo` | Publicación, búsqueda y detalle de servicios | RF-04, RF-05, RF-06, RF-13, RF-14 |
| `disponibilidad` | Franjas horarias y cupos; impide cruces de fecha | RF-09, RF-15 |
| `solicitudes` | Creación, consulta, aceptación y rechazo de solicitudes | RF-07, RF-08, RF-10, RF-11, RF-16 |

**Ejemplo: contenido del módulo `solicitudes`**

| Capa | Clase o interfaz | Rol |
|---|---|---|
| Dominio | `Solicitud` | Entidad con sus reglas (estados, transiciones) |
| Aplicación | `CrearSolicitudUseCase` | Puerto de entrada: lo que el módulo ofrece |
| Aplicación | `CrearSolicitudService` | Implementación del caso de uso |
| Aplicación | `SolicitudRepositoryPort` | Puerto de salida: lo que el módulo necesita para guardar datos |
| Infraestructura | `SolicitudController` | Adaptador de entrada: recibe las peticiones HTTP |
| Infraestructura | `SolicitudJpaAdapter` | Adaptador de salida: implementa el puerto con JPA |

### 3.3 Dependencias entre módulos

Solo existen dos dependencias entre módulos, ambas derivadas de reglas de negocio:

- **Solicitudes → Mascotas:** verifica que la mascota pertenezca al usuario que hace la solicitud (RNF-06).
- **Solicitudes → Disponibilidad:** verifica que la franja esté libre, la ocupa al confirmar y la libera al rechazar (CA 5.2 y 5.3).

Cualquier dependencia nueva debe reflejarse primero en el diagrama de componentes (C3).

### 3.4 Estructura de paquetes

```
com.petcare
├── main/java/*
│   ├── domain/
│   ├── application/
|   |   ├── config/          # Configuración, seguridad y manejo global de errores
│   │   ├── port/in/         # Casos de uso (puertos de entrada)
│   │   ├── port/out/        # Puertos de salida
│   │   └── service/         # Implementación de los casos de uso
│   └── infrastructure/
|       ├── config/          # Configuración, seguridad y manejo global de errores
│       ├── web/             # Controladores REST (adaptador de entrada)
│       └── persistence/     # Repositorios JPA (adaptador de salida)
├── mascotas/                # Misma estructura interna
├── catalogo/
├── disponibilidad/
└── solicitudes/
```

## 4. Seguridad

### 4.1 Roles

| Rol | Autoridad en Spring Security | Corresponde a | Estado en el MVP |
|---|---|---|---|
| `USER` | `ROLE_USER` | Propietario de mascotas | Activo |
| `PROVIDER` | `ROLE_PROVIDER` | Proveedor de servicios | Activo |
| `ADMIN` | `ROLE_ADMIN` | Administrador de la plataforma | Definido, sin funciones en el MVP |

- Al registrarse, el usuario elige entre `USER` y `PROVIDER`.
- El rol `ADMIN` **no puede asignarse desde el registro**; se crea solo de forma controlada.

### 4.2 Autenticación con JWT

1. El usuario envía sus credenciales al endpoint de inicio de sesión, que es **el mismo para todos los roles**.
2. La API verifica la contraseña contra su hash y emite un **JWT firmado** con el identificador y el rol del usuario.
3. El cliente envía el token en cada petición: `Authorization: Bearer <token>`.
4. Un **filtro de Spring Security** valida el token en cada petición antes de que llegue a cualquier módulo.

La API es **sin estado (stateless)**: no mantiene sesiones en el servidor. El cierre de sesión (RF-02) consiste en que el cliente descarte el token; por eso los tokens tienen una expiración corta.

### 4.3 Autorización

| Regla | Requisito |
|---|---|
| Las funciones privadas requieren un token válido | RNF-04 |
| Cada rol solo accede a sus funciones (`USER`, `PROVIDER`) | CA 2.3 |
| Un usuario solo consulta sus propias mascotas y solicitudes | RNF-06 |
| El identificador del usuario se obtiene del token, nunca de parámetros de la petición | RNF-06, RNF-12 |

### 4.4 Otras medidas

- **CORS** restringido al dominio del frontend.
- **HTTPS** en producción, gestionado por Cloudflare (RNF-10).

## 5. Persistencia

- **PostgreSQL** como única base de datos, accedida exclusivamente por la API.
- Cada módulo gestiona sus propias tablas a través de su adaptador JPA.
- Entidades principales: usuario, mascota, servicio, franja de disponibilidad y solicitud.
- **Un usuario puede tener muchas mascotas** (relación uno a muchos).
- La consistencia entre mascota, servicio, fecha, hora y estado de cada solicitud se garantiza en el dominio y con restricciones en la base de datos (RNF-08).
- Los servicios incluyen un campo de referencia a imagen (URL), vacío en el MVP, para incorporar imágenes reales sin cambiar el contrato de la API.

## 6. Endpoints

*Pendiente de definir.* Los endpoints se definirán junto con el alcance final de cada módulo. Decisiones ya tomadas:

- Un **único endpoint de inicio de sesión** para todos los roles; el rol viaja dentro del token.
- Registro de usuario y registro de mascotas como operaciones separadas.

## 7. Despliegue

- La API se empaqueta como **imagen Docker** y corre en una **VM en Cloud**, junto al contenedor de PostgreSQL.
- PostgreSQL usa un **volumen de Docker** para conservar los datos y no expone puertos a internet; solo la API se comunica con él, a través de la red interna de Docker.
- **Cloudflare** gestiona el DNS y el HTTPS (modo Full strict).

## 8. Pruebas

| Tipo | Herramientas | Alcance |
|---|---|---|
| Unitarias | JUnit y Mockito | Casos de uso y reglas de dominio de cada módulo, simulando sus dependencias (puertos de salida) |
| End-to-end (E2E) | Por definir | Flujos completos: registro, solicitud y aceptación |

## 9. Fuera del alcance del backend en el MVP

- Reseñas y calificaciones (RF-12)
- Almacenamiento de imágenes (Object Storage)
- Notificaciones en tiempo real
- Pagos en línea
- Funciones de administración y verificación de proveedores
- Envío de correos o SMS

## 10. Decisiones pendientes

| Decisión | Impacto |
|---|---|
| Estados de la solicitud y sus transiciones (enum) | Reglas del módulo Solicitudes |
| Conexión Cloudflare–VM: certificado de origen o Cloudflare Tunnel | Configuración del despliegue |
| Duración del JWT | Seguridad y experiencia de uso |