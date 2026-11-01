package billeteraclara

import java.time.LocalDate

// Comprobaciones ejecutables sin librerías de pruebas externas.
object Verificacion:
  def main(args: Array[String]): Unit =
    val vacia = Billetera.vacia(Usuario("1", "Ana"))
    val sueldo = Transaccion("1", "Sueldo", 100.00,
      LocalDate.of(2026, 1, 1), Salario, Ingreso)
    val comida = Transaccion("2", "Comida", 80.00,
      LocalDate.of(2026, 1, 2), Alimentacion, Gasto)
    val ocio = Transaccion("3", "Cine", 20.00,
      LocalDate.of(2026, 1, 3), Ocio, Gasto)

    val conSueldo = vacia.registrar(sueldo)
    val conComida = conSueldo.registrar(comida)
    val completa = conComida.registrar(ocio)

    assert(vacia.transacciones.isEmpty, "Registrar no debe modificar la billetera original")
    assert(completa.saldo == 0.0, "100 - 80 - 20 = 0")
    assert(completa.buscar(_.tipo == Gasto).length == 2)
    assert(completa.diagnostico.estado == Saludable, "20% exacto es saludable")
    assert(completa.diagnostico.porcentajeOcio == 20.0)
    assert(completa.totalesPorCategoria.find(_._1 == Ocio).get._2 == 20.0)

    val simulada = completa.simularCompra(1.00, Ocio)
    assert(simulada.saldoProyectado == -1.00)
    assert(simulada.diagnosticoProyectado.estado == Alerta)
    assert(!simulada.compraPosible)
    assert(completa.transacciones.length == 3, "Simular no debe registrar la compra")

    assert(completa.eliminar("2").transacciones.length == 2)
    assert(completa.transacciones.length == 3, "Eliminar también crea una nueva billetera")

    println("Todas las comprobaciones del modelo pasaron.")
