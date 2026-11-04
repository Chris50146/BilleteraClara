//> using scala 3.9.0
package billeteraclara

import java.awt.*
import java.awt.event.ActionEvent
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.event.{DocumentEvent, DocumentListener}

object Main:
  def main(args: Array[String]): Unit =
    SwingUtilities.invokeLater(() =>
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName)
      val ventana = new VentanaBilletera()
      ventana.setVisible(true)
    )

private object Estilo:
  val fondo = new Color(246, 247, 250)
  val blanco = Color.WHITE
  val tinta = new Color(26, 30, 39)
  val tenue = new Color(113, 120, 134)
  val borde = new Color(229, 232, 238)
  val acento = new Color(82, 94, 230)
  val verde = new Color(29, 154, 113)
  val rojo = new Color(215, 79, 88)

  def fuente(tamano: Int, negrita: Boolean = false): Font =
    new Font("SansSerif", if negrita then Font.BOLD else Font.PLAIN, tamano)

  def etiqueta(texto: String, tamano: Int = 14, color: Color = tinta,
               negrita: Boolean = false): JLabel =
    val label = new JLabel(texto)
    label.setFont(fuente(tamano, negrita))
    label.setForeground(color)
    label

  def boton(texto: String, principal: Boolean = false): JButton =
    val boton = new JButton(texto)
    // La UI básica respeta los colores también con el tema nativo de Windows.
    boton.setUI(new javax.swing.plaf.basic.BasicButtonUI())
    boton.setFont(fuente(13, true))
    boton.setFocusPainted(false)
    boton.setBorder(new EmptyBorder(11, 16, 11, 16))
    boton.setForeground(if principal then blanco else tinta)
    boton.setBackground(if principal then acento else new Color(239, 241, 246))
    boton.setOpaque(true)
    boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))
    boton

  def campo(): JTextField =
    val campo = new JTextField()
    campo.setFont(fuente(14))
    campo.setForeground(tinta)
    campo.setBackground(blanco)
    campo.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createLineBorder(borde, 1, true), new EmptyBorder(10, 12, 10, 12)))
    campo

  def dinero(valor: Long): String =
    val absoluto = math.abs(valor)
    val signo = if valor < 0 then "-" else ""
    f"Bs $signo${absoluto / 100}%d.${absoluto % 100}%02d"

private class Tarjeta(color: Color = Estilo.blanco, radio: Int = 22) extends JPanel:
  setOpaque(false)
  setLayout(new BorderLayout())
  override def paintComponent(g: Graphics): Unit =
    val g2 = g.create().asInstanceOf[Graphics2D]
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g2.setColor(color)
    g2.fillRoundRect(0, 0, getWidth, getHeight, radio, radio)
    g2.dispose()
    super.paintComponent(g)

