# Empaquetado, Instalacion y Entrega en Windows

## Proposito

Este documento explica como un producto JavaFX pasa de ser un repositorio de desarrollo a convertirse en una aplicacion instalable en Windows.

No es solo una guia de comandos. Tambien describe los conceptos de ingenieria implicados en la entrega de software de escritorio.

## 1. Build, app-image, instalador y release

Hay varios niveles de salida:

- build: compilar el codigo
- app-image: directorio autocontenido con ejecutable y runtime
- instalador: artefacto distribuible para instalar la app
- release: paquete formal publicado para usuarios

Confundir estos conceptos lleva a errores comunes:

- pensar que un `jar` ya es un producto instalable
- creer que "compila" significa "se puede distribuir"
- ignorar runtime, iconos, acceso directo o dependencias nativas

## 2. Que resuelve jpackage

`jpackage` es la herramienta del ecosistema Java para crear:

- app-image
- instaladores nativos, como MSI o EXE segun plataforma

Su funcion es empaquetar:

- la aplicacion
- el runtime de Java necesario
- configuracion del launcher
- icono y metadatos de la app

Eso evita depender de que el usuario tenga el JDK instalado manualmente.

## 3. Runtime embebido

Un producto de escritorio profesional no deberia exigir al usuario final:

- descargar Java por separado
- configurar variables de entorno
- conocer JavaFX o Maven

Por eso el empaquetado incluye el runtime necesario. Esa es una decision de experiencia de usuario y de soporte tecnico.

## 4. JavaFX y launcher

Una app JavaFX no siempre puede lanzarse bien con un ejecutable generico si faltan:

- modulos de JavaFX
- `module-path`
- opciones de lanzamiento adecuadas

Por eso el launcher del instalador necesita configuracion explicita. No es un detalle menor: si falla, la app puede estar "instalada" pero no abrir.

## 5. Iconos y recursos de identidad visual

En un producto distribuible, el icono no es decoracion. Cumple funciones practicas:

- reconocimiento en explorador de archivos
- acceso directo en menu inicio
- identidad en barra de tareas
- consistencia entre instalador y ejecutable

Por eso el proyecto organiza recursos como:

- `assets/icons`
- `assets/images`
- `assets/styles`

La idea es separar identidad del producto de cualquier referencia personal del desarrollador.

## 6. EXE frente a MSI

Ambos son formatos comunes en Windows, pero no son lo mismo.

### EXE

- puede funcionar como instalador o launcher personalizado
- suele ser flexible
- a veces es mas simple para distribucion rapida

### MSI

- es mas estandar para instalacion corporativa
- se integra mejor con flujos de Windows
- facilita desinstalacion y administracion

Para un cierre mas profesional, MSI suele ser preferible.

## 7. Dependencias nativas y tamano del instalador

El tamano del instalador no depende solo del codigo Java. Tambien depende de:

- runtime embebido
- librerias nativas
- multimedia y codificadores
- assets

En este producto, la exportacion MP4 introduce dependencias de video que pesan bastante. Optimizar el instalador significa:

- incluir lo necesario
- excluir lo no usado
- no romper compatibilidad

Es una optimizacion de ingenieria, no solo de espacio.

## 8. Entrega de artefactos

Un software entregable necesita una politica clara de artefactos.

En este proyecto hay dos tipos:

- artefactos de distribucion del producto
- artefactos generados por el usuario

### Distribucion

- MSI
- app-image portable

### Generados por el usuario

- `render.mp4`
- `frames/`
- `project.fractalstudio.json`

Separar ambos niveles evita mezclar build interno con trabajo creativo del usuario.

## 9. Carpeta de render como unidad portable de trabajo

Cuando el usuario lanza un render, el sistema crea una carpeta de trabajo dentro del directorio base elegido.

Eso tiene varias ventajas:

- organiza salida de video
- encapsula frames temporales si existen
- guarda el JSON unico del proyecto asociado al render
- facilita mover, respaldar o compartir resultados

Conceptualmente, esa carpeta funciona como un paquete autocontenido de produccion.

## 10. Limpieza de sesion frente a persistencia del usuario

El producto distingue entre:

- sesion interna efimera
- entregables persistentes

Esto es importante porque:

- al cerrar la app no deberia reaparecer basura de trabajo interno
- el usuario si debe conservar sus renders y su proyecto JSON

Es una separacion entre estado operacional interno y estado externo significativo.

## 11. Instalador como wizard

Un wizard es una interfaz guiada paso a paso que conduce al usuario por una tarea con varias decisiones.

En este proyecto, el ejemplo natural de wizard es el instalador:

- el usuario acepta destino
- confirma opciones
- instala
- obtiene accesos directos o binarios listos

La idea de wizard es importante en desarrollo de software porque muestra una interfaz orientada a secuencia de decisiones, no a exploracion libre.

## 12. Release engineering

Release engineering es la disciplina que transforma un sistema en algo publicable y repetible.

Incluye:

- build reproducible
- versionado
- empaquetado
- verificacion
- distribucion

No es solo "subir archivos". Es garantizar que otra persona puede instalar y ejecutar el producto con un nivel razonable de confianza.

## 13. Leccion general

Empaquetar una app de escritorio no es el final cosmetico del proyecto. Es una parte esencial del producto.

Un software puede estar muy bien disenado por dentro y aun asi fracasar en la entrega si:

- no instala bien
- no abre
- no tiene icono
- no trae runtime
- no organiza su salida

Por eso la instalacion y el empaquetado forman parte de la ingenieria del software, no de la "presentacion final".
