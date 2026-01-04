package igu;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.toedter.calendar.JDateChooser;
import java.awt.Desktop;
import java.awt.Dimension;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.*;
import javax.swing.*;

public class PanelReportes extends PanelDegradado {

    private Connection conexion;

    // Componentes del panel
    private JButton btnTuristas, btnRecursos, btnAlquileres, btnPorFecha, btnPorUsuario,btnBoletaMora;
    private JDateChooser jDateInicio, jDateFin;
    private JLabel lblDesde, lblHasta;

    public PanelReportes(Connection conexion) {
        this.conexion = conexion;
        
        initComponents();
     

    }

    // ======================================================
    // BOLETA DE PAGO INDIVIDUAL
    // ======================================================
    private void prepararBoletaEstandar() {
        String idPago = JOptionPane.showInputDialog(this, "Ingrese el ID del Pago (Ej: P008):", "Generar Boleta de Alquiler", JOptionPane.QUESTION_MESSAGE);
        
        if (idPago == null || idPago.trim().isEmpty()) return;
        idPago = idPago.trim().toUpperCase();

        try {
            // 1. CONSULTA COMPLETA (Pago + Alquiler + Turista + Promoción)
            String sql = """
                SELECT 
                    p.idPago, p.fechaPago, p.montoConIGV, p.metodoPago,
                    a.idAlquiler, a.mora, 
                    t.nombre, t.apellidos, t.dni,
                    pr.descripcion AS promoDesc
                FROM Pago p
                JOIN Alquiler a ON p.idAlquiler = a.idAlquiler
                JOIN Turista t ON a.idTurista = t.idTurista
                LEFT JOIN Promocion pr ON a.idPromocion = pr.idPromocion
                WHERE p.idPago = ? AND p.estado = 'Completado'
            """;
            
            PreparedStatement ps = conexion.prepareStatement(sql);
            ps.setString(1, idPago);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "No se encontró un pago completado con ese ID.");
                return;
            }

            // 2. RECUPERAR DATOS BÁSICOS
            String idAlquiler = rs.getString("idAlquiler");
            // Usamos Timestamp para tener FECHA y HORA
            java.sql.Timestamp fechaHoraPago = new java.sql.Timestamp(System.currentTimeMillis());
            String metodoPago = rs.getString("metodoPago");
            String cliente = rs.getString("nombre") + " " + rs.getString("apellidos");
            String dni = rs.getString("dni");
            String promoDesc = rs.getString("promoDesc");

            // 3. MATEMÁTICA: PAGO TOTAL - MORA = ALQUILER NETO PAGADO
            double totalPagado = rs.getDouble("montoConIGV"); 
            double moraRegistrada = rs.getDouble("mora");    
            if (rs.wasNull()) moraRegistrada = 0.0;

            double montoAlquilerNeto = totalPagado - moraRegistrada;

            if (montoAlquilerNeto <= 0) {
                 JOptionPane.showMessageDialog(this, "El pago corresponde solo a mora o es inválido.");
                 return;
            }

            // 4. CÁLCULO DE DESCUENTO (Retroactivo)
            // Si hubo descuento, calculamos cuál era el precio original para mostrarlo
            double porcentajeDesc = extraerPorcentaje(promoDesc); // Método auxiliar que ya tienes
            double montoDescuento = 0.0;
            double subtotalBruto = montoAlquilerNeto;

            if (porcentajeDesc > 0) {
                // Ejemplo: Pagó 90 con 10% desc -> Original era 100
                subtotalBruto = montoAlquilerNeto / (1.0 - porcentajeDesc);
                montoDescuento = subtotalBruto - montoAlquilerNeto;
            }

            // 5. LLAMAR AL PDF CON TODOS LOS DATOS
            generarPDFBoletaEstandar(
                idAlquiler,
                idPago,
                fechaHoraPago, // Enviamos timestamp
                metodoPago,
                cliente,
                dni,
                montoAlquilerNeto, // Lo que pagó finalmente por alquiler
                subtotalBruto,     // Precio antes de descuento
                montoDescuento,    // Cuánto ahorró
                promoDesc          // Nombre de la promo
            );

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al preparar boleta: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void generarPDFBoletaEstandar(String idAlquiler, String idPago, java.sql.Timestamp fechaHora, String metodoPago, String cliente, String dni, double montoFinal, double montoBruto, double descuento, String nombrePromo) {
        String ruta = crearRuta("TICKET_ALQ_" + idAlquiler + ".pdf");

        try {
            // FORMATOS
            java.text.SimpleDateFormat sdfFecha = new java.text.SimpleDateFormat("dd/MM/yyyy");
            java.text.SimpleDateFormat sdfHora = new java.text.SimpleDateFormat("HH:mm:ss");

            // 1. CONFIGURACIÓN
            Rectangle pageSize = new Rectangle(226, 750); // Altura dinámica
            Document doc = new Document(pageSize, 14, 14, 10, 10);
            PdfWriter.getInstance(doc, new FileOutputStream(ruta));
            doc.open();

            // 2. FUENTES
            com.itextpdf.text.Font fEmpresa = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.COURIER, 10, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font fCabecera = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.COURIER, 8, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font fNormal = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.COURIER, 8, com.itextpdf.text.Font.NORMAL);
            com.itextpdf.text.Font fTotal = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.COURIER, 12, com.itextpdf.text.Font.BOLD);

            // 3. ENCABEZADO
            Paragraph p = new Paragraph("ALQUILERES TURISTICOS\nDEL NORTE S.A.C.", fEmpresa);
            p.setAlignment(Element.ALIGN_CENTER);
            doc.add(p);
            p = new Paragraph("RUC: 20457896543\nPiura - Perú", fNormal);
            p.setAlignment(Element.ALIGN_CENTER);
            doc.add(p);

            agregarLineaPunteada(doc);

            // 4. DATOS TICKET
            p = new Paragraph("BOLETA DE VENTA ELECTRÓNICA", fCabecera);
            p.setAlignment(Element.ALIGN_CENTER);
            doc.add(p);
            p = new Paragraph("Nro: " + idPago + " (Ref: " + idAlquiler + ")", fNormal);
            p.setAlignment(Element.ALIGN_CENTER);
            doc.add(p);

            p = new Paragraph("Fecha: " + sdfFecha.format(fechaHora) + "  Hora: " + sdfHora.format(fechaHora), fNormal);
            p.setAlignment(Element.ALIGN_CENTER);
            doc.add(p);

            agregarLineaPunteada(doc);

            // 5. CLIENTE
            doc.add(new Paragraph("CLIENTE: " + cliente, fNormal));
            doc.add(new Paragraph("DNI:     " + dni, fNormal));

            agregarLineaPunteada(doc);

            // 6. DETALLE DE ÍTEMS
            doc.add(new Paragraph("CANT  DESCRIPCION             IMPORTE", fCabecera));
            doc.add(new Paragraph(" ", fNormal));

            PdfPTable tablaDet = new PdfPTable(3);
            tablaDet.setWidthPercentage(100);
            tablaDet.setWidths(new float[]{0.8f, 2.5f, 1.2f}); // Cant | Desc | Total

            // Consulta de Detalles
            String sqlDet = "SELECT r.tipo, d.horasUsadas, d.subTotal FROM DetalleAlquiler d JOIN Recursos r ON d.idRecurso = r.idRecursos WHERE d.idAlquiler = ?";
            PreparedStatement psDet = conexion.prepareStatement(sqlDet);
            psDet.setString(1, idAlquiler);
            ResultSet rsDet = psDet.executeQuery();

            while (rsDet.next()) {
                String item = rsDet.getString("tipo");
                if (item.length() > 16) {
                    item = item.substring(0, 16);
                }

                tablaDet.addCell(crearCelda(rsDet.getString("horasUsadas") + "h", fNormal, Element.ALIGN_LEFT));
                tablaDet.addCell(crearCelda(item, fNormal, Element.ALIGN_LEFT));
                tablaDet.addCell(crearCelda(String.format("%.2f", rsDet.getDouble("subTotal")), fNormal, Element.ALIGN_RIGHT));
            }
            doc.add(tablaDet);

