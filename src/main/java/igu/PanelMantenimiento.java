/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package igu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.sql.Connection;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *
 * @author rafae
 */
public class PanelMantenimiento extends javax.swing.JPanel {
    private Connection conexion;
    private String usuario;
    private String nivel;
    /**
     * Creates new form PanelMantenimiento
     */
    public PanelMantenimiento(Connection conexion, String usuario, String nivel) {
        initComponents();
        jLabel1.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 24));
        jLabel1.setForeground(new java.awt.Color(31, 78, 95));
        jLabel1.setText("PANEL DE CONTROL");
        // Usamos HTML para subrayar
        jLabel1.setText("<html><u>PANEL DE CONTROL</u></html>");
        jLabel1.setForeground(new java.awt.Color(31, 78, 95));
// Añádele una sombra o contorno negro si el fondo es muy claro en esa parte
        PanelDegradado fondo = new PanelDegradado();

// MUY IMPORTANTE: dejar layout por defecto (BorderLayout)
        fondo.setLayout(new java.awt.BorderLayout());

// Pasar jPanel1 dentro del panel degradado
        PanelMant.setOpaque(false);
        fondo.add(PanelMant, BorderLayout.CENTER);

// Ahora reemplazas en el contenedor padre
        remove(PanelMant);
        add(fondo, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 640, 500));

