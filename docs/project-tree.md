# Arbol del Proyecto

```text
.
|-- docs/
|   |-- documentation-map.md
|   |-- computing-concepts-glossary.md
|   |-- software-product-computing-foundations.md
|   |-- code-guided-theory-index.md
|   |-- software-engineering-and-design-patterns.md
|   |-- data-structures-algorithms-and-complexity.md
|   |-- computation-theory-and-fractal-science.md
|   |-- numerical-precision-and-rendering-model.md
|   |-- desktop-ui-state-and-mvvm.md
|   |-- concurrency-cancellation-and-parallel-rendering.md
|   |-- agent-based-rendering-model.md
|   |-- persistence-json-and-project-lifecycle.md
|   |-- testing-quality-and-verification.md
|   |-- video-pipeline-and-media-encoding.md
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
|   |       |-- app.properties
|   |       `-- assets/
|   |           |-- icons/
|   |           |-- images/
|   |           `-- styles/
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
- documentacion dividida entre trazabilidad, arquitectura y fundamentos tecnico-cientificos
- `storage/` reservado para artefactos runtime locales
- `target/` sigue siendo generado y no forma parte del arbol fuente
