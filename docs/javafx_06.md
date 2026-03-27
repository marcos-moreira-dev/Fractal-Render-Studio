# JavaFX 06 — Patrones de diseño

## Propósito de este documento
Este documento define los **patrones de diseño** que tienen sentido dentro de Fractal Render Studio y explica **dónde**, **por qué** y **cómo** aplicarlos.

La intención no es llenar el proyecto de patrones por exhibición, sino usarlos como herramientas estructurales para mantener:

- separación de responsabilidades;
- extensibilidad;
- claridad conceptual;
- buena organización entre dominio, aplicación, infraestructura y presentación.

---

# 1. Principio rector

## Regla principal
En este proyecto, un patrón debe entrar por una de estas razones:

1. reduce acoplamiento real;
2. permite variación controlada;
3. organiza un flujo complejo;
4. mejora expresividad del dominio o de la infraestructura;
5. prepara crecimiento sin volver el sistema rígido.

## Regla de rechazo
Un patrón **no** debe entrar si:

- solo añade capas vacías;
- reemplaza un `enum`, una clase simple o una función clara sin beneficio real;
- obliga a crear interfaces absurdas con una sola implementación sin razón;
- vuelve más difícil leer el sistema.

---

# 2. Clasificación por área del proyecto

Los patrones se agrupan aquí en cuatro zonas:

1. patrones del dominio;
2. patrones de aplicación;
3. patrones de infraestructura;
4. patrones de presentación.

Esto evita mezclar patrones de UI con patrones matemáticos o de batching.

---

# 3. Patrones principales elegidos

## Lista base del proyecto
1. Strategy
2. Template Method
3. Builder
4. Command
5. Memento
6. Factory Method
7. Abstract Factory
8. Observer
9. State
10. Facade
11. Repository
12. Mediator

## Patrones secundarios opcionales
13. Prototype
14. Chain of Responsibility
15. Visitor
16. Adapter

---

# 4. Strategy

## Intención
Encapsular variaciones de comportamiento intercambiable sin llenar el sistema de `if/else` gigantes.

## Dónde usarlo en este proyecto

### 4.1 Estrategias de fórmula fractal
Ejemplo conceptual:
- `MandelbrotFormulaStrategy`
- `JuliaFormulaStrategy`
- `MultibrotFormulaStrategy`

### 4.2 Estrategias de colorización
Ejemplo conceptual:
- `IterationColorMappingStrategy`
- `SmoothIterationColorMappingStrategy`
- `GradientColorMappingStrategy`
- `AnalyticalSlopeColorMappingStrategy` si se agrega análisis posterior

### 4.3 Estrategias de interpolación
Ejemplo conceptual:
- `LinearCameraInterpolationStrategy`
- `EaseInOutCameraInterpolationStrategy`
- `LogZoomInterpolationStrategy`

### 4.4 Estrategias de partición de trabajo
Ejemplo conceptual:
- `FrameOnlyPartitionStrategy`
- `UniformTilePartitionStrategy`
- `AdaptiveTilePartitionStrategy` futuro

### 4.5 Estrategias de preview vs render final
Ejemplo conceptual:
- `FastPreviewRenderStrategy`
- `HighQualityRenderStrategy`

## Beneficio
Permite que el producto crezca en fórmulas, paletas, interpolaciones y batching sin destruir clases existentes.

---

# 5. Template Method

## Intención
Definir el esqueleto de un proceso, dejando pasos variables para subclases o especializaciones controladas.

## Dónde usarlo

### 5.1 Pipeline de render por frame
Un flujo típico de render puede tener esta forma:
1. preparar contexto;
2. calcular píxeles o tiles;
3. colorear;
4. postprocesar;
5. publicar resultado.

Esto puede expresarse en una clase base tipo:
- `AbstractFrameRenderer`

Y derivar especializaciones como:
- `PreviewFrameRenderer`
- `FinalFrameRenderer`
- `AnalyticalFrameRenderer`

### 5.2 Exportación
También podría usarse para pipeline de exportación:
1. validar artefactos;
2. preparar salida;
3. ensamblar;
4. registrar resultado;
5. notificar finalización.

## Beneficio
Organiza flujos largos y repetitivos sin copiar lógica estructural.

---

# 6. Builder

## Intención
Construir objetos complejos paso a paso, evitando constructores gigantes y parámetros desordenados.

## Dónde usarlo

### 6.1 Construcción de requests
- `RenderRequestBuilder`
- `VideoExportRequestBuilder`
- `PreviewRequestBuilder`

### 6.2 Construcción de snapshots o descriptores
- `FrameDescriptorBuilder`
- `ProjectSnapshotBuilder`
- `TimelineSequenceBuilder`

### 6.3 Construcción de perfiles
- `RenderProfileBuilder`
- `ColorProfileBuilder`

## Beneficio
Aumenta legibilidad y reduce errores cuando hay muchos parámetros opcionales o combinables.

---

# 7. Command

## Intención
Representar acciones del usuario o del sistema como objetos explícitos.

## Dónde usarlo

