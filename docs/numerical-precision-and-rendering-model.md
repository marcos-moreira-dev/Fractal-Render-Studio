# Precision Numerica y Modelo de Render

## 1. Problema central

En un explorador fractal, el mayor enemigo del deep zoom no es solo la velocidad. Es la precision numerica.

Cuando la camara entra en regiones extremadamente pequenas del plano complejo:

- los cambios entre pixeles son diminutos
- el error de redondeo se vuelve visible
- la geometria aparente puede degradarse o pixelarse

## 2. Dos modos de precision

El sistema distingue entre:

- precision normal para preview rapido
- precision alta para zonas y tareas donde la fidelidad importa mas

Esta separacion permite preservar la experiencia interactiva sin renunciar al detalle profundo cuando el usuario solicita refinamiento o render final.

## 3. Preview rapido vs refinamiento

El modelo general es:

- `preview rapido`
  - responde pronto
  - reduce iteraciones y resolucion efectiva cuando el zoom es extremo
  - prioriza latencia baja
- `refinar`
  - recalcula con mayor costo
  - usa mejor precision y mejor presupuesto de iteraciones
  - prioriza nitidez y fidelidad

Esto evita un error frecuente en este tipo de software: intentar que todo render sea preciso desde el primer scroll, destruyendo la interaccion.

## 4. Politica adaptativa

La calidad del preview no es fija. El sistema adapta:

- resolucion efectiva
- iteraciones maximas
- activacion de precision alta

en funcion de:

- zoom actual
- costo estimado
- presion de memoria

El objetivo no es producir una imagen "perfecta" en cada gesto, sino una imagen util para navegar.

## 5. Cancelacion cooperativa

Una fuente clasica de bloqueo en software de render interactivo es permitir que trabajos viejos sigan vivos demasiado tiempo.

Por eso el proyecto implementa cancelacion cooperativa:

- cuando entra un nuevo preview, el anterior se invalida
- los renderers y samplers consultan un monitor o token
- los tiles obsoletos dejan de competir por CPU

Esto es especialmente importante en deep zoom, donde una sola region puede consumir mucho tiempo si no se corta a tiempo.

## 6. Tiling y viewport

El render no procesa "todo el fractal".

Procesa:

- solo el rectangulo visible del viewport
- dividido en tiles o regiones

Ventajas:

- paralelizacion natural
- actualizacion incremental
- mejor control de cancelacion
- menor desperdicio computacional

## 7. Presupuesto de iteraciones

El numero de iteraciones no debe ser una constante ciega.

Si el zoom aumenta pero las iteraciones no crecen lo suficiente:

- la frontera se ve pobre
- desaparecen estructuras
- el fractal aparenta perder profundidad

Por eso el presupuesto de escape se ajusta con el zoom y con el modo de render.

## 8. Limite de zoom operativo

Aunque matematicamente el fractal sea inagotable, el sistema necesita un limite operativo profesional.

Ese limite no significa "el fractal termino". Significa:

- con la precision actual
- con el presupuesto actual
- con la experiencia esperada

seguir ampliando ya no produce una mejora visual confiable.

Por eso el proyecto incorpora una politica de limite y un indicador visual para el usuario.

## 9. Video y pipeline de exportacion

El render final sigue un pipeline orientado a video:

1. resolver la secuencia temporal de la camara
2. renderizar frames intermedios a resolucion de salida
3. escribir PNG temporales si hace falta
4. codificar MP4
5. guardar el proyecto reabrible dentro de la misma carpeta de render

La ventaja de guardar puntos matematicos y no capturas maestras es que el video puede regenerarse a otra resolucion o preset.

## 10. Riesgos tecnicos asociados

Los principales riesgos de este modelo son:

- previews profundos demasiado caros
- memoria insuficiente para renders largos
- codificacion de video dependiente de binarios o librerias nativas
- inconsistencias entre preview y render final

Por eso la arquitectura separa preview, render final y exportacion de video como subsistemas distintos.
