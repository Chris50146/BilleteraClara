# Billetera Clara - Proyecto final de Programación Funcional

**Integrantes:** Israel Vargas y Christian Campero.

## 1. Descripción del dominio

Billetera Clara permite registrar y clasificar ingresos y gastos personales, consultar un saldo acumulado y comprobar una regla sencilla sobre el gasto en ocio. Está dirigido a personas que quieren entender sus movimientos diarios y evaluar una compra antes de realizarla.

Administra un usuario, sus transacciones, las fechas, los tipos de movimiento, las categorías y los resultados de auditoría y simulación. Toda la información permanece en memoria durante la sesión. El proyecto no incorpora cuentas bancarias, autenticación ni conexión a una base de datos.

## 2. Funcionalidades principales

1. Registrar ingresos y gastos válidos creando una billetera nueva.
2. Calcular ingresos, gastos y saldo acumulado.
3. Buscar movimientos mediante un criterio configurable; la interfaz ofrece búsqueda por descripción o categoría y filtro por tipo.
4. Obtener totales de gastos por categoría.
5. Auditar si el ocio supera el 20% del gasto total.
6. Simular una compra y mostrar saldo y diagnóstico proyectados sin alterar los movimientos.
7. Eliminar un movimiento por su identificador creando una billetera nueva.
8. Comenzar una sesión en blanco a partir de los datos de demostración.

## 3. Modelo de datos

| Tipo | Atributos o alternativas | Responsabilidad |
| --- | --- | --- |
| `Usuario` | `id: String`, `nombre: String` | Identificar al propietario de la billetera. |
| `Fecha` | `dia: Int`, `mes: Int`, `anio: Int` | Representar y validar una fecha sin bibliotecas de calendario en el modelo. |
| `Transaccion` | `id: String`, `descripcion: String`, `montoCentavos: Long`, `fecha: Fecha`, `categoria: Categoria`, `tipo: TipoTransaccion` | Representar un movimiento y obtener su monto con signo mediante `match`. |
| `Billetera` | `usuario: Usuario`, `transacciones: List[Transaccion]` | Encapsular el estado válido y sus operaciones inmutables. |
| `TipoTransaccion` | `Ingreso`, `Gasto` | Jerarquía cerrada del tipo de movimiento. |
| `Categoria` | `Salario`, `Alimentacion`, `Transporte`, `Ocio`, `Hogar`, `Salud`, `Otros` | Clasificar movimientos y determinar compatibilidad con su tipo. |
| `EstadoDiagnostico` | `Saludable`, `Alerta` | Expresar el resultado de la regla de ocio. |
| `Diagnostico` | `estado`, `gastoOcio: Long`, `totalGastos: Long`, `porcentajeOcio: Double` | Presentar la auditoría. |
| `Simulacion` | `saldoActual: Long`, `saldoProyectado: Long`, `diagnosticoActual`, `diagnosticoProyectado`, `compraPosible: Boolean` | Presentar una proyección sin registrarla. |
| `Resultado[+A]` | `Exito[A](valor: A)` o `Error(mensaje: String)` | Comunicar resultados y errores usando genéricos y pattern matching. |

Un usuario posee una billetera; la billetera contiene una lista inmutable de transacciones. Cada transacción tiene una fecha, una categoría y un tipo. `Transaccion`, `Fecha`, `Usuario`, `Diagnostico`, `Simulacion`, `Exito` y `Error` son case classes. Los tipos, categorías y estados concretos son case objects.

`Billetera` es una clase con constructor privado y atributos `val`. Se crea vacía desde su objeto acompañante; `registrar` devuelve `Resultado[Billetera]` y `eliminar` devuelve otra `Billetera`. No existe un constructor público para introducir una lista sin validar.

## 4. Reglas y validaciones

