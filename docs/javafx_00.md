# JavaFX 00 — Visión y producto

## Nombre provisional
**Fractal Render Studio**

## Frase corta del producto
Aplicación desktop en **JavaFX** para explorar fractales, definir trayectorias de cámara mediante **keyframes**, renderizar secuencias por **lotes** y exportarlas como imágenes o video.

## Qué es
Fractal Render Studio es un **estudio de producción visual** orientado a fractales 2D, especialmente pensado para:

- explorar visualmente un fractal mediante paneo y zoom;
- capturar estados de cámara como keyframes;
- interpolar una trayectoria entre keyframes;
- generar frames intermedios;
- procesar renders en segundo plano mediante una infraestructura de trabajos por lotes;
- exportar imágenes y secuencias de video.

No se concibe como una demo matemática aislada, sino como un **producto de escritorio serio** con separación clara entre dominio, aplicación, infraestructura y presentación.

## Problema que resuelve
Normalmente, explorar fractales y producir animaciones implica usar herramientas separadas, scripts manuales o programas con una experiencia poco estructurada para composición, render y exportación.

Este proyecto busca unificar en una sola aplicación:

1. **Exploración visual** del fractal.
2. **Composición de trayectorias** mediante keyframes.
3. **Render por lotes** de alta resolución.
4. **Exportación** de artefactos listos para producción.
5. **Monitoreo técnico** del procesamiento: cola, progreso, estados, métricas y errores.

## Usuario objetivo
Perfil principal:

- persona técnica o creativa que quiere generar imágenes y videos de fractales;
- usuario que valora una app desktop controlada, sin depender del navegador;
- desarrollador que desea una arquitectura rica en patrones, modularidad y procesamiento concurrente.

Perfil secundario:

- usuario curioso que quiere experimentar con paletas, zooms y trayectorias sin escribir scripts.

## Objetivos del producto
### Objetivo principal
Permitir crear una secuencia visual fractal desde una navegación interactiva hasta un render final exportable.

### Objetivos técnicos
- Mantener una **arquitectura limpia y modular**.
- Separar el **dominio fractal** de la **infraestructura de render batch**.
- Permitir crecimiento hacia más fórmulas, colorizaciones y modos de exportación.
- Ofrecer una UI de escritorio clara, rica y profesional en JavaFX.

## Alcance de la versión 1
La primera versión debe incluir como mínimo:

### Exploración
- visualización de un fractal 2D;
- paneo;
- zoom;
- actualización de preview.

### Composición
- captura de keyframes;
- edición básica de keyframes;
- timeline simple;
- interpolación entre keyframes.

### Render
- generación de frames intermedios;
- render por lotes;
- cola de trabajos;
- progreso por trabajo;
- cancelación básica.

### Color y salida
- al menos una fórmula fractal inicial;
- al menos un esquema de colorización básico;
- exportación a PNG;
- posibilidad de ensamblado posterior en video.

### UI
- viewport principal;
- panel de timeline;
- inspector de parámetros;
- render queue;
- navegación interna básica.

## Fuera de alcance en versión 1
Quedan explícitamente fuera de la primera versión:

- editor de video profesional;
- motor 3D completo;
- simulación física;
- IA avanzada o agentes inteligentes complejos;
- edición colaborativa en red;
- backend remoto;
- render distribuido entre varias máquinas.

## Identidad conceptual del sistema
Este sistema debe entenderse como una mezcla de:

- **explorador fractal**;
- **editor de trayectoria de cámara**;
- **workbench de render batch**;
- **herramienta desktop de producción visual**.

No debe diseñarse como un simple viewer ni como un proyecto académico desordenado.

## Valor diferencial
El valor del producto está en combinar en una sola app:

- navegación interactiva;
- composición por keyframes;
- render por lotes;
- exportación;
- monitoreo técnico del procesamiento.

La propuesta no es solo “dibujar fractales”, sino **componer, producir y exportar secuencias fractales**.

## Capacidades principales esperadas
- Crear y guardar proyectos.
- Navegar sobre una región fractal.
- Registrar keyframes a voluntad.
- Construir una trayectoria de cámara.
- Elegir parámetros de render y color.
- Ejecutar renders en segundo plano.
- Ver estados, progreso y errores.
- Exportar los resultados.

## Restricciones técnicas deseadas
- Aplicación **desktop monolítica modular**.
- UI desarrollada con **JavaFX**.
- Procesamiento pesado fuera del hilo de UI.
- Posible uso de herramienta externa para ensamblado de video.
- Dominio desacoplado de la interfaz gráfica y de la concurrencia concreta.

## Criterios de calidad
El sistema debe aspirar a:

- claridad conceptual;
- nombres consistentes;
- extensibilidad;
- separación de responsabilidades;
- buena experiencia visual;
- trazabilidad de jobs y resultados;
- comportamiento predecible.

## Resumen ejecutivo
Fractal Render Studio es una aplicación desktop en JavaFX orientada a la exploración, composición y producción de secuencias fractales. Su núcleo conceptual combina keyframes de cámara, render por lotes y exportación visual, apoyado por una arquitectura modular donde el dominio fractal permanece separado de la infraestructura de ejecución y de la interfaz gráfica.