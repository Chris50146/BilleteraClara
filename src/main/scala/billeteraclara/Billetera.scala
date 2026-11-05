package billeteraclara

// El constructor privado impide saltarse las validaciones del registro.
// Sus atributos son val y cada cambio produce una nueva instancia.
class Billetera private(val usuario: Usuario, val transacciones: List[Transaccion]):
  // Valida primero y devuelve el primer error encontrado o una nueva billetera.
  // Una aceptación antepone el movimiento con :: sin modificar la lista anterior.
  def registrar(transaccion: Transaccion): Resultado[Billetera] =
    if !tieneTexto(transaccion.id) then Error("El identificador no puede estar vacío.")
    else if !buscar(_.id == transaccion.id).isEmpty then
      Error("Ya existe un movimiento con ese identificador.")
    else if !tieneTexto(transaccion.descripcion) then Error("Escribe una descripción.")
    else if !transaccion.fecha.esValida then Error("La fecha no es válida.")
    else if !Categoria.compatible(transaccion.categoria, transaccion.tipo) then
      Error("La categoría no corresponde al tipo de movimiento.")
    else validarMonto(transaccion.montoCentavos) match
      case Error(mensaje) => Error(mensaje)
      case Exito(_) => Exito(new Billetera(usuario, transaccion :: transacciones))

  // Conserva mediante filter todos los IDs distintos; un ID ausente no cambia el contenido.
  def eliminar(id: String): Billetera =
    new Billetera(usuario, transacciones.filter(_.id != id))

  // Función de orden superior: recibe el criterio de búsqueda.
  def buscar(criterio: Transaccion => Boolean): List[Transaccion] =
    transacciones.filter(criterio)

  // Todos los importes del modelo están expresados en centavos enteros.
  def saldo: Long =
    transacciones.foldLeft(0L)((total, tx) => total + tx.montoConSigno)

  // Selecciona ingresos y acumula desde cero; el resultado es positivo y en centavos.
  def ingresos: Long =
    buscar(_.tipo == Ingreso).foldLeft(0L)((total, tx) => total + tx.montoCentavos)

  // Acumula todos los gastos, incluida cualquier categoría distinta de Ocio.
  def gastos: Long =
    buscar(_.tipo == Gasto).foldLeft(0L)((total, tx) => total + tx.montoCentavos)

  // Produce un par (categoría, total) por cada categoría de gasto, incluso con total cero.
  // map transforma las categorías; filter selecciona movimientos y foldLeft suma importes.
  def totalesPorCategoria: List[(Categoria, Long)] =
    Categoria.deGasto.map(categoria =>
      (categoria, buscar(tx => tx.tipo == Gasto && tx.categoria == categoria)
        .foldLeft(0L)((total, tx) => total + tx.montoCentavos))
    )

  // Calcula el numerador de ocio y usa todos los gastos como denominador.
  def diagnostico: Diagnostico =
    val ocio = buscar(tx => tx.tipo == Gasto && tx.categoria == Ocio)
      .foldLeft(0L)((suma, tx) => suma + tx.montoCentavos)
    evaluarGastos(ocio, gastos)

  // La proyección calcula valores nuevos sin crear registros ficticios.
  def simularCompra(montoCentavos: Long, categoria: Categoria): Resultado[Simulacion] =
    if !Categoria.compatible(categoria, Gasto) then
      Error("Elige una categoría de gasto para la compra.")
    else validarMonto(montoCentavos) match
      case Error(mensaje) => Error(mensaje)
      case Exito(_) =>
        val actual = diagnostico
        // La compra siempre suma al gasto total; solo suma a ocio cuando su categoría es Ocio.
        val ocioProyectado = actual.gastoOcio + (if categoria == Ocio then montoCentavos else 0L)
        val proyectado = evaluarGastos(ocioProyectado, actual.totalGastos + montoCentavos)
        // El saldo exactamente cero alcanza. Esta decisión es independiente de la alerta de ocio.
        val nuevoSaldo = saldo - montoCentavos
        Exito(Simulacion(saldo, nuevoSaldo, actual, proyectado, nuevoSaldo >= 0))

  // Recorrido recursivo de String con head/tail, como en el tema de recursión.
  private def tieneTexto(texto: String): Boolean =
    if texto.length() == 0 then false
    else if texto.head == ' ' || texto.head == '\t' || texto.head == '\n' || texto.head == '\r' then
      tieneTexto(texto.tail)
    else true

  // Reutiliza las mismas restricciones para registro y simulación.
  // Comprueba el margen por resta antes de sumar para evitar desbordamientos.
  private def validarMonto(montoCentavos: Long): Resultado[Long] =
    if montoCentavos <= 0 then Error("El monto debe ser mayor a cero.")
    else if montoCentavos > Dinero.limiteCentavos then
      Error("El monto supera el límite admitido por el sistema.")
    else if montoCentavos > Dinero.limiteCentavos - (ingresos + gastos) then
      Error("La suma de importes de la sesión supera el límite del sistema.")
    else Exito(montoCentavos)

  // Comparte la regla entre auditoría actual y proyección; evita dividir entre cero.
  private def evaluarGastos(ocio: Long, total: Long): Diagnostico =
    val porcentaje = if total == 0 then 0.0 else ocio * 100.0 / total
    // La decisión usa enteros; Double se reserva para mostrar el porcentaje.
    val estado = if ocio * 100 > total * 20 then Alerta else Saludable
    Diagnostico(estado, ocio, total, porcentaje)

// El objeto acompañante ofrece la creación válida inicial, con una lista vacía.
object Billetera:
  // Nil representa la ausencia de movimientos y los totales parten de cero.
  def vacia(usuario: Usuario): Billetera = new Billetera(usuario, Nil)
