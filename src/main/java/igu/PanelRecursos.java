/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package igu;

import java.awt.Color;
import java.awt.Font;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 *
 * @author rafae
 */
public class PanelRecursos extends javax.swing.JPanel {

    private Connection conexion;

    /**
     * Creates new form PanelRecursos
     */
    public PanelRecursos(Connection conexion) {
        initComponents();
        this.conexion = conexion;
        // --- CORRECCIÓN: CAMBIAR EL LAYOUT ---
        
        // 1. Cambiamos el diseño de NetBeans (GroupLayout) a BorderLayout
        // Esto nos permite usar NORTH (para el título) y CENTER (para la tabla)
        jPanel1.setLayout(new java.awt.BorderLayout());

        // 2. IMPORTANTE: Al cambiar el layout, el JScrollPane (la tabla) puede perderse.
        // Lo volvemos a agregar explícitamente al CENTRO para que ocupe el espacio sobrante.
        jPanel1.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        // --- FIN CORRECCIÓN ---

        // --- NUEVO: APLICAR DISEÑO ---
        // Estos métodos modifican lo que NetBeans creó
        aplicarFondoDegradado();

        // Agregamos el título por código
        javax.swing.JLabel titulo = new javax.swing.JLabel("LISTADO DE RECURSOS DISPONIBLES");
        titulo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 24));
        titulo.setForeground(new java.awt.Color(31, 78, 95)); // Tu azul oscuro
        titulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        titulo.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 0, 10, 0)); 
        
        // Lo añadimos al panel principal (al Norte)
        jPanel1.add(titulo, java.awt.BorderLayout.NORTH);
        
        estilizarTabla();

        // Cargar datos
        cargarDatosRecursos();

    }
    // 1. Método para meter el degradado detrás de la tabla
    private void aplicarFondoDegradado() {
        PanelDegradado fondo = new PanelDegradado();
        fondo.setLayout(new java.awt.BorderLayout());

        // Hacemos transparente el panel gris de NetBeans
        jPanel1.setOpaque(false);

        // Metemos el panel de la tabla DENTRO del degradado
        fondo.add(jPanel1, java.awt.BorderLayout.CENTER);

        // Reemplazamos todo en el panel principal
        this.setLayout(new java.awt.BorderLayout());
        this.removeAll();
        this.add(fondo, java.awt.BorderLayout.CENTER);

        this.revalidate();
        this.repaint();
    }

// 2. Método para que la tabla se vea moderna
    private void estilizarTabla() {
      // A. FUENTE Y FILAS
        tblRecursos.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tblRecursos.setRowHeight(30); // Filas más altas
        tblRecursos.setGridColor(new Color(230, 230, 230));
        tblRecursos.setShowVerticalLines(false); // Solo líneas horizontales
        tblRecursos.setIntercellSpacing(new java.awt.Dimension(0, 0));

        // B. ENCABEZADO (HEADER) AZUL
        JTableHeader header = tblRecursos.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(31, 78, 95)); // Tu Azul Petróleo
        header.setForeground(Color.WHITE);
        header.setOpaque(true);
        
        // ---> AQUÍ ESTÁ EL BLOQUEO DE COLUMNAS <---
        header.setReorderingAllowed(false); // IMPIDE MOVER COLUMNAS
        // header.setResizingAllowed(false); // DESCOMENTA SI QUIERES IMPEDIR CAMBIAR EL ANCHO

        // C. COLORES ALTERNADOS (Efecto Cebra)
        tblRecursos.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(
                    javax.swing.JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                
                java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                this.setHorizontalAlignment(javax.swing.SwingConstants.CENTER); // Centrar texto
                
                if (isSelected) {
                    c.setBackground(new Color(162, 211, 224)); // Celeste selección
                    c.setForeground(Color.BLACK);
                } else {
                    // Filas pares blancas, impares gris muy suave
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });

        // D. FONDO BLANCO DETRÁS DE LA TABLA
        jScrollPane1.getViewport().setBackground(Color.WHITE);
        jScrollPane1.setBorder(BorderFactory.createEmptyBorder()); // Quitar borde feo
        
        // E. MARGEN ALREDEDOR
        jPanel1.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    }

    private void cargarDatosRecursos() {
      // 1. Definimos las columnas
        String[] columnas = {"ID", "Tipo", "Descripción", "Tarifa/Hora", "Estado", "Ubicación"};

        // 2. Creamos un modelo "inteligente" que bloquea la EDICIÓN (Doble clic)
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // <--- ESTO BLOQUEA LA EDICIÓN DE CELDAS
            }
        };

        // 3. Cargamos los datos desde la BD
        String sql = "SELECT idRecursos, tipo, descripcion, tarifaHora, estado, ubicacion FROM Recursos ORDER BY tipo, idRecursos";

        try (PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                modelo.addRow(new Object[]{
                    rs.getString("idRecursos"),
                    rs.getString("tipo"),
                    rs.getString("descripcion"),
                    rs.getBigDecimal("tarifaHora"),
                    rs.getString("estado"),
                    rs.getString("ubicacion")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }

        // 4. Asignamos este modelo nuevo a la tabla
        tblRecursos.setModel(modelo);
        
        // 5. Ajustamos anchos
        ajustarAnchoColumnas();
    }

    private void ajustarAnchoColumnas() {
      // Indices: 0=ID, 1=Tipo, 2=Descrip, 3=Tarifa, 4=Estado, 5=Ubi
        if (tblRecursos.getColumnCount() > 0) {
            tblRecursos.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID pequeño
            tblRecursos.getColumnModel().getColumn(1).setPreferredWidth(100); // Tipo mediano
            tblRecursos.getColumnModel().getColumn(2).setPreferredWidth(200); // Descripción GRANDE
            tblRecursos.getColumnModel().getColumn(3).setPreferredWidth(80);  // Tarifa pequeña
            tblRecursos.getColumnModel().getColumn(4).setPreferredWidth(90);  // Estado mediano
            tblRecursos.getColumnModel().getColumn(5).setPreferredWidth(80);  // Ubicación pequeña
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
        jScrollPane1 = new javax.swing.JScrollPane();
        tblRecursos = new javax.swing.JTable();

        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(204, 204, 204));

        tblRecursos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "idRecursos", "tipo", "descripcion", "tarifaHora", "estado", "ubicacion"
            }
        ));
        jScrollPane1.setViewportView(tblRecursos);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 640, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
        );

        add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 640, 500));
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblRecursos;
    // End of variables declaration//GEN-END:variables
}
