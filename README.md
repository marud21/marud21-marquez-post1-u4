# Post-contenido — Unidad 4: Patrones de Comportamiento en ComprasUDES

## Descripción
Repositorio del post-contenido de la Unidad 4 de Patrones de Diseño de Software. Un único proyecto
Spring Boot (`compras-comportamiento`) que resuelve cuatro necesidades reales del backend de
**ComprasUDES**, el sistema interno de solicitudes de compra corporativas:

| # | Necesidad | Patrón aplicado | Alternativa descartada |
|---|-----------|-----------------|------------------------|
| 1 | Aprobación por niveles jerárquicos | **Chain of Responsibility** | Command |
| 2 | Ejecución reversible de solicitudes aprobadas | **Command** | Chain of Responsibility |
| 3 | Notificaciones ante cambio de estado | **Observer** | State |
| 4 | Reglas de transición según el estado actual | **State** | Strategy |

El código dado por el enunciado (`Solicitud`, `ServicioAprobacion`, `ResultadoAprobacion`,
`ControladorSolicitudes`, `PresupuestoService`, `OrdenCompraService`, `ClientesNotificacion`) se
conserva sin modificaciones.

## Cómo ejecutar
```
$ mvn clean package
$ mvn spring-boot:run
$ mvn test
```

Con la aplicación en ejecución se puede probar la Necesidad 1 (y ver en consola las reacciones de la
Necesidad 3):
```
$ curl -X POST http://localhost:8080/api/solicitudes/evaluar \
       -H "Content-Type: application/json" \
       -d '{"id":"S-003","solicitanteEmail":"gerencia@udes.edu.co","monto":1000000,"categoria":"INTERNACIONAL","centroCosto":"CC-300"}'
```
Responde `200` si la solicitud se aprueba y `422` si se rechaza.

## Estructura del proyecto
```
src/main/java/com/universidad/compras/
├── ComprasApp.java
├── modelo/
│   └── Solicitud.java                       (DADO)
├── aprobacion/                              Necesidad 1 — Chain of Responsibility
│   ├── ServicioAprobacion.java              (DADO)
│   ├── ResultadoAprobacion.java             (DADO)
│   ├── ControladorSolicitudes.java          (DADO)
│   ├── NivelAprobacion.java                 manejador abstracto (resuelve o delega)
│   ├── NivelConLimite.java                  manejador con tope de monto
│   ├── SupervisorArea.java                  ≤ $2.000.000
│   ├── GerenteArea.java                     ≤ $10.000.000
│   ├── DirectorFinanciero.java              sin límite
│   ├── RevisorCumplimientoNormativo.java    nivel adicional para INTERNACIONAL
│   ├── CadenasAprobacion.java               único lugar que define qué niveles y en qué orden
│   ├── ServicioAprobacionPorNiveles.java    implementa el contrato dado
│   └── ConfiguracionAprobacion.java
├── ejecucion/                               Necesidad 2 — Command
│   ├── PresupuestoService.java              (DADO) receptor
│   ├── OrdenCompraService.java              (DADO) receptor
│   ├── OperacionSolicitud.java              comando: ejecutar() / deshacer()
│   ├── ReservarPresupuestoOperacion.java    comando concreto
│   ├── GenerarOrdenCompraOperacion.java     comando concreto
│   ├── EjecutorOperaciones.java             invocador: pila de undo + historial
│   ├── RegistroOperacion.java               entrada del historial
│   ├── FabricaOperacionesSolicitud.java
│   └── ConfiguracionEjecucion.java
├── notificacion/                            Necesidad 3 — Observer
│   ├── ClientesNotificacion.java            (DADO)
│   ├── ObservadorCambioEstado.java          observador
│   ├── PublicadorCambiosEstado.java         sujeto
│   ├── EventoCambioEstado.java              datos del cambio (inmutable)
│   ├── NotificadorCorreoSolicitante.java    reacción 1
│   ├── ActualizadorDashboardContabilidad.java reacción 2
│   ├── RegistradorAuditoria.java            reacción 3
│   └── ConfiguracionNotificacion.java
└── estado/                                  Necesidad 4 — State
    ├── EstadoSolicitud.java                 interfaz de estado
    ├── EstadoBase.java                      todo inválido por defecto
    ├── EstadoPendiente.java, EstadoEnAprobacion.java, EstadoAprobada.java,
    │   EstadoRechazada.java, EstadoEjecutada.java, EstadoCancelada.java
    ├── CatalogoEstados.java                 nombre (String) → objeto estado
    ├── ContextoSolicitud.java               contexto: delega en el estado y publica el cambio
    └── ResultadoTransicion.java
```

