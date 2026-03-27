# Indice de Teoria Guiada por el Codigo

## Proposito

Este documento vincula conceptos teoricos con clases concretas del repositorio. La intencion es que el lector pueda ir desde la teoria hasta el codigo real y volver.

## 1. Arquitectura por capas

Ver:

- `src/main/java/com/marcos/fractalstudio/domain`
- `src/main/java/com/marcos/fractalstudio/application`
- `src/main/java/com/marcos/fractalstudio/infrastructure`
- `src/main/java/com/marcos/fractalstudio/presentation`

Documentos asociados:

- `implementation-architecture.md`
- `software-engineering-and-design-patterns.md`

## 2. Agregado principal y modelado del proyecto

Ver:

- `domain/project/Project.java`
- `domain/project/ProjectMetadata.java`
- `domain/project/ProjectSettings.java`

Conceptos asociados:

- agregado
- value objects
- invariantes
- consistencia del dominio

## 3. Camara e interpolacion

Ver:

- `domain/camera/CameraState.java`
- `domain/camera/FractalCoordinate.java`
- `domain/camera/ZoomLevel.java`
- `domain/camera/CameraPathInterpolator.java`

Conceptos asociados:

- interpolacion
- precision numerica
- representacion del plano complejo

## 4. Timeline, puntos y secuencia temporal

Ver:

- `domain/timeline/Timeline.java`
- `domain/timeline/Keyframe.java`
- `application/timeline/BuildTimelineSequenceUseCase.java`

Conceptos asociados:

- secuencia temporal
- recorrido de camara
- resolucion de frames intermedios

## 5. Preview y deep zoom

Ver:

- `application/preview/GeneratePreviewUseCase.java`
- `application/preview/AdaptivePreviewQualityPolicy.java`
- `application/preview/ZoomLimitPolicy.java`
- `application/preview/DeepZoomAdvisor.java`

Conceptos asociados:

- calidad adaptativa
- costo computacional
- deep zoom
- politicas de control

## 6. Formulas fractales

Ver:

- `domain/fractal/FractalFormula.java`
- `domain/fractal/MandelbrotFormula.java`
- `domain/fractal/BurningShipFormula.java`
- `domain/fractal/TricornFormula.java`
- `domain/fractal/CelticMandelbrotFormula.java`

Conceptos asociados:

- tiempo de escape
- sistemas dinamicos
- orbita
- radio de escape

## 7. Concurrencia y render batch

Ver:

- `infrastructure/batching/WorkerPoolManager.java`
- `infrastructure/batching/RenderJob.java`
- `infrastructure/batching/JobCancellationToken.java`
- `presentation/shell/StudioPreviewCoordinator.java`

Conceptos asociados:

- jobs
- workers
- tiles
- cancelacion cooperativa
- coordinacion de tareas

## 8. Precision alta

Ver:

- `infrastructure/rendering/HighPrecisionQuadraticSampler.java`
- `infrastructure/rendering/HighPrecisionMandelbrotSampler.java`
- `domain/fractal/FractalIterationMonitor.java`

Conceptos asociados:

- precision extendida
- error numerico
- estabilidad de deep zoom

## 9. Persistencia JSON

Ver:

- `application/project/ProjectRepository.java`
- `infrastructure/persistence/ProjectFileRepository.java`
- `infrastructure/persistence/ProjectSnapshotMapper.java`
- `infrastructure/persistence/ProjectDocument.java`

Conceptos asociados:

- repositorio
- mapper
- DTO
- snapshot
- serializacion

## 10. Video y exportacion

Ver:

- `infrastructure/export/PngFrameSequenceExporter.java`
- `infrastructure/export/Mp4SequenceVideoExporter.java`
- `infrastructure/export/SequenceVideoExporter.java`

Conceptos asociados:

- pipeline multimedia
- codificacion
- artefactos temporales
- salida reproducible

## 11. Estado de la UI

Ver:

- `presentation/shell/StudioShellViewModel.java`
- `presentation/shell/StudioWorkspaceView.java`
- `presentation/explorer/FractalExplorerView.java`
- `presentation/renderqueue/RenderQueueView.java`

Conceptos asociados:

- MVVM pragmatico
- propiedades observables
- coordinacion entre vista y dominio

## 12. Empaquetado e instalacion

Ver:

- `scripts/package-windows.ps1`
- `scripts/generate-windows-icon.py`

Conceptos asociados:

- empaquetado
- runtime embebido
- instalador MSI
- distribucion desktop
