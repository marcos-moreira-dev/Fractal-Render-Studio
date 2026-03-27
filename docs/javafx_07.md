# JavaFX 07 — Estructura real de paquetes y clases

## Propósito de este documento
Este documento aterriza la arquitectura conceptual de Fractal Render Studio en una estructura de paquetes, módulos internos, nombres de clases y responsabilidades concretas.

Su objetivo es evitar dos problemas comunes:

1. quedarse solo en documentos abstractos sin traducción al código real;
2. terminar con una estructura caótica llena de clases genéricas como `Manager`, `Helper` o `Utils`.

Este documento propone una organización razonable para una **aplicación desktop monolítica modular en JavaFX**, con separación entre presentación, aplicación, dominio e infraestructura.

---

# 1. Nombre base sugerido del paquete raíz

## Opción sugerida
```text
com.marcos.fractalstudio
```

## Alternativas válidas
```text
com.marcosmoreira.fractalstudio
com.marcosmor.dev.fractalstudio
```

## Regla
Elegir un único package base y mantenerlo estable durante todo el proyecto.

---

# 2. Estructura general sugerida

```text
com.marcos.fractalstudio
├─ presentation
├─ application
├─ domain
├─ infrastructure
└─ shared
```

---

# 3. Paquetes de `presentation`

## Estructura sugerida
```text
presentation
├─ app
├─ shell
├─ navigation
├─ explorer
├─ timeline
├─ inspector
├─ renderqueue
├─ metrics
├─ dialogs
└─ common
```

## Responsabilidad general
Todo lo que depende de JavaFX y de la experiencia visual del usuario.

---

## 3.1 `presentation.app`

### Clases sugeridas
- `FractalStudioApplication`
- `ApplicationBootstrap`
- `UiCompositionRoot`

### Responsabilidad
- punto de entrada JavaFX;
- inicialización general;
- ensamblado principal de dependencias de la capa visual.

---

## 3.2 `presentation.shell`

### Clases sugeridas
- `StudioShellView`
- `StudioShellController`
- `StudioShellViewModel`
- `StudioScreenMediator`

### Responsabilidad
- ventana principal;
- layout global;
- coordinación de regiones;
- estado visual general del estudio.

---

## 3.3 `presentation.navigation`

### Clases sugeridas
- `Navigator`
- `StudioNavigator`
- `Route`
- `RouteParams`
- `ViewLoader`
- `ViewRegistry`

### Responsabilidad
- navegación entre vistas;
- apertura de diálogos;
- carga de vistas;
- centralización de rutas internas.

---

## 3.4 `presentation.explorer`

### Clases sugeridas
- `FractalExplorerView`
- `FractalExplorerController`
- `FractalExplorerViewModel`
- `ViewportInteractionHandler`
- `ViewportOverlayRenderer`
- `PreviewImagePresenter`

### Responsabilidad
- viewport principal;
- paneo y zoom;
- visualización de preview;
- overlays e interacción del usuario.

---

## 3.5 `presentation.timeline`

### Clases sugeridas
- `TimelineView`
- `TimelineController`
- `TimelineViewModel`
- `KeyframeItemViewModel`
- `TimelineSelectionModel`
- `TimelineCanvasRenderer`

### Responsabilidad
- representación visual de keyframes;
- selección y edición temporal;
- interacción sobre timeline.

---

## 3.6 `presentation.inspector`

### Clases sugeridas
- `InspectorView`
- `InspectorController`
- `InspectorViewModel`
- `CameraInspectorSection`
- `ColorInspectorSection`
- `RenderInspectorSection`
- `KeyframeInspectorSection`

### Responsabilidad
- edición contextual del elemento seleccionado;
- secciones dinámicas del inspector.

---

## 3.7 `presentation.renderqueue`

### Clases sugeridas
- `RenderQueueView`
- `RenderQueueController`
- `RenderQueueViewModel`
- `RenderJobItemViewModel`
- `RenderQueueTableModel`

### Responsabilidad
- visualización de jobs;
- progreso;
- acciones de cancelar;
- acceso a detalle de resultados.

---

## 3.8 `presentation.metrics`

### Clases sugeridas
- `MetricsView`
- `MetricsController`
- `MetricsViewModel`
- `RenderMetricsPresenter`
- `MetricsChartAdapter`

### Responsabilidad
- mostrar tiempos, throughput, frames procesados, workers activos y errores recientes.

---

## 3.9 `presentation.dialogs`

