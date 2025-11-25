package igu;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.toedter.calendar.JDateChooser;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.*;
import javax.swing.*;

public class PanelReportes extends javax.swing.JPanel {

    private Connection conexion;

    // Componentes del panel
    private JButton btnTuristas, btnRecursos, btnAlquileres, btnPorFecha, btnPorUsuario;
    private JDateChooser jDateInicio, jDateFin;
    private JLabel lblDesde, lblHasta;

    public PanelReportes(Connection conexion) {
        this.conexion = conexion;
        initComponents();
        
        // === REPORTE DE BOLETA DE PAGO ===
        JButton btnBoleta = crearBoton("💳 Generar Boleta de Pago");
        btnBoleta.addActionListener(evt -> generarBoletaPago());
        add(btnBoleta, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 370, 340, 40));

    }
    
    // ======================================================
// BOLETA DE PAGO INDIVIDUAL
// ======================================================
    private void generarBoletaPago() {
        String idPago = JOptionPane.showInputDialog(this, "Ingrese el ID del pago (Ej: P001):");
        if (idPago == null || idPago.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar un ID de pago válido.");
            return;
        }

        idPago = idPago.trim().toUpperCase();
        String ruta = crearRuta("boleta_pago_" + idPago + ".pdf");

        try {
            // 🔹 1️⃣ Obtener datos completos del pago + alquiler + turista
            String sql = """
            SELECT p.idPago, p.fechaPago, p.monto AS montoSinIgv, p.igv, p.montoConIGV, 
                   p.metodoPago, p.estado, 
                   a.idAlquiler, t.idTurista, t.nombre, t.apellidos, t.dni
            FROM Pago p
            JOIN Alquiler a ON p.idAlquiler = a.idAlquiler
            JOIN Turista t ON a.idTurista = t.idTurista
            WHERE p.idPago = ?
        """;

            PreparedStatement ps = conexion.prepareStatement(sql);
            ps.setString(1, idPago);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "❌ No se encontró el pago con ID: " + idPago);
                return;
            }

            // 🔹 2️⃣ Obtener los valores reales
            double montoSinIgv = rs.getDouble("montoSinIgv");
            double igvPorcentaje = rs.getDouble("igv"); // 0.18
            double montoConIgv = rs.getDouble("montoConIGV");

            // Calcular IGV en soles desde el porcentaje
            double igvSoles = montoSinIgv * igvPorcentaje;

            // Redondear a 2 decimales
            montoSinIgv = Math.round(montoSinIgv * 100.0) / 100.0;
            igvSoles = Math.round(igvSoles * 100.0) / 100.0;
            montoConIgv = Math.round(montoConIgv * 100.0) / 100.0;

            // 🔹 3️⃣ Crear el documento PDF
            Document doc = new Document(PageSize.A5);
            PdfWriter.getInstance(doc, new FileOutputStream(ruta));
            doc.open();

            // Encabezado tipo boleta
            Paragraph titulo = new Paragraph("BOLETA DE PAGO", new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD));
            titulo.setAlignment(Element.ALIGN_CENTER);
            doc.add(titulo);
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Alquileres Turísticos del Norte - Piura"));
            doc.add(new Paragraph("RUC: 20457896543"));
            doc.add(new Paragraph("Dirección: Av. Grau 1234, Piura"));
            doc.add(new Paragraph("Teléfono: (073) 451234"));
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("------------------------------------------------------------------"));
            doc.add(new Paragraph(" "));

            // 🔹 4️⃣ Datos del cliente
            PdfPTable tablaCliente = new PdfPTable(2);
            tablaCliente.setWidthPercentage(100);
            tablaCliente.addCell("Cliente:");
            tablaCliente.addCell(rs.getString("nombre") + " " + rs.getString("apellidos"));
            tablaCliente.addCell("DNI:");
            tablaCliente.addCell(rs.getString("dni"));
            tablaCliente.addCell("ID Turista:");
            tablaCliente.addCell(rs.getString("idTurista"));
            tablaCliente.addCell("ID Alquiler:");
            tablaCliente.addCell(rs.getString("idAlquiler"));
            tablaCliente.addCell("ID Pago:");
            tablaCliente.addCell(rs.getString("idPago"));
            tablaCliente.addCell("Fecha de Pago:");
            tablaCliente.addCell(rs.getDate("fechaPago").toString());
            tablaCliente.addCell("Método de Pago:");
            tablaCliente.addCell(rs.getString("metodoPago"));
            tablaCliente.addCell("Estado:");
            tablaCliente.addCell(rs.getString("estado"));
            doc.add(tablaCliente);

            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("------------------------------------------------------------------"));
            doc.add(new Paragraph(" "));

            // 🔹 5️⃣ Totales (sin IGV, IGV, total)
            PdfPTable tablaTotales = new PdfPTable(2);
            tablaTotales.setWidthPercentage(70);
            tablaTotales.setHorizontalAlignment(Element.ALIGN_RIGHT);

            tablaTotales.addCell("Monto sin IGV (S/):");
            tablaTotales.addCell(String.format("%.2f", montoSinIgv));

            tablaTotales.addCell("IGV (18%):");
            tablaTotales.addCell(String.format("%.2f", igvSoles));

            tablaTotales.addCell("Total con IGV (S/):");
            PdfPCell totalCell = new PdfPCell(new Phrase(String.format("%.2f", montoConIgv)));
            totalCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            tablaTotales.addCell(totalCell);

            doc.add(tablaTotales);

            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("¡Gracias por su preferencia!", new Font(Font.FontFamily.HELVETICA, 12, Font.ITALIC)));
            doc.close();

            abrirPDF(ruta);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al generar boleta: " + e.getMessage());
        }
    }


    private void initComponents() {
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        setBackground(java.awt.Color.WHITE);
        setSize(640, 500);

        JLabel titulo = new JLabel("📊 GENERACIÓN DE REPORTES DEL SISTEMA");
        titulo.setHorizontalAlignment(SwingConstants.CENTER);
        titulo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 16));
        add(titulo, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 20, 640, 30));

        // === BOTONES DE REPORTES PRINCIPALES ===
        btnTuristas = crearBoton("🧍‍♂️ Reporte de Turistas");
        btnTuristas.addActionListener(evt -> generarReporteTuristas());
        add(btnTuristas, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 80, 340, 40));

        btnRecursos = crearBoton("📦 Reporte de Recursos");
        btnRecursos.addActionListener(evt -> generarReporteRecursos());
        add(btnRecursos, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 130, 340, 40));

        btnAlquileres = crearBoton("🧾 Reporte General de Alquileres");
        btnAlquileres.addActionListener(evt -> generarReporteAlquileres());
        add(btnAlquileres, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 180, 340, 40));

        // === FILTRO POR FECHA ===
        lblDesde = new JLabel("Desde:");
        add(lblDesde, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 235, -1, -1));

        jDateInicio = new JDateChooser();
        add(jDateInicio, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 230, 130, -1));

        lblHasta = new JLabel("Hasta:");
        add(lblHasta, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 235, -1, -1));

        jDateFin = new JDateChooser();
        add(jDateFin, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 230, 130, -1));

        btnPorFecha = crearBoton("🗓 Reporte de Alquileres por Fechas");
        btnPorFecha.addActionListener(evt -> generarReporteAlquileresPorFecha());
        add(btnPorFecha, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 270, 340, 40));

        // === REPORTE POR USUARIO ===
        btnPorUsuario = crearBoton("👤 Reporte de Alquileres por Usuario");
        btnPorUsuario.addActionListener(evt -> generarReporteAlquileresPorUsuario());
        add(btnPorUsuario, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 320, 340, 40));
    }

    // ======================================================
    // MÉTODOS AUXILIARES DE DISEÑO
    // ======================================================
    private JButton crearBoton(String texto) {
        JButton boton = new JButton(texto);
        boton.setFont(new java.awt.Font("Segoe UI Emoji", java.awt.Font.PLAIN, 14));
        boton.setBackground(new java.awt.Color(245, 245, 245));
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createLineBorder(new java.awt.Color(220, 220, 220)));
        boton.setHorizontalAlignment(SwingConstants.LEFT);
        boton.setIconTextGap(10);
        boton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                boton.setBackground(new java.awt.Color(230, 230, 230));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                boton.setBackground(new java.awt.Color(245, 245, 245));
            }
        });
        return boton;
    }

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

    private void agregarEncabezado(Document doc, String titulo) throws DocumentException {
        Paragraph p = new Paragraph(titulo.toUpperCase(),
                new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD));
        p.setAlignment(Element.ALIGN_CENTER);
        p.setSpacingAfter(15);
        doc.add(p);
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
    // ======================================================
    private void generarReporteTuristas() {
        String ruta = crearRuta("reporte_turistas.pdf");
        try {
            Document doc = new Document();
            PdfWriter.getInstance(doc, new FileOutputStream(ruta));
            doc.open();
            agregarEncabezado(doc, "REPORTE DE TURISTAS");

            PdfPTable tabla = new PdfPTable(6);
            tabla.setWidthPercentage(100);
            tabla.addCell("ID Turista");
            tabla.addCell("Nombre");
            tabla.addCell("Apellidos");
            tabla.addCell("DNI");
            tabla.addCell("Nacionalidad");
            tabla.addCell("Contacto");

            String sql = """
                SELECT idTurista, nombre, apellidos, dni, nacionalidad, contacto
                FROM Turista
                ORDER BY apellidos ASC, nombre ASC
            """;

            try (Statement st = conexion.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    tabla.addCell(rs.getString("idTurista"));
                    tabla.addCell(rs.getString("nombre"));
                    tabla.addCell(rs.getString("apellidos"));
                    tabla.addCell(rs.getString("dni"));
                    tabla.addCell(rs.getString("nacionalidad"));
                    tabla.addCell(rs.getString("contacto"));
                }
            }

            doc.add(tabla);
            doc.close();
            abrirPDF(ruta);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al generar reporte: " + e.getMessage());
        }
    }

    // ======================================================
    // REPORTE DE RECURSOS
    // ======================================================
    private void generarReporteRecursos() {
        String ruta = crearRuta("reporte_recursos.pdf");
        try {
            Document doc = new Document();
            PdfWriter.getInstance(doc, new FileOutputStream(ruta));
            doc.open();
            agregarEncabezado(doc, "REPORTE DE RECURSOS TURÍSTICOS");

            PdfPTable tabla = new PdfPTable(7);
            tabla.setWidthPercentage(100);
            tabla.addCell("ID Recurso");
            tabla.addCell("Tipo");
            tabla.addCell("Descripción");
            tabla.addCell("Tarifa x Hora");
            tabla.addCell("Estado");
            tabla.addCell("Ubicación");
            tabla.addCell("Cantidad");

            String sql = "SELECT idRecursos, tipo, descripcion, tarifaHora, estado, ubicacion, cantidad FROM Recursos ORDER BY tipo";
            try (Statement st = conexion.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    tabla.addCell(rs.getString("idRecursos"));
                    tabla.addCell(rs.getString("tipo"));
                    tabla.addCell(rs.getString("descripcion"));
                    tabla.addCell(String.format("%.2f", rs.getDouble("tarifaHora")));
                    tabla.addCell(rs.getString("estado"));
                    tabla.addCell(rs.getString("ubicacion"));
                    tabla.addCell(rs.getString("cantidad"));
                }
            }
            doc.add(tabla);
            doc.close();
            abrirPDF(ruta);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al generar reporte: " + e.getMessage());
        }
    }

    // ======================================================
    // REPORTE GENERAL DE ALQUILERES
    // ======================================================
    private void generarReporteAlquileres() {
        String ruta = crearRuta("reporte_alquileres.pdf");
        try {
            Document doc = new Document();
            PdfWriter.getInstance(doc, new FileOutputStream(ruta));
            doc.open();
            agregarEncabezado(doc, "REPORTE GENERAL DE ALQUILERES");

            PdfPTable tabla = new PdfPTable(10);
            tabla.setWidthPercentage(100);
            tabla.addCell("ID");
            tabla.addCell("Turista");
            tabla.addCell("Fecha");
            tabla.addCell("Hora Inicio");
            tabla.addCell("Duración");
            tabla.addCell("Total (S/)");
            tabla.addCell("Estado");
            tabla.addCell("Promoción");
            tabla.addCell("Usuario");
            tabla.addCell("Hora Final");

            String sql = "SELECT * FROM Alquiler ORDER BY fechaInicio DESC";
            try (Statement st = conexion.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    tabla.addCell(rs.getString("idAlquiler"));
                    tabla.addCell(rs.getString("idTurista"));
                    tabla.addCell(rs.getDate("fechaInicio").toString());
                    tabla.addCell(rs.getTime("horaInicio") != null ? rs.getTime("horaInicio").toString() : "-");
                    tabla.addCell(String.valueOf(rs.getInt("Duracion")));
                    tabla.addCell(String.format("%.2f", rs.getDouble("total")));
                    tabla.addCell(rs.getString("estado"));
                    tabla.addCell(rs.getString("idPromocion") != null ? rs.getString("idPromocion") : "-");
                    tabla.addCell(rs.getString("idUsuario"));
                    tabla.addCell(rs.getTime("horaFinal") != null ? rs.getTime("horaFinal").toString() : "-");
                }
            }
            doc.add(tabla);
            doc.close();
            abrirPDF(ruta);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al generar reporte: " + e.getMessage());
        }
    }

    // ======================================================
    // REPORTE DE ALQUILERES POR FECHA
    // ======================================================
    private void generarReporteAlquileresPorFecha() {
        java.util.Date fechaIni = jDateInicio.getDate();
        java.util.Date fechaFin = jDateFin.getDate();

        if (fechaIni == null || fechaFin == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar ambas fechas.");
            return;
        }

        java.sql.Date f1 = new java.sql.Date(fechaIni.getTime());
        java.sql.Date f2 = new java.sql.Date(fechaFin.getTime());

        String ruta = crearRuta("reporte_alquileres_por_fecha.pdf");
        try {
            Document doc = new Document();
            PdfWriter.getInstance(doc, new FileOutputStream(ruta));
            doc.open();
            agregarEncabezado(doc, "ALQUILERES ENTRE " + f1 + " Y " + f2);

            PdfPTable tabla = new PdfPTable(8);
            tabla.setWidthPercentage(100);
            tabla.addCell("ID");
            tabla.addCell("Turista");
            tabla.addCell("Fecha");
            tabla.addCell("Hora Inicio");
            tabla.addCell("Duración");
            tabla.addCell("Total (S/)");
            tabla.addCell("Estado");
            tabla.addCell("Usuario");

            String sql = """
                SELECT idAlquiler, idTurista, fechaInicio, horaInicio, Duracion, total, estado, idUsuario
                FROM Alquiler
                WHERE fechaInicio BETWEEN ? AND ?
                ORDER BY fechaInicio
            """;

            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                ps.setDate(1, f1);
                ps.setDate(2, f2);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    tabla.addCell(rs.getString("idAlquiler"));
                    tabla.addCell(rs.getString("idTurista"));
                    tabla.addCell(rs.getDate("fechaInicio").toString());
                    tabla.addCell(rs.getTime("horaInicio") != null ? rs.getTime("horaInicio").toString() : "-");
                    tabla.addCell(String.valueOf(rs.getInt("Duracion")));
                    tabla.addCell(String.format("%.2f", rs.getDouble("total")));
                    tabla.addCell(rs.getString("estado"));
                    tabla.addCell(rs.getString("idUsuario"));
                }
            }

            doc.add(tabla);
            doc.close();
            abrirPDF(ruta);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al generar reporte: " + e.getMessage());
        }
    }

    // ======================================================
    // REPORTE DE ALQUILERES POR USUARIO
    // ======================================================
    private void generarReporteAlquileresPorUsuario() {
        String idUsuario = JOptionPane.showInputDialog(this, "Ingrese el ID del usuario (Ej: U001):");
        if (idUsuario == null || idUsuario.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar un ID de usuario válido.");
            return;
        }

        idUsuario = idUsuario.trim().toUpperCase();
        String ruta = crearRuta("reporte_alquileres_por_usuario_" + idUsuario + ".pdf");

        try {
            String nombreUsuario = "Desconocido";
            String sqlNombre = "SELECT nameUsuario FROM ActorUsuario WHERE idUsuario = ?";
            try (PreparedStatement ps = conexion.prepareStatement(sqlNombre)) {
                ps.setString(1, idUsuario);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    nombreUsuario = rs.getString("nameUsuario");
                } else {
                    JOptionPane.showMessageDialog(this, "No se encontró ningún usuario con el ID: " + idUsuario);
                    return;
                }
            }

            Document doc = new Document();
            PdfWriter.getInstance(doc, new FileOutputStream(ruta));
            doc.open();
            agregarEncabezado(doc, "ALQUILERES REGISTRADOS POR " + nombreUsuario + " (" + idUsuario + ")");

            PdfPTable tabla = new PdfPTable(7);
            tabla.setWidthPercentage(100);
            tabla.addCell("ID Alquiler");
            tabla.addCell("ID Turista");
            tabla.addCell("Fecha Inicio");
            tabla.addCell("Hora Inicio");
            tabla.addCell("Duración");
            tabla.addCell("Total (S/)");
            tabla.addCell("Estado");

            String sql = """
                SELECT idAlquiler, idTurista, fechaInicio, horaInicio, Duracion, total, estado
                FROM Alquiler
                WHERE idUsuario = ?
                ORDER BY fechaInicio DESC
            """;

            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                ps.setString(1, idUsuario);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    tabla.addCell(rs.getString("idAlquiler"));
                    tabla.addCell(rs.getString("idTurista"));
                    tabla.addCell(rs.getDate("fechaInicio").toString());
                    tabla.addCell(rs.getTime("horaInicio") != null ? rs.getTime("horaInicio").toString() : "-");
                    tabla.addCell(String.valueOf(rs.getInt("Duracion")));
                    tabla.addCell(String.format("%.2f", rs.getDouble("total")));
                    tabla.addCell(rs.getString("estado"));
                }
            }

            doc.add(tabla);
            doc.close();
            abrirPDF(ruta);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al generar reporte: " + e.getMessage());
        }
    }
}
