package billeteraclara

// Los case object representan alternativas cerradas: no se puede escribir un tipo inválido.
sealed trait TipoTransaccion:
  val nombre: String

case object Ingreso extends TipoTransaccion:
  val nombre = "Ingreso"

case object Gasto extends TipoTransaccion:
  val nombre = "Gasto"

sealed trait Categoria:
  val nombre: String

case object Salario extends Categoria:
  val nombre = "Salario"

case object Alimentacion extends Categoria:
  val nombre = "Alimentación"

case object Transporte extends Categoria:
  val nombre = "Transporte"

case object Ocio extends Categoria:
  val nombre = "Ocio"

case object Hogar extends Categoria:
  val nombre = "Hogar"

case object Salud extends Categoria:
  val nombre = "Salud"

case object Otros extends Categoria:
  val nombre = "Otros"

object Categoria:
  val todas: List[Categoria] = List(Salario, Alimentacion, Transporte, Ocio, Hogar, Salud, Otros)
  val deGasto: List[Categoria] = List(Alimentacion, Transporte, Ocio, Hogar, Salud, Otros)
  val deIngreso: List[Categoria] = List(Salario, Otros)

  def compatible(categoria: Categoria, tipo: TipoTransaccion): Boolean = tipo match
    case Ingreso => deIngreso.contains(categoria)
    case Gasto   => deGasto.contains(categoria)

// Resultado genérico del curso: una operación devuelve un valor o un error.
sealed trait Resultado[+A]
case class Exito[A](valor: A) extends Resultado[A]
case class Error(mensaje: String) extends Resultado[Nothing]

object Dinero:
  // Suma máxima de importes de la sesión: Bs 10.000.000.000 en centavos.
  // Los productos para calcular porcentajes también quedan dentro del rango de Long.
  val limiteCentavos: Long = 1000000000000L

case class Fecha(dia: Int, mes: Int, anio: Int):
  def esValida: Boolean =
    val bisiesto = anio % 400 == 0 || (anio % 4 == 0 && anio % 100 != 0)
    val diasDelMes =
      if mes == 2 then (if bisiesto then 29 else 28)
      else if List(4, 6, 9, 11).contains(mes) then 30
      else 31
    anio >= 1 && anio <= 9999 && mes >= 1 && mes <= 12 &&
      dia >= 1 && dia <= diasDelMes

sealed trait EstadoDiagnostico:
  val nombre: String

case object Saludable extends EstadoDiagnostico:
  val nombre = "Saludable"

case object Alerta extends EstadoDiagnostico:
  val nombre = "Alerta"

case class Usuario(id: String, nombre: String)

case class Transaccion(
    id: String,
    descripcion: String,
    montoCentavos: Long,
    fecha: Fecha,
    categoria: Categoria,
    tipo: TipoTransaccion
):
  // Un movimiento determina su propio efecto en el saldo.
  def montoConSigno: Long = tipo match
    case Ingreso => montoCentavos
    case Gasto   => -montoCentavos

case class Diagnostico(
    estado: EstadoDiagnostico,
    gastoOcio: Long,
    totalGastos: Long,
    porcentajeOcio: Double
)

case class Simulacion(
    saldoActual: Long,
    saldoProyectado: Long,
    diagnosticoActual: Diagnostico,
    diagnosticoProyectado: Diagnostico,
    compraPosible: Boolean
)
