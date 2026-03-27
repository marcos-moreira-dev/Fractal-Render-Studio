# Patrones de Diseno con Ejemplos del Repositorio

## Proposito

Este documento explica patrones de diseño desde un punto de vista didactico, usando el repositorio como caso concreto.

La idea no es "nombrar patrones para adornar". Es mostrar:

- que problema resuelve cada patron
- por que aparece en este producto
- que valor practico aporta

## 1. Facade

### Problema

La interfaz necesita varias operaciones relacionadas, pero no deberia conocer todos los detalles internos.

### Solucion

Una fachada ofrece una superficie de acceso simplificada.

### En este producto

Las fachadas agrupan operaciones de:

- proyecto
- render

### Valor

- reduce acoplamiento
- simplifica la capa de presentacion
- hace mas legible el flujo de casos de uso

## 2. Repository

### Problema

El dominio no deberia depender directamente de JSON, disco o formatos concretos.

### Solucion

Separar la persistencia mediante repositorios.

### Valor

- desacopla dominio e infraestructura
- hace mas facil cambiar o ampliar almacenamiento
- mejora pruebas

## 3. Mapper

### Problema

El modelo de dominio y el modelo persistente o de transporte no siempre deben ser iguales.

### Solucion

Introducir mapeadores explicitos.

### Valor

- evita contaminar el dominio
- hace visible la traduccion de modelos
- reduce ambiguedad

## 4. Aggregate

### Problema

Cuando varios datos deben mantenerse coherentes, tratarlos por separado puede romper invariantes.

### Solucion

Modelarlos como agregado.

### Valor

- integridad conceptual
- control de reglas
- mutaciones mas seguras

## 5. ViewModel

### Problema

Una vista JavaFX no deberia mezclar render visual, estado de sesion y logica de producto en el mismo sitio.

### Solucion

Introducir un view model con propiedades observables y comandos.

### Valor

- UI mas limpia
- estado mas trazable
- mejor testabilidad conceptual

## 6. Strategy

### Problema

No todo render se comporta igual. Hay diferencias entre preview rapido, refinado y render final.

### Solucion

Usar estrategias o politicas intercambiables.

### Valor

- permite cambiar calidad o precision segun contexto
- evita condicionales gigantes
- mejora extension futura

## 7. Factory

### Problema

Cuando crear un objeto requiere decidir entre varias variantes, la construccion repetida se vuelve confusa.

### Solucion

Usar una fabrica.

### Valor

- centraliza decisiones de creacion
- reduce duplicacion
- expresa mejor la intencion del sistema

## 8. Coordinator / Orchestrator

### Problema

En un sistema interactivo hay flujos donde varias partes deben coordinarse.

### Solucion

Introducir un coordinador.

### En este producto

La coordinacion del preview evita que frames viejos se impongan sobre el viewport actual.

### Valor

- controla concurrencia
- reduce caos asincrono
- protege la experiencia de usuario

## 9. Adapter conceptual

### Problema

Un subsistema espera un tipo de interfaz, pero otro ofrece otra forma de trabajar.

### Solucion

Adaptar una representacion a otra.

Aunque no siempre haya una clase llamada `Adapter`, el patron puede existir conceptualmente cuando la aplicacion traduce entre capas o tecnologias.

## 10. Template Method conceptual

### Problema

Hay algoritmos muy parecidos con variaciones puntuales.

### Solucion

Definir una estructura general comun y dejar pasos variables.

Este tipo de patron es frecuente en renderizado y formula fractal, aun cuando no siempre se nombre de forma academica.

## 11. Observer / Observable State

### Problema

La interfaz necesita reaccionar a cambios del estado sin consultar constantemente.

### Solucion

Usar estado observable.

### Valor

- desacopla emisor y receptor
- facilita interfaces reactivas
- mejora sincronizacion entre vista y modelo de presentacion

## 12. Builder conceptual

### Problema

Hay objetos o configuraciones que requieren ensamblar varias decisiones antes de quedar listos.

### Solucion

Construirlos paso a paso.

Esto se ve conceptualmente en configuraciones de render o armado de jobs, aun si no todo usa una clase `Builder` formal.

## 13. Null Object o valores por defecto controlados

### Problema

Un sistema necesita valores validos incluso cuando el usuario todavia no ha configurado algo.

### Solucion

Usar defaults controlados y semanticamente utiles.

### Valor

- mejora arranque del sistema
- reduce nulos innecesarios
- evita estados imposibles

## 14. Leccion general

Los patrones de diseno no son una coleccion ornamental. Son soluciones recurrentes a problemas recurrentes.

Este repositorio sirve como material didactico porque permite ver esos patrones en un producto concreto:

- arquitectura por capas
- persistencia desacoplada
- UI reactiva
- render concurrente
- exportacion multimedia
