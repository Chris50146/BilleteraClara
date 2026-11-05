//> using scala 3.9.0
package billeteraclara

import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.{MouseAdapter, MouseEvent}
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
  val fondo = new Color(246, 247, 252)
  val blanco = Color.WHITE
  val tinta = new Color(30, 35, 62)
  val tenue = new Color(112, 121, 145)
  val borde = new Color(227, 231, 242)
  val acento = new Color(97, 85, 230)
  val acentoClaro = new Color(235, 233, 255)
  val verde = new Color(20, 163, 132)
  val verdeClaro = new Color(223, 249, 240)
  val rojo = new Color(230, 91, 112)
  val rojoClaro = new Color(255, 236, 241)
  val azulOscuro = new Color(32, 36, 80)

  def fuente(tamano: Int, negrita: Boolean = false): Font =
    val preferida = if System.getProperty("os.name").toLowerCase.contains("mac") then "Avenir Next" else "SansSerif"
    new Font(preferida, if negrita then Font.BOLD else Font.PLAIN, tamano)

  def etiqueta(texto: String, tamano: Int = 14, color: Color = tinta,
               negrita: Boolean = false): JLabel =
    val label = new JLabel(texto)
    label.setFont(fuente(tamano, negrita))
    label.setForeground(color)
    label

  def boton(texto: String, principal: Boolean = false): JButton =
    val boton = new JButton(texto):
      private var encima = false
      addMouseListener(new MouseAdapter:
        override def mouseEntered(e: MouseEvent): Unit =
          encima = true
          repaint()
        override def mouseExited(e: MouseEvent): Unit =
          encima = false
          repaint()
      )
      override def paintComponent(g: Graphics): Unit =
        val g2 = g.create().asInstanceOf[Graphics2D]
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        val base = if principal then acento else new Color(239, 241, 249)
        val color = if encima then (if principal then new Color(77, 66, 204) else new Color(226, 229, 246)) else base
        g2.setColor(color)
        g2.fillRoundRect(0, 0, getWidth, getHeight, 16, 16)
        g2.dispose()
        super.paintComponent(g)
    boton.setUI(new javax.swing.plaf.basic.BasicButtonUI())
    boton.setContentAreaFilled(false)
    boton.setFont(fuente(13, true))
    boton.setFocusPainted(false)
    boton.setBorder(new EmptyBorder(11, 17, 11, 17))
    boton.setForeground(if principal then blanco else acento)
    boton.setOpaque(false)
    boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))
    boton

  def campo(): JTextField =
    val campo = new JTextField()
    campo.setFont(fuente(14))
    campo.setForeground(tinta)
    campo.setBackground(blanco)
    campo.setCaretColor(acento)
    campo.setSelectionColor(acentoClaro)
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
    for n <- 5 to 1 by -1 do
      g2.setColor(new Color(42, 51, 109, 3))
      g2.fillRoundRect(n, n + 2, getWidth - n * 2, getHeight - n * 2 - 2, radio, radio)
    g2.setColor(color)
    g2.fillRoundRect(1, 1, getWidth - 2, getHeight - 6, radio, radio)
    g2.dispose()
    super.paintComponent(g)

private class Logo extends JComponent:
  setPreferredSize(new Dimension(58, 58))
  override def paintComponent(g: Graphics): Unit =
    val g2 = g.create().asInstanceOf[Graphics2D]
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g2.setColor(new Color(255, 255, 255, 45))
    g2.fillRoundRect(0, 0, 58, 58, 18, 18)
    g2.setColor(Color.WHITE)
    g2.fillRoundRect(13, 19, 32, 24, 8, 8)
    g2.setColor(new Color(122, 233, 203))
    g2.fillRoundRect(27, 27, 23, 11, 7, 7)
    g2.setColor(new Color(33, 51, 96))
    g2.fillOval(39, 31, 4, 4)
    g2.dispose()

