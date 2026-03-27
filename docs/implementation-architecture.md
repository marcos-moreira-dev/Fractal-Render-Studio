# Arquitectura Implementada

## Objetivo

Esta implementacion busca que `Fractal Render Studio` crezca como producto desktop serio y no como un demo acoplado a JavaFX.

## Capas

### `domain`

Contiene el nucleo conceptual:

- camara fractal
- timeline y keyframes
- perfiles de render
- presets de render
- colorizacion
- configuracion y metadata del proyecto
- validadores del dominio

Reglas:

- no depende de JavaFX
- no depende de IO, JSON ni concurrencia concreta
- concentra invariantes e interpolacion

### `application`

Orquesta casos de uso y fachadas:

- `ProjectFacade`
- `RenderFacade`
- `RenderHistoryFacade`
- casos de uso de crear, guardar, cargar, preview y render

Reglas:

- coordina dominio e infraestructura
- expone contratos limpios a `presentation`

### `infrastructure`

Resuelve detalles tecnicos concretos:

- persistencia JSON con Jackson
- persistencia JSON de historial de renders
- cola de render en memoria
- cancelacion cooperativa por token
- exportacion PNG y ZIP
- metricas basicas del batch
- renderer de preview/final reutilizando el mismo modelo conceptual

### `presentation`

Implementa la app JavaFX:

- shell principal
- content host con navegacion por rutas
- explorer
- inspector
- timeline
- render queue
- metricas y logs
- dialogos de proyecto y render

## Decisiones clave

### Timeline inmutable

La timeline ya no se muta desde fuera. Cada operacion devuelve una nueva instancia. Esto simplifica validacion, pruebas y evolucion futura.

### Project como agregado

`Project` agrupa formula, timeline, perfiles y reglas de renderabilidad. La UI no deberia alterar internamente estas piezas por separado sin pasar por casos de uso o metodos del agregado.

Actualmente tambien encapsula:

- `ProjectMetadata`
- `ProjectSettings`

Eso permite que decisiones como FPS por defecto, cantidad base de frames, preset de render por defecto y trazabilidad temporal vivan en el dominio y no dispersas por la UI.

### Facades para presentation

La shell JavaFX ya no consume varios casos de uso sueltos. Usa:

- `ProjectFacade`
- `RenderFacade`
- `RenderHistoryFacade`

Esto reduce acoplamiento y deja una frontera mas limpia entre UI y logica de aplicacion.

### Navegacion de escritorio mas formal

La presentacion ya no depende solo de una unica composicion fija.

Se añadio:

- `StudioNavigator`
- `RouteSpec`
- `ViewRegistry`
- `StudioWorkspaceView`
- `MetricsScreenView`

La shell ahora actua como contenedor estable con `ContentHost`, y las vistas se resuelven por ruta registrada.

### Navegacion secundaria y dialogos

Se añadio un conjunto de interacciones secundarias formales en `presentation.dialogs`:

- configuracion del proyecto
- configuracion del render
- detalle de job de render

Esto evita seguir metiendo formularios de configuracion directamente dentro de la toolbar o de tablas.

### Persistencia explicita

Se añadio `ProjectRepository` como contrato y `ProjectFileRepository` como implementacion JSON. Tambien se añadio persistencia de historial de renders para que la cola visible no se pierda completamente entre sesiones.

### Render batch con artefactos reales

La cola de render ya no es solo simulacion de progreso:

- genera frames reales
- exporta PNG
- exporta ZIP
- mantiene estado del job
- soporta cancelacion cooperativa
- calcula duracion basica

### Preview y render final separados

El sistema ya no reutiliza exactamente el mismo renderer para todo.

- `PreviewFrameRenderer` prioriza velocidad
- `FinalFrameRenderer` aplica muestreo superior para mejor salida
- `FrameRendererFactory` resuelve la estrategia segun `RenderQuality`

### Presets de render

El sistema ya soporta presets de render:

- `DRAFT`
- `STANDARD`
- `DEEP_ZOOM`

El preset viaja desde settings del proyecto o desde el dialogo de render hasta el `RenderRequest`, y se traduce en un `RenderProfile` efectivo sin mutar el perfil base del proyecto.

### Configuracion explicita de la app

La app carga propiedades de runtime desde `app.properties`:

- titulo
- tamaño inicial de ventana
- ruta de storage
- numero de hilos para preview y render

Esto evita seguir fijando estos valores en clases Java.

## Proximos pasos profesionales recomendados

1. Introducir pruebas de integracion para render/export y carga/guardado.
2. Añadir ensamblado de video como subsistema aislado de exportacion.
3. Incorporar dialogo formal de exportacion separado del render.
4. Añadir perfiles de color y render editables desde UI, no solo presets.
5. Separar completamente el arranque del primer preview para evitar cualquier acoplamiento con layout inicial.
