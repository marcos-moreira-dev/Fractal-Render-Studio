# JavaFX 04 — Pipeline de render y batching

## Propósito de este documento
Este documento define el subsistema técnico de **render por lotes** de Fractal Render Studio. Su objetivo es dejar claro cómo se transforma una secuencia conceptual de frames en una ejecución concreta por jobs, subtareas, workers, artefactos y métricas.

Este documento no define la matemática del fractal en detalle ni la UI, sino la **infraestructura de producción visual** que hace posible pasar de una timeline conceptual a una salida renderizada.

---

# 1. Visión general

El render en este sistema no debe entenderse como una llamada monolítica del tipo “renderiza todo y ya”, sino como un **pipeline de producción** compuesto por fases bien separadas.

## Idea central
La secuencia de producción es:

1. El usuario define una trayectoria o composición.
2. La capa de aplicación deriva una secuencia ordenada de frames conceptuales.
3. La infraestructura transforma esa secuencia en trabajos ejecutables.
4. Los trabajos se reparten a workers.
5. Se producen imágenes intermedias o finales.
6. Se agregan métricas, estados y resultados.
7. Opcionalmente se ensamblan artefactos mayores, como video.

---

# 2. Objetivo funcional del pipeline

El pipeline de render debe permitir:

- renderizar una imagen fija o una secuencia completa;
- procesar trabajo en segundo plano;
- dividir trabajo en unidades manejables;
- observar progreso y estados;
- cancelar trabajos;
- diferenciar preview y render final;
- generar artefactos exportables;
- recopilar métricas técnicas.

---

# 3. Niveles conceptuales del render

## 3.1 Nivel de proyecto
El usuario trabaja sobre un `Project` que contiene timeline, fórmula, perfiles y configuraciones.

## 3.2 Nivel de secuencia
La timeline y el fps generan una colección ordenada de `FrameDescriptor`.

## 3.3 Nivel de trabajo
Una secuencia renderizable se materializa como un `RenderJob`.

## 3.4 Nivel de frame
Cada frame puede ser tratado como una unidad de trabajo independiente: `FrameTask`.

## 3.5 Nivel de tile
Un frame grande puede subdividirse en regiones rectangulares: `TileTask`.

Esta jerarquía es importante porque permite escalar el render sin contaminar el dominio con threads o colas concretas.

---

# 4. Conceptos principales del subsistema

## 4.1 `RenderRequest`
Representa la intención de render desde el punto de vista de aplicación.

### Contiene típicamente
- proyecto o referencia al proyecto;
- rango de frames;
- fps;
- resolución;
- render profile;
- color profile;
- calidad;
- ruta o destino conceptual de salida;
- opciones de exportación.

### Rol
Es el punto de entrada al pipeline de render.

---

## 4.2 `RenderPlan`
Representa el plan derivado de un `RenderRequest`.

### Contiene típicamente
- secuencia de `FrameDescriptor`;
- estrategia de partición;
- política de ejecución;
- perfil de salida;
- metadatos operativos.

### Rol
Traducir la intención de alto nivel a una estructura ejecutable por infraestructura.

---

## 4.3 `RenderJob`
Representa un trabajo principal de render dentro de la cola.

### Contenido típico
- identificador del job;
- estado;
- prioridad;
- número total de frames;
- progreso agregado;
- timestamps;
- errores si existen;
- artefactos generados.

### Estado típico
- `QUEUED`
- `PREPARING`
- `RENDERING`
- `PAUSED`
- `COMPLETED`
- `FAILED`
- `CANCELLED`

### Rol
Ser la unidad observable de trabajo desde la perspectiva del sistema y de la UI.

---

## 4.4 `FrameTask`
Representa la unidad de trabajo asociada a un frame individual.

### Contenido típico
- índice de frame;
- `FrameDescriptor`;
- estado local;
- progreso interno;
- referencia al job padre.

### Rol
Permitir paralelizar por frame y rastrear resultados a granularidad intermedia.

---

## 4.5 `TileTask`
Representa una subdivisión espacial de un frame.

### Contenido típico
- índice de frame;
- región rectangular del frame;
- contexto de render;
- estado;
- resultado parcial.

### Cuándo existe
No todos los modos de render requieren tiles. Se vuelve especialmente útil cuando:
- la resolución es alta;
- el preview no basta;
- el cálculo por frame es costoso;
- se desea mejor aprovechamiento del pool de workers.

---

# 5. Diferencia entre preview y render final

## Preview
El preview existe para exploración y respuesta interactiva.

### Propiedades esperadas
- menor calidad o resolución efectiva;
- respuesta más rápida;
- cancelación frecuente;
- prioridad a la percepción de fluidez;
- puede reutilizar caché o aproximaciones.

