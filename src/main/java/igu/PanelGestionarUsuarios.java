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
public class PanelGestionarUsuarios extends javax.swing.JPanel {
    private Connection conexion;
    private String nivel;
    private String modo;
    /**
     * Creates new form PanelGestionarUsuarios
     */
    public PanelGestionarUsuarios(Connection conexion,String nivel) {
        initComponents();
        this.conexion=conexion;
        this.nivel=nivel;
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
        cargarNiveles();
        aplicarEstilosModernos();
        corregirDistribucion(); // Acomoda todo matemáticamente
        
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
        jLabel1.setText("GESTIÓN DE USUARIOS"); // Texto más corto y limpio

        // 2. Labels
        JLabel[] labels = {jLabel2, jLabel3, jLabel4, jLabel5, jLabel6, jLabel7};
        String[] textos = {"ID Usuario", "Nombre Usuario", "Correo", "Contraseña", "Nivel", "Fecha Creación"};
        
        for (int i = 0; i < labels.length; i++) {
            labels[i].setFont(fuenteLabels);
            labels[i].setForeground(colorTexto);
            labels[i].setText(textos[i]);
        }

        // 3. Inputs
        JTextField[] campos = {txtIdUsuario, txtUserName, txtCorreo, txtContraseña};
        javax.swing.border.Border borde = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        );

        for (JTextField txt : campos) {
            txt.setFont(fuenteCampos);
            txt.setBorder(borde);
        }
        
        // Estilo especial para ComboBox y DateChooser
        jComboBoxNiveles.setFont(fuenteCampos);
        jComboBoxNiveles.setBackground(Color.WHITE);
        jDateChooserFecha.setFont(fuenteCampos);
        jDateChooserFecha.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        // 4. Separador
        jSeparator1.setForeground(new Color(31, 78, 95));
        jSeparator1.setBackground(new Color(31, 78, 95));

        // 5. Botones
        JButton[] botones = {btnNuevo, btnBuscarUsuario, btnEditar, btnEliminar, btnGrabar};
        for (JButton btn : botones) {
            estilizarBoton(btn);
        }
        
        // Emojis
        btnNuevo.setText("📄 Nuevo");
        btnBuscarUsuario.setText("🔍"); // Icono solo
        btnEditar.setText("✏️ Editar");
        btnEliminar.setText("🗑️ Eliminar");
        btnGrabar.setText("💾 Guardar");
        
        // Tooltip para el buscar
        btnBuscarUsuario.setToolTipText("Buscar Usuario por ID");
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
        jPanel1.setLayout(null); // Desactivar layout automático

        // Título
        jLabel1.setBounds(0, 20, 640, 40);

        // Coordenadas
        int xLabel = 40;
        int xInput = 180;
        int wInput = 350; // Ancho estándar
        int h = 30; // Alto estándar
        int y = 80;
        int gap = 45; // Separación vertical

        // Fila 1: ID (+ Botón Buscar pegado)
        jLabel2.setBounds(xLabel, y, 120, h);
        txtIdUsuario.setBounds(xInput, y, 150, h);
        btnBuscarUsuario.setBounds(xInput + 160, y, 50, h); // Botón pequeño al lado

        // Fila 2: Nombre
        y += gap;
        jLabel3.setBounds(xLabel, y, 130, h);
        txtUserName.setBounds(xInput, y, wInput, h);

        // Fila 3: Correo
        y += gap;
        jLabel4.setBounds(xLabel, y, 120, h);
        txtCorreo.setBounds(xInput, y, wInput, h);

        // Fila 4: Contraseña
        y += gap;
        jLabel5.setBounds(xLabel, y, 120, h);
        txtContraseña.setBounds(xInput, y, wInput, h);

        // Fila 5: Nivel
        y += gap;
        jLabel6.setBounds(xLabel, y, 120, h);
        jComboBoxNiveles.setBounds(xInput, y, 200, h); // Combo no tan ancho

        // Fila 6: Fecha
        y += gap;
        jLabel7.setBounds(xLabel, y, 120, h);
        jDateChooserFecha.setBounds(xInput, y, 200, h);

        // Separador
        y += gap + 10;
        jSeparator1.setBounds(20, y, 600, 10);

        // Botones (Abajo)
        int yBtn = y + 20;
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
    
    private void limpiarCampos() {
        txtIdUsuario.setText("");
        txtUserName.setText("");
        txtCorreo.setText("");
        txtContraseña.setText("");
        jDateChooserFecha.setDate(new java.util.Date()); // Fecha actual por defecto
        if (jComboBoxNiveles.getItemCount() > 0) jComboBoxNiveles.setSelectedIndex(0);

    }
    


