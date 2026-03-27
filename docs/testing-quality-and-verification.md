# Pruebas, Calidad y Verificacion

## 1. Proposito

Este documento describe como se entiende la calidad en el proyecto y que rol cumplen las pruebas dentro del ciclo de desarrollo.

## 2. Tipos de prueba presentes

El repositorio contiene pruebas orientadas a varias capas:

- dominio
- mapeo de persistencia
- render y precision
- facades de aplicacion
- navegacion y vistas con logica desacoplada

No todas las piezas visuales requieren pruebas de snapshot, pero si existen pruebas de comportamiento para decisiones importantes.

## 3. Que se prueba en dominio

En dominio importan sobre todo:

- invariantes
- consistencia de timeline
- precision de tipos de camara
- cancelacion cooperativa de formulas

Esto es importante porque el dominio es el lugar donde mas conviene detectar errores sin depender de JavaFX o de IO.

## 4. Que se prueba en persistencia

La persistencia JSON se prueba para:

- round-trip correcto
- compatibilidad con formatos anteriores
- preservacion de precision
- serializacion de bookmarks, keyframes y settings

Esto protege un activo central del producto: la capacidad de reabrir proyectos sin deformacion silenciosa.

## 5. Que se prueba en render

El render no se puede validar solo con "abre y se ve". Hay piezas que merecen pruebas directas:

- politicas de calidad adaptativa
- sampler de alta precision
- construccion de secuencias temporales
- cancelacion de jobs

Esto reduce el riesgo de regresiones donde la UI parece responder pero la salida matematica se degrada.

## 6. Verificacion operativa

El proyecto usa verificaciones de linea de comandos sencillas:

- `mvnw.cmd -q compile`
- `mvnw.cmd -q test`

Estas no sustituyen QA manual, pero establecen una base de confianza reproducible.

## 7. QA manual indispensable

Hay aspectos que necesitan prueba humana:

- sensacion de fluidez del zoom
- legibilidad visual
- comportamiento del drawer
- apertura de dialogs
- experiencia de render y empaquetado
- reproduccion del MP4 en Windows

Eso recuerda una verdad importante de ingenieria de software: no toda calidad es reducible a pruebas automaticas.

## 8. Empaquetado como parte de la calidad

Un proyecto desktop no termina cuando compila. Tambien debe:

- empaquetarse
- abrir correctamente instalado
- mostrar icono
- usar un runtime coherente

Por eso el script de `jpackage` y la validacion del launcher forman parte de la calidad del producto, no son un detalle posterior.

## 9. Calidad como propiedad sistemica

La calidad aqui no depende de una sola tecnica. Surge de la combinacion de:

- arquitectura clara
- boundaries de aplicacion
- pruebas
- documentacion
- limpieza del arbol
- empaquetado reproducible

## 10. Posibles mejoras futuras

- pruebas de integracion de video exportado
- verificacion automatica del instalador
- smoke tests de apertura de app image
- analisis estatico adicional
