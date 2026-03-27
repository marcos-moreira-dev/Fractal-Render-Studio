# JVM, Memoria y Gestion de Recursos

## Proposito

Este documento explica como un producto de escritorio en Java administra memoria, CPU, hilos y recursos de sistema cuando trabaja con imagenes, fractales, miniaturas y video.

La idea no es hablar de la JVM de forma abstracta, sino mostrar por que estos temas existen dentro de Fractal Render Studio.

## 1. Por que este tema importa en este proyecto

El producto hace varias cosas que presionan recursos al mismo tiempo:

- genera previews del viewport
- renderiza miniaturas de puntos
- compone frames intermedios
- escribe video MP4
- mantiene una interfaz grafica JavaFX viva y reactiva

Eso significa que el sistema no es solo "una app con botones". Es una aplicacion de escritorio con:

- computacion numerica
- trabajo grafico en memoria
- tareas concurrentes
- codificacion multimedia

Si no se controla bien la memoria o los hilos, aparecen sintomas clasicos:

- la UI se congela
- un preview se queda en 50% o 75%
- se acumulan renders obsoletos
- el sistema consume demasiado heap
- el video final falla o queda incompleto

## 2. Heap, stack y objetos de corta vida

En Java, los objetos viven principalmente en el heap. El stack contiene llamadas y referencias temporales por hilo.

En este producto, muchos objetos son de vida corta:

- estados de preview
- buffers intermedios
- actualizaciones por tile
- descriptores de frame
- DTOs de estado para la cola de render

Eso es normal, pero obliga a tener una arquitectura donde:

- las asignaciones temporales sean controladas
- no se guarden referencias inutiles
- la UI no retenga imagenes viejas sin necesidad

## 3. Imagenes, buffers y costo espacial

Un frame Full HD de 1920x1080 implica millones de pixeles.

Aunque el numero exacto depende del formato y del pipeline, una idea util es esta:

- ancho x alto = cantidad de pixeles
- cada pixel necesita datos de color
- al renderizar, muchas veces existen al mismo tiempo:
  - el frame actual
  - tiles parciales
  - miniaturas
  - buffers de video

Por eso el costo de memoria no depende solo de "una imagen", sino del conjunto de buffers simultaneos.

## 4. Presion de memoria en el producto

En Fractal Render Studio la presion de memoria aparece sobre todo cuando coinciden:

- deep zoom
- resolucion alta
- video final
- varias miniaturas
- cola de render activa

El sistema ya incorpora observabilidad de memoria JVM para advertir estados de riesgo y evitar que la experiencia se degrade sin explicacion.

## 5. Concurrencia y contencion

La app usa varios hilos con responsabilidades distintas:

- hilo principal JavaFX para UI
- workers de preview por tiles
- pipeline de miniaturas
- workers de render final
- codificacion de video

Esto no se hace por "complejidad gratuita", sino porque una sola hebra no podria mantener la UI fluida mientras calcula fractales y produce video.

Pero paralelizar tambien crea riesgos:

- exceso de tareas simultaneas
- competencia por CPU
- colas viejas que siguen trabajando
- saturacion por renders obsoletos

Por eso el proyecto aplica cancelacion cooperativa y coordinacion serial del preview.

## 6. Cancelacion cooperativa

Cancelar en software concurrente no significa "matar el hilo por la fuerza". En sistemas bien disenados, cancelar significa:

- marcar que el trabajo ya no es relevante
- comprobar periodicamente ese estado
- salir ordenadamente del calculo

Eso es especialmente importante en este producto, donde el usuario puede mover la camara mas rapido de lo que un render profundo puede completarse.

Sin cancelacion cooperativa:

- cada zoom viejo seguiria consumiendo CPU
- la UI pareceria colgada
- el porcentaje de preview se quedaria "pendiente"

## 7. Throughput frente a latencia

Dos conceptos importantes de rendimiento son:

- throughput: cuanto trabajo total se completa por unidad de tiempo
- latencia: cuanto tarda una respuesta concreta en llegar

En un renderer interactivo, la latencia importa mas que el throughput bruto durante la exploracion.

Por eso el producto distingue:

- preview rapido para responder pronto
- refinado manual para mayor calidad
- render final para calidad alta y salida de video

La decision es de ingenieria: no siempre conviene usar el mejor algoritmo posible si eso destruye la capacidad de respuesta.

## 8. Politicas adaptativas

Una politica adaptativa es una regla que cambia el comportamiento del sistema segun el contexto.

Aqui se aplica para:

- reducir resolucion efectiva en auto-preview extremo
- ajustar iteraciones del preview
- reservar caminos mas caros para refinado o render final

Esto conecta directamente con una idea central de ingenieria de producto:

- una decision correcta no es la mas potente en abstracto
- es la que produce la mejor experiencia util bajo restricciones reales

## 9. Limpieza de recursos efimeros

El producto maneja recursos que no deben sobrevivir indefinidamente:

- miniaturas temporales
- estados internos de sesion
- directorios temporales de trabajo interno

El usuario pidio explicitamente una sesion efimera, y eso tiene sentido desde ingenieria:

- reduce basura persistente
- evita confundir trabajo anterior con el proyecto actual
- impide que un estado oculto afecte una sesion nueva

Este principio es un ejemplo de higiene operativa del software.

## 10. Carpeta de render como frontera estable

Aunque la sesion interna es efimera, la carpeta de render elegida por el usuario es persistente y significativa.

Esa carpeta actua como frontera de entrega:

- contiene el video final
- contiene la carpeta `frames/` si hizo falta
- contiene el JSON unico del proyecto

Es decir, el sistema distingue entre:

- estado interno desechable
- artefactos del usuario que deben preservarse

Esa separacion es una buena practica de diseno de herramientas.

## 11. Rendimiento percibido frente a rendimiento real

No basta con que el sistema sea rapido "en promedio". Tambien debe comunicar bien lo que esta haciendo.

Por eso importan:

- porcentajes comprensibles
- cola de render visible
- apertura automatica del panel de render al iniciar un job
- mensajes de estado no ambiguos

La observabilidad forma parte del rendimiento percibido.

## 12. Patrones practicos aplicados

En este proyecto aparecen varios patrones de gestion de recursos:

- pools de workers para limitar concurrencia
- coordinador serial para previews
- cancelacion cooperativa
- separacion entre preview, miniatura y render final
- limpieza explicita de recursos efimeros
- sesion interna no persistente

## 13. Leccion general de ingenieria

Un software visual como este no se sostiene solo con matematica del fractal. Necesita tambien:

- control de memoria
- priorizacion de trabajo
- tratamiento de la concurrencia
- rutas diferenciadas para interaccion y exportacion
- limpieza de recursos

Eso es ingenieria de sistemas: convertir computo costoso en una experiencia util, comprensible y estable.
