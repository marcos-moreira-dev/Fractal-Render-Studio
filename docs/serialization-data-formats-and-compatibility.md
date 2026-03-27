# Serializacion, Formatos de Datos y Compatibilidad

## Proposito

Este documento explica conceptos de serializacion y formatos persistentes usando el proyecto JSON y la salida multimedia como ejemplos.

## 1. Que es serializar

Serializar es transformar una estructura de datos o un estado en una forma que pueda:

- guardarse
- transmitirse
- reconstruirse despues

En este producto, el ejemplo principal es el JSON del proyecto.

## 2. Formato de datos

Un formato de datos es una convencion para estructurar informacion.

Ejemplos en el producto:

- JSON para proyecto
- MP4 para video final
- PNG para frames temporales si existen

## 3. Formato legible frente a formato optimizado

JSON es legible para humanos y muy util para depuracion y portabilidad.

MP4 en cambio es un formato orientado a reproduccion multimedia eficiente.

Un producto serio suele combinar varios formatos segun necesidad.

## 4. Compatibilidad

Compatibilidad significa que los datos producidos por el sistema puedan:

- reabrirse luego
- funcionar en entornos reales
- ser interpretados por herramientas comunes

Esto aplica tanto a:

- proyectos JSON
- videos MP4

## 5. Schema logico

Aunque no exista un archivo formal de schema, el sistema tiene una estructura esperada para sus datos persistentes.

En el JSON del proyecto, por ejemplo, hay un schema logico con:

- metadatos
- configuracion
- puntos
- timeline
- parametros de render

## 6. Versionado conceptual

Todo formato persistente enfrenta una tension:

- hoy debe ser suficiente
- mañana podria cambiar

Por eso conviene pensar en compatibilidad hacia delante y hacia atras, aunque el formato empiece simple.

## 7. Normalizacion

Normalizar significa ajustar los datos a una forma valida y esperada.

Ejemplos:

- nombres de carpeta saneados
- dimensiones pares para video
- valores por defecto cuando falta un campo legacy

## 8. Datos efimeros frente a datos durables

No todo dato merece persistencia.

En este producto:

- miniaturas internas de sesion son efimeras
- el JSON del proyecto es durable
- la carpeta de render es durable

Esta separacion es una decision de diseño, no un detalle casual.

## 9. Leccion general

El diseño de formatos de datos es parte de la arquitectura del producto.

Persistir bien significa:

- guardar lo necesario
- no guardar basura interna
- mantener compatibilidad razonable
- facilitar reanudacion y portabilidad
