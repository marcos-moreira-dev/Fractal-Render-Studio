# Release Checklist

## Objetivo

Cerrar `Fractal Render Studio` como MVP desktop estable, verificable y empaquetable.

## Antes de entregar

- ejecutar `.\mvnw.cmd -q compile`
- ejecutar `.\mvnw.cmd -q test`
- abrir la app y validar:
  - arranque sin temblor visible
  - zoom alrededor del cursor
  - bloqueo de zoom extremo con marcador rojo
  - inspector editable
  - bookmarks con miniatura y menu contextual
  - timeline con miniaturas y menu contextual
  - drawer de timeline/render queue
  - render queue y exportacion ZIP

## Empaquetado Windows

- usar JDK 21 con `jpackage`
- ejecutar:

```powershell
.\scripts\package-windows.ps1
```

- salida esperada:
  - `target/jpackage/FractalRenderStudio/`

## Limitaciones conocidas del MVP

- el deep zoom tiene limite practico por formula y precision numerica
- la exportacion actual se centra en frames PNG y ZIP, no en video ensamblado
- el timeline no tiene todavia scrubber ni reordenamiento temporal por drag and drop
- la validacion de UX final sigue dependiendo de prueba manual prolongada en GUI

## Cierre tecnico deseable

- completar JavaDoc en superficies publicas que queden sin documentar
- mantener `StudioShellViewModel` bajo vigilancia de refactor para no volver a concentrar demasiada logica
- congelar alcance antes de meter nuevas formulas o nuevas vistas
