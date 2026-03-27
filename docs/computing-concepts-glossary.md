# Glosario de Conceptos Computacionales del Proyecto

## Proposito

Este documento funciona como glosario tecnico del repositorio. La idea es que cualquier lector pueda entender los conceptos computacionales que aparecen en el software y en la documentacion sin quedarse solo con palabras sueltas como `job`, `tile`, `facade`, `DTO` o `aggregate`.

Cada concepto se explica con:

- significado general en computacion o ingenieria de software
- sentido concreto dentro de `Fractal Render Studio`
- relacion con el codigo

## 1. Dominio

En ingenieria de software, el dominio es el problema real que el sistema modela.

En este proyecto, el dominio incluye:

- fractales
- estado de camara
- puntos del recorrido
- timeline
- configuracion de render
- colorizacion

No incluye detalles de JavaFX, JSON ni threads. Por eso existe la capa `domain`.

## 2. Agregado

Un agregado es una unidad conceptual consistente del dominio. Agrupa objetos relacionados y define una frontera de integridad.

Aqui el agregado principal es `Project`, porque concentra:

- formula fractal
- render profile
- color profile
- metadata
- settings
- bookmarks
- timeline

La idea es que el proyecto no se trate como piezas aisladas sin relacion.

## 3. Value Object

Un value object es un objeto pequeño cuyo significado depende de su valor, no de identidad mutable.

Ejemplos en este proyecto:

- `FractalCoordinate`
- `ZoomLevel`
- `ProjectName`
- `Resolution`
- `TimePosition`

Esto es mejor que usar solo `double`, `String` o `int` sueltos porque cada tipo expresa una semantica concreta.

## 4. Inmutabilidad

Un objeto inmutable no cambia internamente despues de crearse. Si se necesita una variacion, se crea una nueva instancia.

En el proyecto esto aparece en:

- `Project`
- `Timeline`
- varios tipos de dominio

La inmutabilidad ayuda a:

- razonar mejor sobre el estado
- reducir efectos secundarios
- simplificar validacion y pruebas

## 5. Caso de uso

Un caso de uso representa una operacion del sistema desde el punto de vista del usuario o del negocio.

Ejemplos:

- crear proyecto
- guardar proyecto
- generar preview
- construir secuencia temporal
- enviar job de render

Por eso existen clases como `CreateProjectUseCase`, `GeneratePreviewUseCase` o `BuildTimelineSequenceUseCase`.

## 6. Fachada

Una fachada es una interfaz de alto nivel que agrupa varias operaciones relacionadas y simplifica el acceso a un subsistema.

En este proyecto:

- `ProjectFacade`
- `RenderFacade`
- `ExportFacade`

La UI no necesita conocer todos los casos de uso individuales cuando puede hablar con una fachada.

## 7. DTO

DTO significa Data Transfer Object. Es un objeto pensado para transportar datos entre capas sin exponer directamente el dominio.

Ejemplos:

- `KeyframeDto`
- `RenderJobStatusDto`

Esto ayuda a mantener separadas:

- la representacion interna del dominio
- la informacion que consume la UI

## 8. Repositorio

Un repositorio es una abstraccion para guardar y recuperar agregados o estados persistentes.

En el sistema:

- `ProjectRepository`
- `ProjectFileRepository`

El dominio no sabe si el proyecto se guarda en JSON, base de datos o cualquier otro mecanismo.

## 9. Snapshot

Un snapshot es una captura serializable del estado de un objeto o agregado en un instante dado.

En la persistencia del proyecto se usan documentos snapshot para:

- guardar el proyecto en JSON
- volver a abrirlo despues
- conservar precision y estructura

## 10. Mapper

Un mapper traduce entre dos modelos distintos.

En este proyecto, `ProjectSnapshotMapper` traduce entre:

- dominio
- documentos JSON

Esto es importante porque el modelo de persistencia no tiene por que ser identico al modelo de dominio.

## 11. ViewModel

Un view model es un objeto que mantiene estado preparado para la vista y expone comandos y propiedades observables.

`StudioShellViewModel` coordina:

- labels
- listas observables
- preview actual
- estado del proyecto
- render queue
- drawer inferior

No dibuja controles, pero si decide que informacion y comandos ve la UI.

## 12. Observable State

Estado observable significa que los cambios del modelo de presentacion pueden notificarse a la interfaz automaticamente.

En JavaFX esto se implementa con:

- `StringProperty`
- `ObjectProperty`
- `ObservableList`

## 13. Viewport

El viewport es la ventana visible del espacio fractal en pantalla.

No representa “todo el fractal”, sino:

- una region finita del plano complejo
- a una resolucion concreta
- con una camara concreta

## 14. Tile

Un tile es una region rectangular del viewport o del frame final.

Se usa para:

- dividir el trabajo
- paralelizar
- medir progreso
- cancelar mejor

