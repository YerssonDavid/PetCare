# Entregables de Elkin — Análisis y Diseño de Sistemas

## POV

Los propietarios de mascotas necesitan comparar servicios y coordinar solicitudes en un solo lugar porque actualmente consultan publicaciones, chats y contactos separados. En las entrevistas se identificó que valoran la disponibilidad, la experiencia del proveedor, las reseñas y una confirmación clara de la fecha y la hora. También se evidenció que la información puede estar desactualizada o ser insuficiente.

Los proveedores necesitan organizar sus servicios, disponibilidad y solicitudes porque actualmente combinan redes sociales, WhatsApp, llamadas, agendas físicas o registros manuales. Esto dificulta mantener la información actualizada y evitar cruces de solicitudes.

### Pregunta de oportunidad

¿Cómo podría PetCare centralizar la consulta y solicitud de servicios para que los propietarios comparen opciones con información clara y los proveedores gestionen su oferta y solicitudes con menos dependencia de registros dispersos?

## User Map

| Etapa | Propietario | Sistema | Proveedor |
|---|---|---|---|
| Identifica la necesidad | Define el servicio y las características de su mascota. | Presenta categorías y búsqueda. | — |
| Compara opciones | Revisa información, experiencia, reseñas y disponibilidad. | Filtra y muestra servicios. | Mantiene publicados sus servicios. |
| Solicita | Selecciona la mascota, fecha y hora. | Registra la solicitud y muestra su estado. | Recibe la solicitud. |
| Gestiona | Consulta solicitudes y confirmaciones. | Conserva el historial. | Acepta, rechaza o actualiza el estado. |

## User Flow principal

1. El propietario ingresa a PetCare.
2. Consulta una categoría o busca un servicio.
3. Revisa los detalles del proveedor.
4. Inicia sesión o se registra.
5. Selecciona la mascota, fecha y hora.
6. Envía la solicitud.
7. Consulta la confirmación y el estado.

El flujo del proveedor es: iniciar sesión → registrar o actualizar un servicio → consultar solicitudes → revisar la información de la mascota → cambiar el estado de la solicitud.

## Requisitos funcionales

| Código | Requisito |
|---|---|
| RF-01 | Permitir el registro de propietarios. |
| RF-02 | Permitir el registro de proveedores. |
| RF-03 | Permitir iniciar y cerrar sesión. |
| RF-04 | Mostrar funciones según el rol autenticado. |
| RF-05 | Consultar categorías y buscar servicios. |
| RF-06 | Filtrar o consultar información detallada de un servicio. |
| RF-07 | Registrar una solicitud con mascota, fecha y hora. |
| RF-08 | Consultar el estado y el historial de solicitudes. |
| RF-09 | Registrar y actualizar servicios ofrecidos. |
| RF-10 | Consultar solicitudes recibidas. |
| RF-11 | Aceptar, rechazar o actualizar el estado de una solicitud. |

## Requisitos no funcionales

| Código | Requisito |
|---|---|
| RNF-01 | La interfaz debe usar textos claros, pasos breves y controles identificables. |
| RNF-02 | La aplicación debe adaptarse a celular, tableta y computador. |
| RNF-03 | El sistema debe validar los datos antes de guardarlos. |
| RNF-04 | Las funciones privadas deben requerir autenticación y control por rol. |
| RNF-05 | Las contraseñas deben almacenarse mediante mecanismos seguros de hash. |
| RNF-06 | Los errores y confirmaciones deben comunicarse de forma comprensible. |
| RNF-07 | La información de una solicitud debe conservar consistencia entre propietario y proveedor. |
| RNF-08 | La comunicación en producción debe usar HTTPS. |

## Justificación de Scrum

Se propone Scrum porque PetCare combina investigación con usuarios, diseño y desarrollo de una solución cuyo alcance puede ajustarse durante el proyecto. El trabajo puede dividirse en incrementos verificables: autenticación y roles, consulta de servicios y gestión de solicitudes.

El Product Backlog reunirá requisitos e historias de usuario priorizados para el MVP. En cada Sprint el equipo seleccionará un objetivo, construirá un incremento y revisará su resultado. La Sprint Review permitirá contrastar el producto con los hallazgos disponibles y la Sprint Retrospective permitirá mejorar la coordinación del equipo. Así, Scrum mantiene visible el avance y permite ajustar prioridades sin perder la relación entre necesidad, requisito e implementación.

Las entrevistas constituyen la evidencia disponible. Las pruebas de usabilidad y las validaciones posteriores deberán documentarse cuando se realicen; no se presentan como resultados obtenidos.
