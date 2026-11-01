package billeteraclara

import java.time.LocalDate

// Los case object representan alternativas cerradas: no se puede escribir un tipo inválido.
sealed trait TipoTransaccion:
  def nombre: String

case object Ingreso extends TipoTransaccion:
  val nombre = "Ingreso"

case object Gasto extends TipoTransaccion:
  val nombre = "Gasto"

sealed trait Categoria:
  def nombre: String

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

sealed trait EstadoDiagnostico:
  def nombre: String

case object Saludable extends EstadoDiagnostico:
  val nombre = "Saludable"

case object Alerta extends EstadoDiagnostico:
  val nombre = "Alerta"

case class Usuario(id: String, nombre: String)

case class Transaccion(
    id: String,
    descripcion: String,
    monto: Double,
    fecha: LocalDate,
    categoria: Categoria,
    tipo: TipoTransaccion
):
  // Un movimiento determina su propio efecto en el saldo.
  def montoConSigno: Double = tipo match
    case Ingreso => monto
    case Gasto   => -monto

case class Diagnostico(
    estado: EstadoDiagnostico,
    gastoOcio: Double,
    totalGastos: Double,
    porcentajeOcio: Double
)

case class Simulacion(
    saldoActual: Double,
    saldoProyectado: Double,
    diagnosticoActual: Diagnostico,
    diagnosticoProyectado: Diagnostico,
    compraPosible: Boolean
)