La idea original de “agente por sector del fractal” se puede entender precisamente como trabajo por tiles.

## 15. Worker

Un worker es una unidad ejecutora que procesa tareas.

En este proyecto, un worker:

- toma trabajo de preview o render
- calcula una parte del frame o una secuencia
- devuelve resultado o progreso

No es un “agente inteligente”, sino un actor de ejecucion tecnica.

## 16. Thread

Un thread o hilo es una secuencia de ejecucion dentro de un proceso.

En este producto hay una distincion fundamental:

- el hilo JavaFX para la interfaz
- hilos de trabajo para previews, miniaturas y render

Entender esto es crucial porque una app visual se rompe si intenta hacer calculo pesado en el mismo hilo que dibuja la interfaz.

## 17. UI Thread

El UI thread es el hilo responsable de la interfaz grafica.

En JavaFX, ese hilo:

- procesa eventos
- actualiza controles
- repinta la escena

Por eso el software debe evitar ejecutar en el UI thread tareas como:

- render fractal profundo
- codificacion de video
- lectura pesada de disco

## 18. Thread Pool

Un thread pool es un conjunto reutilizable de hilos de trabajo.

En lugar de crear un hilo nuevo para cada tarea, el sistema reutiliza workers ya existentes. Eso reduce sobrecosto y permite controlar cuanta concurrencia real existe.

En este producto, los pools se usan para:

- previews
- miniaturas
- renders largos

## 19. Executor

Un executor es una abstraccion que recibe tareas y decide donde y cuando ejecutarlas.

Es una solucion tipica en Java concurrente porque separa:

- la definicion del trabajo
- la politica de ejecucion

Eso permite cambiar estrategias sin reescribir toda la logica de negocio.

## 20. Future / CompletableFuture

Un future representa un resultado que todavia no esta listo.

Su valor computacional es enorme: permite trabajar con operaciones asincronas sin bloquear el hilo actual.

En este producto, un `CompletableFuture` encaja naturalmente en:

- previews
- miniaturas
- renders

Porque el resultado llega despues, no inmediatamente.

## 21. Callback

Un callback es una funcion que el sistema invoca cuando ocurre algo.

Ejemplos tipicos:

- progreso de render
- tile terminado
- tarea completada
- tarea fallida

En software interactivo, los callbacks ayudan a mantener desacopladas las partes que producen eventos de las partes que reaccionan a ellos.

## 22. Job

Un job es una unidad de trabajo de nivel mas alto que un tile.

Ejemplo:

- “renderizar este video completo”

Ese job puede contener:

- muchos frames
- muchos tiles
- varios estados de progreso

## 23. Queue

Una queue o cola es una estructura donde los elementos se procesan en orden de llegada o en algun orden administrado.

En este software el concepto de cola aparece en dos niveles:

- cola de render visible para el usuario
- cola interna de trabajo o tareas pendientes

Una cola evita que todo intente ejecutarse a la vez sin control.

## 24. Scheduler

Un scheduler es el componente que decide que trabajo corre antes, cual espera y cual se cancela.

Aunque no siempre aparezca con ese nombre en el codigo, la idea esta presente cuando el software:

- serializa previews
- conserva solo el preview pendiente mas reciente
- cancela renders obsoletos

## 25. Backpressure

Backpressure es una estrategia para evitar que el productor de trabajo sature al consumidor.

En una app como esta, el usuario puede producir eventos muy rapido:

- scroll
- drag
- cambio de inspector

Si el render intentara procesarlo todo, colapsaria. Por eso aparecen medidas como:

- cancelar previews viejos
- mantener un solo preview pendiente
- reducir calidad en auto-preview profundo

## 26. Debounce

Debounce significa esperar una pausa corta antes de ejecutar una accion.

Es util cuando un flujo de eventos muy rapidos no deberia disparar trabajo pesado en cada evento.

Ejemplos:

- esperar un momento despues del scroll
- esperar a que el layout se estabilice antes del primer preview

## 27. Throttling

Throttling significa limitar la frecuencia con que una accion puede ocurrir.

No es exactamente lo mismo que debounce:

- debounce espera silencio
- throttling limita la tasa maxima

Ambos son tecnicas clasicas de software interactivo.

## 28. Cancelacion cooperativa

La cancelacion cooperativa significa que las tareas no se destruyen por fuerza bruta, sino que consultan periodicamente si deben detenerse.

Esto se usa para:

- previews obsoletos
- renders cancelados
- evitar saturacion del CPU

## 29. Pipeline

Un pipeline es una cadena de etapas de procesamiento.

En el render de video:

1. construir secuencia temporal
2. resolver camara por frame
3. renderizar frames
4. exportar PNG temporales
5. codificar MP4

## 30. Stage

Una stage es una etapa bien definida dentro de un pipeline.

Separar stages permite:

- razonar mejor
- medir cuellos de botella
- cambiar una parte sin romper toda la cadena

