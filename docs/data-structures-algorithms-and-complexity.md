# Estructuras de Datos, Algoritmos y Complejidad

## Objetivo

Este documento describe las estructuras de datos principales del sistema, por que fueron elegidas y que implicaciones tienen sobre rendimiento, memoria y mantenibilidad.

## 1. Estructuras de datos del dominio

## Timeline y puntos

La timeline y los puntos del recorrido se modelan como colecciones ordenadas.

Se requiere preservar:

- orden temporal
- estabilidad de indices
- recorrido secuencial
- conversion a frames interpolados

Por eso una estructura tipo lista ordenada es natural. En un editor de este tipo, el acceso dominante no es busqueda arbitraria ultra-rapida, sino:

- iterar
- interpolar entre vecinos
- insertar o eliminar relativamente poco

## Metadata y settings

Estos objetos se modelan como agregados de valores pequeños. No requieren estructuras complejas porque su operacion dominante es lectura y reemplazo completo.

## Render queue

La cola de render usa una representacion orientada a jobs con estados inmutables o casi inmutables hacia la UI:

- pending
- rendering
- completed
- failed
- cancelled

La tabla de render no necesita una estructura sofisticada, pero si requiere actualizacion incremental y orden estable.

## 2. Estructuras de datos de infraestructura

## Mapas y snapshots

La persistencia JSON se apoya en documentos DTO que actuan como snapshots serializables.

Esto separa:

- el modelo de dominio
- el modelo de persistencia

La ventaja es que el JSON puede evolucionar con compatibilidad hacia atras sin deformar el dominio.

## Colas concurrentes

Para preview por tiles y render batch se usan estructuras concurrentes o coordinadas por executor.

La necesidad no es solo paralelizar, sino:

- permitir cancelacion
- evitar acumulacion de previews obsoletos
- drenar resultados de forma segura hacia JavaFX

## 3. Algoritmos principales

## Escape-time fractal iteration

El algoritmo base para Mandelbrot y formulas emparentadas es el esquema de tiempo de escape:

1. tomar el punto complejo correspondiente a un pixel
2. iterar la funcion cuadratica o su variante
3. detener cuando supera el radio de escape o cuando alcanza el maximo de iteraciones
4. traducir el resultado a color

Su costo dominante es:

- `O(pixeles * iteraciones)`

Por eso las decisiones mas importantes de rendimiento son:

- cuantos pixeles se procesan
- cuantas iteraciones se asignan
- si se usa precision normal o alta precision

## Interpolacion de camara

La animacion no requiere keyframes rasterizados. El sistema interpola estados matematicos:

- centro complejo
- zoom
- parametros asociados

Esto permite generar cualquier frame intermedio en funcion del tiempo y de los puntos definidos por el usuario.

## Generacion de miniaturas

Las miniaturas usan una version mas barata del pipeline:

- baja resolucion
- pocas iteraciones
- ejecucion desacoplada del viewport principal

Es un caso clasico de aproximacion eficiente: el objetivo no es exactitud final, sino referencia visual de navegacion.

## 4. Complejidad temporal y espacial

## Preview

La complejidad del preview depende de:

- resolucion efectiva usada
- numero de tiles
- iteraciones por pixel

En zoom profundo, la politica adaptativa reduce el costo para preservar capacidad de respuesta.

## Render final

El render final prioriza fidelidad sobre latencia. Por eso aumenta:

- resolucion
- presupuesto de iteraciones
- precision numerica

La memoria no crece solo por el frame actual; tambien intervienen:

- cola de tiles
- buffers de imagen
- PNG temporales
- codificacion MP4

## 5. Estructuras de datos y decisiones de UX

Una decision importante del proyecto fue unificar el concepto operativo de punto.

En vez de mantener entidades visuales desconectadas para bookmark y keyframe, el sistema se orienta a puntos de recorrido reutilizables:

- en sidebar
- en timeline
- en render

Esto reduce redundancia semantica y simplifica la estructura interna.

## 6. Por que no se usaron estructuras mas complejas

No se justifico introducir:

- arboles balanceados
- grafos generales
- caches complejos LRU
- bases embebidas

porque el dominio actual no lo exige. El costo cognitivo habria superado el beneficio.

## 7. Posibles mejoras futuras

- cache de tiles por viewport cercano
- interpolacion temporal con curvas en vez de solo transiciones lineales
- politicas de calidad adaptativa guiadas por tiempo objetivo
- indexado mas rico para proyectos grandes con muchos puntos
