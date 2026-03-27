# Fundamentos de Computacion Aplicados al Producto

## 1. Introduccion

Un repositorio serio no solo sirve para “mostrar que la app funciona”. Tambien puede servir para estudiar como se materializan conceptos de computacion dentro de un producto concreto.

`Fractal Render Studio` es un buen caso porque mezcla:

- matematica computacional
- concurrencia
- sistemas de archivos
- modelado de dominio
- UI desktop
- persistencia
- empaquetado
- codificacion multimedia

## 2. Del problema abstracto al producto

En teoria de software, un sistema parte de una necesidad del usuario y se transforma en:

- modelo conceptual
- casos de uso
- estructuras de datos
- algoritmos
- interfaces
- artefactos de distribucion

Este proyecto permite estudiar todas esas transiciones.

## 3. Modelado del problema

El problema original no es “dibujar pixeles”, sino algo mas rico:

- navegar un fractal
- registrar puntos del recorrido
- interpolar una trayectoria
- producir una salida multimedia reproducible

Eso exige modelar:

- camara
- tiempo
- color
- resolucion
- render
- persistencia

## 4. Abstraccion

Abstraer significa seleccionar lo esencial y ocultar detalles accidentales.

Ejemplos del proyecto:

- `Project` abstrae el estado global relevante
- `RenderProfile` abstrae una configuracion de render
- `RenderFacade` abstrae varios flujos de render
- `ProjectRepository` abstrae persistencia

Sin abstraccion, el sistema se convierte en una mezcla caotica de UI, disco y matematicas.

## 5. Descomposicion

Descomponer significa partir un problema grande en partes controlables.

Aqui se ve en varios niveles:

- arquitectura por capas
- job de render dividido en frames
- frame dividido en tiles
- documentos JSON divididos en DTO especializados

## 6. Representacion

Todo software representa algo usando estructuras finitas.

En este proyecto:

- el plano complejo se representa con coordenadas finitas
- la timeline se representa como secuencia ordenada
- el proyecto se representa en JSON
- el video se representa como secuencia codificada

Esta nocion de representacion es central en computacion: nunca trabajamos con el objeto “real”, sino con una forma computable del mismo.

## 7. Transformacion de datos

Una buena parte del software consiste en transformar representaciones:

- de interaccion de usuario a comandos de dominio
- de dominio a DTO
- de DTO a JSON
- de estado matematico a pixeles
- de PNG a MP4

Este proyecto contiene todos esos pasos.

## 8. Estado y transicion

Muchos problemas computacionales pueden entenderse como maquinas de estado.

En el producto hay varios ejemplos:

- proyecto nuevo, modificado, guardado
- preview esperando, renderizando, completado
- render job pendiente, preparando, renderizando, completado, cancelado o fallido
- drawer visible u oculto

Pensar asi ayuda a diseñar software mas claro.

## 9. Tiempo como dimension computacional

El software no solo maneja datos estaticos. Tambien maneja evolucion temporal:

- tiempo de la timeline
- tiempo real del usuario
- tiempo de CPU
- tiempo de render
- tiempo de una animacion final

La interpolacion de camara es justamente una manera de convertir tiempo abstracto en estados visuales.

## 10. Recursos limitados

La computacion real siempre esta limitada por recursos:

- CPU
- RAM
- almacenamiento
- ancho de banda de I/O
- latencia perceptible para el usuario

La app trata esos limites mediante:

- preview adaptativo
- render en segundo plano
- cancelacion cooperativa
- precision numerica selectiva

## 11. Paralelismo y coordinacion

Dividir el trabajo no basta. Hay que coordinarlo.

Por eso aparecen conceptos como:

- pool de workers
- tiles
- jobs
- cancelacion
- agregacion de progreso

Esto es teoria de sistemas concurrentes aplicada a un producto multimedia.

## 12. Persistencia como memoria externa del sistema

La memoria RAM contiene la sesion viva. El JSON contiene la memoria durable que el usuario decide conservar.

Ese contraste entre:

- estado volatile
- estado persistente

es una distincion clasica de sistemas y se ve claramente en este proyecto.

## 13. Interfaz como traduccion

La interfaz no es solo “bonita”. Traduce:

- acciones humanas
- en comandos computables
- sobre estructuras y algoritmos concretos

Por ejemplo, un scroll del mouse termina afectando:

- camara
- politica de preview
- costo computacional
- posible limite de zoom

## 14. Correctitud vs usabilidad

En teoria, uno podria intentar calcular siempre el fractal con maxima precision. En practica, eso destruiria la UX.

Este proyecto enseña una tension clasica:

- correctitud numerica maxima
- experiencia interactiva util

La solucion no es elegir una sola, sino separar modos:

- preview rapido
- refinamiento
- render final

## 15. Reproducibilidad

Un producto serio debe poder reproducir sus resultados.

Aqui eso se logra guardando:

- puntos matematicos
- settings
- formula
- color
- parametros de render

en vez de guardar solo capturas rasterizadas.

## 16. Distribucion

La computacion aplicada a productos no termina en el codigo fuente. Tambien incluye:

- empaquetado
- instalacion
- iconos
- runtime
- compatibilidad del ejecutable

Por eso el instalador MSI y el `app-image` forman parte del conocimiento del repositorio.

## 17. Valor didactico del repositorio

Este repositorio puede leerse como un caso de estudio de:

- software desktop
- arquitectura limpia
- matematicas aplicadas
- visualizacion computacional
- render multimedia
- persistencia de proyectos

La meta de la documentacion ampliada es justamente convertirlo en una fuente de aprendizaje y no solo en una entrega funcional.