## 31. Interpolacion

Interpolar significa construir valores intermedios entre dos estados conocidos.

En el proyecto se interpola:

- centro de camara
- zoom
- recorrido temporal

Gracias a eso no hace falta guardar cada frame como imagen maestra.

## 32. Sampling

Sampling o muestreo significa evaluar una funcion o un espacio continuo en puntos discretos.

El fractal es continuo como idea matematica, pero el software solo puede renderizar muestras discretas, por ejemplo:

- un pixel
- un tile
- una miniatura

## 33. Rasterizacion

Rasterizar significa convertir una representacion matematica o vectorial en pixeles concretos.

En este proyecto, la formula fractal no se "guarda como imagen" al principio; se evalua y luego se rasteriza a un frame.

## 34. Deep Zoom

Deep zoom es el uso de niveles de ampliacion muy altos dentro del fractal.

A mayor deep zoom:

- mayor sensibilidad numerica
- mas iteraciones necesarias
- mayor costo computacional

## 35. Precision numerica

La precision numerica describe cuanta fidelidad tiene la representacion de numeros en el sistema.

En zonas de zoom profundo, usar solo `double` puede degradar la estructura visible. Por eso el proyecto combina precision normal y precision ampliada.

## 36. Serializer

Un serializer convierte datos u objetos a una representacion persistible o transferible.

En este caso, el concepto aparece cuando el proyecto se transforma en JSON.

## 37. Deserializer

Un deserializer hace la operacion inversa: reconstruye una representacion util desde datos serializados.

Sin esto no seria posible reabrir un proyecto guardado.

## 38. Schema logico

Un schema logico es la forma conceptual de un formato persistente, aunque no se exprese como un archivo formal de schema.

En este proyecto existe un schema logico del JSON porque hay una estructura esperada:

- metadata
- settings
- formula
- paleta
- puntos
- timeline
- configuracion de render

## 39. Preset

Un preset es una configuracion predefinida lista para usar.

Ejemplos:

- `DRAFT`
- `STANDARD`
- `DEEP_ZOOM`

## 40. Wizard

Un wizard es una interfaz paso a paso orientada a completar una tarea con varias decisiones guiadas.

En este proyecto, el ejemplo mas cercano es el instalador y, en menor medida, los dialogos de configuracion de render.

## 41. Sesion efimera

Sesion efimera significa que la app no arrastra de manera invisible el trabajo interno de la sesion anterior.

El usuario guarda explicitamente lo importante:

- proyecto
- carpeta de render

## 42. Cache

Una cache es almacenamiento temporal para evitar recalcular o recargar algo costoso.

Aunque una cache puede mejorar rendimiento, tambien puede introducir complejidad y estados viejos. Por eso en este proyecto se controla con cuidado la persistencia de miniaturas y previews.

## 43. Artefacto de runtime

Un artefacto de runtime es un archivo o carpeta producida durante la ejecucion, no parte del codigo fuente.

Ejemplos:

- `frames/`
- `render.mp4`
- logs temporales
- carpetas de trabajo del render

## 44. Empaquetado

Empaquetar significa convertir el proyecto en una aplicacion distribuible.

En este caso:

- `MSI`
- `app-image`
- runtime embebido
- icono

## 45. Compatibilidad

Compatibilidad es la capacidad de que la salida o el ejecutable funcione en el entorno real del usuario.

En este proyecto importan dos tipos:

- compatibilidad del launcher Windows
- compatibilidad del MP4 con reproductores comunes

## 46. Observabilidad

Observabilidad es la capacidad de entender que esta haciendo el sistema mientras corre.

En este proyecto eso incluye:

- estados de preview
- progreso de render
- cola de jobs
- metricas y logs
- estado de memoria JVM

Un sistema observable no solo hace trabajo; tambien deja ver su comportamiento.

## 47. Instrumentacion

Instrumentar significa agregar puntos de medicion o reporte para entender el sistema.

Sin instrumentacion, los problemas de rendimiento y bloqueo se convierten en impresiones subjetivas dificiles de diagnosticar.

## 48. Invariante

Una invariante es una condicion que siempre debe mantenerse verdadera para que el modelo sea valido.

Ejemplos de invariantes conceptuales del producto:

- un proyecto renderizable debe tener secuencia valida
- un render debe tener FPS y duracion coherentes
- un punto de camara debe tener datos numericos validos

## 49. Acoplamiento

Acoplamiento es el grado de dependencia entre partes del sistema.

Demasiado acoplamiento hace que pequenos cambios rompan muchas cosas.

## 50. Cohesion

Cohesion es la claridad y unidad de responsabilidad dentro de una parte del sistema.

Un modulo cohesivo hace una cosa relacionada y la hace bien.

Observabilidad significa poder inspeccionar el comportamiento del sistema:

- progreso
- metricas
- estados
- errores

La pestaña de metricas y la render queue responden a ese objetivo.