private class VentanaBilletera extends JFrame("Billetera Clara"):
  import Estilo.*

  // La única variable de estado está en la interfaz. El modelo devuelve nuevas Billeteras.
  private var actual: Billetera = DatosDemo.ejemplo match
    case Exito(billetera) => billetera
    case Error(mensaje) => throw new IllegalStateException(mensaje)

  private val saldoValor = etiqueta("", 31, tinta, true)
  private val ingresosValor = etiqueta("", 23, verde, true)
  private val gastosValor = etiqueta("", 23, rojo, true)
  private val diagnosticoValor = etiqueta("", 15, tinta, true)
  private val ocioValor = etiqueta("", 13, tenue)
  private val barraOcio = new JProgressBar(0, 100)
  private val lista = new JPanel()
  private val categoriasGrafico = new JPanel()
  private val busqueda = campo()
  private val filtroTipo = new JComboBox[String](Array("Todos", "Ingresos", "Gastos"))
  private val descripcion = campo()
  private val monto = campo()
  private val tipo = new JComboBox[String](Array("Gasto", "Ingreso"))
  private val categoria = new JComboBox[Categoria]()
  private val simMonto = campo()
  private val simCategoria = new JComboBox[Categoria](Categoria.deGasto.toArray)
  private val resultadoSimulacion = new JPanel()
  private val cantidadLabel = etiqueta("", 13, tenue)

  setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
  setMinimumSize(new Dimension(1080, 720))
  setSize(1240, 840)
  setLocationRelativeTo(null)
  getContentPane.setBackground(fondo)

  private val raiz = new JPanel(new BorderLayout(0, 24))
  raiz.setBackground(fondo)
  raiz.setBorder(new EmptyBorder(26, 32, 30, 32))
  setContentPane(raiz)
  raiz.add(crearCabecera(), BorderLayout.NORTH)

  private val contenido = new JPanel()
  contenido.setOpaque(false)
  contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS))
  contenido.add(crearResumen())
  contenido.add(Box.createVerticalStrut(22))
  contenido.add(crearCuerpo())

  private val scroll = new JScrollPane(contenido)
  scroll.setBorder(null)
  scroll.getViewport.setBackground(fondo)
  scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER)
  scroll.getVerticalScrollBar.setUnitIncrement(18)
  raiz.add(scroll, BorderLayout.CENTER)

  prepararEventos()
  actualizarTodo()

  private def crearCabecera(): JPanel =
    val panel = new JPanel(new BorderLayout())
    panel.setOpaque(false)
    val marca = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0))
    marca.setOpaque(false)
    val punto = etiqueta("●", 21, acento)
    marca.add(punto)
    marca.add(etiqueta("Billetera Clara", 17, tinta, true))
    panel.add(marca, BorderLayout.WEST)
    val derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0))
    derecha.setOpaque(false)
    derecha.add(etiqueta("FINANZAS PERSONALES  /  PROYECTO FUNCIONAL", 11, tenue, true))
    val limpiar = boton("Empezar en blanco")
    limpiar.setToolTipText("Quita los registros de demostración")
    limpiar.addActionListener((_: ActionEvent) =>
      actual = Billetera.vacia(actual.usuario)
      actualizarTodo()
    )
    derecha.add(limpiar)
    panel.add(derecha, BorderLayout.EAST)
    panel

  private def crearResumen(): JPanel =
    val contenedor = new JPanel(new BorderLayout(0, 19))
    contenedor.setOpaque(false)
    contenedor.setAlignmentX(Component.LEFT_ALIGNMENT)
    val titulo = new JPanel()
    titulo.setOpaque(false)
    titulo.setLayout(new BoxLayout(titulo, BoxLayout.Y_AXIS))
    titulo.add(etiqueta("Tu dinero, con claridad.", 30, tinta, true))
    titulo.add(Box.createVerticalStrut(4))
    titulo.add(etiqueta("Registra, entiende y proyecta tus movimientos en un solo lugar.", 14, tenue))
    contenedor.add(titulo, BorderLayout.NORTH)

    val tarjetas = new JPanel(new GridLayout(1, 3, 16, 0))
    tarjetas.setOpaque(false)
    tarjetas.add(tarjetaNumero("SALDO DISPONIBLE", saldoValor, acento))
    tarjetas.add(tarjetaNumero("INGRESOS", ingresosValor, verde))
    tarjetas.add(tarjetaNumero("GASTOS", gastosValor, rojo))
    contenedor.add(tarjetas, BorderLayout.CENTER)
    contenedor

  private def tarjetaNumero(titulo: String, valor: JLabel, color: Color): JPanel =
    val card = new Tarjeta()
    card.setPreferredSize(new Dimension(200, 145))
    card.setBorder(new EmptyBorder(22, 24, 21, 24))
    val interior = new JPanel()
    interior.setOpaque(false)
    interior.setLayout(new BoxLayout(interior, BoxLayout.Y_AXIS))
    interior.add(etiqueta("●  " + titulo, 11, color, true))
    interior.add(Box.createVerticalGlue())
    interior.add(valor)
    interior.add(Box.createVerticalStrut(5))
    interior.add(etiqueta("Balance actualizado en tiempo real", 11, tenue))
    card.add(interior)
    card

  private def crearCuerpo(): JPanel =
    val cuerpo = new JPanel(new GridBagLayout())
    cuerpo.setOpaque(false)
    cuerpo.setAlignmentX(Component.LEFT_ALIGNMENT)
    val izquierda = new JPanel()
    izquierda.setOpaque(false)
    izquierda.setLayout(new BoxLayout(izquierda, BoxLayout.Y_AXIS))
    izquierda.add(crearMovimientos())
    izquierda.add(Box.createVerticalStrut(18))
    izquierda.add(crearGrafico())

    val derecha = new JPanel()
    derecha.setOpaque(false)
    derecha.setLayout(new BoxLayout(derecha, BoxLayout.Y_AXIS))
    derecha.add(crearFormulario())
    derecha.add(Box.createVerticalStrut(18))
    derecha.add(crearAuditoria())
    derecha.add(Box.createVerticalStrut(18))
    derecha.add(crearSimulador())

    val a = new GridBagConstraints()
    a.gridx = 0; a.gridy = 0; a.weightx = 0.62; a.weighty = 1
    a.fill = GridBagConstraints.BOTH; a.insets = new Insets(0, 0, 0, 18)
    cuerpo.add(izquierda, a)
    val b = new GridBagConstraints()
    b.gridx = 1; b.gridy = 0; b.weightx = 0.38; b.weighty = 1
    b.fill = GridBagConstraints.BOTH
    cuerpo.add(derecha, b)
    cuerpo

  private def crearMovimientos(): JPanel =
    val card = new Tarjeta()
    card.setBorder(new EmptyBorder(24, 24, 18, 24))
    val cuerpo = new JPanel()
    cuerpo.setOpaque(false)
    cuerpo.setLayout(new BoxLayout(cuerpo, BoxLayout.Y_AXIS))
    val cabecera = new JPanel(new BorderLayout())
    cabecera.setOpaque(false)
    cabecera.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28))
    cabecera.add(etiqueta("Movimientos", 20, tinta, true), BorderLayout.WEST)
    cabecera.add(cantidadLabel, BorderLayout.EAST)
    cuerpo.add(cabecera)
    cuerpo.add(Box.createVerticalStrut(18))
    val controles = new JPanel(new GridLayout(1, 2, 10, 0))
    controles.setOpaque(false)
    controles.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44))
    busqueda.setToolTipText("Buscar por descripción o categoría")
    busqueda.putClientProperty("JTextField.placeholderText", "Buscar movimiento...")
    controles.add(busqueda)
    decorarCombo(filtroTipo)
    controles.add(filtroTipo)
    cuerpo.add(controles)
    cuerpo.add(Box.createVerticalStrut(12))
    lista.setOpaque(false)
    lista.setLayout(new BoxLayout(lista, BoxLayout.Y_AXIS))
    cuerpo.add(lista)
    card.add(cuerpo)
    card

  private def crearGrafico(): JPanel =
    val card = new Tarjeta()
    card.setBorder(new EmptyBorder(23, 24, 23, 24))
    val interior = new JPanel()
    interior.setOpaque(false)
    interior.setLayout(new BoxLayout(interior, BoxLayout.Y_AXIS))
    interior.add(etiqueta("Gastos por categoría", 18, tinta, true))
    interior.add(Box.createVerticalStrut(5))
    interior.add(etiqueta("Distribución de tus consumos", 12, tenue))
    interior.add(Box.createVerticalStrut(18))
    categoriasGrafico.setOpaque(false)
    categoriasGrafico.setLayout(new BoxLayout(categoriasGrafico, BoxLayout.Y_AXIS))
    interior.add(categoriasGrafico)
    card.add(interior)
    card

  private def crearFormulario(): JPanel =
    val card = new Tarjeta()
    card.setBorder(new EmptyBorder(23, 24, 23, 24))
    val interior = new JPanel()
    interior.setOpaque(false)
    interior.setLayout(new BoxLayout(interior, BoxLayout.Y_AXIS))
    interior.add(etiqueta("Nuevo movimiento", 18, tinta, true))
    interior.add(Box.createVerticalStrut(5))
    interior.add(etiqueta("Usa hasta dos decimales, por ejemplo: 25,50.", 12, tenue))
    interior.add(Box.createVerticalStrut(19))
    interior.add(campoConTitulo("TIPO", tipo))
    interior.add(Box.createVerticalStrut(12))
    interior.add(campoConTitulo("DESCRIPCIÓN", descripcion))
    interior.add(Box.createVerticalStrut(12))
    interior.add(campoConTitulo("MONTO EN BS", monto))
    interior.add(Box.createVerticalStrut(12))
    interior.add(campoConTitulo("CATEGORÍA", categoria))
    interior.add(Box.createVerticalStrut(17))
    decorarCombo(tipo)
    decorarCombo(categoria)
    monto.putClientProperty("JTextField.placeholderText", "0.00")
    descripcion.putClientProperty("JTextField.placeholderText", "Ej. Supermercado")
    val registrar = boton("Agregar movimiento", true)
    registrar.setAlignmentX(Component.LEFT_ALIGNMENT)
    registrar.addActionListener((_: ActionEvent) => registrarMovimiento())
    interior.add(registrar)
    card.add(interior)
    card

  private def campoConTitulo(titulo: String, componente: JComponent): JPanel =
    val panel = new JPanel(new BorderLayout(0, 7))
    panel.setOpaque(false)
    panel.setAlignmentX(Component.LEFT_ALIGNMENT)
    panel.add(etiqueta(titulo, 10, tenue, true), BorderLayout.NORTH)
    componente.setPreferredSize(new Dimension(100, 42))
    panel.add(componente, BorderLayout.CENTER)
    panel

  private def crearAuditoria(): JPanel =
    val card = new Tarjeta()
    card.setBorder(new EmptyBorder(23, 24, 23, 24))
    val interior = new JPanel()
    interior.setOpaque(false)
    interior.setLayout(new BoxLayout(interior, BoxLayout.Y_AXIS))
    interior.add(etiqueta("Salud del presupuesto", 18, tinta, true))
    interior.add(Box.createVerticalStrut(14))
    interior.add(diagnosticoValor)
    interior.add(Box.createVerticalStrut(6))
    interior.add(ocioValor)
    interior.add(Box.createVerticalStrut(13))
    barraOcio.setPreferredSize(new Dimension(200, 8))
    barraOcio.setMaximumSize(new Dimension(Integer.MAX_VALUE, 8))
    barraOcio.setForeground(acento)
    barraOcio.setBackground(new Color(232, 234, 242))
    barraOcio.setBorderPainted(false)
    interior.add(barraOcio)
    interior.add(Box.createVerticalStrut(12))
    interior.add(etiqueta("Regla: ocio ≤ 20% de todos los gastos.", 12, tenue))
    card.add(interior)
    card

  private def crearSimulador(): JPanel =
    val card = new Tarjeta()
    card.setBorder(new EmptyBorder(23, 24, 23, 24))
    val interior = new JPanel()
    interior.setOpaque(false)
    interior.setLayout(new BoxLayout(interior, BoxLayout.Y_AXIS))
    interior.add(etiqueta("Simula una compra", 18, tinta, true))
    interior.add(Box.createVerticalStrut(5))
    interior.add(etiqueta("Explora el impacto antes de gastar.", 12, tenue))
    interior.add(Box.createVerticalStrut(17))
    simMonto.putClientProperty("JTextField.placeholderText", "0.00")
    decorarCombo(simCategoria)
    interior.add(campoConTitulo("MONTO EN BS", simMonto))
    interior.add(Box.createVerticalStrut(12))
    interior.add(campoConTitulo("CATEGORÍA", simCategoria))
    interior.add(Box.createVerticalStrut(14))
    val simular = boton("Calcular proyección")
    simular.setAlignmentX(Component.LEFT_ALIGNMENT)
    simular.addActionListener((_: ActionEvent) => simularCompra())
    interior.add(simular)
    interior.add(Box.createVerticalStrut(15))
    resultadoSimulacion.setOpaque(false)
    resultadoSimulacion.setLayout(new BoxLayout(resultadoSimulacion, BoxLayout.Y_AXIS))
    resultadoSimulacion.add(etiqueta("La simulación no modifica tus movimientos.", 12, tenue))
    interior.add(resultadoSimulacion)
    card.add(interior)
    card

  private def decorarCombo[A](combo: JComboBox[A]): Unit =
    combo.setFont(fuente(13))
    combo.setForeground(tinta)
    combo.setBackground(blanco)
    combo.setFocusable(false)

  private def prepararEventos(): Unit =
    tipo.addActionListener((_: ActionEvent) => actualizarCategorias())
    actualizarCategorias()
    filtroTipo.addActionListener((_: ActionEvent) => actualizarLista())
    busqueda.getDocument.addDocumentListener(new DocumentListener:
      def insertUpdate(e: DocumentEvent): Unit = actualizarLista()
      def removeUpdate(e: DocumentEvent): Unit = actualizarLista()
      def changedUpdate(e: DocumentEvent): Unit = actualizarLista()
    )

  private def actualizarCategorias(): Unit =
    categoria.removeAllItems()
    val opciones = if tipo.getSelectedItem == "Ingreso" then Categoria.deIngreso else Categoria.deGasto
    opciones.foreach(categoria.addItem)
    categoria.setRenderer(new DefaultListCellRenderer():
      override def getListCellRendererComponent(list: JList[?], value: Object,
          index: Int, selected: Boolean, focus: Boolean): Component =
        val item = super.getListCellRendererComponent(list, value, index, selected, focus)
        value match
          case c: Categoria => setText(c.nombre)
          case _ => ()
        item
    )
    simCategoria.setRenderer(categoria.getRenderer)

  private def registrarMovimiento(): Unit =
    EntradaInterfaz.parsearMonto(monto.getText) match
      case Error(error) => mostrarError(error)
      case Exito(valor) =>
        val hoy = LocalDate.now()
        val transaccion = Transaccion(
          UUID.randomUUID().toString, descripcion.getText.trim, valor,
          Fecha(hoy.getDayOfMonth, hoy.getMonthValue, hoy.getYear),
          categoria.getSelectedItem.asInstanceOf[Categoria],
          if tipo.getSelectedItem == "Ingreso" then Ingreso else Gasto
        )
        actual.registrar(transaccion) match
          case Error(error) => mostrarError(error)
          case Exito(nueva) =>
            actual = nueva
            descripcion.setText("")
            monto.setText("")
            actualizarTodo()

  private def simularCompra(): Unit =
    // No conservar una proyección anterior si el nuevo intento es inválido.
    resultadoSimulacion.removeAll()
    EntradaInterfaz.parsearMonto(simMonto.getText) match
      case Error(error) => mostrarError(error)
      case Exito(valor) =>
        actual.simularCompra(valor, simCategoria.getSelectedItem.asInstanceOf[Categoria]) match
          case Error(error) => mostrarError(error)
          case Exito(s) =>
            resultadoSimulacion.add(etiqueta("Saldo proyectado", 12, tenue))
            resultadoSimulacion.add(Box.createVerticalStrut(4))
            resultadoSimulacion.add(etiqueta(dinero(s.saldoProyectado), 22,
              if s.compraPosible then tinta else rojo, true))
            resultadoSimulacion.add(Box.createVerticalStrut(6))
            resultadoSimulacion.add(etiqueta(
              if s.compraPosible then "Saldo suficiente" else "Saldo insuficiente", 12,
              if s.compraPosible then verde else rojo, true))
            resultadoSimulacion.add(Box.createVerticalStrut(3))
            resultadoSimulacion.add(etiqueta(
              f"Ocio proyectado: ${s.diagnosticoProyectado.porcentajeOcio}%.1f%% · " +
                s.diagnosticoProyectado.estado.nombre, 12, tenue))
    resultadoSimulacion.revalidate()
    resultadoSimulacion.repaint()

  private def actualizarTodo(): Unit =
    saldoValor.setText(dinero(actual.saldo))
    ingresosValor.setText(dinero(actual.ingresos))
    gastosValor.setText(dinero(actual.gastos))
    val d = actual.diagnostico
    diagnosticoValor.setText(if d.totalGastos == 0 then "●  Sin gastos para evaluar"
      else if d.estado == Saludable then "●  Ocio dentro del 20%" else "●  Atención al ocio")
    diagnosticoValor.setForeground(if d.estado == Saludable then verde else rojo)
    ocioValor.setText(f"Ocio: ${d.porcentajeOcio.toDouble}%.1f%% del gasto · ${dinero(d.gastoOcio)}")
    barraOcio.setValue(d.porcentajeOcio.min(100.0).toInt)
    barraOcio.setForeground(if d.estado == Saludable then acento else rojo)
    actualizarLista()
    actualizarGrafico()
    resultadoSimulacion.removeAll()
    resultadoSimulacion.add(etiqueta("La simulación no modifica tus movimientos.", 12, tenue))
    resultadoSimulacion.revalidate()
    resultadoSimulacion.repaint()

  private def actualizarLista(): Unit =
    val texto = busqueda.getText.trim.toLowerCase
    val tipoElegido = filtroTipo.getSelectedItem
    val encontrados = actual.buscar(tx =>
      (texto.isEmpty || tx.descripcion.toLowerCase.contains(texto) ||
        tx.categoria.nombre.toLowerCase.contains(texto)) &&
      (tipoElegido == "Todos" ||
        (tipoElegido == "Ingresos" && tx.tipo == Ingreso) ||
        (tipoElegido == "Gastos" && tx.tipo == Gasto))
    )
    cantidadLabel.setText(s"${encontrados.length} registros")
    lista.removeAll()
    if encontrados.isEmpty then
      val vacio = etiqueta("Sin movimientos para este filtro.", 13, tenue)
      vacio.setBorder(new EmptyBorder(24, 0, 24, 0))
      lista.add(vacio)
    else encontrados.foreach(tx => lista.add(filaMovimiento(tx)))
    lista.revalidate()
    lista.repaint()

  private def filaMovimiento(tx: Transaccion): JPanel =
    val fila = new JPanel(new BorderLayout(12, 0))
    fila.setOpaque(false)
    fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68))
    fila.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createMatteBorder(0, 0, 1, 0, borde),
      new EmptyBorder(10, 0, 10, 0)))

    val icono = etiqueta(if tx.tipo == Ingreso then "↘" else "↗", 18,
      if tx.tipo == Ingreso then verde else acento, true)
    icono.setHorizontalAlignment(SwingConstants.CENTER)
    icono.setPreferredSize(new Dimension(32, 32))
    fila.add(icono, BorderLayout.WEST)

    val datos = new JPanel()
    datos.setOpaque(false)
    datos.setLayout(new BoxLayout(datos, BoxLayout.Y_AXIS))
    datos.add(etiqueta(tx.descripcion, 13, tinta, true))
    datos.add(Box.createVerticalStrut(4))
    datos.add(etiqueta(tx.categoria.nombre + " · " +
      LocalDate.of(tx.fecha.anio, tx.fecha.mes, tx.fecha.dia).format(
        DateTimeFormatter.ofPattern("dd MMM yyyy", java.util.Locale.forLanguageTag("es"))),
      11, tenue))
    fila.add(datos, BorderLayout.CENTER)

    val acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 3))
    acciones.setOpaque(false)
    acciones.add(etiqueta((if tx.tipo == Ingreso then "+" else "−") + dinero(tx.montoCentavos),
      13, if tx.tipo == Ingreso then verde else tinta, true))
    val borrar = boton("×")
    borrar.setToolTipText("Eliminar movimiento")
    borrar.setBorder(new EmptyBorder(3, 8, 3, 8))
    borrar.addActionListener((_: ActionEvent) =>
      actual = actual.eliminar(tx.id)
      actualizarTodo()
    )
    acciones.add(borrar)
    fila.add(acciones, BorderLayout.EAST)
    fila

  private def actualizarGrafico(): Unit =
    categoriasGrafico.removeAll()
    val datos = actual.totalesPorCategoria.filter(_._2 > 0).sortBy(par => -par._2)
    if datos.isEmpty then
      categoriasGrafico.add(etiqueta("Registra un gasto para ver la distribución.", 12, tenue))
    else
      datos.foreach { case (cat, total) =>
        val proporcion = (total * 100 / actual.gastos).toInt
        val linea = new JPanel(new BorderLayout(12, 0))
        linea.setOpaque(false)
        linea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 31))
        val nombre = etiqueta(cat.nombre, 12, tinta)
        nombre.setPreferredSize(new Dimension(100, 21))
        linea.add(nombre, BorderLayout.WEST)
        val barra = new JProgressBar(0, 100)
        barra.setValue(proporcion)
        barra.setForeground(if cat == Ocio then acento else new Color(134, 143, 234))
        barra.setBackground(new Color(234, 236, 243))
        barra.setBorderPainted(false)
        linea.add(barra, BorderLayout.CENTER)
        val importe = etiqueta(dinero(total), 11, tenue)
        importe.setPreferredSize(new Dimension(88, 21))
        importe.setHorizontalAlignment(SwingConstants.RIGHT)
        linea.add(importe, BorderLayout.EAST)
        categoriasGrafico.add(linea)
        categoriasGrafico.add(Box.createVerticalStrut(8))
      }
    categoriasGrafico.revalidate()
    categoriasGrafico.repaint()

  private def mostrarError(mensaje: String): Unit =
    JOptionPane.showMessageDialog(this, mensaje, "Revisa los datos", JOptionPane.WARNING_MESSAGE)
