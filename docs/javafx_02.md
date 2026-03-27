# JavaFX 02 — Arquitectura y capas

## Propósito de este documento
Este documento define la arquitectura base de **Fractal Render Studio** y establece cómo se separan las responsabilidades entre capas y módulos. Su objetivo principal es evitar acoplamientos innecesarios entre:

- la interfaz gráfica en JavaFX;
- el dominio fractal y temporal;
- la infraestructura de procesamiento por lotes;
- la persistencia y exportación.

La idea no es aplicar arquitectura por moda, sino construir una base que permita crecer el sistema sin que se convierta en una mezcla caótica de UI, cálculos, hilos y archivos.

---

# 1. Estilo arquitectónico elegido

## Decisión principal
El sistema se construirá como un:

> **monolito modular desktop**, con separación por capas y tendencia “clean/hexagonal” sin fanatismo.

Esto significa:

- una sola aplicación de escritorio;
- varios módulos internos bien delimitados;
- reglas explícitas de dependencia;
- separación fuerte entre presentación, aplicación, dominio e infraestructura.

## Qué NO será
El sistema no será:

- un conjunto de controladores JavaFX que llaman todo directamente;
- un proyecto “utilitario” plano sin capas;
- una microarquitectura innecesaria con exceso de interfaces vacías;
- un backend con frontend separado;
- un motor distribuido entre máquinas en esta etapa.

---

# 2. Capas principales

La arquitectura se organiza en cuatro capas principales:

1. `presentation`
2. `application`
3. `domain`
4. `infrastructure`

Opcionalmente puede existir una quinta zona pequeña compartida:

5. `shared` o `kernel`

---

# 3. Capa `presentation`

## Responsabilidad
Contener todo lo relacionado con la experiencia visual del usuario en JavaFX.

## Incluye
- vistas;
- controladores o view models;
- navegación;
- mediación entre paneles;
- binding de propiedades visuales;
- validación superficial de entrada de usuario;
- interacción con casos de uso.

## Ejemplos de componentes
- viewport fractal;
- timeline visual;
- panel inspector;
- render queue;
- panel de métricas;
- navegación principal;
- diálogos modales.

## Reglas
- no debe contener lógica matemática de fractales;
- no debe decidir cómo se planifica un render batch;
- no debe usar directamente repositorios de persistencia;
- no debe construir por sí sola la lógica del dominio.

## Dependencias permitidas
`presentation` puede depender de:
- `application`
- tipos DTO o modelos de vista

No debe depender directamente de `domain` si eso ensucia demasiado la UI con conceptos internos. Si se decide exponer conceptos de dominio, debe hacerse con criterio y sin romper encapsulación.

---

# 4. Capa `application`

## Responsabilidad
Orquestar el comportamiento del sistema mediante casos de uso.

Es la capa que conecta la intención del usuario con el dominio y con la infraestructura necesaria para ejecutar acciones reales.

## Incluye
- casos de uso;
- fachadas de aplicación;
- servicios de orquestación;
- ensamblado de requests/responses;
- coordinación entre dominio e infraestructura;
- políticas de aplicación.

## Ejemplos de acciones de aplicación
- crear proyecto;
- agregar keyframe;
- generar preview;
- construir timeline efectiva;
- crear render plan;
- enviar trabajo a cola de render;
- cancelar job;
- exportar secuencia.

## Reglas
- puede usar entidades y servicios del dominio;
- puede invocar gateways, repositorios e infraestructura mediante interfaces o contratos claros;
- no debe contener código visual JavaFX;
- no debe llenarse de detalles matemáticos finos que pertenecen al dominio;
- no debe volverse un “manager gigante” ambiguo.

## Rol conceptual
Esta es la capa intermedia que tú intuías. Es la capa que evita que la UI toque directamente ni la matemática ni la ejecución batch concreta.

---

# 5. Capa `domain`

## Responsabilidad
Modelar los conceptos esenciales del sistema.

## Incluye
- entidades del proyecto;
- cámara;
- keyframes;
- timeline;
- fórmulas fractales;
- perfiles de render;
- perfiles de color;
- descriptores de frame;
- reglas e invariantes conceptuales.

