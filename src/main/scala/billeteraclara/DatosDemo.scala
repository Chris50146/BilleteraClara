package billeteraclara

import java.time.LocalDate

object DatosDemo:
  def ejemplo: Billetera =
    val hoy = LocalDate.now()
    Billetera(
      Usuario("u-01", "Mi billetera"),
      List(
        Transaccion("t-06", "Café y libros", BigDecimal("45.00"), hoy, Ocio, Gasto),
        Transaccion("t-05", "Transporte urbano", BigDecimal("18.50"), hoy.minusDays(1), Transporte, Gasto),
        Transaccion("t-04", "Proyecto freelance", BigDecimal("650.00"), hoy.minusDays(2), Otros, Ingreso),
        Transaccion("t-03", "Supermercado", BigDecimal("123.80"), hoy.minusDays(3), Alimentacion, Gasto),
        Transaccion("t-02", "Alquiler", BigDecimal("380.00"), hoy.minusDays(4), Hogar, Gasto),
        Transaccion("t-01", "Salario mensual", BigDecimal("2500.00"), hoy.minusDays(5), Salario, Ingreso)
      )
    )
