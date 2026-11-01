# Billetera Clara

Mini sistema de finanzas personales en **Scala 3**. Registra ingresos y gastos, calcula el saldo, permite buscar movimientos, audita el porcentaje de ocio y simula compras sin modificar los datos. La interfaz está hecha con **Swing**, que viene con la JVM.

## Requisitos

- Java 17 o superior.
- Scala CLI con Scala 3.9.0, o sbt 2.0.9. La primera ejecución puede descargar Scala.

## Ejecutar

Descarga y descomprime `BilleteraClara-Publicar.zip`. Desde la carpeta `BilleteraClara-Publicar`:

```bash
scala run . --server=false --main-class billeteraclara.Main
```

También puedes usar:

```bash
sbt run
```

El archivo comprimido contiene el código fuente completo, la configuración de sbt y las verificaciones. La aplicación abre con seis movimientos de demostración. El botón **Empezar en blanco** limpia esos datos para registrar los tuyos. Los datos viven solo en memoria: al cerrar y abrir la aplicación se restablece el ejemplo.

## Verificar el modelo

```bash
scala run src/main/scala src/test/scala --server=false --main-class billeteraclara.Verificacion
```

Debe aparecer `Todas las comprobaciones del modelo pasaron.`

## Estructura

```text
BilleteraClara/
├── build.sbt
├── project/build.properties
├── src/main/scala/billeteraclara/
│   ├── Modelo.scala       # tipos del dominio
│   ├── Billetera.scala     # operaciones funcionales
│   ├── DatosDemo.scala     # datos para la presentación
│   └── Main.scala          # interfaz y entrada del programa
├── src/test/scala/billeteraclara/
│   └── Verificacion.scala  # comprobaciones del modelo
└── GUIA_DE_ESTUDIO.md      # explicación para estudiar y exponer
```

## Ejemplo breve

Con los datos de demostración, los ingresos son **Bs 3150.00**, los gastos **Bs 567.30** y el saldo **Bs 2582.70**. Si simulas un gasto de **Bs 100.00** en ocio, el diagnóstico pasa a **Alerta** porque ocio supera el 20% de los gastos. La compra simulada no se agrega a la lista.