### Clases sugeridas
- `ExportDialogView`
- `ExportDialogController`
- `ProjectSettingsDialogView`
- `RenderDetailsDialogView`
- `ConfirmationDialogFactory`

### Responsabilidad
- ventanas o diálogos secundarios.

---

## 3.10 `presentation.common`

### Clases sugeridas
- `UiThreadExecutor`
- `FxBindingsSupport`
- `ErrorDialogPresenter`
- `NotificationPresenter`

### Responsabilidad
- utilidades estrictamente visuales;
- helpers específicos de JavaFX con propósito claro.

---

# 4. Paquetes de `application`

## Estructura sugerida
```text
application
├─ project
├─ camera
├─ timeline
├─ preview
├─ render
├─ export
├─ metrics
└─ dto
```

## Responsabilidad general
Orquestar casos de uso y coordinar dominio con infraestructura.

---

## 4.1 `application.project`

### Clases sugeridas
- `CreateProjectUseCase`
- `LoadProjectUseCase`
- `SaveProjectUseCase`
- `RenameProjectUseCase`
- `ProjectFacade`

### Responsabilidad
- operaciones de alto nivel sobre proyectos.

---

## 4.2 `application.camera`

### Clases sugeridas
- `NavigateCameraUseCase`
- `CenterCameraUseCase`
- `CaptureCameraStateUseCase`

### Responsabilidad
- acciones relacionadas con el estado de cámara y navegación conceptual.

---

## 4.3 `application.timeline`

### Clases sugeridas
- `AddKeyframeUseCase`
- `UpdateKeyframeUseCase`
- `RemoveKeyframeUseCase`
- `MoveKeyframeUseCase`
- `DuplicateKeyframeUseCase`
- `BuildTimelineSequenceUseCase`

### Responsabilidad
- edición y derivación funcional de la timeline.

---

## 4.4 `application.preview`

### Clases sugeridas
- `GeneratePreviewUseCase`
- `RefreshPreviewUseCase`
- `PreviewFacade`

### Responsabilidad
- coordinación del preview rápido.

---

## 4.5 `application.render`

### Clases sugeridas
- `BuildRenderPlanUseCase`
- `SubmitRenderJobUseCase`
- `CancelRenderJobUseCase`
- `ListRenderJobsUseCase`
- `RenderFacade`

### Responsabilidad
- construcción y envío de renders por lotes.

---

## 4.6 `application.export`

### Clases sugeridas
- `ExportFramesUseCase`
- `ExportVideoUseCase`
- `ExportFacade`

### Responsabilidad
- orquestación de exportación.

---

## 4.7 `application.metrics`

### Clases sugeridas
- `GetRenderMetricsUseCase`
- `GetRenderHistoryUseCase`

### Responsabilidad
- exponer información útil a la UI sobre métricas y observabilidad.

---

## 4.8 `application.dto`

### Clases sugeridas
- `ProjectSummaryDto`
- `CameraStateDto`
- `KeyframeDto`
- `RenderRequestDto`
- `RenderJobStatusDto`
- `MetricsSnapshotDto`

### Responsabilidad
- objetos de intercambio entre capas, especialmente hacia presentation.

---

# 5. Paquetes de `domain`

## Estructura sugerida
```text
domain
├─ project
├─ fractal
├─ camera
├─ timeline
├─ color
├─ render
├─ math
└─ validation
```

## Responsabilidad general
Modelo puro del problema: proyecto, fractales, timeline, cámara, perfiles y reglas conceptuales.

---

## 5.1 `domain.project`

### Clases sugeridas
- `Project`
- `ProjectId`
- `ProjectName`
- `ProjectMetadata`

### Responsabilidad
- representar el agregado principal del sistema.

---

## 5.2 `domain.fractal`

### Clases sugeridas
- `FractalFormula`
- `FractalFormulaType`
- `FractalParameters`
- `MandelbrotFormula`
- `JuliaFormula`
- `MultibrotFormula`
- `FractalFormulaFactory`

### Responsabilidad
- representar fórmulas fractales y sus parámetros.

---

## 5.3 `domain.camera`

### Clases sugeridas
- `CameraState`
- `FractalCoordinate`
- `ZoomLevel`
- `CameraPathInterpolator`
- `CameraInterpolationStrategy`
- `LinearCameraInterpolationStrategy`
- `LogZoomInterpolationStrategy`

### Responsabilidad
- modelar la vista matemática del fractal y su interpolación.

---

## 5.4 `domain.timeline`

### Clases sugeridas
- `Timeline`
- `TimelineSegment`
- `Keyframe`
- `KeyframeId`
- `TimePosition`
- `TimelineSequenceBuilder`
- `TimelineMemento`