- Los importes se expresan internamente en centavos enteros positivos: `100L` equivale a Bs 1,00. No se redondean importes ingresados con más de dos decimales: la interfaz los rechaza.
- Los identificadores y las descripciones no pueden estar vacíos ni contener solamente espacios. Los identificadores deben ser únicos dentro de la billetera.
- Las fechas deben existir en el calendario, con años de 1 a 9999. Se consideran las reglas de años bisiestos, incluidos los siglos.
- Un ingreso puede pertenecer a Salario u Otros. Un gasto puede pertenecer a Alimentación, Transporte, Ocio, Hogar, Salud u Otros.
- Una simulación acepta únicamente montos positivos y categorías de gasto.
- La suma de importes registrados (ingresos más gastos) no puede superar `1000000000000L` centavos, equivalentes a Bs 10.000.000.000. Una proyección respeta el mismo límite al añadir su compra hipotética. Esta cota técnica evita desbordamientos en sumas y productos; no representa una regla de presupuesto. Eliminar movimientos libera ese margen.
- Es posible registrar un gasto que deje saldo negativo. En la simulación, `compraPosible` indica si el saldo proyectado es mayor o igual a cero. El saldo exactamente cero sí alcanza.
- La auditoría compara `gastoOcio * 100 > totalGastos * 20` con enteros. Solo se usa `Double` para presentar el porcentaje. Sin gastos, el porcentaje es cero y el estado es Saludable.
- Una operación rechazada devuelve `Error` y conserva la billetera original. La simulación nunca agrega una transacción.

## 5. Decisiones de diseño

### Dinero exacto con tipos básicos

La propuesta inicial usaba `Double` para los montos. Se cambió a `Long` en centavos porque las fracciones decimales pueden generar residuos al sumar o restar con `Double`. Ahora un ingreso de 30 centavos, un gasto de 10 y una compra de 20 producen exactamente cero. No se utiliza una biblioteca financiera en el modelo.

### Fechas con una case class

La fecha se representa mediante tres enteros y se valida con condiciones y pattern matching. Consultar la fecha del equipo y darle formato visual es responsabilidad de la interfaz. Los datos de demostración tienen fechas fijas para obtener ejemplos reproducibles.

### Categorías tipadas

La propuesta inicial describía `categoria: String`. Se sustituyó por una jerarquía `sealed trait Categoria` y case objects para impedir categorías escritas de maneras distintas. La compatibilidad entre categoría y tipo se comprueba al registrar y simular.

### Inmutabilidad y encapsulamiento

La lista de una billetera no cambia. Registrar antepone una transacción a una nueva lista y crea una nueva instancia; eliminar usa `filter`. El constructor privado impide crear una billetera con datos que no hayan pasado por el registro. Las transacciones son datos candidatos; únicamente las aceptadas se incorporan al estado de la billetera.

### Funciones puras y abstracciones

El modelo no consulta el reloj, no genera identificadores, no dibuja controles ni modifica variables. `buscar` recibe un criterio de tipo `Transaccion => Boolean`. `map` genera los totales por categoría; `foldLeft` acumula importes. La auditoría y la simulación comparten `evaluarGastos` para no duplicar la regla del 20%.

Los resultados genéricos `Resultado[+A]`, `Exito[A]` y `Error` permiten comunicar éxito o rechazo tanto para registros como para simulaciones. Este patrón se encuentra en los apuntes de la clase 26.

## 6. Relación con el material del curso

Las rutas siguientes corresponden al ZIP `Programacion-Funcional-Mod2-2026-main` entregado como referencia. Se considera permitido un recurso que aparezca mencionado en ese repositorio, aunque no incluya un ejemplo desarrollado, según el criterio aclarado para este proyecto.

