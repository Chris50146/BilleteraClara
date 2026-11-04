package billeteraclara

// El constructor privado impide saltarse las validaciones del registro.
// Sus atributos son val y cada cambio produce una nueva instancia.
class Billetera private(val usuario: Usuario, val transacciones: List[Transaccion]):
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

  def eliminar(id: String): Billetera =
    new Billetera(usuario, transacciones.filter(_.id != id))

  // Función de orden superior: recibe el criterio de búsqueda.
  def buscar(criterio: Transaccion => Boolean): List[Transaccion] =
    transacciones.filter(criterio)

  // Todos los importes del modelo están expresados en centavos enteros.
  def saldo: Long =
    transacciones.foldLeft(0L)((total, tx) => total + tx.montoConSigno)

  def ingresos: Long =
    buscar(_.tipo == Ingreso).foldLeft(0L)((total, tx) => total + tx.montoCentavos)

  def gastos: Long =
    buscar(_.tipo == Gasto).foldLeft(0L)((total, tx) => total + tx.montoCentavos)

  def totalesPorCategoria: List[(Categoria, Long)] =
    Categoria.deGasto.map(categoria =>
      (categoria, buscar(tx => tx.tipo == Gasto && tx.categoria == categoria)
        .foldLeft(0L)((total, tx) => total + tx.montoCentavos))
    )

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
        val ocioProyectado = actual.gastoOcio + (if categoria == Ocio then montoCentavos else 0L)
        val proyectado = evaluarGastos(ocioProyectado, actual.totalGastos + montoCentavos)
        val nuevoSaldo = saldo - montoCentavos
        Exito(Simulacion(saldo, nuevoSaldo, actual, proyectado, nuevoSaldo >= 0))

  // Recorrido recursivo de String con head/tail, como en el tema de recursión.
  private def tieneTexto(texto: String): Boolean =
    if texto.length() == 0 then false
    else if texto.head == ' ' || texto.head == '\t' || texto.head == '\n' || texto.head == '\r' then
      tieneTexto(texto.tail)
    else true

  private def validarMonto(montoCentavos: Long): Resultado[Long] =
    if montoCentavos <= 0 then Error("El monto debe ser mayor a cero.")
    else if montoCentavos > Dinero.limiteCentavos then
      Error("El monto supera el límite admitido por el sistema.")
    else if montoCentavos > Dinero.limiteCentavos - (ingresos + gastos) then
      Error("La suma de importes de la sesión supera el límite del sistema.")
    else Exito(montoCentavos)

  private def evaluarGastos(ocio: Long, total: Long): Diagnostico =
    val porcentaje = if total == 0 then 0.0 else ocio * 100.0 / total
    // La decisión usa enteros; Double se reserva para mostrar el porcentaje.
    val estado = if ocio * 100 > total * 20 then Alerta else Saludable
    Diagnostico(estado, ocio, total, porcentaje)

object Billetera:
  def vacia(usuario: Usuario): Billetera = new Billetera(usuario, Nil)
