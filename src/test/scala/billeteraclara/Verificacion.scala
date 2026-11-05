package billeteraclara

// Pruebas ejecutables sin dependencias externas. Los fallos terminan con una excepción.
object Verificacion:
  private def comprobar(condicion: Boolean, mensaje: String): Unit =
    if !condicion then throw new RuntimeException(mensaje)

  private def obtener[A](resultado: Resultado[A]): A = resultado match
    case Exito(valor) => valor
    case Error(mensaje) => throw new RuntimeException(s"Se esperaba éxito: $mensaje")

  private def esError[A](resultado: Resultado[A]): Boolean = resultado match
    case Error(_) => true
    case Exito(_) => false

  private def comprobarEntradas(entradas: List[String]): Unit = entradas match
    case Nil => ()
    case cabeza :: cola =>
      comprobar(esError(EntradaInterfaz.parsearMonto(cabeza)), s"Rechazar entrada inválida: $cabeza")
      comprobarEntradas(cola)

  def main(args: Array[String]): Unit =
    // Extremos expresados con literales y aritmética, sin APIs adicionales.
    val mayorLong: Long = 9223372036854775807L
    val menorLong: Long = -mayorLong - 1L
    val vacia = Billetera.vacia(Usuario("1", "Ana"))
    val sueldo = Transaccion("1", "Sueldo", 10000L, Fecha(1, 1, 2026), Salario, Ingreso)
    val comida = Transaccion("2", "Comida", 8000L, Fecha(2, 1, 2026), Alimentacion, Gasto)
    val ocio = Transaccion("3", "Cine", 2000L, Fecha(3, 1, 2026), Ocio, Gasto)
    val conSueldo = obtener(vacia.registrar(sueldo))
    val conComida = obtener(conSueldo.registrar(comida))
    val completa = obtener(conComida.registrar(ocio))

    comprobar(vacia.transacciones.isEmpty, "Registrar debe conservar la billetera original")
    comprobar(conSueldo.transacciones.length == 1, "Las versiones anteriores no cambian")
    comprobar(completa.saldo == 0L, "100 - 80 - 20 = 0 bolivianos")
    comprobar(completa.ingresos == 10000L && completa.gastos == 10000L, "Totales en centavos")
    comprobar(completa.buscar(_.tipo == Gasto).length == 2, "Búsqueda por tipo")
    comprobar(completa.buscar(_.categoria == Ocio) == List(ocio), "Búsqueda por categoría")
    comprobar(completa.buscar(_.montoCentavos > 20000L).isEmpty, "Búsqueda sin coincidencias")
    comprobar(completa.diagnostico.estado == Saludable, "20% exacto debe ser saludable")
    comprobar(completa.diagnostico.porcentajeOcio == 20.0, "Porcentaje exacto")
    val totalesOcio = completa.totalesPorCategoria.filter(par => par match
      case (categoria, _) => categoria == Ocio
    )
    comprobar(totalesOcio == List((Ocio, 2000L)), "Total de ocio")
    val totalCategorias = completa.totalesPorCategoria.foldLeft(0L)((suma, par) => par match
      case (_, importe) => suma + importe
    )
    comprobar(totalCategorias == completa.gastos,
      "Todas las categorías deben sumar el gasto total")
    comprobar(vacia.saldo == 0L && vacia.diagnostico.porcentajeOcio == 0.0,
      "La billetera vacía no divide entre cero")
    comprobar(vacia.diagnostico.estado == Saludable, "Sin gastos no hay alerta de ocio")

    comprobar(conSueldo.ingresos == 10000L && conSueldo.diagnostico.totalGastos == 0L,
      "Desde blanco, un ingreso actualiza los ingresos sin inventar gastos")
    comprobar(conComida.diagnostico.totalGastos == 8000L &&
      conComida.diagnostico.gastoOcio == 0L &&
      conComida.diagnostico.porcentajeOcio == 0.0,
      "Un gasto de Alimentación aumenta el gasto total, no el de Ocio")
    comprobar(completa.diagnostico.totalGastos == 10000L &&
      completa.diagnostico.gastoOcio == 2000L,
      "Un gasto de Ocio actualiza ambos importes de la auditoría")

    val simulada = obtener(completa.simularCompra(1L, Ocio))
    comprobar(simulada.saldoActual == 0L && simulada.saldoProyectado == -1L,
      "Compra sin saldo debe proyectar un saldo negativo")
    comprobar(simulada.diagnosticoActual == completa.diagnostico, "Diagnóstico actual de la simulación")
    comprobar(simulada.diagnosticoProyectado.estado == Alerta, "Un centavo sobre el umbral activa alerta")
    comprobar(!simulada.compraPosible, "Compra sin fondos")
    comprobar(completa.transacciones == List(ocio, comida, sueldo), "Simular no debe registrar nada")
    val sinOcio = obtener(completa.simularCompra(1L, Hogar))
    comprobar(sinOcio.diagnosticoProyectado.gastoOcio == 2000L,
      "Comprar fuera de ocio no aumenta el gasto de ocio")
    comprobar(sinOcio.diagnosticoProyectado.estado == Saludable, "Otra categoría reduce la proporción de ocio")

    // Regresión: 0,30 - 0,10 - 0,20 tiene que ser exactamente cero.
    val centavos = obtener(obtener(vacia.registrar(sueldo.copy(montoCentavos = 30L)))
      .registrar(comida.copy(montoCentavos = 10L)))
    val compraExacta = obtener(centavos.simularCompra(20L, Alimentacion))
    comprobar(compraExacta.saldoProyectado == 0L && compraExacta.compraPosible,
      "Una compra que consume exactamente el saldo debe ser posible")
    comprobar(centavos.saldo == 20L, "La simulación no altera el saldo actual")
    comprobar(obtener(centavos.simularCompra(19L, Alimentacion)).saldoProyectado == 1L,
      "Conservar un centavo de cambio")
    comprobar(!obtener(centavos.simularCompra(21L, Alimentacion)).compraPosible,
      "Un centavo de déficit debe ser insuficiente")

    comprobar(esError(vacia.registrar(sueldo.copy(montoCentavos = 0L))), "Rechazar monto cero")
    comprobar(esError(vacia.registrar(sueldo.copy(montoCentavos = -1L))), "Rechazar monto negativo")
    comprobar(esError(vacia.registrar(sueldo.copy(montoCentavos = menorLong))), "Rechazar mínimo Long")
    comprobar(esError(vacia.registrar(sueldo.copy(montoCentavos = mayorLong))), "Rechazar máximo Long")
    comprobar(esError(vacia.registrar(sueldo.copy(id = "  "))), "Rechazar identificador vacío")
    comprobar(esError(vacia.registrar(sueldo.copy(descripcion = "  "))), "Rechazar descripción vacía")
    comprobar(esError(conSueldo.registrar(sueldo)), "Rechazar identificador duplicado")
    comprobar(esError(vacia.registrar(comida.copy(categoria = Salario))), "Rechazar Salario como gasto")
    comprobar(esError(vacia.registrar(sueldo.copy(categoria = Alimentacion))), "Rechazar Alimentación como ingreso")
    comprobar(esError(vacia.registrar(sueldo.copy(fecha = Fecha(31, 4, 2026)))), "Validar fecha al registrar")
    comprobar(esError(vacia.simularCompra(0L, Ocio)), "Rechazar simulación cero")
    comprobar(esError(vacia.simularCompra(-10L, Ocio)), "Rechazar simulación negativa")
    comprobar(esError(vacia.simularCompra(mayorLong, Ocio)), "Rechazar simulación fuera de rango")
    comprobar(esError(vacia.simularCompra(10L, Salario)), "Rechazar categoría de ingreso en simulación")
    comprobar(vacia.transacciones.isEmpty && conSueldo.transacciones == List(sueldo),
      "Los rechazos deben conservar el estado original")

    val limite = obtener(vacia.registrar(sueldo.copy(montoCentavos = Dinero.limiteCentavos)))
    comprobar(limite.saldo == Dinero.limiteCentavos, "Aceptar el límite exacto")
    comprobar(esError(limite.registrar(comida.copy(montoCentavos = 1L))), "Rechazar exceso acumulado")
    comprobar(esError(limite.simularCompra(1L, Ocio)), "Aplicar límite acumulado a la proyección")
    val maximoOcio = obtener(vacia.registrar(ocio.copy(montoCentavos = Dinero.limiteCentavos)))
    comprobar(maximoOcio.diagnostico.estado == Alerta && maximoOcio.diagnostico.porcentajeOcio == 100.0,
      "El cálculo del umbral no debe desbordarse con el máximo permitido")

    val eliminada = completa.eliminar("2")
    comprobar(eliminada.transacciones == List(ocio, sueldo), "Eliminar únicamente el identificador indicado")
    comprobar(completa.transacciones.length == 3, "Eliminar debe conservar la billetera original")
    comprobar(completa.eliminar("inexistente").transacciones == completa.transacciones,
      "Eliminar un identificador ausente conserva los movimientos")
    comprobar(obtener(eliminada.registrar(comida)).transacciones.length == 3,
      "Un identificador eliminado puede registrarse de nuevo")
    comprobar(limite.eliminar("1").ingresos == 0L, "Eliminar libera el límite de importes")

    comprobar(Fecha(29, 2, 2024).esValida && Fecha(29, 2, 2000).esValida, "Años bisiestos")
    comprobar(!Fecha(29, 2, 2026).esValida && !Fecha(29, 2, 1900).esValida, "Años no bisiestos")
    comprobar(!Fecha(0, 1, 2026).esValida && !Fecha(32, 1, 2026).esValida, "Días fuera de rango")
    comprobar(!Fecha(1, 0, 2026).esValida && !Fecha(1, 13, 2026).esValida, "Meses fuera de rango")
    comprobar(!Fecha(1, 1, 0).esValida && !Fecha(1, 1, 10000).esValida, "Años fuera de rango")
    comprobar(Fecha(31, 12, 9999).esValida, "Fecha máxima")

    // Casos de presentación: tipos compatibles y saldo negativo permitido.
    val ingresoOtros = obtener(vacia.registrar(sueldo.copy(categoria = Otros)))
    comprobar(ingresoOtros.ingresos == 10000L, "Otros también es una categoría de ingreso")
    val gastoSinFondos = obtener(vacia.registrar(comida))
    comprobar(gastoSinFondos.saldo == -8000L, "Registrar gastos no exige saldo previo")
    comprobar(!obtener(gastoSinFondos.simularCompra(1L, Otros)).compraPosible,
      "La simulación informa el déficit de una billetera con saldo negativo")
    comprobar(esError(vacia.registrar(sueldo.copy(id = ""))), "Rechazar identificador sin caracteres")
    comprobar(esError(vacia.registrar(sueldo.copy(descripcion = "\t\n\r "))),
      "Rechazar descripción formada por espacios y saltos de línea")
    comprobar(obtener(limite.eliminar("1").registrar(comida)).gastos == 8000L,
      "Después de eliminar se puede volver a utilizar el margen liberado")
    comprobar(Fecha(30, 4, 2026).esValida && !Fecha(31, 4, 2026).esValida,
      "Abril termina el día 30")
    comprobar(Fecha(28, 2, 1900).esValida && Fecha(1, 1, 1).esValida,
      "Aceptar el fin de febrero no bisiesto y el año mínimo")

    // Cada categoría admitida debe registrarse y acumularse, no únicamente Ocio.
    val todasGastos = Categoria.deGasto.foldLeft(vacia)((billetera, cat) =>
      obtener(billetera.registrar(Transaccion(cat.nombre, cat.nombre, 100L,
        Fecha(1, 1, 2026), cat, Gasto)))
    )
    comprobar(todasGastos.transacciones.length == Categoria.deGasto.length &&
      todasGastos.gastos == 600L && todasGastos.saldo == -600L,
      "Registrar las seis categorías de gasto")
    comprobar(todasGastos.diagnostico.gastoOcio == 100L,
      "Solo Ocio contribuye al numerador de la auditoría; todos contribuyen al gasto total")
    val sumasCategorias = todasGastos.totalesPorCategoria.filter(par => par match
      case (_, total) => total == 100L
    )
    comprobar(sumasCategorias.length == 6, "Cada categoría de gasto tiene su propio total")
    val todosIngresos = Categoria.deIngreso.foldLeft(vacia)((billetera, cat) =>
      obtener(billetera.registrar(Transaccion(cat.nombre, cat.nombre, 100L,
        Fecha(1, 1, 2026), cat, Ingreso)))
    )
    comprobar(todosIngresos.ingresos == 200L && todosIngresos.gastos == 0L,
      "Registrar Salario y Otros como ingresos")
    val proyecciones = Categoria.deGasto.map(cat => obtener(todasGastos.simularCompra(100L, cat)))
    comprobar(proyecciones.filter(s => s.saldoProyectado == -700L &&
      s.diagnosticoProyectado.totalGastos == 700L).length == 6,
      "Simular todas las categorías sin omitir gastos")
    comprobar(todasGastos.transacciones.length == 6 && todasGastos.gastos == 600L,
      "Las simulaciones por categoría conservan los registros")

    // El adaptador de la interfaz también se comprueba sin abrir una ventana.
    comprobar(obtener(EntradaInterfaz.parsearMonto("0.30")) == 30L, "Convertir decimales exactos")
    comprobar(obtener(EntradaInterfaz.parsearMonto(" 25,50 ")) == 2550L, "Aceptar coma decimal y espacios externos")
    comprobar(obtener(EntradaInterfaz.parsearMonto("12")) == 1200L, "Aceptar importe entero")
    comprobar(obtener(EntradaInterfaz.parsearMonto("12.5")) == 1250L, "Aceptar un decimal")
    comprobar(obtener(EntradaInterfaz.parsearMonto("10000000000.00")) == Dinero.limiteCentavos,
      "Convertir el límite sin pérdida de precisión")
    val entradasInvalidas = List("", " ", "NaN", "Infinity", "1e3", "-1", "0.001", "1,000", "1.234,56", "texto",
      "999999999999999999999999999999999999")
    comprobarEntradas(entradasInvalidas)

    val demo = obtener(DatosDemo.ejemplo)
    comprobar(demo.ingresos == 315000L && demo.gastos == 56730L && demo.saldo == 258270L,
      "Los datos de demostración deben coincidir con la documentación")
    comprobar(demo.diagnostico.gastoOcio == 4500L && demo.diagnostico.totalGastos == 56730L,
      "La auditoría de demostración usa exactamente los importes documentados")
    comprobar(demo.transacciones.length == 6 && demo.diagnostico.estado == Saludable, "Ejemplo reproducible")
    println("Todas las comprobaciones del modelo y del adaptador de interfaz pasaron.")
