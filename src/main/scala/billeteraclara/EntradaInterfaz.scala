package billeteraclara

import scala.util.Try

// Adaptador de texto de la interfaz. El modelo recibe únicamente centavos enteros.
// La conversión decimal exacta y Try se mantienen fuera de la lógica del negocio.
object EntradaInterfaz:
  def parsearMonto(texto: String): Resultado[Long] =
    val normalizado = texto.trim.replace(',', '.')
    if !normalizado.matches("[0-9]+(\\.[0-9]{1,2})?") then
      Error("Introduce un monto sin separadores de miles y con hasta dos decimales (ej.: 25,50).")
    else
      Try(new java.math.BigDecimal(normalizado).movePointRight(2).longValueExact()).toOption match
        case Some(centavos) => Exito(centavos)
        case None => Error("El monto supera el rango numérico admitido.")
