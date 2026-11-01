package billeteraclara

import java.time.LocalDate

case class Billetera(usuario: Usuario, transacciones: List[Transaccion]):
  // Cada operación devuelve una billetera nueva: la original no cambia.
  def registrar(transaccion: Transaccion): Billetera =
    copy(transacciones = transaccion :: transacciones)

  def eliminar(id: String): Billetera =
    copy(transacciones = transacciones.filterNot(_.id == id))

  // Recibe una función como parámetro: es una función de orden superior.
  def buscar(criterio: Transaccion => Boolean): List[Transaccion] =
    transacciones.filter(criterio)

  def saldo: Double =
    transacciones.foldLeft(0.0)((total, tx) => total + tx.montoConSigno)

  def ingresos: Double =
    buscar(_.tipo == Ingreso).foldLeft(0.0)((total, tx) => total + tx.monto)

  def gastos: Double =
    buscar(_.tipo == Gasto).foldLeft(0.0)((total, tx) => total + tx.monto)

  def totalesPorCategoria: List[(Categoria, Double)] =
    Categoria.deGasto.map(categoria =>
      categoria -> buscar(tx => tx.tipo == Gasto && tx.categoria == categoria)
        .foldLeft(0.0)((total, tx) => total + tx.monto)
    )

  def diagnostico: Diagnostico =
    val total = gastos
    val ocio = buscar(tx => tx.tipo == Gasto && tx.categoria == Ocio)
      .foldLeft(0.0)((suma, tx) => suma + tx.monto)
    val porcentaje = if total == 0 then 0.0 else ocio * 100 / total
    val estado = if porcentaje > 20 then Alerta else Saludable
    Diagnostico(estado, ocio, total, porcentaje)

  // La compra solo se agrega a una copia temporal.
  def simularCompra(monto: Double, categoria: Categoria): Simulacion =
    val compra = Transaccion("simulacion", "Compra simulada", monto,
      LocalDate.of(2000, 1, 1), categoria, Gasto)
    val proyectada = registrar(compra)
    Simulacion(saldo, proyectada.saldo, diagnostico, proyectada.diagnostico,
      proyectada.saldo >= 0)

object Billetera:
  def vacia(usuario: Usuario): Billetera = Billetera(usuario, List.empty)
