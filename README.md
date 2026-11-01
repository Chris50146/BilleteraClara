# Billetera Clara

Mini sistema de finanzas personales en Scala 3 con interfaz Swing. Permite registrar ingresos y gastos, calcular el saldo, buscar movimientos, auditar el porcentaje de ocio y simular compras sin alterar los datos.

## Descargar y ejecutar

Descarga [BilleteraClara-Publicar.zip](BilleteraClara-Publicar.zip), descomprímelo y entra a la carpeta `BilleteraClara-Publicar`. Requiere Java 17 o superior y Scala CLI.

```bash
scala run . --server=false --main-class billeteraclara.Main
```

También puedes ejecutar `sbt run`. La primera ejecución puede descargar dependencias.

## Verificar el modelo

```bash
scala run src/main/scala src/test/scala --server=false --main-class billeteraclara.Verificacion
```

El proyecto incluye código fuente, configuración de compilación y comprobaciones. Los movimientos iniciales son de demostración; puedes pulsar **Empezar en blanco**. Los datos se mantienen en memoria durante la sesión.
