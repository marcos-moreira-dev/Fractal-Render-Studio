# Persistencia JSON y Ciclo de Vida del Proyecto

## 1. Vision general

La persistencia del proyecto se basa en una idea simple pero poderosa:

- el proyecto se modela en dominio
- se traduce a documentos JSON estables
- el usuario guarda y abre proyectos de manera explicita

Esto evita dos extremos malos:

- depender de estados internos ocultos
- acoplar el dominio a la serializacion

## 2. Repositorio de proyecto

La frontera de persistencia esta en:

- `ProjectRepository`
- `ProjectFileRepository`

El repositorio resuelve:

- guardar proyecto
- cargar proyecto
- mantener compatibilidad con versiones anteriores del JSON cuando sea posible

## 3. Mapper de snapshots

`ProjectSnapshotMapper` cumple un rol fundamental:

- traduce dominio a documento
- traduce documento a dominio

Este patron protege al dominio de detalles de serializacion y permite evolucionar el formato.

## 4. Documentos persistidos

La persistencia utiliza varios documentos auxiliares:

- `ProjectDocument`
- `ProjectMetadataDocument`
- `ProjectSettingsDocument`
- `BookmarkDocument`
- `KeyframeDocument`
- otros documentos de color y render

La existencia de estos DTO no es redundante. Permite controlar con precision como viajan los datos al JSON.

## 5. Un solo JSON por proyecto renderizado

Una decision importante del producto fue guardar un unico archivo:

- `project.fractalstudio.json`

Ese archivo vive dentro de la carpeta del render y contiene lo necesario para reabrir el proyecto asociado.

Esto mejora:

- claridad para el usuario
- portabilidad del render
- trazabilidad entre video y proyecto fuente

## 6. Sesion interna efimera

Aunque exista persistencia JSON, la sesion de la aplicacion es intencionalmente efimera:

- al abrir, no se restaura trabajo anterior interno
- al cerrar, se purgan artefactos de sesion

Eso significa que el ciclo de vida del proyecto es explicito y controlado por el usuario.

## 7. Carpeta de render como unidad autocontenida

Cada render crea una carpeta propia con:

- `render.mp4`
- `frames/` si fueron necesarios
- `project.fractalstudio.json`

Esto convierte al render en una unidad autocontenida y reproducible.

## 8. Compatibilidad y evolucion

Un formato JSON util para software real debe tolerar evolucion.

La arquitectura actual favorece:

- campos nuevos en metadata y settings
- compatibilidad razonable con snapshots anteriores
- separacion entre nombres de dominio y forma de persistencia

## 9. Lecciones de diseño

Este modulo es un caso de estudio de:

- mapeo entre dominio y persistencia
- diseño de archivos de proyecto reabribles
- gestion de sesion local sin basura persistente no deseada

## 10. Posibles mejoras futuras

- versionado explicito del schema JSON
- migraciones formales de proyecto
- checksum o validacion de integridad
- exportacion/importacion comprimida del proyecto
