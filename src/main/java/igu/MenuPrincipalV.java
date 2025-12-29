/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package igu;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import persistencia.Cconexion;

/**
 *
 * @author rafae
 */
public class MenuPrincipalV extends javax.swing.JFrame {
    private String usuarioActual;
    private String idUsuario;
    private String nivelUsuario;
    private LoginVentana ventana;
    private Connection conexion;
    Color brillo = new Color(255, 255, 255, 40);
    // --- AGREGA ESTA VARIABLE AQUÍ ---
    private javax.swing.JButton botonSeleccionado = null; 
    // ---------------------------------

    public MenuPrincipalV(String usuario, String nivel, Connection conexion) {

        initComponents();
      
    this.conexion = conexion;
    this.usuarioActual = usuario;
    this.nivelUsuario = nivel;

    // 2. Configuración de ventana
    setLocationRelativeTo(null);
    
    // 3. Cargar panel inicial
    PanelEntrada p = new PanelEntrada(usuario);
    showPanel(p);

    // 4. Aplicar estilos visuales
    personalizarBotones();
    
    // 5. SOLUCIÓN AL LAG (Pantalla Blanca):
    // Hacemos la consulta a la BD AL FINAL, después de que todo lo visual esté listo.
    // Incluso mejor, forzamos que se haga después de mostrar la ventana.
    java.awt.EventQueue.invokeLater(() -> {
        this.setVisible(true); // Mostramos la ventana primero
        // Ahora sí, consultamos la BD (El usuario ya ve la interfaz cargada)
        this.idUsuario = obtenerIdUsuarioDesdeBD(usuario);
        System.out.println("ID Usuario cargado: " + this.idUsuario);
    });
    marcarBotonComoActivo(btnInicio);
    }

