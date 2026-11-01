package billeteraclara

import java.time.LocalDate

case class Billetera(usuario: Usuario, transacciones: List[Transaccion]):

  // copy crea otra Billetera; la lista original permanece intacta.
  def registrar(transaccion: Transaccion): Either[String, Billetera] =
    if transaccion.descripcion.trim.isEmpty then Left("Escribe una descripción.")
    else if transaccion.monto <= 0 then Left("El monto debe ser mayor a cero.")
    else if transaccion.monto.scale > 2 then Left("Usa como máximo dos decimales.")
    else if transacciones.exists(_.id == transaccion.id) then Left("La transacción ya existe.")
    else if transaccion.tipo == Gasto && !Categoria.deGasto.contains(transaccion.categoria) then
      Left("Selecciona una categoría de gasto.")
    else if transaccion.tipo == Ingreso && !Categoria.deIngreso.contains(transaccion.categoria) then
      Left("Selecciona una categoría de ingreso.")
    else Right(copy(transacciones = transaccion :: transacciones))

  def eliminar(id: String): Billetera =
    copy(transacciones = transacciones.filterNot(_.id == id))

  // Esta función de orden superior permite reutilizar cualquier criterio de búsqueda.
  def buscar(criterio: Transaccion => Boolean): List[Transaccion] =
    transacciones.filter(criterio)

  def saldo: BigDecimal =
    transacciones.foldLeft(BigDecimal(0))((total, tx) => total + tx.montoConSigno)

  def ingresos: BigDecimal =
    Calculos.sumarPor(buscar(_.tipo == Ingreso))(_.monto)

  def gastos: BigDecimal =
    Calculos.sumarPor(buscar(_.tipo == Gasto))(_.monto)

  def totalesPorCategoria: List[(Categoria, BigDecimal)] =
    Categoria.deGasto.map(categoria =>
      categoria -> Calculos.sumarPor(buscar(tx =>
        tx.tipo == Gasto && tx.categoria == categoria))(_.monto)
    )

  def categoriasActivas: List[(Categoria, BigDecimal)] =
    totalesPorCategoria.flatMap(par => if par._2 > 0 then Some(par) else None)

  def categoriaPrincipal: Option[(Categoria, BigDecimal)] =
    categoriasActivas.sortBy(par => -par._2).headOption

  def diagnostico: Diagnostico =
    val total = gastos
    val ocio = Calculos.sumarPor(buscar(tx => tx.tipo == Gasto && tx.categoria == Ocio))(_.monto)
    val porcentaje = if total == 0 then BigDecimal(0) else ocio * 100 / total
    val estado = if porcentaje > 20 then Alerta else Saludable
    Diagnostico(estado, ocio, total, porcentaje)

  // El gasto hipotético solo existe en una nueva Billetera temporal.
  def simularCompra(monto: BigDecimal, categoria: Categoria): Either[String, Simulacion] =
    if monto <= 0 then Left("El monto debe ser mayor a cero.")
    else if monto.scale > 2 then Left("Usa como máximo dos decimales.")
    else if !Categoria.deGasto.contains(categoria) then Left("Selecciona una categoría de gasto.")
    else
      val hipotetica = Transaccion(
        "simulacion", "Compra simulada", monto, LocalDate.of(2000, 1, 1), categoria, Gasto
      )
      val proyectada = copy(transacciones = hipotetica :: transacciones)
      Right(Simulacion(saldo, proyectada.saldo, diagnostico, proyectada.diagnostico,
        proyectada.saldo >= 0))

object Billetera:
  def vacia(usuario: Usuario): Billetera = Billetera(usuario, List.empty)

object Calculos:
  // A es genérico: la misma función suma valores extraídos de cualquier lista.
  def sumarPor[A](datos: List[A])(valor: A => BigDecimal): BigDecimal =
    datos.foldLeft(BigDecimal(0))((total, elemento) => total + valor(elemento))
