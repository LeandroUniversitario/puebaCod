
package igu;

import java.awt.*;
import javax.swing.JPanel;

public class PanelDegradado extends JPanel {

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);

        int w = getWidth();
        int h = getHeight();

        // Colores similares a tu imagen
        Color top = new Color(162, 211, 224);     // azul cielo
        Color middle = new Color(196, 226, 215);  // verde agua
        Color bottom = new Color(245, 238, 220);  // arena

        GradientPaint gp1 = new GradientPaint(0, 0, top, 0, h / 2, middle);
        GradientPaint gp2 = new GradientPaint(0, h / 2, middle, 0, h, bottom);

        g2.setPaint(gp1);
        g2.fillRect(0, 0, w, h / 2);

        g2.setPaint(gp2);
        g2.fillRect(0, h / 2, w, h);
    }
}
