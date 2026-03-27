# Manejo de Errores, Fallos y Recuperacion en Software Interactivo

## Proposito

Este documento explica conceptos de manejo de errores en software de escritorio interactivo, usando Fractal Render Studio como contexto.

La idea es estudiar no solo "como capturar excepciones", sino como pensar fallos en un producto real que mezcla UI, render numerico, concurrencia, disco y video.

## 1. Error, fallo y recovery

Conviene distinguir tres niveles:

- error: condicion incorrecta o anomala
- fallo: manifestacion visible de ese error en el sistema
- recovery: estrategia para volver a un estado util

Ejemplo:

- error: el exportador de video no puede escribir el archivo
- fallo: el job termina como `FAILED`
- recovery: informar al usuario, conservar trazabilidad y permitir reintento

## 2. Por que el software interactivo necesita otro enfoque

En un programa por lotes, un error puede terminar toda la ejecucion.

En una herramienta interactiva eso no siempre es aceptable. Muchas veces el sistema debe:

- seguir abierto
- informar el problema
- conservar lo que si estaba bien
- evitar corrupcion del estado

## 3. Fallos esperables en este producto

En Fractal Render Studio existen varias familias de fallos razonables:

- preview cancelado por obsolescencia
- render fallido por problema de I/O
- video fallido por problema del encoder
- JSON invalido o corrupto
- precision insuficiente en deep zoom
- saturacion por trabajo obsoleto

No todos estos fallos deben tratarse igual.

## 4. Excepcion tecnica frente a error de producto

Una excepcion es un mecanismo del lenguaje. Un error de producto es una condicion significativa para el usuario.

Ejemplo:

- `IOException` es una excepcion tecnica
- "No se pudo guardar el proyecto" es un error de producto

La traduccion entre ambos niveles es parte importante del diseño del sistema.

## 5. Cancelacion no es fallo

En software concurrente, una cancelacion puede ser una decision correcta, no un error.

En este producto:

- el usuario cambia el viewport
- el preview viejo deja de ser relevante
- la tarea se cancela

Eso no es un fallo del sistema. Es una estrategia de control de trabajo.

## 6. Recovery local frente a recovery global

### Recovery local

Corrige o contiene el problema dentro de un subsistema.

Ejemplo:

- descartar un preview obsoleto

### Recovery global

Requiere informar a la aplicacion o al usuario.

Ejemplo:

- render de video fallido

## 7. Estados observables de fallo

En software interactivo es mejor exponer estados observables que esconder errores dentro del codigo.

Esto permite:

- mostrar progreso
- distinguir `COMPLETED`, `FAILED`, `CANCELLED`
- dar diagnostico sin cerrar la app

## 8. Fail fast frente a degradacion graciosa

Dos estrategias clasicas:

### Fail fast

Detenerse rapido cuando una condicion invalida hace peligrosa la continuacion.

### Degradacion graciosa

Seguir funcionando con menor calidad o funcionalidad.

En este producto aparecen ambas:

- fail fast para entradas corruptas o imposibles
- degradacion graciosa para auto-preview profundo con calidad reducida

## 9. Trazabilidad

Un fallo sin contexto es dificil de depurar.

Por eso la trazabilidad es importante:

- logs
- estado de cola de render
- mensajes de progreso
- metricas

La observabilidad es parte del recovery.

## 10. Idempotencia y reintento

Un sistema robusto intenta que ciertas operaciones puedan repetirse sin causar daño adicional.

Ejemplo:

- volver a lanzar un render en una carpeta nueva
- reabrir un proyecto JSON ya existente

La idempotencia facilita reintentos y reduce fragilidad operativa.

## 11. Fallos de datos persistentes

Persistencia introduce su propia clase de problemas:

- archivo faltante
- JSON mal formado
- incompatibilidad de version
- datos incompletos

Una buena herramienta debe decidir:

- cuando abortar
- cuando usar defaults
- cuando informar claramente al usuario

## 12. Leccion general

El manejo de errores no es una capa cosmetica. Es parte de la semantica del producto.

Un software bien diseñado no solo hace su trabajo cuando todo sale bien; tambien sabe:

- fallar con claridad
- recuperarse cuando puede
- evitar corrupcion
- informar de forma comprensible