private class Hero extends JPanel:
  setOpaque(false)
  setLayout(new BorderLayout())
  override def paintComponent(g: Graphics): Unit =
    val g2 = g.create().asInstanceOf[Graphics2D]
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g2.setPaint(new GradientPaint(0, 0, new Color(52, 51, 126), getWidth.toFloat, getHeight.toFloat,
      new Color(103, 91, 224)))
    g2.fillRoundRect(0, 0, getWidth, getHeight, 30, 30)
    g2.setColor(new Color(255, 255, 255, 12))
    g2.fillOval(getWidth - 185, -115, 260, 260)
    g2.fillOval(getWidth - 310, 105, 170, 170)
    g2.dispose()
    super.paintComponent(g)

private class BarraColor(color: Color) extends JProgressBar(0, 100):
  setOpaque(false)
  setBorderPainted(false)
  setForeground(color)
  override def paintComponent(g: Graphics): Unit =
    val g2 = g.create().asInstanceOf[Graphics2D]
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g2.setColor(new Color(231, 234, 245))
    g2.fillRoundRect(0, 0, getWidth, getHeight, getHeight, getHeight)
    val ancho = (getWidth * getValue / 100.0).toInt
    if ancho > 0 then
      g2.setColor(getForeground)
      g2.fillRoundRect(0, 0, ancho, getHeight, getHeight, getHeight)
    g2.dispose()

private class Burbuja(texto: String, fondo: Color) extends JLabel(texto, SwingConstants.CENTER):
  setOpaque(false)
  setFont(Estilo.fuente(18))
  setPreferredSize(new Dimension(38, 38))
  override def paintComponent(g: Graphics): Unit =
    val g2 = g.create().asInstanceOf[Graphics2D]
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g2.setColor(fondo)
    g2.fillOval(0, 0, getWidth, getHeight)
    g2.dispose()
    super.paintComponent(g)

