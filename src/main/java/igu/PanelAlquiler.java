/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package igu;

import java.awt.BorderLayout;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import logica.Recurso;

/**
 *
 * @author rafae
 */
public class PanelAlquiler extends javax.swing.JPanel {
    private Connection conexion;
    private String idUsuario;
    private String modo;
    private boolean modoLimpiar = false;

    /**
     * Creates new form PanelAlquiler
     */
    public PanelAlquiler(Connection conexion, String idUsuario) {
        initComponents();

        PanelDegradado fondo = new PanelDegradado();

// MUY IMPORTANTE: dejar layout por defecto (BorderLayout)
        fondo.setLayout(new java.awt.BorderLayout());

// Pasar jPanel1 dentro del panel degradado
        jPanel1.setOpaque(false);
        fondo.add(jPanel1, BorderLayout.CENTER);

// Ahora reemplazas en el contenedor padre
        remove(jPanel1);
        add(fondo, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 640, 500));

// refrescar
        revalidate();
        repaint();
        jSpinnerHorasUsadas.addChangeListener(e -> calcularSubtotalAutomatico());
       // ocultarColumna(0);
        ocultarColumna(1);
       

        this.conexion = conexion;
        this.idUsuario = idUsuario;
        txtIdVendedor.setText(idUsuario);
        habilitarCampos(false);
        tblDetalles.getTableHeader().setReorderingAllowed(false);
        tblDetalles.setDefaultEditor(Object.class, null);
          cargarPromociones();
          cargarRecursos();

    }

    private void ocultarColumna(int index) {
        tblDetalles.getColumnModel().getColumn(index).setMinWidth(0);
        tblDetalles.getColumnModel().getColumn(index).setMaxWidth(0);
        tblDetalles.getColumnModel().getColumn(index).setWidth(0);
    }


    private void aplicarPromocionAutomatica() {
        try {
            int duracion = txtDuracion.getText().isEmpty() ? 0 : Integer.parseInt(txtDuracion.getText());

            // 🔹 Obtener el total base (sin descuento previo)
            double totalBase;
            if (txtTotal.getClientProperty("totalBase") == null) {
                totalBase = Double.parseDouble(txtTotal.getText().isEmpty() ? "0" : txtTotal.getText());
                txtTotal.putClientProperty("totalBase", totalBase);
            } else {
                totalBase = (double) txtTotal.getClientProperty("totalBase");
            }

            double totalConDescuento = totalBase;
            String promoAplicada = "Ninguna";
            double mejorPorcentaje = 0;

            // 🔹 Consultar las promociones disponibles
            String sql = "SELECT descripcion, condicionHoras FROM Promocion";
            try (PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    String descripcion = rs.getString("descripcion"); // ej. "20%"
                    int condicion = rs.getInt("condicionHoras");

                    if (duracion >= condicion) {
                        double porcentaje = Double.parseDouble(descripcion.replace("%", ""));
                        if (porcentaje > mejorPorcentaje) {
                            mejorPorcentaje = porcentaje;
                            promoAplicada = descripcion;
                        }
                    }
                }
            }

            // 🔹 Aplicar el descuento una sola vez
            if (mejorPorcentaje > 0) {
                totalConDescuento = totalBase - (totalBase * mejorPorcentaje / 100);
                JOptionPane.showMessageDialog(this,
                        "✅ Se aplicó automáticamente la promoción de " + mejorPorcentaje + "% por alcanzar " + duracion + " horas.");
            }

            // 🔹 Actualizar total y selección en ComboBox
            txtTotal.setText(String.format("%.2f", totalConDescuento));

            boolean encontrada = false;
            for (int i = 0; i < jComboBoxPromos.getItemCount(); i++) {
                if (jComboBoxPromos.getItemAt(i).contains(promoAplicada)) {
                    jComboBoxPromos.setSelectedIndex(i);
                    encontrada = true;
                    break;
                }
            }
            if (!encontrada) {
                jComboBoxPromos.setSelectedIndex(0); // Ninguna
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al aplicar promoción: " + e.getMessage());
        }
    }

    private void cargarPromociones() {
        jComboBoxPromos.removeAllItems();
        jComboBoxPromos.addItem("Ninguna");

        String sql = "SELECT descripcion, condicionHoras FROM Promocion";
        try (PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // Solo añadimos la descripción, como "10%", "20%", etc.
                jComboBoxPromos.addItem(rs.getString("descripcion"));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar promociones: " + e.getMessage());
        }
    }
    
    private void cargarRecursos() {
        jComboBoxRecursos.removeAllItems();

        String sql = """
        SELECT idRecursos, tipo, descripcion, tarifaHora, estado, ubicacion
        FROM Recursos
        WHERE estado = 'Disponible'
    """;

        try (PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Recurso r = new Recurso(
                        rs.getString("idRecursos"),
                        rs.getString("tipo"),
                        rs.getString("descripcion"),
                        rs.getDouble("tarifaHora"),
                        rs.getString("estado"),
                        rs.getString("ubicacion")
                );

                jComboBoxRecursos.addItem(r);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error al cargar recursos: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }



   
    private void limpiarCampos() {
             modoLimpiar = true;
        txtIdAlquiler.setText("");
        txtIdTurista.setText("");
      
        txtNombreTuri.setText("");
        jDateChooserFecha.setDate(null);
        txtDuracion.setText("");
        jComboBoxEstado.setSelectedIndex(-1);
        txtNombreTuri.setText("");
        txtHora.setText("");
        jComboBoxPromos.setSelectedIndex(-1);
        jComboBoxRecursos.setSelectedIndex(-1);
        jSpinnerHorasUsadas.setValue(0);
        txtPrecioHora.setText("");
        txtSub.setText("");
        txtTotal.setText("");
        
        javax.swing.table.DefaultTableModel modelo = (javax.swing.table.DefaultTableModel) tblDetalles.getModel();
        modelo.setRowCount(0);
        
        modoLimpiar=false;

    }

    private void calcularDuracion() {
        try {
            int horasUsadas = (int) jSpinnerHorasUsadas.getValue();

            // Obtener la duración actual (0 si está vacío)
            int duracionActual = txtDuracion.getText().isEmpty() ? 0 : Integer.parseInt(txtDuracion.getText());

            // La duración debe ser la MAYOR entre las horas de los detalles
            int nuevaDuracion = Math.max(duracionActual, horasUsadas);
            txtDuracion.setText(String.valueOf(nuevaDuracion));

            // Actualizar hora final en base a la mayor duración
            String horaInicialTexto = txtHora.getText().trim();
            if (!horaInicialTexto.isEmpty()) {

                // Normalizar formato proveniente de SQL
                if (horaInicialTexto.contains(".")) {
                    horaInicialTexto = horaInicialTexto.substring(0, horaInicialTexto.indexOf("."));
                }

                // Ajustar patrón del formato
                java.time.format.DateTimeFormatter formato
                        = horaInicialTexto.length() > 5
                        ? java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")
                        : java.time.format.DateTimeFormatter.ofPattern("HH:mm");

                java.time.LocalTime horaInicial = java.time.LocalTime.parse(horaInicialTexto, formato);
                java.time.LocalTime horaFinal = horaInicial.plusHours(nuevaDuracion);

                // Mostrar en HH:mm
                txtHoraFin.setText(
                        horaFinal.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
                );
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al actualizar duración: " + e.getMessage());
        }
    }

    
    private void habilitarCampos(boolean habilitar) {
        btnAgregarRe.setEnabled(habilitar);
        btnEliminarRe.setEnabled(habilitar);
        jComboBoxRecursos.setEnabled(habilitar);
        txtIdAlquiler.setEditable(false);
        txtHoraFin.setEditable(false);
        txtIdTurista.setEditable(false);
        txtIdVendedor.setEditable(false);
        txtNombreTuri.setEditable(false); // se llena automáticamente al buscar
         // igual
        txtDuracion.setEditable(false);
        txtHora.setEditable(habilitar);
        txtPrecioHora.setEditable(false); // lo define el recurso
        txtSub.setEditable(false);
        txtTotal.setEditable(false);
        
        // DateChooser
        jDateChooserFecha.setEnabled(habilitar);

        // ComboBoxes
        jComboBoxEstado.setEnabled(habilitar);
        jComboBoxPromos.setEnabled(false);

        // Spinner
        jSpinnerHorasUsadas.setEnabled(habilitar);
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        lblIdVendedor = new javax.swing.JLabel();
        txtIdVendedor = new javax.swing.JTextField();
        lblIdTurista = new javax.swing.JLabel();
        lblIdAlquiler = new javax.swing.JLabel();
        txtIdAlquiler = new javax.swing.JTextField();
        txtIdTurista = new javax.swing.JTextField();
        lblNombreTuri = new javax.swing.JLabel();
        txtNombreTuri = new javax.swing.JTextField();
        btnBuscarTuri = new javax.swing.JButton();
        lblFecha = new javax.swing.JLabel();
        jDateChooserFecha = new com.toedter.calendar.JDateChooser();
        lblHora = new javax.swing.JLabel();
        txtHora = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblDetalles = new javax.swing.JTable();
        lblDuracion = new javax.swing.JLabel();
        txtDuracion = new javax.swing.JTextField();
        lblPromo = new javax.swing.JLabel();
        jComboBoxPromos = new javax.swing.JComboBox<>();
        lblEstado = new javax.swing.JLabel();
        jComboBoxEstado = new javax.swing.JComboBox<>();
        jSeparator1 = new javax.swing.JSeparator();
        lblRecurso = new javax.swing.JLabel();
        lblHorasUsadas = new javax.swing.JLabel();
        jSpinnerHorasUsadas = new javax.swing.JSpinner();
        lblPrecioHora = new javax.swing.JLabel();
        txtPrecioHora = new javax.swing.JTextField();
        lblSubtotal = new javax.swing.JLabel();
        txtSub = new javax.swing.JTextField();
        btnAgregarRe = new javax.swing.JButton();
        btnEliminarRe = new javax.swing.JButton();
        lblTotal = new javax.swing.JLabel();
        txtTotal = new javax.swing.JTextField();
        btnNuevo = new javax.swing.JButton();
        btnBuscarAlquiler = new javax.swing.JButton();
        btnEditar = new javax.swing.JButton();
        btnEliminarAlquiler = new javax.swing.JButton();
        btnGrabar = new javax.swing.JButton();
        btnVerAlquileres = new javax.swing.JButton();
        lblHoraFin = new javax.swing.JLabel();
        txtHoraFin = new javax.swing.JTextField();
        jComboBoxRecursos = new javax.swing.JComboBox<>();

        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setForeground(new java.awt.Color(0, 0, 0));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("BIENVENIDO A ALQUILER ");

        lblIdVendedor.setForeground(new java.awt.Color(0, 0, 0));
        lblIdVendedor.setText("Id Vendedor");

        txtIdVendedor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtIdVendedorActionPerformed(evt);
            }
        });

        lblIdTurista.setForeground(new java.awt.Color(0, 0, 0));
        lblIdTurista.setText("id Turista");

        lblIdAlquiler.setForeground(new java.awt.Color(0, 0, 0));
        lblIdAlquiler.setText("id Alquiler");

        lblNombreTuri.setForeground(new java.awt.Color(0, 0, 0));
        lblNombreTuri.setText("Nombre Turista");

        btnBuscarTuri.setText("buscar");
        btnBuscarTuri.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarTuriActionPerformed(evt);
            }
        });

        lblFecha.setForeground(new java.awt.Color(0, 0, 0));
        lblFecha.setText("Fecha ");

        lblHora.setForeground(new java.awt.Color(0, 0, 0));
        lblHora.setText("hora inicio");

        tblDetalles.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Id detalle", "re", "id recurso", "nombre", "precio hora", "horas", "sub"
            }
        ));
        jScrollPane1.setViewportView(tblDetalles);

        lblDuracion.setForeground(new java.awt.Color(0, 0, 0));
        lblDuracion.setText("Duracion");

        lblPromo.setForeground(new java.awt.Color(0, 0, 0));
        lblPromo.setText("Promocion");

        jComboBoxPromos.setForeground(new java.awt.Color(0, 0, 0));
        jComboBoxPromos.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "null", "Item 2", "Item 3", "Item 4" }));

        lblEstado.setForeground(new java.awt.Color(0, 0, 0));
        lblEstado.setText("Estado");

        jComboBoxEstado.setForeground(new java.awt.Color(0, 0, 0));
        jComboBoxEstado.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "reservado", "activo", "finalizado" }));
        jComboBoxEstado.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxEstadoActionPerformed(evt);
            }
        });

        lblRecurso.setForeground(new java.awt.Color(0, 0, 0));
        lblRecurso.setText("Recurso:");

        lblHorasUsadas.setForeground(new java.awt.Color(0, 0, 0));
        lblHorasUsadas.setText("horas usadas");

        lblPrecioHora.setForeground(new java.awt.Color(0, 0, 0));
        lblPrecioHora.setText("precio x hora");

        lblSubtotal.setForeground(new java.awt.Color(0, 0, 0));
        lblSubtotal.setText("subtotal");

        btnAgregarRe.setText("agregar");
        btnAgregarRe.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAgregarReActionPerformed(evt);
            }
        });

        btnEliminarRe.setText("eliminar");
        btnEliminarRe.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarReActionPerformed(evt);
            }
        });

        lblTotal.setForeground(new java.awt.Color(0, 0, 0));
        lblTotal.setText("Total");

        btnNuevo.setText("nuevo");
        btnNuevo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevoActionPerformed(evt);
            }
        });

        btnBuscarAlquiler.setText("buscar");
        btnBuscarAlquiler.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarAlquilerActionPerformed(evt);
            }
        });

        btnEditar.setText("editar");
        btnEditar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditarActionPerformed(evt);
            }
        });

        btnEliminarAlquiler.setText("eliminar");
        btnEliminarAlquiler.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarAlquilerActionPerformed(evt);
            }
        });

        btnGrabar.setText("grabar");
        btnGrabar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGrabarActionPerformed(evt);
            }
        });

        btnVerAlquileres.setText("ver Alquileres");
        btnVerAlquileres.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVerAlquileresActionPerformed(evt);
            }
        });

        lblHoraFin.setForeground(new java.awt.Color(0, 0, 0));
        lblHoraFin.setText("Hora Fin");

        jComboBoxRecursos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxRecursosActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addComponent(jSeparator1))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(40, 40, 40)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblIdAlquiler, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblIdTurista)
                                    .addComponent(lblFecha)
                                    .addComponent(lblDuracion)
                                    .addComponent(lblEstado))
                                .addGap(27, 27, 27)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txtIdAlquiler)
                                    .addComponent(txtIdTurista, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                                    .addComponent(jDateChooserFecha, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(txtDuracion)
                                    .addComponent(jComboBoxEstado, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(33, 33, 33)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(lblIdVendedor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(lblNombreTuri, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(lblHora, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addComponent(lblPromo)
                                    .addComponent(lblHoraFin))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txtIdVendedor)
                                    .addComponent(txtNombreTuri, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                                    .addComponent(txtHora)
                                    .addComponent(jComboBoxPromos, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(txtHoraFin))
                                .addGap(42, 42, 42)
                                .addComponent(btnBuscarTuri, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(27, 27, 27)
                                .addComponent(lblTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(txtTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnNuevo, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(11, 11, 11)
                                .addComponent(btnBuscarAlquiler, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnEditar, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnEliminarAlquiler, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnGrabar, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 14, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnAgregarRe, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(100, 100, 100)
                        .addComponent(btnEliminarRe, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnVerAlquileres, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(20, 20, 20))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblPrecioHora, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblRecurso))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(txtPrecioHora, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(24, 24, 24)
                                .addComponent(lblSubtotal)
                                .addGap(36, 36, 36)
                                .addComponent(txtSub, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(268, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(jComboBoxRecursos, javax.swing.GroupLayout.PREFERRED_SIZE, 314, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(30, 30, 30)
                                .addComponent(lblHorasUsadas)
                                .addGap(18, 18, 18)
                                .addComponent(jSpinnerHorasUsadas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(46, 46, 46))))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblIdVendedor)
                    .addComponent(txtIdVendedor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtIdAlquiler, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblIdAlquiler))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblIdTurista)
                    .addComponent(txtIdTurista, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblNombreTuri)
                    .addComponent(txtNombreTuri, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBuscarTuri))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblFecha)
                    .addComponent(jDateChooserFecha, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblHora, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txtHora, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDuracion)
                    .addComponent(txtDuracion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblPromo)
                    .addComponent(jComboBoxPromos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblEstado)
                        .addComponent(jComboBoxEstado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblHoraFin))
                    .addComponent(txtHoraFin, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblRecurso)
                    .addComponent(jComboBoxRecursos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblHorasUsadas)
                    .addComponent(jSpinnerHorasUsadas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtSub, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblSubtotal)
                    .addComponent(txtPrecioHora, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblPrecioHora))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(btnVerAlquileres)
                        .addGap(18, 18, 18))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnAgregarRe)
                            .addComponent(btnEliminarRe))
                        .addGap(8, 8, 8)))
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTotal)
                    .addComponent(txtTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnNuevo)
                    .addComponent(btnBuscarAlquiler)
                    .addComponent(btnEditar)
                    .addComponent(btnEliminarAlquiler)
                    .addComponent(btnGrabar))
                .addGap(69, 69, 69))
        );

        add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 640, 500));
    }// </editor-fold>//GEN-END:initComponents

    private void txtIdVendedorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtIdVendedorActionPerformed
      
    }//GEN-LAST:event_txtIdVendedorActionPerformed
    
    private void buscarTuristaPorDni(String dniBuscado) {
        if (dniBuscado.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Debe ingresar un DNI para buscar.");
            return; 
        }

        String sql = "SELECT * FROM Turista WHERE dni = ?";
        try (java.sql.PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, dniBuscado);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // 🔹 Rellenar los campos con los datos encontrados
                    txtIdTurista.setText(rs.getString("idTurista"));
                    txtNombreTuri.setText(rs.getString("nombre"));
                   
                    

                    javax.swing.JOptionPane.showMessageDialog(this, "Turista encontrado.");
                } else {
                    javax.swing.JOptionPane.showMessageDialog(this, "No se encontró ningún turista con ese DNI.");
                    limpiarCampos();
                }
            }
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Error al buscar turista: " + e.getMessage());
        }
    }
    
    private void btnBuscarTuriActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarTuriActionPerformed
        String dniBuscado = javax.swing.JOptionPane.showInputDialog(this, "Ingrese el DNI del turista a buscar:");
        if (dniBuscado != null) {
            buscarTuristaPorDni(dniBuscado.trim());
        }
    }//GEN-LAST:event_btnBuscarTuriActionPerformed

    private void btnNuevoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoActionPerformed

       limpiarCampos();
       modo="nuevo";
        habilitarCampos(true);
        btnEditar.setEnabled(false);
        btnEliminarAlquiler.setEnabled(false);
        cargarRecursos();
       
       
    }//GEN-LAST:event_btnNuevoActionPerformed

    


 
    private void btnAgregarReActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarReActionPerformed
        if (!txtHora.getText().isEmpty()) {

            try {
                // 1️⃣ Obtener el recurso seleccionado
                Recurso r = (Recurso) jComboBoxRecursos.getSelectedItem();
                if (r == null) {
                    JOptionPane.showMessageDialog(this, "Debe seleccionar un recurso.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // 🔍 1.5️⃣ VALIDAR si el recurso YA existe en la tabla
                DefaultTableModel modeloCheck = (DefaultTableModel) tblDetalles.getModel();
                for (int i = 0; i < modeloCheck.getRowCount(); i++) {
                    String idTabla = modeloCheck.getValueAt(i, 1).toString(); // columna 1 = idRecurso
                    if (idTabla.equals(r.getId())) {
                        JOptionPane.showMessageDialog(this,
                                "⚠️ Este recurso ya fue agregado al alquiler.\nSolo se permite una unidad por alquiler.",
                                "Recurso duplicado",
                                JOptionPane.WARNING_MESSAGE);
                        return; // 🔥 NO AGREGAR
                    }
                }

                // 2️⃣ Validar horas
                int horasUsadas = (int) jSpinnerHorasUsadas.getValue();
                if (horasUsadas <= 0) {
                    JOptionPane.showMessageDialog(this, "Debe ingresar un número de horas mayor a 0.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // 3️⃣ Validar que el recurso siga disponible
                String sqlCheck = "SELECT estado FROM Recursos WHERE idRecursos = ?";
                try (PreparedStatement ps = conexion.prepareStatement(sqlCheck)) {
                    ps.setString(1, r.getId());
                    ResultSet rs = ps.executeQuery();

                    if (rs.next() && !rs.getString("estado").equalsIgnoreCase("Disponible")) {
                        JOptionPane.showMessageDialog(this,
                                "⚠️ Este vehículo ya no está disponible.",
                                "No disponible",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }


                // 4️⃣ Calcular subtotal
                double precioHora = r.getTarifaHora();
                double subtotal = precioHora * horasUsadas;
                txtSub.setText(String.format("%.2f", subtotal));

                // 5️⃣ Agregar fila a la tabla
                DefaultTableModel modelo = (DefaultTableModel) tblDetalles.getModel();
                
                modelo.addRow(new Object[]{
                    "",
                    r, // 🔹 OBJETO Recurso (columna oculta)
                    r.getId(),
                    r.getTipo(),
                    precioHora,
                    horasUsadas,
                    subtotal
                });
                
             
                // 8️⃣ Quitar recurso del combo visualmente
                jComboBoxRecursos.removeItem(r);

                // 6️⃣ Actualizar cálculos del alquiler
                calcularDuracion();
                calcularTotal();
                aplicarPromocionAutomatica();

                // 7️⃣ Reset campos
                jComboBoxRecursos.setSelectedIndex(-1);
                txtPrecioHora.setText("");
                jSpinnerHorasUsadas.setValue(0);
                txtSub.setText("");

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al agregar detalle:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }

        } else {
            JOptionPane.showMessageDialog(null, "Ingrese la hora de inicio");
        }
    }//GEN-LAST:event_btnAgregarReActionPerformed

    private void btnEliminarReActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarReActionPerformed

        int filaSeleccionada = tblDetalles.getSelectedRow();

        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this,
                    "Debe seleccionar una fila del detalle para eliminar.",
                    "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirmar = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de eliminar este detalle?",
                "Confirmación",
                JOptionPane.YES_NO_OPTION);

        if (confirmar != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            DefaultTableModel modelo = (DefaultTableModel) tblDetalles.getModel();
            String idRecurso = modelo.getValueAt(filaSeleccionada, 2).toString();

            modelo.removeRow(filaSeleccionada);
            // 🔹 SOLO PARA EFECTO VISUAL
            

                Recurso r = obtenerRecursoPorId(idRecurso);

                    jComboBoxRecursos.addItem(r);
                
            

            // 🔹 Recalcular la duración como el valor MÁXIMO de horas en la tabla
            int nuevaDuracion = 0;
            for (int i = 0; i < modelo.getRowCount(); i++) {
                int horas = Integer.parseInt(modelo.getValueAt(i, 5).toString());
                nuevaDuracion = Math.max(nuevaDuracion, horas);
            }

            txtDuracion.setText(String.valueOf(nuevaDuracion));

            // 🔹 Recalcular total
            calcularTotal();

            // 🔹 Recalcular hora final basado en la nueva duración máxima
            String horaInicialTexto = txtHora.getText().trim();
            if (!horaInicialTexto.isEmpty()) {

                if (horaInicialTexto.contains(".")) {
                    horaInicialTexto = horaInicialTexto.substring(0, horaInicialTexto.indexOf("."));
                }

                DateTimeFormatter formato = horaInicialTexto.length() > 5
                        ? DateTimeFormatter.ofPattern("HH:mm:ss")
                        : DateTimeFormatter.ofPattern("HH:mm");

                LocalTime horaInicial = LocalTime.parse(horaInicialTexto, formato);
                LocalTime horaFinal = horaInicial.plusHours(nuevaDuracion);

                txtHoraFin.setText(
                        horaFinal.format(DateTimeFormatter.ofPattern("HH:mm"))
                );
            } else {
                txtHoraFin.setText("");
            }

            // 🔹 Reaplicar promoción automáticamente
            aplicarPromocionAutomatica();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al eliminar detalle: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }


    }//GEN-LAST:event_btnEliminarReActionPerformed

    private Recurso obtenerRecursoPorId(String idRecurso) throws SQLException {
        String sql = """
        SELECT idRecursos, tipo, descripcion, tarifaHora, estado, ubicacion
        FROM Recursos
        WHERE idRecursos = ?
    """;

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, idRecurso);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Recurso(
                        rs.getString("idRecursos"),
                        rs.getString("tipo"),
                        rs.getString("descripcion"),
                        rs.getDouble("tarifaHora"),
                        rs.getString("estado"),
                        rs.getString("ubicacion")
                );
            }
        }
        return null;
    }

    private void buscarAlquilerPorId(String idBuscar) {
        if (idBuscar == null || idBuscar.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar un ID de alquiler válido.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // 🔹 Buscar datos del alquiler principal
            String sqlAlquiler = """
            SELECT idAlquiler, idTurista, fechaInicio, horaInicio, Duracion, total, estado, idPromocion, idUsuario, horaFinal
            FROM Alquiler
            WHERE idAlquiler = ?
        """;

            PreparedStatement ps = conexion.prepareStatement(sqlAlquiler);
            ps.setString(1, idBuscar.trim());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // Rellenar los campos del formulario
                txtIdAlquiler.setText(rs.getString("idAlquiler"));
                txtIdTurista.setText(rs.getString("idTurista"));
                txtIdVendedor.setText(rs.getString("idUsuario"));
                txtDuracion.setText(String.valueOf(rs.getInt("Duracion")));
                txtHora.setText(rs.getString("horaInicio"));
                txtHoraFin.setText(rs.getString("horaFinal") != null ? rs.getString("horaFinal") : "");
                txtTotal.setText(String.format("%.2f", rs.getDouble("total")));
                jComboBoxEstado.setSelectedItem(rs.getString("estado"));

                java.sql.Date fecha = rs.getDate("fechaInicio");
                jDateChooserFecha.setDate(fecha);

                // Buscar nombre del turista
                PreparedStatement pst2 = conexion.prepareStatement("SELECT nombre FROM Turista WHERE idTurista = ?");
                pst2.setString(1, rs.getString("idTurista"));
                ResultSet rsT = pst2.executeQuery();
                if (rsT.next()) {
                    txtNombreTuri.setText(rsT.getString("nombre"));
                }

                // Cargar detalles del alquiler
                cargarDetallesAlquiler(idBuscar);

               

                JOptionPane.showMessageDialog(this, "✅ Alquiler encontrado con éxito.");
            } else {
                JOptionPane.showMessageDialog(this, "No se encontró ningún alquiler con ese ID.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al buscar alquiler: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarDetallesAlquiler(String idAlquiler) {
        DefaultTableModel modelo = (DefaultTableModel) tblDetalles.getModel();
        modelo.setRowCount(0); // limpiar la tabla antes de cargar

        try {
            String sql = """
            SELECT d.idDetalleAlquiler, d.idRecurso, r.tipo, r.tarifaHora, d.horasUsadas, d.subTotal
            FROM DetalleAlquiler d
            JOIN Recursos r ON d.idRecurso = r.idRecursos
            WHERE d.idAlquiler = ?
        """;
            PreparedStatement ps = conexion.prepareStatement(sql);
            ps.setString(1, idAlquiler);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                modelo.addRow(new Object[]{
                    rs.getString("idDetalleAlquiler"),
                    "",//r 
                    rs.getString("idRecurso"),
                    rs.getString("tipo"),
                    rs.getDouble("tarifaHora"),
                    rs.getInt("horasUsadas"),
                    rs.getDouble("subTotal")
                });
            }

            calcularTotal();
            aplicarPromocionAutomatica();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar los detalles: " + e.getMessage());
        }
    }

    
    private void btnBuscarAlquilerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarAlquilerActionPerformed
        String idBuscar = JOptionPane.showInputDialog(this, "Ingrese el ID del alquiler (Ej: A001):");
        if (idBuscar != null && !idBuscar.trim().isEmpty()) {
            buscarAlquilerPorId(idBuscar.trim());
            cargarRecursos();
            btnEditar.setEnabled(true);
            btnEliminarAlquiler.setEnabled(true);
        }
    }//GEN-LAST:event_btnBuscarAlquilerActionPerformed

    private void btnEditarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditarActionPerformed
        // 1️⃣ Verificar que haya un alquiler cargado
        if (txtIdAlquiler.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe buscar un alquiler antes de editar.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2️⃣ Verificar si el estado es finalizado
        String estado = jComboBoxEstado.getSelectedItem() != null
                ? jComboBoxEstado.getSelectedItem().toString().toLowerCase()
                : "";
        if (estado.equals("finalizado")) {
            JOptionPane.showMessageDialog(this,
                    "⚠️ Este alquiler está FINALIZADO y no puede editarse.",
                    "Acción no permitida",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 3️⃣ Cambiar modo
        modo = "edicion";

        // 4️⃣ Habilitar los campos necesarios
        habilitarCampos(true);

        

        // 5️⃣ Mostrar mensaje de confirmación
        JOptionPane.showMessageDialog(this,
                "Modo edición activado. Ahora puede modificar los detalles del alquiler.",
                "Modo edición", JOptionPane.INFORMATION_MESSAGE);
        
      

    }//GEN-LAST:event_btnEditarActionPerformed

    private void btnEliminarAlquilerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarAlquilerActionPerformed
        String idAlquiler = txtIdAlquiler.getText().trim();

        // 1️⃣ Verificar que haya un alquiler cargado
        if (idAlquiler.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Debe seleccionar o buscar un alquiler antes de eliminar.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2️⃣ Verificar si el usuario actual tiene permisos de administrador
        if (!esAdministrador(idUsuario)) {
            JOptionPane.showMessageDialog(this,
                    "❌ Solo los usuarios con rol ADMINISTRADOR pueden eliminar alquileres.",
                    "Acceso denegado", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 3️⃣ Confirmar eliminación
        int opcion = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de eliminar el alquiler " + idAlquiler + "?\n"
                + "Se eliminarán también todos sus detalles y se restaurará el stock de recursos.",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (opcion != JOptionPane.YES_OPTION) {
            return;
        }

        // 4️⃣ Ejecutar eliminación con devolución de stock
        try {
            conexion.setAutoCommit(false); // Iniciar transacción

           

            // 🟡 Luego eliminamos los detalles
            String sqlDetalles = "DELETE FROM DetalleAlquiler WHERE idAlquiler = ?";
            try (PreparedStatement psDet = conexion.prepareStatement(sqlDetalles)) {
                psDet.setString(1, idAlquiler);
                psDet.executeUpdate();
            }

            // 🔴 Finalmente eliminamos el alquiler principal
            String sqlAlquiler = "DELETE FROM Alquiler WHERE idAlquiler = ?";
            try (PreparedStatement psAlq = conexion.prepareStatement(sqlAlquiler)) {
                psAlq.setString(1, idAlquiler);
                psAlq.executeUpdate();
            }

            conexion.commit();

            JOptionPane.showMessageDialog(this,
                    "✅ Alquiler eliminado correctamente.\n"
                + "Los recursos quedaron disponibles.",
                "Eliminado", JOptionPane.INFORMATION_MESSAGE);

            // 5️⃣ Limpiar interfaz
            limpiarCampos();
            ((javax.swing.table.DefaultTableModel) tblDetalles.getModel()).setRowCount(0);
            habilitarCampos(false);

            btnEditar.setEnabled(false);
            btnEliminarAlquiler.setEnabled(false);
            btnGrabar.setEnabled(false);
            cargarRecursos();

        } catch (SQLException e) {
            try {
                conexion.rollback();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error al revertir cambios: " + ex.getMessage());
            }
            JOptionPane.showMessageDialog(this,
                    "Error al eliminar el alquiler: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                conexion.setAutoCommit(true);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error al restaurar conexión: " + ex.getMessage());
            }
        }
    }//GEN-LAST:event_btnEliminarAlquilerActionPerformed

    private void btnGrabarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGrabarActionPerformed
        if (modo == null || (!modo.equals("nuevo") && !modo.equals("edicion"))) {
            JOptionPane.showMessageDialog(this,
                    "Debe seleccionar primero 'Nuevo' o 'Editar' antes de grabar.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            conexion.setAutoCommit(false);

            // 🟢 1️⃣ Obtener datos del formulario
            String idAlquiler = txtIdAlquiler.getText().trim();
            String idTurista = txtIdTurista.getText().trim();
            String idUsuario = txtIdVendedor.getText().trim();
            java.sql.Date fechaInicio = new java.sql.Date(jDateChooserFecha.getDate().getTime());
            String horaInicio = txtHora.getText().trim();
            String horaFinal = txtHoraFin.getText().trim();
            int duracion = Integer.parseInt(txtDuracion.getText().isEmpty() ? "0" : txtDuracion.getText());
            double total = Double.parseDouble(txtTotal.getText().isEmpty() ? "0" : txtTotal.getText());
            String estado = jComboBoxEstado.getSelectedItem().toString();

            // 🧮 2️⃣ Determinar idPromocion
            String promoSeleccionada = jComboBoxPromos.getSelectedItem() != null
                    ? jComboBoxPromos.getSelectedItem().toString()
                    : "Ninguna";
            String idPromocion = null;

            if (!promoSeleccionada.equalsIgnoreCase("Ninguna")) {
                String sqlPromo = "SELECT idPromocion FROM Promocion WHERE descripcion LIKE ?";
                try (PreparedStatement psPromo = conexion.prepareStatement(sqlPromo)) {
                    psPromo.setString(1, "%" + promoSeleccionada.replace("%", "").trim() + "%");
                    try (ResultSet rsPromo = psPromo.executeQuery()) {
                        if (rsPromo.next()) {
                            idPromocion = rsPromo.getString("idPromocion");
                        }
                    }
                }
            }

            // 🧾 3️⃣ Si es NUEVO → registrar con SP
            if (modo.equals("nuevo")) {
                try (CallableStatement cs = conexion.prepareCall("{call registrarAlquiler(?,?,?,?,?,?,?,?,?)}")) {
                    cs.setString(1, idTurista);
                    cs.setDate(2, fechaInicio);
                    cs.setString(3, horaInicio);
                    cs.setInt(4, duracion);
                    cs.setBigDecimal(5, new java.math.BigDecimal(total));
                    cs.setString(6, estado);
                    cs.setString(7, idPromocion);
                    cs.setString(8, idUsuario);
                    cs.setString(9, horaFinal);
                    cs.execute();
                }
               
                // Recuperar el último ID generado
                try (PreparedStatement psUltimo = conexion.prepareStatement(
                        "SELECT TOP 1 idAlquiler FROM Alquiler ORDER BY idAlquiler DESC"); ResultSet rs = psUltimo.executeQuery()) {
                    if (rs.next()) {
                        idAlquiler = rs.getString(1);
                        txtIdAlquiler.setText(idAlquiler);
                    }
                }

                // Guardar los detalles 
                guardarSoloNuevosDetalles(idAlquiler);

                JOptionPane.showMessageDialog(this, "✅ Alquiler registrado correctamente.");
                cargarRecursos();//recargar para q solo aparezcan los disponibles
                // ✏️ 4️⃣ Si es EDICIÓN
            } else if (modo.equals("edicion")) {
               eliminarDetallesQuitados(idAlquiler, (DefaultTableModel) tblDetalles.getModel());

                guardarSoloNuevosDetalles(idAlquiler);
                cargarDetallesAlquiler(idAlquiler);
                // ✏️ Actualizar cabecera
                String sqlUpdate = """
                UPDATE Alquiler
                SET idTurista=?, fechaInicio=?, horaInicio=?, Duracion=?, total=?, estado=?, idPromocion=?, idUsuario=?, horaFinal=?
                WHERE idAlquiler=?
            """;
                try (PreparedStatement ps = conexion.prepareStatement(sqlUpdate)) {
                    ps.setString(1, idTurista);
                    ps.setDate(2, fechaInicio);
                    ps.setString(3, horaInicio);
                    ps.setInt(4, duracion);
                    ps.setDouble(5, total);
                    ps.setString(6, estado);
                    ps.setString(7, idPromocion);
                    ps.setString(8, idUsuario);
                    ps.setString(9, horaFinal);
                    ps.setString(10, idAlquiler);
                    ps.executeUpdate();
                }

                // 🧩 Insertar SOLO los detalles nuevos (sin duplicar ni tocar los existentes)
                
                
                JOptionPane.showMessageDialog(this, "✅ Alquiler actualizado correctamente.");
                 cargarRecursos();
            }

            // 🟢 Confirmar todo
            conexion.commit();
            modo = null;
            habilitarCampos(false);
           

        } catch (Exception e) {
            try {
                conexion.rollback();
            } catch (SQLException ex) {
            }
            JOptionPane.showMessageDialog(this, "❌ Error al grabar alquiler: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                conexion.setAutoCommit(true);
            } catch (SQLException ex) {
            }
        }
    }//GEN-LAST:event_btnGrabarActionPerformed

    private void btnVerAlquileresActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVerAlquileresActionPerformed
        VentanaAlquileres v= new VentanaAlquileres(conexion);
        v.setVisible(true);
    }//GEN-LAST:event_btnVerAlquileresActionPerformed

    private void jComboBoxRecursosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxRecursosActionPerformed
        try {
            // Si aún no se cargó o se limpió, no hacer nada
            if (jComboBoxRecursos.getSelectedIndex() == -1) {
                txtPrecioHora.setText("");
                return;
            }

            // 1️⃣ Obtener el recurso seleccionado
            Recurso r = (Recurso) jComboBoxRecursos.getSelectedItem();
            if (r == null) {
                return;
            }

            // 2️⃣ Mostrar precio por hora
            txtPrecioHora.setText(String.valueOf(r.getTarifaHora()));

            // 3️⃣ Recalcular subtotal automáticamente
            calcularSubtotalAutomatico();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar datos del recurso: " + e.getMessage());
        }
    }//GEN-LAST:event_jComboBoxRecursosActionPerformed

    private void jComboBoxEstadoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxEstadoActionPerformed
        if (modoLimpiar) {
            return; // ⛔ ignorar eventos provocados por limpiar
        }

        validarEstadoAlquiler();

    }//GEN-LAST:event_jComboBoxEstadoActionPerformed

    private void validarEstadoAlquiler() {
        Object item = jComboBoxEstado.getSelectedItem();

        if (item == null) {
            btnGrabar.setEnabled(false);
            return;
        }

        String estado = item.toString();

        if (txtIdAlquiler.getText().trim().isEmpty()
                && (estado.equalsIgnoreCase("FINALIZADO"))) {

            JOptionPane.showMessageDialog(this,
                    "No puede finalizar un alquiler que no ha sido grabado.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);

            btnGrabar.setEnabled(false);
            return;
        }

        btnGrabar.setEnabled(true);
    }

    
    private void calcularSubtotalAutomatico() {
        try {
            Recurso r = (Recurso) jComboBoxRecursos.getSelectedItem();
            if (r == null) {
                return;
            }

            int horas = (int) jSpinnerHorasUsadas.getValue();
            if (horas <= 0) {
                txtSub.setText("");
                return;
            }

            double subtotal = horas * r.getTarifaHora();
            txtSub.setText(String.format("%.2f", subtotal));

        } catch (Exception e) {
            // Evitamos errores si aún no hay datos
        }
    }


  
    private void guardarSoloNuevosDetalles(String idAlquiler) throws SQLException {
        DefaultTableModel modelo = (DefaultTableModel) tblDetalles.getModel();
        String sqlDetalle = "{call registrarDetalleAlquiler(?, ?, ?, ?)}";

        for (int i = 0; i < modelo.getRowCount(); i++) {
            String idDetalle = modelo.getValueAt(i, 0) != null
                    ? modelo.getValueAt(i, 0).toString().trim()
                    : "";

            // 🔹 Solo insertar si es nuevo
            if (idDetalle.isEmpty()) {
                String idRecurso = modelo.getValueAt(i, 2).toString();
                int horas = Integer.parseInt(modelo.getValueAt(i, 5).toString());
                double subtotal = Double.parseDouble(modelo.getValueAt(i, 6).toString());

                try (CallableStatement cs = conexion.prepareCall(sqlDetalle)) {
                    cs.setString(1, idAlquiler);
                    cs.setString(2, idRecurso);
                    cs.setInt(3, horas);
                    cs.setBigDecimal(4, BigDecimal.valueOf(subtotal));
                    cs.execute();
                }
            }
        }
    }

    
    private boolean esAdministrador(String idUsuario) {
        try {
            String sql = "SELECT n.nombreNivel FROM ActorUsuario u "
                    + "INNER JOIN Nivel n ON u.idNivel = n.idNivel "
                    + "WHERE u.idUsuario = ?";
            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                ps.setString(1, idUsuario);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String nivel = rs.getString("nombreNivel").toLowerCase();
                        return nivel.contains("administrador"); // puede ser "Administrador" o "Admin"
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error al verificar rol del usuario: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

 
    private void calcularTotal() {
        double total = 0.0;
        DefaultTableModel modelo = (DefaultTableModel) tblDetalles.getModel();

        for (int i = 0; i < modelo.getRowCount(); i++) {
            Object valor = modelo.getValueAt(i, 6); // columna subtotal
            if (valor != null) {
                total += Double.parseDouble(valor.toString());
            }
        }

        txtTotal.setText(String.format("%.2f", total));
        txtTotal.putClientProperty("totalBase", total); // 🔹 Reinicia el total base
    }

    private void eliminarDetallesQuitados(String idAlquiler, DefaultTableModel modeloActual) throws SQLException {
        // 1️⃣ Obtener IDs de los detalles actuales en la interfaz
        java.util.Set<String> detallesActuales = new java.util.HashSet<>();
        
        for (int i = 0; i < modeloActual.getRowCount(); i++) {
            
            Object idDetalleObj = modeloActual.getValueAt(i, 0);
            if (idDetalleObj != null && !idDetalleObj.toString().trim().isEmpty()) {
                detallesActuales.add(idDetalleObj.toString().trim());
                
            }
        }

        // 2️⃣ Buscar los detalles que existen en la BD
        String sql = "SELECT idDetalleAlquiler, idRecurso FROM DetalleAlquiler WHERE idAlquiler = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, idAlquiler);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String idDetalleBD = rs.getString("idDetalleAlquiler");
                String idRecursoBD = rs.getString("idRecurso");

                // 3️⃣ Si un detalle está en BD pero no en la tabla visual → eliminarlo
                if (!detallesActuales.contains(idDetalleBD)) {
                    try (PreparedStatement psDel = conexion.prepareStatement(
                            "DELETE FROM DetalleAlquiler WHERE idDetalleAlquiler = ?")) {
                        psDel.setString(1, idDetalleBD);
                        psDel.executeUpdate();
                    }

                    System.out.println("🗑️ Detalle eliminado de BD: " + idDetalleBD);
                }
            }
        }
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAgregarRe;
    private javax.swing.JButton btnBuscarAlquiler;
    private javax.swing.JButton btnBuscarTuri;
    private javax.swing.JButton btnEditar;
    private javax.swing.JButton btnEliminarAlquiler;
    private javax.swing.JButton btnEliminarRe;
    private javax.swing.JButton btnGrabar;
    private javax.swing.JButton btnNuevo;
    private javax.swing.JButton btnVerAlquileres;
    private javax.swing.JComboBox<String> jComboBoxEstado;
    private javax.swing.JComboBox<String> jComboBoxPromos;
    private javax.swing.JComboBox<Recurso> jComboBoxRecursos;
    private com.toedter.calendar.JDateChooser jDateChooserFecha;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSpinner jSpinnerHorasUsadas;
    private javax.swing.JLabel lblDuracion;
    private javax.swing.JLabel lblEstado;
    private javax.swing.JLabel lblFecha;
    private javax.swing.JLabel lblHora;
    private javax.swing.JLabel lblHoraFin;
    private javax.swing.JLabel lblHorasUsadas;
    private javax.swing.JLabel lblIdAlquiler;
    private javax.swing.JLabel lblIdTurista;
    private javax.swing.JLabel lblIdVendedor;
    private javax.swing.JLabel lblNombreTuri;
    private javax.swing.JLabel lblPrecioHora;
    private javax.swing.JLabel lblPromo;
    private javax.swing.JLabel lblRecurso;
    private javax.swing.JLabel lblSubtotal;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JTable tblDetalles;
    private javax.swing.JTextField txtDuracion;
    private javax.swing.JTextField txtHora;
    private javax.swing.JTextField txtHoraFin;
    private javax.swing.JTextField txtIdAlquiler;
    private javax.swing.JTextField txtIdTurista;
    private javax.swing.JTextField txtIdVendedor;
    private javax.swing.JTextField txtNombreTuri;
    private javax.swing.JTextField txtPrecioHora;
    private javax.swing.JTextField txtSub;
    private javax.swing.JTextField txtTotal;
    // End of variables declaration//GEN-END:variables
}
