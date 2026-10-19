# Post-contenido — Unidad 4: Patrones de Comportamiento en ComprasUDES

## Descripción
Repositorio del post-contenido de la Unidad 4 de Patrones de Diseño de Software. Un único proyecto
Spring Boot (`compras-comportamiento`) que resuelve necesidades reales del backend de **ComprasUDES**,
el sistema interno de solicitudes de compra corporativas.

- **Parte 1:** aprobación por niveles jerárquicos y ejecución reversible de solicitudes aprobadas.
- **Parte 2:** *(en construcción)* notificaciones ante cambios de estado y reglas de transición.

## Cómo ejecutar
```
$ mvn clean package
$ mvn spring-boot:run
$ mvn test
```

## Decisiones de diseño

### Necesidad 1 — Aprobación por niveles jerárquicos → **Chain of Responsibility**

**Síntoma de diseño.** Una solicitud debe recorrer una secuencia de decisores independientes
(Supervisor ≤ $2.000.000, Gerente ≤ $10.000.000, Director sin límite y, para compras INTERNACIONALES,
el Revisor de Cumplimiento Normativo antes del nivel por monto). Cada uno resuelve o delega. La secuencia
debe poder crecer, reducirse o reordenarse sin tocar `ControladorSolicitudes`, que solo conoce el contrato
`ServicioAprobacion`.

**Solución.**
- `NivelAprobacion` (manejador abstracto) guarda solo a su sucesor y define `manejar()`: si
  `puedeResolver()` decide (aprobar o rechazar) y registra su nombre en `nivelResolutor`; si no, delega.
- Eslabones concretos: `SupervisorArea`, `GerenteArea` (ambos heredan de `NivelConLimite`),
  `DirectorFinanciero` y `RevisorCumplimientoNormativo`.
- `CadenasAprobacion` es el único lugar donde se decide qué niveles existen y en qué orden.
  `NivelAprobacion.encadenar(List)` arma cualquier cadena.
- `ServicioAprobacionPorNiveles` implementa el contrato dado y solo hace `primerNivel.manejar(solicitud)`.
  No sabe cuántos niveles hay ni si la categoría requiere uno adicional.
- `ConfiguracionAprobacion` publica la cadena como bean de Spring. `ControladorSolicitudes` no se modificó.

**Regla del Revisor de Cumplimiento.** Si la categoría no es INTERNACIONAL, el revisor no interviene y
delega. Si lo es:
- si la solicitud incumple la normativa (solicitante sin correo institucional `@udes.edu.co`), la
  **rechaza**;
- si cumple y el monto está dentro de su autoridad (≤ $2.000.000), la **aprueba**;
- si cumple pero el monto la supera, la **remite** al nivel por monto (Gerente o Director).

Así se evalúa siempre *antes* del nivel por monto, y la prueba `S-003` (internacional, $1.000.000)
queda resuelta por el Revisor.

**Alternativa descartada: Command.** Command encapsula una *operación* como objeto para poder
ejecutarla, encolarla, deshacerla y guardarla en un historial. Aquí no hay nada que deshacer ni
historial que consultar: el problema es **a quién le corresponde decidir**. Con Command, cada nivel
sería una acción que alguien externo tendría que elegir e invocar. Ese invocador tendría que conocer
el orden y la regla de cada nivel ("si monto ≤ 2M ejecuta Supervisor, si no Gerente…"), que es justo
el `if/switch` de niveles que la restricción prohíbe. En Chain of Responsibility, la decisión de
"resolver o delegar" vive dentro de cada eslabón, y el emisor solo conoce el primero. Agregar el
Revisor fue crear una clase y añadirla a la lista en `CadenasAprobacion`, sin tocar los demás niveles
ni el controlador. Lo demuestra la prueba `reordenarOQuitarNivelesNoRequiereModificarLosNiveles`.

### Necesidad 2 — Ejecución reversible de solicitudes → **Command**

**Síntoma de diseño.** Sobre una solicitud APROBADA el equipo ejecuta dos operaciones discretas
(reservar presupuesto, generar orden de compra) que deben poder **deshacerse de forma independiente**.
El sistema además necesita un **historial ordenado** de todas las operaciones, no solo la última.

**Solución.**
- `OperacionSolicitud` (comando) con `ejecutar()` y `deshacer()`.
- `ReservarPresupuestoOperacion`: su receptor es `PresupuestoService`. `reservar` / `liberar`.
- `GenerarOrdenCompraOperacion`: su receptor es `OrdenCompraService`. `generar` / `cancelar`. Guarda
  el número de orden y el estado previo para que el undo deje la solicitud como estaba (`EJECUTADA` →
  `APROBADA`).
- `EjecutorOperaciones` (invocador) mantiene, por solicitud:
  - una **pila de operaciones activas**: `deshacerUltima(id)` revierte la más reciente, y
    `deshacer(id, Tipo.class)` revierte una en particular (por ejemplo, liberar el presupuesto sin
    cancelar la orden, o al revés);
  - un **historial** (`List<RegistroOperacion>`) con secuencia, operación, acción
    (EJECUTADA/DESHECHA) y momento. `historial(id)` devuelve todas las entradas en orden, y cada
    entrada conserva la referencia al comando para inspeccionarlo.
- `PresupuestoService` y `OrdenCompraService` no se modificaron. Como no tienen anotaciones,
  `ConfiguracionEjecucion` los registra como beans con `@Bean`.

**Alternativa descartada: Chain of Responsibility.** Aquí no llega una petición que deba encontrar
quién la resuelve. Un mismo actor, el equipo de Compras, decide **qué** operación ejecutar y
**cuándo** deshacerla. Con una cadena:
- la reserva y la orden serían eslabones que se pasan la solicitud, y el primero que "resuelve"
  corta el recorrido. Pero aquí se necesitan **las dos** operaciones, no la primera competente;
- la cadena no guarda lo que pasó, así que no hay forma natural de deshacer ni de consultar un
  historial;
- el estado necesario para revertir (número de orden, estado previo) no tiene dónde vivir.

Command convierte cada operación en un objeto con memoria propia. Eso es lo que permite apilarla,
revertirla por separado y conservarla en el historial. A la inversa, usar Command en la Necesidad 1
obligaría al invocador a conocer el orden de los niveles, como se explicó arriba.

## Herramientas utilizadas
- Java 17, Spring Boot 3.2, Apache Maven, JUnit 5
- VS Code, Git, GitHub
