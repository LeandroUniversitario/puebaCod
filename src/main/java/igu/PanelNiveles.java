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
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 *
 * @author rafae
 */
public class PanelNiveles extends javax.swing.JPanel {
    private Connection conexion;
    private String modo;
    /**
     * Creates new form PanelNiveles
     */
    public PanelNiveles(Connection conexion) {
        initComponents();
        this.conexion=conexion;
        // --- 1. APLICAR FONDO DEGRADADO ---
        PanelDegradado fondo = new PanelDegradado();
        fondo.setLayout(new java.awt.BorderLayout());

        // Hacemos transparente el panel de NetBeans
        jPanel1.setOpaque(false);
        fondo.add(jPanel1, java.awt.BorderLayout.CENTER);

        // Reemplazamos en el panel principal
        this.setLayout(new java.awt.BorderLayout());
        this.removeAll();
        this.add(fondo, java.awt.BorderLayout.CENTER);

        this.revalidate();
        this.repaint();

        // --- 2. LÓGICA Y DISEÑO ---
        aplicarEstilosModernos();
        corregirDistribucion(); // Layout manual
        
        limpiarCampos();
        habilitarCampos(false);
    }

    // --- DISEÑO VISUAL ---
    private void aplicarEstilosModernos() {
        Color colorTexto = new Color(31, 78, 95); // Azul Petróleo
        Font fuenteTitulo = new Font("Segoe UI", Font.BOLD, 24);
        Font fuenteLabels = new Font("Segoe UI", Font.BOLD, 14);
        Font fuenteCampos = new Font("Segoe UI", Font.PLAIN, 14);

        // 1. Título
        jLabel1.setFont(fuenteTitulo);
        jLabel1.setForeground(colorTexto);
        jLabel1.setText("GESTIÓN DE NIVELES DE ACCESO"); // Texto más formal

        // 2. Labels
        JLabel[] labels = {jLabel2, jLabel3, jLabel4};
        String[] textos = {"ID Nivel", "Nombre Nivel", "Descripción"};
        
        for (int i = 0; i < labels.length; i++) {
            labels[i].setFont(fuenteLabels);
            labels[i].setForeground(colorTexto);
            labels[i].setText(textos[i]);
        }

        // 3. Inputs
        JTextField[] campos = {txtIdNivel, txtNombreNivel, txtDescripcion};
        javax.swing.border.Border borde = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        );

        for (JTextField txt : campos) {
            txt.setFont(fuenteCampos);
            txt.setBorder(borde);
        }

        // 4. Separador
        jSeparator1.setForeground(new Color(31, 78, 95));
        jSeparator1.setBackground(new Color(31, 78, 95));

        // 5. Botones
        JButton[] botones = {btnNuevo, btnBuscar, btnEditar, btnEliminar, btnGrabar};
        for (JButton btn : botones) {
            estilizarBoton(btn);
        }
        
        // Emojis y Textos
        btnNuevo.setText("📄 Nuevo");
        btnBuscar.setText("🔍"); // Icono solo para ID
        btnEditar.setText("✏️ Editar");
        btnEliminar.setText("🗑️ Eliminar");
        btnGrabar.setText("💾 Guardar");
        
