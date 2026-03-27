# Maquinas de Estado y Modelos de Ciclo de Vida

## Proposito

Este documento explica como pensar el software como un conjunto de estados y transiciones.

Esa idea es central en computacion porque muchos sistemas se entienden mejor como maquinas de estado que como simples secuencias de instrucciones.

## 1. Que es una maquina de estado

Una maquina de estado describe:

- estados posibles
- transiciones entre estados
- eventos que disparan cambios

Esto es util cuando el comportamiento depende de la situacion actual del sistema.

## 2. Estado en este producto

Fractal Render Studio tiene varios subsistemas que pueden modelarse como maquinas de estado:

- proyecto
- preview
- render job
- drawer inferior
- explorer

## 3. Proyecto

El proyecto puede entenderse en estados como:

- nuevo
- editado
- guardado
- cargado

No todos estos estados tienen una clase dedicada, pero el concepto es real y afecta UX, persistencia y decisiones del usuario.

## 4. Preview

El preview interactivo tiene un ciclo de vida claro:

- idle
- solicitado
- renderizando
- cancelado
- completado
- fallido

Pensarlo asi ayuda a entender por que aparecen coordinadores, politicas adaptativas y cancelacion cooperativa.

## 5. Render job

Un render job es el ejemplo mas claro de maquina de estado del sistema.

Puede pasar por:

- queued
- preparing
- rendering
- encoding
- completed
- cancelled
- failed

La cola de render es, en buena medida, una visualizacion de esa maquina de estado.

## 6. Drawer y modo de vista

Incluso la UI tiene ciclos de vida:

- drawer oculto
- drawer visible
- pestaña timeline activa
- pestaña render queue activa

No todo estado necesita una clase formal, pero modelarlo mentalmente mejora diseño y mantenimiento.

## 7. Eventos

Los eventos son disparadores de transicion.

Ejemplos:

- scroll
- drag
- click en render
- cierre de ventana
- finalizacion de preview
- error de exportacion

## 8. Estado observable

Una idea clave en UI moderna es que el estado no solo exista, sino que pueda observarse.

Esto permite:

- sincronizar vista y logica
- actualizar controles automaticamente
- reducir codigo imperativo repetitivo

## 9. Invariantes del ciclo de vida

Cada maquina de estado suele tener invariantes.

Ejemplos:

- un job `COMPLETED` no deberia seguir aumentando progreso
- un preview cancelado no deberia aplicarse a la UI
- un proyecto nuevo no deberia restaurar basura de una sesion anterior

## 10. Estados explicitos frente a estados implicitos

Un estado puede estar:

- explicitamente modelado como enum o flag
- implicitamente deducido por combinacion de variables

La ingenieria de software suele buscar un equilibrio:

- no modelar todo en exceso
- no dejar estados importantes demasiado ocultos

## 11. Leccion general

Pensar el software como maquina de estado ayuda a:

- encontrar bugs de transicion
- mejorar claridad arquitectonica
- documentar comportamiento
- diseñar interfaces mas previsibles