### 7.1 Edición de timeline
- `AddKeyframeCommand`
- `RemoveKeyframeCommand`
- `MoveKeyframeCommand`
- `UpdateKeyframeCommand`

### 7.2 Acciones del proyecto
- `CreateProjectCommand`
- `SaveProjectCommand`
- `LoadProjectCommand`

### 7.3 Acciones de producción
- `SubmitRenderCommand`
- `CancelRenderCommand`
- `ExportSequenceCommand`

## Beneficio
Permite:
- encapsular acciones;
- soportar undo/redo;
- desacoplar UI de la ejecución concreta;
- registrar historial de acciones si luego hace falta.

---

# 8. Memento

## Intención
Capturar y restaurar estados internos sin exponer toda la estructura de un objeto.

## Dónde usarlo

### 8.1 Undo/redo de timeline
- `TimelineMemento`

### 8.2 Snapshot de proyecto
- `ProjectMemento`

### 8.3 Restauración de configuración visual
- `ExplorerStateMemento`

## Beneficio
Se complementa naturalmente con Command para edición reversible.

## Regla
No usarlo para cualquier cosa. Debe reservarse para estados complejos que realmente necesiten reversibilidad o snapshot limpio.

---

# 9. Factory Method

## Intención
Delegar la creación de objetos a métodos de fábrica especializados.

## Dónde usarlo

### 9.1 Fórmulas fractales
- `FractalFormulaFactory`

### 9.2 Colorizadores
- `ColorMappingFactory`

### 9.3 Renderers puntuales
- `FrameRendererFactory`

### 9.4 Jobs o tareas
- `RenderJobFactory`
- `FrameTaskFactory`

## Beneficio
Evita lógica de creación dispersa y mejora cohesión cuando la instancia depende de configuración o tipo.

---

# 10. Abstract Factory

## Intención
Crear familias coherentes de objetos relacionados.

## Dónde usarlo

### 10.1 Modos de render
Una familia puede definir conjuntamente:
- renderer;
- colorizador;
- estrategia de partición;
- política de preview;
- reportador de progreso.

Ejemplo conceptual:
- `PreviewRenderComponentFactory`
- `FinalRenderComponentFactory`
- `AnalysisRenderComponentFactory`

### 10.2 Familias de exportación
- exportación de secuencia de imágenes
- exportación de video
- exportación analítica futura

## Beneficio
Sirve cuando varios componentes deben cambiar juntos de manera consistente.

---

# 11. Observer

## Intención
Permitir que distintos componentes reaccionen a cambios de estado sin acoplamiento directo fuerte.

## Dónde usarlo

### 11.1 Progreso de render
La cola, el panel de métricas y la UI pueden reaccionar al progreso del job.

### 11.2 Estado del proyecto
La shell puede reaccionar a modificaciones del proyecto.

### 11.3 Contexto de selección
Inspector, timeline y viewport pueden reaccionar a cambios de selección.

## Regla importante
En el dominio no debe colarse JavaFX como mecanismo nativo de observación. La adaptación a `ObservableValue` o propiedades JavaFX debe quedarse en `presentation`.

## Beneficio
Favorece reactividad controlada y reduce dependencias directas entre subsistemas.

---

# 12. State

## Intención
Modelar comportamientos dependientes del estado sin llenar el sistema de condicionales repetitivos.

## Dónde usarlo

### 12.1 Estado de trabajos de render
Un job puede pasar por:
- `QUEUED`
- `PREPARING`
- `RENDERING`
- `PAUSED`
- `COMPLETED`
- `FAILED`
- `CANCELLED`

### 12.2 Estado de exportación
- pendiente
- ejecutando
- completado
- fallido

### 12.3 Estado de herramientas UI
Si más adelante existen herramientas explícitas de interacción:
- modo paneo
- modo zoom
- modo selección
- modo edición de keyframe

## Regla práctica
En versión 1 se puede empezar con enum + transiciones claras. Si el comportamiento se vuelve complejo, migrar a State pattern real.

---

# 13. Facade

## Intención
Ofrecer una interfaz simple para un subsistema complejo.

## Dónde usarlo

### 13.1 Casos de uso de render
- `RenderFacade`

### 13.2 Proyecto y persistencia
- `ProjectFacade`

### 13.3 Exportación
- `ExportFacade`

## Beneficio
La UI y los view models no necesitan conocer la maquinaria completa detrás de cada operación.

---

# 14. Repository

## Intención
Abstraer el acceso a almacenamiento o recuperación de agregados del sistema.

## Dónde usarlo

### 14.1 Proyecto
- `ProjectRepository`

### 14.2 Presets
- `RenderProfileRepository`
- `ColorProfileRepository`

### 14.3 Historial de renders
- `RenderHistoryRepository` si luego se justifica

## Regla
Repository no es un “contenedor universal de todo”. Debe usarse para agregados o conjuntos de datos con identidad clara.

---

# 15. Mediator

## Intención
Coordinar componentes de UI sin que se acoplen entre sí de forma caótica.

## Dónde usarlo

### 15.1 Shell principal
Un coordinador puede sincronizar:
- explorer;
- timeline;
- inspector;
- render queue;
- métricas.

