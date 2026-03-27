# JavaFX 01 — Dominio y modelo conceptual

## Propósito de este documento
Este documento define el **núcleo conceptual del dominio** de Fractal Render Studio. Su objetivo es fijar las entidades, objetos de valor, relaciones y reglas básicas del sistema sin contaminar el modelo con detalles de JavaFX, concurrencia, archivos, colas de ejecución o herramientas externas.

El dominio debe responder a esta pregunta:

> ¿Qué significa, conceptualmente, componer y producir una secuencia fractal?

## Principio general
El dominio **no** debe conocer:

- `Stage`, `Scene`, `Canvas`, `ImageView` u otros tipos de JavaFX;
- `ExecutorService`, `Task`, `Thread`, `Future` u otros tipos de concurrencia concreta;
- `Path`, `File`, `ProcessBuilder`, FFmpeg o detalles del sistema de archivos;
- JSON, FXML, base de datos o formato concreto de persistencia.

El dominio **sí** debe conocer:

- fractales;
- estados de cámara;
- keyframes;
- timelines;
- perfiles de render;
- perfiles de color;
- descriptores de frame;
- proyectos y configuraciones conceptuales.

---

# 1. Visión conceptual del dominio
El sistema modela una producción visual fractal como una secuencia de decisiones:

1. Se elige una **fórmula fractal**.
2. Se define una **cámara conceptual** sobre el plano fractal.
3. Se registran **keyframes**.
4. Se compone una **timeline**.
5. Se deriva una secuencia de **frames conceptuales**.
6. Cada frame se renderiza según un **perfil de render** y un **perfil de color**.
7. El resultado final es una **secuencia visual exportable**.

El dominio, por tanto, no es “dibujar píxeles”, sino:

> **componer, parametrizar y describir una secuencia fractal renderizable**.

---

# 2. Entidades principales

## 2.1 `Project`
Representa la unidad principal de trabajo del usuario.

### Responsabilidad
Agrupar toda la información conceptual necesaria para definir una producción fractal.

### Contenido típico
- identidad del proyecto;
- nombre;
- fórmula fractal activa o disponible;
- timeline;
- perfiles activos;
- configuración general;
- metadatos.

### Reglas
- un proyecto puede tener una o varias configuraciones visuales asociadas;
- un proyecto debe tener como mínimo una timeline válida para producir una secuencia;
- un proyecto debe poder describirse sin necesidad de UI ni ejecución batch.

---

## 2.2 `FractalFormula`
Representa una familia de fractal o regla matemática que permite evaluar puntos del plano.

### Responsabilidad
Definir el comportamiento conceptual de la fórmula fractal.

### Ejemplos futuros
- Mandelbrot;
- Julia;
- Multibrot;
- otras variantes 2D.

### Reglas
- debe ser intercambiable sin romper el resto del sistema;
- debe exponer los parámetros necesarios para su evaluación;
- no debe depender de una implementación gráfica concreta.

---

## 2.3 `CameraState`
Describe el estado conceptual de la cámara en un instante.

### Responsabilidad
Representar cómo se observa el fractal en un punto del tiempo.

### Atributos típicos
- centro `x`;
- centro `y`;
- nivel de zoom;
- rotación opcional futura;
- otros parámetros visuales relacionados con la observación.

### Reglas
- debe ser inmutable o tratarse como objeto de valor;
- debe poder interpolarse respecto de otro estado de cámara;
- no representa controles de UI, sino una vista matemática del plano fractal.

---

## 2.4 `Keyframe`
Representa un punto de control explícito dentro de una trayectoria.

### Responsabilidad
Fijar una configuración visual en una posición concreta de la timeline.

### Contenido típico
- posición temporal;
- estado de cámara;
- parámetros fractales relevantes;
- perfil de color opcionalmente asociado;
- notas o etiqueta opcional.

### Reglas
- un keyframe debe tener una posición temporal válida;
- dos keyframes pueden coexistir, pero el sistema debe definir claramente cómo se ordenan;
- debe ser posible interpolar el espacio entre dos keyframes compatibles.

---

## 2.5 `Timeline`
Representa la secuencia conceptual del proyecto.

### Responsabilidad
Ordenar keyframes y permitir derivar estados intermedios.

### Funciones conceptuales
- almacenar keyframes;
- garantizar orden temporal;
- exponer segmentos entre keyframes;
- permitir consultas como “estado en el tiempo t”.

### Reglas
- la timeline debe mantener una relación temporal consistente;
- debe tener al menos dos keyframes para una animación interpolada real;
- debe poder existir con un solo keyframe para imagen fija o escena aislada.

---

## 2.6 `RenderProfile`
Representa la intención conceptual del render.

### Responsabilidad
Describir cómo debe generarse visualmente una salida.

### Atributos típicos
- resolución;
- iteraciones máximas;
- radio de escape;
- calidad;
- modo preview o final;
- subdivisión conceptual del trabajo.

### Reglas
- no debe incluir detalles concretos de threads o colas;
- debe poder reutilizarse entre proyectos o secuencias;
- debe ser explícito respecto a sus parámetros relevantes.

---

## 2.7 `ColorProfile`
Representa la política conceptual de colorización.

### Responsabilidad
Definir cómo se traduce el resultado matemático del fractal a una interpretación cromática.

### Posibles enfoques
- iteración directa;
- iteración suavizada;
- gradientes;
- análisis derivado;
- pseudo-relieve o modo analítico futuro.