## Reglas
- no conoce JavaFX;
- no conoce `ExecutorService`, `Task`, `Thread` ni colas concretas;
- no conoce JSON, disco, FFmpeg ni archivos;
- debe ser el núcleo más estable del sistema.

## Qué sí puede tener
- objetos de valor;
- servicios de dominio;
- interfaces puras si el modelo lo necesita;
- validaciones conceptuales;
- lógica matemática y temporal propia del producto.

---

# 6. Capa `infrastructure`

## Responsabilidad
Resolver los detalles técnicos concretos necesarios para hacer funcionar la aplicación.

## Incluye
- procesamiento por lotes;
- pools de workers;
- colas de jobs;
- persistencia;
- caché;
- exportación a PNG;
- ensamblado de video;
- logs;
- medición técnica;
- integración con herramientas externas.

## Ejemplos
- `RenderWorker`
- `JobQueue`
- `ProjectFileRepository`
- `PreviewImageCache`
- `FfmpegVideoAssembler`
- `BatchMetricsCollector`

## Reglas
- no debe decidir el significado conceptual del dominio;
- no debe invadir la UI;
- debe ofrecer implementaciones concretas para contratos usados por aplicación;
- puede depender de librerías externas y del JDK concreto.

---

# 7. Zona `shared` o `kernel`

## Cuándo usarla
Solo si hace falta centralizar tipos pequeños, neutrales y reusables.

## Posibles contenidos
- IDs tipados;
- `Result` o `Either` simples;
- errores base;
- utilidades pequeñas de validación;
- eventos genéricos.

## Regla importante
No debe volverse un basurero global. Si algo pertenece claramente a una capa, debe quedarse en esa capa.

---

# 8. Regla de dependencias

La dirección general de dependencias debe ser esta:

```text
presentation -> application -> domain
presentation -> application -> infrastructure (solo indirectamente a través de application)
infrastructure -> domain
```

## Lectura práctica
- `presentation` nunca debe hablar directamente con `infrastructure` salvo casos muy justificados y controlados.
- `domain` no depende de ninguna capa externa.
- `application` coordina.
- `infrastructure` implementa.

## Regla fuerte
Si un controlador JavaFX necesita tocar un thread pool, una cola o FFmpeg directamente, la arquitectura ya se está degradando.

---

# 9. Módulos internos sugeridos

## En `presentation`
- `shell`
- `navigation`
- `explorer`
- `timeline`
- `inspector`
- `renderqueue`
- `metrics`
- `common`

## En `application`
- `project`
- `camera`
- `timeline`
- `preview`
- `render`
- `export`
- `dto`

## En `domain`
- `project`
- `fractal`
- `camera`
- `timeline`
- `color`
- `render`
- `math`
- `job` (solo si existe concepto puro de job; no el job técnico de infraestructura)

## En `infrastructure`
- `batching`
- `rendering`
- `persistence`
- `cache`
- `export`
- `logging`
- `metrics`
- `time`

---

# 10. Arquitectura del flujo principal

## Flujo conceptual completo
1. El usuario navega el fractal en la UI.
2. La UI solicita un caso de uso de aplicación.
3. La capa de aplicación consulta/modifica el dominio.
4. Si hace falta preview o render, la aplicación crea requests hacia infraestructura.
5. Infraestructura ejecuta el trabajo concreto.
6. Los resultados vuelven a aplicación.
7. Presentation los adapta y los muestra.

## Ejemplo: agregar keyframe
1. Usuario pulsa “Agregar keyframe”.
2. `presentation` recopila el estado visual actual.
3. `application` lo transforma en un comando/caso de uso.
4. `domain` valida y agrega el keyframe a la timeline.
5. `application` devuelve una respuesta de actualización.
6. `presentation` refresca timeline e inspector.

## Ejemplo: render batch
1. Usuario pulsa “Render”.
2. `presentation` recoge parámetros.
3. `application` construye un `RenderRequest`.
4. `application` deriva `FrameDescriptor` y arma un plan.
5. `infrastructure.batching` ejecuta los jobs.
6. `infrastructure.rendering` genera frames.
7. `infrastructure.export` guarda resultados.
8. `presentation` observa progreso y estados.

