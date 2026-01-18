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

        if (idPago == null || idPago.trim().isEmpty()) {
            return;
        }
        idPago = idPago.trim().toUpperCase();

        try {
            String sql = """
            SELECT 
                p.idPago, p.fechaPago, p.metodoPago,
                a.idAlquiler, a.subtotal, a.montoDescuento, -- Usamos las columnas desglosadas
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

            // Datos Básicos
            String idAlquiler = rs.getString("idAlquiler");
            java.sql.Timestamp fechaHoraPago = rs.getTimestamp("fechaPago");
            String metodoPago = rs.getString("metodoPago");
            String cliente = rs.getString("nombre") + " " + rs.getString("apellidos");
            String dni = rs.getString("dni");
            String promoDesc = rs.getString("promoDesc");
            if (promoDesc == null) {
                promoDesc = "";
            }

            // --- CORRECCIÓN LÓGICA: EXCLUIR MORA ---
            // Leemos los costos del servicio puro desde la BD
            double subtotalServicio = rs.getDouble("subtotal");      // Precio lista
            double descuento = rs.getDouble("montoDescuento");       // Descuento ganado

            // Calculamos el Total de ESTA boleta (Solo Alquiler)
            // Ignoramos lo que diga la tabla Pago o la columna Mora
            double totalSoloAlquiler = subtotalServicio - descuento;

            // Validación de seguridad (no imprimir negativos)
            if (totalSoloAlquiler < 0) {
                totalSoloAlquiler = 0;
            }

            // Generar PDF
            generarPDFBoletaEstandar(
                    idAlquiler,
                    idPago,
                    fechaHoraPago,
                    metodoPago,
                    cliente,
                    dni,
                    totalSoloAlquiler, // <--- Enviamos el monto SIN mora
                    subtotalServicio, // <--- Enviamos el subtotal puro
                    descuento, // <--- Enviamos el descuento
                    promoDesc
            );

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al preparar boleta: " + e.getMessage());
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

        String ruta = crearRuta("reporte_turistas_valor.pdf");

        // Colores
        BaseColor colorPrimario = new BaseColor(33, 97, 140);
        BaseColor colorFondoGris = new BaseColor(242, 243, 244);

        try {
            Document doc = new Document(PageSize.A4.rotate(), 20, 20, 30, 30); // ROTATE: Hoja horizontal para que quepa todo
            PdfWriter.getInstance(doc, new FileOutputStream(ruta));
            doc.open();

            // 1. ENCABEZADO
            agregarEncabezadoProfesional(doc, "CARTERA DE CLIENTES Y RENDIMIENTO", colorPrimario);

            // 2. CONFIGURACIÓN DE LA TABLA (Ahora 8 Columnas)
            // Agregamos: N° Viajes y Total Gastado
            PdfPTable tabla = new PdfPTable(8);
            tabla.setWidthPercentage(100);

            // Ajustamos anchos: ID, Nom, Ape, DNI, Nac, Contacto, Viajes, $$
            tabla.setWidths(new float[]{0.8f, 2.5f, 2.5f, 1.5f, 1.5f, 2f, 1f, 1.5f});
            tabla.setSpacingBefore(15f);

            // 3. CABECERA
            String[] columnas = {"ID", "Nombres", "Apellidos", "DNI", "Nacionalidad", "Contacto", "Alq.", "Inv. Total"};
            for (String col : columnas) {
                tabla.addCell(crearCeldaHeader(col, colorPrimario));
            }

            // 4. CONSULTA SQL INTELIGENTE
            // Usamos subconsultas para calcular los datos financieros al vuelo
            // GRACIAS A TU NUEVA BD, 'SUM(a.total)' ahora es matemáticamente exacto (incluye moras y descuentos)
            String sql = """
            SELECT 
                t.idTurista, t.nombre, t.apellidos, t.dni, t.nacionalidad, t.contacto,
                (SELECT COUNT(*) FROM Alquiler a WHERE a.idTurista = t.idTurista) AS numViajes,
                (SELECT ISNULL(SUM(a.total), 0) FROM Alquiler a WHERE a.idTurista = t.idTurista) AS totalGastado
            FROM Turista t
            ORDER BY totalGastado DESC -- Ordenamos por quién gasta más (Mejor para el negocio)
        """;

            try (Statement st = conexion.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                boolean filaImpar = true;

                while (rs.next()) {
                    BaseColor colorFondo = filaImpar ? BaseColor.WHITE : colorFondoGris;

                    // Datos Personales
                    tabla.addCell(crearCeldaData(rs.getString("idTurista"), colorFondo, Element.ALIGN_CENTER));
                    tabla.addCell(crearCeldaData(rs.getString("nombre"), colorFondo, Element.ALIGN_LEFT));
                    tabla.addCell(crearCeldaData(rs.getString("apellidos"), colorFondo, Element.ALIGN_LEFT));
                    tabla.addCell(crearCeldaData(rs.getString("dni"), colorFondo, Element.ALIGN_CENTER));
                    tabla.addCell(crearCeldaData(rs.getString("nacionalidad"), colorFondo, Element.ALIGN_CENTER));
                    tabla.addCell(crearCeldaData(rs.getString("contacto"), colorFondo, Element.ALIGN_LEFT));

                    // Datos Financieros (Nuevos)
                    tabla.addCell(crearCeldaData(rs.getString("numViajes"), colorFondo, Element.ALIGN_CENTER));

                    double gastado = rs.getDouble("totalGastado");
                    tabla.addCell(crearCeldaData("S/ " + String.format("%.2f", gastado), colorFondo, Element.ALIGN_RIGHT));

                    filaImpar = !filaImpar;
                }
            }

            doc.add(tabla);

            // 5. PIE
            doc.add(new Paragraph("\n"));
            Paragraph fin = new Paragraph("Reporte ordenado por nivel de inversión del cliente.",
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

        BaseColor colorPrimario = new BaseColor(33, 97, 140);
        BaseColor colorFondoGris = new BaseColor(242, 243, 244);
        BaseColor colorDeudor = new BaseColor(192, 57, 43); // Rojo para impagos
        BaseColor colorTextoNormal = BaseColor.DARK_GRAY;

        try {
            Document doc = new Document(PageSize.A4.rotate(), 20, 20, 30, 30);
            PdfWriter.getInstance(doc, new FileOutputStream(ruta));
            doc.open();

            agregarEncabezadoProfesional(doc, "REPORTE GENERAL (PAGADOS Y PENDIENTES)", colorPrimario);

            // TABLA
            PdfPTable tabla = new PdfPTable(11); // Agregamos columna "Estado Pago"
            tabla.setWidthPercentage(100);
            tabla.setSpacingBefore(15f);

            // Ajustamos anchos
            float[] anchos = {1.2f, 3.5f, 1.5f, 1.2f, 0.8f, 1.5f, 1.5f, 1.5f, 1.2f, 1.2f, 1.8f};
            tabla.setWidths(anchos);

            String[] headers = {"ID", "Cliente", "Fecha", "Hora", "H", "Est. Alq", "Subtotal", "Dscto.", "Mora", "Est. Pago", "Total"};
            for (String h : headers) {
                tabla.addCell(crearCeldaHeader(h, colorPrimario));
            }

            // --- SQL CON LEFT JOIN PARA VER ESTADO DE PAGO ---
            String sql = """
            SELECT a.idAlquiler, 
                   t.nombre + ' ' + t.apellidos AS cliente,
                   a.fechaInicio, a.horaInicio, a.Duracion, a.estado AS estadoAlquiler,
                   ISNULL(a.subtotal, a.total - a.mora) as subtotal,
                   ISNULL(a.montoDescuento, 0) as descuento,
                   a.mora, a.total,
                   p.estado AS estadoPago -- Traemos si está pagado o no
            FROM Alquiler a
            JOIN Turista t ON a.idTurista = t.idTurista
            LEFT JOIN Pago p ON a.idAlquiler = p.idAlquiler AND p.estado = 'Completado'
            ORDER BY a.fechaInicio DESC, a.horaInicio DESC
        """;

            double sumaSubtotal = 0, sumaDescuento = 0, sumaMora = 0, sumaTotal = 0;

            try (Statement st = conexion.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                boolean filaImpar = true;
                java.text.SimpleDateFormat sdfHora = new java.text.SimpleDateFormat("HH:mm");
                java.text.SimpleDateFormat sdfFecha = new java.text.SimpleDateFormat("dd/MM/yyyy");

                while (rs.next()) {
                    BaseColor colorFondo = filaImpar ? BaseColor.WHITE : colorFondoGris;

                    // 1. DETERMINAR SI ESTÁ PAGADO
                    String estPago = rs.getString("estadoPago");
                    boolean isPagado = (estPago != null && estPago.equalsIgnoreCase("Completado"));

                    // 2. DEFINIR COLOR DEL TEXTO (Rojo si debe, Normal si pagó)
                    BaseColor colorTextoFila = isPagado ? colorTextoNormal : colorDeudor;
                    String textoEstadoPago = isPagado ? "PAGADO" : "DEBE";

                    // --- IMPRIMIR CELDAS CON EL COLOR ELEGIDO ---
                    tabla.addCell(crearCeldaDataColor(rs.getString("idAlquiler"), colorFondo, Element.ALIGN_CENTER, colorTextoFila));
                    tabla.addCell(crearCeldaDataColor(rs.getString("cliente"), colorFondo, Element.ALIGN_LEFT, colorTextoFila));

                    java.sql.Date fecha = rs.getDate("fechaInicio");
                    tabla.addCell(crearCeldaDataColor(fecha != null ? sdfFecha.format(fecha) : "-", colorFondo, Element.ALIGN_CENTER, colorTextoFila));

                    java.sql.Time hora = rs.getTime("horaInicio");
                    tabla.addCell(crearCeldaDataColor(hora != null ? sdfHora.format(hora) : "-", colorFondo, Element.ALIGN_CENTER, colorTextoFila));

                    tabla.addCell(crearCeldaDataColor(String.valueOf(rs.getInt("Duracion")), colorFondo, Element.ALIGN_CENTER, colorTextoFila));
                    tabla.addCell(crearCeldaDataColor(rs.getString("estadoAlquiler"), colorFondo, Element.ALIGN_CENTER, colorTextoFila));

                    // Datos Financieros
                    double sub = rs.getDouble("subtotal");
                    double desc = rs.getDouble("descuento");
                    double mora = rs.getDouble("mora");
                    double tot = rs.getDouble("total");

                    // 3. LÓGICA DE ACUMULACIÓN: SOLO SUMAMOS SI ESTÁ PAGADO
                    if (isPagado) {
                        sumaSubtotal += sub;
                        sumaDescuento += desc;
                        sumaMora += mora;
                        sumaTotal += tot;
                    }

                    tabla.addCell(crearCeldaDataColor(String.format("%.2f", sub), colorFondo, Element.ALIGN_RIGHT, colorTextoFila));
                    tabla.addCell(crearCeldaDataColor(desc > 0 ? String.format("%.2f", desc) : "-", colorFondo, Element.ALIGN_RIGHT, colorTextoFila));
                    tabla.addCell(crearCeldaDataColor(mora > 0 ? String.format("%.2f", mora) : "-", colorFondo, Element.ALIGN_RIGHT, colorTextoFila));

                    // Columna Estado Pago Visual
                    tabla.addCell(crearCeldaDataColor(textoEstadoPago, colorFondo, Element.ALIGN_CENTER, colorTextoFila));

                    // Total Fila
                    PdfPCell cTotal = crearCeldaDataColor(String.format("%.2f", tot), colorFondo, Element.ALIGN_RIGHT, colorTextoFila);
                    // Si quieres negrita en el total de fila:
                    cTotal.setPhrase(new Phrase(String.format("%.2f", tot), new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD, colorTextoFila)));
                    tabla.addCell(cTotal);

                    filaImpar = !filaImpar;
                }
            }
            doc.add(tabla);

            // --- RESUMEN (SOLO MUESTRA DINERO RECAUDADO REAL) ---
            doc.add(new Paragraph("\n"));

            // Nota explicativa
            Paragraph nota = new Paragraph("* Los montos en ROJO indican deudas pendientes y NO se incluyen en el total recaudado.",
                    new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, colorDeudor));
            nota.setAlignment(Element.ALIGN_RIGHT);
            doc.add(nota);
            doc.add(new Paragraph("\n"));

            PdfPTable tablaResumen = new PdfPTable(2);
            tablaResumen.setWidthPercentage(40);
            tablaResumen.setHorizontalAlignment(Element.ALIGN_RIGHT);

            tablaResumen.addCell(crearCeldaHeader("SUBTOTAL (COBRADO)", BaseColor.GRAY));
            tablaResumen.addCell(crearCeldaData("S/ " + String.format("%.2f", sumaSubtotal), BaseColor.WHITE, Element.ALIGN_RIGHT));

            tablaResumen.addCell(crearCeldaHeader("DESCUENTOS DADOS", BaseColor.GRAY));
            tablaResumen.addCell(crearCeldaData("- S/ " + String.format("%.2f", sumaDescuento), BaseColor.WHITE, Element.ALIGN_RIGHT));

            tablaResumen.addCell(crearCeldaHeader("MORAS COBRADAS", BaseColor.GRAY));
            tablaResumen.addCell(crearCeldaData("+ S/ " + String.format("%.2f", sumaMora), BaseColor.WHITE, Element.ALIGN_RIGHT));

            // IGV
            double valorVenta = sumaTotal / 1.18;
            double impuesto = sumaTotal - valorVenta;

            tablaResumen.addCell(crearCeldaHeader("VALOR VENTA (NETO)", BaseColor.DARK_GRAY));
            tablaResumen.addCell(crearCeldaData("S/ " + String.format("%.2f", valorVenta), BaseColor.WHITE, Element.ALIGN_RIGHT));

            tablaResumen.addCell(crearCeldaHeader("IGV (18%)", BaseColor.DARK_GRAY));
            tablaResumen.addCell(crearCeldaData("S/ " + String.format("%.2f", impuesto), BaseColor.WHITE, Element.ALIGN_RIGHT));

            // TOTAL CAJA REAL
            Font fontTotal = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
            PdfPCell cTotalL = new PdfPCell(new Paragraph("CAJA REAL (RECAUDADO)", fontTotal));
            cTotalL.setBackgroundColor(colorPrimario);
            tablaResumen.addCell(cTotalL);

            PdfPCell cTotalV = new PdfPCell(new Paragraph("S/ " + String.format("%.2f", sumaTotal), fontTotal));
            cTotalV.setBackgroundColor(colorPrimario);
            cTotalV.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tablaResumen.addCell(cTotalV);

            doc.add(tablaResumen);
            doc.close();
            abrirPDF(ruta);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    // ======================================================
    // REPORTE DE ALQUILERES POR FECHA
    private void generarReporteAlquileresPorFecha() {

        // 1. Validaciones de fecha
        java.util.Date fechaIni = jDateInicio.getDate();
        java.util.Date fechaFin = jDateFin.getDate();

        if (fechaIni == null || fechaFin == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar ambas fechas.");
            return;
        }

        java.sql.Date f1 = new java.sql.Date(fechaIni.getTime());
        java.sql.Date f2 = new java.sql.Date(fechaFin.getTime());

        java.text.SimpleDateFormat sdfTitulo = new java.text.SimpleDateFormat("dd/MM/yyyy");
        String rangoFechas = "DEL " + sdfTitulo.format(fechaIni) + " AL " + sdfTitulo.format(fechaFin);
        String ruta = crearRuta("reporte_alquileres_fechas.pdf");

        // Colores
        BaseColor colorPrimario = new BaseColor(33, 97, 140);
        BaseColor colorFondoGris = new BaseColor(242, 243, 244);
        BaseColor colorDeudor = new BaseColor(192, 57, 43); // Rojo para impagos
        BaseColor colorTextoNormal = BaseColor.DARK_GRAY;

        try {
            Document doc = new Document(PageSize.A4.rotate(), 20, 20, 30, 30);
            PdfWriter.getInstance(doc, new FileOutputStream(ruta));
            doc.open();

            agregarEncabezadoProfesional(doc, "REPORTE DE ALQUILERES (" + rangoFechas + ")", colorPrimario);

            // 2. TABLA (11 Columnas)
            PdfPTable tabla = new PdfPTable(11);
            tabla.setWidthPercentage(100);
            tabla.setSpacingBefore(15f);

            // Ajustamos anchos
            float[] anchos = {1.2f, 3.5f, 1.5f, 1.2f, 0.8f, 1.5f, 1.5f, 1.5f, 1.2f, 1.2f, 1.8f};
            tabla.setWidths(anchos);

            String[] headers = {"ID", "Cliente", "Fecha", "Hora", "H", "Est. Alq", "Subtotal", "Dscto.", "Mora", "Est. Pago", "Total"};
            for (String h : headers) {
                tabla.addCell(crearCeldaHeader(h, colorPrimario));
            }

            // 3. SQL CON LEFT JOIN PAGO
            String sql = """
            SELECT a.idAlquiler, 
                   t.nombre + ' ' + t.apellidos AS cliente,
                   a.fechaInicio, a.horaInicio, a.Duracion, a.estado AS estadoAlquiler,
                   ISNULL(a.subtotal, a.total - a.mora) as subtotal,
                   ISNULL(a.montoDescuento, 0) as descuento,
                   a.mora, a.total,
                   p.estado AS estadoPago
            FROM Alquiler a
            JOIN Turista t ON a.idTurista = t.idTurista
            LEFT JOIN Pago p ON a.idAlquiler = p.idAlquiler AND p.estado = 'Completado'
            WHERE a.fechaInicio BETWEEN ? AND ?
            ORDER BY a.fechaInicio ASC, a.horaInicio ASC
        """;

            double sumaSubtotal = 0, sumaDescuento = 0, sumaMora = 0, sumaTotal = 0;

            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                ps.setDate(1, f1);
                ps.setDate(2, f2);
                ResultSet rs = ps.executeQuery();

                boolean filaImpar = true;
                java.text.SimpleDateFormat sdfHora = new java.text.SimpleDateFormat("HH:mm");
                java.text.SimpleDateFormat sdfFecha = new java.text.SimpleDateFormat("dd/MM/yyyy");

                while (rs.next()) {
                    BaseColor colorFondo = filaImpar ? BaseColor.WHITE : colorFondoGris;

                    // A. Verificar Pago
                    String estPago = rs.getString("estadoPago");
                    boolean isPagado = (estPago != null && estPago.equalsIgnoreCase("Completado"));

                    // B. Definir Color Texto (Rojo si debe)
                    BaseColor colorTextoFila = isPagado ? colorTextoNormal : colorDeudor;
                    String textoEstadoPago = isPagado ? "PAGADO" : "DEBE";

                    // C. Llenar Celdas con Color
                    tabla.addCell(crearCeldaDataColor(rs.getString("idAlquiler"), colorFondo, Element.ALIGN_CENTER, colorTextoFila));
                    tabla.addCell(crearCeldaDataColor(rs.getString("cliente"), colorFondo, Element.ALIGN_LEFT, colorTextoFila));

                    java.sql.Date fecha = rs.getDate("fechaInicio");
                    tabla.addCell(crearCeldaDataColor(fecha != null ? sdfFecha.format(fecha) : "-", colorFondo, Element.ALIGN_CENTER, colorTextoFila));

                    java.sql.Time hora = rs.getTime("horaInicio");
                    tabla.addCell(crearCeldaDataColor(hora != null ? sdfHora.format(hora) : "-", colorFondo, Element.ALIGN_CENTER, colorTextoFila));

                    tabla.addCell(crearCeldaDataColor(String.valueOf(rs.getInt("Duracion")), colorFondo, Element.ALIGN_CENTER, colorTextoFila));
                    tabla.addCell(crearCeldaDataColor(rs.getString("estadoAlquiler"), colorFondo, Element.ALIGN_CENTER, colorTextoFila));

                    // Datos Financieros
                    double sub = rs.getDouble("subtotal");
                    double desc = rs.getDouble("descuento");
                    double mora = rs.getDouble("mora");
                    double tot = rs.getDouble("total");

                    // D. ACUMULAR SOLO SI PAGÓ
                    if (isPagado) {
                        sumaSubtotal += sub;
                        sumaDescuento += desc;
                        sumaMora += mora;
                        sumaTotal += tot;
                    }

                    tabla.addCell(crearCeldaDataColor(String.format("%.2f", sub), colorFondo, Element.ALIGN_RIGHT, colorTextoFila));
                    tabla.addCell(crearCeldaDataColor(desc > 0 ? String.format("%.2f", desc) : "-", colorFondo, Element.ALIGN_RIGHT, colorTextoFila));
                    tabla.addCell(crearCeldaDataColor(mora > 0 ? String.format("%.2f", mora) : "-", colorFondo, Element.ALIGN_RIGHT, colorTextoFila));

                    tabla.addCell(crearCeldaDataColor(textoEstadoPago, colorFondo, Element.ALIGN_CENTER, colorTextoFila));

                    PdfPCell cTotal = crearCeldaDataColor(String.format("%.2f", tot), colorFondo, Element.ALIGN_RIGHT, colorTextoFila);
                    cTotal.setPhrase(new Phrase(String.format("%.2f", tot), new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD, colorTextoFila)));
                    tabla.addCell(cTotal);

                    filaImpar = !filaImpar;
                }
            }
            doc.add(tabla);

            // 4. RESUMEN (CAJA REAL)
            doc.add(new Paragraph("\n"));
            Paragraph nota = new Paragraph("* Los montos en ROJO indican deudas y NO se incluyen en la caja real.",
                    new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, colorDeudor));
            nota.setAlignment(Element.ALIGN_RIGHT);
            doc.add(nota);
            doc.add(new Paragraph("\n"));

            PdfPTable tablaResumen = new PdfPTable(2);
            tablaResumen.setWidthPercentage(40);
            tablaResumen.setHorizontalAlignment(Element.ALIGN_RIGHT);

            tablaResumen.addCell(crearCeldaHeader("SUBTOTAL (COBRADO)", BaseColor.GRAY));
            tablaResumen.addCell(crearCeldaData("S/ " + String.format("%.2f", sumaSubtotal), BaseColor.WHITE, Element.ALIGN_RIGHT));

            tablaResumen.addCell(crearCeldaHeader("DESCUENTOS", BaseColor.GRAY));
            tablaResumen.addCell(crearCeldaData("- S/ " + String.format("%.2f", sumaDescuento), BaseColor.WHITE, Element.ALIGN_RIGHT));

            tablaResumen.addCell(crearCeldaHeader("MORAS COBRADAS", BaseColor.GRAY));
            tablaResumen.addCell(crearCeldaData("+ S/ " + String.format("%.2f", sumaMora), BaseColor.WHITE, Element.ALIGN_RIGHT));

            // IGV
            double valorVenta = sumaTotal / 1.18;
            double impuesto = sumaTotal - valorVenta;

            tablaResumen.addCell(crearCeldaHeader("VALOR VENTA (NETO)", BaseColor.DARK_GRAY));
            tablaResumen.addCell(crearCeldaData("S/ " + String.format("%.2f", valorVenta), BaseColor.WHITE, Element.ALIGN_RIGHT));

            tablaResumen.addCell(crearCeldaHeader("IGV (18%)", BaseColor.DARK_GRAY));
            tablaResumen.addCell(crearCeldaData("S/ " + String.format("%.2f", impuesto), BaseColor.WHITE, Element.ALIGN_RIGHT));

            // TOTAL
            Font fontTotal = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
            PdfPCell cTotalL = new PdfPCell(new Paragraph("CAJA REAL (RECAUDADO)", fontTotal));
            cTotalL.setBackgroundColor(colorPrimario);
            tablaResumen.addCell(cTotalL);

            PdfPCell cTotalV = new PdfPCell(new Paragraph("S/ " + String.format("%.2f", sumaTotal), fontTotal));
            cTotalV.setBackgroundColor(colorPrimario);
            cTotalV.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tablaResumen.addCell(cTotalV);

            doc.add(tablaResumen);
            doc.close();
            abrirPDF(ruta);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error reporte fechas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ======================================================
    // REPORTE DE ALQUILERES POR USUARIO
    // ======================================================
    private void generarReporteAlquileresPorUsuario() {
        String idUsuario = JOptionPane.showInputDialog(this, "Ingrese el ID del usuario (Ej: U001):");
        if (idUsuario == null || idUsuario.trim().isEmpty()) {
            return;
        }
        idUsuario = idUsuario.trim().toUpperCase();

        String nombreUsuario = "Usuario " + idUsuario;
        try (PreparedStatement ps = conexion.prepareStatement("SELECT nameUsuario FROM ActorUsuario WHERE idUsuario = ?")) {
            ps.setString(1, idUsuario);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                nombreUsuario = rs.getString("nameUsuario");
            } else {
                JOptionPane.showMessageDialog(this, "Usuario no encontrado.");
                return;
            }
        } catch (SQLException e) {
        }

        String ruta = crearRuta("reporte_ventas_" + idUsuario + ".pdf");

        // Colores
        BaseColor colorPrimario = new BaseColor(33, 97, 140);
        BaseColor colorFondoGris = new BaseColor(242, 243, 244);
        BaseColor colorDeudor = new BaseColor(192, 57, 43);
        BaseColor colorTextoNormal = BaseColor.DARK_GRAY;

        try {
            Document doc = new Document(PageSize.A4.rotate(), 20, 20, 30, 30);
            PdfWriter.getInstance(doc, new FileOutputStream(ruta));
            doc.open();

            agregarEncabezadoProfesional(doc, "RENDIMIENTO: " + nombreUsuario.toUpperCase(), colorPrimario);

            PdfPTable tabla = new PdfPTable(11);
            tabla.setWidthPercentage(100);
            tabla.setSpacingBefore(15f);
            float[] anchos = {1.2f, 3.5f, 1.5f, 1.2f, 0.8f, 1.5f, 1.5f, 1.5f, 1.2f, 1.2f, 1.8f};
            tabla.setWidths(anchos);

            String[] headers = {"ID", "Cliente", "Fecha", "Hora", "H", "Est. Alq", "Subtotal", "Dscto.", "Mora", "Est. Pago", "Total"};
            for (String h : headers) {
                tabla.addCell(crearCeldaHeader(h, colorPrimario));
            }

            String sql = """
            SELECT a.idAlquiler, 
                   t.nombre + ' ' + t.apellidos AS cliente,
                   a.fechaInicio, a.horaInicio, a.Duracion, a.estado AS estadoAlquiler,
                   ISNULL(a.subtotal, a.total - a.mora) as subtotal,
                   ISNULL(a.montoDescuento, 0) as descuento,
                   a.mora, a.total,
                   p.estado AS estadoPago
            FROM Alquiler a
            JOIN Turista t ON a.idTurista = t.idTurista
            LEFT JOIN Pago p ON a.idAlquiler = p.idAlquiler AND p.estado = 'Completado'
            WHERE a.idUsuario = ?
            ORDER BY a.fechaInicio DESC, a.horaInicio DESC
        """;

            double sumaSubtotal = 0, sumaDescuento = 0, sumaMora = 0, sumaTotal = 0;

            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                ps.setString(1, idUsuario);
                ResultSet rs = ps.executeQuery();

                boolean filaImpar = true;
                java.text.SimpleDateFormat sdfHora = new java.text.SimpleDateFormat("HH:mm");
                java.text.SimpleDateFormat sdfFecha = new java.text.SimpleDateFormat("dd/MM/yyyy");

                while (rs.next()) {
                    BaseColor colorFondo = filaImpar ? BaseColor.WHITE : colorFondoGris;

                    String estPago = rs.getString("estadoPago");
                    boolean isPagado = (estPago != null && estPago.equalsIgnoreCase("Completado"));
                    BaseColor colorTextoFila = isPagado ? colorTextoNormal : colorDeudor;
                    String textoEstadoPago = isPagado ? "PAGADO" : "DEBE";

                    tabla.addCell(crearCeldaDataColor(rs.getString("idAlquiler"), colorFondo, Element.ALIGN_CENTER, colorTextoFila));
                    tabla.addCell(crearCeldaDataColor(rs.getString("cliente"), colorFondo, Element.ALIGN_LEFT, colorTextoFila));

                    java.sql.Date fecha = rs.getDate("fechaInicio");
                    tabla.addCell(crearCeldaDataColor(fecha != null ? sdfFecha.format(fecha) : "-", colorFondo, Element.ALIGN_CENTER, colorTextoFila));

                    java.sql.Time hora = rs.getTime("horaInicio");
                    tabla.addCell(crearCeldaDataColor(hora != null ? sdfHora.format(hora) : "-", colorFondo, Element.ALIGN_CENTER, colorTextoFila));

                    tabla.addCell(crearCeldaDataColor(String.valueOf(rs.getInt("Duracion")), colorFondo, Element.ALIGN_CENTER, colorTextoFila));
                    tabla.addCell(crearCeldaDataColor(rs.getString("estadoAlquiler"), colorFondo, Element.ALIGN_CENTER, colorTextoFila));

                    double sub = rs.getDouble("subtotal");
                    double desc = rs.getDouble("descuento");
                    double mora = rs.getDouble("mora");
                    double tot = rs.getDouble("total");

                    if (isPagado) {
                        sumaSubtotal += sub;
                        sumaDescuento += desc;
                        sumaMora += mora;
                        sumaTotal += tot;
                    }

                    tabla.addCell(crearCeldaDataColor(String.format("%.2f", sub), colorFondo, Element.ALIGN_RIGHT, colorTextoFila));
                    tabla.addCell(crearCeldaDataColor(desc > 0 ? String.format("%.2f", desc) : "-", colorFondo, Element.ALIGN_RIGHT, colorTextoFila));
                    tabla.addCell(crearCeldaDataColor(mora > 0 ? String.format("%.2f", mora) : "-", colorFondo, Element.ALIGN_RIGHT, colorTextoFila));

                    tabla.addCell(crearCeldaDataColor(textoEstadoPago, colorFondo, Element.ALIGN_CENTER, colorTextoFila));

                    PdfPCell cTotal = crearCeldaDataColor(String.format("%.2f", tot), colorFondo, Element.ALIGN_RIGHT, colorTextoFila);
                    cTotal.setPhrase(new Phrase(String.format("%.2f", tot), new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD, colorTextoFila)));
                    tabla.addCell(cTotal);

                    filaImpar = !filaImpar;
                }
            }
            doc.add(tabla);

            // RESUMEN
            doc.add(new Paragraph("\n"));
            Paragraph nota = new Paragraph("* Los montos en ROJO indican deudas y NO se incluyen en la caja real.",
                    new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, colorDeudor));
            nota.setAlignment(Element.ALIGN_RIGHT);
            doc.add(nota);
            doc.add(new Paragraph("\n"));

            PdfPTable tablaResumen = new PdfPTable(2);
            tablaResumen.setWidthPercentage(40);
            tablaResumen.setHorizontalAlignment(Element.ALIGN_RIGHT);

            tablaResumen.addCell(crearCeldaHeader("SUBTOTAL (COBRADO)", BaseColor.GRAY));
            tablaResumen.addCell(crearCeldaData("S/ " + String.format("%.2f", sumaSubtotal), BaseColor.WHITE, Element.ALIGN_RIGHT));

            tablaResumen.addCell(crearCeldaHeader("DESCUENTOS", BaseColor.GRAY));
            tablaResumen.addCell(crearCeldaData("- S/ " + String.format("%.2f", sumaDescuento), BaseColor.WHITE, Element.ALIGN_RIGHT));

            tablaResumen.addCell(crearCeldaHeader("MORAS COBRADAS", BaseColor.GRAY));
            tablaResumen.addCell(crearCeldaData("+ S/ " + String.format("%.2f", sumaMora), BaseColor.WHITE, Element.ALIGN_RIGHT));

            // IGV
            double valorVenta = sumaTotal / 1.18;
            double impuesto = sumaTotal - valorVenta;

            tablaResumen.addCell(crearCeldaHeader("VALOR VENTA (NETO)", BaseColor.DARK_GRAY));
            tablaResumen.addCell(crearCeldaData("S/ " + String.format("%.2f", valorVenta), BaseColor.WHITE, Element.ALIGN_RIGHT));

            tablaResumen.addCell(crearCeldaHeader("IGV (18%)", BaseColor.DARK_GRAY));
            tablaResumen.addCell(crearCeldaData("S/ " + String.format("%.2f", impuesto), BaseColor.WHITE, Element.ALIGN_RIGHT));

            // TOTAL
            Font fontTotal = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
            PdfPCell cTotalL = new PdfPCell(new Paragraph("CAJA GENERADA (REAL)", fontTotal));
            cTotalL.setBackgroundColor(colorPrimario);
            tablaResumen.addCell(cTotalL);

            PdfPCell cTotalV = new PdfPCell(new Paragraph("S/ " + String.format("%.2f", sumaTotal), fontTotal));
            cTotalV.setBackgroundColor(colorPrimario);
            cTotalV.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tablaResumen.addCell(cTotalV);

            doc.add(tablaResumen);
            doc.close();
            abrirPDF(ruta);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error reporte usuario: " + e.getMessage());
            e.printStackTrace();
        }
    }
    // Nuevo método helper para cambiar el color del TEXTO (no solo el fondo)

    private PdfPCell crearCeldaDataColor(String texto, BaseColor colorFondo, int alineacion, BaseColor colorTexto) {
        String textoFinal = (texto == null || texto.equalsIgnoreCase("null")) ? "-" : texto;

        // Aquí usamos el colorTexto que recibimos
        com.itextpdf.text.Font fontData = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 8, com.itextpdf.text.Font.NORMAL, colorTexto);

        PdfPCell cell = new PdfPCell(new Paragraph(textoFinal, fontData));
        cell.setBackgroundColor(colorFondo);
        cell.setHorizontalAlignment(alineacion);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(4f);
        return cell;
    }
}
