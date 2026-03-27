![Fractal Render Studio](src/main/resources/assets/images/app-wordmark.png)

# Fractal Render Studio

Explora fractales, guarda recorridos profundos y conviértelos en video MP4 desde una aplicación de escritorio pensada para trabajo visual real, no para pruebas de laboratorio.

Fractal Render Studio está orientado a una experiencia directa:
- exploras el fractal en tiempo real;
- guardas puntos matemáticos del recorrido;
- interpolas automáticamente los estados intermedios;
- renderizas video en una carpeta de trabajo autocontenida.

## Qué hace

- exploración interactiva con paneo y zoom profundo
- puntos de cámara con miniaturas
- timeline visual basado en puntos
- inspector editable de fórmula, paleta e iteraciones
- render de video MP4 como salida principal
- carpeta de render con `render.mp4`, `frames/` y `project.fractalstudio.json`
- proyecto reabrible desde el JSON generado
- modo de sesión efímero: al cerrar, no arrastra trabajo anterior interno

## Capturas

### Vista principal

![Vista principal](docs/readme-images/vista%20principal.png)

### Timeline y puntos

![Timeline](docs/readme-images/timeline.png)

### Métricas y cola de render

![Metrics](docs/readme-images/metrics.png)

## Flujo de trabajo

1. Abre un proyecto limpio.
2. Explora el fractal con scroll y paneo.
3. Guarda puntos del recorrido.
4. Define duración y FPS del video.
5. Elige una carpeta base y el nombre del render.
6. El sistema crea una carpeta propia del render con todo lo necesario.
7. Reabre el proyecto más tarde usando el `project.fractalstudio.json` guardado dentro de esa carpeta.

## Fórmulas disponibles

- Mandelbrot
- Burning Ship
- Tricorn
- Celtic Mandelbrot

## Salida del render

Cada render crea una carpeta de trabajo dentro del directorio que elijas. Ahí encontrarás:

- `render.mp4`
- `frames/` si hubo frames temporales
- `project.fractalstudio.json`

Ese JSON contiene el proyecto completo para volver a abrirlo en la aplicación.

## Ejecutar en desarrollo

Requisitos:

- JDK 21
- Maven Wrapper incluido

Windows:

```powershell
.\mvnw.cmd javafx:run
```

## Verificación

```powershell
.\mvnw.cmd -q compile
.\mvnw.cmd -q test
```

## Generar instalador de Windows

El proyecto ya incluye script de empaquetado para generar un instalador real de Windows. La salida recomendada para distribucion es `MSI`, y ademas puedes obtener un `app-image` portable para validacion local.

```powershell
.\scripts\package-windows.ps1 -Type msi
```

Salida esperada:

- `target/installer/FractalRenderStudio-0.1.0.msi`

También puedes elegir el tipo manualmente:

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

Documentación complementaria:

- [Arquitectura implementada](docs/implementation-architecture.md)
- [Árbol del proyecto](docs/project-tree.md)
- [Checklist de release](docs/release-checklist.md)

## Estado del producto

Esta versión ya está pensada como entrega usable:

- UX de escritorio para exploración fractal
- render final orientado a video
- proyectos guardables y reabribles
- empaquetado para distribución en Windows