Flujo completo:
1. `ControladorSolicitudes` llama a `ServicioAprobacion.evaluar()`.
2. La **cadena** decide quién resuelve.
3. El resultado se aplica con el **estado** de la solicitud (`ContextoSolicitud.aprobar/rechazar`).
4. El contexto **publica** el cambio a los observadores.
5. Después, el equipo de Compras ejecuta los **comandos** (reservar presupuesto, generar orden).
   `GenerarOrdenCompraOperacion` también pasa por `ContextoSolicitud.ejecutar()`, así que la regla de
   transición se valida y los observadores se enteran tanto de la ejecución como de su undo.

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

### Necesidad 3 — Notificaciones ante cambio de estado → **Observer**

**Síntoma de diseño.** Cada vez que una solicitud cambia de estado, tres módulos **ajenos a ella**
(correo, tablero de contabilidad, auditoría) deben reaccionar. Ni la solicitud ni el código que la
modifica deben conocerlos, y una cuarta reacción no debe obligar a tocar ese código.

**Solución.**
- `PublicadorCambiosEstado` (sujeto) guarda una lista de `ObservadorCambioEstado` y ofrece
  `suscribir`, `desuscribir` y `publicar`. Un observador que falla no impide que los demás reciban el
  evento.
- Tres observadores concretos usan tal cual los métodos de `ClientesNotificacion`:
  `NotificadorCorreoSolicitante`, `ActualizadorDashboardContabilidad` y `RegistradorAuditoria`.
- `EventoCambioEstado` es un `record` inmutable (id, email, monto, estado anterior, estado nuevo,
  detalle). Los observadores no pueden alterar la solicitud.
- En Spring, los observadores son `@Component`, y `ConfiguracionNotificacion` inyecta
  `List<ObservadorCambioEstado>` en el publicador. Una cuarta reacción, por ejemplo el canal interno
  del área, es solo un nuevo `@Component`.
- **Conexión con las Necesidades 1 y 2:** el único punto donde cambia el estado es
  `ContextoSolicitud.transicionarA()` (Necesidad 4), y ese método llama a `publicar`. Tanto
  `ServicioAprobacionPorNiveles` (aprobar/rechazar) como `GenerarOrdenCompraOperacion` (ejecutar y
  su undo, que cancela la orden) pasan por ese contexto. Lo verifican las pruebas
  `laResolucionDeLaCadenaDeAprobacionNotifica` y `ejecutarYCancelarLaOrdenDeCompraNotificaAmbosCambios`.
- La prueba `agregarUnCuartoSuscriptorDePruebaNoRequiereModificarElMecanismo` registra un
  `ColectorEventos` sin tocar el publicador y comprueba que recibe el evento.

**Alternativa descartada: State (el patrón de la Necesidad 4).** State cambia el comportamiento **de
la propia solicitud** según su estado: qué operaciones admite y a dónde transiciona. Aquí la solicitud
ya cambió, y lo que hay que resolver es que **otros objetos** se enteren. Si las reacciones se
pusieran dentro de los objetos estado (por ejemplo, que `EstadoAprobada` enviara el correo y
actualizara el tablero):
- cada estado quedaría acoplado a correo, contabilidad y auditoría;
- cada reacción quedaría repetida en todos los estados;
- una cuarta reacción obligaría a modificar **todas** las clases de estado.

