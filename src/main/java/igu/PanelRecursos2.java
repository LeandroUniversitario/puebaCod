/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package igu;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 *
 * @author rafae
 */
public class PanelRecursos2 extends javax.swing.JPanel {
    private Connection conexion;
    private String nivel;
    private String modo;
    private boolean cargando = true;

    /**
     * Creates new form PanelRecursos2
     */
    public PanelRecursos2(Connection conexion, String nivel) {
        this.conexion = conexion;
        this.nivel = nivel;
        initComponents();
        // --- 1. INTEGRAR FONDO DEGRADADO ---
        PanelDegradado fondo = new PanelDegradado();
        fondo.setLayout(new java.awt.BorderLayout());

        // Hacemos transparente el panel "bg" para ver el degradado
        bg.setOpaque(false);
        fondo.add(bg, java.awt.BorderLayout.CENTER);

        // Reemplazamos en el panel principal
        this.setLayout(new java.awt.BorderLayout());
        this.removeAll();
        this.add(fondo, java.awt.BorderLayout.CENTER);

        this.revalidate();
        this.repaint();
        // --- 2. LOGICA DE NEGOCIO ---
        cargando = true;
        cargarTiposRecursos();
        cargarEstados();
        limpiarCampos();
        cargando = false;

// --- 3. DISEÑO VISUAL (La Magia) ---
        aplicarEstilosModernos();
        corregirDistribucion(); // Acomoda todo matemáticamente

        // Aplicamos el bloqueo visual inicial
        habilitarCampos(false);

    }
    private void aplicarEstilosModernos() {
        // Definir Colores y Fuentes
        java.awt.Color colorTexto = new java.awt.Color(31, 78, 95); // Azul Petróleo
        java.awt.Font fuenteTitulo = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 24);
        java.awt.Font fuenteLabels = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14);
        java.awt.Font fuenteCampos = new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14);

        // 1. Título
        jLabel1.setFont(fuenteTitulo);
        jLabel1.setForeground(colorTexto);
        jLabel1.setText("GESTIÓN DE RECURSOS");

        // 2. Etiquetas (Labels)
        javax.swing.JLabel[] labels = {lblId, lblTipo, lblDescrip, lblTarifa, lblEstado, lblUbi};
        for (javax.swing.JLabel lbl : labels) {
            lbl.setFont(fuenteLabels);
            lbl.setForeground(colorTexto);
        }

        // Arreglamos textos (Capitalización)
        lblDescrip.setText("Descripción");
        lblUbi.setText("Ubicación");
        lblTarifa.setText("Tarifa / Hora");

        // 3. Estilo de Campos de Texto (Bordes suaves)
        javax.swing.border.Border bordeCampo = javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)),
                javax.swing.BorderFactory.createEmptyBorder(5, 8, 5, 8)
        );

        javax.swing.JTextField[] campos = {txtId, txtTarifa, txtUbi};
        for (javax.swing.JTextField txt : campos) {
            txt.setFont(fuenteCampos);
            txt.setBorder(bordeCampo);
        }

        // Estilo especial para el Área de Texto y Combos
        txtAreaDescrip.setFont(fuenteCampos);
        txtAreaDescrip.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Padding interno
        jScrollPane1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200))); // Borde externo

        jComboBoxTipo.setFont(fuenteCampos);
        jComboBoxEstado.setFont(fuenteCampos);

        // 4. Botones con Iconos
        javax.swing.JButton[] botones = {btnNuevo, btnBuscar, btnEditar, btnEliminar, btnGrabar, btnVer};
        for (javax.swing.JButton btn : botones) {
            estilizarBoton(btn);
        }

        // Asignar Emojis y Textos
        btnNuevo.setText("📄 Nuevo");
        btnBuscar.setText("🔍 Buscar");
        btnEditar.setText("✏️ Editar");
        btnEliminar.setText("🗑️ Eliminar");
        btnGrabar.setText("💾 Guardar");
        btnVer.setText("📋 Ver Lista");
    }

