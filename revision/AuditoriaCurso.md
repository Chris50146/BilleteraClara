# Auditoría de correspondencia con el repositorio del curso

Actualizada el 4 de noviembre de 2026 tras la aclaración del usuario: basta que un recurso esté mencionado en el repositorio del curso, aunque no tenga un ejemplo desarrollado. Esta versión sustituye el dictamen anterior, que exigía una correspondencia más estricta con ejemplos concretos.

## Dictamen vigente

El modelo y el procesamiento funcional utilizan recursos respaldados por el repositorio del curso, de acuerdo con el criterio aclarado. No se encontraron importaciones ni dependencias externas en Modelo, Billetera o DatosDemo. Las herramientas adicionales de interfaz y adaptación de texto permanecen separadas del modelo.

FoldLeft está permitido: aparece tanto en los apuntes de listas como en el enunciado del proyecto. No se exige copiar funciones literalmente ni mantener la misma combinación de construcciones que los ejemplos. Los algoritmos propios del dominio se construyen con esos recursos.

## 1. Material y alcance

Se revisaron los 31 archivos Scala y los 19 documentos Markdown del ZIP `Programacion-Funcional-Mod2-2026-main (5).zip`. Los 51 archivos extraídos, incluida su configuración gitignore, se verificaron byte a byte contra el ZIP original.

Se contrastaron todos los fuentes del proyecto, incluidas las comprobaciones, los archivos de compilación y el ZIP de entrega. Las referencias al curso usan rutas internas a su carpeta `Programacion-Funcional-Mod2-2026-main`, con líneas contadas desde 1 en la versión proporcionada.

## 2. Clasificación de archivos

| Archivo | Responsabilidad y resultado |
| --- | --- |
| `Modelo.scala` | Entidades, tipos, fechas y resultados. Case classes, traits, atributos abstractos val, case objects, genéricos y aritmética del curso. Sin imports. |
| `Billetera.scala` | Registro, búsqueda, eliminación, totales, auditoría y simulación. Listas inmutables, funciones de orden superior, map/filter/foldLeft, recursión y match. Sin imports. |
| `DatosDemo.scala` | Ejemplo fijo cargado por operaciones validadas y foldLeft. Sin reloj ni imports. |
| `Verificacion.scala` | Comprobaciones con funciones, recursión, listas, match, copy y excepciones mostradas en el curso. También comprueba la adaptación de entradas de interfaz. |
| `Main.scala` | Swing/AWT, eventos, presentación, fecha local e identificadores. Dentro de la excepción de interfaz. |
| `EntradaInterfaz.scala` | Adaptador del formulario: convierte texto a centavos y comunica errores de formato. Dentro de la capa de interfaz. |

## 3. Trazabilidad al curso