| Concepto usado | Material del curso | Aplicación en el proyecto |
| --- | --- | --- |
| Valores, expresiones, funciones y tipos básicos | `Tema-02-Expresiones-Valores-Funciones` y `Tema-06-Polimorfismo-Genericos/Apuntes-Clase23.md` (jerarquía de tipos numéricos, incluido Long) | Atributos `val`, cálculos en centavos y validaciones. |
| Recursión sobre cadenas | `Tema-03-Recursion/Clase7-Strings.scala` | Comprobar si un identificador o descripción contiene texto usando `head` y `tail`. |
| Funciones de orden superior | `Tema-04-Funciones-OrdenSuperior` | `buscar(criterio: Transaccion => Boolean)`. |
| Atributos abstractos val en traits | `Tema-05-POO/Apuntes-Clase20.md`, apartado 9 | Nombres de tipos, categorías y estados. |
| Constructor privado y objeto acompañante | `Tema-05-POO/Clase19.scala` (Product) | Encapsulamiento de `Billetera` y creación con `Billetera.vacia`. |
| Genéricos | `Tema-06-Polimorfismo-Genericos` | `Resultado[+A]` para distintos tipos de resultado. |
| Case classes, copy y pattern matching | `Tema-07-Case-Classes/Clase25.scala` y sus apuntes | Entidades, resultados y datos de las comprobaciones. |
| Sealed trait, case object y ADT genérico de resultados | `Tema-07-Case-Classes/Clase26.scala` y `Apuntes-Clase26.md`, apartados 15 y 16 | Tipos, categorías, estados y `Resultado/Exito/Error`. |
| Listas inmutables, Nil y :: | `Tema-08-Listas/Clase27.scala` | Almacenamiento y registro inmutable de transacciones. |
| Map y filter | `Tema-08-Listas/Apuntes-Clase28.md` | Totales por categoría, búsqueda y eliminación. |
| FoldLeft | `Tema-08-Listas/Apuntes-Clase27.md` y `Practicas/PropuestaProyectoFinal.md` | Sumas y carga funcional del ejemplo. Se menciona en el material y se propone expresamente en el enunciado. |

No se requiere utilizar todas las operaciones citadas en el enunciado: se aplican según corresponda al problema. El modelo no usa `LocalDate`, `filterNot`, `List.empty`, `Try`, `Either`, bases de datos ni frameworks funcionales externos.

## 7. Interfaz gráfica asociada

Se utiliza `javax.swing`, la alternativa de JVM contemplada en la propuesta. La interfaz muestra saldo, ingresos, gastos, movimientos, distribución por categoría, auditoría y simulación. Después de registrar correctamente, la interfaz limpia la búsqueda y restablece el filtro a Todos para que el nuevo movimiento no quede oculto. El diseño incorpora un logo dibujado, tarjetas con colores, iconos por categoría, avisos de registro y eliminación, navegación suave y una barra de ocio animada. La auditoría muestra también ingresos y gastos totales, y distingue entre ausencia de gastos y ausencia de ocio. Empezar en blanco pide confirmación si hay movimientos. El buscador tiene una etiqueta visible y los selectores permiten navegación con teclado. Los botones invocan el modelo y procesan `Exito` o `Error` mediante `match`.

`Main.scala` contiene Swing, formato visual, eventos, fecha del sistema e identificadores. La variable `actual` referencia la versión vigente de la billetera. Las demás referencias mutables administran componentes y temporizadores de animación; pertenecen a presentación y se detienen al cerrar la ventana. Los objetos del dominio y sus listas siguen siendo inmutables.

`EntradaInterfaz.scala` adapta el texto a centavos. Emplea una conversión decimal exacta de la JVM y `Try` exclusivamente para manejar entradas de la interfaz. Estas herramientas, Swing y sus componentes pertenecen a la capa de interfaz autorizada como excepción al contenido del curso. El modelo recibe centavos enteros y vuelve a validar las reglas del negocio.

