/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package igu;

import java.awt.BorderLayout;
import java.awt.List;
import java.sql.Connection;
// Para el diálogo de selección de archivo
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

// Para leer archivos
import java.io.File;
import java.io.FileInputStream;

// Para JDBC
import java.sql.Connection;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;

// Para Apache POI (Excel)
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.apache.poi.ss.usermodel.Row;
import java.util.ArrayList;





/**
 *
 * @author rafae
 */
public class PanelGestionarTurista extends javax.swing.JPanel {
    private Connection conexion;
    private String modo;
    private String nivel;
    /**
     * Creates new form GestionarTurista
     */
    private void limpiarCampos() {
        txtNombre.setText("");
        txtApellidos.setText("");
        txtDni.setText("");
        txtNacionalidad.setText("");
        txtContacto.setText("");
    }
    
    private void corregirDistribucion() {
        jPanel1.setLayout(null);
        // 1. EL TÍTULO (Arriba y centrado)
        // setBounds(x, y, ancho, alto)
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setBounds(0, 20, 640, 40);

        // 2. CONFIGURACIÓN DE FILAS (Compactamos el espacio vertical)
        int xLabel = 40;       // Donde empiezan las etiquetas
        int xTxt = 150;        // Donde empiezan las cajas
        int anchoTxt = 430;    // Cajas bien anchas
        int alto = 30;         // Altura estándar
        int inicioY = 80;      // Empezamos más arriba para ganar espacio
        int separacion = 45;   // Espacio entre cada renglón

        // Fila 1: Nombre
        lblNombre.setBounds(xLabel, inicioY, 100, alto);
        txtNombre.setBounds(xTxt, inicioY, anchoTxt, alto);

        // Fila 2: Apellidos
        lblApellidos.setBounds(xLabel, inicioY + separacion, 100, alto);
        txtApellidos.setBounds(xTxt, inicioY + separacion, anchoTxt, alto);

        // Fila 3: DNI
        lblDni.setBounds(xLabel, inicioY + (separacion * 2), 100, alto);
        txtDni.setBounds(xTxt, inicioY + (separacion * 2), anchoTxt, alto);

        // Fila 4: Nacionalidad
        lblNacionalidad.setBounds(xLabel, inicioY + (separacion * 3), 100, alto);
        txtNacionalidad.setBounds(xTxt, inicioY + (separacion * 3), anchoTxt, alto);

        // Fila 5: Contacto
        lblContacto.setBounds(xLabel, inicioY + (separacion * 4), 100, alto);
        txtContacto.setBounds(xTxt, inicioY + (separacion * 4), anchoTxt, alto);

        // 3. LOS BOTONES (Todos en una sola fila ordenada)
        int yBotones = 330; // Altura perfecta debajo del formulario
        int anchoBtn = 100;
        int altoBtn = 35;
        int gap = 15; // Espacio entre botones
        int xBtn = 30; // Margen izquierdo inicial

        // Calculamos la posición de cada uno sumando el ancho + gap
        btnNuevoTurista.setBounds(xBtn, yBotones, anchoBtn, altoBtn);

        xBtn += anchoBtn + gap;
        btnBuscarturista.setBounds(xBtn, yBotones, anchoBtn, altoBtn);

        xBtn += anchoBtn + gap;
        btnEditarTurista.setBounds(xBtn, yBotones, anchoBtn, altoBtn);

        xBtn += anchoBtn + gap;
        btnEliminaTurista.setBounds(xBtn, yBotones, anchoBtn, altoBtn);

        xBtn += anchoBtn + gap;
        btnGuardarTurista.setBounds(xBtn, yBotones, anchoBtn, altoBtn);

        // 4. EL BOTÓN IMPORTAR (Abajo, separado)
        btnImportar.setBounds(30, 400, 160, 35);
    }
    
