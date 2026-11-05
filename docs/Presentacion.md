# Guía de presentación de YourWallet

**Integrantes:** Israel Vargas y Christian Campero.

## Preparación

Usar Java 17 o superior **con soporte gráfico** y Scala CLI. La versión de Scala está fijada en 3.9.0. Ejecutar desde la raíz del proyecto o del paquete descomprimido:

```sh
scala run src/main/scala src/test/scala --server=false --jvm system --main-class billeteraclara.Verificacion
scala run src/main/scala --server=false --jvm system --main-class billeteraclara.Main
```

Las comprobaciones deben terminar con `Todas las comprobaciones del modelo y del adaptador de interfaz pasaron.` Abrir la ventana antes de exponer para descargar y compilar lo necesario. La primera ejecución necesita Internet; las siguientes reutilizan la caché. Un Java instalado solo como `headless` permite verificar el modelo, pero no abrir Swing. La pantalla puede necesitar desplazamiento vertical para mostrar el gráfico, la auditoría y el simulador.

En el entorno cloud preparado, añadir primero `/workspace/.local/bin` al PATH. Una pantalla virtual o un escritorio activo y un Java gráfico son requisitos adicionales para Swing; la existencia de Scala CLI no los sustituye.

## Demostración sugerida (6–8 minutos)

1. **Problema y alcance.** Explicar que el sistema registra movimientos personales, calcula totales y permite evaluar una compra. No utiliza base de datos; al cerrar se pierde la sesión.
2. **Ejemplo inicial.** Mostrar seis movimientos: ingresos Bs 3.150,00, gastos Bs 567,30 y saldo Bs 2.582,70. El ocio suma Bs 45,00, aproximadamente 7,9%, y es Saludable. La ventana muestra los importes con punto decimal, sin separador de miles.
3. **Búsqueda.** Escribir `ocio` para mostrar Café y libros. Borrar la búsqueda y elegir Ingresos: deben aparecer dos registros. Restablecer Todos. El formulario filtra usando `buscar(criterio: Transaccion => Boolean)`.
4. **Simulación.** Abrir la pestaña Simular compra. Simular Bs 100,00 en Ocio: el saldo proyectado es Bs 2.482,70 y el ocio proyectado es aproximadamente 21,7%, Alerta. Hay saldo suficiente y, a la vez, alerta de ocio: son resultados independientes. Los seis movimientos y el saldo actual permanecen iguales.
5. **Registro y eliminación.** Volver a la pestaña Registrar. Pulsar Guardar movimiento para agregar un gasto de Alimentación con descripción `Compra de prueba` y monto `25,50`: quedan siete movimientos y saldo Bs 2.557,20. Eliminarlo con × recupera los seis movimientos y Bs 2.582,70. Explicar que el modelo devuelve una nueva billetera en ambas operaciones.
6. **Exactitud monetaria.** Pulsar Empezar en blanco y confirmar el vaciado. Registrar un ingreso de Salario por `0,30` y un gasto de Alimentación por `0,10`. Simular `0,20` en Alimentación: saldo proyectado Bs 0,00 y saldo suficiente, sin registrar la compra. Un intento por `0,21` debe indicar saldo insuficiente.
7. **Validación.** Intentar registrar `0` o un importe con tres decimales: se informa el error y no aparece otro movimiento. Las fechas, identificadores, categorías y límites también se verifican en las comprobaciones ejecutables.
8. **Código y cierre.** Mostrar la separación entre modelo, datos de demostración e interfaz, y relacionarla con los requisitos de la tabla siguiente.

## Correspondencia con los requisitos obligatorios

| Requisito | Evidencia para explicar |
| --- | --- |
| Clases, objetos y encapsulamiento | `Modelo.scala`: entidades; `Billetera.scala`: constructor privado y objeto acompañante. |
| Inmutabilidad | Atributos `val`, `List[Transaccion]` y nuevas instancias al registrar o eliminar. |
| Listas y procesamiento funcional | `saldo` con `foldLeft`, `buscar` con `filter` y `totalesPorCategoria` con `map`. No se necesitan todas las operaciones citadas en el enunciado. |
| Función de orden superior | `buscar` recibe `Transaccion => Boolean`; permite cambiar el criterio sin duplicar el recorrido. |
| Case classes y pattern matching | `Transaccion`, `Fecha`, `Diagnostico`, `Simulacion`, `Exito` y `Error`; `match` sobre tipos y resultados. |
| Jerarquías y genéricos | `TipoTransaccion`, `Categoria`, `EstadoDiagnostico` y `Resultado[+A]`. |
| Abstracciones compartidas | `validarMonto` para registro y simulación; `evaluarGastos` para diagnóstico actual y proyectado. |
| Interfaz sencilla conectada al modelo | Swing en `Main.scala`; conversión de entradas en `EntradaInterfaz.scala`. |
| Documento y ejemplos | `docs/ProyectoFinal.md`, esta guía y `README.md`. |

## Decisiones que deben poder defenderse

- **Long en centavos:** evita residuos de punto flotante. `Double` solo presenta porcentajes; la decisión de alerta usa enteros.
- **Regla del ocio:** Alerta cuando supera el 20% del gasto, no del ingreso; exactamente 20% es Saludable. Sin gastos es cero.
- **Simulación pura:** calcula nuevos valores y no introduce transacciones ficticias.
- **Constructor privado:** evita construir una billetera con una lista que salte el registro validado.
- **Límite técnico:** ingresos más gastos admiten hasta `1000000000000L` centavos para asegurar sumas y productos seguros. No es un presupuesto del usuario.
- **Excepción de interfaz:** Swing, eventos, reloj, identificadores y adaptación decimal pertenecen a la interfaz autorizada. El modelo conserva las reglas y sus validaciones.
- **Respaldo académico:** basta la mención de un recurso en el material. `foldLeft` está mencionado en el curso y en el enunciado; se pueden combinar los recursos para construir algoritmos propios.

## Archivos para entregar

Entregar `YourWallet-Publicar.zip`, que contiene código, comprobaciones, configuración de compilación, README, documento del sistema y esta guía. El material del curso, credenciales, cachés y archivos del editor quedan fuera del paquete. La propuesta original se conserva como antecedente en el repositorio; las decisiones vigentes están explicadas en el documento del sistema.
