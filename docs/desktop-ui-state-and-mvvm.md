# UI de Escritorio, Estado y MVVM Pragmatico

## 1. Proposito

Este documento explica como esta construida la interfaz de escritorio desde una perspectiva de ingenieria de software, no solo de apariencia. El objetivo es mostrar como JavaFX, el estado observable y la coordinacion de casos de uso se integran en un cliente desktop mantenible.

## 2. Punto de entrada y composicion

El arranque real de la aplicacion esta en:

- `FractalStudioApplication`
- `ApplicationBootstrap`
- `UiCompositionRoot`

La responsabilidad esta dividida asi:

- `FractalStudioApplication`
  - crea la escena JavaFX
  - aplica estilos
  - carga el icono
  - muestra el `Stage`
- `ApplicationBootstrap`
  - construye el grafo de objetos
  - instancia facades, renderers, exportadores y pools
  - controla el shutdown
- `UiCompositionRoot`
  - ensambla la shell y las vistas principales

Esta organizacion evita una mala practica comun en JavaFX: meter composicion, negocio y wiring dentro del `Application`.

## 3. View y ViewModel

La UI sigue una variante pragmatica de MVVM:

- las vistas JavaFX viven en `presentation`
- el estado y comandos principales se concentran en `StudioShellViewModel`
- las vistas observan propiedades y listas observables

No es MVVM dogmatico, pero si una separacion util:

- la vista dibuja y enruta eventos visuales
- el view model decide que hacer con el proyecto, el preview y el render

## 4. StudioShellViewModel como orquestador de sesion

`StudioShellViewModel` es el centro de gravedad del cliente desktop. Coordina:

- proyecto actual
- estado de camara
- preview actual
- timeline visible
- puntos guardados
- cola de render
- estado del drawer inferior
- labels derivados del inspector
- metricas y mensajes

Aunque sigue siendo una clase densa, la arquitectura ha ido empujando responsabilidades hacia colaboradores como:

- `StudioPreviewCoordinator`
- `StudioStoragePaths`
- `StudioProjectPresentation`

Eso muestra una estrategia de refactor evolutiva: no romper la aplicacion por una gran reescritura, sino ir extrayendo subsistemas con valor real.

## 5. Propiedades observables

JavaFX trabaja bien cuando el estado visual se expresa como propiedades. El view model expone:

- `StringProperty`
- `ObjectProperty`
- `ObservableList`
- `SimpleBooleanProperty`

Ejemplos de uso:

- textos del inspector
- imagen del preview
- lista de jobs
- lista de items del timeline
- visibilidad del panel inferior

La ventaja academica y practica aqui es clara:

- desacopla lectura y escritura de estado
- facilita binding
- evita que las vistas recalculen demasiado por su cuenta

## 6. Navegacion de escritorio

La app no esta montada como una sola vista inmensa. Usa un esquema de navegacion interna:

- `StudioNavigator`
- `RouteSpec`
- `ViewRegistry`

Esto se acerca a una idea de router desktop. No es web, pero resuelve el mismo problema:

- que pantalla mostrar
- como registrar vistas
- como mantener una shell estable

## 7. Drawer inferior como decision de UX con impacto tecnico

El panel inferior no fue tratado como una simple caja mas. Se transformo en un drawer controlado por estado:

- visible u oculto
- con pestañas para puntos y render queue
- sin redimensionar el viewport principal

Esto importa porque la experiencia del fractal depende fuertemente del tamaño y estabilidad del area central. Si el timeline empuja el layout, el zoom y el preview se sienten peores.

## 8. Preview y refinamiento como comandos distintos

La UI tiene que comunicar bien dos modos distintos:

- preview automatico y economico
- refinamiento manual y mas costoso

El primero soporta interaccion. El segundo soporta inspeccion. Esta diferencia UX se apoya en logica real del view model y en politicas de calidad adaptativa.

## 9. Sidebar, inspector y timeline como vistas del mismo modelo

Un mismo estado del proyecto se proyecta de varias maneras:

- el sidebar muestra el arbol semantico del proyecto
- el inspector muestra propiedades editables o de solo lectura
- el timeline muestra puntos como secuencia visual

Este es un buen ejemplo de separacion entre modelo conceptual y representaciones visuales.

## 10. Sesion efimera y persistencia explicita

La UI no restaura automaticamente proyectos viejos al abrir. Esta decision afecta fuertemente la experiencia:

- la app inicia limpia
- el usuario abre o genera proyecto de manera explicita
- los residuos internos de sesion se eliminan al cerrar

Esto hace que el comportamiento sea mas predecible para distribucion y evaluacion academica.

## 11. Lecciones de ingenieria UI

Desde el punto de vista de aprendizaje, este modulo demuestra:

- como modelar un cliente JavaFX serio
- como evitar una UI acoplada directamente a persistencia y render
- como usar propiedades observables sin colapsar en logica dispersa
- como introducir navegacion interna, drawer y paneles especializados

## 12. Posibles extensiones futuras

- dividir mas `StudioShellViewModel`
- atajos de teclado configurables
- temas visuales intercambiables
- timeline con drag and drop temporal
- accesibilidad y lectura de contraste automatica
