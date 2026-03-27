# Multimedia Digital: FPS, Resolucion, Codec y Contenedor

## Proposito

Este documento explica conceptos multimedia clave que el producto utiliza al generar video.

## 1. Frame

Un frame es una imagen individual dentro de una secuencia temporal.

Un video no es mas que una sucesion de frames organizada temporalmente.

## 2. FPS

FPS significa frames por segundo.

Define cuantas imagenes se muestran cada segundo en la animacion final.

Si el usuario elige:

- mas FPS -> mas fluidez
- pero tambien mas frames totales

## 3. Duracion

La duracion del video, junto con FPS, determina cuantos frames finales necesita el sistema.

Esta es una relacion computacional basica:

- frames totales = duracion x FPS

## 4. Resolucion

La resolucion define el tamaño espacial del frame:

- ancho
- alto

Full HD significa normalmente:

- 1920x1080

## 5. Codec

Un codec es el mecanismo que codifica y decodifica el flujo multimedia.

No es lo mismo que el contenedor.

## 6. Contenedor

El contenedor es el formato de archivo que organiza la salida multimedia.

MP4 es un contenedor muy comun y compatible.

## 7. Por que importa la compatibilidad

No basta con generar "algo llamado MP4". Debe abrirse bien en reproductores comunes.

Por eso la compatibilidad del encoder es una preocupacion real del producto.

## 8. Frames temporales frente a salida principal

El producto es video-first. Los frames en imagen, si existen, son artefactos auxiliares.

Eso significa que:

- el objetivo principal es `render.mp4`
- `frames/` es una carpeta secundaria de trabajo

## 9. Leccion general

Generar video no es solo juntar imagenes. Implica:

- tiempo
- espacio
- compatibilidad
- codificacion
- organizacion de salida