Observer separa al que cambia (sujeto) de los que reaccionan (suscriptores) mediante una lista que
crece en tiempo de ejecución. State no tiene ese mecanismo de suscripción, porque su propósito es
otro. Por eso la solución combina ambos: State decide **si** el cambio es válido, y Observer
**difunde** que ocurrió.

> Alternativa equivalente: `ApplicationEventPublisher` + `@EventListener` de Spring también son un
> Observer. Se prefirió un sujeto propio para que el mecanismo sea testeable sin levantar el contexto
> de Spring y para que el registro de suscriptores sea explícito.

### Necesidad 4 — Reglas de transición según el estado → **State**

**Síntoma de diseño.** Las reglas sobre qué operación es válida (aprobar, rechazar, ejecutar,
cancelar) estaban dispersas en `if/else` sobre `getEstado()`, repetidos en varios métodos. Cada
estado nuevo, como `EN_ESPERA_PROVEEDOR`, obligaba a revisarlos todos. La versión de la Parte 1 de
`GenerarOrdenCompraOperacion` tenía justamente un `if (!"APROBADA".equals(solicitud.getEstado()))`.

**Solución.**
- `EstadoSolicitud` declara las cuatro operaciones. Cada una recibe el `ContextoSolicitud` para poder
  transicionar.
- `EstadoBase` rechaza **todas** las operaciones por defecto. Cada estado concreto redefine solo las
  que admite:

  | Estado | aprobar | rechazar | ejecutar | cancelar |
  |---|---|---|---|---|
  | PENDIENTE / EN_APROBACION | → APROBADA | → RECHAZADA | ✗ | → CANCELADA |
  | APROBADA | ✗ | ✗ | → EJECUTADA | → CANCELADA |
  | RECHAZADA, EJECUTADA, CANCELADA | ✗ | ✗ | ✗ | ✗ |

- `ContextoSolicitud` envuelve la `Solicitud` y delega cada operación en su estado actual
  (`estado.ejecutar(this, motivo)`). Solo los objetos estado pueden llamar a `transicionarA()`
  (visibilidad de paquete). Una operación inválida devuelve `ResultadoTransicion` con
  `exitosa = false` y **no** cambia el estado ni notifica.
- Como `Solicitud` (dada) guarda el estado como `String`, `CatalogoEstados` traduce ese nombre al
  objeto estado. Agregar `EN_ESPERA_PROVEEDOR` es crear su clase, definir qué operaciones admite y
  registrarla en el catálogo. No hay que revisar ningún `if/else`.
- `revertirA()` es una compensación usada solo por el undo de la Necesidad 2. No es una operación de
  negocio, así que no está en la interfaz de estado.

**Alternativa descartada: Strategy.** Estructuralmente se parecen: un contexto delega en un objeto
intercambiable que implementa una interfaz común. La diferencia está en **quién elige** y en **si el
objeto conoce a los demás**:
- En Strategy (el carrito que activa el descuento que quiere), un **cliente externo** elige e inyecta
  la estrategia. Las estrategias son independientes entre sí y no cambian al contexto: ninguna
  estrategia de descuento decide pasar a otra.
- Aquí nadie de afuera elige qué comportamiento usar. Lo determina la historia de la propia
  solicitud. Además, resolver la operación **implica transicionar**: `EstadoAprobada.ejecutar()` es
  quien decide que el siguiente estado es `EstadoEjecutada`. Los estados se conocen entre sí y
  cambian el contexto desde dentro, algo que un conjunto de estrategias independientes no hace.

Con Strategy, el cliente tendría que preguntar el estado actual para elegir la estrategia correcta
y luego actualizarla él mismo tras cada operación. Ese es el mismo `if (getEstado()...)` disperso
que se quería eliminar, solo que movido de lugar.