---

# 11. Subarquitectura de UI

## Recomendación
Usar una variante simple de:

> **View + ViewModel + Application Use Case**

## Esquema
```text
View -> ViewModel -> Application
```

## Detalle
- `View`: FXML o vista programática JavaFX.
- `ViewModel`: estado observable y comandos de UI.
- `Application`: lógica orquestadora.

## Beneficio
Evita que los controladores JavaFX se conviertan en el centro de todo.

---

# 12. Navegación y coordinación visual

## `Navigator`
Debe existir un componente de navegación en `presentation.navigation` encargado de:
- cambiar vistas;
- abrir modales;
- manejar rutas internas.

No pertenece al dominio ni a la infraestructura de render.

## `Mediator`
Puede existir en pantallas complejas para coordinar paneles como:
- explorer;
- timeline;
- inspector;
- render queue.

Ejemplo: seleccionar un keyframe en la timeline debe reflejar cambios en inspector y viewport sin acoplar directamente todos los paneles entre sí.

---

# 13. Arquitectura del procesamiento por lotes

La infraestructura de batch processing es una parte clave del sistema, pero no define por sí sola el producto.

## Conceptos técnicos principales
- `RenderJob`
- `FrameTask`
- `TileTask`
- `JobQueue`
- `WorkerPoolManager`
- `ProgressAggregator`
- `JobCancellationToken`

## Ubicación
Todos estos conceptos viven principalmente en `infrastructure.batching` y `infrastructure.rendering`, aunque algunos requests/responses se expresen desde `application`.

## Regla importante
La UI no debe saber cómo se reparten tiles ni cómo se coordinan threads. Solo debe observar estados, progreso y resultados.

---

# 14. Contratos entre capas

## Application hacia Infrastructure
La capa `application` debería depender de contratos claros como:
- repositorios de proyecto;
- servicio de preview;
- submitter de render jobs;
- exportador de video;
- proveedor de métricas.

## Beneficio
Esto permite cambiar implementaciones sin romper el modelo central ni la UI.

---

# 15. Errores y resultados

## Regla general
Cada capa debe manejar errores a su nivel.

### Presentation
- mensajes amigables;
- estados visuales;
- notificaciones.

### Application
- traducción de errores técnicos a respuestas útiles;
- coordinación de fallos de casos de uso.

### Domain
- validación conceptual;
- rechazo de estados inválidos.

### Infrastructure
- errores de IO;
- errores de proceso externo;
- fallos de render;
- problemas de concurrencia.

---

# 16. Criterios de calidad arquitectónica

La arquitectura debe favorecer:

- separación clara de responsabilidades;
- testabilidad del dominio;
- posibilidad de evolucionar la UI sin destruir la lógica;
- posibilidad de cambiar infraestructura sin alterar el núcleo conceptual;
- claridad de nombres y dependencias;
- crecimiento progresivo sin reescritura masiva.

---

# 17. Decisiones explícitas

## Se decide que:
- JavaFX vive en `presentation`.
- La lógica de orquestación vive en `application`.
- El modelo puro vive en `domain`.
- La ejecución concreta, persistencia y exportación viven en `infrastructure`.
- El sistema será un monolito modular, no una arquitectura distribuida.
- El batch processing es un subsistema técnico clave, pero no reemplaza al dominio.

## No se decide todavía:
- si todo será con FXML o con vistas programáticas;
- si se usará DI formal o wiring manual moderado;
- si la persistencia será JSON puro, ZIP de proyecto u otro formato híbrido;
- si el preview usará `Canvas`, `ImageView` o ambos según la pantalla.

---

# 18. Resumen
La arquitectura de Fractal Render Studio se basa en un monolito modular desktop con cuatro capas principales: presentación, aplicación, dominio e infraestructura. La capa de aplicación actúa como bisagra entre la UI y el sistema técnico, evitando mezclar JavaFX, matemática fractal y batch processing en un mismo lugar.

El objetivo no es solo “que funcione”, sino que el sistema mantenga una forma interna coherente, extensible y clara mientras crece en complejidad visual y técnica.

