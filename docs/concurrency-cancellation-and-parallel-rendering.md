# Concurrencia, Cancelacion y Render Paralelo

## 1. Problema

Un explorador de fractales tiene dos tensiones en conflicto:

- el usuario quiere respuesta inmediata al mover la camara
- el calculo del fractal puede ser costoso por pixel, por tile y por frame

Resolver esto exige concurrencia controlada. No basta con lanzar hilos: hay que coordinar tareas, evitar acumulaciones obsoletas y proteger la UI.

## 2. Donde vive la concurrencia

La concurrencia del proyecto se concentra en sitios concretos:

- `ApplicationBootstrap`
  - define pools de preview, render y miniaturas
- `StudioPreviewCoordinator`
  - coordina previews en curso
- `WorkerPoolManager`
  - administra jobs de render

La UI JavaFX no calcula fractales pesados en su hilo principal. Ese es un principio crucial de software desktop responsivo.

## 3. Preview concurrente

El preview se genera a partir del viewport visible. Para eso se usa:

- un plan de calidad adaptativa
- una division en tiles
- un coordinador que decide que preview sigue vivo

El punto pedagogico importante es este: no toda concurrencia mejora la UX. Si cada gesto del usuario deja previews viejos vivos, el sistema empeora. Por eso la cancelacion es tan importante como el paralelismo.

## 4. Cancelacion cooperativa

El sistema no mata hilos de manera brutal. Usa cancelacion cooperativa:

- `PreviewCancellationToken`
- `JobCancellationToken`
- `FractalIterationMonitor`

La idea es que el trabajo de calculo consulte periodicamente si debe detenerse. Esto es mas seguro y mas compatible con codigo numerico intensivo.

## 5. Jobs de render

El render final opera como job:

- tiene identidad
- tiene estado
- tiene progreso
- puede fallar o cancelarse

`WorkerPoolManager` implementa esta logica. Registra el job, actualiza estado, exporta frames y finalmente codifica el video.

## 6. Riesgo de backlog

En software interactivo, el mayor riesgo no es que una tarea sea lenta, sino que tareas obsoletas se acumulen:

- previews viejos
- miniaturas atrasadas
- renders ya irrelevantes

El proyecto combate esto con:

- invalidacion de previews previos
- control de generaciones para miniaturas
- apertura explicita de la cola de render al comenzar un job

## 7. JavaFX y el hilo de UI

Todo cliente JavaFX debe respetar una regla fundamental:

- los nodos y controles se manipulan en el hilo de UI

Para eso se usa `UiThreadExecutor` como adaptador. Esto reduce el riesgo de errores sutiles por actualizaciones cruzadas desde hilos de trabajo.

## 8. Tiling como forma de paralelizacion

El viewport se divide en regiones o tiles. Eso permite:

- repartir trabajo
- medir progreso parcial
- cancelar por region
- reusar la idea tanto en preview como en render final

Es un buen ejemplo de descomposicion de problema para ejecucion paralela.

## 9. Miniaturas y baja prioridad implícita

Las miniaturas de timeline y puntos son utiles, pero no deben competir contra el viewport principal. Por eso se renderizan aparte y con calidad reducida.

Esto enseña una leccion importante de diseño de sistemas: no todos los calculos visibles tienen la misma prioridad operacional.

## 10. Concurrencia y experiencia de usuario

La concurrencia aqui no es solo una optimizacion. Es una condicion de usabilidad:

- sin cancelacion, el zoom profundo se atasca
- sin pools separados, el render final puede congelar el preview
- sin control del hilo de UI, la interfaz se rompe

## 11. Posibles mejoras futuras

- politicas de prioridad dinamica entre preview, miniaturas y render
- limites temporales para previews muy profundos
- metricas mas finas de ocupacion de pool
- instrumentacion mas rica para diagnosticar cuellos de botella
