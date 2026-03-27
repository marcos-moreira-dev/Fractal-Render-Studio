# Concurrencia en Java, Modelo de Ejecucion y CompletableFuture

## Proposito

Este documento explica conceptos de concurrencia de Java usando como referencia un producto concreto: Fractal Render Studio.

La meta es entender no solo "que el programa usa varios hilos", sino que problemas computacionales aparecen, que soluciones ofrece Java y por que ciertas decisiones fueron adecuadas para este software.

## 1. Que es concurrencia

Concurrencia es la capacidad de un sistema de avanzar en varias tareas solapadas en el tiempo.

No siempre significa que todo corre literalmente al mismo tiempo en CPU distinta. Significa, mas ampliamente, que el programa organiza varias lineas de trabajo sin obligar a que una termine por completo antes de que la otra pueda avanzar.

En este producto, la concurrencia aparece porque existen al menos estas actividades:

- interfaz grafica
- preview del fractal
- miniaturas de puntos
- render final de video
- escritura de archivos

Si todo esto se hiciera en secuencia estricta, la experiencia seria torpe o inutilizable.

## 2. Paralelismo frente a concurrencia

Estos conceptos suelen confundirse.

### Concurrencia

Varias tareas avanzan de forma intercalada o coordinada.

### Paralelismo

Varias tareas realmente se ejecutan al mismo tiempo en diferentes nucleos o hilos de trabajo.

En Fractal Render Studio se usan ambas ideas:

- concurrencia para coordinar UI, preview y render
- paralelismo para repartir tiles del fractal entre workers

## 3. El problema clasico del hilo de UI

En aplicaciones de escritorio hay un concepto clave: el hilo de interfaz.

En JavaFX, el hilo principal procesa:

- eventos del mouse
- eventos del teclado
- cambios visuales
- repintado de la escena

Si se pone calculo pesado en ese mismo hilo, la app se congela. Este es uno de los problemas mas clasicos de software interactivo.

Por eso una decision fundamental del producto es:

- UI en el hilo JavaFX
- calculo fuera del hilo JavaFX

## 4. Thread, Runnable y Task como base conceptual

En Java, un `Thread` representa una linea de ejecucion.

Un `Runnable` representa una unidad de trabajo.

Una `Task` o tarea de mas alto nivel suele ser una abstraccion adicional sobre el trabajo que se va a ejecutar.

El software moderno rara vez crea hilos manualmente para cada cosa. En su lugar, usa abstracciones superiores.

## 5. Executor y ExecutorService

Una de las soluciones tipicas de concurrencia en Java es usar un `Executor`.

La idea es separar:

- que trabajo hay que hacer
- como y donde se ejecuta ese trabajo

Ventajas:

- control centralizado
- facilidad para limitar concurrencia
- reutilizacion de hilos
- mejor limpieza de codigo

## 6. Thread pool

Un thread pool es un conjunto reutilizable de hilos de trabajo.

En lugar de crear un hilo por cada preview o tile, el sistema reaprovecha un grupo de workers.

Esto resuelve varios problemas:

- reduce sobrecosto de crear hilos
- evita crecimiento descontrolado
- permite fijar politicas de capacidad

En este producto, la existencia de pools es natural porque el render por tiles genera muchas unidades pequenas de trabajo.

## 7. Granularidad del trabajo

Un tema central en concurrencia es la granularidad:

- si una tarea es demasiado grande, se pierde capacidad de respuesta
- si una tarea es demasiado pequeña, el sistema gasta demasiado en coordinarla

El render por tiles es una forma de elegir una granularidad intermedia:

- mas pequeno que un frame completo
- mas grande que un solo pixel

Esto mejora paralelismo y cancelacion sin caer en exceso de overhead.

## 8. CompletableFuture como modelo asincrono

`CompletableFuture` es una abstraccion de Java para representar computo futuro y encadenable.

Su ventaja no es solo que "ejecuta en background", sino que permite:

