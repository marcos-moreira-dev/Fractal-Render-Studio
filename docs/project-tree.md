# Arbol del Proyecto

```text
.
|-- docs/
|   |-- implementation-architecture.md
|   |-- javafx_00.md ... javafx_07.md
|   `-- project-tree.md
|-- src/
|   |-- main/
|   |   |-- java/com/marcos/fractalstudio/
|   |   |   |-- application/
|   |   |   |   |-- dto/
|   |   |   |   |-- preview/
|   |   |   |   |-- project/
|   |   |   |   |-- render/
|   |   |   |   |-- renderhistory/
|   |   |   |   `-- timeline/
|   |   |   |-- domain/
|   |   |   |   |-- camera/
|   |   |   |   |-- color/
|   |   |   |   |-- fractal/
|   |   |   |   |-- project/
|   |   |   |   |-- render/
|   |   |   |   |-- timeline/
|   |   |   |   `-- validation/
|   |   |   |-- infrastructure/
|   |   |   |   |-- batching/
|   |   |   |   |-- export/
|   |   |   |   |-- metrics/
|   |   |   |   |-- persistence/
|   |   |   |   `-- rendering/
|   |   |   `-- presentation/
|   |   |       |-- app/
|   |   |       |-- common/
|   |   |       |-- dialogs/
|   |   |       |-- explorer/
|   |   |       |-- inspector/
|   |   |       |-- metrics/
|   |   |       |-- navigation/
|   |   |       |-- renderqueue/
|   |   |       |-- shell/
|   |   |       `-- timeline/
|   |   `-- resources/
|   |       `-- app.properties
|   `-- test/
|-- storage/
|   |-- exports/
|   |-- projects/
|   `-- renders/
|-- pom.xml
|-- mvnw
`-- mvnw.cmd
```

## Criterios del arbol

- paquetes con nombres semanticos y responsabilidad concreta
- sin carpetas comodin tipo `utils`, `misc` o `helpers`
- documentacion cerca del proyecto
- `storage/` reservado para artefactos runtime locales
- `target/` sigue siendo generado y no forma parte del arbol fuente