### Responsabilidad
- modelar el eje temporal y los keyframes.

---

## 5.5 `domain.color`

### Clases sugeridas
- `ColorProfile`
- `Palette`
- `ColorStop`
- `ColorMappingStrategy`
- `IterationColorMappingStrategy`
- `SmoothIterationColorMappingStrategy`
- `GradientColorMappingStrategy`

### Responsabilidad
- modelar colorización y paletas.

---

## 5.6 `domain.render`

### Clases sugeridas
- `RenderProfile`
- `FrameDescriptor`
- `FrameIndex`
- `Resolution`
- `EscapeParameters`
- `RenderQuality`

### Responsabilidad
- describir conceptualmente qué debe renderizarse.

---

## 5.7 `domain.math`

### Clases sugeridas
- `ComplexNumber`
- `EscapeTimeResult`
- `OrbitTrapResult` futuro
- `InterpolationFunctions`

### Responsabilidad
- objetos matemáticos puros y auxiliares del dominio.

---

## 5.8 `domain.validation`

### Clases sugeridas
- `ProjectRenderabilityValidator`
- `TimelineConsistencyValidator`
- `RenderProfileValidator`

### Responsabilidad
- validar coherencia conceptual del sistema.

---

# 6. Paquetes de `infrastructure`

## Estructura sugerida
```text
infrastructure
├─ persistence
├─ rendering
├─ batching
├─ cache
├─ export
├─ logging
├─ metrics
└─ time
```

## Responsabilidad general
Resolver detalles técnicos concretos: hilos, disco, caché, exportación, colas, métricas y procesos externos.

---

## 6.1 `infrastructure.persistence`

### Clases sugeridas
- `ProjectRepository`
- `ProjectFileRepository`
- `ProjectSnapshotMapper`
- `ProjectDocument`

### Responsabilidad
- guardar y cargar proyectos.

---

## 6.2 `infrastructure.rendering`

### Clases sugeridas
- `FrameRenderer`
- `AbstractFrameRenderer`
- `PreviewFrameRenderer`
- `FinalFrameRenderer`
- `TileRenderer`
- `RenderContext`
- `PixelBufferAssembler`
- `FrameImageWriter`

### Responsabilidad
- cálculo concreto de frames y tiles.

---

## 6.3 `infrastructure.batching`

### Clases sugeridas
- `RenderJob`
- `RenderJobId`
- `RenderJobFactory`
- `FrameTask`
- `TileTask`
- `JobQueue`
- `PriorityJobQueue`
- `WorkerPoolManager`
- `RenderWorker`
- `TaskDispatcher`
- `ProgressAggregator`
- `JobCancellationToken`
- `JobState`

### Responsabilidad
- cola, workers, estados y ejecución por lotes.

---

## 6.4 `infrastructure.cache`

### Clases sugeridas
- `PreviewImageCache`
- `FrameCacheKey`
- `CacheEvictionPolicy`

### Responsabilidad
- cachear previews o artefactos intermedios si hace falta.

---

## 6.5 `infrastructure.export`

### Clases sugeridas
- `FrameSequenceExporter`
- `VideoAssembler`
- `FfmpegVideoAssembler`
- `ExportArtifactRegistry`

### Responsabilidad
- exportar secuencias e integrar ensamblado de video.

---

## 6.6 `infrastructure.logging`

### Clases sugeridas
- `RenderJobLogger`
- `ApplicationLogger`
- `StructuredLogEntry`

### Responsabilidad
- logging técnico del sistema.

---

## 6.7 `infrastructure.metrics`

### Clases sugeridas
- `BatchMetricsCollector`
- `FrameMetrics`
- `RenderJobMetricsSnapshot`
- `MetricsSnapshotMapper`

### Responsabilidad
- capturar y exponer métricas del pipeline.

---

## 6.8 `infrastructure.time`

### Clases sugeridas
- `SystemClockProvider`
- `TimeSource`

### Responsabilidad
- abstraer tiempo si luego se necesita testabilidad o consistencia.

---

# 7. Paquetes de `shared`

## Estructura sugerida
```text
shared
├─ ids
├─ result
├─ errors
└─ events
```

## Responsabilidad general
Tipos pequeños y neutrales, compartibles, sin convertir esto en un basurero global.

### Clases posibles
- `DomainError`
- `ApplicationError`
- `Result<T>`
- `EventId`

---

# 8. Nombres de variables sugeridos