            // 7. SECCIÓN: SUBTOTAL Y DESCUENTOS (Aquí es donde pediste el cambio)
            // Se muestra ANTES de la línea punteada de impuestos
            if (descuento > 0) {
                // Añadimos un pequeño espacio
                doc.add(new Paragraph(" ", fNormal));

                PdfPTable tablaDesc = new PdfPTable(2);
                tablaDesc.setWidthPercentage(100);
                tablaDesc.setWidths(new float[]{2f, 1f}); // Misma alineación que la tabla de abajo

                // Subtotal Bruto (Precio normal)
                tablaDesc.addCell(crearCelda("SUBTOTAL:", fNormal, Element.ALIGN_LEFT));
                tablaDesc.addCell(crearCelda(String.format("%.2f", montoBruto), fNormal, Element.ALIGN_RIGHT));

                // El Descuento Restando
                tablaDesc.addCell(crearCelda("DESCUENTO (" + (nombrePromo != null ? nombrePromo : "") + "):", fNormal, Element.ALIGN_LEFT));
                tablaDesc.addCell(crearCelda("-" + String.format("%.2f", descuento), fNormal, Element.ALIGN_RIGHT));

                doc.add(tablaDesc);
            }

            // AHORA SÍ: LA LÍNEA SEPARADORA
            agregarLineaPunteada(doc);

            // 8. IMPUESTOS (Alineados a la IZQUIERDA)
            // Cálculos finales sobre el monto real a pagar
            double subtotalNeto = montoFinal / 1.18;
            double igv = montoFinal - subtotalNeto;

            PdfPTable tablaImp = new PdfPTable(2);
            tablaImp.setWidthPercentage(100);
            tablaImp.setWidths(new float[]{2f, 1f});

            // Etiquetas a la Izquierda (ALIGN_LEFT)
            tablaImp.addCell(crearCelda("OP. GRAVADA:", fNormal, Element.ALIGN_LEFT));
            tablaImp.addCell(crearCelda(String.format("%.2f", subtotalNeto), fNormal, Element.ALIGN_RIGHT));

            tablaImp.addCell(crearCelda("I.G.V. (18%):", fNormal, Element.ALIGN_LEFT));
            tablaImp.addCell(crearCelda(String.format("%.2f", igv), fNormal, Element.ALIGN_RIGHT));

            doc.add(tablaImp);
            doc.add(new Paragraph("\n"));

            // 9. TOTAL GRANDE
            Paragraph pTotal = new Paragraph("TOTAL: S/ " + String.format("%.2f", montoFinal), fTotal);
            pTotal.setAlignment(Element.ALIGN_RIGHT);
            doc.add(pTotal);

            // 10. MÉTODO DE PAGO (Al final)
            Paragraph pMetodo = new Paragraph("FORMA DE PAGO: " + metodoPago.toUpperCase(), fNormal);
            pMetodo.setAlignment(Element.ALIGN_RIGHT);
            doc.add(pMetodo);

            // PIE
            doc.add(new Paragraph("\n"));
            p = new Paragraph("Comprobante solo por servicio de alquiler.\nNo incluye penalidades.", fNormal);
            p.setAlignment(Element.ALIGN_CENTER);
            doc.add(p);

            doc.add(new Paragraph("\nGRACIAS POR SU PREFERENCIA", fCabecera));
            p = new Paragraph("www.alquileresnorte.com", fNormal);
            p.setAlignment(Element.ALIGN_CENTER);
            doc.add(p);

            doc.close();
            abrirPDF(ruta);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    
    private PdfPCell crearCelda(String texto, com.itextpdf.text.Font fuente, int alineacion) {
        // Creamos la celda con el texto y la fuente
        PdfPCell celda = new PdfPCell(new Paragraph(texto, fuente));

        // Quitamos los bordes para que parezca una lista limpia
        celda.setBorder(Rectangle.NO_BORDER);

        // Alineamos (Izquierda, Derecha o Centro)
        celda.setHorizontalAlignment(alineacion);

        // Un poco de aire abajo para que no se vea todo apretado
        celda.setPaddingBottom(3f);

        return celda;
    }
   

    private double extraerPorcentaje(String texto) {
        if (texto == null) {
            return 0.0;
        }
        texto = texto.replaceAll("[^0-9]", "");
        if (texto.isEmpty()) {
            return 0.0;
        }
        return Double.parseDouble(texto) / 100.0;
    }