// Método auxiliar para botones blancos y limpios
    private void estilizarBoton(javax.swing.JButton btn) {
        btn.setFont(new java.awt.Font("Segoe UI Emoji", java.awt.Font.BOLD, 12));
        btn.setForeground(new java.awt.Color(31, 78, 95));
        btn.setBackground(java.awt.Color.WHITE);
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.setBorderPainted(true);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        btn.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200), 1),
                javax.swing.BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
    }

    private void corregirDistribucion() {
        // ¡IMPORTANTE! Desactivar el layout automático de NetBeans
        bg.setLayout(null);

        // Título
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setBounds(0, 20, 640, 40);

        // Configuración de columnas
        int xLabel = 40;
        int xInput = 160;
        int anchoInput = 420;
        int altoStd = 30;

        int y = 80;   // Altura inicial
        int gap = 45; // Separación entre filas

        // Fila 1: ID
        lblId.setBounds(xLabel, y, 100, altoStd);
        txtId.setBounds(xInput, y, 150, altoStd); // ID más corto

        // Fila 2: Tipo
        y += gap;
        lblTipo.setBounds(xLabel, y, 100, altoStd);
        jComboBoxTipo.setBounds(xInput, y, anchoInput, altoStd);

        // Fila 3: Descripción (Más alta)
        y += gap;
        lblDescrip.setBounds(xLabel, y, 100, altoStd);
        jScrollPane1.setBounds(xInput, y, anchoInput, 60); // 60px de alto

        // Fila 4: Tarifa (Ajustamos Y porque la descripción ocupó más espacio)
        y += gap + 25;
        lblTarifa.setBounds(xLabel, y, 100, altoStd);
        txtTarifa.setBounds(xInput, y, anchoInput, altoStd);

        // Fila 5: Estado
        y += gap;
        lblEstado.setBounds(xLabel, y, 100, altoStd);
        jComboBoxEstado.setBounds(xInput, y, anchoInput, altoStd);

        // Fila 6: Ubicación
        y += gap;
        lblUbi.setBounds(xLabel, y, 100, altoStd);
        txtUbi.setBounds(xInput, y, anchoInput, altoStd);

        // --- NUEVO: LÍNEA SEPARADORA DE BOTONES ---
        javax.swing.JPanel lineaBotones = new javax.swing.JPanel();
        lineaBotones.setBackground(new java.awt.Color(31, 78, 95)); // Azul Petróleo
        // La ponemos en Y=370 (Justo entre el campo y los botones)
        lineaBotones.setBounds(40, 370, 560, 2); 
        bg.add(lineaBotones);
        // ---------------------
        // --- BOTONES ---
        int yBtn = 380;
        int wBtn = 100;
        int hBtn = 35;
        int gapBtn = 10;
        int xBtn = 40; // Margen izquierdo

        btnNuevo.setBounds(xBtn, yBtn, wBtn, hBtn);
        xBtn += wBtn + gapBtn;
        btnBuscar.setBounds(xBtn, yBtn, wBtn, hBtn);
        xBtn += wBtn + gapBtn;
        btnEditar.setBounds(xBtn, yBtn, wBtn, hBtn);
        xBtn += wBtn + gapBtn;
        btnEliminar.setBounds(xBtn, yBtn, wBtn, hBtn);
        xBtn += wBtn + gapBtn;
        btnGrabar.setBounds(xBtn, yBtn, wBtn, hBtn);

        // Botón Ver Lista (Abajo a la derecha)
        btnVer.setBounds(460, 440, 120, 35);
    }
   
    private void habilitarCampos(boolean habilitar) {
        java.awt.Color colorFondo = habilitar ? java.awt.Color.WHITE : new java.awt.Color(240, 240, 240);

        // ID siempre bloqueado (es autogenerado normalmente)
        txtId.setEditable(false);
        txtId.setBackground(new java.awt.Color(230, 230, 230));

        // Campos de Texto
        javax.swing.JTextField[] campos = {txtTarifa, txtUbi};
        for (javax.swing.JTextField txt : campos) {
            txt.setEditable(habilitar);
            txt.setBackground(colorFondo);
        }

        // Área de texto
        txtAreaDescrip.setEditable(habilitar);
        txtAreaDescrip.setBackground(colorFondo);

        // Combos
        jComboBoxTipo.setEnabled(habilitar);
        jComboBoxEstado.setEnabled(habilitar);
    }
    private void cargarTiposRecursos() {
        jComboBoxTipo.removeAllItems();
        jComboBoxTipo.addItem("➕ Nuevo tipo...");

        String sql = "SELECT DISTINCT tipo FROM Recursos ORDER BY tipo";

        try (PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                jComboBoxTipo.addItem(rs.getString("tipo"));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar tipos: " + e.getMessage());
        }

    }
    private void cargarEstados() {
        jComboBoxEstado.removeAllItems();
        jComboBoxEstado.addItem("Disponible");
        jComboBoxEstado.addItem("Ocupado");
        jComboBoxEstado.addItem("Mantenimiento");
    }


    private void limpiarCampos() {
        txtId.setText("");
        txtAreaDescrip.setText("");
        txtTarifa.setText("");
        txtUbi.setText("");

        cargando = true;
        jComboBoxTipo.setSelectedIndex(0); // Primer item seguro
        jComboBoxEstado.setSelectedIndex(0);
        cargando = false;
    }

   // private void habilitarCampos(boolean habilitar) {
     //   txtId.setEnabled(false); // SIEMPRE false (ID automático)
       // jComboBoxTipo.setEnabled(habilitar);
        //txtAreaDescrip.setEnabled(habilitar);
        //txtTarifa.setEnabled(habilitar);
        //jComboBoxEstado.setEnabled(habilitar);
        //txtUbi.setEnabled(habilitar);

    //}

    private void buscarRecurso(String idBuscar) {
        if (idBuscar == null || idBuscar.trim().isEmpty()) {
            return;
        }

        try {
            String sql = "SELECT * FROM Recursos WHERE idRecursos = ?";
            PreparedStatement ps = conexion.prepareStatement(sql);
            ps.setString(1, idBuscar.trim());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                txtId.setText(rs.getString("idRecursos"));
                jComboBoxTipo.setSelectedItem(rs.getString("tipo"));
                txtAreaDescrip.setText(rs.getString("descripcion"));
                txtTarifa.setText(rs.getString("tarifaHora"));
                jComboBoxEstado.setSelectedItem(rs.getString("estado"));
                txtUbi.setText(rs.getString("ubicacion"));

                habilitarCampos(false);
                JOptionPane.showMessageDialog(this, "Recurso encontrado.");
            } else {
                JOptionPane.showMessageDialog(this, "No se encontró el recurso.");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al buscar recurso: " + e.getMessage());
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

        bg = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        lblTipo = new javax.swing.JLabel();
        lblDescrip = new javax.swing.JLabel();
        lblTarifa = new javax.swing.JLabel();
        lblEstado = new javax.swing.JLabel();
        lblUbi = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtAreaDescrip = new javax.swing.JTextArea();
        txtTarifa = new javax.swing.JTextField();
        txtUbi = new javax.swing.JTextField();
        btnNuevo = new javax.swing.JButton();
        btnBuscar = new javax.swing.JButton();
        btnEditar = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        btnGrabar = new javax.swing.JButton();
        btnVer = new javax.swing.JButton();
        lblId = new javax.swing.JLabel();
        txtId = new javax.swing.JTextField();
        jComboBoxTipo = new javax.swing.JComboBox<>();
        jComboBoxEstado = new javax.swing.JComboBox<>();

        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        bg.setBackground(new java.awt.Color(255, 255, 255));
        bg.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setForeground(new java.awt.Color(0, 0, 0));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("BIENVENIDO A LA GESTION DE RECURSOS");
        bg.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 10, 280, -1));

        lblTipo.setForeground(new java.awt.Color(0, 0, 0));
        lblTipo.setText("Tipo");
        bg.add(lblTipo, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 90, 93, -1));

        lblDescrip.setForeground(new java.awt.Color(0, 0, 0));
        lblDescrip.setText("Descripcion");
        bg.add(lblDescrip, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 130, 93, -1));

        lblTarifa.setForeground(new java.awt.Color(0, 0, 0));
        lblTarifa.setText("Tarifa hora");
        bg.add(lblTarifa, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 180, 93, -1));

        lblEstado.setForeground(new java.awt.Color(0, 0, 0));
        lblEstado.setText("Estado");
        bg.add(lblEstado, new org.netbeans.lib.awtextra.AbsoluteConstraints(25, 227, 93, -1));

        lblUbi.setForeground(new java.awt.Color(0, 0, 0));
        lblUbi.setText("Ubicacion");
        bg.add(lblUbi, new org.netbeans.lib.awtextra.AbsoluteConstraints(25, 273, 93, -1));

        txtAreaDescrip.setColumns(20);
        txtAreaDescrip.setRows(5);
        jScrollPane1.setViewportView(txtAreaDescrip);

        bg.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 120, 387, 41));

        txtTarifa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtTarifaActionPerformed(evt);
            }
        });
        bg.add(txtTarifa, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 180, 387, -1));
        bg.add(txtUbi, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 270, 387, -1));

        btnNuevo.setText("Nuevo");
        btnNuevo.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNuevo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevoActionPerformed(evt);
            }
        });
        bg.add(btnNuevo, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 370, 80, -1));

        btnBuscar.setText("buscar");
        btnBuscar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnBuscar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarActionPerformed(evt);
            }
        });
        bg.add(btnBuscar, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 370, 90, -1));

        btnEditar.setText("Editar");
        btnEditar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnEditar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditarActionPerformed(evt);
            }
        });
        bg.add(btnEditar, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 370, 90, -1));

        btnEliminar.setBackground(new java.awt.Color(255, 51, 51));
        btnEliminar.setText("Eliminar");
        btnEliminar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnEliminar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarActionPerformed(evt);
            }
        });
        bg.add(btnEliminar, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 370, 100, -1));

        btnGrabar.setBackground(new java.awt.Color(0, 255, 0));
        btnGrabar.setText("Grabar");
        btnGrabar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnGrabar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGrabarActionPerformed(evt);
            }
        });
        bg.add(btnGrabar, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 370, 120, -1));

        btnVer.setText("ver recursos");
        btnVer.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnVer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVerActionPerformed(evt);
            }
        });
        bg.add(btnVer, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 450, 140, 30));

        lblId.setForeground(new java.awt.Color(0, 0, 0));
        lblId.setText("Id");
        bg.add(lblId, new org.netbeans.lib.awtextra.AbsoluteConstraints(25, 42, -1, -1));
        bg.add(txtId, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 40, 387, -1));

        jComboBoxTipo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBoxTipo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxTipoActionPerformed(evt);
            }
        });
        bg.add(jComboBoxTipo, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 80, 210, 30));

        jComboBoxEstado.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBoxEstado.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxEstadoActionPerformed(evt);
            }
        });
        bg.add(jComboBoxEstado, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 230, 220, -1));

        add(bg, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 640, 500));
    }// </editor-fold>//GEN-END:initComponents

    private void txtTarifaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTarifaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtTarifaActionPerformed

    private void btnNuevoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoActionPerformed
        if (nivel.equalsIgnoreCase("administrador")) {
            txtId.setEnabled(false);
            limpiarCampos();
            modo = "nuevo";
            habilitarCampos(true);
        } else {
            JOptionPane.showMessageDialog(null, "opcion solo para administradores");
        }
    }//GEN-LAST:event_btnNuevoActionPerformed

    private void btnBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarActionPerformed
        String recursoBuscado = javax.swing.JOptionPane.showInputDialog(this, "Ingrese el ID del recurso a buscar (ej: R001)");
        if (recursoBuscado != null) {
            buscarRecurso(recursoBuscado.trim());
        }
    }//GEN-LAST:event_btnBuscarActionPerformed

    private void btnEditarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditarActionPerformed
      if (nivel.equalsIgnoreCase("administrador")) {
        // --- VALIDACIÓN NUEVA ---
        if (txtId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Primero debe buscar un recurso para editarlo.");
            return;
        }
        // ------------------------

        habilitarCampos(true);
        txtId.setEnabled(false); // El ID nunca se debe editar en SQL relacional
        modo = "edicion";
    } else {
        JOptionPane.showMessageDialog(null, "opcion solo para administradores");
    }

    }//GEN-LAST:event_btnEditarActionPerformed

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarActionPerformed
        if (nivel.equalsIgnoreCase("administrador")) {

            String idEliminar = JOptionPane.showInputDialog(this, "Ingrese el ID del recurso a eliminar (ej: R001):");

            // Validar entrada
            if (idEliminar == null || idEliminar.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Debe ingresar un ID de recurso válido.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Confirmación
            int confirmar = JOptionPane.showConfirmDialog(this,
                    "¿Está seguro que desea eliminar el recurso con ID " + idEliminar + "?",
                    "Confirmar eliminación",
                    JOptionPane.YES_NO_OPTION);

            if (confirmar != JOptionPane.YES_OPTION) {
                return; // usuario canceló
            }

            try {
                // Verificar si existe el recurso antes de eliminarlo
                String sqlCheck = "SELECT COUNT(*) FROM Recursos WHERE idRecursos = ?";
                PreparedStatement psCheck = conexion.prepareStatement(sqlCheck);
                psCheck.setString(1, idEliminar.trim());
                ResultSet rs = psCheck.executeQuery();
                rs.next();
                int count = rs.getInt(1);

                if (count == 0) {
                    JOptionPane.showMessageDialog(this, "No existe ningún recurso con el ID especificado.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Eliminar registro
                String sqlDelete = "DELETE FROM Recursos WHERE idRecursos = ?";
                PreparedStatement psDelete = conexion.prepareStatement(sqlDelete);
                psDelete.setString(1, idEliminar.trim());
                int filas = psDelete.executeUpdate();

                if (filas > 0) {
                    JOptionPane.showMessageDialog(this, "Recurso eliminado correctamente.");
                    limpiarCampos();
                    habilitarCampos(false);
                    modo = null;
                    cargarTiposRecursos();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo eliminar el recurso.", "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error al eliminar recurso: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, "opcion solo para administradores");
        }
    }//GEN-LAST:event_btnEliminarActionPerformed

    private void btnGrabarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGrabarActionPerformed
        
        if (modo == null) {
            JOptionPane.showMessageDialog(this, "Seleccione primero si es un registro nuevo o edición.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String tipoSeleccionado = jComboBoxTipo.getSelectedItem().toString();

        if (tipoSeleccionado.equals("➕ Nuevo tipo...")) {
            JOptionPane.showMessageDialog(this,
                    "Debe ingresar un tipo de vehículo válido.",
                    "Validación",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // --- Validaciones ---
        if (jComboBoxTipo.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un tipo.");
            return;
        }

        if (txtTarifa.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese tarifa.");
            return;
        }

        if (jComboBoxEstado.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione estado.");
            return;
        }
        String estado = jComboBoxEstado.getSelectedItem().toString();

        // --- AQUÍ ESTÁ LA SOLUCIÓN 3 ---
        // Regla: Un recurso NUEVO no puede nacer "Ocupado".
        if (modo.equals("nuevo") && estado.equalsIgnoreCase("Ocupado")) {
            JOptionPane.showMessageDialog(this,
                    "Un recurso nuevo debe registrarse como 'Disponible' o 'Mantenimiento'.\n"
                    + "No puede crearse directamente como 'Ocupado'.",
                    "Validación de Estado",
                    JOptionPane.WARNING_MESSAGE);
            return; // Detenemos el guardado
        }


        try {
            double tarifa = Double.parseDouble(txtTarifa.getText().trim());
            

            if (tarifa <= 0) {
                JOptionPane.showMessageDialog(this, "La tarifa debe ser mayor que 0.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            

            if (modo.equals("nuevo")) {
                // Procedimiento almacenado para registrar
                CallableStatement cs = conexion.prepareCall("{call registrarRecurso(?, ?, ?, ?, ?)}");
                cs.setString(1, jComboBoxTipo.getSelectedItem().toString());
                cs.setString(2, txtAreaDescrip.getText().trim());
                cs.setBigDecimal(3, new BigDecimal(txtTarifa.getText().trim()));
                cs.setString(4, jComboBoxEstado.getSelectedItem().toString());
                cs.setString(5, txtUbi.getText().trim());
                cs.execute();


                JOptionPane.showMessageDialog(this, "Recurso registrado correctamente.");
                limpiarCampos();
                habilitarCampos(false);

            } else if (modo.equals("edicion")) {
                // Aquí puedes hacer un UPDATE
                String sql = """
                    UPDATE Recursos 
                    SET tipo=?, descripcion=?, tarifaHora=?, estado=?, ubicacion=?
                    WHERE idRecursos=?
                    """;

                PreparedStatement ps = conexion.prepareStatement(sql);
                ps.setString(1, jComboBoxTipo.getSelectedItem().toString());
                ps.setString(2, txtAreaDescrip.getText().trim());
                ps.setBigDecimal(3, new java.math.BigDecimal(txtTarifa.getText().trim()));
                ps.setString(4, jComboBoxEstado.getSelectedItem().toString());
                ps.setString(5, txtUbi.getText().trim());
                ps.setString(6, txtId.getText().trim());
                

                int filas = ps.executeUpdate();

                if (filas > 0) {
                    JOptionPane.showMessageDialog(this, "Recurso actualizado correctamente.");
                    limpiarCampos();
                    habilitarCampos(false);
                } else {
                    JOptionPane.showMessageDialog(this, "No se encontró ningún recurso con el ID especificado.", "Error", JOptionPane.ERROR_MESSAGE);
                }

            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Error: asegúrese de ingresar valores numéricos válidos en tarifa", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar recurso: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnGrabarActionPerformed

    private void btnVerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVerActionPerformed
        ventanaRecursos v=new ventanaRecursos(conexion);
        v.setVisible(true);
        v.setLocationRelativeTo(null);
    }//GEN-LAST:event_btnVerActionPerformed

    private void jComboBoxTipoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxTipoActionPerformed
        if (cargando) {
            return;                // ⛔ sistema
        }
        if (!jComboBoxTipo.isEnabled()) {
            return; // ⛔ no editable
        }
        if (jComboBoxTipo.getSelectedItem() == null) {
            return;
        }

        String tipo = jComboBoxTipo.getSelectedItem().toString();

        // 👉 NUEVO TIPO
        if (tipo.equals("➕ Nuevo tipo...")) {
            
            
            String nuevoTipo = JOptionPane.showInputDialog(
                    this,
                    "Ingrese el nombre del nuevo tipo de vehículo:"
            );

            if (nuevoTipo == null || nuevoTipo.trim().isEmpty()) {
                cargando = true;
                jComboBoxTipo.setSelectedIndex(0);
                cargando = false;
                return;
            }

            String tipoNormalizado = nuevoTipo.trim();

            for (int i = 0; i < jComboBoxTipo.getItemCount(); i++) {
                if (jComboBoxTipo.getItemAt(i).equalsIgnoreCase(tipoNormalizado)) {
                    JOptionPane.showMessageDialog(this,
                            "Ese tipo ya existe.");
                    cargando = true;
                    jComboBoxTipo.setSelectedItem(jComboBoxTipo.getItemAt(i));
                    cargando = false;
                    return;
                }
            }

            txtAreaDescrip.setText("");
            txtTarifa.setText("");

            cargando = true;

            try {
                // Estas dos líneas DISPARAN eventos. Al tener cargando=true, 
                // al inicio de este método el 'if(cargando) return' nos protege.
                jComboBoxTipo.addItem(tipoNormalizado);
                jComboBoxTipo.setSelectedItem(tipoNormalizado);

            } finally {
                // 🟢 SEMÁFORO VERDE: "Listo Java, ya terminé. Vuelve a escuchar eventos."
                // El 'finally' asegura que esto se ejecute SIEMPRE, incluso si hay error.
                cargando = false;
            }

            JOptionPane.showMessageDialog(this, "Nuevo tipo agregado. Complete los datos.");
            return;
        }

        if (modo != null && modo.equals("nuevo")) {
            
            String sql = "SELECT TOP 1 descripcion, tarifaHora FROM Recursos WHERE tipo = ?";

            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                ps.setString(1, tipo);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    txtAreaDescrip.setText(rs.getString("descripcion"));
                    txtTarifa.setText(rs.getBigDecimal("tarifaHora").toString());
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }//GEN-LAST:event_jComboBoxTipoActionPerformed

    private void jComboBoxEstadoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxEstadoActionPerformed
       // Solo dejamos la validación de nulos si quieres, pero QUITAMOS el btnGrabar.setEnabled(false)
    if (jComboBoxEstado.getSelectedItem() == null) {
        return;
    }
    
    // Aquí puedes poner un mensaje informativo si quieres, 
    // pero NO bloquees el botón.
    }//GEN-LAST:event_jComboBoxEstadoActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bg;
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnEditar;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnGrabar;
    private javax.swing.JButton btnNuevo;
    private javax.swing.JButton btnVer;
    private javax.swing.JComboBox<String> jComboBoxEstado;
    private javax.swing.JComboBox<String> jComboBoxTipo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblDescrip;
    private javax.swing.JLabel lblEstado;
    private javax.swing.JLabel lblId;
    private javax.swing.JLabel lblTarifa;
    private javax.swing.JLabel lblTipo;
    private javax.swing.JLabel lblUbi;
    private javax.swing.JTextArea txtAreaDescrip;
    private javax.swing.JTextField txtId;
    private javax.swing.JTextField txtTarifa;
    private javax.swing.JTextField txtUbi;
    // End of variables declaration//GEN-END:variables
}