## Render final
El render final existe para producción.

### Propiedades esperadas
- calidad alta;
- persistencia de artefactos;
- métricas completas;
- mayor costo computacional;
- posibilidad de dividir por frame o tile con más rigor.

## Regla de diseño
Preview y render final comparten el mismo modelo conceptual, pero pueden usar estrategias de ejecución distintas.

---

# 6. Fases del pipeline

## Fase 1 — Preparación
Se valida el request y se construye el plan.

### Actividades
- validar `RenderRequest`;
- construir `RenderPlan`;
- resolver secuencia de `FrameDescriptor`;
- preparar directorios o artefactos temporales si corresponde;
- inicializar métricas y estado del job.

---

## Fase 2 — Descomposición
El trabajo se divide en unidades ejecutables.

### Opciones de descomposición
- por frame;
- por frame + tiles;
- preview simple sin partición fina.

### Resultado
Se generan `FrameTask` y, si corresponde, `TileTask`.

---

## Fase 3 — Planificación
Las tareas se insertan en la cola de ejecución.

### Actividades
- asignar prioridad;
- registrar orden;
- marcar estados iniciales;
- preparar cancelación cooperativa.

---

## Fase 4 — Ejecución
Los workers procesan tareas.

### Actividades
- extraer tarea de cola;
- construir contexto de render;
- evaluar la fórmula fractal para píxeles o tiles;
- colorear resultado;
- generar buffer o imagen parcial;
- reportar progreso.

---

## Fase 5 — Ensamblado
Si hubo tiles, se ensamblan en el frame final.

### Actividades
- unir regiones parciales;
- verificar integridad del frame;
- guardar el frame si corresponde;
- actualizar estado del frame.

---

## Fase 6 — Persistencia de artefactos
Se guardan salidas intermedias o finales.

### Posibles artefactos
- preview temporal;
- frame PNG;
- metadatos del frame;
- logs;
- métricas agregadas.

---

## Fase 7 — Exportación mayor
Si el usuario lo pidió, la secuencia se transforma en un artefacto superior.

### Ejemplos
- secuencia de imágenes;
- video;
- paquete de render.

---

# 7. Cola de trabajos

## Responsabilidad
Mantener trabajos pendientes y activos de forma observable y controlada.

## Capacidades mínimas
- encolar jobs;
- consultar estado;
- cancelar;
- actualizar progreso;
- conservar histórico reciente;
- manejar prioridad básica.

## Conceptos técnicos sugeridos
- `JobQueue`
- `PriorityJobQueue`
- `RenderQueueCoordinator`

## Regla importante
La cola debe ser un componente de infraestructura, no una entidad del dominio.

---

# 8. Pool de workers

## Responsabilidad
Ejecutar tareas concretas sin bloquear el hilo de UI.

## Capacidades mínimas
- número configurable de workers;
- ejecución concurrente;
- cancelación cooperativa;
- reporte de progreso;
- tolerancia básica a fallos por tarea.

## Componentes sugeridos
- `WorkerPoolManager`
- `RenderWorker`
- `TaskDispatcher`

## Regla importante
La existencia del pool es una decisión de implementación. El dominio no debe depender de ella.

---

# 9. Contexto de render

## Propósito
Evitar pasar docenas de parámetros sueltos a cada cálculo.

## Concepto sugerido
`RenderContext`

## Posible contenido
- resolución efectiva;
- fórmula fractal;
- parámetros numéricos efectivos;
- color profile;
- frame index;
- cámara interpolada;
- iteraciones máximas;
- radio de escape;
- información del tile si aplica.

## Beneficio
Agrupa los datos necesarios para renderizar una unidad de trabajo de forma consistente.

---

# 10. Estrategias de partición

## Propósito
Definir cómo se divide un trabajo de render.

## Opciones esperadas
- sin partición;
- partición por frame;
- partición en tiles uniformes;
- partición adaptativa futura.

## Utilidad
Esto permite usar el patrón Strategy en la infraestructura de batching sin mezclar la política de subdivisión con la lógica del dominio.

---

# 11. Gestión del progreso

## Requisito
El sistema debe poder reportar progreso útil sin engañar al usuario.

## Niveles de progreso
- progreso del tile;
- progreso del frame;
- progreso del job;
- progreso global de la cola si se quiere en el futuro.

## Componente sugerido
`ProgressAggregator`

## Responsabilidades
- recibir avances parciales;
- consolidarlos;
- calcular porcentaje agregado;
- exponer información observable a la UI.

---

# 12. Cancelación

## Requisito
La cancelación debe existir al menos de forma cooperativa.

