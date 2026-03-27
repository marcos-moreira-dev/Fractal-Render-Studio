# Pipeline de Video y Codificacion Multimedia

## 1. Objetivo

El producto esta orientado a generar video como salida principal. Este documento explica como se arma ese pipeline y por que se eligio ese enfoque.

## 2. Del punto matematico al frame

El sistema no parte de imagenes maestras guardadas por el usuario. Parte de un modelo matematico:

- puntos del recorrido
- camara interpolada
- formula fractal
- perfil de render
- paleta de color

Cada frame del video se recalcula a partir de ese estado matematico.

## 3. Secuencia temporal

`BuildTimelineSequenceUseCase` transforma:

- proyecto
- duracion
- FPS
- preset

en una secuencia de `FrameDescriptor`.

Cada descriptor contiene lo necesario para renderizar un frame individual.

## 4. Render por frame

`WorkerPoolManager` recorre la secuencia y para cada `FrameDescriptor`:

1. elige el renderer adecuado
2. genera el frame
3. lo exporta como PNG temporal
4. actualiza el estado del job

La exportacion de PNG no es la salida principal para el usuario. Es un artefacto intermedio del pipeline.

## 5. Codificacion MP4

Una vez completados los PNG temporales:

- `Mp4SequenceVideoExporter` recorre la secuencia
- normaliza dimensiones pares
- usa FFmpeg via JavaCV
- produce `render.mp4`

Esta arquitectura hace que el producto sea video-first, pero sin renunciar a una ruta intermedia controlada.

## 6. Compatibilidad en Windows

La eleccion de codificacion no fue teorica. Se tuvo que privilegiar compatibilidad con reproductores reales de Windows.

Eso llevo a:

- adoptar FFmpeg via JavaCV
- ajustar el empaquetado del runtime y dependencias
- validar el instalador y el launcher

## 7. Por que mantener `frames/`

Aunque el usuario final no quiera "render por imagenes" como flujo principal, conservar `frames/` cuando existan puede tener valor:

- depuracion
- exportacion secundaria
- reprocesado
- evidencia de cada frame generado

La clave esta en que queden contenidos dentro de la carpeta del render y no dispersos en el sistema.

## 8. Relacion con el JSON del proyecto

Cada carpeta de render contiene tambien `project.fractalstudio.json`. Esto crea una relacion clara entre:

- el video generado
- los frames temporales
- el estado del proyecto que produjo esa salida

## 9. Conceptos informaticos implicados

Este pipeline toca varias areas:

- codificacion multimedia
- procesamiento de imagenes
- sistemas de archivos
- concurrencia
- reproducibilidad de builds

Por eso resulta valioso como material didactico y no solo como funcionalidad de producto.

## 10. Posibles mejoras futuras

- perfiles de bitrate configurables
- multiples codecs de salida
- metadatos embebidos en el video
- exportacion de audio o banda sonora asociada
