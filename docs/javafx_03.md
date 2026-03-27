# JavaFX 03 — Casos de uso y flujos

## Propósito de este documento
Este documento define los **casos de uso principales** de Fractal Render Studio y los flujos de interacción entre usuario, presentación, aplicación, dominio e infraestructura.

Su objetivo es responder con claridad:

- qué acciones puede realizar el usuario;
- qué espera obtener de cada acción;
- qué capas participan;
- qué entradas y salidas existen;
- qué errores o condiciones anómalas deben contemplarse.

Este documento no describe clases concretas ni widgets específicos, sino comportamiento funcional del sistema.

---

# 1. Principios de diseño funcional

## 1.1 Enfoque general
Los casos de uso se organizan alrededor de tres ejes principales del producto:

1. **Exploración**
2. **Composición**
3. **Producción**

## 1.2 Regla importante
Toda acción relevante del usuario debe poder expresarse como un caso de uso claro, con:

- intención;
- entrada;
- procesamiento;
- salida;
- errores posibles.

## 1.3 Capas participantes
En general, el flujo esperado será:

```text
Usuario -> Presentation -> Application -> Domain / Infrastructure -> Presentation
```

---

# 2. Catálogo de casos de uso principales

## Núcleo inicial
1. Crear proyecto
2. Abrir proyecto
3. Guardar proyecto
4. Navegar fractal
5. Generar preview
6. Agregar keyframe
7. Editar keyframe
8. Eliminar keyframe
9. Reordenar o ajustar timeline
10. Construir secuencia de frames
11. Configurar render
12. Enviar render batch
13. Monitorear render batch
14. Cancelar render
15. Exportar resultados

---

# 3. Caso de uso: Crear proyecto

## Objetivo
Permitir al usuario iniciar un nuevo proyecto de producción fractal.

## Entrada
- nombre del proyecto;
- configuración inicial opcional;
- fórmula fractal inicial;
- perfil básico de render y color.

## Flujo principal
1. El usuario solicita crear un nuevo proyecto.
2. Presentation recopila datos mínimos.
3. Application crea un comando o request.
4. Domain construye el `Project` con estado inicial válido.
5. Infrastructure puede persistirlo si corresponde.
6. Presentation muestra el proyecto cargado.

## Salida
- proyecto inicializado y disponible en la UI.

## Errores posibles
- nombre inválido;
- conflicto de ubicación al guardar;
- perfil inicial inconsistente.

---

# 4. Caso de uso: Abrir proyecto

## Objetivo
Cargar un proyecto existente para continuar exploración, edición o render.

## Entrada
- identificador o ruta conceptual del proyecto.

## Flujo principal
1. El usuario solicita abrir proyecto.
2. Presentation delega a Application.
3. Application solicita a Infrastructure recuperar datos persistidos.
4. Infrastructure reconstruye el estado del proyecto.
5. Application valida consistencia básica.
6. Presentation refresca explorer, timeline, inspector y paneles relacionados.

## Salida
- proyecto cargado.

## Errores posibles
- archivo inexistente;
- formato inválido;
- incompatibilidad de versión;
- proyecto corrupto.

---

# 5. Caso de uso: Guardar proyecto

## Objetivo
Persistir el estado actual del proyecto.

## Entrada
- proyecto en memoria.

## Flujo principal
1. El usuario solicita guardar.
2. Presentation delega a Application.
3. Application prepara snapshot conceptual del proyecto.
4. Infrastructure lo serializa y persiste.
5. Presentation notifica éxito o fallo.

## Salida
- proyecto persistido.

## Errores posibles
- fallo de escritura;
- ruta inválida;
- acceso denegado.

---

# 6. Caso de uso: Navegar fractal

## Objetivo
Permitir explorar visualmente el fractal mediante paneo y zoom.

## Entrada
- interacción del usuario sobre el viewport;
- cámara actual;
- fórmula activa.

## Flujo principal
1. El usuario realiza paneo o zoom.
2. Presentation actualiza intención de navegación.
3. Application genera una solicitud de preview.
4. Domain ajusta o valida el nuevo `CameraState`.
5. Infrastructure genera preview.
6. Presentation actualiza el viewport.

## Salida
- vista actualizada del fractal.

## Errores posibles
- render preview interrumpido;
- parámetros fuera de rango;
- falta temporal de recursos.

---

# 7. Caso de uso: Generar preview

## Objetivo
Obtener una representación rápida del fractal para explorar o revisar una configuración.

## Entrada
- fórmula fractal;
- `CameraState`;
- configuración preview;
- perfil de color.

## Flujo principal
1. Presentation solicita preview.
2. Application construye request de preview.
3. Domain aporta la descripción conceptual del frame.
4. Infrastructure renderiza con estrategia de preview.
5. Application recibe resultado.
6. Presentation lo muestra.

