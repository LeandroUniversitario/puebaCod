/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package igu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
/**
 *
 * @author rafae
 */
public class VentanaPromociones extends javax.swing.JFrame {
    private Connection conexion;
    /**
     * Creates new form VentanaPromociones
     */
    public VentanaPromociones(Connection conexion) {
        initComponents();
        this.conexion=conexion;
        // 1. CONFIGURACIÓN BÁSICA DE LA VENTANA
        this.setTitle("Listado de Promociones");
        this.setSize(800, 500); // Un tamaño más decente por defecto
        this.setLocationRelativeTo(null); // Centrar en pantalla
        this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        // 2. APLICAR EL DISEÑO (Fondo y Título)
        aplicarFondoDegradado();

        // 3. ESTILIZAR LA TABLA (Igual que en Recursos)
        estilizarTabla();

        // 4. CARGAR DATOS
        cargarDatosPromociones();
    }
    
    private void aplicarFondoDegradado() {
        // Creamos el panel degradado
        PanelDegradado fondo = new PanelDegradado();
        fondo.setLayout(new BorderLayout());

        // TÍTULO
        JLabel titulo = new JLabel("LISTADO DE PROMOCIONES ACTIVAS");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titulo.setForeground(new Color(31, 78, 95)); // Azul Petróleo
        titulo.setHorizontalAlignment(SwingConstants.CENTER);
        // Margen del título: (Arriba, Izq, Abajo, Der)
        titulo.setBorder(BorderFactory.createEmptyBorder(20, 0, 15, 0));

        // Hacemos transparentes los paneles viejos
        jPanel1.setOpaque(false);
        jPanel2.setOpaque(false);

        // --- AQUÍ ESTÁ EL TRUCO PARA VER EL DEGRADADO A LOS LADOS ---
        
        // 1. Hacemos transparente el ScrollPane para ver el fondo en los márgenes
        jScrollPane1.setOpaque(false);
        
        // 2. Definimos el margen (hueco) alrededor de la tabla
        // (Arriba=0, Izquierda=50, Abajo=30, Derecha=50)
        jScrollPane1.setBorder(BorderFactory.createEmptyBorder(0, 50, 30, 50));
        
        // 3. Importante: Pintamos de BLANCO el fondo interno donde van las filas
        // Esto arregla el "gris" si hay pocas filas
        jScrollPane1.getViewport().setBackground(Color.WHITE);

        // AGREGAR AL PANEL DEGRADADO
        fondo.add(titulo, BorderLayout.NORTH);
        fondo.add(jScrollPane1, BorderLayout.CENTER);

        // Reemplazamos el contenedor principal
        this.setContentPane(fondo);
        this.validate();
    }

    private void estilizarTabla() {
      // A. FUENTE Y FILAS
        tblPromociones.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tblPromociones.setRowHeight(30);
        tblPromociones.setGridColor(new Color(230, 230, 230));
        tblPromociones.setShowVerticalLines(false);
        tblPromociones.setIntercellSpacing(new java.awt.Dimension(0, 0));

        // B. ENCABEZADO (HEADER)
        JTableHeader header = tblPromociones.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(31, 78, 95)); // Azul Petróleo
        header.setForeground(Color.WHITE);
        header.setOpaque(true);
        header.setReorderingAllowed(false);

        // C. RENDERER (COLORES Y CENTRADO)
        tblPromociones.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(
                    javax.swing.JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {

                java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                this.setHorizontalAlignment(SwingConstants.CENTER);

                if (isSelected) {
                    c.setBackground(new Color(162, 211, 224)); // Celeste selección
                    c.setForeground(Color.BLACK);
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });
    }

     private void cargarDatosPromociones() {
       // Modelo que NO permite edición
        DefaultTableModel modelo = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Definimos columnas manualmente para asegurar orden
        modelo.addColumn("ID");
        modelo.addColumn("Descripción");
        modelo.addColumn("Tipo");
        modelo.addColumn("Condición (Horas)");

        try {
            String sql = "SELECT idPromocion, descripcion, tipo, condicionHoras FROM Promocion";
            Statement st = conexion.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                Object[] fila = {
                    rs.getString("idPromocion"),
                    rs.getString("descripcion"),
                    rs.getString("tipo"),
                    rs.getInt("condicionHoras") // Usamos getInt para que no salga "5.0"
                };
                modelo.addRow(fila);
            }

            tblPromociones.setModel(modelo);
            
            // Ajustar anchos de columna (Opcional, pero se ve mejor)
            if (tblPromociones.getColumnCount() > 0) {
                tblPromociones.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
                tblPromociones.getColumnModel().getColumn(1).setPreferredWidth(250); // Descripción larga
                tblPromociones.getColumnModel().getColumn(2).setPreferredWidth(100); // Tipo
                tblPromociones.getColumnModel().getColumn(3).setPreferredWidth(50);  // Horas
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar: " + e.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
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
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblPromociones = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel2.setBackground(new java.awt.Color(204, 204, 204));

        tblPromociones.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "idPromocion", "descripcion ", "tipo", "condicionHora"
            }
        ));
        jScrollPane1.setViewportView(tblPromociones);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 196, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jPanel1.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 600, 190));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblPromociones;
    // End of variables declaration//GEN-END:variables
}