    private void aplicarEfectoHover(JButton boton) {
        boton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                // Si el botón NO es el que ya está seleccionado (para no quitarle su color Cyan)
                if (boton != botonSeleccionado) {
                    // 1. Cambiamos el borde a BLANCO (o Amarillo si prefieres)
                    boton.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                            new javax.swing.border.LineBorder(Color.WHITE, 1),
                            javax.swing.BorderFactory.createEmptyBorder(5, 15, 5, 0)
                    ));
                    // 2. Opcional: Cambiar letra
                    // boton.setForeground(Color.YELLOW);
                }
                boton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // Si NO es el seleccionado, volvemos al estado invisible
                if (boton != botonSeleccionado) {
                    boton.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                            new javax.swing.border.LineBorder(new Color(0, 0, 0, 0), 1), // Volver a transparente
                            javax.swing.BorderFactory.createEmptyBorder(5, 15, 5, 0)
                    ));
                    boton.setForeground(Color.WHITE);
                }
            }
        });
    }
    private void marcarBotonComoActivo(JButton boton) {
        // 1. Limpiar el anterior (si existe)
        if (botonSeleccionado != null) {
            // Le ponemos el borde transparente de nuevo
            botonSeleccionado.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    new javax.swing.border.LineBorder(new Color(0, 0, 0, 0), 1),
                    javax.swing.BorderFactory.createEmptyBorder(5, 15, 5, 0)
            ));
            botonSeleccionado.setForeground(Color.WHITE);
        }

        // 2. Nuevo botón seleccionado
        botonSeleccionado = boton;

        // 3. Le ponemos borde CIAN (Celeste) fijo
        botonSeleccionado.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(new Color(0, 255, 255), 1),
                javax.swing.BorderFactory.createEmptyBorder(5, 15, 5, 0)
        ));
        botonSeleccionado.setForeground(new Color(0, 255, 255)); // Letra también celeste
    }

    private void personalizarBotones() {
        java.awt.Font fuenteMenu = new java.awt.Font("Segoe UI Emoji", java.awt.Font.PLAIN, 14);

        JButton[] botones = {btnInicio, btnMantenimiento, btnAlquileres, btnPagos, btnReporte, btnCerrarSesion};

        for (JButton btn : botones) {
            btn.setFont(fuenteMenu);
            btn.setForeground(Color.WHITE);
            btn.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);

            // --- TRUCO DEL BORDE INVISIBLE ---
            // Creamos un borde compuesto: 
            // 1. Línea exterior (1 pixel) transparente (new Color(0,0,0,0))
            // 2. Margen interior (EmptyBorder) para separar el texto
            javax.swing.border.Border bordeInvisible = javax.swing.BorderFactory.createCompoundBorder(
                    new javax.swing.border.LineBorder(new Color(0, 0, 0, 0), 1), // Transparente
                    javax.swing.BorderFactory.createEmptyBorder(5, 15, 5, 0) // Margen
            );
            btn.setBorder(bordeInvisible);

            // --- CONFIGURACIÓN "CERO LAG" ---
            btn.setContentAreaFilled(false); // IMPORTANTE: Nunca pintar fondo
            btn.setBorderPainted(true);      // IMPORTANTE: Permitir pintar el borde (aunque sea transparente al inicio)
            btn.setFocusPainted(false);
            btn.setOpaque(false);

            aplicarEfectoHover(btn);
        }

        // Textos...
        btnInicio.setText("🏠   INICIO");
        btnMantenimiento.setText("🛠️ MANTENIMIENTO");
        btnAlquileres.setText("🚗   ALQUILERES");
        btnPagos.setText("💰   PAGOS");
        btnReporte.setText("📊   REPORTES");
        btnCerrarSesion.setText("🚪   CERRAR SESIÓN");

        // Marcar inicio por defecto
        marcarBotonComoActivo(btnInicio);
    }


    private String obtenerIdUsuarioDesdeBD(String nombreUsuario) {
        String id = null;
        try {
            String sql = "SELECT idUsuario FROM ActorUsuario WHERE nameUsuario = ?";
            PreparedStatement ps = conexion.prepareStatement(sql);
            ps.setString(1, nombreUsuario);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                id = rs.getString("idUsuario");
            } else {
                JOptionPane.showMessageDialog(this, "No se encontró el ID para el usuario: " + nombreUsuario, "Aviso", JOptionPane.WARNING_MESSAGE);
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al obtener el ID del usuario: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return id;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        PaneldeInicio = new javax.swing.JPanel();
        jPanel1 = new igu.PanelMenuDegradado();
        btnInicio = new javax.swing.JButton();
        btnCerrarSesion = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        btnMantenimiento = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JSeparator();
        jSeparator4 = new javax.swing.JSeparator();
        btnReporte = new javax.swing.JButton();
        jSeparator5 = new javax.swing.JSeparator();
        btnAlquileres = new javax.swing.JButton();
        btnPagos = new javax.swing.JButton();
        jSeparator6 = new javax.swing.JSeparator();
        jSeparator7 = new javax.swing.JSeparator();
        PanelOriginal = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        PaneldeInicio.setBackground(new java.awt.Color(255, 255, 255));
        PaneldeInicio.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(51, 153, 255));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnInicio.setForeground(new java.awt.Color(255, 255, 255));
        btnInicio.setText("INICIO");
        btnInicio.setBorder(null);
        btnInicio.setContentAreaFilled(false);
        btnInicio.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnInicio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnInicioActionPerformed(evt);
            }
        });
        jPanel1.add(btnInicio, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 6, 170, 56));

        btnCerrarSesion.setForeground(new java.awt.Color(0, 0, 0));
        btnCerrarSesion.setText("CERRAR SESION");
        btnCerrarSesion.setBorder(null);
        btnCerrarSesion.setContentAreaFilled(false);
        btnCerrarSesion.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCerrarSesion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCerrarSesionActionPerformed(evt);
            }
        });
        jPanel1.add(btnCerrarSesion, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 436, 158, 35));
        jPanel1.add(jSeparator2, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 477, 158, 13));

        btnMantenimiento.setBackground(new java.awt.Color(204, 204, 0));
        btnMantenimiento.setForeground(new java.awt.Color(255, 255, 255));
        btnMantenimiento.setText(" MANTENIMIENTO");
        btnMantenimiento.setBorder(null);
        btnMantenimiento.setContentAreaFilled(false);
        btnMantenimiento.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnMantenimiento.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnMantenimientoMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnMantenimientoMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                btnMantenimientoMousePressed(evt);
            }
        });
        btnMantenimiento.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMantenimientoActionPerformed(evt);
            }
        });
        jPanel1.add(btnMantenimiento, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 90, 158, 40));
        jPanel1.add(jSeparator3, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 142, 158, 10));
        jPanel1.add(jSeparator4, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 74, 158, 10));

        btnReporte.setForeground(new java.awt.Color(255, 255, 255));
        btnReporte.setText("REPORTES");
        btnReporte.setBorder(null);
        btnReporte.setContentAreaFilled(false);
        btnReporte.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnReporte.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReporteActionPerformed(evt);
            }
        });
        jPanel1.add(btnReporte, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 362, 158, 56));
        jPanel1.add(jSeparator5, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 422, 158, 10));

        btnAlquileres.setForeground(new java.awt.Color(255, 255, 255));
        btnAlquileres.setText("ALQUILERES");
        btnAlquileres.setBorder(null);
        btnAlquileres.setContentAreaFilled(false);
        btnAlquileres.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAlquileres.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAlquileresActionPerformed(evt);
            }
        });
        jPanel1.add(btnAlquileres, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 158, 158, 44));

        btnPagos.setForeground(new java.awt.Color(255, 255, 255));
        btnPagos.setText("PAGOS");
        btnPagos.setBorder(null);
        btnPagos.setContentAreaFilled(false);
        btnPagos.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnPagos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPagosActionPerformed(evt);
            }
        });
        jPanel1.add(btnPagos, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 223, 158, 40));
        jPanel1.add(jSeparator6, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 269, 158, 10));
        jPanel1.add(jSeparator7, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 208, 158, 10));

        PaneldeInicio.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 170, 500));

        PanelOriginal.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout PanelOriginalLayout = new javax.swing.GroupLayout(PanelOriginal);
        PanelOriginal.setLayout(PanelOriginalLayout);
        PanelOriginalLayout.setHorizontalGroup(
            PanelOriginalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 640, Short.MAX_VALUE)
        );
        PanelOriginalLayout.setVerticalGroup(
            PanelOriginalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 500, Short.MAX_VALUE)
        );

        PaneldeInicio.add(PanelOriginal, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 0, 640, 500));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(PaneldeInicio, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(PaneldeInicio, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnMantenimientoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMantenimientoActionPerformed
        PanelMantenimiento p1= new PanelMantenimiento(conexion,usuarioActual,nivelUsuario);
        showPanel(p1);
        marcarBotonComoActivo(btnMantenimiento);
    }//GEN-LAST:event_btnMantenimientoActionPerformed

    private void btnMantenimientoMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnMantenimientoMousePressed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnMantenimientoMousePressed

    private void btnMantenimientoMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnMantenimientoMouseExited

    }//GEN-LAST:event_btnMantenimientoMouseExited

    private void btnMantenimientoMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnMantenimientoMouseEntered

    }//GEN-LAST:event_btnMantenimientoMouseEntered

    private void btnCerrarSesionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarSesionActionPerformed

        int confirm = JOptionPane.showConfirmDialog(this, "¿Deseas cerrar sesión?", "Confirmación", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // 1. Cerramos la conexión actual para liberar al servidor
                if (this.conexion != null && !this.conexion.isClosed()) {
                    this.conexion.close();
                }
            } catch (SQLException ex) {
                System.out.println("Error al cerrar la conexión: " + ex.getMessage());
            }

            // 2. Abrimos el Login nuevo (que creará su propia conexión fresca)
            LoginVentana login = new LoginVentana();
            login.setVisible(true);

            // 3. Matamos el menú
            this.dispose();
        }

    }//GEN-LAST:event_btnCerrarSesionActionPerformed

    private void btnInicioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInicioActionPerformed
        PanelEntrada p= new PanelEntrada(usuarioActual);
        showPanel(p);
        marcarBotonComoActivo(btnInicio);
    }//GEN-LAST:event_btnInicioActionPerformed

    private void btnReporteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReporteActionPerformed
       PanelReportes p= new PanelReportes(conexion);
        showPanel(p);
        marcarBotonComoActivo(btnReporte);
    }//GEN-LAST:event_btnReporteActionPerformed

    private void btnAlquileresActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAlquileresActionPerformed
        PanelAlquiler p = new PanelAlquiler(conexion,idUsuario);
        showPanel(p);
        marcarBotonComoActivo(btnAlquileres);
    }//GEN-LAST:event_btnAlquileresActionPerformed

    private void btnPagosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPagosActionPerformed
        PanelPagos p = new PanelPagos(conexion,idUsuario);
        showPanel(p);
        marcarBotonComoActivo(btnPagos);
    }//GEN-LAST:event_btnPagosActionPerformed
     void showPanel(JPanel p){
        p.setSize(640, 500);
        p.setLocation(0, 0);
        PanelOriginal.removeAll();
        PanelOriginal.add(p);
        PanelOriginal.revalidate();
        PanelOriginal.repaint();
    
    
    }
    /**
     * @param args the command line arguments
     */


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel PanelOriginal;
    private javax.swing.JPanel PaneldeInicio;
    private javax.swing.JButton btnAlquileres;
    private javax.swing.JButton btnCerrarSesion;
    private javax.swing.JButton btnInicio;
    private javax.swing.JButton btnMantenimiento;
    private javax.swing.JButton btnPagos;
    private javax.swing.JButton btnReporte;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    // End of variables declaration//GEN-END:variables
}
