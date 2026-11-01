package billeteraclara

import java.time.LocalDate

object DatosDemo:
  def ejemplo: Billetera =
    val hoy = LocalDate.now()
    Billetera(
      Usuario("u-01", "Mi billetera"),
      List(
        Transaccion("t-06", "Café y libros", 45.00, hoy, Ocio, Gasto),
        Transaccion("t-05", "Transporte urbano", 18.50, hoy.minusDays(1), Transporte, Gasto),
        Transaccion("t-04", "Proyecto freelance", 650.00, hoy.minusDays(2), Otros, Ingreso),
        Transaccion("t-03", "Supermercado", 123.80, hoy.minusDays(3), Alimentacion, Gasto),
        Transaccion("t-02", "Alquiler", 380.00, hoy.minusDays(4), Hogar, Gasto),
        Transaccion("t-01", "Salario mensual", 2500.00, hoy.minusDays(5), Salario, Ingreso)
      )
    )
