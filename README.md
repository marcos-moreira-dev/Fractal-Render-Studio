![Fractal Render Studio](src/main/resources/assets/images/app-wordmark.png)

# Fractal Render Studio

Explora fractales, guarda recorridos profundos y conviertelos en video MP4 desde una aplicacion de escritorio pensada para trabajo visual real, no para pruebas de laboratorio.

Fractal Render Studio esta orientado a una experiencia directa:
- exploras el fractal en tiempo real
- guardas puntos matematicos del recorrido
- interpolas automaticamente los estados intermedios
- renderizas video en una carpeta de trabajo autocontenida

## Que hace

- exploracion interactiva con paneo y zoom profundo
- puntos de camara con miniaturas
- timeline visual basado en puntos
- inspector editable de formula, paleta e iteraciones
- render de video MP4 como salida principal
- carpeta de render con `render.mp4`, `frames/` y `project.fractalstudio.json`
- proyecto reabrible desde el JSON generado
- modo de sesion efimero: al cerrar, no arrastra trabajo anterior interno

## Capturas

### Vista principal

![Vista principal](docs/readme-images/vista%20principal.png)

### Timeline y puntos

![Timeline](docs/readme-images/timeline.png)

### Metricas y cola de render

![Metrics](docs/readme-images/metrics.png)

## Flujo de trabajo

1. Abre un proyecto limpio.
2. Explora el fractal con scroll y paneo.
3. Guarda puntos del recorrido.
4. Define duracion y FPS del video.
5. Elige una carpeta base y el nombre del render.
6. El sistema crea una carpeta propia del render con todo lo necesario.
7. Reabre el proyecto mas tarde usando el `project.fractalstudio.json` guardado dentro de esa carpeta.

## Formulas disponibles

- Mandelbrot
- Burning Ship
- Tricorn
- Celtic Mandelbrot

## Salida del render

Cada render crea una carpeta de trabajo dentro del directorio que elijas. Ahi encontraras:

- `render.mp4`
- `frames/` si hubo frames temporales
- `project.fractalstudio.json`

Ese JSON contiene el proyecto completo para volver a abrirlo en la aplicacion.

## Punto de entrada

La aplicacion arranca desde:

- [FractalStudioApplication.java](src/main/java/com/marcos/fractalstudio/FractalStudioApplication.java)

## Ejecutar en desarrollo

Requisitos:

- JDK 21
- Maven Wrapper incluido

Windows:

```powershell
.\mvnw.cmd javafx:run
```

## Verificacion

```powershell
.\mvnw.cmd -q compile
.\mvnw.cmd -q test
```

## Generar instalador de Windows

El proyecto incluye script de empaquetado para generar un instalador real de Windows. La salida recomendada para distribucion es `MSI`, y ademas puedes obtener un `app-image` portable para validacion local.

```powershell
.\scripts\package-windows.ps1 -Type msi
```

Salida esperada:

- `target/installer/FractalRenderStudio-0.1.0.msi`

Tambien puedes elegir el tipo manualmente:

```powershell
.\scripts\package-windows.ps1 -Type exe
.\scripts\package-windows.ps1 -Type msi
.\scripts\package-windows.ps1 -Type app-image
```

## Arquitectura

```text
src/main/java/com/marcos/fractalstudio
|-- presentation
|-- application
|-- domain
`-- infrastructure
```

Documentacion complementaria:

- [Mapa documental](docs/documentation-map.md)
- [Glosario de conceptos computacionales](docs/computing-concepts-glossary.md)
- [Fundamentos de computacion aplicados al producto](docs/software-product-computing-foundations.md)
- [Conceptos computacionales explicitos y soluciones tipicas](docs/explicit-computing-concepts-and-software-solutions.md)
- [Concurrencia en Java y modelo de ejecucion](docs/java-concurrency-execution-model-and-completablefuture.md)
- [Estructuras de datos usadas en el producto](docs/data-structures-used-in-the-product.md)
- [Patrones de diseno con ejemplos del repositorio](docs/design-patterns-with-repository-examples.md)
- [Numeros complejos, escape-time y render fractal](docs/complex-numbers-escape-time-and-fractal-rendering.md)
- [Indice teoria-codigo](docs/code-guided-theory-index.md)
- [Arquitectura implementada](docs/implementation-architecture.md)
- [Ingenieria de software y patrones](docs/software-engineering-and-design-patterns.md)
- [Estructuras de datos, algoritmos y complejidad](docs/data-structures-algorithms-and-complexity.md)
- [Teoria de la computacion y ciencia del fractal](docs/computation-theory-and-fractal-science.md)
- [Precision numerica y modelo de render](docs/numerical-precision-and-rendering-model.md)
- [UI de escritorio, estado y MVVM](docs/desktop-ui-state-and-mvvm.md)
- [Concurrencia, cancelacion y render paralelo](docs/concurrency-cancellation-and-parallel-rendering.md)
- [Modelo de render basado en agentes](docs/agent-based-rendering-model.md)
- [Fronteras de API y contratos entre capas](docs/api-boundaries-and-layer-contracts.md)
- [JVM, memoria y gestion de recursos](docs/jvm-memory-performance-and-resource-management.md)
- [Persistencia JSON y ciclo de vida del proyecto](docs/persistence-json-and-project-lifecycle.md)
- [Errores, fallos y recovery en software interactivo](docs/error-handling-failures-and-recovery-in-interactive-software.md)
- [Maquinas de estado y ciclos de vida](docs/state-machines-and-lifecycle-models.md)
- [Complejidad temporal, espacial y escalado del render](docs/time-space-complexity-and-scaling-of-rendering.md)
- [Serializacion, formatos y compatibilidad](docs/serialization-data-formats-and-compatibility.md)
- [Pruebas, calidad y verificacion](docs/testing-quality-and-verification.md)
- [Pipeline de video y codificacion multimedia](docs/video-pipeline-and-media-encoding.md)
- [Multimedia digital: FPS, resolucion, codec y contenedor](docs/digital-media-fps-resolution-codec-and-container.md)
- [Empaquetado, instalacion y entrega en Windows](docs/windows-packaging-installation-and-delivery.md)
- [Arbol del proyecto](docs/project-tree.md)
- [Checklist de release](docs/release-checklist.md)

## Estado del producto

Esta version ya esta pensada como entrega usable:

- UX de escritorio para exploracion fractal
- render final orientado a video
- proyectos guardables y reabribles
- empaquetado para distribucion en Windows
