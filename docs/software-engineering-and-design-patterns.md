# Ingenieria de Software y Patrones de Diseno

## Objetivo de este documento

Este documento explica como se aplicaron principios de ingenieria de software al sistema `Fractal Render Studio`, no solo desde la arquitectura general, sino desde la implementacion concreta de los modulos, la UI, la persistencia y el pipeline de render.

## 1. Estilo arquitectonico principal

El sistema sigue una arquitectura en capas con separacion explicita de responsabilidades:

- `domain`
- `application`
- `infrastructure`
- `presentation`

Esta division responde a tres objetivos:

1. aislar las reglas del problema de los detalles de JavaFX
2. permitir evolucionar almacenamiento, render y exportacion sin tocar el dominio
3. mantener la UI como cliente de casos de uso y no como dueña de la logica

## 2. Principios SOLID aplicados

### Responsabilidad unica

Las clases mas importantes tienden a tener una razon de cambio dominante:

- `Project` mantiene el estado y las invariantes del proyecto
- `StudioShellViewModel` coordina estado de presentacion
- `ProjectFileRepository` persiste el proyecto
- `WorkerPoolManager` orquesta jobs de render
- `Mp4SequenceVideoExporter` codifica video

### Abierto/cerrado

La familia de formulas fractales se extendio sin cambiar el contrato central de uso. Mandelbrot, Burning Ship, Tricorn y Celtic Mandelbrot se integran como variaciones del mismo modelo de formula de escape.

### Sustitucion de Liskov

Los renderers concretos de preview y final respetan el mismo contrato conceptual. Esto permite intercambiar estrategias de render sin romper al consumidor de aplicacion.

### Segregacion de interfaces

Se evitaron interfaces gigantes. Por ejemplo:

- contratos de repositorio para proyecto e historial
- contratos de exportacion de secuencias y video
- contratos de gateway para jobs

### Inversion de dependencias

La UI no depende directamente de clases de persistencia o de render concreto. Depende de facades y casos de uso que luego se resuelven en el bootstrap.

## 3. Patrones de diseno utilizados

## Fachada

`ProjectFacade`, `RenderFacade` y `RenderHistoryFacade` encapsulan flujos de negocio completos y presentan una API de aplicacion estable a `presentation`.

Esto reduce:

- acoplamiento de la UI con docenas de casos de uso
- dispersion de reglas
- complejidad de montaje en la shell

## Agregado

`Project` funciona como agregado principal.

Agrupa:

- formula fractal
- configuracion del proyecto
- metadata
- puntos del recorrido
- timeline derivable
- parametros de render

La idea es que cambios significativos del proyecto no se hagan saltando por varios objetos sueltos, sino pasando por el borde del agregado o por casos de uso que lo coordinan.

## Value Objects

Se modelan conceptos de precision y camara como valores:

- coordenadas fractales
- nivel de zoom
- metadata
- settings
- presets

Esto evita que valores de dominio importantes se manipulen como primitivas sin semantica.

## Repositorio

La persistencia del proyecto se abstrae mediante repositorios. La implementacion actual es JSON, pero el resto del sistema no depende de Jackson de forma directa.

## Factory

Se usan factories o catalogos cuando el sistema necesita resolver variaciones:

- formulas fractales
- paletas de color
- estrategias de renderer
- perfiles de preset

## Strategy

Hay varias decisiones que se resuelven como estrategia:

- render rapido vs render final
- politica de calidad adaptativa
- politica de limite de zoom
- codificacion de salida

## Builder implicito en el bootstrap

`ApplicationBootstrap` y la composicion de la app cumplen el papel de ensamblaje. Aunque no se use un builder formal, el bootstrap construye el grafo de objetos de forma centralizada.

## 4. Decisiones de UX con impacto de arquitectura

La UX no se trata como decoracion. Varias decisiones de arquitectura fueron tomadas para sostener interacciones fluidas:

- preview automatico barato y cancelable
- render final separado del preview
- drawer inferior desacoplado del viewport principal
- puntos unificados como concepto operativo para timeline y recorrido
- inspector editable solo donde tiene sentido de dominio

Esto evita que la UI sea una suma arbitraria de paneles y estados duplicados.

## 5. Trazabilidad y persistencia explicita

El proyecto no depende de autosave implícito entre sesiones. La decision fue:

- sesion efimera al abrir la app
- persistencia solo cuando el usuario guarda proyecto o genera carpeta de render

Esto favorece previsibilidad y reduce el riesgo de estados fantasmas o residuos de sesiones anteriores.

## 6. Concurrencia y responsabilidad tecnica

La concurrencia no se deja dispersa por la UI.

Se centraliza en:

- pools configurados en bootstrap
- coordinador de preview
- manager de render jobs

Y se aplica cancelacion cooperativa para evitar que trabajos viejos saturen CPU cuando el usuario ya pidio otra vista.

## 7. Mantenibilidad

Las decisiones de mantenibilidad mas importantes son:

- documentacion por capas
- README orientado a uso y entrega
- rutas de recursos neutras en `assets/`
- empaquetado Windows en script reproducible
- pruebas unitarias y de comportamiento en modulos clave

## 8. Deuda tecnica controlada

Aunque el sistema ya esta en estado utilizable, existen fronteras donde la deuda tecnica es reconocida:

- el `ViewModel` principal sigue siendo denso
- el paquete Java mantiene un namespace heredado
- el pipeline de precision extrema puede seguir refinandose por formula

La diferencia es que esta deuda es visible, localizada y documentada.