    private void habilitarCampos(boolean habilitar) {
       Color colorFondo = habilitar ? Color.WHITE : new Color(245, 245, 245);

        txtIdUsuario.setEditable(false); // ID siempre bloqueado (es automático o búsqueda)
        txtIdUsuario.setBackground(new Color(230, 230, 230));

        txtUserName.setEditable(habilitar);
        txtUserName.setBackground(colorFondo);

        txtCorreo.setEditable(habilitar);
        txtCorreo.setBackground(colorFondo);

        txtContraseña.setEditable(habilitar);
        txtContraseña.setBackground(colorFondo);

        jDateChooserFecha.setEnabled(habilitar);
        jComboBoxNiveles.setEnabled(habilitar); // Usar setEnabled para combos
        
    }


     private void cargarNiveles() {
        jComboBoxNiveles.removeAllItems();

        String sql = "SELECT NombreNivel FROM Nivel";
        try (PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // Solo añadimos la descripción, como "10%", "20%", etc.
                jComboBoxNiveles.addItem(rs.getString("NombreNivel"));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar promociones: " + e.getMessage());
        }
        
    }
     private boolean esCorreoValido(String correo) {
        return correo.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
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
        txtUserName = new javax.swing.JTextField();
        txtIdUsuario = new javax.swing.JTextField();
        txtCorreo = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtContraseña = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jComboBoxNiveles = new javax.swing.JComboBox<>();
        jLabel7 = new javax.swing.JLabel();
        jDateChooserFecha = new com.toedter.calendar.JDateChooser();
        jSeparator1 = new javax.swing.JSeparator();
        btnNuevo = new javax.swing.JButton();
        btnBuscarUsuario = new javax.swing.JButton();
        btnEditar = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        btnGrabar = new javax.swing.JButton();

        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setForeground(new java.awt.Color(0, 0, 0));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("BIENVENIDO AL MANTENIMIENTO DE USUARIOS DEL SISTEMA");

        jLabel2.setText("id Usuario");

        jLabel3.setText("Nombre de Usuario");

        jLabel4.setText("Correo");

        jLabel5.setText("contraseña");

        jLabel6.setText("Nivel");

        jComboBoxNiveles.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel7.setText("fecha de creacion");

        btnNuevo.setText("nuevo");
        btnNuevo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevoActionPerformed(evt);
            }
        });

        btnBuscarUsuario.setText("buscar");
        btnBuscarUsuario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarUsuarioActionPerformed(evt);
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
                        .addGap(53, 53, 53)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel2)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5)
                            .addComponent(jLabel6)
                            .addComponent(jLabel7))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txtIdUsuario)
                                    .addComponent(txtUserName)
                                    .addComponent(txtCorreo)
                                    .addComponent(txtContraseña, javax.swing.GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE))
                                .addGap(33, 33, 33)
                                .addComponent(btnBuscarUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jDateChooserFecha, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE)
                                .addComponent(jComboBoxNiveles, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addComponent(btnNuevo, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(37, 37, 37)
                .addComponent(btnEditar, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(42, 42, 42)
                .addComponent(btnEliminar, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(36, 36, 36)
                .addComponent(btnGrabar, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(51, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(34, 34, 34)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtIdUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnBuscarUsuario)))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtUserName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(txtCorreo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(jLabel5))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(txtContraseña, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(27, 27, 27)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jComboBoxNiveles, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(28, 28, 28)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(jDateChooserFecha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(39, 39, 39)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnNuevo)
                    .addComponent(btnEditar)
                    .addComponent(btnEliminar)
                    .addComponent(btnGrabar))
                .addContainerGap(108, Short.MAX_VALUE))
        );

        add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 640, 500));
    }// </editor-fold>//GEN-END:initComponents

    private void btnNuevoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoActionPerformed
        if (nivel.equalsIgnoreCase("administrador")) {
            limpiarCampos();
            habilitarCampos(true);
            modo = "nuevo";
            txtIdUsuario.setText("(Auto)");
        } else {
            JOptionPane.showMessageDialog(this, "Solo administradores.");
        }
    }//GEN-LAST:event_btnNuevoActionPerformed

    private void btnBuscarUsuarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarUsuarioActionPerformed
       String id = JOptionPane.showInputDialog(this, "Ingrese ID Usuario (Ej: U001):");
        if (id == null || id.trim().isEmpty()) return;

        limpiarCampos(); // Limpiamos antes de mostrar
        
        try {
            String sql = "SELECT * FROM ActorUsuario WHERE idUsuario = ?";
            PreparedStatement ps = conexion.prepareStatement(sql);
            ps.setString(1, id.trim());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                txtIdUsuario.setText(rs.getString("idUsuario"));
                txtUserName.setText(rs.getString("nameUsuario"));
                txtCorreo.setText(rs.getString("correo"));
                txtContraseña.setText(rs.getString("contraseña"));
                jDateChooserFecha.setDate(rs.getDate("fechaRegistro"));

                // Seleccionar Nivel en el Combo
                String idNivel = rs.getString("idNivel");
                seleccionarNivelPorId(idNivel);

                habilitarCampos(false);
                modo = "consulta";
                JOptionPane.showMessageDialog(this, "Usuario encontrado.");
            } else {
                JOptionPane.showMessageDialog(this, "Usuario no encontrado.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error SQL: " + e.getMessage());
        }
    }//GEN-LAST:event_btnBuscarUsuarioActionPerformed
    
    // Método auxiliar para poner el combo en la posición correcta
    private void seleccionarNivelPorId(String idNivel) {
        try {
            PreparedStatement ps = conexion.prepareStatement("SELECT nombreNivel FROM Nivel WHERE idNivel = ?");
            ps.setString(1, idNivel);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                jComboBoxNiveles.setSelectedItem(rs.getString("nombreNivel"));
            }
        } catch (SQLException e) {
            System.out.println("Error seleccionando nivel: " + e.getMessage());
        }
    }
    

    private void btnEditarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditarActionPerformed
        if (!nivel.equalsIgnoreCase("administrador")) {
            JOptionPane.showMessageDialog(this, "Acceso denegado.");
            return;
        }
        if (txtIdUsuario.getText().isEmpty() || txtIdUsuario.getText().equals("(Auto)")) {
            JOptionPane.showMessageDialog(this, "Busque un usuario primero.");
            return;
        }
        habilitarCampos(true);
        modo = "edicion";
    }//GEN-LAST:event_btnEditarActionPerformed

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarActionPerformed
        if (!nivel.equalsIgnoreCase("administrador")) {
            return;
        }

        String id = txtIdUsuario.getText().trim();
        if (id.isEmpty() || id.equals("(Auto)")) {
            JOptionPane.showMessageDialog(this, "Busque un usuario para eliminar.");
            return;
        }

        if (JOptionPane.showConfirmDialog(this, "¿Eliminar usuario " + id + "?") == JOptionPane.YES_OPTION) {
            try {
                PreparedStatement ps = conexion.prepareStatement("DELETE FROM ActorUsuario WHERE idUsuario = ?");
                ps.setString(1, id);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Eliminado.");
                limpiarCampos();
                modo = null;
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

        // Validaciones básicas
        if (txtUserName.getText().trim().isEmpty() || txtCorreo.getText().trim().isEmpty() || 
            txtContraseña.getText().trim().isEmpty() || jDateChooserFecha.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Complete todos los campos.");
            return;
        }

        if (!esCorreoValido(txtCorreo.getText().trim())) {
            JOptionPane.showMessageDialog(this, "Correo inválido.");
            return;
        }

        try {
            // Obtener ID del nivel seleccionado
            String idNivel = null;
            String nivelNombre = (String) jComboBoxNiveles.getSelectedItem();
            PreparedStatement psNivel = conexion.prepareStatement("SELECT idNivel FROM Nivel WHERE nombreNivel = ?");
            psNivel.setString(1, nivelNombre);
            ResultSet rsNivel = psNivel.executeQuery();
            if (rsNivel.next()) idNivel = rsNivel.getString("idNivel");
            else {
                JOptionPane.showMessageDialog(this, "Error obteniendo nivel.");
                return;
            }

            // GUARDAR
            if (modo.equals("nuevo")) {
                CallableStatement cs = conexion.prepareCall("{ call dbo.registrarUsuario(?, ?, ?, ?, ?, ?) }");
                cs.setString(1, txtUserName.getText().trim());
                cs.setString(2, txtCorreo.getText().trim());
                cs.setString(3, txtContraseña.getText().trim());
                cs.setDate(4, new java.sql.Date(jDateChooserFecha.getDate().getTime()));
                cs.setString(5, idNivel);
                cs.registerOutParameter(6, java.sql.Types.CHAR);
                cs.execute();
                
                String nuevoId = cs.getString(6);
                txtIdUsuario.setText(nuevoId);
                JOptionPane.showMessageDialog(this, "Usuario creado: " + nuevoId);

            } else if (modo.equals("edicion")) {
                String sql = "UPDATE ActorUsuario SET nameUsuario=?, correo=?, contraseña=?, fechaRegistro=?, idNivel=? WHERE idUsuario=?";
                PreparedStatement ps = conexion.prepareStatement(sql);
                ps.setString(1, txtUserName.getText().trim());
                ps.setString(2, txtCorreo.getText().trim());
                ps.setString(3, txtContraseña.getText().trim());
                ps.setDate(4, new java.sql.Date(jDateChooserFecha.getDate().getTime()));
                ps.setString(5, idNivel);
                ps.setString(6, txtIdUsuario.getText().trim());
                
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
    private javax.swing.JButton btnBuscarUsuario;
    private javax.swing.JButton btnEditar;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnGrabar;
    private javax.swing.JButton btnNuevo;
    private javax.swing.JComboBox<String> jComboBoxNiveles;
    private com.toedter.calendar.JDateChooser jDateChooserFecha;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField txtContraseña;
    private javax.swing.JTextField txtCorreo;
    private javax.swing.JTextField txtIdUsuario;
    private javax.swing.JTextField txtUserName;
    // End of variables declaration//GEN-END:variables
}