## Buenos ejemplos
- `projectId`
- `projectName`
- `cameraState`
- `frameIndex`
- `frameDescriptor`
- `renderProfile`
- `colorProfile`
- `maxIterations`
- `escapeRadius`
- `outputDirectory`
- `renderContext`
- `timelineSelection`
- `pendingFrameTasks`
- `completedTileCount`
- `currentPreviewImage`

## Evitar
- `data`
- `thing`
- `obj`
- `temp`
- `manager`
- `helper`
- `misc`

---

# 9. Nombres de métodos sugeridos

## Dominio
- `interpolateTo(...)`
- `resolveFrameAt(...)`
- `addKeyframe(...)`
- `removeKeyframe(...)`
- `buildFrameDescriptors(...)`
- `validateRenderability()`

## Aplicación
- `createProject(...)`
- `loadProject(...)`
- `saveProject(...)`
- `generatePreview(...)`
- `submitRenderJob(...)`
- `cancelRenderJob(...)`
- `exportVideo(...)`

## Infraestructura
- `enqueue(...)`
- `dispatch(...)`
- `renderFrame(...)`
- `renderTile(...)`
- `assembleFrame(...)`
- `writeFrameImage(...)`
- `collectMetrics(...)`

---

# 10. Reglas de nombres de clases

## Sí usar
Nombres con rol específico y semántica clara.

Ejemplos:
- `RenderRequestBuilder`
- `TimelineConsistencyValidator`
- `FrameRendererFactory`
- `RenderQueueViewModel`

## Evitar
- `MainService`
- `RenderManager`
- `GeneralHelper`
- `FractalUtils`

Solo usar nombres amplios si el rol realmente es amplio y central, y aun así justificarlo.

---

# 11. Estructura mínima realista para versión 1

Si se quiere arrancar más austero, una estructura inicial razonable podría ser:

```text
com.marcos.fractalstudio
├─ presentation
│  ├─ app
│  ├─ shell
│  ├─ explorer
│  ├─ timeline
│  ├─ inspector
│  └─ renderqueue
│
├─ application
│  ├─ project
│  ├─ preview
│  ├─ timeline
│  ├─ render
│  └─ dto
│
├─ domain
│  ├─ project
│  ├─ fractal
│  ├─ camera
│  ├─ timeline
│  ├─ color
│  └─ render
│
└─ infrastructure
   ├─ persistence
   ├─ rendering
   ├─ batching
   └─ export
```

Esto ya basta para una primera versión limpia.

---

# 12. Estructura posible de arranque por archivo

## Mínimo inicial muy práctico
- `FractalStudioApplication`
- `StudioShellViewModel`
- `FractalExplorerViewModel`
- `TimelineViewModel`
- `CreateProjectUseCase`
- `GeneratePreviewUseCase`
- `SubmitRenderJobUseCase`
- `Project`
- `CameraState`
- `Keyframe`
- `Timeline`
- `FractalFormula`
- `RenderProfile`
- `ColorProfile`
- `FrameDescriptor`
- `PreviewFrameRenderer`
- `RenderJob`
- `WorkerPoolManager`
- `ProjectFileRepository`
- `FfmpegVideoAssembler`

Ese núcleo ya te deja arrancar sin improvisar demasiado.

---

# 13. Qué no crear demasiado pronto

No conviene crear desde el día uno:
- diez factories sin necesidad;
- repositorios para todo;
- interfaces vacías para cada clase;
- adapters para cosas que aún no existen;
- `Mediator` si la pantalla todavía es simple;
- submódulos excesivamente finos.

La estructura debe ser limpia, pero también progresiva.

---

# 14. Estrategia de crecimiento

## Fase 1
Crear estructura base, casos de uso esenciales y rendering preview/final mínimo.

## Fase 2
Añadir cola de render más sólida, timeline mejorada y exportación más completa.

## Fase 3
Expandir fórmulas, colorización, métricas y refinamientos visuales.

La estructura debe permitir crecer sin tener que reorganizar todo el árbol de paquetes cada semana.

---

# 15. Resumen
La estructura real del proyecto debe reflejar la arquitectura conceptual sin caer en nombres vacíos ni clases exageradamente abstractas. Se propone un paquete raíz estable y una organización por capas: `presentation`, `application`, `domain`, `infrastructure` y `shared`.

Dentro de cada capa, los paquetes y clases deben tener nombres explícitos y responsabilidades concretas. El proyecto debe sentirse como una herramienta desktop profesional, y su estructura interna debe transmitir lo mismo: claridad, modularidad y crecimiento controlado.