Ejemplos:
- seleccionar un keyframe en timeline actualiza inspector y viewport;
- cambiar paleta en inspector dispara preview;
- finalizar render actualiza cola y métricas.

## Beneficio
Evita que cada panel conozca directamente a todos los demás.

---

# 16. Prototype

## Intención
Clonar objetos configurados sin reconstruirlos desde cero.

## Dónde podría servir
- duplicar keyframes;
- clonar perfiles de render;
- clonar perfiles de color;
- duplicar configuraciones de proyecto base.

## Estado
Opcional. No es imprescindible en la primera versión si el clonado es trivial.

---

# 17. Chain of Responsibility

## Intención
Procesar una solicitud mediante una secuencia de pasos desacoplados.

## Dónde podría servir
- validación de requests de render;
- postprocesado visual;
- pipeline de exportación;
- filtros analíticos encadenables.

## Estado
Secundario. Solo usarlo si aparecen cadenas reales de pasos configurables.

---

# 18. Visitor

## Intención
Separar operaciones sobre una estructura estable de objetos.

## Dónde podría servir
- exportar estructuras de proyecto a distintos formatos;
- recorrer timeline con varias operaciones;
- análisis sobre árbol de proyecto si llega a existir.

## Estado
No obligatorio en versión 1. Puede resultar útil más adelante si el modelo crece y las operaciones sobre él se multiplican.

---

# 19. Adapter

## Intención
Adaptar una interfaz externa a una forma que el sistema pueda usar limpiamente.

## Dónde usarlo
- integración con FFmpeg;
- adaptación de librerías externas de color o exportación;
- envoltorio de mecanismos de cache o archivos.

## Beneficio
Aísla dependencias técnicas externas y evita contaminar el resto del sistema.

---

# 20. Patrones por capa

## En `domain`
Más naturales:
- Strategy
- Builder
- Factory Method
- Prototype opcional
- Visitor opcional

## En `application`
Más naturales:
- Facade
- Command
- Builder
- Repository (como contrato)

## En `infrastructure`
Más naturales:
- Template Method
- Factory Method
- Abstract Factory
- State
- Adapter
- Chain of Responsibility opcional

## En `presentation`
Más naturales:
- Observer
- Mediator
- State opcional para herramientas
- Facade de acceso a casos de uso

---

# 21. Combinaciones importantes

## Command + Memento
Sirve para edición reversible de timeline y proyecto.

## Strategy + Factory Method
Sirve para elegir y crear fórmulas, colorizadores o interpoladores sin condicionales enormes.

## Facade + Application Layer
Sirve para exponer operaciones limpias a la UI.

## Observer + Mediator
Sirve para lograr UI reactiva sin acoplamiento entre paneles.

## Template Method + Abstract Factory
Sirve para definir pipelines de render con familias coherentes de componentes.

---

# 22. Qué patrones NO forzar en versión 1

En la primera versión no se deben forzar si todavía no hay presión real:

- Bridge
- Flyweight
- Interpreter
- Proxy
- Singleton generalizado
- Visitor si no existe árbol o estructura rica que lo pida

## Nota sobre Singleton
Evitar usar Singleton como atajo para `Navigator`, repositorios, colas o coordinadores. Si se necesita una única instancia, se debe resolver desde el ensamblado de la aplicación, no con acceso global indiscriminado.

---

# 23. Patrones y nombres sugeridos

## Ejemplos de nombres correctos
- `FractalFormulaStrategy`
- `ColorMappingStrategy`
- `TimelineInterpolationStrategy`
- `RenderRequestBuilder`
- `AddKeyframeCommand`
- `ProjectMemento`
- `FrameRendererFactory`
- `PreviewRenderComponentFactory`
- `RenderFacade`
- `ProjectRepository`
- `StudioScreenMediator`
- `JobState`

## Evitar nombres vagos
- `Manager`
- `Handler`
- `Helper`
- `Processor` si no queda claro qué procesa
- `Service` para cualquier cosa sin criterio

---

# 24. Orden de implementación recomendado

## Primera tanda
- Strategy
- Builder
- Command
- Facade
- Repository
- Observer

## Segunda tanda
- Template Method
- Factory Method
- Mediator
- State

## Tercera tanda
- Abstract Factory
- Memento
- Adapter
- Prototype o Chain of Responsibility si de verdad se justifican

Esto permite crecer de forma progresiva, sin intentar meter todos los patrones desde el día uno.

---

# 25. Resumen
Fractal Render Studio sí es un proyecto adecuado para usar muchos patrones de diseño, pero deben aplicarse con criterio. Los más importantes del sistema son Strategy, Template Method, Builder, Command, Memento, Factory Method, Abstract Factory, Observer, State, Facade, Repository y Mediator.

La clave no está en “usar muchos patrones”, sino en que cada uno responda a una tensión real del diseño: variación de fórmulas, edición reversible, pipeline de render, cola de jobs, desacoplamiento de UI y organización de la infraestructura.

Si se aplican así, los patrones no se verán como adorno académico, sino como parte natural de la arquitectura del producto.