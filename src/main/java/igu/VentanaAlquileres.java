/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package igu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
public class VentanaAlquileres extends javax.swing.JFrame {
    private Connection conexion;
    private DefaultTableModel modeloTabla; // Variable de clase para el modelo
    /**
     * Creates new form VentanaAlquileres
     */
    public VentanaAlquileres(Connection conexion) {
        initComponents();
        this.conexion = conexion;
        // 1. Aplicar el diseño visual (Fondo degradado y Título)
        aplicarDiseñoVisual();

        // 2. Configurar la tabla con el estilo moderno (Colores, Fuentes, Alineación)
        estilizarTabla();

        // 3. Cargar los datos completos
        cargarAlquileres();

        // Configuraciones de la ventana
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Historial de Alquileres");
        setSize(1100, 650); // Ventana grande
        setLocationRelativeTo(null); // Centrar en pantalla
    }

    // --- MÉTODO 1: DISEÑO GENERAL (Fondo y Título) ---
    private void aplicarDiseñoVisual() {
        // Usamos tu clase PanelDegradado
        PanelDegradado panelFondo = new PanelDegradado();
        panelFondo.setLayout(new BorderLayout());
        
        // Añadimos un margen externo invisible
        panelFondo.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // TÍTULO: Lo creamos por código para no depender del diseñador
        JLabel titulo = new JLabel("HISTORIAL DE ALQUILERES");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titulo.setForeground(new Color(31, 78, 95)); // Azul Petróleo
        titulo.setHorizontalAlignment(SwingConstants.CENTER);
        titulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0)); // Espacio abajo del título

        // Añadimos el título al NORTE del panel degradado
        panelFondo.add(titulo, BorderLayout.NORTH);

        // TABLA: Sacamos el JScrollPane que creó NetBeans y lo metemos en nuestro panel
        // Esto hace que la tabla ocupe todo el CENTRO y crezca con la ventana
        panelFondo.add(jScrollPane1, BorderLayout.CENTER);

        // Reemplazamos el panel principal de la ventana con nuestro panel degradado
        this.setContentPane(panelFondo);
        
        // Refrescamos
        this.validate();
    }
    
    // --- MÉTODO 2: ESTILIZADO DE TABLA (Idéntico a PanelRecursos) ---
    private void estilizarTabla() {
        // A. FUENTE Y FILAS
        jTable1.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        jTable1.setRowHeight(30); // Filas altas
        jTable1.setGridColor(new Color(230, 230, 230)); // Gris suave
        jTable1.setShowVerticalLines(false); // Estilo limpio sin lineas verticales
        jTable1.setIntercellSpacing(new java.awt.Dimension(0, 0));

        // B. ENCABEZADO (HEADER) AZUL PETRÓLEO
        JTableHeader header = jTable1.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(31, 78, 95)); // Tu color corporativo
        header.setForeground(Color.WHITE);
        header.setOpaque(true);
        header.setReorderingAllowed(false); // No mover columnas

        // C. RENDERER: Colores Alternados (Efecto Cebra) y Centrado
        jTable1.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(
                    javax.swing.JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {

                java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                // Centrar texto
                this.setHorizontalAlignment(SwingConstants.CENTER);

                if (isSelected) {
                    // Color de selección (Celeste del degradado)
                    c.setBackground(new Color(162, 211, 224));
                    c.setForeground(Color.BLACK);
                } else {
                    // Filas pares blancas, impares gris muy claro
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });

        // D. FONDO DEL SCROLLPANE
        jScrollPane1.getViewport().setBackground(Color.WHITE);
        jScrollPane1.setBorder(BorderFactory.createEmptyBorder()); // Sin borde feo
    }
   
    // --- MÉTODO 3: DATOS Y COLUMNAS ---
    private void cargarAlquileres() {
        // 1. Definir columnas (14 en total)
        String[] titulos = {
            "ID Alq.",      // 0
            "ID Turista",   // 1
            "DNI",          // 2
            "Nombre Turista",// 3
            "F. Inicio",    // 4
            "H. Inicio",    // 5
            "Dur.",         // 6
            "Total",        // 7
            "Estado",       // 8
            "Promo",        // 9
            "User",         // 10
            "H. Fin Plan",  // 11
            "H. Fin Real",  // 12
            "Mora"          // 13
        };

        // 2. Modelo que bloquea edición
        modeloTabla = new DefaultTableModel(null, titulos) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        jTable1.setModel(modeloTabla);
        
        // Scroll horizontal necesario para tantas columnas
        jTable1.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF); 

        // 3. Ajustar Anchos de Columnas
        int[] anchos = {60, 60, 80, 200, 80, 70, 50, 70, 80, 70, 60, 80, 80, 70};
        for (int i = 0; i < jTable1.getColumnCount(); i++) {
            if (i < anchos.length) {
                jTable1.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
            }
        }

        // 4. SQL: Incluye horaFinalReal y Mora
        modeloTabla.setRowCount(0);
        String sql = """
            SELECT 
                A.idAlquiler, 
                A.idTurista, 
                ISNULL(T.DNI, '—') AS DNI,
                ISNULL(CONCAT(T.nombre, ' ', T.apellidos), '—') AS NombreCompleto,
                A.fechaInicio, 
                A.horaInicio, 
                A.Duracion, 
                A.total, 
                A.estado, 
                ISNULL(A.idPromocion, '—') AS idPromocion,
                A.idUsuario, 
                ISNULL(CONVERT(VARCHAR(8), A.horaFinal, 108), '—') AS horaFinal,
                ISNULL(CONVERT(VARCHAR(8), A.horaFinalReal, 108), '—') AS horaFinalReal,
                ISNULL(A.mora, 0.00) AS mora
            FROM Alquiler A
            LEFT JOIN Turista T ON A.idTurista = T.idTurista
            ORDER BY A.idAlquiler DESC
        """;

        try (PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Object[] fila = new Object[14];
                fila[0] = rs.getString("idAlquiler");
                fila[1] = rs.getString("idTurista");
                fila[2] = rs.getString("DNI");
                fila[3] = rs.getString("NombreCompleto");
                fila[4] = rs.getDate("fechaInicio");
                fila[5] = rs.getString("horaInicio");
                fila[6] = rs.getInt("Duracion");
                fila[7] = rs.getBigDecimal("total");
                fila[8] = rs.getString("estado");
                fila[9] = rs.getString("idPromocion");
                fila[10] = rs.getString("idUsuario");
                fila[11] = rs.getString("horaFinal");
                fila[12] = rs.getString("horaFinalReal");
                fila[13] = rs.getBigDecimal("mora");
                modeloTabla.addRow(fila);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error SQL: " + e.getMessage());
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
        jTable1 = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "idAlquiler", "idTurista", "fechaInicio", "horaInicio", "Duracion", "Total", "estado", "idPromocion", "idUsuario", "horaFinal"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 780, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(73, Short.MAX_VALUE))
        );

        jPanel1.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 780, 500));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 781, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}
