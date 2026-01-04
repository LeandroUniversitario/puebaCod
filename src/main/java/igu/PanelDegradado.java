package igu;

import java.awt.*;
import javax.swing.JPanel;

public class PanelDegradado extends JPanel {

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        
        // Suavizado para que se vea HD
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);

        int w = getWidth();
        int h = getHeight();

        // 1. TUS COLORES (Están perfectos)
        Color top = new Color(162, 211, 224);     // azul cielo
        Color middle = new Color(196, 226, 215);  // verde agua
        Color bottom = new Color(245, 238, 220);  // arena

        // 2. LA MEJORA: LinearGradientPaint
        // Definimos las posiciones (fractions): 
        // 0.0 = inicio (arriba), 0.5 = mitad, 1.0 = fin (abajo)
        float[] dist = {0.0f, 0.5f, 1.0f};
        
        // Definimos los colores en orden
        Color[] colors = {top, middle, bottom};

        // Creamos un SOLO degradado que recorre todo el alto (0, 0) -> (0, h)
        LinearGradientPaint gp = new LinearGradientPaint(
                0, 0, 0, h, // Coordenadas: x1, y1, x2, y2
                dist,       // Posiciones de los colores
                colors      // Los colores
        );

        // 3. Pintamos UN SOLO rectángulo completo
        g2.setPaint(gp);
        g2.fillRect(0, 0, w, h);
    }
}