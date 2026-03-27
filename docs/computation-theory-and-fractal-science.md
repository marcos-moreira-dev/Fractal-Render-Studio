# Teoria de la Computacion y Ciencia del Fractal

## 1. Proposito

Este documento vincula el sistema con fundamentos teoricos y matematicos. No es una simple guia de uso: explica por que un explorador de fractales exige decisiones de computacion, precision, complejidad y modelado matematico.

## 2. El problema computacional

El sistema resuelve, para cada pixel visible, una pregunta computable:

- dado un punto del plano complejo y una funcion iterativa concreta
- determinar si el punto parece permanecer acotado o escapar
- y estimar en cuantas iteraciones ocurre ese escape

Esta formulacion es algorítmica y repetitiva, lo que la vuelve paralelizable pero muy costosa cuando aumentan:

- la resolucion
- el zoom
- el presupuesto de iteraciones

## 3. Fractales de tiempo de escape

La familia principal implementada en el proyecto pertenece a fractales de tiempo de escape.

La forma general es:

- tomar un valor inicial
- aplicar una iteracion no lineal
- medir si la orbita escapa de una region acotada

En Mandelbrot, por ejemplo, se itera una transformacion cuadratica sobre numeros complejos. Burning Ship, Tricorn y Celtic Mandelbrot son variaciones estructurales de la misma familia.

## 4. Auto-semejanza y escalamiento

El usuario percibe al fractal como "infinito" porque:

- hay auto-semejanza
- aparecen patrones que se repiten
- el detalle relevante no se agota rapidamente

Pero computacionalmente no existe un infinito utilizable en una pantalla finita. El sistema siempre aproxima una ventana finita del plano complejo con:

- una malla finita de pixeles
- un maximo finito de iteraciones
- precision numerica finita

El desafio del software consiste en empujar esos limites sin perder estabilidad ni fluidez.

## 5. Complejidad computacional aplicada

No toda la teoria de la computacion se traduce aqui en automatas o gramaticas. En este proyecto importa especialmente:

- complejidad temporal
- complejidad espacial
- computacion aproximada
- decidibilidad practica bajo recursos finitos

La pregunta "este punto pertenece exactamente al conjunto?" no se responde de manera absoluta con recursos finitos. Lo que se obtiene es una clasificacion operativa segun el numero de iteraciones disponible.

## 6. Precision numerica como problema teorico y practico

Cuando el zoom es profundo, los errores de redondeo destruyen la geometria visible.

Por eso el proyecto migra entre:

- precision `double` en zonas normales
- precision decimal de mayor capacidad en deep zoom

Esto no es un capricho de implementacion. Es una necesidad derivada de la sensibilidad del sistema dinamico a perturbaciones numericas.

## 7. Plano complejo y representacion visual

Cada pixel de la pantalla representa una coordenada del plano complejo.

El sistema necesita mantener la correspondencia entre:

- espacio de pantalla
- centro de camara
- nivel de zoom
- coordenada compleja

El viewport no renderiza "todo el fractal". Renderiza una ventana matematica finita cuyo mapeo al plano complejo depende del estado de camara.

## 8. Interpolacion matematica del recorrido

La animacion no consiste en guardar imagenes maestras. Se guardan estados matematicos de la camara.

Eso permite:

- reabrir proyectos sin perdida estructural
- recalcular frames a cualquier resolucion
- producir video con precision superior a la del preview

Desde el punto de vista teorico, esto es mucho mas correcto que almacenar capturas rasterizadas del fractal.

## 9. Fractal como objeto cientifico y como artefacto de software

El fractal en este proyecto cumple dos roles:

- objeto matematico de estudio
- experiencia visual interactiva

Por eso el software mezcla:

- exactitud razonable
- heuristicas de rendimiento
- decisiones de UX
- persistencia reproducible

El producto no es un paper ni un juguete visual puro: es una herramienta de exploracion y render.

## 10. Limites inevitables

Aunque el fractal sea conceptualmente inagotable, el sistema real siempre tiene limites:

- memoria RAM
- tiempo de CPU
- precision numerica
- tasa de refresco aceptable para el usuario

El valor profesional del proyecto no esta en negar esos limites, sino en administrarlos bien:

- preview adaptativo
- render final separado
- limite de zoom operativo
- avisos de deep zoom
- cancelacion de trabajos obsoletos

## 11. Relevancia academica

Este proyecto se puede justificar academicamente desde varias areas:

- algebra y analisis complejo
- sistemas dinamicos discretos
- computacion grafica
- concurrencia
- ingenieria de software
- analisis de complejidad

Por eso es apropiado documentarlo no solo como aplicacion JavaFX, sino como sistema computacional con base matematica real.