## Salida
- imagen preview.

## Errores posibles
- timeout o cancelación del preview;
- parámetros inconsistentes;
- fallo de render puntual.

---

# 8. Caso de uso: Agregar keyframe

## Objetivo
Registrar el estado actual de cámara y parámetros como punto de control temporal.

## Entrada
- tiempo o posición deseada en timeline;
- estado actual de cámara;
- parámetros visuales activos.

## Flujo principal
1. Usuario pulsa “Agregar keyframe”.
2. Presentation toma el estado actual relevante.
3. Application envía la solicitud.
4. Domain crea un nuevo `Keyframe`.
5. Domain lo inserta en la `Timeline` respetando reglas temporales.
6. Presentation actualiza timeline e inspector.

## Salida
- keyframe agregado.

## Errores posibles
- posición temporal inválida;
- conflicto no resoluble de orden;
- falta de datos mínimos del estado actual.

---

# 9. Caso de uso: Editar keyframe

## Objetivo
Modificar un keyframe existente.

## Entrada
- identificador del keyframe;
- nuevos parámetros;
- nueva posición temporal si aplica.

## Flujo principal
1. Usuario selecciona un keyframe.
2. Presentation muestra sus propiedades.
3. Usuario modifica valores.
4. Application envía la modificación.
5. Domain valida y actualiza el keyframe.
6. Presentation refresca timeline, inspector y preview si hace falta.

## Salida
- keyframe actualizado.

## Errores posibles
- keyframe inexistente;
- parámetros inválidos;
- colisión temporal incompatible.

---

# 10. Caso de uso: Eliminar keyframe

## Objetivo
Remover un keyframe de la timeline.

## Entrada
- identificador del keyframe.

## Flujo principal
1. Usuario solicita eliminar.
2. Application procesa la orden.
3. Domain elimina el keyframe.
4. Presentation refresca la timeline.
5. Si procede, se recalcula preview o estado de selección.

## Salida
- timeline actualizada.

## Errores posibles
- keyframe inexistente;
- la eliminación dejaría un estado no permitido según reglas del producto.

---

# 11. Caso de uso: Ajustar timeline

## Objetivo
Modificar la estructura temporal de la secuencia.

## Posibles acciones
- mover keyframes;
- cambiar duración total;
- cambiar duración entre segmentos;
- cambiar interpolación.

## Flujo principal
1. Usuario modifica timeline.
2. Presentation produce una acción concreta.
3. Application delega al dominio.
4. Domain recalcula orden y relaciones temporales.
5. Presentation refresca timeline y estados derivados.

## Salida
- timeline reconfigurada.

## Errores posibles
- orden temporal inválido;
- duración inconsistente;
- segmento no interpolable.

---

# 12. Caso de uso: Construir secuencia de frames

## Objetivo
Traducir proyecto + timeline en una secuencia ordenada de frames conceptuales renderizables.

## Entrada
- proyecto;
- timeline;
- fps;
- duración;
- perfiles activos.

## Flujo principal
1. Usuario solicita preparar render o previsualización temporal.
2. Application consulta la timeline y perfiles.
3. Domain resuelve el estado de cada instante discreto.
4. Domain o Application construyen `FrameDescriptor` ordenados.
5. El resultado se entrega al subsistema que lo usará para preview o render final.

## Salida
- colección de `FrameDescriptor`.

## Errores posibles
- timeline insuficiente;
- fps inválido;
- perfiles incompletos;
- keyframes incompatibles.

---

# 13. Caso de uso: Configurar render

## Objetivo
Definir cómo debe producirse una salida final.

## Entrada
- resolución;
- calidad;
- iteraciones máximas;
- perfil de color;
- rango de frames;
- directorio de salida;
- opciones de exportación.

## Flujo principal
1. Usuario abre configuración de render.
2. Presentation recopila parámetros.
3. Application valida consistencia básica.
4. Se construye un `RenderRequest`.
5. Queda listo para ser ejecutado o editado.

## Salida
- solicitud de render válida.

## Errores posibles
- resolución inválida;
- iteraciones fuera de rango;
- salida no disponible;
- request incompleta.

---

# 14. Caso de uso: Enviar render batch

## Objetivo
Mandar una secuencia de render al subsistema de procesamiento por lotes.

## Entrada
- `RenderRequest` válido.

## Flujo principal
1. Usuario pulsa “Render”.
2. Presentation delega a Application.
3. Application construye o finaliza el plan de render.
4. Domain aporta los `FrameDescriptor` necesarios.
5. Application entrega el plan a Infrastructure.
6. Infrastructure lo transforma en jobs y tareas ejecutables.
7. Se registra el trabajo en la cola.
8. Presentation actualiza la vista de render queue.

## Salida
- `RenderJob` creado y encolado.

