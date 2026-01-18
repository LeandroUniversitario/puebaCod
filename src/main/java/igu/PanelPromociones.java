/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package igu;

import java.awt.Color;
import java.awt.Font;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

/**
 *
 * @author rafae
 */
public class PanelPromociones extends javax.swing.JPanel {
    private Connection conexion;
    private String nivel;
    private String modo;
    /**
     * Creates new form PanelPromociones
     */
    public PanelPromociones(Connection conexion, String nivel) {
        initComponents();
        this.conexion = conexion;
        this.nivel = nivel;
        // 2. INTEGRACIÓN DEL DEGRADADO (Igual que en Recursos)
        PanelDegradado fondo = new PanelDegradado();
        fondo.setLayout(new java.awt.BorderLayout());

        // Hacemos transparente el panel de NetBeans
        jPanel1.setOpaque(false);

        // Agregamos jPanel1 al fondo degradado
        fondo.add(jPanel1, java.awt.BorderLayout.CENTER);

        // Reemplazamos el contenido del panel principal
        this.setLayout(new java.awt.BorderLayout());
        this.removeAll();
        this.add(fondo, java.awt.BorderLayout.CENTER);

        this.revalidate();
        this.repaint();

        // 3. ESTILOS Y DISTRIBUCIÓN
        aplicarEstilosModernos();
        corregirDistribucion(); // Acomoda los campos matemáticamente
        
        limpiarCampos();
        habilitarCampos(false);

    }
    
    private void aplicarEstilosModernos() {
        // Colores y Fuentes
        Color colorTexto = new Color(31, 78, 95); // Azul Petróleo
        Font fuenteTitulo = new Font("Segoe UI", Font.BOLD, 24);
        Font fuenteLabels = new Font("Segoe UI", Font.BOLD, 14);
        Font fuenteCampos = new Font("Segoe UI", Font.PLAIN, 14);

        // 1. Título
        jLabel1.setFont(fuenteTitulo);
        jLabel1.setForeground(colorTexto);
        jLabel1.setText("GESTIÓN DE PROMOCIONES");
        jLabel1.setHorizontalAlignment(SwingConstants.CENTER);

        // 2. Etiquetas (Labels)
        javax.swing.JLabel[] labels = {jLabel2, jLabel3, jLabel4, jLabel5};
        String[] textos = {"ID", "Descripción", "Tipo", "Condición (Horas)"};

        for (int i = 0; i < labels.length; i++) {
            labels[i].setFont(fuenteLabels);
            labels[i].setForeground(colorTexto);
            labels[i].setText(textos[i]); // Corregimos textos
        }

        // 3. Campos de Texto
        javax.swing.JTextField[] campos = {txtId, txtDescripcion, txtTipo, txtCondicion};
        javax.swing.border.Border bordeCampo = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        );

        for (javax.swing.JTextField txt : campos) {
            txt.setFont(fuenteCampos);
            txt.setBorder(bordeCampo);
        }

        // 4. Botones
        javax.swing.JButton[] botones = {btnNuevo, btnBuscar, btnEditar, btnEliminar, btnGrabar, btnVerPromos};
        for (javax.swing.JButton btn : botones) {
            estilizarBoton(btn);
        }

