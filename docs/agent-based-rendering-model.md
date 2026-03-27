# Modelo de Render Basado en Agentes

## 1. Proposito

Este documento recupera y formaliza una idea fundacional del proyecto:

- el render no debe pensarse como un calculo monolitico del fractal completo
- debe entenderse como un conjunto de agentes de trabajo
- cada agente se encarga de una region concreta del frame

En la implementacion actual, esa idea se traduce tecnicamente en:

- `workers`
- `jobs`
- `tiles`
- cancelacion cooperativa
- agregacion de progreso

La palabra `agente` se conserva aqui como concepto de alto nivel, porque ayuda a explicar el sistema de forma intuitiva y arquitectonicamente rica.

## 2. Idea central

La idea original del modelo es esta:

1. el usuario define una camara sobre una region del fractal
2. esa vista visible se divide en sectores o baldosas
3. cada agente de render toma uno de esos sectores
4. el agente calcula solo esa parte del fractal
5. el sistema agrega todos los resultados para construir el frame completo

Esto significa que el software:

- no renderiza todo el fractal matematico
- no renderiza una “imagen infinita”
- renderiza solo el rectangulo visible del viewport o del frame final

## 3. Del concepto de agente a la implementacion real

En el lenguaje del usuario o del producto, puede hablarse de agentes.

En el lenguaje del codigo, esos agentes se reflejan en varias piezas:

- `WorkerPoolManager`
- `RenderJob`
- `JobCancellationToken`
- pipeline de preview por tiles
- renderers que procesan regiones parciales

En otras palabras:

- **agente** es el concepto
- **worker** es la unidad ejecutora concreta
- **tile** es la porcion espacial que procesa
- **job** es el trabajo grande que agrupa muchas tareas

## 4. Jerarquia del trabajo

El modelo puede verse como una jerarquia:

### Nivel 1: Proyecto

Contiene:

- formula fractal
- puntos del recorrido
- configuracion de render
- paleta de color

### Nivel 2: Render Job

Representa algo como:

- “producir este video”
- “calcular esta secuencia”

### Nivel 3: Frame

Cada job de video se divide en muchos frames.

### Nivel 4: Tile o baldosa

Cada frame puede dividirse en regiones rectangulares.

### Nivel 5: Agente o worker

Cada agente procesa una o varias baldosas.

## 5. Por que este modelo es correcto

Este modelo tiene sentido por varias razones computacionales:

- permite paralelismo natural
- evita trabajo innecesario fuera del viewport
- hace posible medir progreso parcial
- permite cancelar trabajo obsoleto
- se adapta a preview y render final

Sin esta descomposicion, el sistema se vuelve:

- menos escalable
- menos explicable
- menos responsive

## 6. Agentes y viewport

La coordenada esencial no es “todo el fractal”, sino el viewport visible.

El viewport depende de:

- centro de camara
- zoom
- relacion de aspecto
- resolucion actual

Una vez definido el viewport:

- se sabe exactamente que region matematica debe mapearse a pixeles
- esa region se divide en tiles
- los agentes procesan solo esos tiles

## 7. Agentes y progreso

Una ventaja importante del modelo por agentes es que el progreso deja de ser un numero ficticio.

Se puede medir progreso real en funcion de:

- tiles completados
- frames completados
- jobs terminados

Esto permite a la UI mostrar:

- estado del preview
- estado del render final
- cola de trabajos
- posibilidad de cancelacion

## 8. Agentes y cancelacion cooperativa

Si el usuario hace zoom otra vez o cambia de idea, los agentes del render viejo ya no deben monopolizar CPU.

Por eso el sistema usa cancelacion cooperativa:

- el job o preview viejo se invalida
- los agentes consultan un token o monitor
- el trabajo obsoleto se detiene sin corromper el sistema

Esto es especialmente importante en deep zoom, donde una sola region puede consumir bastante tiempo.

## 9. Agentes para preview vs agentes para render final

No todos los agentes trabajan con la misma exigencia.

### Preview rapido

Los agentes de preview:

- usan menos precision
- usan menos iteraciones
- pueden trabajar sobre una resolucion efectiva reducida
- priorizan latencia baja

### Render final

Los agentes de render final:

- usan mas fidelidad
- procesan resolucion de salida real
- participan en una secuencia completa
- alimentan el pipeline de video

La arquitectura permite que ambos existan sin mezclar sus objetivos.

## 10. Relacion con el codigo

Este modelo se puede rastrear en varias clases:

- `application.preview.GeneratePreviewUseCase`
- `application.preview.TiledPreviewRenderer`
- `infrastructure.batching.WorkerPoolManager`
- `infrastructure.rendering.AbstractFrameRenderer`
- `infrastructure.rendering.PreviewFrameRenderer`
- `infrastructure.rendering.FinalFrameRenderer`

Y tambien en las piezas de coordinacion:

- `presentation.shell.StudioPreviewCoordinator`
- `domain.fractal.FractalIterationMonitor`
- `infrastructure.batching.JobCancellationToken`

## 11. Relacion con la idea de IA o subagentes

Aqui es importante distinguir dos cosas.

Este proyecto no usa agentes inteligentes en el sentido de IA deliberativa. No hay agentes que razonen estrategicamente sobre el fractal.

Lo que si existe es una **arquitectura de agentes de trabajo computacional**, donde:

- cada agente procesa una parte
- el sistema coordina resultados
- el objetivo es rendimiento, control y modularidad

Eso es totalmente valido y muy comun en sistemas de render, batch y paralelizacion.

## 12. Valor didactico del modelo

Desde el punto de vista academico y profesional, este modelo sirve para enseñar:

- descomposicion espacial de un problema visual
- paralelismo por regiones
- coordinacion de tareas
- cancelacion cooperativa
- relacion entre arquitectura conceptual y codigo real

## 13. Posibles extensiones futuras

Este modelo aun podria crecer hacia:

- agentes con prioridad dinamica segun region visible
- agentes especializados por formula o precision
- particion adaptativa en tiles no uniformes
- heuristicas de carga por dificultad local del fractal
- render distribuido entre procesos o maquinas

## 14. Resumen

La idea original de “un agente por parte del fractal” no solo sigue teniendo sentido: describe con bastante precision la filosofia del render del sistema.

En la implementacion concreta, esos agentes aparecen como workers coordinados que procesan tiles dentro de jobs observables y cancelables. Esa traduccion entre intuicion conceptual y estructura tecnica es una de las piezas mas ricas del proyecto.