| Construcción o recurso | Evidencia en el curso | Uso del proyecto |
| --- | --- | --- |
| `val`, tipos básicos, inferencia | `Tema-02-Expresiones-Valores-Funciones/Clase3.scala:4` y siguientes | Campos y valores intermedios. |
| Operaciones `+ - * / %`, comparaciones, `&&`, `||`, `!`, `if/else` | `Tema-02-Expresiones-Valores-Funciones/Clase3.scala:18`, `:26`, `:67` | Dinero, validaciones y calendario. |
| Tipo `Long` y aritmética con él | `Tema-03-Recursion/Clase6.scala:10` y `:19`; `Clase8.scala:30` | Centavos y acumulaciones. No es un tipo ajeno al curso. |
| Literales con sufijo `L` | `Tema-03-Recursion/Clase6.scala:22`; `Clase8.scala:30`; `Clase9.scala:11` | `0L`, `1L` e importes enteros. |
| Clases y nuevos objetos inmutables | `Tema-05-POO/Clase18.scala:1` y `:12` | Nueva `Billetera` como resultado de un cambio. |
| Constructor privado y objeto acompañante | `Tema-05-POO/Clase19.scala:9` y `:20` | Constructor de Billetera y `Billetera.vacia`. |
| `trait`, métodos abstractos y herencia | `Tema-05-POO/Clase20.scala:1`; `Apuntes-Clase20.md` | Jerarquías del modelo. Los nombres se declaran como atributos abstractos `val`, siguiendo `Apuntes-Clase20.md:289-309`. |
| `case class` y métodos/campos de entidades | `Tema-07-Case-Classes/Clase25.scala:1`; `Apuntes-Clase25.md` | Usuario, Fecha, Transaccion, Diagnostico y Simulacion. |
| `copy` y argumentos nombrados | `Tema-07-Case-Classes/Clase25.scala:30` | Variantes de datos para comprobaciones. |
| `sealed trait`, `case object`, pattern matching | `Tema-07-Case-Classes/Clase26.scala:1`, `:3`, `:20` | Tipos, categorías, estados y resultados. |
| `Resultado[+A]`, `Exito[A]`, `Error` con `Nothing` | `Tema-07-Case-Classes/Apuntes-Clase26.md:571`, `:573`, `:577` | Modelo.scala:47-49. El patrón está desarrollado en los apuntes, no solo mencionado. |
| Match sobre resultados genéricos | `Tema-07-Case-Classes/Apuntes-Clase26.md:608` | Registro, simulación y carga del ejemplo. |
| Métodos genéricos y argumentos de tipo explícitos | `Tema-06-Polimorfismo-Genericos/Apuntes-Clase24.md:166`, `:175` | Auxiliares de pruebas y argumento `Resultado[Billetera]`. |
| Tuplas como datos | `Tema-04-Funciones-OrdenSuperior/Clase13.scala:30`; `Tema-06-Polimorfismo-Genericos/Apuntes-Clase24.md:340` | Pares de categoría y total. Las pruebas extraen sus componentes con pattern matching, mostrado en `Apuntes-Clase25.md:871`. |
| Funciones como parámetro y lambdas | `Tema-04-Funciones-OrdenSuperior/Clase14.scala:6`, `:24` | `buscar(criterio: Transaccion => Boolean)` y acumuladores. |
| Varios grupos de parámetros | `Tema-04-Funciones-OrdenSuperior/Clase16.scala:9`, `:53` | Forma de aplicación usada al acumular. La operación foldLeft tiene además una mención explícita en el material. |
| Listas, `List`, `Nil`, `::`, recursión de listas | `Tema-08-Listas/Clase27.scala:3`, `:12`, `:52` | Almacenamiento, registro y recorrido de comprobaciones. |
| `List.contains`, `List.length`, `List.isEmpty` | `Tema-08-Listas/Clase27.scala:70-72`; `Apuntes-Clase27.md:207-228` | Categorías, búsquedas y comprobaciones. |
| `map` y `filter` | `Tema-08-Listas/Apuntes-Clase28.md:47`, `:79`, `:174` | Totales por categoría, búsqueda y eliminación. |
| Lambda abreviada con un parámetro, `_.campo` | `Tema-08-Listas/Apuntes-Clase28.md:176-177` | Búsquedas como `_.tipo == Ingreso`. |
| Recursión de cadenas con `head` y `tail` | `Tema-03-Recursion/Clase7-Strings.scala:12`, `:23` | `tieneTexto`. El caso base utiliza `texto.length() == 0`, como el ejemplo de cadenas. |
| Comparación de case classes por valor | `Tema-07-Case-Classes/Apuntes-Clase25.md:320` | Comparar diagnósticos y entidades en pruebas. |
| `throw new RuntimeException` | `Tema-06-Polimorfismo-Genericos/Clase23.scala:27-31` | Señalar un fallo en las comprobaciones sin librería externa. |
| `String` interpolation y `println` | `Tema-02-Expresiones-Valores-Funciones/Clase3.scala:14-17` | Mensajes de comprobación. |

## 4. Cambios tras aplicar el criterio aclarado