## Principio
No se debe intentar “matar” cálculos de forma brutal cuando puede evitarse. Las tareas deben consultar periódicamente si el job fue marcado como cancelado.

## Componentes sugeridos
- `JobCancellationToken`
- `CancellationRegistry`

## Reglas
- la cancelación debe afectar tareas aún no iniciadas;
- las tareas activas deben terminar tan pronto como sea razonable;
- el estado final del job debe quedar explícito.

---

# 13. Manejo de errores

## Tipos de error relevantes
- request inválida;
- fallo de preparación;
- error de render de un frame;
- error al guardar archivo;
- cancelación del usuario;
- fallo de ensamblado de video.

## Regla general
Un fallo puntual no debe implicar necesariamente caída total del sistema. Debe registrarse el error, marcar el estado correspondiente y exponer información clara a la UI.

## Posibles estrategias
- fallo fatal del job;
- fallo parcial con artefactos rescatables;
- reintento futuro en extensiones posteriores.

---

# 14. Artefactos producidos

## Artefactos mínimos esperados
- imágenes preview temporales;
- imágenes PNG por frame;
- logs del job;
- metadatos de ejecución.

## Artefactos opcionales posteriores
- video ensamblado;
- miniaturas;
- snapshot del proyecto asociado al render;
- reporte técnico del batch.

---

# 15. Métricas técnicas

## Objetivo
Permitir observabilidad básica del proceso de render.

## Métricas útiles
- tiempo total del job;
- tiempo por frame;
- tiempo por tile;
- frames completados;
- tiles completados;
- porcentaje global;
- errores por job;
- throughput aproximado.

## Componentes sugeridos
- `BatchMetricsCollector`
- `FrameMetrics`
- `RenderJobMetricsSnapshot`

## Regla
Las métricas no deben ensuciar el dominio. Son parte de infraestructura y de la observabilidad del sistema.

---

# 16. Integración con exportación de video

## Principio
El pipeline de render no debe depender de una implementación específica de codificación de video para existir.

## Modelo recomendado
1. Primero se generan frames como imágenes.
2. Luego, si el usuario lo solicita, un subsistema de exportación ensambla el video.

## Beneficio
- simplifica el render batch;
- separa responsabilidades;
- permite depurar secuencias antes de codificarlas.

## Componente sugerido
- `VideoAssembler`
- `FfmpegVideoAssembler` como implementación posible en infraestructura.

---

# 17. Límites y decisiones de versión 1

## En versión 1 sí debe existir
- render por frames;
- preview separado del render final;
- cola de jobs;
- workers en segundo plano;
- progreso observable;
- cancelación básica;
- exportación de frames a PNG.

## En versión 1 puede quedar simple
- prioridad básica;
- historial limitado;
- tiles solo si aportan valor inmediato;
- pocas estrategias de partición.

## En versión 1 puede quedar fuera
- reintentos avanzados;
- distribución entre máquinas;
- tolerancia sofisticada a fallos;
- balanceo adaptativo complejo.

---

# 18. Ubicación conceptual por capas

## Application
- `RenderRequest`
- `RenderPlan` (si se modela como contrato de aplicación)
- casos de uso de submit/cancel/export

## Domain
- `FrameDescriptor`
- `RenderProfile`
- `ColorProfile`
- conceptos matemáticos y temporales

## Infrastructure
- `RenderJob`
- `FrameTask`
- `TileTask`
- `JobQueue`
- `RenderWorker`
- `ProgressAggregator`
- `VideoAssembler`
- caché, archivos, logs y métricas

---

# 19. Flujo resumido extremo a extremo

## Escenario: render final de una secuencia
1. El usuario configura parámetros de salida.
2. Presentation invoca el caso de uso de render.
3. Application valida y crea `RenderRequest`.
4. Domain contribuye con la secuencia de `FrameDescriptor`.
5. Application construye un `RenderPlan`.
6. Infrastructure crea un `RenderJob`.
7. El job se divide en `FrameTask` o `TileTask`.
8. Los workers procesan tareas.
9. Se ensamblan y guardan frames.
10. Se actualiza progreso y métricas.
11. Si se solicita, se ensamblan artefactos mayores.
12. Presentation muestra estado final y resultados.

---

# 20. Resumen
El pipeline de render y batching de Fractal Render Studio convierte una composición fractal en una producción ejecutable. La secuencia conceptual de frames no se renderiza de forma monolítica, sino mediante jobs, tareas y workers coordinados por una infraestructura observable y cancelable.

Esto permite conservar el sabor de software desktop serio: cola de trabajos, progreso, métricas, artefactos y separación clara entre dominio visual e infraestructura de ejecución.