- esperar resultados sin bloquear inmediatamente
- encadenar etapas
- manejar errores
- disparar callbacks de completitud

En un software como este encaja muy bien porque un preview, una miniatura o una exportacion no producen resultado instantaneo.

## 9. thenAccept, exceptionally y encadenamiento

Una de las ideas importantes de `CompletableFuture` es el encadenamiento de etapas.

Ejemplos conceptuales:

- cuando termine el preview, aplica el frame a la UI
- cuando falle el render, informa el error
- cuando termine una miniatura, actualiza la lista visual

Esto evita escribir flujos asincronos demasiado acoplados o basados en espera activa.

## 10. Cancelacion cooperativa

En interfaces interactivas, gran parte del trabajo se vuelve obsoleto antes de completarse.

Si el usuario hace scroll varias veces:

- el preview anterior ya no importa
- el ultimo viewport es el que vale

La cancelacion cooperativa resuelve este problema:

- la tarea consulta si sigue siendo relevante
- si ya no lo es, termina

Esto es mejor que forzar una detencion violenta del hilo.

## 11. Race condition

Una `race condition` ocurre cuando el resultado depende del orden no controlado en que se ejecutan operaciones concurrentes.

Ejemplo conceptual del producto:

- un preview viejo termina despues que uno nuevo
- si se aplica sin control, la UI mostraria una imagen obsoleta

El coordinador de preview existe precisamente para evitar ese tipo de fallo.

## 12. Estado activo, pendiente y completado

Una solucion elegante a tormentas de eventos es mantener un estado pequeño y controlado:

- preview activo
- preview pendiente
- ultimo completado relevante

Eso es mejor que aceptar una cola infinita de previews, porque la mayoria serian inutiles antes de terminar.

## 13. Backpressure

Backpressure es un concepto de sistemas concurrentes donde el consumidor no puede ser saturado indefinidamente por el productor.

En este software:

- el usuario puede producir eventos muy rapido
- el renderer no puede responder a todos instantaneamente

Soluciones aplicadas:

- un preview pendiente como maximo
- cancelacion del preview viejo
- reduccion adaptativa de calidad en deep zoom

## 14. Debounce

Debounce significa esperar una pausa breve antes de ejecutar algo costoso.

En interfaces esto es util porque:

- resize, scroll y drag producen muchos eventos
- no todos merecen un recalculo completo

La consecuencia practica es una app mas estable y menos compulsiva.

## 15. Latencia frente a throughput

### Latencia

Tiempo que tarda una respuesta individual.

### Throughput

Cantidad total de trabajo completado por unidad de tiempo.

En software interactivo, la latencia manda durante la exploracion. El usuario prefiere una respuesta rapida y aproximada antes que una respuesta perfecta demasiado tardia.

Por eso el producto distingue:

- preview rapido
- refinado manual
- render final

## 16. Modelo mental del flujo concurrente del producto

Se puede pensar asi:

1. la UI recibe gesto
2. el view model actualiza camara
3. el coordinador decide si lanzar, sustituir o cancelar preview
4. los workers procesan tiles
5. el resultado vuelve a la UI solo si sigue siendo vigente

Este modelo es mucho mas importante que memorizar clases concretas. Es el verdadero concepto computacional del sistema.

## 17. Errores asincronos

En sistemas concurrentes y asincronos los errores no siempre aparecen donde se disparo la tarea.

Esto obliga a disenar rutas de error como parte del modelo:

- excepcion en worker
- cancelacion de preview
- fallo de exportador de video
- job fallido en cola

El software robusto no solo hace trabajo concurrente; tambien sabe reportar fallos concurrentes.

## 18. Leccion general

La concurrencia bien aplicada no es "usar varios hilos porque si". Es convertir una aplicacion costosa en una experiencia usable.

En este producto, concurrencia significa:

- no bloquear la UI
- repartir calculo por tiles
- cancelar trabajo obsoleto
- coordinar resultados asincronos
- distinguir claramente entre exploracion rapida y salida final