- Se conserva `foldLeft`, citado en `Practicas/PropuestaProyectoFinal.md:120` y `Tema-08-Listas/Apuntes-Clase27.md:252`. La ausencia de ejemplo desarrollado deja de ser una objeción.
- Los tres traits con nombre declaran ahora `val nombre: String`. Esto sigue el ejemplo explícito de atributos abstractos en `Tema-05-POO/Apuntes-Clase20.md:289-309` y evita depender de la particularidad de implementar un def con un val.
- El caso base de `tieneTexto` utiliza `texto.length() == 0`, como `Tema-03-Recursion/Clase7-Strings.scala:8`, `:13` y `:24`.
- Las pruebas extraen los componentes de las tuplas con `match`, mostrado en `Tema-07-Case-Classes/Apuntes-Clase25.md:871`, en lugar de usar los accesores `_1/_2`.
- Se reemplazó la lambda binaria abreviada `_ + _` por parámetros con nombres.
- Los extremos numéricos probados se expresan con literales Long y aritmética, en lugar de llamadas a `Long.MinValue` o `Long.MaxValue`. Se mantiene la misma cobertura de valores extremos.

`private`, `Unit`, listas, comparación mediante ==, funciones y Scala 3 tienen respaldo conceptual en el material. Las variantes de escritura, la combinación de estas construcciones y los datos concretos del dominio no se tratan como tecnologías nuevas. Los caracteres de tabulación y retorno son datos de entrada considerados por la validación.

## 5. Revisión por operación

| Operación | Recursos del curso empleados |
| --- | --- |
| Categoria.compatible | Match y List.contains. |
| Fecha.esValida | Enteros, %, comparaciones, operadores booleanos, if/else y List.contains. |
| Transaccion.montoConSigno | Match sobre alternativas e importe Long con signo. |
| Billetera.vacia | Constructor privado, objeto acompañante, new y Nil. |
| Registrar | Condiciones, búsqueda, resultados genéricos, match, :: y nueva instancia inmutable. |
| Eliminar | Filter, lambda y nueva instancia. |
| Buscar | Función de orden superior que recibe Transaccion => Boolean y utiliza filter. |
| Saldo, ingresos y gastos | Filter y foldLeft sobre Long. |
| Totales por categoría | Map, tuplas, filter y foldLeft. |
| Diagnóstico | Filter, acumulación, aritmética y construcción de una case class. |
| Simular compra | Validaciones, match, cálculos sobre nuevos valores y resultado tipado; conserva la lista original. |
| TieneTexto | Recursión sobre String con length, head y tail. |
| Validar monto | Comparaciones, aritmética y Exito/Error. |
| Evaluar gastos | Aritmética, condicionales y Diagnostico. |
| Cargar demostración | Literales, listas, case classes, resultado genérico y foldLeft. |
| Comprobaciones | Funciones genéricas, RuntimeException, listas, recursión, copy, pattern matching de tuplas, foldLeft, operadores básicos y println. |

La representación en centavos, la regla del 20%, las validaciones del calendario y el límite monetario son decisiones propias del proyecto implementadas con recursos del curso. No añaden una biblioteca financiera ni de fechas al modelo.

## 6. Separación real de la excepción de interfaz

Main contiene Swing, AWT, listeners, layouts, renderizado, formato visual, LocalDate, DateTimeFormatter, UUID, ordenamiento y carga de componentes. La referencia mutable actual pertenece a los eventos de la interfaz; los objetos del dominio siguen siendo inmutables.

EntradaInterfaz utiliza trim, replace, expresiones regulares, Try y java.math.BigDecimal para adaptar el texto del formulario a centavos sin pérdida de precisión. No calcula saldo, no evalúa el ocio ni modifica movimientos. No es llamado por Modelo, Billetera ni DatosDemo: su consumidor de producción es Main y las pruebas lo llaman para verificar la adaptación.

Las validaciones de monto positivo, límites, categoría, fecha e identificadores están en Billetera. No se desplazaron a la interfaz para exceptuarlas de la regla del curso.

El modelo no utiliza bases de datos, red, concurrencia, frameworks funcionales ni librerías financieras externas.

## 7. Organización y compilación

El paquete `billeteraclara`, build.sbt, project/build.properties y la directiva de Scala CLI son infraestructura de organización y ejecución del proyecto. No están en el ZIP de ejemplos y no se presentan como materia enseñada ni como lógica del negocio. build.sbt no declara libraryDependencies.