private class VentanaBilletera extends JFrame("Billetera Clara"):
  import Estilo.*

  // La referencia al estado del dominio y las animaciones pertenecen a la interfaz.
  private var actual: Billetera = DatosDemo.ejemplo match
    case Exito(billetera) => billetera
    case Error(mensaje) => throw new IllegalStateException(mensaje)

  private val saldoValor = etiqueta("", 32, tinta, true)
  private val ingresosValor = etiqueta("", 25, verde, true)
  private val gastosValor = etiqueta("", 25, rojo, true)
  private val diagnosticoValor = etiqueta("", 15, tinta, true)
  private val ocioValor = etiqueta("", 13, tenue)
  private val totalesAuditoriaValor = etiqueta("", 12, tenue)
  private val barraOcio = new BarraColor(acento)
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
  private val aviso = etiqueta("", 12, acento, true)
  private var formularioPanel: JPanel = null
  private var movimientosPanel: JPanel = null
  private var animacionScroll: Timer = null
  private var animacionOcio: Timer = null
  private var temporizadorAviso: Timer = null

  setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
  setMinimumSize(new Dimension(980, 700))
  setSize(1280, 900)
  setLocationRelativeTo(null)
  getContentPane.setBackground(fondo)

  private val raiz = new JPanel(new BorderLayout(0, 18))
  raiz.setBackground(fondo)
  raiz.setBorder(new EmptyBorder(22, 27, 14, 27))
  setContentPane(raiz)
  raiz.add(crearCabecera(), BorderLayout.NORTH)

  private val contenido = new JPanel()
  contenido.setOpaque(false)
  contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS))
  contenido.add(crearResumen())
  contenido.add(Box.createVerticalStrut(18))
  contenido.add(crearCuerpo())

  private val scroll = new JScrollPane(contenido)
  scroll.setBorder(null)
  scroll.getViewport.setBackground(fondo)
  scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER)
  scroll.getVerticalScrollBar.setUnitIncrement(18)
  raiz.add(scroll, BorderLayout.CENTER)
  aviso.setBorder(new EmptyBorder(0, 8, 0, 8))
  aviso.setVisible(false)
  raiz.add(aviso, BorderLayout.SOUTH)

  prepararEventos()
  actualizarTodo()

  override def dispose(): Unit =
    if animacionScroll != null then animacionScroll.stop()
    if animacionOcio != null then animacionOcio.stop()
    if temporizadorAviso != null then temporizadorAviso.stop()
    super.dispose()

  private def crearCabecera(): JPanel =
    val panel = new Hero()
    panel.setPreferredSize(new Dimension(100, 178))
    panel.setBorder(new EmptyBorder(20, 26, 20, 26))
    val izquierda = new JPanel()
    izquierda.setOpaque(false)
    izquierda.setLayout(new BoxLayout(izquierda, BoxLayout.Y_AXIS))
    val marca = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0))
    marca.setOpaque(false)
    marca.setAlignmentX(Component.LEFT_ALIGNMENT)
    marca.add(new Logo())
    val marcaTexto = new JPanel()
    marcaTexto.setOpaque(false)
    marcaTexto.setLayout(new BoxLayout(marcaTexto, BoxLayout.Y_AXIS))
    marcaTexto.add(etiqueta("BILLETERA CLARA", 16, blanco, true))
    marcaTexto.add(etiqueta("Tus finanzas, más simples", 11, new Color(216, 216, 255)))
    marca.add(marcaTexto)
    izquierda.add(marca)
    izquierda.add(Box.createVerticalStrut(11))
    izquierda.add(etiqueta("Tu dinero, con claridad.", 27, blanco, true))
    izquierda.add(Box.createVerticalStrut(2))
    izquierda.add(etiqueta("Organiza tus movimientos y decide con confianza.", 12,
      new Color(224, 225, 255)))
    panel.add(izquierda, BorderLayout.WEST)

    val derecha = new JPanel()
    derecha.setOpaque(false)
    derecha.setLayout(new BoxLayout(derecha, BoxLayout.Y_AXIS))
    val fecha = etiqueta(LocalDate.now().format(
      DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", java.util.Locale.forLanguageTag("es"))),
      12, new Color(225, 226, 255), true)
    fecha.setAlignmentX(Component.RIGHT_ALIGNMENT)
    derecha.add(fecha)
    derecha.add(Box.createVerticalGlue())
    val acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0))
    acciones.setOpaque(false)
    val ingreso = boton("＋ Ingreso", true)
    ingreso.addActionListener((_: ActionEvent) =>
      tipo.setSelectedItem("Ingreso")
      irA(formularioPanel)
      descripcion.requestFocusInWindow()
    )
    acciones.add(ingreso)
    val gasto = boton("− Gasto")
    gasto.addActionListener((_: ActionEvent) =>
      tipo.setSelectedItem("Gasto")
      irA(formularioPanel)
      descripcion.requestFocusInWindow()
    )
    acciones.add(gasto)
    derecha.add(acciones)
    derecha.add(Box.createVerticalStrut(11))
    val limpiar = boton("↺  Empezar en blanco")
    limpiar.setToolTipText("Quita todos los movimientos de la sesión")
    limpiar.setAlignmentX(Component.RIGHT_ALIGNMENT)
    limpiar.addActionListener((_: ActionEvent) =>
      val confirmar = actual.transacciones.isEmpty ||
        JOptionPane.showConfirmDialog(this, "¿Quitar todos los movimientos de esta sesión?",
          "Empezar en blanco", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION
      if confirmar then
        actual = Billetera.vacia(actual.usuario)
        actualizarTodo()
        mostrarAviso("✨  Listo: ahora puedes registrar tus propios movimientos.")
    )
    derecha.add(limpiar)
    panel.add(derecha, BorderLayout.EAST)
    panel

  private def crearResumen(): JPanel =
    val contenedor = new JPanel(new BorderLayout())
    contenedor.setOpaque(false)
    contenedor.setAlignmentX(Component.LEFT_ALIGNMENT)

    val tarjetas = new JPanel(new GridLayout(1, 3, 16, 0))
    tarjetas.setOpaque(false)
    tarjetas.add(tarjetaNumero("💳  SALDO DISPONIBLE", saldoValor, acento, acentoClaro))
    tarjetas.add(tarjetaNumero("↗  INGRESOS", ingresosValor, verde, verdeClaro))
    tarjetas.add(tarjetaNumero("↘  GASTOS", gastosValor, rojo, rojoClaro))
    contenedor.add(tarjetas, BorderLayout.CENTER)
    contenedor

  private def tarjetaNumero(titulo: String, valor: JLabel, color: Color,
      fondoIcono: Color): JPanel =
    val card = new Tarjeta()
    card.setPreferredSize(new Dimension(200, 136))
    card.setBorder(new EmptyBorder(22, 24, 21, 24))
    val interior = new JPanel()
    interior.setOpaque(false)
    interior.setLayout(new BoxLayout(interior, BoxLayout.Y_AXIS))
    val tituloValor = etiqueta(titulo, 11, color, true)
    tituloValor.setOpaque(true)
    tituloValor.setBackground(fondoIcono)
    tituloValor.setBorder(new EmptyBorder(5, 9, 5, 9))
    interior.add(tituloValor)
    interior.add(Box.createVerticalGlue())
    interior.add(valor)
    interior.add(Box.createVerticalStrut(5))
    interior.add(etiqueta("Actualizado al instante", 11, tenue))
    card.add(interior)
    card

  private def crearCuerpo(): JPanel =
    val cuerpo = new JPanel(new GridBagLayout())
    cuerpo.setOpaque(false)
    cuerpo.setAlignmentX(Component.LEFT_ALIGNMENT)
    val izquierda = new JPanel()
    izquierda.setOpaque(false)
    izquierda.setLayout(new BoxLayout(izquierda, BoxLayout.Y_AXIS))
    movimientosPanel = crearMovimientos()
    izquierda.add(movimientosPanel)
    izquierda.add(Box.createVerticalStrut(18))
    izquierda.add(crearGrafico())

    val derecha = new JPanel()
    derecha.setOpaque(false)
    derecha.setLayout(new BoxLayout(derecha, BoxLayout.Y_AXIS))
    formularioPanel = crearFormulario()
    derecha.add(formularioPanel)
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

  private def irA(panel: JPanel): Unit =
    if panel != null then
      if animacionScroll != null then animacionScroll.stop()
      val inicio = scroll.getViewport.getViewPosition.y
      val posicion = SwingUtilities.convertPoint(panel, 0, 0, contenido).y
      val maximo = math.max(0, contenido.getHeight - scroll.getViewport.getHeight)
      val destino = posicion.min(maximo)
      var paso = 0
      animacionScroll = new Timer(16, (_: ActionEvent) =>
        paso += 1
        val avance = 1.0 - math.pow(1.0 - paso / 15.0, 3)
        scroll.getViewport.setViewPosition(new Point(0,
          (inicio + (destino - inicio) * avance).toInt))
        if paso >= 15 then animacionScroll.stop()
      )
      animacionScroll.start()

  private def mostrarAviso(mensaje: String): Unit =
    aviso.setText(mensaje)
    aviso.setVisible(true)
    if temporizadorAviso != null then temporizadorAviso.stop()
    temporizadorAviso = new Timer(3500, (_: ActionEvent) => aviso.setVisible(false))
    temporizadorAviso.setRepeats(false)
    temporizadorAviso.start()

  private def animarOcio(destino: Int): Unit =
    if animacionOcio != null then animacionOcio.stop()
    val inicio = barraOcio.getValue
    var paso = 0
    animacionOcio = new Timer(18, (_: ActionEvent) =>
      paso += 1
      barraOcio.setValue(inicio + (destino - inicio) * paso / 15)
      if paso >= 15 then animacionOcio.stop()
    )
    animacionOcio.start()

  private def crearMovimientos(): JPanel =
    val card = new Tarjeta()
    card.setBorder(new EmptyBorder(24, 24, 18, 24))
    val cuerpo = new JPanel()
    cuerpo.setOpaque(false)
    cuerpo.setLayout(new BoxLayout(cuerpo, BoxLayout.Y_AXIS))
    val cabecera = new JPanel(new BorderLayout())
    cabecera.setOpaque(false)
    cabecera.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28))
    cabecera.add(etiqueta("📒  Movimientos", 20, tinta, true), BorderLayout.WEST)
    cabecera.add(cantidadLabel, BorderLayout.EAST)
    cuerpo.add(cabecera)
    cuerpo.add(Box.createVerticalStrut(18))
    val controles = new JPanel(new GridLayout(1, 2, 10, 0))
    controles.setOpaque(false)
    controles.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44))
    busqueda.setToolTipText("Buscar por descripción o categoría")
    busqueda.putClientProperty("JTextField.placeholderText", "Buscar movimiento...")
    busqueda.getAccessibleContext.setAccessibleName("Buscar por descripción o categoría")
    val buscarPanel = new JPanel(new BorderLayout(6, 0))
    buscarPanel.setOpaque(false)
    buscarPanel.add(etiqueta("Buscar", 12, tenue), BorderLayout.WEST)
    buscarPanel.add(busqueda, BorderLayout.CENTER)
    controles.add(buscarPanel)
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
    interior.add(etiqueta("🎨  Gastos por categoría", 18, tinta, true))
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
    interior.add(etiqueta("✨  Nuevo movimiento", 18, tinta, true))
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
    val registrar = boton("＋  Guardar movimiento", true)
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
    interior.add(etiqueta("🌿  Salud del presupuesto", 18, tinta, true))
    interior.add(Box.createVerticalStrut(14))
    interior.add(diagnosticoValor)
    interior.add(Box.createVerticalStrut(6))
    interior.add(ocioValor)
    interior.add(Box.createVerticalStrut(4))
    interior.add(totalesAuditoriaValor)
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
    interior.add(etiqueta("🔮  Simula una compra", 18, tinta, true))
    interior.add(Box.createVerticalStrut(5))
    interior.add(etiqueta("Explora el impacto antes de gastar.", 12, tenue))
    interior.add(Box.createVerticalStrut(17))
    simMonto.putClientProperty("JTextField.placeholderText", "0.00")
    decorarCombo(simCategoria)
    interior.add(campoConTitulo("MONTO EN BS", simMonto))
    interior.add(Box.createVerticalStrut(12))
    interior.add(campoConTitulo("CATEGORÍA", simCategoria))
    interior.add(Box.createVerticalStrut(14))
    val simular = boton("Ver proyección  →")
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
    combo.setFocusable(true)
    combo.setBorder(BorderFactory.createLineBorder(borde, 1, true))

  private def prepararEventos(): Unit =
    tipo.addActionListener((_: ActionEvent) => actualizarCategorias())
    actualizarCategorias()
    descripcion.addActionListener((_: ActionEvent) => monto.requestFocusInWindow())
    monto.addActionListener((_: ActionEvent) => registrarMovimiento())
    simMonto.addActionListener((_: ActionEvent) => simularCompra())
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
            // Mostrar el registro nuevo aunque hubiera filtros activos.
            busqueda.setText("")
            filtroTipo.setSelectedItem("Todos")
            descripcion.setText("")
            monto.setText("")
            actualizarTodo()
            mostrarAviso("✅  Movimiento guardado. Tu resumen ya está actualizado.")
            descripcion.requestFocusInWindow()

  private def simularCompra(): Unit =
    // No conservar una proyección anterior si el nuevo intento es inválido.
    resultadoSimulacion.removeAll()
    EntradaInterfaz.parsearMonto(simMonto.getText) match
      case Error(error) => mostrarError(error)
      case Exito(valor) =>
        actual.simularCompra(valor, simCategoria.getSelectedItem.asInstanceOf[Categoria]) match
          case Error(error) => mostrarError(error)
          case Exito(s) =>
            resultadoSimulacion.add(etiqueta("🔮  SALDO PROYECTADO", 11, acento, true))
            resultadoSimulacion.add(Box.createVerticalStrut(4))
            resultadoSimulacion.add(etiqueta(dinero(s.saldoProyectado), 22,
              if s.compraPosible then tinta else rojo, true))
            resultadoSimulacion.add(Box.createVerticalStrut(6))
            resultadoSimulacion.add(etiqueta(
              if s.compraPosible then "✅  Saldo suficiente" else "⚠️  Saldo insuficiente", 12,
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
      else if d.gastoOcio == 0 then "●  Sin gastos de ocio"
      else if d.estado == Saludable then "●  Ocio dentro del 20%" else "●  Atención al ocio")
    diagnosticoValor.setForeground(if d.estado == Saludable then verde else rojo)
    ocioValor.setText(f"Ocio: ${d.porcentajeOcio}%.1f%% de los gastos · ${dinero(d.gastoOcio)}")
    totalesAuditoriaValor.setText(
      s"Ingresos: ${dinero(actual.ingresos)} · Gastos: ${dinero(d.totalGastos)}")
    barraOcio.setForeground(if d.estado == Saludable then acento else rojo)
    animarOcio(d.porcentajeOcio.min(100.0).toInt)
    actualizarLista()
    actualizarGrafico()
    resultadoSimulacion.removeAll()
    resultadoSimulacion.add(etiqueta("💡  La simulación no modifica tus movimientos.", 12, tenue))
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
    cantidadLabel.setText(s"${encontrados.length} " +
      (if encontrados.length == 1 then "registro" else "registros"))
    lista.removeAll()
    if encontrados.isEmpty then
      val vacio = etiqueta("🪄  No hay movimientos para este filtro.", 13, tenue)
      vacio.setBorder(new EmptyBorder(24, 0, 24, 0))
      lista.add(vacio)
    else encontrados.foreach(tx => lista.add(filaMovimiento(tx)))
    lista.revalidate()
    lista.repaint()

  private def iconoCategoria(categoria: Categoria): String = categoria match
    case Salario => "💼"
    case Alimentacion => "🥗"
    case Transporte => "🚗"
    case Ocio => "🎬"
    case Hogar => "🏠"
    case Salud => "💊"
    case Otros => "✨"

  private def filaMovimiento(tx: Transaccion): JPanel =
    val fila = new JPanel(new BorderLayout(12, 0))
    fila.setOpaque(false)
    fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68))
    fila.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createMatteBorder(0, 0, 1, 0, borde),
      new EmptyBorder(10, 0, 10, 0)))

    val icono = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4))
    icono.setOpaque(false)
    icono.add(new Burbuja(iconoCategoria(tx.categoria),
      if tx.tipo == Ingreso then verdeClaro else acentoClaro))
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
      mostrarAviso("🗑️  Movimiento eliminado. El resumen se recalculó.")
    )
    acciones.add(borrar)
    fila.add(acciones, BorderLayout.EAST)
    fila

  private def actualizarGrafico(): Unit =
    categoriasGrafico.removeAll()
    val datos = actual.totalesPorCategoria.filter(_._2 > 0).sortBy(par => -par._2)
    if datos.isEmpty then
      categoriasGrafico.add(etiqueta("🫧  Tus gastos aparecerán aquí al registrarlos.", 12, tenue))
    else
      datos.foreach { case (cat, total) =>
        val proporcion = (total * 100 / actual.gastos).toInt
        val linea = new JPanel(new BorderLayout(12, 0))
        linea.setOpaque(false)
        linea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 31))
        val nombre = etiqueta(iconoCategoria(cat) + "  " + cat.nombre, 12, tinta)
        nombre.setPreferredSize(new Dimension(112, 21))
        linea.add(nombre, BorderLayout.WEST)
        val barra = new BarraColor(if cat == Ocio then acento else verde)
        barra.setValue(proporcion)
        barra.setPreferredSize(new Dimension(100, 9))
        val barraContenedor = new JPanel(new GridBagLayout())
        barraContenedor.setOpaque(false)
        val ajuste = new GridBagConstraints()
        ajuste.weightx = 1
        ajuste.fill = GridBagConstraints.HORIZONTAL
        barraContenedor.add(barra, ajuste)
        linea.add(barraContenedor, BorderLayout.CENTER)
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
