# Mapa de Documentacion

## Proposito

Esta carpeta contiene dos grupos de documentos distintos:

- documentos de levantamiento y evolucion incremental del proyecto
- documentacion tecnica, cientifica y de ingenieria del producto ya construido

La idea es que una persona pueda entrar a `docs/` y distinguir rapidamente entre:

- que se pidio y como fue creciendo la aplicacion
- como funciona realmente el sistema y sobre que fundamentos esta construido

## Documentos de referencia rapida

- [implementation-architecture.md](implementation-architecture.md)
  - resume la arquitectura en capas, fronteras y decisiones principales
- [project-tree.md](project-tree.md)
  - explica la organizacion del arbol del proyecto
- [release-checklist.md](release-checklist.md)
  - lista de verificacion para empaquetado y entrega

## Documentacion de ingenieria y teoria

- [software-engineering-and-design-patterns.md](software-engineering-and-design-patterns.md)
  - patrones de diseno, modularidad, responsabilidades y decisiones de arquitectura
- [computing-concepts-glossary.md](computing-concepts-glossary.md)
  - glosario de conceptos computacionales usados por el producto y el codigo
- [software-product-computing-foundations.md](software-product-computing-foundations.md)
  - fundamentos generales de computacion aplicados al software construido
- [explicit-computing-concepts-and-software-solutions.md](explicit-computing-concepts-and-software-solutions.md)
  - conceptos computacionales explicitos y soluciones tipicas de ingenieria de software aplicadas al producto
- [java-concurrency-execution-model-and-completablefuture.md](java-concurrency-execution-model-and-completablefuture.md)
  - concurrencia en Java, hilo de UI, pools, futures, backpressure y modelo de ejecucion del producto
- [data-structures-used-in-the-product.md](data-structures-used-in-the-product.md)
  - listas, colas, arboles, buffers, snapshots y estructuras usadas por el sistema
- [design-patterns-with-repository-examples.md](design-patterns-with-repository-examples.md)
  - patrones de diseno explicados con problemas, soluciones y ejemplos del repositorio
- [complex-numbers-escape-time-and-fractal-rendering.md](complex-numbers-escape-time-and-fractal-rendering.md)
  - numeros complejos, plano complejo, algoritmo de escape-time y render fractal
- [code-guided-theory-index.md](code-guided-theory-index.md)
  - indice que conecta teoria con clases y modulos concretos del repositorio
- [data-structures-algorithms-and-complexity.md](data-structures-algorithms-and-complexity.md)
  - estructuras de datos, algoritmos y consideraciones de complejidad
- [computation-theory-and-fractal-science.md](computation-theory-and-fractal-science.md)
  - base matematica del fractal, teoria de computacion aplicable y fundamentos cientificos
- [numerical-precision-and-rendering-model.md](numerical-precision-and-rendering-model.md)
  - precision numerica, deep zoom, cancelacion cooperativa y pipeline de render
- [desktop-ui-state-and-mvvm.md](desktop-ui-state-and-mvvm.md)
  - arquitectura de cliente JavaFX, estado observable y separacion view/viewmodel
- [concurrency-cancellation-and-parallel-rendering.md](concurrency-cancellation-and-parallel-rendering.md)
  - concurrencia, pools, cancelacion cooperativa y render paralelo por tiles
- [agent-based-rendering-model.md](agent-based-rendering-model.md)
  - explicacion conceptual del render por agentes, workers, tiles y jobs
- [api-boundaries-and-layer-contracts.md](api-boundaries-and-layer-contracts.md)
  - contratos entre capas, fachadas, DTOs y fronteras internas de la aplicacion
- [jvm-memory-performance-and-resource-management.md](jvm-memory-performance-and-resource-management.md)
  - heap, buffers, presion de memoria, cancelacion y gestion de recursos en tiempo de ejecucion
- [persistence-json-and-project-lifecycle.md](persistence-json-and-project-lifecycle.md)
  - persistencia JSON, carpeta de render y ciclo de vida del proyecto
- [testing-quality-and-verification.md](testing-quality-and-verification.md)
  - estrategia de pruebas, QA y calidad operativa
- [video-pipeline-and-media-encoding.md](video-pipeline-and-media-encoding.md)
  - secuencia temporal, frames intermedios y codificacion MP4
- [windows-packaging-installation-and-delivery.md](windows-packaging-installation-and-delivery.md)
  - empaquetado, instalacion, runtime embebido, iconos y entrega del producto en Windows
- [error-handling-failures-and-recovery-in-interactive-software.md](error-handling-failures-and-recovery-in-interactive-software.md)
  - errores, fallos, recovery, cancelacion y recuperacion en software interactivo
- [state-machines-and-lifecycle-models.md](state-machines-and-lifecycle-models.md)
  - estados, transiciones y modelos de ciclo de vida del producto
- [time-space-complexity-and-scaling-of-rendering.md](time-space-complexity-and-scaling-of-rendering.md)
  - complejidad temporal, espacial y escalado del render por tiles y del pipeline de video
- [serialization-data-formats-and-compatibility.md](serialization-data-formats-and-compatibility.md)
  - serializacion, formatos persistentes, schema logico y compatibilidad
- [digital-media-fps-resolution-codec-and-container.md](digital-media-fps-resolution-codec-and-container.md)
  - fundamentos multimedia de frame, FPS, resolucion, codec y contenedor

## Documentos historicos de levantamiento

- `javafx_00.md` a `javafx_07.md`

Estos markdowns representan el proceso de levantamiento, refinamiento y construccion del producto. Se conservan porque forman parte de la trazabilidad del proyecto, pero no sustituyen la documentacion tecnica formal.

## Orden recomendado de lectura

1. `README.md`
2. `implementation-architecture.md`
3. `computing-concepts-glossary.md`
4. `software-product-computing-foundations.md`
5. `explicit-computing-concepts-and-software-solutions.md`
6. `java-concurrency-execution-model-and-completablefuture.md`
7. `data-structures-used-in-the-product.md`
8. `design-patterns-with-repository-examples.md`
9. `complex-numbers-escape-time-and-fractal-rendering.md`
10. `code-guided-theory-index.md`
11. `software-engineering-and-design-patterns.md`
12. `data-structures-algorithms-and-complexity.md`
13. `computation-theory-and-fractal-science.md`
14. `numerical-precision-and-rendering-model.md`
15. `desktop-ui-state-and-mvvm.md`
16. `concurrency-cancellation-and-parallel-rendering.md`
17. `agent-based-rendering-model.md`
18. `api-boundaries-and-layer-contracts.md`
19. `jvm-memory-performance-and-resource-management.md`
20. `persistence-json-and-project-lifecycle.md`
21. `video-pipeline-and-media-encoding.md`
22. `windows-packaging-installation-and-delivery.md`
23. `error-handling-failures-and-recovery-in-interactive-software.md`
24. `state-machines-and-lifecycle-models.md`
25. `time-space-complexity-and-scaling-of-rendering.md`
26. `serialization-data-formats-and-compatibility.md`
27. `digital-media-fps-resolution-codec-and-container.md`
28. `testing-quality-and-verification.md`
29. `project-tree.md`
