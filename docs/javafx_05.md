# JavaFX 05 — UI y navegación

## Propósito de este documento
Este documento define la estructura general de la interfaz de usuario de **Fractal Render Studio** en JavaFX, así como la navegación entre pantallas, paneles y diálogos.

Su objetivo es dejar claro:

- cómo se distribuyen las áreas funcionales de la app;
- qué pantallas o vistas existen;
- qué rol tiene cada panel;
- cómo se coordina la UI sin acoplarlo todo;
- cómo encajan `Navigator`, `ViewModel` y `Mediator` dentro de la capa `presentation`.

Este documento no entra en detalles finos de CSS, FXML o layout exacto píxel por píxel, pero sí fija la forma conceptual de la interfaz.

---

# 1. Principios generales de UI

## 1.1 Enfoque
La app debe sentirse como una **herramienta de escritorio técnica y visual**, no como un formulario empresarial ni como una demo académica improvisada.

## 1.2 Sensación buscada
La interfaz debe transmitir:
- control;
- claridad;
- producción visual;
- observabilidad del procesamiento;
- composición por etapas.

## 1.3 Regla de diseño
La UI debe organizarse alrededor de los tres ejes funcionales del producto:

1. **Exploración**
2. **Composición**
3. **Producción**

---

# 2. Estructura general de la aplicación

## Ventana principal
La aplicación debe tener una **shell principal** que contenga las regiones clave del estudio.

## Distribución conceptual recomendada

### Zona superior
Barra principal de acciones y contexto del proyecto.

### Zona izquierda
Explorador de proyecto, presets, recursos o navegación secundaria.

### Zona central
Viewport principal del fractal.

### Zona derecha
Inspector de propiedades y configuración contextual.

### Zona inferior
Timeline y/o paneles de producción según modo activo.

### Zona secundaria inferior o pestañas inferiores
Render queue, logs, métricas y eventos.

---

# 3. Pantallas o vistas principales

## 3.1 `StudioShell`
Es la vista principal de la aplicación.

### Responsabilidad
Actuar como contenedor general de la experiencia.

### Contiene
- menú o toolbar principal;
- regiones del layout;
- acceso a explorer, viewport, inspector, timeline y render queue.

### Observación
No debe contener lógica de render, ni lógica matemática, ni reglas del dominio.

---

## 3.2 `FractalExplorerView`
Vista principal para navegar visualmente el fractal.

### Responsabilidad
Permitir paneo, zoom, visualización de preview y captura del estado actual.

### Posibles elementos
- canvas o image area principal;
- overlay de información;
- controles rápidos de zoom;
- estado actual de cámara;
- acciones de preview.

### Interacciones principales
- paneo;
- zoom;
- centrar vista;
- agregar keyframe desde estado actual.

---

## 3.3 `TimelineView`
Vista para editar la trayectoria temporal del proyecto.

### Responsabilidad
Mostrar y permitir editar los keyframes y segmentos temporales.

### Elementos posibles
- carril de keyframes;
- marcadores temporales;
- scrubber;
- duración total;
- controles para mover o eliminar keyframes.

### Interacciones principales
- seleccionar keyframe;
- mover keyframe;
- cambiar duración;
- editar transición;
- duplicar o eliminar.

---

## 3.4 `InspectorView`
Panel contextual de propiedades.

### Responsabilidad
Mostrar y editar la configuración del objeto o contexto seleccionado.

### Modos típicos
- propiedades del proyecto;
- propiedades de cámara actual;
- propiedades del keyframe seleccionado;
- configuración de color;
- configuración de render.

### Regla importante
El inspector debe reaccionar al contexto actual, no ser un panel estático desconectado.

---

## 3.5 `RenderQueueView`
Vista dedicada a trabajos de render y producción.

### Responsabilidad
Mostrar el estado de los jobs de render.

### Información útil
- id o nombre del job;
- estado;
- progreso;
- rango de frames;
- tiempo transcurrido;
- errores;
- acciones de cancelar o revisar salida.

### Sensación buscada
Debe sentirse como una cola de producción seria, no como una lista improvisada.

---

## 3.6 `MetricsView`
Vista para métricas técnicas y observabilidad.

### Responsabilidad
Permitir revisar información de rendimiento de manera legible.

### Ejemplos de información
- tiempo por frame;
- tiempo total;
- cantidad de frames completados;
- throughput aproximado;
- workers activos;
- errores recientes.

---

## 3.7 `ProjectExplorerView`
Panel lateral para estructura del proyecto.

### Responsabilidad
Permitir navegar por elementos lógicos del proyecto.

### Contenido posible
- proyecto actual;
- timeline;
- fórmulas;
- perfiles;
- presets;
- renders históricos;
- configuraciones guardadas.