## Errores posibles
- request inválida;
- fallo al crear artefactos temporales;
- cola no disponible;
- falta de permisos de salida.

---

# 15. Caso de uso: Monitorear render batch

## Objetivo
Permitir al usuario seguir el progreso y estado de los trabajos de render.

## Entrada
- trabajos activos o históricos.

## Flujo principal
1. Infrastructure reporta progreso y estados.
2. Application adapta la información relevante.
3. Presentation actualiza la cola de render, métricas y logs.
4. Usuario consulta estado, tiempo, errores o artefactos generados.

## Salida
- visualización consistente del estado del render.

## Estados típicos
- pendiente;
- preparando;
- renderizando;
- pausado;
- completado;
- cancelado;
- fallido.

---

# 16. Caso de uso: Cancelar render

## Objetivo
Detener un trabajo de render en curso o pendiente.

## Entrada
- identificador del job.

## Flujo principal
1. Usuario solicita cancelación.
2. Presentation delega a Application.
3. Application solicita cancelación a Infrastructure.
4. Infrastructure marca el job y sus tareas como cancelables/canceladas.
5. Presentation refleja el nuevo estado.

## Salida
- render detenido o marcado para detenerse.

## Errores posibles
- job inexistente;
- job ya finalizado;
- cancelación parcial con artefactos ya escritos.

---

# 17. Caso de uso: Exportar resultados

## Objetivo
Obtener artefactos finales del proyecto renderizado.

## Posibles salidas
- imágenes PNG;
- secuencia de frames;
- video ensamblado;
- metadatos de ejecución.

## Flujo principal
1. Usuario solicita exportación.
2. Application valida que existan resultados o parámetros adecuados.
3. Infrastructure ejecuta el guardado o ensamblado.
4. Presentation notifica finalización y muestra acceso al resultado.

## Salida
- artefactos exportados.

## Errores posibles
- faltan frames;
- proceso de ensamblado falló;
- ruta de salida inválida;
- archivos bloqueados.

---

# 18. Casos de uso secundarios recomendados

## 18.1 Cambiar fórmula fractal
Permite alternar entre familias de fractales sin romper proyecto ni UI.

## 18.2 Cambiar colorización
Permite experimentar con distintos perfiles cromáticos.

## 18.3 Duplicar keyframe
Acelera composición.

## 18.4 Re-render de preview
Permite refrescar el viewport tras cambios parciales.

## 18.5 Consultar métricas
Permite ver tiempos por frame, progreso y rendimiento.

---

# 19. Flujos de alto nivel del producto

## Flujo A — Exploración simple
1. Abrir o crear proyecto.
2. Navegar fractal.
3. Generar previews sucesivos.
4. Ajustar cámara y color.

## Flujo B — Composición de secuencia
1. Navegar.
2. Agregar keyframe inicial.
3. Moverse por el fractal.
4. Agregar nuevos keyframes.
5. Ajustar timeline e interpolación.
6. Revisar preview general.

## Flujo C — Producción final
1. Configurar render.
2. Construir secuencia de frames.
3. Enviar render batch.
4. Monitorear progreso.
5. Exportar resultados.

---

# 20. Casos de error globales

## Errores de entrada
- valores fuera de rango;
- timeline inconsistente;
- configuración incompleta.

## Errores de dominio
- proyecto no renderizable;
- keyframes incompatibles;
- falta de perfiles efectivos.

## Errores de infraestructura
- disco lleno;
- fallo de IO;
- proceso externo fallido;
- job cancelado;
- corrupción o pérdida de archivos temporales.

## Errores de presentación
- selección inválida;
- estados desincronizados de paneles;
- intento de operación sin contexto cargado.

---

# 21. Priorización para versión 1

## Prioridad alta
- crear proyecto;
- abrir/guardar proyecto;
- navegar fractal;
- generar preview;
- agregar/editar/eliminar keyframes;
- ajustar timeline de forma básica;
- construir secuencia de frames;
- configurar render;
- enviar render batch;
- monitorear progreso;
- exportar PNG y secuencia.

## Prioridad media
- cancelar render;
- métricas visibles;
- colorización configurable;
- duplicar keyframe.

## Prioridad baja
- histórico avanzado;
- presets complejos;
- múltiples fórmulas desde el día uno;
- ensamblado de video totalmente integrado en primera iteración.

---

# 22. Resumen
Los casos de uso de Fractal Render Studio se organizan alrededor de tres ejes: exploración, composición y producción. El usuario no interactúa con cálculos aislados, sino con un flujo de trabajo coherente: navega, marca keyframes, construye una trayectoria, configura un render y produce una secuencia final.

La definición explícita de estos casos de uso servirá como guía para diseñar:

- la API de aplicación;
- los comandos de UI;
- los contratos de infraestructura;
- la organización de pantallas y paneles.