        // Emojis para botones
        btnNuevo.setText("📄 Nuevo");
        btnBuscar.setText("🔍 Buscar");
        btnEditar.setText("✏️ Editar");
        btnEliminar.setText("🗑️ Eliminar");
        btnGrabar.setText("💾 Guardar");
        btnVerPromos.setText("📋 Ver Lista");
    }

    private void estilizarBoton(javax.swing.JButton btn) {
        btn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 12));
        btn.setForeground(new Color(31, 78, 95));
        btn.setBackground(Color.WHITE);
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
    }

    private void corregirDistribucion() {
       // Desactivar layout automático
        jPanel1.setLayout(null);

        // 1. TÍTULO
        jLabel1.setBounds(0, 20, 640, 40);

       

        // 2. FORMULARIO
        int xLabel = 40;
        int xInput = 180;
        int anchoInput = 400;
        int alto = 30;
        int y = 90;     // Altura inicial
        int gap = 50;   // Separación entre filas

        // Fila 1: ID
        jLabel2.setBounds(xLabel, y, 120, alto);
        txtId.setBounds(xInput, y, 150, alto);

        // Fila 2: Descripción
        y += gap; // 140
        jLabel3.setBounds(xLabel, y, 120, alto);
        txtDescripcion.setBounds(xInput, y, anchoInput, alto);

        // Fila 3: Tipo
        y += gap; // 190
        jLabel4.setBounds(xLabel, y, 120, alto);
        txtTipo.setBounds(xInput, y, anchoInput, alto);

        // Fila 4: Condición (Termina en Y=240 + 30 = 270)
        y += gap; // 240
        jLabel5.setBounds(xLabel, y, 140, alto);
        txtCondicion.setBounds(xInput, y, 100, alto);

        // --- 3. LÍNEA SEPARADORA (FORMULARIO / BOTONES) ---
        javax.swing.JPanel lineaSeparadora = new javax.swing.JPanel();
        lineaSeparadora.setBackground(new java.awt.Color(31, 78, 95)); // Azul Petróleo
        // La ponemos en Y=295 (El hueco perfecto entre el form y los botones)
        lineaSeparadora.setBounds(30, 295, 580, 2); 
        jPanel1.add(lineaSeparadora);
        // --------------------------------------------------

        // 4. BOTONES (Los subimos a Y=320 para acercarlos a la línea)
        int yBtn = 320; 
        int xBtn = 30;
        int wBtn = 105;
        int gapBtn = 10;

        btnNuevo.setBounds(xBtn, yBtn, wBtn, 35);
        xBtn += wBtn + gapBtn;
        
        btnBuscar.setBounds(xBtn, yBtn, wBtn, 35);
        xBtn += wBtn + gapBtn;
        
        btnEditar.setBounds(xBtn, yBtn, wBtn, 35);
        xBtn += wBtn + gapBtn;
        
        btnEliminar.setBounds(xBtn, yBtn, wBtn, 35);
        xBtn += wBtn + gapBtn;
        
        btnGrabar.setBounds(xBtn, yBtn, wBtn, 35);

        // Botón Ver Lista (Un poco más abajo)
        btnVerPromos.setBounds(480, 400, 120, 35);
    }

    private void limpiarCampos() {
        txtId.setText("");
        txtTipo.setText("");
        txtDescripcion.setText("");
        txtCondicion.setText("");
       
    }

    private void habilitarCampos(boolean habilitar) {
        Color colorFondo = habilitar ? Color.WHITE : new Color(245, 245, 245);

        // El ID siempre bloqueado visualmente (lo maneja el sistema o la búsqueda)
        txtId.setEditable(false);
        txtId.setBackground(new Color(230, 230, 230));

        txtTipo.setEditable(habilitar);
        txtTipo.setBackground(colorFondo);

        txtDescripcion.setEditable(habilitar);
        txtDescripcion.setBackground(colorFondo);

        txtCondicion.setEditable(habilitar);
        txtCondicion.setBackground(colorFondo);

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
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        txtId = new javax.swing.JTextField();
        txtDescripcion = new javax.swing.JTextField();
        txtTipo = new javax.swing.JTextField();
        txtCondicion = new javax.swing.JTextField();
        btnNuevo = new javax.swing.JButton();
        btnBuscar = new javax.swing.JButton();
        btnEditar = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        btnGrabar = new javax.swing.JButton();
        btnVerPromos = new javax.swing.JButton();

        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setForeground(new java.awt.Color(0, 0, 0));

        jLabel1.setForeground(new java.awt.Color(0, 0, 0));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel1.setText("BIENVENIDO A LA GESTION DE PROMCIONES");

        jLabel2.setForeground(new java.awt.Color(0, 0, 0));
        jLabel2.setText("Id");

        jLabel3.setForeground(new java.awt.Color(0, 0, 0));
        jLabel3.setText("Descripcion");

        jLabel4.setForeground(new java.awt.Color(0, 0, 0));
        jLabel4.setText("Tipo");

        jLabel5.setForeground(new java.awt.Color(0, 0, 0));
        jLabel5.setText("Condicion de horas");

        txtId.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtIdActionPerformed(evt);
            }
        });

        txtCondicion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCondicionActionPerformed(evt);
            }
        });

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

        btnVerPromos.setText("ver promociones");
        btnVerPromos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVerPromosActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 272, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                    .addComponent(jLabel5)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(txtCondicion, javax.swing.GroupLayout.PREFERRED_SIZE, 296, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(txtDescripcion, javax.swing.GroupLayout.PREFERRED_SIZE, 297, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel2)
                                            .addComponent(jLabel3)
                                            .addComponent(jLabel4))
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGap(62, 62, 62)
                                                .addComponent(txtId, javax.swing.GroupLayout.PREFERRED_SIZE, 297, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(txtTipo, javax.swing.GroupLayout.PREFERRED_SIZE, 297, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(btnNuevo, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(25, 25, 25)
                                .addComponent(btnBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(30, 30, 30)
                                .addComponent(btnEditar, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(28, 28, 28)
                                .addComponent(btnEliminar, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnGrabar, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(219, 219, 219)
                        .addComponent(btnVerPromos, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(82, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel1)
                .addGap(28, 28, 28)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtDescripcion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtTipo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(22, 22, 22)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(txtCondicion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(40, 40, 40)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnNuevo)
                    .addComponent(btnBuscar)
                    .addComponent(btnEditar)
                    .addComponent(btnEliminar)
                    .addComponent(btnGrabar))
                .addGap(72, 72, 72)
                .addComponent(btnVerPromos)
                .addContainerGap(122, Short.MAX_VALUE))
        );

        add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 640, 500));
    }// </editor-fold>//GEN-END:initComponents

    private void txtIdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtIdActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtIdActionPerformed

    private void txtCondicionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCondicionActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCondicionActionPerformed

    private void btnVerPromosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVerPromosActionPerformed
       VentanaPromociones v= new VentanaPromociones(conexion);
       v.setVisible(true);
       v.setLocationRelativeTo(null);
    }//GEN-LAST:event_btnVerPromosActionPerformed

    private void btnNuevoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoActionPerformed
       if (nivel.equalsIgnoreCase("administrador")) {
            limpiarCampos();
            modo = "nuevo";
            habilitarCampos(true);
            txtId.setText("(Automático)"); // Feedback visual
        } else {
            JOptionPane.showMessageDialog(this, "Opción solo para administradores.");
        }
    }//GEN-LAST:event_btnNuevoActionPerformed
    
    private void buscarPromo(String idBuscar) {
        try {
            String sql = "SELECT * FROM Promocion WHERE idPromocion = ?";
            PreparedStatement ps = conexion.prepareStatement(sql);
            ps.setString(1, idBuscar);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                txtId.setText(rs.getString("idPromocion"));
                txtDescripcion.setText(rs.getString("descripcion"));
                txtTipo.setText(rs.getString("tipo"));
                txtCondicion.setText(String.valueOf(rs.getInt("condicionHoras")));

                habilitarCampos(false);
                modo = null; // Reseteamos modo para evitar ediciones accidentales
                JOptionPane.showMessageDialog(this, "Promoción encontrada.");
            } else {
                JOptionPane.showMessageDialog(this, "No se encontró la promoción.", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error SQL: " + e.getMessage());
        }
    }
    private void btnBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarActionPerformed
       String promoBuscada = JOptionPane.showInputDialog(this, "Ingrese el ID de la promoción (ej: P001)");
        if (promoBuscada != null && !promoBuscada.trim().isEmpty()) {
            buscarPromo(promoBuscada.trim());
        }
    }//GEN-LAST:event_btnBuscarActionPerformed

    private void btnEditarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditarActionPerformed
       if (nivel.equalsIgnoreCase("administrador")) {
            // CORRECCIÓN IMPORTANTE: Validar que haya algo cargado
            if (txtId.getText().isEmpty() || txtId.getText().equals("(Automático)")) {
                JOptionPane.showMessageDialog(this, "Primero busque una promoción para editar.");
                return;
            }
            
            habilitarCampos(true);
            modo = "edicion";
        } else {
            JOptionPane.showMessageDialog(this, "Opción solo para administradores.");
        }
    }//GEN-LAST:event_btnEditarActionPerformed

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarActionPerformed
        if (!nivel.equalsIgnoreCase("administrador")) {
            JOptionPane.showMessageDialog(this, "Acceso denegado.");
            return;
        }

        String idEliminar = JOptionPane.showInputDialog(this, "Ingrese ID a eliminar:");
        if (idEliminar == null || idEliminar.trim().isEmpty()) {
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "¿Seguro de eliminar " + idEliminar + "?");
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            // Verificar existencia primero
            String sqlCheck = "SELECT COUNT(*) FROM Promocion WHERE idPromocion = ?";
            PreparedStatement psCheck = conexion.prepareStatement(sqlCheck);
            psCheck.setString(1, idEliminar);
            ResultSet rs = psCheck.executeQuery();
            rs.next();

            if (rs.getInt(1) == 0) {
                JOptionPane.showMessageDialog(this, "Ese ID no existe.");
                return;
            }

            // Eliminar
            String sqlDel = "DELETE FROM Promocion WHERE idPromocion = ?";
            PreparedStatement psDel = conexion.prepareStatement(sqlDel);
            psDel.setString(1, idEliminar);
            psDel.executeUpdate();

            JOptionPane.showMessageDialog(this, "Eliminado correctamente.");
            limpiarCampos(); // Por si estaba mostrándose en pantalla

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al eliminar: " + e.getMessage());
        }
    }//GEN-LAST:event_btnEliminarActionPerformed

    private void btnGrabarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGrabarActionPerformed
        if (modo == null) {
            JOptionPane.showMessageDialog(this, "Seleccione 'Nuevo' o 'Editar' primero.");
            return;
        }

        // 1. Validaciones Generales (Campos Vacíos)
        if (txtDescripcion.getText().trim().isEmpty()
                || txtTipo.getText().trim().isEmpty()
                || txtCondicion.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // ---------------------------------------------------------
        // 1.1 VALIDACIÓN DE FORMATO PORCENTAJE (NUEVO CÓDIGO)
        // ---------------------------------------------------------
        String desc = txtDescripcion.getText().trim();

        // Regex: ^\d+(\.\d+)?%$
        // Explico: Empieza con números, opcionalmente tiene un punto y más números, y termina obligatoriamente en %.
        if (!desc.matches("^\\d+(\\.\\d+)?%$")) {
            JOptionPane.showMessageDialog(this,
                    "El campo Descripción debe ser un porcentaje válido (ej: 10% o 12.5%).\nNo incluya texto extra.",
                    "Formato Incorrecto",
                    JOptionPane.WARNING_MESSAGE);
            return; // <--- Detenemos aquí si no cumple el formato
        }
        // Opcional: Validar que esté entre 0 y 100
        double valorNumerico = Double.parseDouble(desc.replace("%", ""));
        if (valorNumerico < 0 || valorNumerico > 100) {
            JOptionPane.showMessageDialog(this, "El porcentaje debe estar entre 0% y 100%.");
            return;
        }
        // ---------------------------------------------------------

        // Validación de Horas (tu código original)
        int condicionHoras = 0;
        try {
            condicionHoras = Integer.parseInt(txtCondicion.getText().trim());
            if (condicionHoras <= 0) {
                JOptionPane.showMessageDialog(this, "La condición de horas debe ser mayor a 0.");
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Ingrese un número válido para las horas.");
            return;
        }

        // 2. Guardado (tu código original)
        try {
            if (modo.equals("nuevo")) {
                CallableStatement cs = conexion.prepareCall("{call registrarPromocion(?, ?, ?)}");
                cs.setString(1, txtDescripcion.getText().trim());
                cs.setString(2, txtTipo.getText().trim());
                cs.setInt(3, condicionHoras);
                cs.execute();
                JOptionPane.showMessageDialog(this, "¡Promoción registrada!");

            } else if (modo.equals("edicion")) {
                String sql = "UPDATE Promocion SET descripcion=?, tipo=?, condicionHoras=? WHERE idPromocion=?";
                PreparedStatement ps = conexion.prepareStatement(sql);
                ps.setString(1, txtDescripcion.getText().trim());
                ps.setString(2, txtTipo.getText().trim());
                ps.setInt(3, condicionHoras);
                ps.setString(4, txtId.getText().trim());

                int filas = ps.executeUpdate();
                if (filas > 0) {
                    JOptionPane.showMessageDialog(this, "¡Promoción actualizada!");
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo actualizar.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }

            // Limpieza final
            limpiarCampos();
            habilitarCampos(false);
            modo = null;

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error en BD: " + ex.getMessage());
        }
    }//GEN-LAST:event_btnGrabarActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnEditar;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnGrabar;
    private javax.swing.JButton btnNuevo;
    private javax.swing.JButton btnVerPromos;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField txtCondicion;
    private javax.swing.JTextField txtDescripcion;
    private javax.swing.JTextField txtId;
    private javax.swing.JTextField txtTipo;
    // End of variables declaration//GEN-END:variables
}