// refrescar
        revalidate();
        repaint();

        this.conexion = conexion;
        this.usuario=usuario;
        this.nivel=nivel;
        personalizarBotones();
        
    }

    private void personalizarBotones() {
        // Fuente moderna y un poco más grande
        java.awt.Font fuenteTitulo = new java.awt.Font("Segoe UI Emoji", java.awt.Font.BOLD, 14);

        // Array para aplicar estilo a todos de golpe
        javax.swing.JButton[] botones = {btnGestionarTuristas, btnGestionarRecursos, btnGestionarPromos, btnGestionarUsuarios, btnGestionarNiv};

        // Colores del tema (Azul Playa)
        java.awt.Color colorFondoBtn = new java.awt.Color(255, 255, 255, 200); // Blanco semi-transparente
        java.awt.Color colorTexto = new java.awt.Color(31, 78, 95); // Azul Petróleo (mismo del menú)

        for (javax.swing.JButton btn : botones) {
            btn.setFont(fuenteTitulo);
            btn.setForeground(colorTexto);
            btn.setBackground(colorFondoBtn);

            // ESTILO MODERNO (FLAT)
            btn.setFocusPainted(false);
            btn.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255), 2), // Borde blanco
                    javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10) // Margen interno
            ));

            // Hacemos que el icono esté ARRIBA del texto
            btn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
            btn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

            // Cursor de mano
            btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

            // Efecto Hover (Sencillo)
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    btn.setBackground(new java.awt.Color(255, 255, 255)); // Blanco puro al pasar mouse
                }

                public void mouseExited(java.awt.event.MouseEvent evt) {
                    btn.setBackground(colorFondoBtn); // Vuelve a semi-transparente
                }
            });
        }

        // TEXTOS (Emojis más grandes como "Iconos")
        // Nota: Si puedes, usa iconos reales (.png), pero los emojis funcionan rápido.
        // Usamos HTML para forzar saltos de línea si el texto es largo
        btnGestionarTuristas.setText("<html><center>"
                + "<font size='6'>" + "🧍‍♂️" + "</font><br>"
                + "<nobr>" + "TURISTAS " + "</nobr>" // <--- ESTO EVITA QUE LA 'S' BAJE
                + "</center></html>");
         btnGestionarRecursos.setText("<html><center>"
                + "<font size='6'>" + "🛞" + "</font><br>"
                + "<nobr>" + "RECURSOS " + "</nobr>" // <--- ESTO EVITA QUE LA 'S' BAJE
                + "</center></html>");
        btnGestionarPromos.setText("<html><center>"
                + "<font size='6'>" + "🎟" + "</font><br>"
                + "<nobr>" + "PROMOS " + "</nobr>" // <--- ESTO EVITA QUE LA 'S' BAJE
                + "</center></html>");
        btnGestionarUsuarios.setText("<html><center>"
                + "<font size='6'>" + "👥" + "</font><br>"
                + "<nobr>" + "USUARIOS " + "</nobr>" // <--- ESTO EVITA QUE LA 'S' BAJE
                + "</center></html>");
        btnGestionarNiv.setText("<html><center>"
                + "<font size='6'>" + "📈" + "</font><br>"
                + "<nobr>" + "NIVELES " + "</nobr>" // <--- ESTO EVITA QUE LA 'S' BAJE
                + "</center></html>");
        
      
       
        
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        PanelMant = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        btnGestionarTuristas = new javax.swing.JButton();
        btnGestionarRecursos = new javax.swing.JButton();
        btnGestionarPromos = new javax.swing.JButton();
        btnGestionarUsuarios = new javax.swing.JButton();
        btnGestionarNiv = new javax.swing.JButton();

        setBackground(new java.awt.Color(204, 204, 204));
        setPreferredSize(new java.awt.Dimension(640, 500));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        PanelMant.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setBackground(new java.awt.Color(0, 0, 0));
        jLabel1.setFont(new java.awt.Font("DialogInput", 0, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 0, 0));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("BIENVENIDO AL PANEL DE MANTENIMIENTO");

        btnGestionarTuristas.setText("Gestionar Turistas");
        btnGestionarTuristas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGestionarTuristasActionPerformed(evt);
            }
        });

        btnGestionarRecursos.setText("GestionarRecursos");
        btnGestionarRecursos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGestionarRecursosActionPerformed(evt);
            }
        });

        btnGestionarPromos.setText("jButton1");
        btnGestionarPromos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGestionarPromosActionPerformed(evt);
            }
        });

        btnGestionarUsuarios.setText("jButton1");
        btnGestionarUsuarios.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGestionarUsuariosActionPerformed(evt);
            }
        });

        btnGestionarNiv.setText("jButton1");
        btnGestionarNiv.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGestionarNivActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout PanelMantLayout = new javax.swing.GroupLayout(PanelMant);
        PanelMant.setLayout(PanelMantLayout);
        PanelMantLayout.setHorizontalGroup(
            PanelMantLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(PanelMantLayout.createSequentialGroup()
                .addGap(47, 47, 47)
                .addComponent(btnGestionarTuristas, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 28, Short.MAX_VALUE)
                .addComponent(btnGestionarRecursos, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(33, 33, 33)
                .addComponent(btnGestionarPromos, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(31, 31, 31))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PanelMantLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnGestionarUsuarios, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(48, 48, 48)
                .addComponent(btnGestionarNiv, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(128, 128, 128))
        );
        PanelMantLayout.setVerticalGroup(
            PanelMantLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelMantLayout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addComponent(jLabel1)
                .addGap(91, 91, 91)
                .addGroup(PanelMantLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnGestionarTuristas, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnGestionarRecursos, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnGestionarPromos, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(62, 62, 62)
                .addGroup(PanelMantLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnGestionarUsuarios, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnGestionarNiv, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(110, Short.MAX_VALUE))
        );

        add(PanelMant, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 640, 500));
    }// </editor-fold>//GEN-END:initComponents

    private void btnGestionarTuristasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGestionarTuristasActionPerformed
          PanelGestionarTurista panelTuristas = new PanelGestionarTurista(conexion,nivel);
        showPanelMant(panelTuristas);
    }//GEN-LAST:event_btnGestionarTuristasActionPerformed

    private void btnGestionarPromosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGestionarPromosActionPerformed
          PanelPromociones p = new PanelPromociones(conexion, nivel);
        showPanelMant(p);
    }//GEN-LAST:event_btnGestionarPromosActionPerformed

    private void btnGestionarRecursosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGestionarRecursosActionPerformed
          PanelRecursos2 pRecurso = new PanelRecursos2(conexion,nivel);
        showPanelMant(pRecurso);
    }//GEN-LAST:event_btnGestionarRecursosActionPerformed

    private void btnGestionarUsuariosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGestionarUsuariosActionPerformed
          if (nivel.equalsIgnoreCase("administrador")) {
            PanelGestionarUsuarios p = new PanelGestionarUsuarios(conexion, nivel);
            showPanelMant(p);
        }else{
            JOptionPane.showMessageDialog(null,"opcion solo para administrador");
        }
    }//GEN-LAST:event_btnGestionarUsuariosActionPerformed

    private void btnGestionarNivActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGestionarNivActionPerformed
        if (nivel.equalsIgnoreCase("administrador")) {
            PanelNiveles p = new PanelNiveles(conexion);
            showPanelMant(p);
        }else{
            JOptionPane.showMessageDialog(null,"opcion solo para administrador");
        }
    }//GEN-LAST:event_btnGestionarNivActionPerformed
    void showPanelMant(JPanel p){
        p.setSize(640, 500);
        p.setLocation(0, 0);
        PanelMant.removeAll();
        PanelMant.add(p);
        PanelMant.revalidate();
        PanelMant.repaint();
    
    
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel PanelMant;
    private javax.swing.JButton btnGestionarNiv;
    private javax.swing.JButton btnGestionarPromos;
    private javax.swing.JButton btnGestionarRecursos;
    private javax.swing.JButton btnGestionarTuristas;
    private javax.swing.JButton btnGestionarUsuarios;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
}
