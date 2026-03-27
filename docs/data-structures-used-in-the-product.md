# Estructuras de Datos Usadas en el Producto

## Proposito

Este documento explica estructuras de datos tipicas de computacion y como aparecen en Fractal Render Studio.

La idea no es listar clases Java sin mas, sino entender por que ciertas estructuras existen y que problema resuelven.

## 1. Lista

Una lista es una secuencia ordenada de elementos.

En este producto las listas son naturales para:

- puntos del recorrido
- timeline
- jobs de render
- miniaturas visibles

La lista es apropiada cuando importa el orden y se recorre secuencialmente con frecuencia.

## 2. Lista observable

Una lista observable es una lista que notifica cambios a la interfaz u otros consumidores.

En JavaFX esto es muy importante porque la UI necesita reaccionar a:

- elementos agregados
- elementos eliminados
- reordenamientos

Es una estructura de datos con comportamiento adicional de notificacion.

## 3. Cola

Una cola se usa cuando el orden de procesamiento importa.

En el producto aparece conceptualmente en:

- cola de render
- previews pendientes
- trabajo interno de render

La cola es ideal para modelar "tareas que esperan su turno".

## 4. Conjunto implicito

Aunque no siempre haya una clase `Set`, el sistema a veces trabaja como si mantuviera conjuntos:

- requests ya deduplicados
- estados que no deben repetirse
- artefactos unicos por identificador

Este es un buen recordatorio de que una estructura de datos no siempre se reconoce por el nombre del contenedor, sino por la operacion que resuelve.

## 5. Mapa

Un mapa asocia claves con valores.

En productos de software, aparece cuando hace falta acceso por identificador:

- estado por job id
- nodos por id
- caches indexadas

Si el acceso principal es "dame el elemento con esta clave", un mapa suele ser mejor que una lista.

## 6. Arbol

Un arbol representa jerarquia.

En este producto, el panel lateral usa una estructura jerarquica porque el proyecto contiene:

- metadata
- configuracion
- puntos
- otras secciones subordinadas

Eso hace natural una representacion tipo arbol y no solo una lista plana.

## 7. Grafo conceptual

Aunque el software no exponga un grafo explicito, varios conceptos pueden pensarse como grafo:

- dependencias entre capas
- flujo del pipeline
- transiciones de estado

Esto es util porque muchos problemas de software se entienden mejor como nodos y relaciones que como simples listas.

## 8. Registro o record

Un record es una estructura de datos compacta para agrupar campos relacionados.

Su valor esta en:

- claridad
- inmutabilidad frecuente
- semantica directa

Es una forma muy adecuada para transportar configuraciones, estados y resultados temporales.

## 9. DTO como estructura de transferencia

Un DTO no es solo un patron. Tambien es una estructura de datos con un objetivo claro:

- mover informacion entre capas
- evitar exponer internals del dominio

Su forma suele ser simple, plana y orientada a transporte.

## 10. Snapshot como estructura persistente

Un snapshot es una estructura que captura estado completo o suficiente de un sistema.

En este producto, el JSON del proyecto representa una estructura snapshot, porque guarda:

- configuracion
- puntos
- timeline
- metadata
- caracteristicas de render

## 11. Matriz implícita de pixeles

Un frame rasterizado puede entenderse como una matriz de pixeles:

- filas
- columnas
- color por celda

Aunque el sistema no siempre use una clase "matriz" explicita, el problema renderizado se organiza como una estructura bidimensional.

## 12. Tile como particion espacial

Un tile es una subdivision rectangular de esa matriz o viewport.

Desde la perspectiva de estructuras de datos, el frame se convierte en:

- una superficie 2D
- particionada en bloques

Esto permite:

- paralelismo
- progreso parcial
- ensamblaje incremental

## 13. Buffer

Un buffer es una region de memoria usada para almacenar datos temporalmente mientras se producen, transforman o consumen.

En este software los buffers son esenciales para:

- imagenes
- tiles
- composicion de frames
- exportacion de video

## 14. Estructura temporal

La timeline no es solo una lista; es una estructura temporal.

Eso significa que cada elemento tiene:

- posicion temporal
- orden
- significado dentro de una animacion

No es lo mismo una coleccion de puntos arbitrarios que una secuencia temporal interpretable.

## 15. Estructuras efimeras frente a persistentes

Una distincion importante de computacion es:

- estructura efimera: vive solo durante la sesion
- estructura persistente: queda guardada y reabrible

En este producto:

- previews y miniaturas temporales son efimeros
- proyecto JSON y carpeta del render son persistentes

## 16. Eleccion de estructura y complejidad

Elegir una estructura de datos afecta:

- tiempo de acceso
- tiempo de insercion
- costo de recorrido
- facilidad de mantener invariantes

Por eso no es trivial decidir si algo debe ser:

- lista
- mapa
- arbol
- cola
- snapshot

## 17. Leccion general

Las estructuras de datos no son teoria aislada. En un producto real determinan:

- como se organiza la informacion
- como se actualiza la UI
- como se persiste el estado
- como se paraleliza el render
- como se entiende el sistema al leerlo
