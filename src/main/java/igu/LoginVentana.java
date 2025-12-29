/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package igu;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import persistencia.Cconexion;

/**
 *
 * @author rafae
 */
public class LoginVentana extends javax.swing.JFrame {

    private Cconexion conexionBD;

    /**
     * Creates new form LoginFinal
     */
    public LoginVentana() {
        conexionBD = new Cconexion();
        initComponents();
        setLocationRelativeTo(null);
        cargarImagenEnLabel("/img/fotoPlaya.png", city);
        aplicarEstilos();
        
        configurarTeclaEnter();



    }

    private void cargarImagenEnLabel(String ruta, JLabel label) {
        ImageIcon iconoOriginal = new ImageIcon(getClass().getResource(ruta));
        Image imagen = getScaledImage(iconoOriginal.getImage(), label.getWidth(), label.getHeight());
        label.setIcon(new ImageIcon(imagen));
    }

    private Image getScaledImage(Image srcImg, int w, int h) {
        BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resizedImg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(srcImg, 0, 0, w, h, null);
        g2.dispose();
        return resizedImg;
    }
    private void configurarTeclaEnter() {
        // Acción que simula el click en el botón "INGRESAR"
        java.awt.event.ActionListener accionEnter = new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                botonIngresar.doClick(); // <--- Esto es lo que hace la magia
            }
        };

        // Le decimos a las cajas de texto: "Si te dan Enter, ejecuta esa acción"
        txtUsuario.addActionListener(accionEnter);
        jPasswordField1.addActionListener(accionEnter);
    }

    private void aplicarEstilos() {

        Color AZUL = new Color(0, 134, 190);
        Color FONDO = new Color(245, 247, 250);

        // Fondo general
        Bg.setBackground(FONDO);

        // Títulos
        jSesion.setFont(new Font("Segoe UI", Font.BOLD, 24));
        jSesion.setForeground(Color.BLACK);

        jUsuario.setFont(new Font("Segoe UI", Font.BOLD, 13));
        jPassword.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // --- ESTILIZAR CAMPO DE USUARIO ---
        txtUsuario.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUsuario.setForeground(new Color(60, 60, 60)); // Color de la letra gris oscuro
        txtUsuario.setCaretColor(AZUL); // Color de la barrita que parpadea

        // 1. Quitar el fondo blanco (hacerlo transparente)
        txtUsuario.setBackground(new Color(0, 0, 0, 0)); // El 4to valor es Alpha (transparencia)
        txtUsuario.setOpaque(false); // Importante para que Swing no pinte el rectángulo

        // 2. Quitar el borde predeterminado (ese recuadro gris o 3D)
        txtUsuario.setBorder(null);

        // --- ESTILIZAR CAMPO DE CONTRASEÑA ---
        jPasswordField1.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        jPasswordField1.setForeground(new Color(60, 60, 60));
        jPasswordField1.setCaretColor(AZUL);

        // Lo mismo para el password
        jPasswordField1.setBackground(new Color(0, 0, 0, 0));
        jPasswordField1.setOpaque(false);
        jPasswordField1.setBorder(null);

        // --- SEPARADORES (Las líneas debajo) ---
        // Asegúrate de que tus separadores tengan el color correcto para que resalten
        jSeparator1.setForeground(AZUL); // O Color.BLACK si prefieres
        jSeparator1.setBackground(AZUL); // A veces necesario dependiendo del LAF

        jSeparator2.setForeground(AZUL);
        jSeparator2.setBackground(AZUL);
        // Botones
        estiloBoton(botonIngresar, AZUL, Color.WHITE);
        estiloBoton(btnSalir, new Color(200, 200, 200), Color.BLACK);

        // Enter para ingresar
        getRootPane().setDefaultButton(botonIngresar);
    }

    private void estiloBoton(JButton btn, Color fondo, Color texto) {
        btn.setBackground(fondo);
        btn.setForeground(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        hover(botonIngresar, new Color(0, 134, 190), new Color(0, 160, 220));
        hover(btnSalir, new Color(200, 200, 200), new Color(180, 180, 180));

    }

    private void hover(JButton btn, Color normal, Color hover) {
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(hover);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(normal);
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

        Bg = new javax.swing.JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(245, 247, 250), 
                    0, getHeight(), new Color(210, 235, 245)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        jLabel1 = new javax.swing.JLabel();
        nombre = new javax.swing.JLabel();
        logo = new javax.swing.JLabel();
        city = new javax.swing.JLabel();
        LOGO = new javax.swing.JLabel();
        jSesion = new javax.swing.JLabel();
        jUsuario = new javax.swing.JLabel();
        txtUsuario = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        jPassword = new javax.swing.JLabel();
        jPasswordField1 = new javax.swing.JPasswordField();
        jSeparator2 = new javax.swing.JSeparator();
        botonIngresar = new javax.swing.JButton();
        btnSalir = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        Bg.setBackground(new java.awt.Color(255, 255, 255));
        Bg.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("SansSerif", 3, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 0, 0));
        jLabel1.setText("Piura");
        Bg.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(670, 340, -1, -1));

        nombre.setBackground(new java.awt.Color(255, 255, 255));
        nombre.setFont(new java.awt.Font("SansSerif", 3, 18)); // NOI18N
        nombre.setForeground(new java.awt.Color(0, 0, 0));
        nombre.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        nombre.setText("Alquileres Turísticos del Norte");
        Bg.add(nombre, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 280, 280, 90));

        logo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        logo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/logoPlaya (1).png"))); // NOI18N
        Bg.add(logo, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 10, 280, 160));

        city.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/fotoPlaya.png"))); // NOI18N
        Bg.add(city, new org.netbeans.lib.awtextra.AbsoluteConstraints(544, 0, 300, 500));

        LOGO.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/favicon.png"))); // NOI18N
        Bg.add(LOGO, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, 50, 80));

        jSesion.setFont(new java.awt.Font("Roboto", 1, 24)); // NOI18N
        jSesion.setForeground(new java.awt.Color(0, 0, 0));
        jSesion.setText("INICIAR SESION");
        jSesion.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        Bg.add(jSesion, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 90, 200, 80));

        jUsuario.setFont(new java.awt.Font("Roboto", 1, 14)); // NOI18N
        jUsuario.setForeground(new java.awt.Color(0, 0, 0));
        jUsuario.setText("USUARIO");
        Bg.add(jUsuario, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 170, 100, 40));

        txtUsuario.setBackground(new java.awt.Color(255, 255, 255));
        txtUsuario.setForeground(new java.awt.Color(153, 153, 153));
        txtUsuario.setBorder(null);
        txtUsuario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtUsuarioActionPerformed(evt);
            }
        });
        Bg.add(txtUsuario, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 220, 400, -1));
        Bg.add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 250, 380, 20));

        jPassword.setBackground(new java.awt.Color(255, 255, 255));
        jPassword.setFont(new java.awt.Font("Roboto", 1, 14)); // NOI18N
        jPassword.setForeground(new java.awt.Color(0, 0, 0));
        jPassword.setText("CONTRASEÑA");
        Bg.add(jPassword, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 270, 220, 50));

        jPasswordField1.setBackground(new java.awt.Color(255, 255, 255));
        jPasswordField1.setFont(new java.awt.Font("Dialog", 0, 14)); // NOI18N
        jPasswordField1.setBorder(null);
        jPasswordField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jPasswordField1ActionPerformed(evt);
            }
        });
        Bg.add(jPasswordField1, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 320, 380, -1));
        Bg.add(jSeparator2, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 350, 380, -1));

        botonIngresar.setBackground(new java.awt.Color(0, 134, 190));
        botonIngresar.setForeground(new java.awt.Color(255, 255, 255));
        botonIngresar.setText("INGRESAR");
        botonIngresar.setContentAreaFilled(false);
        botonIngresar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        botonIngresar.setOpaque(true);
        botonIngresar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonIngresarActionPerformed(evt);
            }
        });
        Bg.add(botonIngresar, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 390, 100, 40));

        btnSalir.setBackground(new java.awt.Color(0, 134, 190));
        btnSalir.setFont(new java.awt.Font("Dialog", 0, 14)); // NOI18N
        btnSalir.setForeground(new java.awt.Color(255, 255, 255));
        btnSalir.setText("SALIR");
        btnSalir.setContentAreaFilled(false);
        btnSalir.setOpaque(true);
        btnSalir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSalirActionPerformed(evt);
            }
        });
        Bg.add(btnSalir, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 390, 100, 40));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(Bg, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(Bg, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private void verificarUsuario() {
        String usuario = txtUsuario.getText().trim();
        String contraseña = new String(jPasswordField1.getPassword()).trim();

        if (usuario.isEmpty() || contraseña.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar usuario y contraseña.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Connection conn = conexionBD.conectar();
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "No hay conexión con la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // 🔹 Consulta con JOIN a la tabla Nivel
            String sql = """
            SELECT a.nameUsuario, a.contraseña, n.nombreNivel
            FROM ActorUsuario a
            INNER JOIN Nivel n ON a.idNivel = n.idNivel
            WHERE a.nameUsuario = ? AND a.contraseña = ?
        """;

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, usuario);
            ps.setString(2, contraseña);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String nivel = rs.getString("nombreNivel");

                JOptionPane.showMessageDialog(this, "Bienvenido " + usuario + " (" + nivel + ")!");

                // 🔹 Pasar también el nivel al menú principal
                MenuPrincipalV menu = new MenuPrincipalV(usuario, nivel, conn);
                menu.setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Usuario o contraseña incorrectos.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al verificar usuario: " + e.getMessage());
        }
    }

    private void txtUsuarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtUsuarioActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtUsuarioActionPerformed

    private void botonIngresarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonIngresarActionPerformed
          verificarUsuario();
    }//GEN-LAST:event_botonIngresarActionPerformed

    private void jPasswordField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPasswordField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jPasswordField1ActionPerformed

    private void btnSalirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSalirActionPerformed
        System.exit(0);
    }//GEN-LAST:event_btnSalirActionPerformed

    /**
     * @param args the command line arguments
     */
   
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel Bg;
    private javax.swing.JLabel LOGO;
    private javax.swing.JButton botonIngresar;
    private javax.swing.JButton btnSalir;
    private javax.swing.JLabel city;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jPassword;
    private javax.swing.JPasswordField jPasswordField1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel jSesion;
    private javax.swing.JLabel jUsuario;
    private javax.swing.JLabel logo;
    private javax.swing.JLabel nombre;
    private javax.swing.JTextField txtUsuario;
    // End of variables declaration//GEN-END:variables
}