---

# 4. Layout principal recomendado

## Opción sugerida
Un `BorderPane` o composición equivalente con divisiones internas.

### Estructura conceptual
- `top`: toolbar y menú
- `left`: project explorer
- `center`: fractal explorer
- `right`: inspector
- `bottom`: timeline + pestañas técnicas

## Nota
No se obliga a una implementación exacta, pero sí a respetar la idea de separación visual por responsabilidades.

---

# 5. Toolbar principal

## Objetivo
Agrupar acciones globales y rápidas del proyecto.

## Acciones sugeridas
- nuevo proyecto;
- abrir;
- guardar;
- agregar keyframe;
- generar preview;
- render;
- exportar;
- abrir cola de render;
- abrir métricas.

## Regla
La toolbar debe contener acciones de alto nivel, no controles hiper específicos del dominio fino.

---

# 6. Navegación

## Principio
La navegación debe estar centralizada en un componente dedicado de la capa `presentation`.

## Componente recomendado
`Navigator`

### Responsabilidades del `Navigator`
- cambiar entre vistas principales;
- abrir diálogos modales;
- mostrar pantallas secundarias;
- manejar rutas internas del estudio.

### No debe hacer
- lógica de negocio;
- render;
- acceso a infraestructura;
- decisiones del dominio.

---

# 7. Rutas sugeridas

## Rutas principales
- `STUDIO`
- `PROJECT_SETTINGS`
- `EXPORT_DIALOG`
- `RENDER_DETAILS`
- `METRICS`
- `ABOUT` si algún día se añade

## Idea práctica
Aunque gran parte de la app viva dentro del shell principal, conviene pensar en rutas explícitas para evitar navegación improvisada entre componentes.

---

# 8. Mediación entre paneles

## Problema
El viewport, la timeline, el inspector y la cola pueden quedar muy acoplados si se comunican directamente entre sí.

## Solución recomendada
Usar un `Mediator` o coordinador de pantalla para sincronizar eventos de UI.

## Ejemplos
- seleccionar un keyframe en la timeline actualiza el inspector;
- cambiar de keyframe actualiza el viewport;
- finalizar un render actualiza la cola y el panel de métricas;
- cambiar un perfil de color fuerza una actualización de preview.

## Beneficio
Se reduce el acoplamiento directo entre vistas y controladores.

---

# 9. Modelo de presentación recomendado

## Enfoque sugerido
Usar una variante simple de:

> **View + ViewModel**

## Responsabilidades

### `View`
- estructura visual;
- binding de eventos;
- presentación de datos.

### `ViewModel`
- estado observable;
- comandos de UI;
- transformación entre respuestas de aplicación y propiedades visuales.

## Regla
El `ViewModel` no debe duplicar el dominio completo ni convertirse en un servicio de infraestructura disfrazado.

---

# 10. ViewModels sugeridos

## `StudioShellViewModel`
Gestiona el contexto global de la shell.

## `FractalExplorerViewModel`
Gestiona estado del viewport, preview visible y navegación actual.

## `TimelineViewModel`
Gestiona keyframes, selección temporal y scrubber.

## `InspectorViewModel`
Gestiona el panel de propiedades del contexto seleccionado.

## `RenderQueueViewModel`
Gestiona estado observable de la cola de trabajos.

## `MetricsViewModel`
Gestiona información de rendimiento y observabilidad.

---

# 11. Coordinación entre UI y aplicación

## Regla principal
La UI no debe llamar directamente a la infraestructura de render o persistencia.

## Flujo correcto
```text
View -> ViewModel -> Application Use Case
```

## Ejemplo
- la vista pulsa “Render”;
- el `ViewModel` construye la intención;
- la capa `application` recibe el caso de uso;
- la infraestructura ejecuta;
- el resultado vuelve a UI mediante estados y actualizaciones.

---

# 12. Estado visual clave

La UI debe exponer y mantener de forma clara al menos estos estados:

## Proyecto actual
- nombre;
- modificado/no modificado;
- fórmula activa.

## Cámara actual
- centro;
- zoom;
- estado de preview.

## Timeline actual
- keyframes;
- selección actual;
- duración.

## Render actual o recientes
- jobs;
- progreso;
- errores;
- resultados.

## Métricas
- rendimiento reciente;
- tiempo estimado o consumido;
- actividad del pipeline.

---

# 13. Controles JavaFX razonables para este proyecto

## Muy útiles
- `BorderPane`
- `SplitPane`
- `TabPane`
- `TreeView`
- `ListView`
- `TableView`
- `Accordion`
- `ToolBar`
- `ScrollPane`
- `Slider`
- `ColorPicker`
- `Canvas`
- `ImageView`

