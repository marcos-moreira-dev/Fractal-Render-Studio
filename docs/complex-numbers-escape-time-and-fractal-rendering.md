# Numeros Complejos, Escape-Time y Render del Fractal

## Proposito

Este documento explica la base matematica computable del render fractal, especialmente el enfoque de numeros complejos y algoritmo de escape-time.

No se trata de una clase completa de analisis complejo, sino de mostrar la teoria que si se vuelve software dentro del producto.

## 1. El plano complejo

Un numero complejo suele escribirse como:

- `z = a + bi`

donde:

- `a` es la parte real
- `b` es la parte imaginaria
- `i^2 = -1`

El plano complejo permite interpretar un numero complejo como un punto:

- eje horizontal: parte real
- eje vertical: parte imaginaria

Eso es exactamente lo que hace posible navegar un fractal como si fuera una geografia matematica.

## 2. La camara como ventana sobre el plano complejo

El viewport del software no observa "todo el fractal". Observa una region finita del plano complejo.

La camara define:

- centro
- zoom
- relacion de aspecto
- resolucion

Renderizar un frame significa convertir esa ventana matematica en una cuadricula de pixeles.

## 3. De pixel a punto matematico

Cada pixel de la imagen corresponde a una coordenada del plano complejo.

Ese paso es una transformacion de coordenadas:

- desde un sistema discreto de pantalla
- hacia un sistema continuo matematizable

Este es uno de los pasos fundamentales del render.

## 4. Iteracion

Una iteracion consiste en aplicar repetidamente una regla.

En fractales tipo Mandelbrot y familia cuadratica, se estudia una recurrencia del estilo:

- `z_{n+1} = f(z_n, c)`

La pregunta computacional es:

- que pasa con la secuencia al iterar muchas veces

## 5. Algoritmo de escape-time

Escape-time es un enfoque computacional donde se observa si una orbita:

- permanece acotada
- o escapa mas alla de cierto radio

El algoritmo general es:

1. iniciar en un estado
2. iterar la formula
3. medir la magnitud
4. si supera un umbral, considerar que escapo
5. registrar en que iteracion ocurrio

Ese numero de iteracion es la base para colorear.

## 6. Radio de escape

El radio de escape es el umbral a partir del cual se considera que la orbita ya no volvera a una region acotada relevante.

Desde la perspectiva computacional, este radio permite detener la iteracion antes de llegar siempre al maximo.

Eso reduce trabajo y hace viable el render.

## 7. Maximo de iteraciones

Como un algoritmo no puede iterar infinitamente, se fija un maximo.

Si una orbita no escapa dentro de ese maximo, el sistema la trata como:

- posiblemente perteneciente al conjunto
- o al menos no distinguible con la precision actual

Esto conecta teoria matematica con limite computacional.

## 8. Coloracion

El color del fractal no es una propiedad fisica del conjunto. Es una interpretacion visual construida por el software.

Generalmente depende de:

- cuan rapido escapo la orbita
- mapeo hacia una paleta
- ajustes tonales

Es decir, el color es una capa de visualizacion sobre un proceso matematico.

## 9. Zoom profundo y precision

A mayor zoom, menores diferencias numericas separan dos pixeles vecinos en el plano complejo.

Eso vuelve mas critico:

- el error de redondeo
- la precision numerica
- la estabilidad del algoritmo

Por eso el deep zoom no es solo "mas cerca". Es un problema numerico mas exigente.

## 10. Double frente a precision ampliada

`double` ofrece precision finita. Para exploracion normal es suficiente muchas veces, pero en deep zoom extremo puede degradarse.

Entonces aparecen estrategias de precision ampliada:

- tipos decimales de mayor precision
- calculo mas costoso
- politicas de activacion selectiva

Esto muestra una tension clasica entre:

- costo computacional
- fidelidad numerica

## 11. Sampling y aliasing conceptual

Cuando una estructura continua se representa con una cuadricula discreta, aparecen limites de muestreo.

Si la estructura contiene detalle muy fino, un pixel puede no bastar para capturarlo bien.

Esto ayuda a explicar por que:

- algunas zonas se ven pixeladas
- la nitidez depende del zoom y de la resolucion
- no todo problema visual se resuelve solo aumentando iteraciones

## 12. Interpolacion entre puntos de camara

La animacion del producto no guarda cada frame como estado independiente de alto nivel. Guarda puntos de camara y construye estados intermedios.

Eso significa que:

- la geometria del recorrido se define por puntos
- el tiempo total se reparte entre ellos
- cada frame final corresponde a una camara interpolada

Matematicamente, eso es una trayectoria en el espacio de parametros de la camara.

## 13. Por que el fractal parece infinito

El fractal no es "infinito" en el sentido trivial de tener un archivo infinito ya dibujado. Es una estructura definida por una regla iterativa que puede explorarse con detalle arbitrario.

Computacionalmente, eso significa:

- no existe una imagen total precalculada
- siempre se renderiza una ventana finita
- cada aumento de zoom exige nuevo calculo

## 14. Lo que el software realmente calcula

El software no calcula "todo el fractal". Calcula:

- solo el viewport visible
- a una resolucion concreta
- con una precision concreta
- segun una formula concreta
- con un limite concreto de iteraciones

Esto es importante porque conecta la teoria matematica con la realidad finita del computador.

## 15. Leccion general

El render fractal es un excelente ejemplo de computacion aplicada porque mezcla:

- representacion matematica
- conversion de coordenadas
- iteracion
- criterio de escape
- precision numerica
- rasterizacion
- visualizacion

Es una cadena donde cada idea matematica debe traducirse a una decision computacional concreta.