    public PanelGestionarTurista(Connection conexion,String nivel) {
        this.conexion= conexion;
        initComponents();
        
        habilitarCampos(false);
        this.nivel=nivel;
         
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
        aplicarEstilosModernos();
        corregirDistribucion();
    }
    private void aplicarEstilosModernos() {
        // COLORES Y FUENTES
        java.awt.Color colorTextoOscuro = new java.awt.Color(31, 78, 95); // Azul Petróleo
        java.awt.Font fuenteTitulo = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 24);
        java.awt.Font fuenteNormal = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14);

        // 1. TÍTULO
        jLabel1.setFont(fuenteTitulo);
        jLabel1.setForeground(colorTextoOscuro);
        jLabel1.setText("GESTIÓN DE TURISTAS");

        // 2. LABELS (Etiquetas)
        javax.swing.JLabel[] labels = {lblNombre, lblApellidos, lblDni, lblNacionalidad, lblContacto};
        for (javax.swing.JLabel lbl : labels) {
            lbl.setFont(fuenteNormal);
            lbl.setForeground(colorTextoOscuro);
        }

        // 3. BOTONES (Estilo Glass)
        javax.swing.JButton[] botones = {btnNuevoTurista, btnBuscarturista, btnEditarTurista, btnEliminaTurista, btnGuardarTurista, btnImportar};

        for (javax.swing.JButton btn : botones) {
            estilizarBoton(btn);
        }
        // --- AQUÍ ES DONDE PONES LOS ICONOS (EMOJIS) ---
        btnNuevoTurista.setText("📄 Nuevo");
        btnBuscarturista.setText("🔍 Buscar");
        btnEditarTurista.setText("✏️ Editar");
        btnEliminaTurista.setText("🗑️ Eliminar");
        btnGuardarTurista.setText("💾 Guardar");
        btnImportar.setText("📂 Importar Excel");
       
    }

    private void estilizarBoton(javax.swing.JButton btn) {
        // 1. FUENTE CORRECTA PARA EMOJIS
        // Usamos "Segoe UI Emoji" para asegurar que se vean los iconos a color
        btn.setFont(new java.awt.Font("Segoe UI Emoji", java.awt.Font.BOLD, 12));

        // Resto del código igual...
        btn.setForeground(new java.awt.Color(31, 78, 95)); // Azul oscuro

        // 2. FONDO SÓLIDO
        btn.setBackground(java.awt.Color.WHITE);

        // 3. CURSOR
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        // 4. BORDES Y PINTADO
        btn.setFocusPainted(false);
        btn.setBorderPainted(true);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);

        // 5. BORDE DE DISEÑO
        btn.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200), 1),
                javax.swing.BorderFactory.createEmptyBorder(6, 15, 6, 15)
        ));

        // 6. EFECTO HOVER
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new java.awt.Color(240, 240, 240));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(java.awt.Color.WHITE);
            }
        });
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
        btnNuevoTurista = new javax.swing.JButton();
        btnBuscarturista = new javax.swing.JButton();
        btnEditarTurista = new javax.swing.JButton();
        btnEliminaTurista = new javax.swing.JButton();
        btnGuardarTurista = new javax.swing.JButton();
        lblNombre = new javax.swing.JLabel();
        lblApellidos = new javax.swing.JLabel();
        lblDni = new javax.swing.JLabel();
        txtNombre = new javax.swing.JTextField();
        lblNacionalidad = new javax.swing.JLabel();
        lblContacto = new javax.swing.JLabel();
        txtApellidos = new javax.swing.JTextField();
        txtDni = new javax.swing.JTextField();
        txtNacionalidad = new javax.swing.JTextField();
        txtContacto = new javax.swing.JTextField();
        btnImportar = new javax.swing.JButton();

        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setForeground(new java.awt.Color(0, 0, 0));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("BIENVENIDO A LA GESTION DE TURISTAS");

        btnNuevoTurista.setText("Nuevo");
        btnNuevoTurista.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevoTuristaActionPerformed(evt);
            }
        });

        btnBuscarturista.setText("Buscar");
        btnBuscarturista.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarturistaActionPerformed(evt);
            }
        });

        btnEditarTurista.setText("Editar");
        btnEditarTurista.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditarTuristaActionPerformed(evt);
            }
        });

        btnEliminaTurista.setBackground(new java.awt.Color(255, 51, 51));
        btnEliminaTurista.setText("Eliminar");
        btnEliminaTurista.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminaTuristaActionPerformed(evt);
            }
        });

        btnGuardarTurista.setBackground(new java.awt.Color(51, 255, 51));
        btnGuardarTurista.setText("Guardar");
        btnGuardarTurista.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarTuristaActionPerformed(evt);
            }
        });

        lblNombre.setForeground(new java.awt.Color(0, 0, 0));
        lblNombre.setText("Nombre ");

        lblApellidos.setForeground(new java.awt.Color(0, 0, 0));
        lblApellidos.setText("Apellidos");

        lblDni.setForeground(new java.awt.Color(0, 0, 0));
        lblDni.setText("DNI");

        txtNombre.setBackground(new java.awt.Color(255, 255, 255));
        txtNombre.setForeground(new java.awt.Color(0, 0, 0));

        lblNacionalidad.setForeground(new java.awt.Color(0, 0, 0));
        lblNacionalidad.setText("Nacionalidad");

        lblContacto.setForeground(new java.awt.Color(0, 0, 0));
        lblContacto.setText("Contacto");

        txtApellidos.setBackground(new java.awt.Color(255, 255, 255));
        txtApellidos.setForeground(new java.awt.Color(0, 0, 0));
        txtApellidos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtApellidosActionPerformed(evt);
            }
        });

        txtDni.setBackground(new java.awt.Color(255, 255, 255));
        txtDni.setForeground(new java.awt.Color(0, 0, 0));

        txtNacionalidad.setBackground(new java.awt.Color(255, 255, 255));
        txtNacionalidad.setForeground(new java.awt.Color(0, 0, 0));

        txtContacto.setBackground(new java.awt.Color(255, 255, 255));
        txtContacto.setForeground(new java.awt.Color(0, 0, 0));

        btnImportar.setBackground(new java.awt.Color(0, 153, 0));
        btnImportar.setText("Importar");
        btnImportar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImportarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnNuevoTurista, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnBuscarturista, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(29, 29, 29)
                        .addComponent(btnEliminaTurista, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnGuardarTurista, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(23, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 268, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(23, 23, 23)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(lblApellidos)
                                            .addComponent(lblNombre)
                                            .addComponent(lblDni))
                                        .addGap(19, 19, 19))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(lblNacionalidad)
                                        .addComponent(lblContacto)))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtNombre, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 391, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtApellidos, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 391, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtDni, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 391, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtContacto, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 391, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtNacionalidad, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 391, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(btnEditarTurista, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnImportar, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblNombre, javax.swing.GroupLayout.DEFAULT_SIZE, 37, Short.MAX_VALUE)
                    .addComponent(txtNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblApellidos)
                    .addComponent(txtApellidos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDni)
                    .addComponent(txtDni, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtNacionalidad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblNacionalidad, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtContacto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblContacto))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 60, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnNuevoTurista)
                    .addComponent(btnBuscarturista, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnEliminaTurista)
                    .addComponent(btnGuardarTurista))
                .addGap(18, 18, 18)
                .addComponent(btnEditarTurista)
                .addGap(46, 46, 46)
                .addComponent(btnImportar)
                .addGap(79, 79, 79))
        );

        add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 640, 500));
    }// </editor-fold>//GEN-END:initComponents
    // 🔹 Habilita o deshabilita los campos del formulario

    private void habilitarCampos(boolean habilitar) {
        // 1. Definimos los colores
        // Si habilitar es TRUE (edición) -> Fondo Blanco brillante
        // Si habilitar es FALSE (bloqueado) -> Fondo Gris Claro
        java.awt.Color colorFondo = habilitar ? java.awt.Color.WHITE : new java.awt.Color(240, 240, 240);
        java.awt.Color colorBorde = habilitar ? new java.awt.Color(31, 78, 95) : new java.awt.Color(200, 200, 200);

        // 2. Lista de tus campos de texto
        javax.swing.JTextField[] campos = {txtNombre, txtApellidos, txtDni, txtNacionalidad, txtContacto};

        for (javax.swing.JTextField txt : campos) {
            // A. Usamos setEditable en lugar de setEnabled
            // Esto permite que el usuario pueda COPIAR el texto aunque no pueda editarlo (Mejor UX)
            txt.setEditable(habilitar);

            // B. Cambiamos el color de fondo para dar feedback visual
            txt.setBackground(colorFondo);

            // C. (Opcional) Cambiamos el borde para que se note más el estado
            txt.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createLineBorder(colorBorde, 1),
                    javax.swing.BorderFactory.createEmptyBorder(6, 10, 6, 10) // Mantenemos el relleno interno
            ));
        }
    }

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
                txtNombre.setText(rs.getString("nombre"));
                txtApellidos.setText(rs.getString("apellidos"));
                txtDni.setText(rs.getString("dni"));
                txtNacionalidad.setText(rs.getString("nacionalidad"));
                txtContacto.setText(rs.getString("contacto"));
                
                habilitarCampos(false);
                
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

    private void btnBuscarturistaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarturistaActionPerformed
        String dniBuscado = javax.swing.JOptionPane.showInputDialog(this, "Ingrese el DNI del turista a buscar:");
        if (dniBuscado != null) {
            buscarTuristaPorDni(dniBuscado.trim());
        }
    }//GEN-LAST:event_btnBuscarturistaActionPerformed

    private void btnEditarTuristaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditarTuristaActionPerformed
        if (!txtDni.getText().isEmpty()) {
            habilitarCampos(true);
        modo = "edicion";
        }else{
        JOptionPane.showMessageDialog(null, "buscar un turista primero");
        
        }
        
    }//GEN-LAST:event_btnEditarTuristaActionPerformed

    private void txtApellidosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtApellidosActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtApellidosActionPerformed

    private void btnNuevoTuristaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoTuristaActionPerformed
       limpiarCampos();
       modo="nuevo";
        habilitarCampos(true);
    }//GEN-LAST:event_btnNuevoTuristaActionPerformed

    private void btnGuardarTuristaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarTuristaActionPerformed
        // 1️⃣ Validar que se haya elegido un modo
        if (modo == null || modo.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Debe seleccionar una acción: Nuevo o Editar");
            return;
        }

        // 2️⃣ Capturar los valores del formulario
        String nombre = txtNombre.getText().trim();
        String apellidos = txtApellidos.getText().trim();
        String dni = txtDni.getText().trim();
        String nacionalidad = txtNacionalidad.getText().trim();
        String contacto = txtContacto.getText().trim();

        // 3️⃣ Validar campos obligatorios
        if (nombre.isEmpty() || apellidos.isEmpty() || dni.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Debe completar al menos Nombre, Apellidos y DNI.");
            return;
        }

        // 4️⃣ Ejecutar acción según el modo
        if (modo.equals("edicion")) {
            // 🔹 UPDATE (editar)
            String sql = "UPDATE Turista SET nombre=?, apellidos=?, nacionalidad=?, contacto=? WHERE dni=?";
            try (java.sql.PreparedStatement ps = conexion.prepareStatement(sql)) {
                ps.setString(1, nombre);
                ps.setString(2, apellidos);
                ps.setString(3, nacionalidad);
                ps.setString(4, contacto);
                ps.setString(5, dni);

                int filas = ps.executeUpdate();
                if (filas > 0) {
                    javax.swing.JOptionPane.showMessageDialog(this, "Turista actualizado correctamente.");
                    limpiarCampos();
                    habilitarCampos(false);
                    modo = "";
                } else {
                    javax.swing.JOptionPane.showMessageDialog(this, "No se encontró un turista con ese DNI.");
                }

            } catch (Exception e) {
                javax.swing.JOptionPane.showMessageDialog(this, "Error al actualizar turista: " + e.getMessage());
            }

        } else if (modo.equals("nuevo")) {
            // 🔹 INSERT mediante procedimiento almacenado registrarTurista
            try (java.sql.CallableStatement cs = conexion.prepareCall("{CALL registrarTurista(?, ?, ?, ?, ?)}")) {
                cs.setString(1, nombre);
                cs.setString(2, apellidos);
                cs.setString(3, dni);
                cs.setString(4, nacionalidad.isEmpty() ? null : nacionalidad);
                cs.setString(5, contacto.isEmpty() ? null : contacto);

                cs.execute();
                javax.swing.JOptionPane.showMessageDialog(this, "Turista registrado correctamente mediante SP.");

                limpiarCampos();
                habilitarCampos(false);
                modo = "";

            } catch (java.sql.SQLIntegrityConstraintViolationException e) {
                javax.swing.JOptionPane.showMessageDialog(this, "Ya existe un turista con ese DNI.");
            } catch (Exception e) {
                javax.swing.JOptionPane.showMessageDialog(this, "Error al registrar turista: " + e.getMessage());
            }
        } else {
            javax.swing.JOptionPane.showMessageDialog(this, "Modo desconocido: " + modo);
        }
    }//GEN-LAST:event_btnGuardarTuristaActionPerformed

    private void btnEliminaTuristaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminaTuristaActionPerformed
        if (nivel.equalsIgnoreCase("administrador")) {

            String dni = txtDni.getText().trim();

            if (dni.isEmpty()) {
                javax.swing.JOptionPane.showMessageDialog(this, "Debe ingresar o buscar un turista para eliminar.");
                return;
            }

            int confirmacion = javax.swing.JOptionPane.showConfirmDialog(
                    this,
                    "¿Está seguro de eliminar al turista con DNI " + dni + "?",
                    "Confirmar eliminación",
                    javax.swing.JOptionPane.YES_NO_OPTION
            );

            if (confirmacion == javax.swing.JOptionPane.YES_OPTION) {
                String sql = "DELETE FROM Turista WHERE dni = ?";
                try (java.sql.PreparedStatement ps = conexion.prepareStatement(sql)) {
                    ps.setString(1, dni);
                    int filas = ps.executeUpdate();

                    if (filas > 0) {
                        javax.swing.JOptionPane.showMessageDialog(this, "Turista eliminado correctamente.");
                        limpiarCampos();
                        habilitarCampos(false);
                        modo = "";
                    } else {
                        javax.swing.JOptionPane.showMessageDialog(this, "No se encontró un turista con ese DNI.");
                    }
                } catch (Exception e) {
                    javax.swing.JOptionPane.showMessageDialog(this, "Error al eliminar turista: " + e.getMessage());
                }
            }
        }
    }//GEN-LAST:event_btnEliminaTuristaActionPerformed

    private void btnImportarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImportarActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecciona el archivo Excel de turistas");
        int seleccion = fileChooser.showOpenDialog(null);

        if (seleccion == JFileChooser.APPROVE_OPTION) {
            File archivo = fileChooser.getSelectedFile();
            try (FileInputStream fis = new FileInputStream(archivo); Workbook workbook = new XSSFWorkbook(fis)) {

                Sheet sheet = workbook.getSheetAt(0); // primera hoja
                String sql = "{call dbo.registrarTurista(?, ?, ?, ?, ?)}";
                CallableStatement cs = conexion.prepareCall(sql);

                DataFormatter formatter = new DataFormatter();

                int importados = 0;
                int duplicados = 0;
                int incompletos = 0;
                ArrayList<Row> bloque = new ArrayList<>();

                int contadorBloque = 0;

                // Agrupar filas en bloques de 100
                for (Row row : sheet) {
                    if (row.getRowNum() == 0) {
                        continue; // saltar encabezado
                    }
                    bloque.add(row);
                    contadorBloque++;

                    if (contadorBloque == 100) {
                        int[] resultados = procesarBloqueOptimizado(bloque, cs, formatter);
                        importados += resultados[0];
                        duplicados += resultados[1];
                        incompletos += resultados[2];

                        bloque.clear();
                        contadorBloque = 0;
                    }
                }

                // Procesar bloque restante
                if (!bloque.isEmpty()) {
                    int[] resultados = procesarBloqueOptimizado(bloque, cs, formatter);
                    importados += resultados[0];
                    duplicados += resultados[1];
                    incompletos += resultados[2];
                }

                cs.close();

                JOptionPane.showMessageDialog(null, "Importación finalizada!\n"
                        + "Registros importados: " + importados + "\n"
                        + "Duplicados ignorados: " + duplicados + "\n"
                        + "Registros incompletos ignorados: " + incompletos);

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error al importar: " + e.getMessage());
            }
        }
    }//GEN-LAST:event_btnImportarActionPerformed

    private int[] procesarBloqueOptimizado(ArrayList<Row> bloque, CallableStatement cs, DataFormatter formatter) throws SQLException {
        int importados = 0;
        int duplicados = 0;
        int incompletos = 0;

        // Limpiar y validar filas antes de batch
        ArrayList<Row> filasValidas = new ArrayList<>();
        for (Row row : bloque) {
            String nombre = row.getCell(0) != null ? formatter.formatCellValue(row.getCell(0)).trim() : "";
            String apellidos = row.getCell(1) != null ? formatter.formatCellValue(row.getCell(1)).trim() : "";
            String dni = row.getCell(2) != null ? formatter.formatCellValue(row.getCell(2)).trim() : "";
            String nacionalidad = row.getCell(3) != null ? formatter.formatCellValue(row.getCell(3)).trim() : null;
            String contacto = row.getCell(4) != null ? formatter.formatCellValue(row.getCell(4)).trim() : null;

            // Limpiar espacios y saltos
            nombre = nombre.replaceAll("\\s+", " ").trim();
            apellidos = apellidos.replaceAll("\\s+", " ").trim();
            dni = dni.replaceAll("\\s+", "").trim();

            // Validar obligatorios
            if (nombre.isEmpty() || apellidos.isEmpty() || dni.isEmpty()) {
                incompletos++;
                continue;
            }

            // Verificar duplicado
            try (PreparedStatement psCheck = cs.getConnection().prepareStatement(
                    "SELECT COUNT(*) FROM Turista WHERE dni = ?")) {
                psCheck.setString(1, dni);
                ResultSet rs = psCheck.executeQuery();
                rs.next();
                if (rs.getInt(1) > 0) {
                    duplicados++;
                    continue;
                }
            }

            // Almacenar fila válida en batch
            cs.setString(1, nombre);
            cs.setString(2, apellidos);
            cs.setString(3, dni);
            cs.setString(4, nacionalidad);
            cs.setString(5, contacto);
            cs.addBatch();
            filasValidas.add(row);
        }

        // Ejecutar batch
        try {
            cs.executeBatch();
            importados += filasValidas.size();
        } catch (SQLException e) {
            // Si falla el batch, insertar fila por fila
            for (Row row : filasValidas) {
                try {
                    String nombre = row.getCell(0) != null ? formatter.formatCellValue(row.getCell(0)).trim() : "";
                    String apellidos = row.getCell(1) != null ? formatter.formatCellValue(row.getCell(1)).trim() : "";
                    String dni = row.getCell(2) != null ? formatter.formatCellValue(row.getCell(2)).trim() : "";
                    String nacionalidad = row.getCell(3) != null ? formatter.formatCellValue(row.getCell(3)).trim() : null;
                    String contacto = row.getCell(4) != null ? formatter.formatCellValue(row.getCell(4)).trim() : null;

                    cs.setString(1, nombre);
                    cs.setString(2, apellidos);
                    cs.setString(3, dni);
                    cs.setString(4, nacionalidad);
                    cs.setString(5, contacto);
                    cs.execute();
                    importados++;
                } catch (SQLException exFila) {
                    exFila.printStackTrace(); // log si falla fila individual
                }
            }
        }

        return new int[]{importados, duplicados, incompletos};
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuscarturista;
    private javax.swing.JButton btnEditarTurista;
    private javax.swing.JButton btnEliminaTurista;
    private javax.swing.JButton btnGuardarTurista;
    private javax.swing.JButton btnImportar;
    private javax.swing.JButton btnNuevoTurista;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lblApellidos;
    private javax.swing.JLabel lblContacto;
    private javax.swing.JLabel lblDni;
    private javax.swing.JLabel lblNacionalidad;
    private javax.swing.JLabel lblNombre;
    private javax.swing.JTextField txtApellidos;
    private javax.swing.JTextField txtContacto;
    private javax.swing.JTextField txtDni;
    private javax.swing.JTextField txtNacionalidad;
    private javax.swing.JTextField txtNombre;
    // End of variables declaration//GEN-END:variables
}
