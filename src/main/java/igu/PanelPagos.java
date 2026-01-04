/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package igu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Component;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.JTextField;
 
public class PanelPagos extends javax.swing.JPanel {

    private Connection conexion;
    private String modo;
    private String idUsuario;

    /**
     * Creates new form PanelPagos
     */
    public PanelPagos(Connection conexion, String idUsuario) {
        initComponents();
        this.conexion = conexion;
        this.idUsuario = idUsuario;
      // --- 1. INTEGRACIÓN DEL FONDO DEGRADADO ---
        PanelDegradado fondo = new PanelDegradado();
        fondo.setLayout(new java.awt.BorderLayout());

        // Hacemos transparente el panel de NetBeans
        jPanel1.setOpaque(false);
        
        // Metemos el panel de controles DENTRO del degradado
        fondo.add(jPanel1, java.awt.BorderLayout.CENTER);

        // Reemplazamos el contenido principal
        this.setLayout(new java.awt.BorderLayout());
        this.removeAll();
        this.add(fondo, java.awt.BorderLayout.CENTER);

        this.revalidate();
        this.repaint();

        ((javax.swing.JTextField) jDateChooserFechaPago.getDateEditor().getUiComponent()).setEditable(false);
        // --- 2. APLICAR ESTILOS VISUALES (LLAMADA AL NUEVO MÉTODO) ---
        aplicarEstilosModernos(); 
        corregirDistribucion();

        // --- 3. CONFIGURACIÓN LÓGICA ---
        habilitarCampos(false);
        txtIgv.setEditable(false);
        txtIgv.setText("0.00");

        txtMontoSinIgv.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                calcularMontos();
            }
        });

        cargarUltimoAlquiler();
    }
    
  
    
    // --- LÓGICA DE NEGOCIO (Validada) ---

    private void cargarUltimoAlquiler() {
        String sql = """
            SELECT TOP 1 a.idAlquiler, t.dni, a.total
            FROM Alquiler a
            JOIN Turista t ON a.idTurista = t.idTurista
            WHERE a.idUsuario = ?
              AND NOT EXISTS (SELECT 1 FROM Pago p WHERE p.idAlquiler = a.idAlquiler AND p.estado != 'anulado')
            ORDER BY a.fechaInicio DESC
        """;

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, idUsuario);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                txtIdAlquiler.setText(rs.getString("idAlquiler"));
                txtDniTuri.setText(rs.getString("dni"));
                txtMontoTotal.setText(rs.getBigDecimal("total").toString());
                
                calcularMontos(); 

                JcmbMetodoPago.setSelectedIndex(0);
                JcmbEstadoPago.setSelectedItem("Pendiente");
                
                modo = "nuevo";
                habilitarCampos(true);
                JOptionPane.showMessageDialog(this, 
                    "Se cargó el último alquiler pendiente de pago.", 
                    "Información", JOptionPane.INFORMATION_MESSAGE);
            } 
        } catch (SQLException e) {
            System.err.println("Error carga inicial: " + e.getMessage());
        }
    }

    private void calcularMontos() {
        try {
            // 1. CAMBIO CLAVE: Ahora leemos el TOTAL, no el "Sin IGV".
            // Asumo que el usuario escribe o el sistema carga el dato en 'txtMontoTotal'
            String textoTotal = txtMontoTotal.getText().trim().replace(",", ".");

            if (textoTotal.isEmpty()) {
                txtMontoSinIgv.setText("0.00");
                txtIgv.setText("0.00");
                return;
            }

            BigDecimal total = new BigDecimal(textoTotal);

            // 2. LÓGICA INVERSA: Dividimos entre 1.18 para sacar la base
            // Usamos RoundingMode.HALF_UP para redondear correctamente a 2 decimales
            BigDecimal baseImponible = total.divide(new BigDecimal("1.18"), 2, java.math.RoundingMode.HALF_UP);

            // 3. Calculamos el IGV restando (Total - Base)
            BigDecimal igv = total.subtract(baseImponible);

            // 4. Mostramos los resultados desglosados
            txtMontoSinIgv.setText(baseImponible.toString());
            txtIgv.setText(igv.toString());

        } catch (NumberFormatException e) {
            // Ignoramos errores mientras escribe
        } catch (ArithmeticException e) {
            // Por si acaso ocurra error de división (aunque con el rounding mode no debería)
        }
    }

    private void limpiarCampos() {
        txtIdPago.setText("");
        txtIdAlquiler.setText("");
        txtDniTuri.setText("");
        jDateChooserFechaPago.setDate(null);
        txtMontoSinIgv.setText("");
        txtIgv.setText("");
        txtMontoTotal.setText("");
        JcmbMetodoPago.setSelectedIndex(0);
        JcmbEstadoPago.setSelectedIndex(0);
    }

    private void habilitarCampos(boolean estado) {
        txtIdAlquiler.setEditable(false);
        txtIdPago.setEditable(false);
        txtDniTuri.setEditable(false);
        txtMontoTotal.setEditable(false);
        txtIgv.setEditable(false);
        txtMontoSinIgv.setEditable(false); 
        
        btnBuscarAlquiller.setEnabled(estado);
        jDateChooserFechaPago.setEnabled(estado);
        JcmbMetodoPago.setEnabled(estado);
        JcmbEstadoPago.setEnabled(false); 
    }
    
    private void seleccionarItemIgnoreCase(JComboBox<String> combo, String valorBD) {
        if (valorBD == null) return;
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).equalsIgnoreCase(valorBD)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
        combo.setSelectedIndex(0);
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
        jLabel2 = new javax.swing.JLabel();
        txtIdAlquiler = new javax.swing.JTextField();
        btnBuscarAlquiller = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        txtDniTuri = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtMontoTotal = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jDateChooserFechaPago = new com.toedter.calendar.JDateChooser();
        jLabel6 = new javax.swing.JLabel();
        JcmbMetodoPago = new javax.swing.JComboBox<>();
        jLabel7 = new javax.swing.JLabel();
        txtIdPago = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        JcmbEstadoPago = new javax.swing.JComboBox<>();
        jSeparator1 = new javax.swing.JSeparator();
        btnNuevo = new javax.swing.JButton();
        btnBuscar = new javax.swing.JButton();
        btnEditar = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        btnGrabar = new javax.swing.JButton();
        btnPagar = new javax.swing.JButton();
        btnAnular = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        txtMontoSinIgv = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        txtIgv = new javax.swing.JTextField();

        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setForeground(new java.awt.Color(0, 0, 0));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Bienvenido a pagos");

        jLabel2.setForeground(new java.awt.Color(0, 0, 0));
        jLabel2.setText("id Alquiler");

        btnBuscarAlquiller.setText("buscar");
        btnBuscarAlquiller.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarAlquillerActionPerformed(evt);
            }
        });

        jLabel3.setForeground(new java.awt.Color(0, 0, 0));
        jLabel3.setText("Dni Turista");

        jLabel4.setForeground(new java.awt.Color(0, 0, 0));
        jLabel4.setText("Monto a pagar");

        jLabel5.setForeground(new java.awt.Color(0, 0, 0));
        jLabel5.setText("fecha de pago");

        jLabel6.setForeground(new java.awt.Color(0, 0, 0));
        jLabel6.setText("Metodo de pago");

        JcmbMetodoPago.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "efectivo", "yape", "plin", "tarjeta" }));

        jLabel7.setForeground(new java.awt.Color(0, 0, 0));
        jLabel7.setText("id Pago");

        jLabel8.setForeground(new java.awt.Color(0, 0, 0));
        jLabel8.setText("estado del pago");

        JcmbEstadoPago.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Pendiente", "Completado", "anulado" }));

        btnNuevo.setText("nuevo");
        btnNuevo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevoActionPerformed(evt);
            }
        });

        btnBuscar.setText("buscar");
        btnBuscar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarActionPerformed(evt);
            }
        });

        btnEditar.setText("editar");
        btnEditar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditarActionPerformed(evt);
            }
        });

        btnEliminar.setText("eliminar");
        btnEliminar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarActionPerformed(evt);
            }
        });

        btnGrabar.setText("grabar");
        btnGrabar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGrabarActionPerformed(evt);
            }
        });

        btnPagar.setText("Pagar");
        btnPagar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPagarActionPerformed(evt);
            }
        });

        btnAnular.setText("Anular");
        btnAnular.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAnularActionPerformed(evt);
            }
        });

        jLabel9.setForeground(new java.awt.Color(0, 0, 0));
        jLabel9.setText("monto sin igv");

        txtMontoSinIgv.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtMontoSinIgvActionPerformed(evt);
            }
        });

        jLabel10.setForeground(new java.awt.Color(0, 0, 0));
        jLabel10.setText("igv");

        txtIgv.setText("18%");
        txtIgv.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtIgvActionPerformed(evt);
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
                        .addGap(21, 21, 21)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSeparator1)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel8)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(JcmbEstadoPago, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel6)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(JcmbMetodoPago, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(24, 24, 24)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel2)
                                            .addComponent(jLabel7))
                                        .addGap(18, 18, 18)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(txtIdAlquiler)
                                            .addComponent(txtIdPago, javax.swing.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE))))
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(119, 119, 119)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel4)
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(btnPagar, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(btnAnular, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGap(18, 18, 18)
                                                .addComponent(btnBuscarAlquiller, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(38, 38, 38)
                                                .addComponent(jLabel3))
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                                .addGap(97, 97, 97)
                                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 82, Short.MAX_VALUE)))))
                                        .addGap(29, 29, 29)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(txtIgv)
                                            .addComponent(txtDniTuri, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jDateChooserFechaPago, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE)
                                            .addComponent(txtMontoSinIgv)
                                            .addComponent(txtMontoTotal))))
                                .addGap(0, 63, Short.MAX_VALUE)))))
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(36, 36, 36)
                .addComponent(btnNuevo, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29)
                .addComponent(btnBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(33, 33, 33)
                .addComponent(btnEditar, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addComponent(btnEliminar, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addComponent(btnGrabar, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(70, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtIdAlquiler, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnBuscarAlquiller)
                        .addComponent(jLabel3)
                        .addComponent(txtDniTuri, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(24, 24, 24)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel5)
                        .addComponent(jLabel7)
                        .addComponent(txtIdPago, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jDateChooserFechaPago, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(JcmbMetodoPago, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(txtMontoSinIgv, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtIgv, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel10))))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(47, 47, 47)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8)
                            .addComponent(JcmbEstadoPago, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(33, 33, 33))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(txtMontoTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnPagar)
                            .addComponent(btnAnular))
                        .addGap(18, 18, 18)))
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnNuevo)
                    .addComponent(btnBuscar)
                    .addComponent(btnEditar)
                    .addComponent(btnEliminar)
                    .addComponent(btnGrabar))
                .addContainerGap(152, Short.MAX_VALUE))
        );

        add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 640, 500));
    }// </editor-fold>//GEN-END:initComponents

   
    private void btnBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarActionPerformed
        String id = JOptionPane.showInputDialog("Ingrese ID Pago:");
        if (id == null) {
            return;
        }

        String sql = "SELECT * FROM Pago WHERE idPago = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                txtIdPago.setText(rs.getString("idPago"));
                txtIdAlquiler.setText(rs.getString("idAlquiler"));
                txtMontoSinIgv.setText(rs.getBigDecimal("monto").toString());
                jDateChooserFechaPago.setDate(rs.getDate("fechaPago"));

                seleccionarItemIgnoreCase(JcmbMetodoPago, rs.getString("metodoPago"));
                seleccionarItemIgnoreCase(JcmbEstadoPago, rs.getString("estado"));

                calcularMontos();

                PreparedStatement psDni = conexion.prepareStatement(
                        "SELECT t.dni FROM Turista t JOIN Alquiler a ON a.idTurista = t.idTurista WHERE a.idAlquiler=?");
                psDni.setString(1, txtIdAlquiler.getText());
                ResultSet rsDni = psDni.executeQuery();
                if (rsDni.next()) {
                    txtDniTuri.setText(rsDni.getString("dni"));
                }

            } else {
                JOptionPane.showMessageDialog(this, "Pago no encontrado");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_btnBuscarActionPerformed

    private void btnNuevoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoActionPerformed
        limpiarCampos();
        habilitarCampos(true);
        btnAnular.setEnabled(false);
        modo = "nuevo";
    }//GEN-LAST:event_btnNuevoActionPerformed

    private void btnBuscarAlquillerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarAlquillerActionPerformed
      String idAlquiler = JOptionPane.showInputDialog(this, "Ingrese ID Alquiler (ej. A001):");
        if (idAlquiler == null || idAlquiler.trim().isEmpty()) return;

        // 1. Validar duplicados (Tu código está bien aquí)
        try {
            String sqlCheck = "SELECT COUNT(*) FROM Pago WHERE idAlquiler = ? AND estado != 'anulado'";
            PreparedStatement psCheck = conexion.prepareStatement(sqlCheck);
            psCheck.setString(1, idAlquiler);
            ResultSet rsCheck = psCheck.executeQuery();
            if (rsCheck.next() && rsCheck.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "⚠️ Este alquiler ya tiene un pago activo.", "Duplicado", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (SQLException e) { e.printStackTrace(); }

        // 2. Buscar datos
        String sql = "SELECT a.total, t.dni FROM Alquiler a JOIN Turista t ON a.idTurista = t.idTurista WHERE a.idAlquiler = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, idAlquiler);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                txtIdAlquiler.setText(idAlquiler);
                txtDniTuri.setText(rs.getString("dni"));
                
                // --- CORRECCIÓN AQUÍ ---
                // El total de la BD va al campo TOTAL, no al subtotal.
                txtMontoTotal.setText(rs.getBigDecimal("total").toString());
                
                // Ahora sí, calculamos hacia atrás (desagregamos IGV)
                calcularMontos();
                
            } else {
                JOptionPane.showMessageDialog(this, "Alquiler no encontrado.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error SQL: " + e.getMessage());
        }

    }//GEN-LAST:event_btnBuscarAlquillerActionPerformed

    private void btnEditarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditarActionPerformed
         if (txtIdPago.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe buscar un pago antes de editar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        modo = "edicion";
        habilitarCampos(true);
        btnBuscarAlquiller.setEnabled(false);
    }//GEN-LAST:event_btnEditarActionPerformed

    private void btnPagarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPagarActionPerformed
        // Simplemente cambiamos el estado visualmente
        JcmbEstadoPago.setSelectedItem("Completado");

        // Feedback visual al usuario
        if (modo != null && (modo.equals("nuevo") || modo.equals("edicion"))) {
            JOptionPane.showMessageDialog(this,
                    "Estado cambiado a 'Completado'.\nPulse GRABAR para confirmar la transacción.",
                    "Acción requerida", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Primero presione Nuevo o Editar.");
        }
    }//GEN-LAST:event_btnPagarActionPerformed

    private void btnAnularActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAnularActionPerformed
        // Simplemente cambiamos el estado visualmente
        JcmbEstadoPago.setSelectedItem("anulado"); // Asegúrate que coincida con el item de tu combo

        if (modo != null && (modo.equals("nuevo") || modo.equals("edicion"))) {
            JOptionPane.showMessageDialog(this,
                    "Estado cambiado a 'Anulado'.\nPulse GRABAR para confirmar.",
                    "Acción requerida", JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Primero presione Nuevo o Editar.");
        }
    }//GEN-LAST:event_btnAnularActionPerformed

    private void btnGrabarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGrabarActionPerformed
        if (modo == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar Nuevo o Editar.");
            return;
        }

        try {
            if (jDateChooserFechaPago.getDate() == null) {
                JOptionPane.showMessageDialog(this, "Seleccione fecha.");
                return;
            }
            if (txtMontoTotal.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay monto calculado.");
                return;
            }

            // INICIO TRANSACCIÓN
            conexion.setAutoCommit(false);

            java.sql.Date fecha = new java.sql.Date(jDateChooserFechaPago.getDate().getTime());
            BigDecimal monto = new BigDecimal(txtMontoSinIgv.getText());
            String metodo = JcmbMetodoPago.getSelectedItem().toString();
            String estado = JcmbEstadoPago.getSelectedItem().toString();
            String idAlq = txtIdAlquiler.getText();

            if (modo.equals("nuevo")) {
                // Validación extra dentro de la transacción
                String sqlVerif = "SELECT estado FROM Pago WHERE idAlquiler = ?";
                try (PreparedStatement psV = conexion.prepareStatement(sqlVerif)) {
                    psV.setString(1, idAlq);
                    ResultSet rsV = psV.executeQuery();
                    if (rsV.next() && rsV.getString("estado").equalsIgnoreCase("Completado")) {
                        JOptionPane.showMessageDialog(this, "Este alquiler ya fue pagado.");
                        conexion.rollback();
                        return; // Finally se encargará del autoCommit
                    }
                }

                String sql = "{call registrarPago(?,?,?,?,?)}";
                try (CallableStatement cs = conexion.prepareCall(sql)) {
                    cs.setString(1, idAlq);
                    cs.setDate(2, fecha);
                    cs.setBigDecimal(3, monto);
                    cs.setString(4, metodo);
                    cs.setString(5, estado);
                    cs.execute();
                    JOptionPane.showMessageDialog(this, "Pago Registrado Exitosamente");

                    PreparedStatement psId = conexion.prepareStatement("SELECT TOP 1 idPago FROM Pago ORDER BY idPago DESC");
                    ResultSet rsId = psId.executeQuery();
                    if (rsId.next()) {
                        txtIdPago.setText(rsId.getString(1));
                    }
                }
            } else if (modo.equals("edicion")) {
                String sql = "UPDATE Pago SET fechaPago=?, monto=?, metodoPago=?, estado=? WHERE idPago=?";
                try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                    ps.setDate(1, fecha);
                    ps.setBigDecimal(2, monto);
                    ps.setString(3, metodo);
                    ps.setString(4, estado);
                    ps.setString(5, txtIdPago.getText());
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Pago Actualizado");
                }
            }

            conexion.commit(); // CONFIRMAR
            habilitarCampos(false);
            modo = null;

        } catch (Exception e) {
            try {
                conexion.rollback();
            } catch (SQLException ex) {
            }
            JOptionPane.showMessageDialog(this, "Error al grabar: " + e.getMessage());
        } finally {
            try {
                conexion.setAutoCommit(true);
            } catch (SQLException ex) {
            }
        }
    }//GEN-LAST:event_btnGrabarActionPerformed

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarActionPerformed
        if (txtIdPago.getText().isEmpty()) {
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "¿Eliminar pago?") == 0) {
            try {
                PreparedStatement ps = conexion.prepareStatement("DELETE FROM Pago WHERE idPago=?");
                ps.setString(1, txtIdPago.getText());
                ps.executeUpdate();
                limpiarCampos();
                JOptionPane.showMessageDialog(this, "Eliminado");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }//GEN-LAST:event_btnEliminarActionPerformed

    private void txtIgvActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtIgvActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtIgvActionPerformed

    private void txtMontoSinIgvActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtMontoSinIgvActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtMontoSinIgvActionPerformed

    // --- MÉTODOS DE DISEÑO VISUAL (Copiados y adaptados de PanelRecursos2) ---
    
    private void aplicarEstilosModernos() {
        // 1. Colores y Fuentes
        java.awt.Color colorTexto = new java.awt.Color(31, 78, 95); // Azul Petróleo
        java.awt.Font fuenteTitulo = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 28);
        java.awt.Font fuenteLabels = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14);
        java.awt.Font fuenteCampos = new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14);

        

        // 3. Estilizar Etiquetas (Labels)
        javax.swing.JLabel[] labels = {
            jLabel2, jLabel3, jLabel4, jLabel5, jLabel6, 
            jLabel7, jLabel8, jLabel9, jLabel10
        };
        for (javax.swing.JLabel lbl : labels) {
            lbl.setFont(fuenteLabels);
            lbl.setForeground(colorTexto);
        }
        
        // Arreglar textos de labels para que se vean prolijos
        jLabel2.setText("ID Alquiler:");   jLabel3.setText("DNI Turista:");
        jLabel7.setText("ID Pago:");       jLabel5.setText("Fecha Pago:");
        jLabel9.setText("Subtotal:");      jLabel10.setText("IGV (18%):");
        jLabel4.setText("Total a Pagar:"); jLabel6.setText("Método:");
        jLabel8.setText("Estado:");

        // 4. Estilo de Campos de Texto (Bordes suaves)
        javax.swing.border.Border bordeCampo = javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)),
            javax.swing.BorderFactory.createEmptyBorder(5, 8, 5, 8)
        );

        javax.swing.JTextField[] campos = {
            txtIdAlquiler, txtIdPago, txtDniTuri, txtMontoSinIgv, txtIgv, txtMontoTotal
        };
        for (javax.swing.JTextField txt : campos) {
            txt.setFont(fuenteCampos);
            txt.setBorder(bordeCampo);
        }
        
        // Estilo de Combos
        JcmbMetodoPago.setFont(fuenteCampos);
        JcmbEstadoPago.setFont(fuenteCampos);

        // 5. ESTILIZAR BOTONES (Aquí está la clave de Recursos2)
        // Usamos el método auxiliar para darles fondo blanco y borde
        estilizarBoton(btnNuevo, "📄 Nuevo");
        estilizarBoton(btnBuscar, "🔍 Buscar");
        estilizarBoton(btnEditar, "✏️ Editar");
        estilizarBoton(btnEliminar, "🗑️ Eliminar");
        estilizarBoton(btnGrabar, "💾 Guardar");
        
        // Botones específicos de Pagos
        estilizarBoton(btnBuscarAlquiller, "🔎"); 
        estilizarBoton(btnPagar, "💰 Pagar");
        estilizarBoton(btnAnular, "🚫 Anular");
        
        // Opcional: Hacer el botón eliminar rojo suave o dejarlo blanco
        // btnEliminar.setForeground(java.awt.Color.RED); 
    }

    // Método auxiliar idéntico al de PanelRecursos2
    private void estilizarBoton(javax.swing.JButton btn, String texto) {
        btn.setText(texto);
        btn.setFont(new java.awt.Font("Segoe UI Emoji", java.awt.Font.BOLD, 12));
        btn.setForeground(new java.awt.Color(31, 78, 95)); // Azul texto
        btn.setBackground(java.awt.Color.WHITE);           // FONDO BLANCO
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.setBorderPainted(true);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        
        // Borde redondeado suave o cuadrado
        btn.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200), 1),
            javax.swing.BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
    }

  private void corregirDistribucion() {
        // Desactivar layout automático
        jPanel1.setLayout(null);

        // --- 1. TÍTULO (Usamos el label existente para asegurar que se vea) ---
        jLabel1.setVisible(true); // Aseguramos que se vea
        jLabel1.setText("GESTIÓN DE PAGOS");
        jLabel1.setFont(new Font("Segoe UI", Font.BOLD, 26)); // Fuente grande
        jLabel1.setForeground(new Color(31, 78, 95)); // Azul corporativo
        jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
        // Lo ponemos bien arriba y ancho completo
        jLabel1.setBounds(0, 30, 640, 40); 

        // --- VARIABLES DE POSICIÓN (Ajustadas para bajar todo) ---
        // Antes empezabamos en 40, ahora en 100 para dejar espacio al título
        int yInicio = 100; 
        
        int col1Label = 40;
        int col1Field = 150;
        
        int col2Label = 380;
        int col2Field = 480;
        
        int gapY = 55; // Más separación vertical entre filas (antes 45)

        // --- FILA 1: ID Alquiler y Buscar ---
        jLabel2.setText("ID Alquiler:");
        jLabel2.setBounds(col1Label, yInicio, 100, 30);
        txtIdAlquiler.setBounds(col1Field, yInicio, 120, 30);
        btnBuscarAlquiller.setBounds(280, yInicio, 50, 30); 

        jLabel3.setText("DNI Turista:");
        jLabel3.setBounds(col2Label, yInicio, 100, 30);
        txtDniTuri.setBounds(col2Field, yInicio, 120, 30);

        // --- FILA 2: ID Pago y Fecha ---
        int yFila2 = yInicio + gapY;
        jLabel7.setText("ID Pago:");
        jLabel7.setBounds(col1Label, yFila2, 100, 30);
        txtIdPago.setBounds(col1Field, yFila2, 120, 30);

        jLabel5.setText("Fecha Pago:");
        jLabel5.setBounds(col2Label, yFila2, 100, 30);
        jDateChooserFechaPago.setBounds(col2Field, yFila2, 120, 30);

        // --- FILA 3: Método y Subtotal ---
        int yFila3 = yInicio + (gapY * 2);
        jLabel6.setText("Método:");
        jLabel6.setBounds(col1Label, yFila3, 100, 30);
        JcmbMetodoPago.setBounds(col1Field, yFila3, 180, 30);

        jLabel9.setText("Subtotal:");
        jLabel9.setBounds(col2Label, yFila3, 100, 30);
        txtMontoSinIgv.setBounds(col2Field, yFila3, 120, 30);

        // --- FILA 4: Estado e IGV ---
        int yFila4 = yInicio + (gapY * 3);
        jLabel8.setText("Estado:");
        jLabel8.setBounds(col1Label, yFila4, 100, 30);
        JcmbEstadoPago.setBounds(col1Field, yFila4, 180, 30);

        jLabel10.setText("IGV (18%):");
        jLabel10.setBounds(col2Label, yFila4, 100, 30);
        txtIgv.setBounds(col2Field, yFila4, 120, 30);

        // --- FILA 5: TOTAL (Destacado) ---
        int yFila5 = yInicio + (gapY * 4);
        jLabel4.setText("TOTAL:");
        jLabel4.setFont(new Font("Segoe UI", Font.BOLD, 18)); 
        jLabel4.setBounds(col2Label, yFila5, 100, 30);
        
        txtMontoTotal.setBounds(col2Field, yFila5, 120, 35);
        txtMontoTotal.setFont(new Font("Segoe UI", Font.BOLD, 15));
        txtMontoTotal.setForeground(new Color(0, 100, 0)); 

        // --- BOTONES DE ACCIÓN (Pagar / Anular) ---
        // Bajamos más y separamos horizontalmente
        int yAccion = yFila5 + 50;
        
        // Pagar alineado a la etiqueta Total
        btnPagar.setBounds(col2Label, yAccion, 110, 35); 
        
        // Anular más a la derecha (separado 20px del otro)
        btnAnular.setBounds(col2Label + 130, yAccion, 110, 35); 

        // --- BARRA INFERIOR ---
        // Separador
        jSeparator1.setBounds(20, yAccion + 50, 600, 10);

        // Botones CRUD (Más abajo)
        int yBotones = yAccion + 70;
        int wBtn = 100;
        int hBtn = 40;
        int gapBtn = 15; 
        int xInicial = 35; 

        btnNuevo.setBounds(xInicial, yBotones, wBtn, hBtn);
        btnBuscar.setBounds(xInicial + (wBtn + gapBtn) * 1, yBotones, wBtn, hBtn);
        btnEditar.setBounds(xInicial + (wBtn + gapBtn) * 2, yBotones, wBtn, hBtn);
        btnEliminar.setBounds(xInicial + (wBtn + gapBtn) * 3, yBotones, wBtn, hBtn);
        btnGrabar.setBounds(xInicial + (wBtn + gapBtn) * 4, yBotones, wBtn, hBtn);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> JcmbEstadoPago;
    private javax.swing.JComboBox<String> JcmbMetodoPago;
    private javax.swing.JButton btnAnular;
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnBuscarAlquiller;
    private javax.swing.JButton btnEditar;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnGrabar;
    private javax.swing.JButton btnNuevo;
    private javax.swing.JButton btnPagar;
    private com.toedter.calendar.JDateChooser jDateChooserFechaPago;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField txtDniTuri;
    private javax.swing.JTextField txtIdAlquiler;
    private javax.swing.JTextField txtIdPago;
    private javax.swing.JTextField txtIgv;
    private javax.swing.JTextField txtMontoSinIgv;
    private javax.swing.JTextField txtMontoTotal;
    // End of variables declaration//GEN-END:variables
}