    private void initComponents() {
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        setSize(640, 560);
        setOpaque(false); // Importante para ver el degradado de fondo

        // --- COLORES ---
        // Azul Corporativo (El mismo de tus botones)
        java.awt.Color colorPrimario = new java.awt.Color(33, 97, 140); 
        java.awt.Color colorBlanco = java.awt.Color.WHITE;

        // --- FUENTES (Mantenemos Segoe UI Emoji para los íconos) ---
        java.awt.Font fontEmoji = new java.awt.Font("Segoe UI Emoji", java.awt.Font.PLAIN, 14);
        java.awt.Font fontTitulo = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 24);
        java.awt.Font fontSubtitulo = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14);

        // --- TÍTULO PRINCIPAL (AHORA AZUL) ---
        JLabel titulo = new JLabel("CENTRO DE REPORTES Y FACTURACIÓN");
        titulo.setHorizontalAlignment(SwingConstants.CENTER);
        titulo.setFont(fontTitulo);
        titulo.setForeground(colorPrimario); // <--- CAMBIO: Color Azul
        add(titulo, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 20, 640, 30));

        // --- LÍNEA SEPARADORA 1 (AHORA AZUL) ---
        JPanel linea1 = new JPanel();
        linea1.setBackground(colorPrimario); // <--- CAMBIO: Color Azul
        add(linea1, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 60, 540, 2));

        // --- SECCIÓN 1: INFORMACIÓN GENERAL ---
        JLabel lblSec1 = new JLabel("INFORMACIÓN GENERAL");
        lblSec1.setFont(fontSubtitulo);
        lblSec1.setForeground(colorPrimario); // <--- CAMBIO: Color Azul
        add(lblSec1, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 80, -1, -1));

        // Botones (Ya estaban bien, usan fontEmoji)
        btnTuristas = crearBoton("🧍‍♂️ Reporte de Turistas", fontEmoji);
        btnTuristas.addActionListener(evt -> generarReporteTuristas());
        add(btnTuristas, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 110, 250, 40));

        btnRecursos = crearBoton("📦 Inventario de Recursos", fontEmoji);
        btnRecursos.addActionListener(evt -> generarReporteRecursos());
        add(btnRecursos, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 160, 250, 40));

        btnAlquileres = crearBoton("📊 Alquileres (Histórico)", fontEmoji);
        btnAlquileres.addActionListener(evt -> generarReporteAlquileres());
        add(btnAlquileres, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 210, 250, 40));

        // --- SECCIÓN 2: FILTROS ---
        JLabel lblSec2 = new JLabel("FILTROS Y RENDIMIENTO");
        lblSec2.setFont(fontSubtitulo);
        lblSec2.setForeground(colorPrimario); // <--- CAMBIO: Color Azul
        add(lblSec2, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 80, -1, -1));

        // Panel Fechas
        JPanel pnlFechas = new JPanel();
        pnlFechas.setLayout(null);
        pnlFechas.setBackground(new java.awt.Color(255, 255, 255, 100)); // Un poco más blanco para contraste
        pnlFechas.setBorder(BorderFactory.createLineBorder(colorPrimario, 1)); // Borde Azul
        
        lblDesde = new JLabel("Desde:");
        lblDesde.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        lblDesde.setForeground(colorPrimario); // <--- CAMBIO: Texto Azul
        pnlFechas.add(lblDesde);
        lblDesde.setBounds(10, 10, 50, 20);

        jDateInicio = new JDateChooser();
        pnlFechas.add(jDateInicio);
        jDateInicio.setBounds(60, 8, 120, 25);

        lblHasta = new JLabel("Hasta:");
        lblHasta.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        lblHasta.setForeground(colorPrimario); // <--- CAMBIO: Texto Azul
        pnlFechas.add(lblHasta);
        lblHasta.setBounds(10, 40, 50, 20);

        jDateFin = new JDateChooser();
        pnlFechas.add(jDateFin);
        jDateFin.setBounds(60, 38, 120, 25);
        
        // Botón Lupa
        JButton btnBuscarFecha = new JButton(); 
        // 1. CARGAMOS LA IMAGEN
        // Asegúrate que la ruta empiece con "/" y sea correcta según tu paquete
        try {
            // Opción A: Si usas 32x32 directo
            ImageIcon iconoOriginal = new ImageIcon(getClass().getResource("/img/lupa.png"));
            
            // Opción B: Si descargaste una imagen grande y quieres redimensionarla por código (Más seguro)
            java.awt.Image imagen = iconoOriginal.getImage(); 
            java.awt.Image imagenRedimensionada = imagen.getScaledInstance(24, 24, java.awt.Image.SCALE_SMOOTH); // 24x24 px
            ImageIcon iconoFinal = new ImageIcon(imagenRedimensionada);
            
            btnBuscarFecha.setIcon(iconoFinal);
            
        } catch (Exception e) {
            // Si falla la imagen, ponemos texto de respaldo
            btnBuscarFecha.setText("VER"); 
            System.out.println("olaaa");
        }
        
        
        btnBuscarFecha.setBackground(colorPrimario); // Fondo Azul
        btnBuscarFecha.setForeground(colorBlanco);   // Lupa Blanca
        btnBuscarFecha.setBorderPainted(false);
        btnBuscarFecha.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnBuscarFecha.addActionListener(evt -> generarReporteAlquileresPorFecha());
        pnlFechas.add(btnBuscarFecha);
        btnBuscarFecha.setBounds(190, 8, 50, 55);

        add(pnlFechas, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 110, 250, 75));

        btnPorUsuario = crearBoton("👤 Rendimiento por Usuario", fontEmoji);
        btnPorUsuario.addActionListener(evt -> generarReporteAlquileresPorUsuario());
        add(btnPorUsuario, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 210, 250, 40));

        // --- SECCIÓN 3: CAJA ---
        
        // Línea Separadora 2 (AHORA AZUL)
        JPanel linea2 = new JPanel();
        linea2.setBackground(colorPrimario); // <--- CAMBIO: Color Azul
        add(linea2, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 280, 540, 2));

        JLabel lblSec3 = new JLabel("FACTURACIÓN Y CAJA");
        lblSec3.setFont(fontSubtitulo);
        lblSec3.setForeground(colorPrimario); // <--- CAMBIO: Color Azul
        add(lblSec3, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 300, -1, -1));

        // Botones de acción
        JButton btnBoletaCaja = crearBotonDestacado("💳 Reimprimir Boleta de Alquiler / Pago", fontEmoji);
        btnBoletaCaja.addActionListener(evt -> prepararBoletaEstandar());
        add(btnBoletaCaja, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 330, 540, 45));

        btnBoletaMora = crearBoton("⚠️ Generar Ticket de Mora / Penalidad", fontEmoji);
        btnBoletaMora.addActionListener(evt -> generarBoletaMora());
        add(btnBoletaMora, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 390, 540, 45));
    }

    // --- MÉTODOS DE CREACIÓN DE BOTONES MEJORADOS ---

    private JButton crearBoton(String texto, java.awt.Font fuenteEmoji) {
        JButton boton = new JButton(texto);
        boton.setFont(fuenteEmoji); // Aplicamos la fuente que soporta emojis
        boton.setBackground(java.awt.Color.WHITE);
        boton.setForeground(new java.awt.Color(33, 97, 140));
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new java.awt.Color(33, 97, 140), 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        boton.setHorizontalAlignment(SwingConstants.LEFT);
        boton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                boton.setBackground(new java.awt.Color(240, 245, 250));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                boton.setBackground(java.awt.Color.WHITE);
            }
        });
        return boton;
    }

    private JButton crearBotonDestacado(String texto, java.awt.Font fuenteEmoji) {
        JButton boton = new JButton(texto);
        // Usamos la misma fuente emoji pero en negrita si es posible, o la base
        boton.setFont(new java.awt.Font("Segoe UI Emoji", java.awt.Font.BOLD, 14)); 
        boton.setBackground(new java.awt.Color(33, 97, 140));
        boton.setForeground(java.awt.Color.WHITE);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        boton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                boton.setBackground(new java.awt.Color(23, 77, 110));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                boton.setBackground(new java.awt.Color(33, 97, 140));
            }
        });
        return boton;
    }
    private void generarPDFBoletaMora(String idAlquiler, String cliente, String dni, double moraTotal) {
        String ruta = crearRuta("TICKET_MORA_" + idAlquiler + ".pdf");

        try {
            // FORMATOS DE FECHA Y HORA (Hora actual del sistema)
            java.util.Date ahora = new java.util.Date();
            java.text.SimpleDateFormat sdfFecha = new java.text.SimpleDateFormat("dd/MM/yyyy");
            java.text.SimpleDateFormat sdfHora = new java.text.SimpleDateFormat("HH:mm:ss");

            // 1. CONFIGURACIÓN TICKET (80mm)
            Rectangle pageSize = new Rectangle(226, 750);
            Document doc = new Document(pageSize, 14, 14, 10, 10);
            PdfWriter.getInstance(doc, new FileOutputStream(ruta));
            doc.open();

            // 2. FUENTES
            com.itextpdf.text.Font fEmpresa = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.COURIER, 10, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font fCabecera = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.COURIER, 8, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font fNormal = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.COURIER, 8, com.itextpdf.text.Font.NORMAL);
            com.itextpdf.text.Font fTotal = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.COURIER, 12, com.itextpdf.text.Font.BOLD);

            // 3. CABECERA EMPRESA
            Paragraph p = new Paragraph("ALQUILERES TURISTICOS\nDEL NORTE S.A.C.", fEmpresa);
            p.setAlignment(Element.ALIGN_CENTER);
            doc.add(p);
            
            p = new Paragraph("RUC: 20457896543\nPiura - Perú", fNormal);
            p.setAlignment(Element.ALIGN_CENTER);
            doc.add(p);
            
            agregarLineaPunteada(doc);

            // 4. DATOS DOCUMENTO
            p = new Paragraph("TICKET DE PENALIDAD", fCabecera);
            p.setAlignment(Element.ALIGN_CENTER);
            doc.add(p);
            
            p = new Paragraph("Serie: M001-" + idAlquiler, fNormal);
            p.setAlignment(Element.ALIGN_CENTER);
            doc.add(p);
            
            // FECHA Y HORA ACTUAL (Formateada)
            p = new Paragraph("Fecha: " + sdfFecha.format(ahora) + "  Hora: " + sdfHora.format(ahora), fNormal);
            p.setAlignment(Element.ALIGN_CENTER);
            doc.add(p);

            agregarLineaPunteada(doc);

            // 5. CLIENTE
            doc.add(new Paragraph("CLIENTE: " + cliente, fNormal));
            doc.add(new Paragraph("DNI:     " + dni, fNormal));

            agregarLineaPunteada(doc);

            // 6. DETALLE DE VEHÍCULOS CON MORA
            p = new Paragraph("DETALLE DE RETRASOS", fCabecera);
            p.setAlignment(Element.ALIGN_CENTER);
            doc.add(p);
            doc.add(new Paragraph(" ", fNormal));

            // Tabla de detalle
            PdfPTable tablaDetalle = new PdfPTable(2);
            tablaDetalle.setWidthPercentage(100);
            tablaDetalle.setWidths(new float[]{2.8f, 1.2f}); // Columna ancha izq, estrecha der

            String sqlDetalle = """
                SELECT r.tipo, d.idRecurso, d.moraGenerada,
                       a.horaInicio, d.horasUsadas, 
                       d.horaDevolucionReal
                FROM DetalleAlquiler d
                JOIN Recursos r ON d.idRecurso = r.idRecursos
                JOIN Alquiler a ON d.idAlquiler = a.idAlquiler
                WHERE d.idAlquiler = ? AND d.moraGenerada > 0
            """;

            PreparedStatement ps = conexion.prepareStatement(sqlDetalle);
            ps.setString(1, idAlquiler);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String tipo = rs.getString("tipo");
                
                // Cálculo de Hora Pactada
                java.sql.Time hInicio = rs.getTime("horaInicio");
                int duracion = rs.getInt("horasUsadas");
                java.time.LocalTime localInicio = hInicio.toLocalTime();
                java.time.LocalTime localPactada = localInicio.plusHours(duracion);
                String horaPactadaStr = localPactada.toString();
                if(horaPactadaStr.length() > 5) horaPactadaStr = horaPactadaStr.substring(0, 5);

                // Hora Real
                String hReal = rs.getString("horaDevolucionReal");
                if(hReal != null && hReal.length() > 8) hReal = hReal.substring(0, 8);

                String descripcion = tipo + "\n Pactado: " + horaPactadaStr + "\n Devol.:  " + hReal;
                
                // Celdas
                tablaDetalle.addCell(crearCeldaSinBorde(descripcion, fNormal, Element.ALIGN_LEFT));
                
                PdfPCell cMonto = crearCeldaSinBorde(String.format("%.2f", rs.getDouble("moraGenerada")), fNormal, Element.ALIGN_RIGHT);
                cMonto.setVerticalAlignment(Element.ALIGN_BOTTOM);
                tablaDetalle.addCell(cMonto);
                
                // Espacio
                PdfPCell cEspacio = crearCeldaSinBorde(" ", fNormal, Element.ALIGN_LEFT);
                cEspacio.setColspan(2); 
                tablaDetalle.addCell(cEspacio);
            }

            doc.add(tablaDetalle);
            agregarLineaPunteada(doc);

            // 7. TOTALES E IMPUESTOS (Alineados a la IZQUIERDA)
            
            double subtotal = moraTotal / 1.18;
            double igv = moraTotal - subtotal;

            PdfPTable tablaTotales = new PdfPTable(2);
            tablaTotales.setWidthPercentage(100);
            tablaTotales.setWidths(new float[]{2f, 1f}); // Columna Izquierda más ancha para el texto

            // AQUÍ EL CAMBIO: Element.ALIGN_LEFT para las etiquetas
            tablaTotales.addCell(crearCeldaSinBorde("OP. GRAVADA:", fNormal, Element.ALIGN_LEFT));
            tablaTotales.addCell(crearCeldaSinBorde(String.format("%.2f", subtotal), fNormal, Element.ALIGN_RIGHT));
            
            tablaTotales.addCell(crearCeldaSinBorde("I.G.V. (18%):", fNormal, Element.ALIGN_LEFT));
            tablaTotales.addCell(crearCeldaSinBorde(String.format("%.2f", igv), fNormal, Element.ALIGN_RIGHT));
            
            doc.add(tablaTotales);

            doc.add(new Paragraph("\n"));

            // TOTAL GRANDE (Derecha)
            Paragraph pTotal = new Paragraph("TOTAL MORA: S/ " + String.format("%.2f", moraTotal), fTotal);
            pTotal.setAlignment(Element.ALIGN_RIGHT);
            doc.add(pTotal);

            // 8. PIE
            doc.add(new Paragraph("\n"));
            p = new Paragraph("Penalidad por devolución tardía.\nNo incluye costo de alquiler.", fNormal);
            p.setAlignment(Element.ALIGN_CENTER);
            doc.add(p);
            
            doc.add(new Paragraph("\nGRACIAS POR SU PREFERENCIA", fCabecera));
            p = new Paragraph("www.alquileresnorte.com", fNormal);
            p.setAlignment(Element.ALIGN_CENTER);
            doc.add(p);
            
            doc.close();
            abrirPDF(ruta);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error Ticket Mora: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // --- UTILS PARA DISEÑO TICKET ---
       private void agregarLineaPunteada(Document doc) throws DocumentException {
        Paragraph p = new Paragraph("------------------------------------------------", new Font(Font.FontFamily.COURIER, 8));
        p.setAlignment(Element.ALIGN_CENTER);
        doc.add(p);
    }

    private com.itextpdf.text.pdf.PdfPCell crearCeldaSinBorde(String texto, Font fuente, int alineacion) {
        com.itextpdf.text.pdf.PdfPCell celda = new com.itextpdf.text.pdf.PdfPCell(new Paragraph(texto, fuente));
        celda.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
        celda.setHorizontalAlignment(alineacion);
        return celda;
    }

   
    private void generarBoletaMora() {
        String idAlquiler = JOptionPane.showInputDialog(this, "Ingrese el ID del alquiler:", "Boleta por Mora", JOptionPane.QUESTION_MESSAGE);

        if (idAlquiler == null || idAlquiler.trim().isEmpty()) {
            return;
        }
        idAlquiler = idAlquiler.trim().toUpperCase();

        try {
            String sql = "SELECT estado, mora, horaFinal, horaFinalReal, "
                    + "(SELECT nombre + ' ' + apellidos FROM Turista WHERE idTurista = Alquiler.idTurista) as cliente, "
                    + "(SELECT dni FROM Turista WHERE idTurista = Alquiler.idTurista) as dni "
                    + "FROM Alquiler WHERE idAlquiler = ?";

            PreparedStatement ps = conexion.prepareStatement(sql);
            ps.setString(1, idAlquiler);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "No existe un alquiler con ese ID", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String estado = rs.getString("estado");
            double mora = rs.getDouble("mora");

            // Validaciones
            if (!"FINALIZADO".equalsIgnoreCase(estado)) {
                JOptionPane.showMessageDialog(this, "El alquiler debe estar FINALIZADO para cobrar mora.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (mora <= 0) {
                JOptionPane.showMessageDialog(this, "Este alquiler NO tiene mora registrada.", "Información", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Generar el PDF detallado
            generarPDFBoletaMora(
                    idAlquiler,
                    rs.getString("cliente"),
                    rs.getString("dni"),
                    mora
            );

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    

    // ======================================================
   

    // ======================================================
    // MÉTODOS DE GENERACIÓN DE REPORTES
    // ======================================================
    private String crearRuta(String nombreArchivo) {
        String rutaCarpeta = System.getProperty("user.home") + "\\reportes\\";
        File carpeta = new File(rutaCarpeta);
        if (!carpeta.exists()) {
            carpeta.mkdirs();
        }
        return rutaCarpeta + nombreArchivo;
    }

    

    private void abrirPDF(String ruta) {
        try {
            Desktop.getDesktop().open(new File(ruta));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "No se pudo abrir el PDF: " + e.getMessage());
        }
    }

    // ======================================================
    // REPORTE DE TURISTAS (orden alfabético)
   private void generarReporteTuristas() {
        String ruta = crearRuta("reporte_turistas.pdf");
        
        // Colores Corporativos (Puedes cambiarlos por los de tu empresa)
        BaseColor colorPrimario = new BaseColor(33, 97, 140); // Azul oscuro profesional
        BaseColor colorFondoGris = new BaseColor(242, 243, 244); // Gris muy suave

        try {
            // Configuración del documento con márgenes más amplios
            Document doc = new Document(PageSize.A4, 30, 30, 40, 40);
            PdfWriter.getInstance(doc, new FileOutputStream(ruta));
            doc.open();

            // 1. ENCABEZADO CON FECHA
            agregarEncabezadoProfesional(doc, "LISTADO GENERAL DE TURISTAS", colorPrimario);

            // 2. CONFIGURACIÓN DE LA TABLA
            PdfPTable tabla = new PdfPTable(6);
            tabla.setWidthPercentage(100);
            // Ajustamos anchos: ID corto, Nombres largos, DNI medio...
            // Suma de proporciones: 1+3+3+2+2+2.5 = 13.5 partes
            tabla.setWidths(new float[]{1f, 3f, 3f, 2f, 2f, 2.5f}); 
            tabla.setSpacingBefore(20f); // Espacio entre título y tabla

            // 3. CABECERA DE TABLA (Estilizada)
            String[] columnas = {"ID", "Nombres", "Apellidos", "DNI / Pasaporte", "Nacionalidad", "Contacto"};
            for (String col : columnas) {
                tabla.addCell(crearCeldaHeader(col, colorPrimario));
            }

            // 4. CUERPO DE DATOS (Con filas alternadas)
            String sql = """
                SELECT idTurista, nombre, apellidos, dni, nacionalidad, contacto
                FROM Turista
                ORDER BY apellidos ASC, nombre ASC
            """;

            try (Statement st = conexion.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                boolean filaImpar = true;
                
                while (rs.next()) {
                    BaseColor colorFondo = filaImpar ? BaseColor.WHITE : colorFondoGris;
                    
                    // Usamos un helper para limpiar datos nulos
                    tabla.addCell(crearCeldaData(rs.getString("idTurista"), colorFondo, Element.ALIGN_CENTER));
                    tabla.addCell(crearCeldaData(rs.getString("nombre"), colorFondo, Element.ALIGN_LEFT));
                    tabla.addCell(crearCeldaData(rs.getString("apellidos"), colorFondo, Element.ALIGN_LEFT));
                    tabla.addCell(crearCeldaData(rs.getString("dni"), colorFondo, Element.ALIGN_CENTER));
                    tabla.addCell(crearCeldaData(rs.getString("nacionalidad"), colorFondo, Element.ALIGN_CENTER));
                    tabla.addCell(crearCeldaData(rs.getString("contacto"), colorFondo, Element.ALIGN_LEFT));
                    
                    filaImpar = !filaImpar; // Alternar color
                }
            }

            doc.add(tabla);

            // 5. PIE DE REPORTE (Resumen)
            doc.add(new Paragraph("\n"));
            Paragraph fin = new Paragraph("Fin del reporte - Documento generado por el sistema.", 
                    new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 8, com.itextpdf.text.Font.ITALIC, BaseColor.GRAY));
            fin.setAlignment(Element.ALIGN_CENTER);
            doc.add(fin);

            doc.close();
            abrirPDF(ruta);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al generar reporte: " + e.getMessage());
            e.printStackTrace();
        }
    }
   // --- ESTILOS DE REPORTE PROFESIONAL ---

    // 1. Encabezado con Título y Fecha alineados
    private void agregarEncabezadoProfesional(Document doc, String titulo, BaseColor color) throws DocumentException {
        // Tabla invisible de 2 columnas para el encabezado
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        
        // Título Empresa / Reporte
        com.itextpdf.text.Font fontTitulo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 14, com.itextpdf.text.Font.BOLD, color);
        PdfPCell cellTitulo = new PdfPCell(new Paragraph(titulo, fontTitulo));
        cellTitulo.setBorder(Rectangle.NO_BORDER);
        cellTitulo.setVerticalAlignment(Element.ALIGN_BOTTOM);
        
        // Fecha y Hora actual
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        String fechaStr = "Fecha: " + sdf.format(new java.util.Date());
        com.itextpdf.text.Font fontFecha = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 9, com.itextpdf.text.Font.NORMAL, BaseColor.GRAY);
        
        PdfPCell cellFecha = new PdfPCell(new Paragraph("ALQUILERES DEL NORTE S.A.C.\n" + fechaStr, fontFecha));
        cellFecha.setBorder(Rectangle.NO_BORDER);
        cellFecha.setHorizontalAlignment(Element.ALIGN_RIGHT);
        
        headerTable.addCell(cellTitulo);
        headerTable.addCell(cellFecha);
        
        doc.add(headerTable);
        
        // Línea separadora de color
        com.itextpdf.text.pdf.draw.LineSeparator linea = new com.itextpdf.text.pdf.draw.LineSeparator();
        linea.setLineColor(color);
        linea.setLineWidth(1.5f);
        linea.setOffset(-5);
        doc.add(new Paragraph(" ")); // Espacio
        doc.add(linea);
    }

    // 2. Celda para la Cabecera de la Tabla (Fondo oscuro, texto blanco)
    private PdfPCell crearCeldaHeader(String texto, BaseColor colorFondo) {
        com.itextpdf.text.Font fontHeader = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 9, com.itextpdf.text.Font.BOLD, BaseColor.WHITE);
        PdfPCell cell = new PdfPCell(new Paragraph(texto, fontHeader));
        cell.setBackgroundColor(colorFondo);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPaddingTop(5f);
        cell.setPaddingBottom(5f);
        return cell;
    }

    // 3. Celda para los Datos (Con color alternado y validación de nulls)
    private PdfPCell crearCeldaData(String texto, BaseColor colorFondo, int alineacion) {
        // Validamos null para que no salga la palabra "null"
        String textoFinal = (texto == null || texto.equalsIgnoreCase("null")) ? "-" : texto;
        
        com.itextpdf.text.Font fontData = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 8, com.itextpdf.text.Font.NORMAL, BaseColor.DARK_GRAY);
        PdfPCell cell = new PdfPCell(new Paragraph(textoFinal, fontData));
        cell.setBackgroundColor(colorFondo);
        cell.setHorizontalAlignment(alineacion);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(4f); // Un poco de aire
        return cell;
    }

    // ======================================================
    // REPORTE DE RECURSOS
    // ======================================================
   private void generarReporteRecursos() {
        String ruta = crearRuta("reporte_recursos.pdf");
        
        // Colores Corporativos
        BaseColor colorPrimario = new BaseColor(33, 97, 140); 
        BaseColor colorFondoGris = new BaseColor(242, 243, 244);

        try {
            // 1. ORIENTACIÓN HORIZONTAL (Landscape)
            // Esto da "aire" a la tabla para que se vea elegante y no apretada
            Document doc = new Document(PageSize.A4.rotate(), 20, 20, 30, 30);
            PdfWriter.getInstance(doc, new FileOutputStream(ruta));
            doc.open();

            // Encabezado Profesional
            agregarEncabezadoProfesional(doc, "INVENTARIO DE FLOTA Y EQUIPOS", colorPrimario);

            // 2. CONFIGURACIÓN DE TABLA (6 Columnas ahora)
            PdfPTable tabla = new PdfPTable(6);
            tabla.setWidthPercentage(100);
            tabla.setSpacingBefore(15f);
            
            // 3. ANCHOS INTELIGENTES
            // Le damos el 40% del espacio a la Descripción para que se lea bien
            float[] anchos = {1f, 2.5f, 4.5f, 1.5f, 1.5f, 2f}; 
            tabla.setWidths(anchos);

            // Cabeceras (Sin Cantidad)
            String[] headers = {"ID", "Tipo/Modelo", "Descripción Detallada", "Tarifa", "Estado", "Ubicación"};
            for (String h : headers) {
                tabla.addCell(crearCeldaHeader(h, colorPrimario));
            }

            // 4. CONSULTA SQL (Sin columna cantidad)
            String sql = "SELECT idRecursos, tipo, descripcion, tarifaHora, estado, ubicacion FROM Recursos ORDER BY tipo, estado";
            
            try (Statement st = conexion.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                boolean filaImpar = true;
                while (rs.next()) {
                    BaseColor colorFondo = filaImpar ? BaseColor.WHITE : colorFondoGris;

                    // ID
                    tabla.addCell(crearCeldaData(rs.getString("idRecursos"), colorFondo, Element.ALIGN_CENTER));
                    
                    // Tipo (Ej: Cuatrimoto)
                    tabla.addCell(crearCeldaData(rs.getString("tipo"), colorFondo, Element.ALIGN_LEFT));
                    
                    // Descripción (Control de longitud)
                    String desc = rs.getString("descripcion");
                    if (desc != null && desc.length() > 45) { // Permitimos más texto por ser horizontal
                        desc = desc.substring(0, 42) + "...";
                    }
                    tabla.addCell(crearCeldaData(desc, colorFondo, Element.ALIGN_LEFT));

                    // Tarifa (Formato Moneda)
                    double tarifa = rs.getDouble("tarifaHora");
                    tabla.addCell(crearCeldaData("S/ " + String.format("%.2f", tarifa), colorFondo, Element.ALIGN_RIGHT));

                    // Estado (Mayúsculas para resaltar)
                    String estado = rs.getString("estado");
                    tabla.addCell(crearCeldaData(estado != null ? estado.toUpperCase() : "-", colorFondo, Element.ALIGN_CENTER));

                    // Ubicación
                    tabla.addCell(crearCeldaData(rs.getString("ubicacion"), colorFondo, Element.ALIGN_CENTER));

                    filaImpar = !filaImpar;
                }
            }

            doc.add(tabla);

            // Pie de página
            doc.add(new Paragraph("\n"));
            Paragraph fin = new Paragraph("Inventario actualizado al " + new java.util.Date(), 
                    new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 8, com.itextpdf.text.Font.ITALIC, BaseColor.GRAY));
            fin.setAlignment(Element.ALIGN_CENTER);
            doc.add(fin);

            doc.close();
            abrirPDF(ruta);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error reporte recursos: " + e.getMessage());
        }
    }
    // ======================================================
    // REPORTE GENERAL DE ALQUILERES
    // ======================================================
    private void generarReporteAlquileres() {
        String ruta = crearRuta("reporte_alquileres_general.pdf");
        
        // Colores
        BaseColor colorPrimario = new BaseColor(33, 97, 140);
        BaseColor colorFondoGris = new BaseColor(242, 243, 244);
        BaseColor colorAlerta = new BaseColor(192, 57, 43); // Rojo suave para moras/pendientes

        try {
            // 1. HORIZONTAL (Landscape) para que quepan las columnas
            Document doc = new Document(PageSize.A4.rotate(), 20, 20, 30, 30);
            PdfWriter.getInstance(doc, new FileOutputStream(ruta));
            doc.open();

            // Encabezado
            agregarEncabezadoProfesional(doc, "REPORTE GENERAL DE MOVIMIENTOS Y ALQUILERES", colorPrimario);

            // 2. TABLA (8 Columnas optimizadas)
            PdfPTable tabla = new PdfPTable(8);
            tabla.setWidthPercentage(100);
            tabla.setSpacingBefore(15f);
            
            // Anchos: Cliente ancho, ID estrecho, Precios medios
            float[] anchos = {1f, 4f, 1.5f, 1.2f, 1f, 1.5f, 1.5f, 1.5f};
            tabla.setWidths(anchos);

            // Cabeceras
            String[] headers = {"ID", "Cliente", "Fecha", "Hora", "Dur.", "Estado", "Total", "Mora"};
            for (String h : headers) {
                tabla.addCell(crearCeldaHeader(h, colorPrimario));
            }

            // 3. CONSULTA SQL CON JOIN (Para ver nombres, no códigos)
            String sql = """
                SELECT a.idAlquiler, 
                       t.nombre + ' ' + t.apellidos AS cliente,
                       a.fechaInicio, 
                       a.horaInicio, 
                       a.Duracion, 
                       a.estado, 
                       a.total, 
                       a.mora
                FROM Alquiler a
                JOIN Turista t ON a.idTurista = t.idTurista
                ORDER BY a.fechaInicio DESC, a.horaInicio DESC
            """;

            // Variables para sumar totales al final
            double sumaTotal = 0;
            double sumaMora = 0;

            try (Statement st = conexion.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                boolean filaImpar = true;
                
                // Formateadores
                java.text.SimpleDateFormat sdfHora = new java.text.SimpleDateFormat("HH:mm");
                java.text.SimpleDateFormat sdfFecha = new java.text.SimpleDateFormat("dd/MM/yyyy");

                while (rs.next()) {
                    BaseColor colorFondo = filaImpar ? BaseColor.WHITE : colorFondoGris;
                    
                    // ID
                    tabla.addCell(crearCeldaData(rs.getString("idAlquiler"), colorFondo, Element.ALIGN_CENTER));
                    
                    // Cliente (Nombre real)
                    tabla.addCell(crearCeldaData(rs.getString("cliente"), colorFondo, Element.ALIGN_LEFT));
                    
                    // Fecha
                    java.sql.Date fecha = rs.getDate("fechaInicio");
                    tabla.addCell(crearCeldaData(fecha != null ? sdfFecha.format(fecha) : "-", colorFondo, Element.ALIGN_CENTER));

                    // Hora
                    java.sql.Time hora = rs.getTime("horaInicio");
                    tabla.addCell(crearCeldaData(hora != null ? sdfHora.format(hora) : "-", colorFondo, Element.ALIGN_CENTER));

                    // Duración
                    tabla.addCell(crearCeldaData(rs.getInt("Duracion") + " h", colorFondo, Element.ALIGN_CENTER));

                    // Estado (Si está PENDIENTE, lo pintamos rojo para alertar)
                    String estado = rs.getString("estado");
                    if ("PENDIENTE".equalsIgnoreCase(estado)) {
                       // 1. Creamos la fuente ROJA manualmente
                        com.itextpdf.text.Font fontRojo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 8, com.itextpdf.text.Font.BOLD, colorAlerta);
                        
                        // 2. Creamos la celda directamente con esa fuente
                        PdfPCell cellEstado = new PdfPCell(new Paragraph(estado, fontRojo));
                        
                        // 3. Aplicamos los mismos estilos que el helper 'crearCeldaData'
                        cellEstado.setBackgroundColor(colorFondo);
                        cellEstado.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cellEstado.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        cellEstado.setPadding(4f);
                        
                        tabla.addCell(cellEstado);
                    } else {
                        tabla.addCell(crearCeldaData(estado, colorFondo, Element.ALIGN_CENTER));
                    }

                    // Total
                    double total = rs.getDouble("total");
                    sumaTotal += total;
                    tabla.addCell(crearCeldaData("S/ " + String.format("%.2f", total), colorFondo, Element.ALIGN_RIGHT));

                    // Mora
                    double mora = rs.getDouble("mora");
                    sumaMora += mora;
                    String txtMora = mora > 0 ? "S/ " + String.format("%.2f", mora) : "-";
                    tabla.addCell(crearCeldaData(txtMora, colorFondo, Element.ALIGN_RIGHT));

                    filaImpar = !filaImpar;
                }
            }

            doc.add(tabla);

            // 4. CUADRO DE RESUMEN FINANCIERO (Totales)
            // ... (Después de agregar la tabla principal y el doc.add(tabla)) ...
// 4. CUADRO DE RESUMEN FINANCIERO (Totales)
            doc.add(new Paragraph("\n"));
            
            PdfPTable tablaResumen = new PdfPTable(3);
            tablaResumen.setWidthPercentage(40); // Ajustado al 40% para que se vea compacto a la derecha
            tablaResumen.setHorizontalAlignment(Element.ALIGN_RIGHT);
            
            // --- CÁLCULOS GLOBALES ---
            double totalRecaudado = sumaTotal; 
            double totalBase = totalRecaudado / 1.18;
            double totalIGV = totalRecaudado - totalBase;

            // Fila 1: Ingreso por Alquiler
            // CORRECCIÓN: Usamos BaseColor.GRAY para que el texto BLANCO se lea
            tablaResumen.addCell(crearCeldaHeader("INGRESOS ALQUILER", BaseColor.GRAY));
            
            PdfPCell c1 = crearCeldaData("S/ " + String.format("%.2f", sumaTotal - sumaMora), BaseColor.WHITE, Element.ALIGN_RIGHT);
            c1.setColspan(2); 
            tablaResumen.addCell(c1);

            // Fila 2: Ingreso por Mora
            tablaResumen.addCell(crearCeldaHeader("INGRESOS MORA", BaseColor.GRAY));
            
            PdfPCell c2 = crearCeldaData("S/ " + String.format("%.2f", sumaMora), BaseColor.WHITE, Element.ALIGN_RIGHT);
            c2.setColspan(2);
            tablaResumen.addCell(c2);

            // Fila 3: Valor Neto (Base Imponible)
            // CORRECCIÓN: Fondo Gris Oscuro para distinguir el desglose de impuestos
            tablaResumen.addCell(crearCeldaHeader("VALOR VENTA (NETO)", BaseColor.DARK_GRAY));
            
            PdfPCell cNeto = crearCeldaData("S/ " + String.format("%.2f", totalBase), BaseColor.WHITE, Element.ALIGN_RIGHT);
            cNeto.setColspan(2);
            tablaResumen.addCell(cNeto);

            // Fila 4: IGV
            tablaResumen.addCell(crearCeldaHeader("IGV TOTAL (18%)", BaseColor.DARK_GRAY));
            
            PdfPCell cIgv = crearCeldaData("S/ " + String.format("%.2f", totalIGV), BaseColor.WHITE, Element.ALIGN_RIGHT);
            cIgv.setColspan(2);
            tablaResumen.addCell(cIgv);

            // Fila 5: GRAN TOTAL (Azul Corporativo)
            // Creamos manualmente la celda para asegurar la fuente Negrita y Grande
            Font fontTotal = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
            
            PdfPCell celdaTotalLabel = new PdfPCell(new Paragraph("TOTAL RECAUDADO", fontTotal));
            celdaTotalLabel.setBackgroundColor(colorPrimario);
            celdaTotalLabel.setHorizontalAlignment(Element.ALIGN_LEFT); // Alineado a la izquierda del bloque
            celdaTotalLabel.setVerticalAlignment(Element.ALIGN_MIDDLE);
            celdaTotalLabel.setPadding(5f);
            tablaResumen.addCell(celdaTotalLabel);

            PdfPCell celdaTotalValue = new PdfPCell(new Paragraph("S/ " + String.format("%.2f", totalRecaudado), fontTotal));
            celdaTotalValue.setBackgroundColor(colorPrimario);
            celdaTotalValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
            celdaTotalValue.setVerticalAlignment(Element.ALIGN_MIDDLE);
            celdaTotalValue.setPadding(5f);
            celdaTotalValue.setColspan(2);
            tablaResumen.addCell(celdaTotalValue);
            
            doc.add(tablaResumen);

            // Pie
            doc.add(new Paragraph("\n"));
            Paragraph fin = new Paragraph("Reporte generado el " + new java.util.Date(), 
                    new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 8, com.itextpdf.text.Font.ITALIC, BaseColor.GRAY));
            fin.setAlignment(Element.ALIGN_CENTER);
            doc.add(fin);

            doc.close();
            abrirPDF(ruta);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error reporte alquileres: " + e.getMessage());
            e.printStackTrace();
        }
    }
    // ======================================================
    // REPORTE DE ALQUILERES POR FECHA
   private void generarReporteAlquileresPorFecha() {
        java.util.Date fechaIni = jDateInicio.getDate();
        java.util.Date fechaFin = jDateFin.getDate();

        if (fechaIni == null || fechaFin == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar ambas fechas.");
            return;
        }

        java.sql.Date f1 = new java.sql.Date(fechaIni.getTime());
        java.sql.Date f2 = new java.sql.Date(fechaFin.getTime());

        // Formato para el título del PDF
        java.text.SimpleDateFormat sdfTitulo = new java.text.SimpleDateFormat("dd/MM/yyyy");
        String rangoFechas = "DEL " + sdfTitulo.format(fechaIni) + " AL " + sdfTitulo.format(fechaFin);

        String ruta = crearRuta("reporte_alquileres_fechas.pdf");

        // Colores
        BaseColor colorPrimario = new BaseColor(33, 97, 140);
        BaseColor colorFondoGris = new BaseColor(242, 243, 244);
        BaseColor colorAlerta = new BaseColor(192, 57, 43);

        try {
            // 1. DOCUMENTO HORIZONTAL
            Document doc = new Document(PageSize.A4.rotate(), 20, 20, 30, 30);
            PdfWriter.getInstance(doc, new FileOutputStream(ruta));
            doc.open();

            // Encabezado con el Rango de Fechas
            agregarEncabezadoProfesional(doc, "REPORTE DE ALQUILERES (" + rangoFechas + ")", colorPrimario);

            // 2. TABLA (8 Columnas)
            PdfPTable tabla = new PdfPTable(8);
            tabla.setWidthPercentage(100);
            tabla.setSpacingBefore(15f);

            // Anchos
            float[] anchos = {1f, 4f, 1.5f, 1.2f, 1f, 1.5f, 1.5f, 1.5f};
            tabla.setWidths(anchos);

            // Cabeceras
            String[] headers = {"ID", "Cliente", "Fecha", "Hora", "Dur.", "Estado", "Total", "Mora"};
            for (String h : headers) {
                tabla.addCell(crearCeldaHeader(h, colorPrimario));
            }

            // 3. CONSULTA SQL FILTRADA POR FECHA
            String sql = """
                SELECT a.idAlquiler, 
                       t.nombre + ' ' + t.apellidos AS cliente,
                       a.fechaInicio, 
                       a.horaInicio, 
                       a.Duracion, 
                       a.estado, 
                       a.total, 
                       a.mora
                FROM Alquiler a
                JOIN Turista t ON a.idTurista = t.idTurista
                WHERE a.fechaInicio BETWEEN ? AND ?
                ORDER BY a.fechaInicio ASC, a.horaInicio ASC
            """;

            // Acumuladores para el resumen final
            double sumaTotal = 0;
            double sumaMora = 0;

            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                ps.setDate(1, f1);
                ps.setDate(2, f2);
                ResultSet rs = ps.executeQuery();

                boolean filaImpar = true;
                java.text.SimpleDateFormat sdfHora = new java.text.SimpleDateFormat("HH:mm");
                java.text.SimpleDateFormat sdfFecha = new java.text.SimpleDateFormat("dd/MM/yyyy");

                while (rs.next()) {
                    BaseColor colorFondo = filaImpar ? BaseColor.WHITE : colorFondoGris;

                    // Datos Básicos
                    tabla.addCell(crearCeldaData(rs.getString("idAlquiler"), colorFondo, Element.ALIGN_CENTER));
                    tabla.addCell(crearCeldaData(rs.getString("cliente"), colorFondo, Element.ALIGN_LEFT));
                    
                    java.sql.Date fecha = rs.getDate("fechaInicio");
                    tabla.addCell(crearCeldaData(fecha != null ? sdfFecha.format(fecha) : "-", colorFondo, Element.ALIGN_CENTER));

                    java.sql.Time hora = rs.getTime("horaInicio");
                    tabla.addCell(crearCeldaData(hora != null ? sdfHora.format(hora) : "-", colorFondo, Element.ALIGN_CENTER));

                    tabla.addCell(crearCeldaData(rs.getInt("Duracion") + " h", colorFondo, Element.ALIGN_CENTER));

                    // Estado con Alerta Roja (Versión corregida sin getElement)
                    String estado = rs.getString("estado");
                    if ("PENDIENTE".equalsIgnoreCase(estado)) {
                        com.itextpdf.text.Font fontRojo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 8, com.itextpdf.text.Font.BOLD, colorAlerta);
                        PdfPCell cellEstado = new PdfPCell(new Paragraph(estado, fontRojo));
                        cellEstado.setBackgroundColor(colorFondo);
                        cellEstado.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cellEstado.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        cellEstado.setPadding(4f);
                        tabla.addCell(cellEstado);
                    } else {
                        tabla.addCell(crearCeldaData(estado, colorFondo, Element.ALIGN_CENTER));
                    }

                    // Totales Financieros
                    double total = rs.getDouble("total");
                    sumaTotal += total;
                    tabla.addCell(crearCeldaData("S/ " + String.format("%.2f", total), colorFondo, Element.ALIGN_RIGHT));

                    double mora = rs.getDouble("mora");
                    sumaMora += mora;
                    String txtMora = mora > 0 ? "S/ " + String.format("%.2f", mora) : "-";
                    tabla.addCell(crearCeldaData(txtMora, colorFondo, Element.ALIGN_RIGHT));

                    filaImpar = !filaImpar;
                }
            }

            doc.add(tabla);

            // 4. RESUMEN FINANCIERO DEL PERIODO (Igual que el reporte general)
            doc.add(new Paragraph("\n"));
            
            PdfPTable tablaResumen = new PdfPTable(3);
            tablaResumen.setWidthPercentage(40); 
            tablaResumen.setHorizontalAlignment(Element.ALIGN_RIGHT);
            
            // Cálculos
            double totalRecaudado = sumaTotal; 
            double totalBase = totalRecaudado / 1.18;
            double totalIGV = totalRecaudado - totalBase;

            // Fila 1: Alquiler
            tablaResumen.addCell(crearCeldaHeader("ALQUILER (" + rangoFechas + ")", BaseColor.GRAY));
            PdfPCell c1 = crearCeldaData("S/ " + String.format("%.2f", sumaTotal - sumaMora), BaseColor.WHITE, Element.ALIGN_RIGHT);
            c1.setColspan(2); 
            tablaResumen.addCell(c1);

            // Fila 2: Mora
            tablaResumen.addCell(crearCeldaHeader("MORA ACUMULADA", BaseColor.GRAY));
            PdfPCell c2 = crearCeldaData("S/ " + String.format("%.2f", sumaMora), BaseColor.WHITE, Element.ALIGN_RIGHT);
            c2.setColspan(2);
            tablaResumen.addCell(c2);

            // Fila 3: Neto
            tablaResumen.addCell(crearCeldaHeader("VALOR VENTA (NETO)", BaseColor.DARK_GRAY));
            PdfPCell cNeto = crearCeldaData("S/ " + String.format("%.2f", totalBase), BaseColor.WHITE, Element.ALIGN_RIGHT);
            cNeto.setColspan(2);
            tablaResumen.addCell(cNeto);

            // Fila 4: IGV
            tablaResumen.addCell(crearCeldaHeader("IGV (18%)", BaseColor.DARK_GRAY));
            PdfPCell cIgv = crearCeldaData("S/ " + String.format("%.2f", totalIGV), BaseColor.WHITE, Element.ALIGN_RIGHT);
            cIgv.setColspan(2);
            tablaResumen.addCell(cIgv);

            // Fila 5: TOTAL PERIODO
            com.itextpdf.text.Font fontTotal = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD, BaseColor.WHITE);
            
            PdfPCell celdaTotalLabel = new PdfPCell(new Paragraph("TOTAL PERIODO", fontTotal));
            celdaTotalLabel.setBackgroundColor(colorPrimario);
            celdaTotalLabel.setHorizontalAlignment(Element.ALIGN_LEFT);
            celdaTotalLabel.setVerticalAlignment(Element.ALIGN_MIDDLE);
            celdaTotalLabel.setPadding(5f);
            tablaResumen.addCell(celdaTotalLabel);

            PdfPCell celdaTotalValue = new PdfPCell(new Paragraph("S/ " + String.format("%.2f", totalRecaudado), fontTotal));
            celdaTotalValue.setBackgroundColor(colorPrimario);
            celdaTotalValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
            celdaTotalValue.setVerticalAlignment(Element.ALIGN_MIDDLE);
            celdaTotalValue.setPadding(5f);
            celdaTotalValue.setColspan(2);
            tablaResumen.addCell(celdaTotalValue);
            
            doc.add(tablaResumen);

            // Pie
            doc.add(new Paragraph("\n"));
            Paragraph fin = new Paragraph("Reporte generado el " + new java.util.Date(), 
                    new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 8, com.itextpdf.text.Font.ITALIC, BaseColor.GRAY));
            fin.setAlignment(Element.ALIGN_CENTER);
            doc.add(fin);

            doc.close();
            abrirPDF(ruta);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error reporte por fechas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ======================================================
    // REPORTE DE ALQUILERES POR USUARIO
    // ======================================================
  
   private void generarReporteAlquileresPorUsuario() {
        String idUsuario = JOptionPane.showInputDialog(this, "Ingrese el ID del usuario (Ej: U001):");
        if (idUsuario == null || idUsuario.trim().isEmpty()) {
            return; // Cancelado
        }

        idUsuario = idUsuario.trim().toUpperCase();
        String ruta = crearRuta("reporte_ventas_usuario_" + idUsuario + ".pdf");

        // Colores Corporativos
        BaseColor colorPrimario = new BaseColor(33, 97, 140);
        BaseColor colorFondoGris = new BaseColor(242, 243, 244);
        BaseColor colorAlerta = new BaseColor(192, 57, 43);

        try {
            // 1. OBTENER NOMBRE DEL USUARIO (Para el título)
            String nombreUsuario = "Desconocido";
            String sqlUser = "SELECT nameUsuario FROM ActorUsuario WHERE idUsuario = ?";
            try (PreparedStatement ps = conexion.prepareStatement(sqlUser)) {
                ps.setString(1, idUsuario);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    nombreUsuario = rs.getString("nameUsuario");
                } else {
                    JOptionPane.showMessageDialog(this, "Usuario no encontrado.");
                    return;
                }
            }

            // 2. CONFIGURACIÓN DEL PDF (Horizontal)
            Document doc = new Document(PageSize.A4.rotate(), 20, 20, 30, 30);
            PdfWriter.getInstance(doc, new FileOutputStream(ruta));
            doc.open();

            // Encabezado Personalizado
            agregarEncabezadoProfesional(doc, "REPORTE DE VENTAS POR USUARIO: " + nombreUsuario.toUpperCase(), colorPrimario);

            // 3. TABLA (8 Columnas)
            PdfPTable tabla = new PdfPTable(8);
            tabla.setWidthPercentage(100);
            tabla.setSpacingBefore(15f);

            // Anchos
            float[] anchos = {1f, 4f, 1.5f, 1.2f, 1f, 1.5f, 1.5f, 1.5f};
            tabla.setWidths(anchos);

            // Cabeceras
            String[] headers = {"ID", "Cliente Atendido", "Fecha", "Hora", "Dur.", "Estado", "Total", "Mora"};
            for (String h : headers) {
                tabla.addCell(crearCeldaHeader(h, colorPrimario));
            }

            // 4. CONSULTA DE VENTAS DEL USUARIO (Con JOIN para nombre de cliente)
            String sql = """
                SELECT a.idAlquiler, 
                       t.nombre + ' ' + t.apellidos AS cliente,
                       a.fechaInicio, 
                       a.horaInicio, 
                       a.Duracion, 
                       a.estado, 
                       a.total, 
                       a.mora
                FROM Alquiler a
                JOIN Turista t ON a.idTurista = t.idTurista
                WHERE a.idUsuario = ?
                ORDER BY a.fechaInicio DESC, a.horaInicio DESC
            """;

            double sumaTotal = 0;
            double sumaMora = 0;

            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                ps.setString(1, idUsuario);
                ResultSet rs = ps.executeQuery();

                boolean filaImpar = true;
                java.text.SimpleDateFormat sdfHora = new java.text.SimpleDateFormat("HH:mm");
                java.text.SimpleDateFormat sdfFecha = new java.text.SimpleDateFormat("dd/MM/yyyy");

                while (rs.next()) {
                    BaseColor colorFondo = filaImpar ? BaseColor.WHITE : colorFondoGris;

                    // Datos
                    tabla.addCell(crearCeldaData(rs.getString("idAlquiler"), colorFondo, Element.ALIGN_CENTER));
                    tabla.addCell(crearCeldaData(rs.getString("cliente"), colorFondo, Element.ALIGN_LEFT)); // Cliente real
                    
                    java.sql.Date fecha = rs.getDate("fechaInicio");
                    tabla.addCell(crearCeldaData(fecha != null ? sdfFecha.format(fecha) : "-", colorFondo, Element.ALIGN_CENTER));

                    java.sql.Time hora = rs.getTime("horaInicio");
                    tabla.addCell(crearCeldaData(hora != null ? sdfHora.format(hora) : "-", colorFondo, Element.ALIGN_CENTER));

                    tabla.addCell(crearCeldaData(rs.getInt("Duracion") + " h", colorFondo, Element.ALIGN_CENTER));

                    // Estado (Con alerta roja segura si es pendiente)
                    String estado = rs.getString("estado");
                    if ("PENDIENTE".equalsIgnoreCase(estado)) {
                        com.itextpdf.text.Font fontRojo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 8, com.itextpdf.text.Font.BOLD, colorAlerta);
                        PdfPCell cellEstado = new PdfPCell(new Paragraph(estado, fontRojo));
                        cellEstado.setBackgroundColor(colorFondo);
                        cellEstado.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cellEstado.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        cellEstado.setPadding(4f);
                        tabla.addCell(cellEstado);
                    } else {
                        tabla.addCell(crearCeldaData(estado, colorFondo, Element.ALIGN_CENTER));
                    }

                    // Dinero
                    double total = rs.getDouble("total");
                    sumaTotal += total;
                    tabla.addCell(crearCeldaData("S/ " + String.format("%.2f", total), colorFondo, Element.ALIGN_RIGHT));

                    double mora = rs.getDouble("mora");
                    sumaMora += mora;
                    String txtMora = mora > 0 ? "S/ " + String.format("%.2f", mora) : "-";
                    tabla.addCell(crearCeldaData(txtMora, colorFondo, Element.ALIGN_RIGHT));

                    filaImpar = !filaImpar;
                }
            }

            doc.add(tabla);

            // 5. RESUMEN DE RENDIMIENTO (Totales)
            doc.add(new Paragraph("\n"));
            
            PdfPTable tablaResumen = new PdfPTable(3);
            tablaResumen.setWidthPercentage(40);
            tablaResumen.setHorizontalAlignment(Element.ALIGN_RIGHT);
            
            // Fila 1: Ventas Brutas
            tablaResumen.addCell(crearCeldaHeader("VENTAS ALQUILER", BaseColor.GRAY));
            PdfPCell c1 = crearCeldaData("S/ " + String.format("%.2f", sumaTotal - sumaMora), BaseColor.WHITE, Element.ALIGN_RIGHT);
            c1.setColspan(2);
            tablaResumen.addCell(c1);

            // Fila 2: Moras Cobradas
            tablaResumen.addCell(crearCeldaHeader("MORAS COBRADAS", BaseColor.GRAY));
            PdfPCell c2 = crearCeldaData("S/ " + String.format("%.2f", sumaMora), BaseColor.WHITE, Element.ALIGN_RIGHT);
            c2.setColspan(2);
            tablaResumen.addCell(c2);
            
            // Fila 3: TOTAL GENERADO POR EL USUARIO
            com.itextpdf.text.Font fontTotal = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD, BaseColor.WHITE);
            
            PdfPCell cTotalLabel = new PdfPCell(new Paragraph("TOTAL GENERADO", fontTotal));
            cTotalLabel.setBackgroundColor(colorPrimario);
            cTotalLabel.setHorizontalAlignment(Element.ALIGN_LEFT);
            cTotalLabel.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cTotalLabel.setPadding(5f);
            tablaResumen.addCell(cTotalLabel);
            
            PdfPCell cTotalValue = new PdfPCell(new Paragraph("S/ " + String.format("%.2f", sumaTotal), fontTotal));
            cTotalValue.setBackgroundColor(colorPrimario);
            cTotalValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cTotalValue.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cTotalValue.setPadding(5f);
            cTotalValue.setColspan(2);
            tablaResumen.addCell(cTotalValue);
            
            doc.add(tablaResumen);

            // Pie
            doc.add(new Paragraph("\n"));
            Paragraph fin = new Paragraph("Reporte de rendimiento individual - " + new java.util.Date(), 
                    new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 8, com.itextpdf.text.Font.ITALIC, BaseColor.GRAY));
            fin.setAlignment(Element.ALIGN_CENTER);
            doc.add(fin);

            doc.close();
            abrirPDF(ruta);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error reporte usuario: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