        btnBuscar.setToolTipText("Buscar Nivel por ID");
    }

    private void estilizarBoton(JButton btn) {
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
        jPanel1.setLayout(null); // Layout manual

        // Título
        jLabel1.setBounds(0, 30, 640, 40);

        // Coordenadas base
        int xLabel = 50;
        int xInput = 180;
        int wInput = 350;
        int h = 35; // Altura estándar
        int y = 100;
        int gap = 50; // Separación vertical

        // Fila 1: ID + Botón Buscar
        jLabel2.setBounds(xLabel, y, 120, h);
        txtIdNivel.setBounds(xInput, y, 120, h); // ID corto
        btnBuscar.setBounds(xInput + 130, y, 50, h); // Lupa al lado

        // Fila 2: Nombre
        y += gap;
        jLabel3.setBounds(xLabel, y, 120, h);
        txtNombreNivel.setBounds(xInput, y, wInput, h);

        // Fila 3: Descripción
        y += gap;
        jLabel4.setBounds(xLabel, y, 120, h);
        txtDescripcion.setBounds(xInput, y, wInput, h);

        // Separador
        y += gap + 20;
        jSeparator1.setBounds(30, y, 580, 10);

        // Botones (Abajo)
        int yBtn = y + 30;
        int xBtn = 60;
        int wBtn = 110;
        
        btnNuevo.setBounds(xBtn, yBtn, wBtn, 35);
        xBtn += wBtn + 20;
        btnEditar.setBounds(xBtn, yBtn, wBtn, 35);
        xBtn += wBtn + 20;
        btnEliminar.setBounds(xBtn, yBtn, wBtn, 35);
        xBtn += wBtn + 20;
        btnGrabar.setBounds(xBtn, yBtn, wBtn, 35);
    }
    
    // --- LÓGICA DE NEGOCIO ---

    private void limpiarCampos() {
        txtIdNivel.setText("");
        txtNombreNivel.setText("");
        txtDescripcion.setText("");
    }

    private void habilitarCampos(boolean habilitar) {
        Color colorFondo = habilitar ? Color.WHITE : new Color(245, 245, 245);
        
        txtIdNivel.setEditable(false);
        txtIdNivel.setBackground(new Color(230, 230, 230)); // Gris claro fijo

        txtNombreNivel.setEditable(habilitar);
        txtNombreNivel.setBackground(colorFondo);

        txtDescripcion.setEditable(habilitar);
        txtDescripcion.setBackground(colorFondo);
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
        txtIdNivel = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtNombreNivel = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtDescripcion = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        btnBuscar = new javax.swing.JButton();
        btnNuevo = new javax.swing.JButton();
        btnEditar = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        btnGrabar = new javax.swing.JButton();

        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setForeground(new java.awt.Color(0, 0, 0));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("bienvenido a la gestion de niveles");

        jLabel2.setText("id Nivel");

        jLabel3.setText("nombre nivel");

        jLabel4.setText("descripcion");

        btnBuscar.setText("buscar");
        btnBuscar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarActionPerformed(evt);
            }
        });

        btnNuevo.setText("nuevo");
        btnNuevo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevoActionPerformed(evt);
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
                        .addGap(60, 60, 60)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txtIdNivel)
                                    .addComponent(txtNombreNivel, javax.swing.GroupLayout.DEFAULT_SIZE, 138, Short.MAX_VALUE))
                                .addGap(61, 61, 61)
                                .addComponent(btnBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(txtDescripcion, javax.swing.GroupLayout.PREFERRED_SIZE, 376, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(39, 39, 39)
                .addComponent(btnNuevo, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(38, 38, 38)
                .addComponent(btnEditar, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(37, 37, 37)
                .addComponent(btnEliminar, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(38, 38, 38)
                .addComponent(btnGrabar, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(112, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(35, 35, 35)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtIdNivel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBuscar))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtNombreNivel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(txtDescripcion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(30, 30, 30)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnNuevo)
                    .addComponent(btnEditar)
                    .addComponent(btnEliminar)
                    .addComponent(btnGrabar))
                .addContainerGap(265, Short.MAX_VALUE))
        );

        add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 640, 500));
    }// </editor-fold>//GEN-END:initComponents

    private void btnNuevoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoActionPerformed
       limpiarCampos();
        habilitarCampos(true);
        modo = "nuevo";
        txtIdNivel.setText("(Auto)"); // Feedback visual
    }//GEN-LAST:event_btnNuevoActionPerformed

    private void btnBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarActionPerformed
        String id = JOptionPane.showInputDialog(this, "Ingrese ID Nivel (Ej: N001):");
        if (id == null || id.trim().isEmpty()) {
            return;
        }

        limpiarCampos();

        try (PreparedStatement ps = conexion.prepareStatement("SELECT * FROM Nivel WHERE idNivel = ?")) {
            ps.setString(1, id.trim().toUpperCase());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                txtIdNivel.setText(rs.getString("idNivel"));
                txtNombreNivel.setText(rs.getString("nombreNivel"));
                txtDescripcion.setText(rs.getString("descripcion"));

                habilitarCampos(false);
                modo = "consulta"; // Evita ediciones accidentales
                JOptionPane.showMessageDialog(this, "Nivel encontrado.");
            } else {
                JOptionPane.showMessageDialog(this, "Nivel no encontrado.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error SQL: " + e.getMessage());
        }
    }//GEN-LAST:event_btnBuscarActionPerformed

    private void btnEditarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditarActionPerformed
        if (txtIdNivel.getText().isEmpty() || txtIdNivel.getText().equals("(Auto)")) {
            JOptionPane.showMessageDialog(this, "Busque un nivel primero.");
            return;
        }
        habilitarCampos(true);
        modo = "edicion";
    }//GEN-LAST:event_btnEditarActionPerformed

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarActionPerformed
        String id = txtIdNivel.getText().trim();
        if (id.isEmpty() || id.equals("(Auto)")) {
            JOptionPane.showMessageDialog(this, "Busque un nivel para eliminar.");
            return;
        }

        if (JOptionPane.showConfirmDialog(this, "¿Eliminar nivel " + id + "?") == JOptionPane.YES_OPTION) {
            try (PreparedStatement ps = conexion.prepareStatement("DELETE FROM Nivel WHERE idNivel = ?")) {
                ps.setString(1, id);
                if (ps.executeUpdate() > 0) {
                    JOptionPane.showMessageDialog(this, "Eliminado correctamente.");
                    limpiarCampos();
                    modo = null;
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo eliminar.");
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }//GEN-LAST:event_btnEliminarActionPerformed

    private void btnGrabarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGrabarActionPerformed
       if (modo == null || modo.isEmpty() || modo.equals("consulta")) {
            JOptionPane.showMessageDialog(this, "Use 'Nuevo' o 'Editar' primero.");
            return;
        }

        // Validaciones
        if (txtNombreNivel.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese el nombre del nivel.");
            return;
        }
        if (txtDescripcion.getText().trim().length() < 3) {
            JOptionPane.showMessageDialog(this, "Descripción muy corta.");
            return;
        }

        try {
            if (modo.equals("nuevo")) {
                CallableStatement cs = conexion.prepareCall("{ call dbo.registrarNivel(?, ?, ?) }");
                cs.setString(1, txtNombreNivel.getText().trim());
                cs.setString(2, txtDescripcion.getText().trim());
                cs.registerOutParameter(3, java.sql.Types.CHAR);
                cs.execute();
                
                String nuevoId = cs.getString(3);
                txtIdNivel.setText(nuevoId);
                JOptionPane.showMessageDialog(this, "Nivel creado: " + nuevoId);

            } else if (modo.equals("edicion")) {
                String sql = "UPDATE Nivel SET nombreNivel=?, descripcion=? WHERE idNivel=?";
                PreparedStatement ps = conexion.prepareStatement(sql);
                ps.setString(1, txtNombreNivel.getText().trim());
                ps.setString(2, txtDescripcion.getText().trim());
                ps.setString(3, txtIdNivel.getText().trim());
                
                if (ps.executeUpdate() > 0) JOptionPane.showMessageDialog(this, "Actualizado correctamente.");
                else JOptionPane.showMessageDialog(this, "Error al actualizar.");
            }

            habilitarCampos(false);
            modo = "consulta";

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error BD: " + e.getMessage());
        }
    }//GEN-LAST:event_btnGrabarActionPerformed

    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnEditar;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnGrabar;
    private javax.swing.JButton btnNuevo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField txtDescripcion;
    private javax.swing.JTextField txtIdNivel;
    private javax.swing.JTextField txtNombreNivel;
    // End of variables declaration//GEN-END:variables
}