## Posibles según necesidad
- `Dialog`
- `Popover` o componentes similares si se usan librerías adicionales;
- `SubScene` para experimentos visuales avanzados futuros.

## Regla
No usar controles complejos solo por exhibición. Deben aportar claridad a la herramienta.

---

# 14. Viewport fractal

## Objetivo
Ser el corazón visual de la app.

## Debe permitir
- mostrar preview actual;
- paneo;
- zoom;
- overlays discretos;
- selección contextual si se requiere;
- captura del estado actual para keyframe.

## Posibles tecnologías internas
- `Canvas` para dibujo controlado;
- `ImageView` para mostrar imágenes renderizadas;
- combinación de ambos.

## Regla
La implementación exacta puede decidirse después, pero la UI debe tratar el viewport como un componente especializado y central.

---

# 15. Timeline visual

## Objetivo
Hacer visible la trayectoria temporal y facilitar composición.

## Debe mostrar
- keyframes;
- duración entre segmentos;
- posición actual;
- selección;
- quizá tipos de interpolación en el futuro.

## Interacción mínima esperada
- seleccionar;
- arrastrar;
- borrar;
- añadir desde estado actual.

---

# 16. Render queue visual

## Objetivo
Mostrar el subsistema batch como parte importante del producto.

## Debe transmitir
- que hay trabajos reales ocurriendo;
- qué frame o rango se está produciendo;
- cuánto falta;
- si falló algo;
- si se puede cancelar.

## Posibles formatos visuales
- tabla;
- lista con tarjetas;
- lista + panel de detalle.

---

# 17. Logs y métricas en la UI

## Objetivo
Aportar observabilidad sin abrumar.

## Recomendación
Separar en pestañas o paneles secundarios:
- `Render Queue`
- `Metrics`
- `Logs`

## Regla
No saturar la pantalla principal con texto técnico si no es necesario, pero sí dar acceso fácil a esa información.

---

# 18. Estilo visual recomendado

## Estética general
La app debe verse como un **estudio técnico de producción visual**.

## Características deseables
- tema oscuro o neutro;
- contraste razonable;
- paneles bien delimitados;
- tipografía legible;
- acentos de color sobrios;
- sensación de herramienta profesional.

## Evitar
- colores chillones sin función;
- sobrecarga visual;
- paneles sin jerarquía;
- estilo de formulario administrativo clásico.

---

# 19. Estados vacíos y feedback

## Requisito
La UI debe saber qué mostrar cuando aún no hay datos suficientes.

## Ejemplos
- proyecto vacío;
- timeline sin keyframes;
- cola sin renders;
- métricas aún no disponibles;
- exportación sin resultados previos.

## Beneficio
Evita sensación de aplicación rota o incompleta.

---

# 20. Diálogos y ventanas secundarias

## Casos razonables
- exportación;
- configuración de proyecto;
- detalles de un render;
- confirmaciones de borrado;
- errores técnicos resumidos.

## Regla
No convertir toda la app en una colección de ventanas sueltas. La shell principal debe seguir siendo el centro.

---

# 21. Responsabilidades explícitas en `presentation`

## `Navigator`
Controla navegación entre vistas y modales.

## `ViewLoader`
Carga vistas y las conecta con sus dependencias visuales.

## `Mediator`
Coordina paneles dentro de una misma pantalla compleja.

## `ViewModel`
Mantiene estado observable y comandos de UI.

## `View`
Representa visualmente la información y recibe interacción.

---

# 22. Qué NO debe pasar en la UI

- no ejecutar render pesado en el hilo gráfico;
- no hablar directamente con `ExecutorService`;
- no crear procesos externos;
- no serializar proyectos desde controladores;
- no meter reglas matemáticas del fractal en la vista;
- no hacer que cada panel navegue por su cuenta sin coordinación.

---

# 23. Prioridades de UI para versión 1

## Alta prioridad
- shell principal;
- viewport fractal;
- timeline básica;
- inspector contextual;
- render queue;
- toolbar principal;
- navegación mínima coherente.

## Prioridad media
- métricas visibles;
- logs básicos;
- diálogos de exportación y configuración más pulidos.

## Prioridad baja
- animaciones cosméticas sofisticadas;
- temas múltiples;
- layouts alternativos configurables;
- docking avanzado tipo IDE.

---

# 24. Resumen
La UI de Fractal Render Studio debe organizarse como un estudio de trabajo visual y técnico: un viewport central para explorar el fractal, una timeline para componer trayectorias, un inspector para editar propiedades y una render queue para seguir la producción por lotes.

La navegación debe centralizarse con `Navigator`, la coordinación entre paneles debe manejarse con `Mediator` cuando haga falta, y la lógica visual debe apoyarse en `ViewModel` para evitar que JavaFX se convierta en el centro caótico del sistema.