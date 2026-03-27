# Conceptos Computacionales Explicitos y Soluciones Tipicas de Software

## Proposito

Este documento reune conceptos tipicos de ingenieria en computacion y desarrollo de software, pero aterrizados a un caso real: Fractal Render Studio.

La meta es que el repositorio sirva tambien para aprender lenguaje tecnico y soluciones recurrentes, no solo para ver una aplicacion terminada.

## 1. Worker como solucion de concurrencia

Cuando un sistema tiene trabajo pesado, una solucion tipica es delegarlo a workers.

Un worker no es una inteligencia separada ni un modulo de negocio. Es una unidad de ejecucion tecnica que toma trabajo y lo procesa.

En este producto, la idea original de "agentes" que renderizan partes del fractal se traduce a una solucion clasica:

- dividir el frame en tiles
- asignar tiles a workers
- recolectar resultados parciales
- ensamblar el frame final

Eso es una solucion de paralelismo por particion espacial.

## 2. Thread pool como solucion de reutilizacion

Crear hilos nuevos todo el tiempo es costoso y dificil de controlar. Una solucion tipica es usar un thread pool.

Ventajas:

- limita concurrencia maxima
- reutiliza hilos
- reduce overhead
- facilita gestion centralizada

Esta es una decision clasica en Java y en muchos entornos concurrentes.

## 3. Future como solucion asincrona

Cuando una operacion tarda, bloquear no suele ser la mejor opcion. Un `Future` o `CompletableFuture` permite:

- iniciar trabajo
- seguir con otras tareas
- reaccionar cuando haya resultado

Es una solucion tipica para software de escritorio reactivo.

## 4. Callback como solucion de reaccion

Un callback resuelve un problema comun: una parte del sistema necesita ser avisada cuando otra termina algo.

Es una tecnica recurrente en:

- interfaces graficas
- sistemas concurrentes
- redes
- multimedia

En este producto, progreso de render y actualizacion de tiles son ejemplos naturales.

## 5. Debounce y throttling como soluciones a tormentas de eventos

Las interfaces modernas producen muchos eventos:

- scroll
- resize
- drag
- teclado

Dos soluciones tipicas son:

### Debounce

Esperar una pausa antes de actuar.

### Throttling

Limitar cuantas veces se actua por unidad de tiempo.

Estas tecnicas son fundamentales para que una UI no dispare computo pesado en cada microevento.

## 6. Cancelacion cooperativa como solucion de obsolescencia

En software interactivo, gran parte del trabajo se vuelve obsoleto antes de terminar.

Ejemplo:

- el usuario ya cambio el zoom
- el preview viejo ya no sirve

La solucion elegante es cancelacion cooperativa:

- la tarea consulta si sigue siendo relevante
- si no lo es, termina ordenadamente

Esto evita despilfarro computacional.

## 7. DTO como solucion de cruce entre capas

Un DTO aparece cuando no conviene exponer directamente el modelo interno a otra capa.

Es una solucion tipica para:

- UI
- APIs
- persistencia
- integraciones

Resuelve el problema de mezclar estructuras de negocio con estructuras de transporte.

## 8. Facade como solucion de simplificacion

Cuando un subsistema tiene muchas operaciones, una solucion tipica es una fachada.

La fachada:

- simplifica acceso
- reduce acoplamiento
- mejora legibilidad del cliente

Es un patron clasico de ingenieria de software.

## 9. Repository como solucion de persistencia desacoplada

Una aplicacion casi nunca deberia depender directamente del filesystem o de una base de datos desde todas partes.

La solucion tipica es un repository:

- define operaciones de guardar/cargar
- oculta el mecanismo real
- protege al dominio

## 10. Mapper como solucion de traduccion

Cuando hay dos modelos distintos, la solucion tipica es un mapper.

Ejemplos:

- dominio a JSON
- dominio a DTO
- datos externos a representacion interna

Resolver explicitamente este problema mejora mantenibilidad.

## 11. Aggregate como solucion de consistencia

Cuando varios objetos deben mantenerse coherentes, una solucion de modelado es tratarlos como un agregado.

Eso evita manipular piezas sueltas sin respetar invariantes.

## 12. Snapshot como solucion de reanudacion

Un snapshot permite capturar un estado y reconstruirlo despues.

Es una solucion tipica cuando:

- se quiere persistencia
- se necesita reabrir una sesion
- interesa versionar estados

## 13. Pipeline como solucion por etapas

Cuando un problema grande puede dividirse en fases, la solucion tipica es un pipeline.

Eso aparece en:

- procesamiento multimedia
- compiladores
- ETL
- render

En este producto:

- secuencia temporal
- render de frames
- escritura de video

## 14. Rasterizacion como solucion de visualizacion

Un fractal es una estructura matematica continua, pero la pantalla no.

La solucion computacional es rasterizar:

- tomar muestras
- asignarlas a pixeles
- producir una imagen concreta

## 15. Interpolacion como solucion de continuidad

Guardar todos los frames seria costoso y poco flexible.

La solucion es guardar estados clave y calcular intermedios. Esa idea aparece en:

- animacion
- audio
- simulacion
- visualizacion cientifica

## 16. Preset como solucion de usabilidad

Muchos sistemas podrian exponer todos los parametros crudos. Eso no siempre es buena UX.

La solucion tipica es usar presets:

- configuran varias cosas a la vez
- reducen carga cognitiva
- ofrecen puntos de partida razonables

## 17. Wizard como solucion guiada

Un wizard es una solucion de interfaz cuando una tarea requiere varias decisiones ordenadas y el usuario necesita guia.

No es obligatorio que toda app tenga uno, pero es un concepto importante en software de escritorio e instalacion.

## 18. Session ephemeral como solucion de higiene operativa

Una sesion efimera resuelve el problema de arrastrar basura interna entre ejecuciones.

Es especialmente valiosa cuando el usuario:

- trabaja por proyectos
- guarda explicitamente resultados
- no quiere restauracion automatica sorpresiva

## 19. Packaging como solucion de entrega

Un build de desarrollo no es una entrega para usuario final.

La solucion tipica de producto es empaquetar:

- runtime
- launcher
- icono
- instalador

Eso forma parte de la ingenieria del producto, no solo del marketing.

## 20. Observabilidad como solucion de diagnostico

Si un sistema hace trabajo pesado pero no lo comunica, el usuario lo interpreta como fallo.

La observabilidad es la solucion a ese problema:

- progreso
- estados
- logs
- metricas
- memoria

## 21. Contrato como solucion de limites claros

Cuando varias partes del software colaboran, una solucion tipica es definir contratos.

Eso reduce ambiguedad y evita que una capa invada a otra.

## 22. Leccion general

La computacion aplicada no consiste solo en "hacer que funcione". Consiste en elegir soluciones conocidas para problemas conocidos:

- concurrencia
- persistencia
- visualizacion
- instalacion
- escalabilidad
- claridad arquitectonica

Este repositorio sirve precisamente para estudiar como esas soluciones aparecen dentro de un producto concreto.