### Reflexión — otros tres patrones

1. **Reporte que recorre todas las solicitudes de un centro de costo → Iterator.** Un
   `Iterator<Solicitud>` (o un `Iterable`) expuesto por la colección del centro de costo permite
   recorrerlas en secuencia sin revelar si están en una `List`, un `Map` o un árbol. El reporte
   depende solo de `hasNext()/next()`.
2. **Tres comprobantes con el mismo esqueleto (encabezado, cuerpo, pie) → Template Method.** Una clase
   abstracta `Comprobante` con un método `final imprimir()` que llama a `encabezado()`, `cuerpo()` y
   `pie()`. La orden de compra, la reserva presupuestal y el acta de rechazo solo redefinen
   `cuerpo()`.
3. **Guardar y restaurar instantáneas completas de una solicitud → Memento.** `Solicitud` (el
   originador) produciría un memento opaco con todo su estado, y un cuidador lo guardaría sin conocer
   su contenido. Se diferencia del Command de la Necesidad 2 en dos cosas:
   - Command revierte **una operación** con su acción inversa (`liberar`, `cancelar`) y solo conoce
     lo que esa operación tocó.
   - Memento restaura el **estado completo** del objeto en cualquier punto de su historia, sin
     necesitar una operación inversa, a costa de almacenar instantáneas.

## Pruebas
`mvn test` ejecuta 34 pruebas JUnit 5:

| Clase de prueba | Qué valida |
|---|---|
| `AprobacionNivelesTest` (9) | Las 3 pruebas del enunciado, más: Director, revisor que no interviene, internacional que escala, rechazo por normativa, rechazo dentro de la autoridad y reordenamiento de la cadena. |
| `ControladorSolicitudesTest` (3) | Endpoint `POST /api/solicitudes/evaluar` con el contexto Spring real (200 / 422). |
| `EjecucionSolicitudTest` (7) | Las 3 pruebas del enunciado, más: undo selectivo por tipo, historial con operaciones deshechas, orden rechazada si no está APROBADA y undo sin operaciones. |
| `NotificacionEstadoTest` (6) | Las 2 pruebas del enunciado (con captura de consola de las 3 reacciones), integración con las Necesidades 1 y 2, operación inválida sin notificación y suscriptor que falla. |
| `TransicionEstadoTest` (9) | Las 3 pruebas del enunciado, más la tabla completa de transiciones, estados finales y estado desconocido. |

En las pruebas del enunciado de la Necesidad 2, a la solicitud `S-011` se le asigna el estado
`APROBADA` antes de ejecutar. La necesidad parte de "una vez una solicitud queda APROBADA", y la
regla de la Necesidad 4 impide generar la orden de una solicitud PENDIENTE.

## Herramientas utilizadas
- Java 17, Spring Boot 3.2, Apache Maven, JUnit 5
- VS Code, Git, GitHub

## Conclusiones
Lo más difícil no fue implementar cada patrón, sino distinguirlo de su vecino. Command y Chain of
Responsibility, o State y Strategy, tienen estructuras de clases casi idénticas, así que reconocer
la forma no basta: hay que preguntar **quién decide** (un eslabón, un invocador, un cliente externo
o el propio objeto) y **qué se necesita conservar** (un historial, un sucesor, una lista de
suscriptores). La Necesidad 1 se resolvió al notar que el emisor no debía conocer a los niveles, y
la Necesidad 2 al notar que había que recordar lo ejecutado para revertirlo. Observer y State
resultaron complementarios: el estado decide si un cambio es válido y el publicador lo difunde, y
ambos se apoyan en un único punto de cambio (`ContextoSolicitud`). Por último, las restricciones de
"no modificar el código dado" guiaron el diseño: fueron las que obligaron a separar responsabilidades
en lugar de agregar otro `if`.
