package billeteraclara

import scala.util.Try

// Adaptador de texto de la interfaz. El modelo recibe únicamente centavos enteros.
// La conversión decimal exacta y Try se mantienen fuera de la lógica del negocio.
object EntradaInterfaz:
  // Convierte un texto del formulario a centavos exactos o informa un error de formato.
  // La positividad y el límite de sesión se validan posteriormente en el modelo.
  def parsearMonto(texto: String): Resultado[Long] =
    // Acepta coma o punto decimal y elimina espacios de los extremos.
    val normalizado = texto.trim.replace(',', '.')
    // Admite dígitos y hasta dos decimales; no redondea ni acepta separadores de miles.
    if !normalizado.matches("[0-9]+(\\.[0-9]{1,2})?") then
      Error("Introduce un monto sin separadores de miles y con hasta dos decimales (ej.: 25,50).")
    else
      // BigDecimal evita errores binarios: desplaza dos decimales y exige un Long exacto.
      // Try captura fallos de conversión o rango, sin lanzar una excepción hacia la ventana.
      Try(new java.math.BigDecimal(normalizado).movePointRight(2).longValueExact()).toOption match
        case Some(centavos) => Exito(centavos)
        case None => Error("El monto supera el rango numérico admitido.")
