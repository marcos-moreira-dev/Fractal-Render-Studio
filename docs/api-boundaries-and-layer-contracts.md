# Fronteras de API y Contratos Entre Capas

## Proposito

Este documento explica como se comunican las capas del sistema y por que esa comunicacion no debe hacerse de cualquier manera.

La palabra "API" aqui no significa necesariamente una API web. Significa, mas ampliamente, un contrato de comunicacion entre partes del software.

En este producto existen varias fronteras internas:

- UI con logica de aplicacion
- logica de aplicacion con dominio
- dominio con infraestructura
- render interactivo con render final
- persistencia con representacion JSON

## 1. Que es un contrato de software

Un contrato define:

- que puede pedir un cliente
- que garantiza el proveedor
- que formato tienen los datos
- que errores o limites existen

Cuando un sistema no define bien sus contratos, aparecen sintomas conocidos:

- acoplamiento excesivo
- vistas que conocen demasiado del dominio
- infraestructura infiltrada en UI
- cambios pequenos que rompen muchas partes

## 2. Por que este proyecto necesita contratos claros

Fractal Render Studio no es un programa de una sola pantalla y un solo flujo lineal. Tiene:

- exploracion interactiva
- inspector editable
- puntos de camara
- timeline
- render de video
- persistencia de proyecto

Eso obliga a que el software se organice en responsabilidades y contratos.

## 3. Capa de presentacion

La capa de presentacion trabaja con conceptos de experiencia de usuario:

- mostrar preview
- abrir drawer de timeline
- aplicar cambios del inspector
- iniciar render
- mostrar cola y progreso

La presentacion no deberia saber:

- como se codifica el MP4
- como se serializa JSON internamente
- como se implementa la cancelacion de workers

Su contrato con el resto del sistema debe expresarse en acciones de producto.

## 4. Capa de aplicacion

La capa de aplicacion orquesta casos de uso.

Su trabajo es traducir acciones del producto a operaciones coordinadas:

- crear proyecto
- guardar punto
- construir secuencia temporal
- lanzar render
- listar jobs
- cargar o guardar proyecto

No es la capa donde deberia vivir ni la UI ni el detalle matematico del fractal.

## 5. Capa de dominio

La capa de dominio representa las reglas estables del problema.

Aqui viven conceptos como:

- proyecto
- camara
- timeline
- punto o bookmark
- perfil de render
- formula fractal

El dominio no deberia depender de JavaFX ni de detalles de disco o ventanas.

## 6. Capa de infraestructura

La infraestructura hace el trabajo concreto con tecnologia:

- archivos JSON
- exportacion MP4
- pool de workers
- render por tiles
- persistencia en filesystem

Es la parte mas cercana al "como", no al "que".

## 7. Fachadas como frontera estable

Una fachada es una superficie de acceso simplificada.

En este producto, las fachadas ayudan a que la UI no conozca demasiados use cases concretos. Eso reduce acoplamiento y mantiene la experiencia mas coherente.

La ventaja conceptual es clara:

- la pantalla piensa en acciones de usuario
- la fachada traduce a operaciones del sistema
- el resto de capas mantiene sus reglas internas

## 8. DTOs y traduccion entre modelos

Un DTO es un objeto de transferencia de datos. No representa necesariamente una entidad rica de dominio; representa datos utiles para cruzar una frontera.

Esto es importante porque:

- la UI necesita datos listos para mostrar
- el dominio necesita proteger sus invariantes
- la persistencia necesita formatos serializables

Por eso no conviene mezclarlo todo en una sola estructura.

## 9. Contratos de persistencia

Persistir un proyecto no es solo "guardar variables". Es definir una representacion portable del estado del producto.

El JSON del proyecto actua como contrato externo:

- debe ser suficiente para reabrir el proyecto
- debe incluir metadatos significativos
- debe preservar puntos, timeline, formula, paleta y configuracion relevante
- debe evitar depender de rutas privadas o estado efimero interno

## 10. Contratos de render

El render tambien necesita un contrato claro. Un job de render deberia responder preguntas como:

- que proyecto o secuencia se esta renderizando
- a que resolucion
- con que FPS
- cuanto dura
- donde se escribe la salida
- como se reporta el estado

Sin esto, la cola de render se vuelve ambigua y dificil de depurar.

## 11. Contratos de error y fallos

Un buen contrato tambien describe fallos.

Ejemplos en este producto:

- preview cancelado porque el usuario ya hizo otro zoom
- render fallido porque el directorio no es valido
- video no exportable por error del encoder
- proyecto no cargable por JSON corrupto

No todo error merece una excepcion cruda en la UI. Muchas veces conviene traducirlo a:

- estado observable
- dialogo de error
- mensaje de cola

## 12. Contratos temporales

El tiempo tambien es un contrato.

En una animacion:

- puntos discretos deben transformarse en una secuencia temporal continua
- la duracion y los FPS definen cuantos frames existen
- cada instante debe corresponder a una camara interpolada

Eso significa que el software no solo maneja datos; maneja datos en el tiempo.

## 13. Acoplamiento y cohesion

Dos ideas clave de ingenieria:

- cohesion: que una parte del sistema tenga una responsabilidad clara
- acoplamiento: que las partes dependan lo minimo necesario unas de otras

En este producto, la arquitectura por capas y las fachadas buscan:

- alta cohesion dentro de cada modulo
- bajo acoplamiento entre presentacion, aplicacion, dominio e infraestructura

## 14. Leccion general

Las fronteras de API no son burocracia. Son la forma de evitar que el software se convierta en una sola masa de codigo donde todo conoce todo.

En Fractal Render Studio eso permite:

- cambiar la UI sin romper el dominio
- mejorar el exportador sin tocar vistas
- cambiar persistencia sin rehacer la logica del producto
- documentar el sistema como conocimiento reusable
