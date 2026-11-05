package billeteraclara

// Los case object representan alternativas cerradas: no se puede escribir un tipo inválido.
sealed trait TipoTransaccion:
  val nombre: String

case object Ingreso extends TipoTransaccion:
  val nombre = "Ingreso"

case object Gasto extends TipoTransaccion:
  val nombre = "Gasto"

// Categorías cerradas: el compilador conoce las alternativas del dominio.
// El nombre es presentación; las reglas comparan los objetos tipados.
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

// Listas compartidas por las validaciones del modelo y los selectores de interfaz.
object Categoria:
  val todas: List[Categoria] = List(Salario, Alimentacion, Transporte, Ocio, Hogar, Salud, Otros)
  val deGasto: List[Categoria] = List(Alimentacion, Transporte, Ocio, Hogar, Salud, Otros)
  val deIngreso: List[Categoria] = List(Salario, Otros)

  // Determina qué combinaciones pueden registrarse: Otros admite ambos tipos.
  def compatible(categoria: Categoria, tipo: TipoTransaccion): Boolean = tipo match
    case Ingreso => deIngreso.contains(categoria)
    case Gasto   => deGasto.contains(categoria)

// Resultado genérico del curso: una operación devuelve un valor o un error.
sealed trait Resultado[+A]
// Contiene el valor aceptado; A puede ser Billetera, Simulacion u otro tipo.
case class Exito[A](valor: A) extends Resultado[A]
// Nothing permite utilizar este error como Resultado de cualquier tipo de éxito.
case class Error(mensaje: String) extends Resultado[Nothing]

object Dinero:
  // Suma máxima de importes de la sesión: Bs 10.000.000.000 en centavos.
  // Los productos para calcular porcentajes también quedan dentro del rango de Long.
  val limiteCentavos: Long = 1000000000000L

// Representa una fecha candidata con tipos básicos; registrar consulta esValida.
case class Fecha(dia: Int, mes: Int, anio: Int):
  // Comprueba el calendario y el rango de años sin consultar el reloj ni usar LocalDate.
  def esValida: Boolean =
    // Un siglo solo es bisiesto si es divisible por 400; los demás años, por 4.
    val bisiesto = anio % 400 == 0 || (anio % 4 == 0 && anio % 100 != 0)
    val diasDelMes =
      if mes == 2 then (if bisiesto then 29 else 28)
      else if List(4, 6, 9, 11).contains(mes) then 30
      else 31
    anio >= 1 && anio <= 9999 && mes >= 1 && mes <= 12 &&
      dia >= 1 && dia <= diasDelMes

// Resultado de la regla del ocio; no representa la disponibilidad de saldo.
sealed trait EstadoDiagnostico:
  val nombre: String

case object Saludable extends EstadoDiagnostico:
  val nombre = "Saludable"

case object Alerta extends EstadoDiagnostico:
  val nombre = "Alerta"

// Identifica al titular de la sesión; no incorpora autenticación ni persistencia.
case class Usuario(id: String, nombre: String)

// Datos de un movimiento candidato. Su incorporación a la billetera exige registrar.
// El importe se guarda positivo en centavos: el tipo determina su efecto sobre el saldo.
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

// Importes exactos de la auditoría y porcentaje aproximado para presentación.
case class Diagnostico(
    estado: EstadoDiagnostico,
    gastoOcio: Long,
    totalGastos: Long,
    porcentajeOcio: Double
)

// Agrupa los valores actuales y proyectados; no contiene una transacción registrada.
case class Simulacion(
    saldoActual: Long,
    saldoProyectado: Long,
    diagnosticoActual: Diagnostico,
    diagnosticoProyectado: Diagnostico,
    compraPosible: Boolean
)
