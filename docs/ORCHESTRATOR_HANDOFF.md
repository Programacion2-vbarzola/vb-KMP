# Traspaso de rol orquestador

Este documento es el punto de entrada para el chat orquestador del proyecto integrador 2026. Leé esto antes de cualquier otra cosa.

## Qué es este proyecto

Sistema distribuido de turnos médicos, proyecto académico individual. Tres repositorios obligatorios (cátedra exige separados, sección 13.1 del enunciado):

| # | Repo | Tecnología | Responsabilidad |
|---|------|-----------|-----------------|
| 1 | **programacion2-vbarzola** (este repo) | Kotlin Multiplatform / Compose / Ktor | App Android — UI, autenticación de usuario final, caché local SQLDelight |
| 2 | Repo Catálogo | Spring Boot + JHipster + PostgreSQL | Copia local del catálogo, sincronización con cátedra vía Kafka/Redis |
| 3 | Repo Turnos | Spring Boot + JHipster + PostgreSQL | Usuarios finales, JWT propio, holds, reservas, disponibilidad |

## Dónde está el plan completo

**`docs/SPRINTS.md`** en este repo contiene el plan de sprints de los 3 repos, con estado actualizado de cada issue, dependencias entre repos y notas técnicas. Es la fuente de verdad del avance.

## Estado actual (al momento de este traspaso)

### Repo KMP (este repo) — Sprint 0 completo
- Estructura KMP compilando y corriendo en emulador Android API 37
- Contrato REST documentado en `docs/contracts/API_CONTRACT.md`
- Sprint 1 pendiente: JWT storage, Ktor client, pantallas login/registro

### Repo Catálogo — Sprint 0 completo
- JHipster generado, hexagonal migrado, ArchUnit pasando
- Sprint 1 pendiente: sincronización Kafka + Redis (próximo a trabajar)

### Repo Turnos — Sprint 0 y Sprint 1 completos
- Hexagonal + ArchUnit funcionando
- Auth endpoints verificados contra contrato (`/api/register`, `/api/authenticate`, `/api/account`)
- `CatalogServiceClient` (port/out) + `CatalogServiceRestAdapter` implementados
- CORS listo, falta configurar `CORS_ALLOWED_ORIGINS` con el origen KMP real
- ⚠️ Pendiente verificar: activación inmediata de usuarios (JHipster activa por email por defecto; el enunciado sección 3.2 exige `activated = true` inmediatamente)

## Reglas de arquitectura que no deben violarse

1. **Tres JWTs, nunca confundirlos:**
   - JWT técnico de cátedra — solo en backends, NUNCA llega a KMP ni al usuario final
   - JWT propio — emitido por Repo Turnos, usado por KMP para todas las APIs protegidas
   - JWT técnico de servicio — usado por Repo Turnos → Repo Catálogo (el usuario final no se propaga)

2. **Ningún repo accede a las tablas del otro.** Toda integración es HTTP + JWT.

3. **Usuarios finales viven en Repo Turnos**, no en KMP ni en Repo Catálogo. Repo Turnos emite el JWT propio.

4. **Repo Catálogo no duplica datos en Repo Turnos.** Repo Turnos consulta a Repo Catálogo en tiempo de operación.

5. **`externalPatientId`** es un UUID estable generado en Repo Turnos al registrar un usuario. Es lo que se envía a la cátedra para identificar al paciente.

6. **Credenciales nunca en código fuente.** Usar variables de entorno / `.env` (en `.gitignore`).

## Documentos de la cátedra

Están en la carpeta de Descargas del sistema:
- `/home/valentin/Descargas/PROJECT_STATEMENT-v1(3).md` — enunciado completo del proyecto
- `/home/valentin/Descargas/INTEGRATION_REFERENCE-v2(4).md` — contrato REST/Redis/Kafka con el servicio central de la cátedra

## Próxima tarea recomendada

Ir al **Repo Catálogo — Sprint 1**: implementar la sincronización completa e incremental (Kafka consumer + Redis reader + snapshot REST). Es el cuello de botella del proyecto: Repo Turnos Sprint 2 (disponibilidad) depende de que Catálogo tenga horarios disponibles.
