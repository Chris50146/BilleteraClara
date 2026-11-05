package billeteraclara

// Datos fijos: el ejemplo no consulta el reloj y siempre produce el mismo resultado.
object DatosDemo:
  // Devuelve el ejemplo validado, o un error si algún dato fijo incumpliera las reglas.
  def ejemplo: Resultado[Billetera] =
    val movimientos = List(
      Transaccion("t-01", "Salario mensual", 250000L, Fecha(1, 1, 2026), Salario, Ingreso),
      Transaccion("t-02", "Alquiler", 38000L, Fecha(2, 1, 2026), Hogar, Gasto),
      Transaccion("t-03", "Supermercado", 12380L, Fecha(3, 1, 2026), Alimentacion, Gasto),
      Transaccion("t-04", "Proyecto freelance", 65000L, Fecha(4, 1, 2026), Otros, Ingreso),
      Transaccion("t-05", "Transporte urbano", 1850L, Fecha(5, 1, 2026), Transporte, Gasto),
      Transaccion("t-06", "Café y libros", 4500L, Fecha(6, 1, 2026), Ocio, Gasto)
    )
    // El acumulador lleva una billetera aceptada o el error previo.
    // Cada movimiento pasa por registrar; después de un Error no se incorporan más datos.
    movimientos.foldLeft[Resultado[Billetera]](Exito(Billetera.vacia(Usuario("u-01", "Mi billetera"))))(
      (resultado, movimiento) => resultado match
        case Exito(billetera) => billetera.registrar(movimiento)
        case Error(mensaje) => Error(mensaje)
    )