Money Manager Expense & Budget (https://www.realbyteapps.com/) se toma como referencia de organización visual de movimientos, resúmenes y categorías. No se pretende reproducir todas sus funciones.

## 8. Ejemplos de ejecución

### Ejemplo A: demostración inicial

La aplicación carga seis movimientos fijos. Ingresos: Bs 2.500,00 y Bs 650,00. Gastos: Bs 380,00, Bs 123,80, Bs 18,50 y Bs 45,00. Resultado: ingresos Bs 3.150,00; gastos Bs 567,30; saldo Bs 2.582,70; ocio aproximadamente 7,9%; diagnóstico Saludable.

### Ejemplo B: saldo exacto

Desde una billetera vacía, registrar un ingreso de Bs 0,30 y un gasto de Alimentación de Bs 0,10. Simular otra compra de Alimentación de Bs 0,20. Resultado: saldo actual Bs 0,20, saldo proyectado Bs 0,00 y saldo suficiente. La lista original conserva sus dos movimientos.

### Ejemplo C: umbral de ocio

Registrar un ingreso de Bs 100,00, un gasto de Alimentación de Bs 80,00 y uno de Ocio de Bs 20,00. El saldo es cero y el ocio constituye exactamente el 20%: Saludable. Simular Bs 0,01 de Ocio produce saldo proyectado de Bs -0,01, saldo insuficiente y diagnóstico Alerta. Los tres registros originales se conservan.

### Ejemplo D: rechazo sin alterar el estado

Intentar registrar un movimiento con monto cero o negativo, fecha 31/04/2026, identificador repetido o un gasto en Salario devuelve `Error`. Un texto como `12,345` se rechaza en la interfaz por tener más de dos decimales. El saldo y los movimientos permanecen iguales.

### Ejemplo de uso del modelo

```scala
val inicial = Billetera.vacia(Usuario("ana", "Ana"))
val ingreso = Transaccion("1", "Ingreso", 30L, Fecha(1, 1, 2026), Salario, Ingreso)
inicial.registrar(ingreso) match
  case Error(mensaje) => println(mensaje)
  case Exito(conIngreso) =>
    val gasto = Transaccion("2", "Comida", 10L, Fecha(1, 1, 2026), Alimentacion, Gasto)
    conIngreso.registrar(gasto) match
      case Error(mensaje) => println(mensaje)
      case Exito(conGasto) =>
        conGasto.simularCompra(20L, Alimentacion) match
          case Error(mensaje) => println(mensaje)
          case Exito(simulacion) =>
            println(simulacion.saldoProyectado) // 0 centavos
            println(simulacion.compraPosible)   // true
```

## 9. Ejecución y comprobaciones

Desde la raíz, con Java 17 o superior y Scala CLI. Para Swing se necesita Java con soporte gráfico y una pantalla activa; una instalación solo headless permite ejecutar las comprobaciones, pero no abrir la ventana:

```bash
scala run src/main/scala --server=false --jvm system --main-class billeteraclara.Main
scala run src/main/scala src/test/scala --server=false --jvm system --main-class billeteraclara.Verificacion
```

Alternativas con sbt: `sbt run` y `sbt "Test / runMain billeteraclara.Verificacion"`.

La primera ejecución puede descargar las herramientas de compilación. Las comprobaciones son independientes de la ventana y cubren los ejemplos anteriores, el límite numérico, búsquedas, totales, eliminación, bisiestos y conversión de entradas. El mensaje de éxito esperado es: `Todas las comprobaciones del modelo y del adaptador de interfaz pasaron.`

## 10. Contenido de la entrega

- Código completo en `src/main/scala`.
- Comprobaciones ejecutables en `src/test/scala`.
- Este documento, con dominio, modelo, funcionalidades, interfaz, decisiones y ejemplos.
- README con instrucciones de ejecución y uso.
- `docs/Presentacion.md`: guía de demostración y correspondencia con los requisitos obligatorios.
- Configuración `build.sbt` y `project/build.properties` (infraestructura de compilación, separada del contenido del modelo).
- Paquete `BilleteraClara-Publicar.zip` con los mismos fuentes y documentos, sin cachés ni archivos compilados.