### Reglas
- debe ser intercambiable;
- no debe acoplarse a controles visuales concretos;
- puede tener parámetros propios.

---

## 2.8 `FrameDescriptor`
Representa la descripción conceptual de un frame individual.

### Responsabilidad
Concentrar todos los parámetros necesarios para definir un frame antes de su renderizado concreto.

### Contenido típico
- índice de frame;
- tiempo lógico;
- estado de cámara interpolado;
- fórmula fractal efectiva;
- perfil de render efectivo;
- perfil de color efectivo.

### Reglas
- debe ser completamente derivable desde el proyecto y la timeline;
- debe ser suficiente para describir el frame sin depender de UI o disco;
- es un objeto puente entre composición y producción.

---

# 3. Objetos de valor

## 3.1 `FractalCoordinate`
Representa una coordenada del plano fractal.

## 3.2 `ZoomLevel`
Representa el nivel de acercamiento de la cámara.

## 3.3 `TimePosition`
Representa una posición temporal dentro de la timeline.

## 3.4 `FrameIndex`
Representa el número conceptual de frame.

## 3.5 `Resolution`
Representa ancho y alto de salida.

## 3.6 `EscapeParameters`
Representa parámetros matemáticos como iteraciones máximas o radio de escape.

## 3.7 `ColorStop` / `Palette`
Representan una estructura de color reusable.

Estos objetos de valor ayudan a evitar primitivas sueltas por todo el sistema y mejoran legibilidad, validación y extensibilidad.

---

# 4. Relaciones entre conceptos

## Proyecto y timeline
- un `Project` contiene una `Timeline`;
- una `Timeline` contiene cero o más `Keyframe`;
- la utilidad real de la timeline aumenta cuando hay múltiples keyframes ordenados.

## Keyframe y cámara
- un `Keyframe` contiene un `CameraState`;
- el `CameraState` describe la vista matemática del fractal en ese punto.

## Proyecto y perfiles
- un `Project` puede tener un `RenderProfile` activo;
- un `Project` puede tener un `ColorProfile` activo;
- keyframes concretos podrían sobrescribir algunos parámetros en evoluciones futuras.

## Timeline y frames
- una `Timeline` puede producir una secuencia de `FrameDescriptor`;
- cada `FrameDescriptor` representa un estado visual completo para un instante discreto.

---

# 5. Invariantes del dominio

## Invariantes generales
- el dominio debe ser independiente de la UI;
- el dominio debe ser independiente de la ejecución batch concreta;
- los conceptos de proyecto, timeline y frame deben poder existir aunque todavía no se renderice nada.

## Invariantes de timeline
- los keyframes deben estar ordenados temporalmente o ser ordenables;
- no debe existir ambigüedad irresoluble en la posición temporal;
- el sistema debe poder definir cómo se comporta entre dos keyframes consecutivos.

## Invariantes de frame
- un `FrameDescriptor` debe tener un índice válido;
- debe tener una cámara válida;
- debe tener fórmula, render profile y color profile efectivos.

## Invariantes de perfiles
- un `RenderProfile` debe tener resolución válida;
- las iteraciones máximas y radios deben ser coherentes;
- un `ColorProfile` debe ser interpretable por el sistema.

---

# 6. Operaciones conceptuales del dominio
Estas operaciones pueden existir como métodos del dominio o como servicios de dominio, según convenga.

## 6.1 Interpolación de cámara
Dado un par de `CameraState`, obtener un estado intermedio.

## 6.2 Resolución temporal de timeline
Dado un tiempo `t`, determinar qué keyframes afectan ese instante.

## 6.3 Construcción de descriptores de frame
Dada una timeline y una configuración de salida, generar una secuencia ordenada de `FrameDescriptor`.

## 6.4 Validación conceptual del proyecto
Comprobar si un proyecto es renderizable desde el punto de vista del dominio.

## 6.5 Resolución de perfiles efectivos
Determinar qué fórmula, color y render profile aplican a un frame concreto.

---

# 7. Lo que NO pertenece al dominio
Los siguientes conceptos no deben modelarse dentro del dominio puro:

- `RenderWorker`;
- `JobQueue`;
- `ExecutorService`;
- `Task` de JavaFX;
- caché de preview concreta;
- persistencia JSON;
- llamadas a FFmpeg;
- construcción de ventanas;
- navegación entre pantallas.

Estos pertenecen a aplicación, infraestructura o presentación.

---

# 8. Lenguaje ubicuo sugerido
Para mantener consistencia, se sugiere usar este vocabulario como base:

- Project
- FractalFormula
- CameraState
- Keyframe
- Timeline
- Segment
- FrameDescriptor
- RenderProfile
- ColorProfile
- Palette
- Resolution
- FrameIndex
- TimePosition

Evitar nombres ambiguos como:
- `data`
- `thing`
- `object`
- `manager`
- `helper`
- `stuff`

---

# 9. Resumen conceptual
El dominio de Fractal Render Studio modela una secuencia fractal como una composición temporal de vistas y parámetros. La unidad central no es el píxel ni el thread, sino la descripción estructurada de una producción visual fractal: proyecto, cámara, keyframes, timeline, perfiles y frames.

El dominio debe permanecer puro, estable y expresivo. Todo lo relacionado con ejecución por lotes, UI y persistencia debe construirse alrededor de este núcleo, pero no mezclar