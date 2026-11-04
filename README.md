# Billetera Clara

Proyecto final de Programación Funcional en Scala 3, de Israel Vargas y Christian Campero. Mini sistema de finanzas personales con interfaz Swing: registra ingresos y gastos, calcula el saldo, filtra movimientos, audita el porcentaje de ocio y simula compras sin modificar los registros.

## Ejecutar

Requiere Java 17 o superior y Scala CLI. Desde la raíz del repositorio:

```bash
scala run src/main/scala --server=false --jvm system --main-class billeteraclara.Main
```

También se puede usar `sbt run`. La primera ejecución puede descargar Scala y las herramientas de compilación; luego utiliza la caché local. `--jvm system` usa el Java instalado.

Para usar el paquete de entrega, descarga [BilleteraClara-Publicar.zip](BilleteraClara-Publicar.zip), descomprímelo y ejecuta el mismo comando desde la carpeta `BilleteraClara-Publicar`.

## Verificar

```bash
scala run src/main/scala src/test/scala --server=false --jvm system --main-class billeteraclara.Verificacion
```

Con sbt: `sbt "Test / runMain billeteraclara.Verificacion"`.

El resultado esperado es:

```text
Todas las comprobaciones del modelo y del adaptador de interfaz pasaron.
```

Las comprobaciones cubren inmutabilidad, saldo exacto en centavos, umbral de ocio, simulaciones, búsquedas, eliminación, validaciones del modelo, fechas, límites numéricos y conversión de entradas. No requieren librerías de pruebas externas.

## Uso y ejemplo inicial

La ventana empieza con seis movimientos de demostración fechados del 1 al 6 de enero de 2026:

| Ingresos | Gastos | Saldo | Ocio |
| --- | --- | --- | --- |
| Bs 3.150,00 | Bs 567,30 | Bs 2.582,70 | Bs 45,00 (aprox. 7,9%) |

Introduce importes en bolivianos, sin separadores de miles y con hasta dos decimales: `25`, `25.50` o `25,50`. Cada registro nuevo usa la fecha local del equipo. Los cálculos del modelo utilizan centavos enteros: `2550L` representa Bs 25,50.

La auditoría marca **Alerta** cuando el ocio supera el 20% del gasto total. El 20% exacto es **Saludable**. Este diagnóstico describe únicamente esa regla; la simulación indica por separado si alcanza el saldo.

El botón **Empezar en blanco** quita los movimientos de la sesión. Los datos permanecen en memoria y se pierden al cerrar la aplicación. No se utiliza una base de datos.

## Documento del proyecto

[Descripción completa, decisiones de diseño, relación con el curso y ejemplos de ejecución](docs/ProyectoFinal.md).

El modelo se encuentra en `Modelo.scala` y `Billetera.scala`; `DatosDemo.scala` genera un ejemplo fijo. Swing y la adaptación de texto, fechas del sistema y formato visual se encuentran en `Main.scala` y `EntradaInterfaz.scala`.
