# Complejidad Temporal, Espacial y Escalado del Render

## Proposito

Este documento explica la complejidad computacional del producto, especialmente del render fractal y del pipeline de video.

No se limita a notacion Big-O abstracta; la conecta con sintomas reales del software.

## 1. Tiempo y espacio

Dos dimensiones clasicas de complejidad:

- complejidad temporal: cuanto trabajo tarda en completarse
- complejidad espacial: cuanta memoria requiere

En un renderer visual, ambas importan de forma directa.

## 2. Complejidad por pixel

Cada pixel requiere:

- convertir coordenadas
- iterar la formula
- decidir escape
- colorear

Si el maximo de iteraciones aumenta, el costo por pixel crece.

## 3. Complejidad por frame

Un frame completo depende de:

- ancho
- alto
- costo por pixel

De forma informal:

- mas resolucion -> mas pixeles
- mas iteraciones -> mas trabajo por pixel
- mas precision -> calculo mas caro

## 4. Complejidad del deep zoom

Deep zoom incrementa el costo por varias razones:

- se requieren mas iteraciones
- hay mas sensibilidad numerica
- la precision puede necesitar ampliarse

Eso vuelve el problema mas caro incluso si la resolucion de salida no cambia.

## 5. Tiles y escalado

Dividir el frame en tiles no cambia el trabajo matematico total, pero mejora:

- paralelismo
- latencia percibida
- control de cancelacion

Es una optimizacion de organizacion del trabajo, no una eliminacion magica del costo.

## 6. Amdahl y limites del paralelismo

Un principio importante es que no todo se paraleliza perfectamente.

Siempre existen partes:

- seriales
- sincronizaciones
- ensamblaje final
- I/O

Eso significa que agregar mas workers no produce aceleracion infinita.

## 7. Coste de miniaturas

Las miniaturas son baratas comparadas con un render final, pero no son gratis.

Cuando hay muchos puntos:

- aumentan tareas
- aumenta memoria temporal
- aumenta necesidad de cancelacion o priorizacion

## 8. Pipeline de video

El render de video agrega nuevas capas de complejidad:

- calcular la secuencia temporal
- renderizar todos los frames
- escribir frames o buffers
- codificar el contenedor final

Esto significa que el costo total no es solo "un frame multiplicado por N", aunque esa intuicion sea una aproximacion util.

## 9. Complejidad espacial de buffers

En memoria pueden coexistir:

- preview actual
- tiles parciales
- miniaturas
- frame del encoder
- buffers temporales

Por eso la complejidad espacial sube con:

- resolucion
- concurrencia
- cantidad de artefactos simultaneos

## 10. Escalado percibido

En productos interactivos no basta con medir costo teorico. Tambien importa:

- cuando ve el usuario algo util
- cuanto tarda la primera respuesta
- cuanto tarda la respuesta final

Por eso el sistema usa:

- preview rapido
- refinado manual
- render final separado

## 11. Leccion general

La complejidad computacional del render fractal es un buen ejemplo de como teoria y producto se encuentran:

- Big-O es util
- pero tambien importan latencia, memoria, I/O, UX y cancelacion
