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
        this.conexion = conexion;
        this.idUsuario = idUsuario;
        initComponents();
        // --- 1. DISEÑO DEGRADADO ---
        PanelDegradado fondo = new PanelDegradado();
        fondo.setLayout(new java.awt.BorderLayout());
        jPanel1.setOpaque(false);
        fondo.add(jPanel1, BorderLayout.CENTER);
        remove(jPanel1);
        add(fondo, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 640, 500));
        revalidate();
        repaint();
        
        ((javax.swing.JTextField) jDateChooserFecha.getDateEditor().getUiComponent()).setEditable(false);
        
         configurarEstiloVisual2();
         reestructurarLayout();
        // --- 2. CONFIGURACIÓN TABLA Y EVENTOS ---
        jSpinnerHorasUsadas.addChangeListener(e -> calcularSubtotalAutomatico());

        // Configurar modelo de tabla con las NUEVAS columnas
        tblDetalles.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {
                "Id detalle", "re", "id recurso", "nombre", "precio hora", "horas", "sub", "Estado", "Mora"
            }
        ));

        // Ocultar columnas internas
        ocultarColumna(0); // Id Detalle
        ocultarColumna(1); // Objeto Recurso

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

    // ---------------------------------------------------------
// VERSIÓN MEJORADA: USA LA COLUMNA NUMÉRICA 'porcentaje'
// ---------------------------------------------------------
    private void aplicarPromocionAutomatica() {
        try {
            // 1. Validación rápida: Si no hay duración, no hacemos nada
            String textoDuracion = txtDuracion.getText().trim();
            if (textoDuracion.isEmpty()) {
                return;
            }

            int duracion = Integer.parseInt(textoDuracion);

            // 2. Obtener el total base (Patrón State)
            // Esto evita que el descuento se aplique sobre un precio ya descontado
            double totalBase;
            if (txtTotal.getClientProperty("totalBase") == null) {
                String textoTotal = txtTotal.getText().trim().replace(",", "."); // Parche para comas
                if (textoTotal.isEmpty()) {
                    textoTotal = "0";
                }

                totalBase = Double.parseDouble(textoTotal);
                txtTotal.putClientProperty("totalBase", totalBase);
            } else {
                totalBase = (double) txtTotal.getClientProperty("totalBase");
            }

            double totalConDescuento = totalBase;
            String promoAplicada = "Ninguna";
            double mejorPorcentaje = 0.0;

            // 3. CONSULTA SEGURA: Traemos el número limpio desde la BD
            String sql = "SELECT descripcion, condicionHoras, porcentaje FROM Promocion";

            try (PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    // AQUÍ ESTÁ LA MAGIA: Leemos el double directo, sin parsear texto
                    double porcentajeBD = rs.getDouble("porcentaje");
                    int condicion = rs.getInt("condicionHoras");
                    String descBD = rs.getString("descripcion");

                    // Lógica: Si cumplo las horas Y este porcentaje es mejor que el anterior
                    if (duracion >= condicion) {
                        if (porcentajeBD > mejorPorcentaje) {
                            mejorPorcentaje = porcentajeBD;
                            promoAplicada = descBD; // Guardamos el nombre para el ComboBox
                        }
                    }
                }
            }

            // 4. Aplicar el descuento matemático
            if (mejorPorcentaje > 0) {
                // Fórmula: Total - (Total * (Porcentaje / 100))
                totalConDescuento = totalBase - (totalBase * mejorPorcentaje / 100.0);

                // Mensaje opcional (puedes comentarlo si es molesto)
                // JOptionPane.showMessageDialog(this,
                //        "✅ Se aplicó automáticamente " + mejorPorcentaje + "% de descuento.");
            } else {
                promoAplicada = "Ninguna"; // Si bajó las horas, quitamos la promo
            }

            // 5. Actualizar interfaz
            txtTotal.setText(String.format("%.2f", totalConDescuento).replace(",", "."));

            // Seleccionar en el ComboBox visualmente
            boolean encontrada = false;
            for (int i = 0; i < jComboBoxPromos.getItemCount(); i++) {
                // Usamos contains para ser flexibles con el texto
                if (jComboBoxPromos.getItemAt(i).toString().contains(promoAplicada)) {
                    jComboBoxPromos.setSelectedIndex(i);
                    encontrada = true;
                    break;
                }
            }
            if (!encontrada) {
                jComboBoxPromos.setSelectedIndex(0); // Index 0 suele ser "Ninguna"
            }
        } catch (NumberFormatException e) {
            System.out.println("Error numérico al calcular promo: " + e.getMessage());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error de BD en promo: " + e.getMessage());
        }
    }

    private void cargarPromociones() {
        jComboBoxPromos.removeAllItems();
        jComboBoxPromos.addItem("Ninguna");

        // Agregamos ORDER BY para que salgan ordenadas (3h, 5h, 10h...)
        String sql = "SELECT descripcion FROM Promocion ORDER BY condicionHoras ASC";

        try (PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
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

    

    
    private void habilitarCampos(boolean habilitar) {
        // 1. Botones básicos
        btnAgregarRe.setEnabled(habilitar);
        btnBuscarTuri.setEnabled(habilitar);

        // 2. ComboBoxes y Fechas
        jComboBoxRecursos.setEnabled(habilitar);
        jDateChooserFecha.setEnabled(habilitar);
        jComboBoxEstado.setEnabled(habilitar);
        jComboBoxPromos.setEnabled(false); // Promo suele ser auto, mejor bloqueado
        jSpinnerHorasUsadas.setEnabled(habilitar);

        // 3. Campos de Texto (Bloqueados siempre o según lógica)
        txtIdAlquiler.setEditable(false);
        txtHoraFin.setEditable(false);
        txtIdTurista.setEditable(false);
        txtIdVendedor.setEditable(false);
        txtNombreTuri.setEditable(false);
        txtDuracion.setEditable(false);
        txtPrecioHora.setEditable(false);
        txtSub.setEditable(false);
        txtTotal.setEditable(false);

        // La hora solo se edita si está habilitado
        txtHora.setEditable(habilitar);

        // 🔴 4. LÓGICA ESPECIAL DE BOTONES 🔴
        // ELIMINAR FILA: Se permite en 'Nuevo' (para corregir errores) 
        // y en 'Edición' (solo para cancelar ítems no guardados, aunque tu código ya protege los devueltos)
        btnEliminarRe.setEnabled(habilitar);

        // DEVOLVER ÍTEM: EXCLUSIVO de Modo Edición
        // Solo se activa si estamos habilitados Y el modo es "edicion"
        if (habilitar && "edicion".equals(modo)) {
            btnDevolverItem.setEnabled(true);
        } else {
            btnDevolverItem.setEnabled(false);
        }
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
        btnDevolverItem = new javax.swing.JButton();

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
        btnBuscarTuri.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnBuscarTuri.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarTuriActionPerformed(evt);
            }
        });

        lblFecha.setForeground(new java.awt.Color(0, 0, 0));
        lblFecha.setText("Fecha ");

        lblHora.setForeground(new java.awt.Color(0, 0, 0));
        lblHora.setText("hora inicio");

        txtHora.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtHoraActionPerformed(evt);
            }
        });

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

        txtDuracion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtDuracionActionPerformed(evt);
            }
        });

        lblPromo.setForeground(new java.awt.Color(0, 0, 0));
        lblPromo.setText("Promocion");

        jComboBoxPromos.setForeground(new java.awt.Color(0, 0, 0));
        jComboBoxPromos.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "null", "Item 2", "Item 3", "Item 4" }));
        jComboBoxPromos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxPromosActionPerformed(evt);
            }
        });

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
        btnAgregarRe.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAgregarRe.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAgregarReActionPerformed(evt);
            }
        });

        btnEliminarRe.setText("eliminar");
        btnEliminarRe.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
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
        btnVerAlquileres.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
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

        btnDevolverItem.setText("Devolver ");
        btnDevolverItem.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnDevolverItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDevolverItemActionPerformed(evt);
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
                        .addGap(27, 27, 27)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
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
                                .addComponent(lblTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(txtTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnNuevo, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(27, 27, 27)
                                .addComponent(btnBuscarAlquiler, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnEditar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnEliminarAlquiler, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnGrabar, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
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
                                .addGap(0, 15, Short.MAX_VALUE)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jComboBoxRecursos, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 314, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnDevolverItem, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(30, 30, 30)
                                .addComponent(lblHorasUsadas)
                                .addGap(18, 18, 18)
                                .addComponent(jSpinnerHorasUsadas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(46, 46, 46))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnAgregarRe, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(50, 50, 50)
                        .addComponent(btnEliminarRe, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnVerAlquileres, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(19, 19, 19))))
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
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAgregarRe)
                    .addComponent(btnEliminarRe)
                    .addComponent(btnDevolverItem)
                    .addComponent(btnVerAlquileres))
                .addGap(8, 8, 8)
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
        // 1️⃣ VALIDACIÓN DE HORA (Formato HH:mm)
        String horaInput = txtHora.getText().trim();

        // Regex: 00:00 a 23:59
        if (horaInput.isEmpty() || !horaInput.matches("^([01]\\d|2[0-3]):[0-5]\\d$")) {
            JOptionPane.showMessageDialog(this,
                    "Ingrese una hora de inicio válida (Formato 24h: HH:mm).\nEjemplo: 09:30 or 14:00",
                    "Hora inválida", JOptionPane.WARNING_MESSAGE);
            txtHora.requestFocus();
            return;
        }

        try {
            // 2️⃣ Obtener el recurso seleccionado
            Recurso r = (Recurso) jComboBoxRecursos.getSelectedItem();
            if (r == null) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar un recurso.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 3️⃣ Validar duplicados en la tabla
            DefaultTableModel modelo = (DefaultTableModel) tblDetalles.getModel();
            for (int i = 0; i < modelo.getRowCount(); i++) {
                String idTabla = modelo.getValueAt(i, 2).toString(); // Columna 2 = idRecurso (ajustado índice visual)
                if (idTabla.equals(r.getId())) {
                    JOptionPane.showMessageDialog(this, "⚠️ Este recurso ya está en la lista.", "Duplicado", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            // 4️⃣ Validar horas > 0
            int horasUsadas = (int) jSpinnerHorasUsadas.getValue();
            if (horasUsadas <= 0) {
                JOptionPane.showMessageDialog(this, "Las horas deben ser mayor a 0.");
                return;
            }

            // 5️⃣ Validar disponibilidad en BD (Doble check)
            String sqlCheck = "SELECT estado FROM Recursos WHERE idRecursos = ?";
            try (PreparedStatement ps = conexion.prepareStatement(sqlCheck)) {
                ps.setString(1, r.getId());
                ResultSet rs = ps.executeQuery();
                if (rs.next() && !rs.getString("estado").equalsIgnoreCase("Disponible")) {
                    JOptionPane.showMessageDialog(this, "⚠️ Este vehículo ya no está disponible (fue tomado por otro usuario).");
                    cargarRecursos(); // Refrescar combo
                    return;
                }
            }

            // 6️⃣ Cálculos
            double precioHora = r.getTarifaHora();
            double subtotal = precioHora * horasUsadas;
            txtSub.setText(String.format("%.2f", subtotal));

            // 7️⃣ AGREGAR FILA (CORREGIDO: 9 COLUMNAS)
            modelo.addRow(new Object[]{
                "", // 0: Id Detalle (vacío porque es nuevo)
                "", // 1: Objeto (vacío)
                r.getId(), // 2: ID Recurso
                r.getTipo(), // 3: Nombre/Tipo
                precioHora, // 4: Precio
                horasUsadas, // 5: Horas
                subtotal, // 6: Subtotal
                "En Uso", // 7: Estado Inicial (CORREGIDO)
                0.00 // 8: Mora Inicial (CORREGIDO)
            });

            // 8️⃣ Limpieza y Recálculo
            jComboBoxRecursos.removeItem(r);
            jComboBoxRecursos.setSelectedIndex(-1);
            txtPrecioHora.setText("");
            jSpinnerHorasUsadas.setValue(0);
            txtSub.setText("");

            recalcularTiemposGlobales();//calcularDuracion();
            calcularTotal();
            aplicarPromocionAutomatica();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }//GEN-LAST:event_btnAgregarReActionPerformed

    // Reemplaza tu antiguo calcularDuracion() con este método más robusto
    private void recalcularTiemposGlobales() {
        DefaultTableModel modelo = (DefaultTableModel) tblDetalles.getModel();
        int maxHoras = 0;

        // 1. Recorremos la tabla para buscar el valor MÁXIMO real de horas
        for (int i = 0; i < modelo.getRowCount(); i++) {
            Object valHoras = modelo.getValueAt(i, 5); // Columna 5 = Horas pactadas
            if (valHoras != null) {
                try {
                    int h = Integer.parseInt(valHoras.toString());
                    if (h > maxHoras) {
                        maxHoras = h;
                    }
                } catch (NumberFormatException e) {
                    // Ignorar valores no numéricos si los hubiera
                }
            }
        }

        // 2. Actualizamos la caja de texto con la duración real
        txtDuracion.setText(String.valueOf(maxHoras));

        // 3. Recalculamos la Hora Fin usando la Hora Inicio + MaxHoras
        String horaInicioTexto = txtHora.getText().trim();

        // Limpieza rápida del formato de hora (por si viene de SQL con .0000000)
        if (horaInicioTexto.length() > 5) {
            horaInicioTexto = horaInicioTexto.substring(0, 5);
        }

        if (!horaInicioTexto.isEmpty()) {
            try {
                java.time.LocalTime horaInicio = java.time.LocalTime.parse(horaInicioTexto);

                // Si hay duración, sumamos. Si es 0, la hora fin es igual a la inicio.
                java.time.LocalTime horaFin = horaInicio.plusHours(maxHoras);

                txtHoraFin.setText(horaFin.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));

            } catch (Exception e) {
                // Si la hora de inicio está mal formada, no calculamos el fin
                System.out.println("Error formato hora: " + e.getMessage());
            }
        }
    }
    
    private void btnEliminarReActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarReActionPerformed
        int filaSeleccionada = tblDetalles.getSelectedRow();

        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar una fila para eliminar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 1️⃣ VALIDACIÓN DE SEGURIDAD (Bloquear borrado de ítems devueltos)
        try {
            Object estadoObj = tblDetalles.getValueAt(filaSeleccionada, 7); // Columna 7 = Estado
            if (estadoObj != null && "Devuelto".equalsIgnoreCase(estadoObj.toString())) {
                JOptionPane.showMessageDialog(this,
                        "⛔ No puede eliminar un vehículo que ya fue DEVUELTO.\nEsto alteraría el historial.",
                        "Acción denegada", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (Exception e) {
        }

        // 2️⃣ Confirmación
        if (JOptionPane.showConfirmDialog(this, "¿Eliminar este detalle?", "Confirmar", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            DefaultTableModel modelo = (DefaultTableModel) tblDetalles.getModel();
            String idRecurso = modelo.getValueAt(filaSeleccionada, 2).toString(); // Columna 2 = ID Recurso

            // 3️⃣ Eliminar fila visualmente
            modelo.removeRow(filaSeleccionada);

            // 4️⃣ Restaurar al ComboBox (Visualmente)
            Recurso r = obtenerRecursoPorId(idRecurso);
            if (r != null) {
                jComboBoxRecursos.addItem(r);
            }

            // 5️⃣ RECALCULAR TODO AUTOMÁTICAMENTE 🚀
            // Aquí usamos el método centralizado. ¡Míra cuánto código te ahorras!
            recalcularTiemposGlobales();
            calcularTotal();
            aplicarPromocionAutomatica();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al eliminar: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
                
                // --- USO DEL FORMATEADOR AQUÍ ---
                txtHora.setText(formatearHora(rs.getString("horaInicio")));
                txtHoraFin.setText(formatearHora(rs.getString("horaFinal")));
                // --------------------------------
                
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
        modelo.setRowCount(0);

        try {
            // Consulta actualizada con las nuevas columnas
            String sql = """
                SELECT d.idDetalleAlquiler, d.idRecurso, r.tipo, r.tarifaHora, 
                       d.horasUsadas, d.subTotal, d.estadoDetalle, d.moraGenerada
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
                    "", // Placeholder para objeto Recurso
                    rs.getString("idRecurso"),
                    rs.getString("tipo"),
                    rs.getDouble("tarifaHora"),
                    rs.getInt("horasUsadas"),
                    rs.getDouble("subTotal"),
                    rs.getString("estadoDetalle"), // Nuevo: Estado
                    rs.getDouble("moraGenerada")   // Nuevo: Mora
                });
            }

            calcularTotal();
            aplicarPromocionAutomatica();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar detalles: " + e.getMessage());
        }
    }

    
    private void btnBuscarAlquilerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarAlquilerActionPerformed
        String idBuscar = JOptionPane.showInputDialog(this, "Ingrese el ID del alquiler (Ej: A001):");
        if (idBuscar != null && !idBuscar.trim().isEmpty()) {
            buscarAlquilerPorId(idBuscar.trim());
            cargarRecursos();
            btnEditar.setEnabled(true);
            btnEliminarAlquiler.setEnabled(true);
            habilitarCampos(false);
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
        // 1. Validar Modo
        if (modo == null || (!modo.equals("nuevo") && !modo.equals("edicion"))) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar primero 'Nuevo' o 'Editar'.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2. 🛡️ VALIDAR SELECCIONES OBLIGATORIAS
        if (jComboBoxEstado.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "⚠️ Debe seleccionar un ESTADO (ej. Reservado/Activo).", "Campo Requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (jDateChooserFecha.getDate() == null) {
            JOptionPane.showMessageDialog(this, "⚠️ Debe seleccionar una FECHA de inicio.", "Campo Requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Ahora es seguro leer el estado
        String estado = jComboBoxEstado.getSelectedItem().toString();

        // 3. VALIDACIÓN DE CIERRE: No cerrar si hay pendientes
        if (estado.equalsIgnoreCase("FINALIZADO")) {
            DefaultTableModel modelo = (DefaultTableModel) tblDetalles.getModel();
            boolean hayPendientes = false;

            for (int i = 0; i < modelo.getRowCount(); i++) {
                Object valorEstado = modelo.getValueAt(i, 7);
                String est = (valorEstado != null) ? valorEstado.toString() : "En Uso";

                if (!"Devuelto".equalsIgnoreCase(est)) {
                    hayPendientes = true;
                    break;
                }
            }

            if (hayPendientes) {
                JOptionPane.showMessageDialog(this,
                        "⛔ NO SE PUEDE FINALIZAR EL ALQUILER.\n\n"
                        + "Aún hay vehículos en estado 'En Uso'.\n"
                        + "Debe realizar la devolución de cada ítem primero.",
                        "Vehículos Pendientes", JOptionPane.ERROR_MESSAGE);
                return; // 🛑 Detiene el guardado
            }
        }

        // --------------------------------------------------------------------------------
        // 🧠 4. CÁLCULO DE DESGLOSE FINANCIERO (AUDITORÍA)
        // Recalculamos todo desde cero basándonos en la tabla para asegurar precisión.
        // --------------------------------------------------------------------------------
        double dbSubtotal = 0.0;
        double dbMora = 0.0;
        double dbDescuento = 0.0;
        double dbTotalFinal = 0.0;

        DefaultTableModel modeloCalc = (DefaultTableModel) tblDetalles.getModel();

        // A) Sumar Subtotal y Mora de la tabla
        for (int i = 0; i < modeloCalc.getRowCount(); i++) {
            // Columna 6: Subtotal del recurso (Precio x Horas)
            Object subVal = modeloCalc.getValueAt(i, 6);
            if (subVal != null) {
                try {
                    dbSubtotal += Double.parseDouble(subVal.toString().replace(",", "."));
                } catch (Exception e) {
                }
            }

            // Columna 8: Mora generada (si existe)
            Object moraVal = modeloCalc.getValueAt(i, 8);
            if (moraVal != null) {
                try {
                    dbMora += Double.parseDouble(moraVal.toString().replace(",", "."));
                } catch (Exception e) {
                }
            }
        }

        // B) Calcular Descuento
        // Obtenemos el porcentaje visualmente del ComboBox para saber cuál aplica
        String textoPromo = jComboBoxPromos.getSelectedItem() != null
                ? jComboBoxPromos.getSelectedItem().toString() : "Ninguna";

        // Buscar ID Promocion y calcular porcentaje
        String idPromocion = null;
        double porcentaje = 0.0;

        if (!textoPromo.equalsIgnoreCase("Ninguna")) {
            // 1. Obtener ID de BD
            try {
                String sqlPromo = "SELECT idPromocion, porcentaje FROM Promocion WHERE descripcion = ?";
                try (PreparedStatement psPromo = conexion.prepareStatement(sqlPromo)) {
                    psPromo.setString(1, textoPromo);
                    try (ResultSet rsPromo = psPromo.executeQuery()) {
                        if (rsPromo.next()) {
                            idPromocion = rsPromo.getString("idPromocion");
                            porcentaje = rsPromo.getDouble("porcentaje"); // Leemos el numérico 10.00
                        }
                    }
                }
            } catch (SQLException e) {
                System.out.println("Error buscando promo: " + e.getMessage());
            }
        }

        // C) Aplicar Fórmula: El descuento se aplica al SUBTOTAL, no a la MORA.
        if (porcentaje > 0) {
            dbDescuento = dbSubtotal * (porcentaje / 100.0);
        }

        // Redondeos a 2 decimales para evitar 10.999999999
        dbDescuento = Math.round(dbDescuento * 100.0) / 100.0;

        // D) TOTAL FINAL = (Subtotal - Descuento) + Mora
        dbTotalFinal = (dbSubtotal - dbDescuento) + dbMora;
        dbTotalFinal = Math.round(dbTotalFinal * 100.0) / 100.0;

        // --------------------------------------------------------------------------------
        try {
            conexion.setAutoCommit(false);

            // Datos Generales
            String idAlquiler = txtIdAlquiler.getText().trim();
            String idTurista = txtIdTurista.getText().trim();
            String idUsuario = txtIdVendedor.getText().trim();
            java.sql.Date fechaInicio = new java.sql.Date(jDateChooserFecha.getDate().getTime());
            String horaInicio = txtHora.getText().trim();
            String horaFinal = txtHoraFin.getText().trim();
            int duracion = 0;
            try {
                duracion = Integer.parseInt(txtDuracion.getText());
            } catch (Exception e) {
            }

            // ============================
            // 5️⃣ MODO NUEVO (INSERT)
            // ============================
            if (modo.equals("nuevo")) {
                // Nota: Se han agregado 2 interrogantes al final para subtotal y montoDescuento
                // Total parámetros: 11
                try (CallableStatement cs = conexion.prepareCall("{call registrarAlquiler(?,?,?,?,?,?,?,?,?,?,?)}")) {
                    cs.setString(1, idTurista);
                    cs.setDate(2, fechaInicio);
                    cs.setString(3, horaInicio);
                    cs.setInt(4, duracion);
                    cs.setBigDecimal(5, BigDecimal.valueOf(dbTotalFinal)); // TOTAL FINAL CALCULADO
                    cs.setString(6, estado);
                    cs.setString(7, idPromocion);
                    cs.setString(8, idUsuario);
                    cs.setString(9, horaFinal);

                    // NUEVOS CAMPOS FINANCIEROS
                    cs.setBigDecimal(10, BigDecimal.valueOf(dbSubtotal));
                    cs.setBigDecimal(11, BigDecimal.valueOf(dbDescuento));

                    cs.execute();
                }

                // Recuperar ID generado para guardar detalles
                try (PreparedStatement psUltimo = conexion.prepareStatement(
                        "SELECT TOP 1 idAlquiler FROM Alquiler ORDER BY idAlquiler DESC"); ResultSet rs = psUltimo.executeQuery()) {
                    if (rs.next()) {
                        idAlquiler = rs.getString(1);
                        txtIdAlquiler.setText(idAlquiler);
                    }
                }

                guardarSoloNuevosDetalles(idAlquiler);
                JOptionPane.showMessageDialog(this, "✅ Alquiler registrado correctamente.");
                cargarRecursos();

                // ============================
                // 6️⃣ MODO EDICIÓN (UPDATE)
                // ============================
            } else if (modo.equals("edicion")) {
                eliminarDetallesQuitados(idAlquiler, (DefaultTableModel) tblDetalles.getModel());
                guardarSoloNuevosDetalles(idAlquiler);

                String sqlUpdate = """
                UPDATE Alquiler
                SET idTurista=?, fechaInicio=?, horaInicio=?, Duracion=?, 
                    total=?, estado=?, idPromocion=?, idUsuario=?, horaFinal=?,
                    horaFinalReal = CASE WHEN ? = 'FINALIZADO' THEN CAST(GETDATE() AS TIME) ELSE horaFinalReal END,
                    mora = ?,
                    subtotal = ?,
                    montoDescuento = ?
                WHERE idAlquiler=?
            """;

                try (PreparedStatement ps = conexion.prepareStatement(sqlUpdate)) {
                    ps.setString(1, idTurista);
                    ps.setDate(2, fechaInicio);
                    ps.setString(3, horaInicio);
                    ps.setInt(4, duracion);

                    // Si está finalizado usamos el cálculo total con moras, sino el subtotal con descuento
                    // (Aunque dbTotalFinal ya incluye la lógica correcta para ambos casos)
                    ps.setDouble(5, dbTotalFinal);

                    ps.setString(6, estado);
                    ps.setString(7, idPromocion);
                    ps.setString(8, idUsuario);
                    ps.setString(9, horaFinal);
                    ps.setString(10, estado); // Para el CASE del horaFinalReal
                    ps.setDouble(11, dbMora);

                    // NUEVOS CAMPOS
                    ps.setBigDecimal(12, BigDecimal.valueOf(dbSubtotal));
                    ps.setBigDecimal(13, BigDecimal.valueOf(dbDescuento));

                    ps.setString(14, idAlquiler);
                    ps.executeUpdate();
                }

                if (estado.equalsIgnoreCase("FINALIZADO")) {
                    JOptionPane.showMessageDialog(this,
                            "✅ Alquiler FINALIZADO.\n\n"
                            + "Subtotal:    S/ " + String.format("%.2f", dbSubtotal) + "\n"
                            + "Descuento: - S/ " + String.format("%.2f", dbDescuento) + "\n"
                            + "Mora:      + S/ " + String.format("%.2f", dbMora) + "\n"
                            + "--------------------------\n"
                            + "TOTAL PAGADO: S/ " + String.format("%.2f", dbTotalFinal));
                } else {
                    JOptionPane.showMessageDialog(this, "✅ Alquiler actualizado correctamente.");
                }

                cargarRecursos();
                cargarDetallesAlquiler(idAlquiler);
            }

            conexion.commit();
            modo = null;
            habilitarCampos(false);

        } catch (Exception e) {
            try {
                conexion.rollback();
            } catch (SQLException ex) {
            }
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "❌ Error al grabar: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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

    private void txtHoraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtHoraActionPerformed
       
    }//GEN-LAST:event_txtHoraActionPerformed

    private void btnDevolverItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDevolverItemActionPerformed
       
        int fila = tblDetalles.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un vehículo de la tabla para devolver.");
            return;
        }

        // Validar si ya está devuelto
        String estadoActual = tblDetalles.getValueAt(fila, 7) != null ? tblDetalles.getValueAt(fila, 7).toString() : "En Uso";
        if ("Devuelto".equalsIgnoreCase(estadoActual)) {
            JOptionPane.showMessageDialog(this, "Este vehículo ya fue devuelto.");
            return;
        }

        String idDetalle = tblDetalles.getValueAt(fila, 0).toString();
        String idRecurso = tblDetalles.getValueAt(fila, 2).toString();
        double tarifa = Double.parseDouble(tblDetalles.getValueAt(fila, 4).toString());
        int horasPactadas = Integer.parseInt(tblDetalles.getValueAt(fila, 5).toString());

        // Validar hora inicio
        if (txtHora.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay hora de inicio registrada.");
            return;
        }
        LocalTime horaInicio = LocalTime.parse(txtHora.getText());

        // Calcular Mora
        double mora = calcularMoraIndividual(horasPactadas, horaInicio, tarifa);
        String mensajeMora = (mora > 0) ? "\n⚠️ SE APLICARÁ MORA DE: S/ " + mora : "\n✅ Entrega a tiempo. Sin mora.";

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Confirmar devolución del vehículo " + idRecurso + "?" + mensajeMora,
                "Devolución de Recurso", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Actualizar BD: El trigger se encargará de liberar el recurso
                String sql = "UPDATE DetalleAlquiler SET estadoDetalle = 'Devuelto', horaDevolucionReal = ?, moraGenerada = ? WHERE idDetalleAlquiler = ?";
                PreparedStatement ps = conexion.prepareStatement(sql);
                ps.setString(1, LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                ps.setDouble(2, mora);
                ps.setString(3, idDetalle);
                
                if (ps.executeUpdate() > 0) {
                    JOptionPane.showMessageDialog(this, "Vehículo devuelto exitosamente.");
                    cargarDetallesAlquiler(txtIdAlquiler.getText()); // Recargar tabla
                    cargarRecursos(); // Refrescar combo de disponibles
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error al devolver: " + e.getMessage());
            }
        }
    }//GEN-LAST:event_btnDevolverItemActionPerformed

    private void txtDuracionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDuracionActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtDuracionActionPerformed

    private void jComboBoxPromosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxPromosActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBoxPromosActionPerformed

    private double calcularMoraIndividual(int horasPactadas, LocalTime horaInicio, double tarifaHora) {
        // 1. Definir fechas y horas (Usamos hoy como referencia según tu lógica actual)
        java.time.LocalDate hoy = java.time.LocalDate.now();
        java.time.LocalDateTime inicio = java.time.LocalDateTime.of(hoy, horaInicio);
        java.time.LocalDateTime limite = inicio.plusHours(horasPactadas);
        java.time.LocalDateTime ahora = java.time.LocalDateTime.now();

        // 2. Definir el límite REAL con la tolerancia incluida
        java.time.LocalDateTime limiteConTolerancia = limite.plusMinutes(10);

        // 3. Si todavía estamos dentro de la tolerancia (o antes), no hay mora
        if (ahora.isBefore(limiteConTolerancia)) {
            return 0.0;
        }

        // 4. CÁLCULO DEL TIEMPO EXCEDIDO (LA CORRECCIÓN ESTÁ AQUÍ)
        // Calculamos la diferencia entre "Ahora" y el "Límite + 10 min"
        // De esta forma, los primeros 10 minutos nunca se cobran.
        long minutosRetraso = java.time.temporal.ChronoUnit.MINUTES.between(limiteConTolerancia, ahora);

        if (minutosRetraso <= 0) {
            return 0.0;
        }

        // 5. Cálculo monetario
        double horasExcedidas = minutosRetraso / 60.0;
        double penalidadFactor = 2.0; // Sigues cobrando el doble, pero solo por el tiempo "extra-extra"
        
        double montoMora = horasExcedidas * tarifaHora * penalidadFactor;

        return Math.round(montoMora * 100.0) / 100.0;

    }
    
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
    // Método para limpiar la hora de SQL Server (quita los segundos y milisegundos)

    private String formatearHora(String horaSql) {
        if (horaSql == null || horaSql.trim().isEmpty()) {
            return "";
        }
        // Si la cadena es larga (ej: 19:00:00.0000), cortamos los primeros 5 caracteres
        if (horaSql.length() >= 5) {
            return horaSql.substring(0, 5);
        }
        return horaSql;
    }

   

    
    
    
   private void configurarEstiloVisual2() {
        // --- NUEVO: COLORES DE TEXTO Y TÍTULO ---
        java.awt.Color colorTexto = new java.awt.Color(31, 78, 95); // Azul Petróleo
        java.awt.Font fuenteTitulo = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 16);
        java.awt.Font fuenteLabels = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14);

        // 1. TÍTULO
        jLabel1.setFont(fuenteTitulo);
        jLabel1.setForeground(colorTexto);
        jLabel1.setText("GESTIÓN DE ALQUILERES"); 
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        // 2. ETIQUETAS (LABELS) - Aplicar color a todas
        javax.swing.JLabel[] labels = {
            lblIdAlquiler, lblIdTurista, lblFecha, lblDuracion, lblEstado,
            lblIdVendedor, lblNombreTuri, lblHora, lblPromo, lblHoraFin,
            lblRecurso, lblHorasUsadas, lblPrecioHora, lblSubtotal, lblTotal
        };

        for (javax.swing.JLabel lbl : labels) {
            lbl.setFont(fuenteLabels);
            lbl.setForeground(colorTexto);
        }
        // ------------------------------------------------------

        // 3. ESTILO TABLA (Tu código original)
        tblDetalles.setRowHeight(22); 
        tblDetalles.getTableHeader().setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        tblDetalles.getTableHeader().setBackground(new java.awt.Color(240, 248, 255));
        tblDetalles.getTableHeader().setOpaque(true);
        tblDetalles.setShowVerticalLines(false);

        // 4. TEXTOS DE BOTONES
        btnNuevo.setText("✨ Nuevo");
        btnEliminarAlquiler.setText("🗑️ Eliminar");
        btnVerAlquileres.setText("📋 Historial");

        // SOLO ÍCONOS 
        btnBuscarAlquiler.setText("🔍");
        btnEditar.setText("✏️");
        btnGrabar.setText("💾");
        btnBuscarTuri.setText("🔍");

        // Gestión
        btnAgregarRe.setText("➕ Agregar");
        btnEliminarRe.setText("❌ Quitar");
        btnDevolverItem.setText("🗝️ Devolver");

        // 5. ESTILIZADO DE BOTONES (Tu código original)
        javax.swing.JButton[] botonesAccion = {
            btnNuevo, btnBuscarAlquiler, btnEditar, btnEliminarAlquiler,
            btnGrabar, btnVerAlquileres, btnAgregarRe, btnEliminarRe,
            btnDevolverItem, btnBuscarTuri
        };

        for (javax.swing.JButton btn : botonesAccion) {
            btn.setBackground(new java.awt.Color(255, 255, 255));
            btn.setForeground(new java.awt.Color(0, 51, 102));
            btn.setFont(new java.awt.Font("Segoe UI Emoji", java.awt.Font.BOLD, 12));

            btn.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)),
                    javax.swing.BorderFactory.createEmptyBorder(2, 5, 2, 5)
            ));

            btn.setFocusPainted(false);
            btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }

        // 6. COLORES ESPECÍFICOS
        btnGrabar.setBackground(new java.awt.Color(0, 102, 204));
        btnGrabar.setForeground(java.awt.Color.WHITE);

        btnAgregarRe.setBackground(new java.awt.Color(40, 167, 69));
        btnAgregarRe.setForeground(java.awt.Color.WHITE);

        btnEliminarRe.setBackground(new java.awt.Color(220, 53, 69));
        btnEliminarRe.setForeground(java.awt.Color.WHITE);

        btnDevolverItem.setBackground(new java.awt.Color(255, 193, 7));
        btnDevolverItem.setForeground(java.awt.Color.BLACK);

        btnBuscarTuri.setBackground(new java.awt.Color(230, 240, 255));

        // 7. CAMPOS DE TEXTO
        javax.swing.JTextField[] campos = {
            txtIdAlquiler, txtIdTurista, txtIdVendedor, txtNombreTuri,
            txtHora, txtHoraFin, txtDuracion, txtPrecioHora, txtSub
        };
        for (javax.swing.JTextField txt : campos) {
            txt.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)),
                    javax.swing.BorderFactory.createEmptyBorder(2, 4, 2, 4)
            ));
        }

        // Total
        txtTotal.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        txtTotal.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    }
    
    private void reestructurarLayout() {
    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(jPanel1);
    jPanel1.setLayout(layout);

    layout.setAutoCreateGaps(false); 
    layout.setAutoCreateContainerGaps(false);

    // -----------------------------------------------------------
    // HORIZONTAL (Eje X)
    // -----------------------------------------------------------
    layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        // Título
        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        
        // CUERPO PRINCIPAL
        .addGroup(layout.createSequentialGroup()
            .addGap(25) // Margen Izquierdo General
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                
                // 1. BLOQUE DE DATOS (ARRIBA)
                .addGroup(layout.createSequentialGroup()
                    // --- Columna Izquierda ---
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(lblIdAlquiler).addComponent(lblIdTurista).addComponent(lblFecha).addComponent(lblDuracion).addComponent(lblEstado))
                    .addGap(15)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(txtIdAlquiler, 100, 100, 100).addComponent(txtIdTurista, 100, 100, 100).addComponent(jDateChooserFecha, 100, 100, 100).addComponent(txtDuracion, 100, 100, 100).addComponent(jComboBoxEstado, 100, 100, 100))
                    
                    // --- ESPACIO FLEXIBLE EN EL MEDIO (El "Muelle") ---
                    // Esto separa las columnas para que no se vean pegadas
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    
                    // --- Columna Derecha ---
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(lblIdVendedor).addComponent(lblNombreTuri).addComponent(lblHora).addComponent(lblPromo).addComponent(lblHoraFin))
                    .addGap(15)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(txtIdVendedor, 100, 100, 100).addComponent(txtNombreTuri, 100, 100, 100).addComponent(txtHora, 100, 100, 100).addComponent(jComboBoxPromos, 100, 100, 100).addComponent(txtHoraFin, 100, 100, 100))
                    .addGap(10)
                    .addComponent(btnBuscarTuri, 30, 30, 30) // Lupa
                )
                
                .addComponent(jSeparator1)

                // 2. GESTIÓN RECURSOS (MEDIO)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(lblRecurso).addGap(10).addComponent(jComboBoxRecursos, 0, 260, Short.MAX_VALUE).addGap(20)
                    .addComponent(lblHorasUsadas).addGap(10).addComponent(jSpinnerHorasUsadas, 50, 50, 50))
                
                .addGroup(layout.createSequentialGroup()
                    .addComponent(lblPrecioHora).addGap(10).addComponent(txtPrecioHora, 70, 70, 70)
                    .addGap(30).addComponent(lblSubtotal).addGap(10).addComponent(txtSub, 90, 90, 90)
                )
                
                .addGroup(layout.createSequentialGroup()
                    .addComponent(btnAgregarRe, 90, 90, 90).addGap(10)
                    .addComponent(btnEliminarRe, 90, 90, 90).addGap(10)
                    .addComponent(btnDevolverItem, 100, 100, 100).addGap(20)
                    .addComponent(btnVerAlquileres, 120, 120, 120))
                
                .addComponent(jScrollPane1)

                // 3. BLOQUE INFERIOR (BOTONES)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(lblTotal).addGap(10).addComponent(txtTotal, 100, 100, 100)
                    
                    // --- MUELLE INFERIOR ---
                    // Esto empuja los botones A LA DERECHA
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    
                    .addComponent(btnNuevo, 90, 90, 90).addGap(5)
                    .addComponent(btnBuscarAlquiler, 40, 40, 40).addGap(5)
                    .addComponent(btnEditar, 40, 40, 40).addGap(5)
                    .addComponent(btnEliminarAlquiler, 90, 90, 90).addGap(5)
                    .addComponent(btnGrabar, 45, 45, 45)
                )
            )
            .addGap(25) // Margen Derecho General
        )
    );

    // -----------------------------------------------------------
    // VERTICAL (Eje Y)
    // -----------------------------------------------------------
    layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
            .addGap(10)
            .addComponent(jLabel1)
            .addGap(20)
            
            // Fila 1
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER).addComponent(lblIdAlquiler).addComponent(txtIdAlquiler, 25, 25, 25).addComponent(lblIdVendedor).addComponent(txtIdVendedor, 25, 25, 25))
            .addGap(8)
            // Fila 2
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER).addComponent(lblIdTurista).addComponent(txtIdTurista, 25, 25, 25).addComponent(lblNombreTuri).addComponent(txtNombreTuri, 25, 25, 25).addComponent(btnBuscarTuri, 25, 25, 25))
            .addGap(8)
            // Fila 3
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER).addComponent(lblFecha).addComponent(jDateChooserFecha, 25, 25, 25).addComponent(lblHora).addComponent(txtHora, 25, 25, 25))
            .addGap(8)
            // Fila 4
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER).addComponent(lblDuracion).addComponent(txtDuracion, 25, 25, 25).addComponent(lblPromo).addComponent(jComboBoxPromos, 25, 25, 25))
            .addGap(8)
            // Fila 5
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER).addComponent(lblEstado).addComponent(jComboBoxEstado, 25, 25, 25).addComponent(lblHoraFin).addComponent(txtHoraFin, 25, 25, 25))
            
            .addGap(15).addComponent(jSeparator1, 10, 10, 10).addGap(15)
            
            // Recursos
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER).addComponent(lblRecurso).addComponent(jComboBoxRecursos, 28, 28, 28).addComponent(lblHorasUsadas).addComponent(jSpinnerHorasUsadas, 28, 28, 28))
            .addGap(10)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER).addComponent(lblPrecioHora).addComponent(txtPrecioHora, 25, 25, 25).addComponent(lblSubtotal).addComponent(txtSub, 25, 25, 25))
            .addGap(15)
            
            // Botones Gestión
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(btnAgregarRe, 30, 30, 30).addComponent(btnEliminarRe, 30, 30, 30).addComponent(btnDevolverItem, 30, 30, 30).addComponent(btnVerAlquileres, 30, 30, 30))
            
            .addGap(10)
            .addComponent(jScrollPane1, 80,80, 80)
            .addGap(15)
            
            // Botones Inferiores (30px altura)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(lblTotal).addComponent(txtTotal, 30, 30, 30)
                .addComponent(btnNuevo, 30, 30, 30)
                .addComponent(btnBuscarAlquiler, 30, 30, 30)
                .addComponent(btnEditar, 30, 30, 30)
                .addComponent(btnEliminarAlquiler, 30, 30, 30)
                .addComponent(btnGrabar, 30, 30, 30)
            )
            .addGap(20)
        )
    );
}
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAgregarRe;
    private javax.swing.JButton btnBuscarAlquiler;
    private javax.swing.JButton btnBuscarTuri;
    private javax.swing.JButton btnDevolverItem;
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