Esta distinción se mantiene explícita: el dictamen sobre recursos del curso se refiere al modelo, su procesamiento y las comprobaciones, con la excepción autorizada para la interfaz. La configuración de compilación no constituye una técnica funcional adicional.

## 8. Validación

Tras estos cambios se ejecutaron las comprobaciones del proyecto y todas pasaron. Se conservaron los resultados y la cobertura de precisión, inmutabilidad, errores, categorías, fechas, límites y adaptación de entradas.

El paquete de entrega se regeneró después de que pasaran las comprobaciones y sus archivos se verificaron byte a byte contra los fuentes y documentos locales. Este informe de revisión se mantiene separado del documento de entrega.

## Anexo: identidad de los fuentes auditados

SHA-256 del ZIP del curso: `81468fca26bed498d103ea2d2de1aaaed78283869df3820369c02543e6886371`.

| Fuente del proyecto | SHA-256 |
| --- | --- |
| `src/main/scala/billeteraclara/Billetera.scala` | `dac50761f910abfc70cfc15392fe072f6a718e1fff2aaf0b795c76d6590b765c` |
| `src/main/scala/billeteraclara/DatosDemo.scala` | `e8dd7f5418f681bdbc7e3403e0672e886958b2896f02876459731338e0c11bee` |
| `src/main/scala/billeteraclara/EntradaInterfaz.scala` | `ec8e7e3b2ba02208bc2a3389105900c13ee0acad9851fad29e293bac3fd8d29f` |
| `src/main/scala/billeteraclara/Main.scala` | `36e05bedbf687e94b68aa8b40990cfa63fb75e52bb6918aff7ef3ea9373ab4ce` |
| `src/main/scala/billeteraclara/Modelo.scala` | `d1ad84a5df337bbf17eeef3792c15c0c1d417b8c2ba2da857d2f4a787663de98` |
| `src/test/scala/billeteraclara/Verificacion.scala` | `065e079fbe084cb9c22fe9980776b7b5083a9e2daaf0e20839d59468675885ce` |

## Revisión final para presentación (4 de noviembre de 2026)

Se volvieron a contrastar el enunciado, el ZIP del curso y la propuesta PDF adjuntos con las referencias guardadas: coinciden (normalizando finales de línea en el Markdown). No fue necesario cambiar el modelo, sus reglas ni sus dependencias. Las nuevas comprobaciones usan únicamente construcciones ya respaldadas en esta auditoría.

Se añadió una etiqueta visible y un nombre accesible al buscador, se habilitó el foco de teclado en los selectores y se corrigió el contador singular. Se agregó `docs/Presentacion.md` con un guion de exposición y trazabilidad a los requisitos obligatorios. README y documento del sistema explican el requisito de Java gráfico.

La compilación y las comprobaciones ampliadas del modelo y del adaptador pasaron con Java 21 y Scala CLI 1.18.0 / Scala 3.9.0. El Java del entorno era solo headless; para verificar Swing se preparó temporalmente Java gráfico 21.0.13 y un servidor X virtual. Se observó la ventana inicial y se probaron las acciones de la interfaz mediante un auxiliar temporal sobre los componentes Swing: búsqueda, filtro, registro, simulación sin registro, eliminación y sesión vacía. Esta evidencia corresponde a Linux; no constituye una comprobación del aspecto nativo en Windows ni macOS.

Las vías verificadas usan Scala CLI. La alternativa sbt del README no se ejecutó en esta revisión. No se publicaron cambios en GitHub ni se modificó la configuración publicada del entorno.

El auxiliar temporal de interfaz también confirmó la compra con saldo proyectado exactamente cero, el rechazo de un registro con monto cero sin alterar el estado visible y la independencia entre saldo suficiente y alerta de ocio. El auxiliar se mantuvo fuera de los fuentes de entrega.

El paquete final contiene 12 archivos y coincide byte a byte con sus archivos del checkout. Desde una extracción independiente se ejecutaron de nuevo la compilación y todas las comprobaciones, ambas con resultado correcto. Se revisaron los enlaces locales de README y documentos de entrega, así como `git diff --check`, sin errores. Los hashes de este anexo corresponden a los archivos actuales (con finales de línea LF).
