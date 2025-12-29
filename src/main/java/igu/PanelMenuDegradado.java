package igu;

import java.awt.*;
import javax.swing.JPanel;

public class PanelMenuDegradado extends JPanel {

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);

        int w = getWidth();
        int h = getHeight();

        // Azul petróleo -> azul
        Color top = new Color(31, 78, 95);   // #1F4E5F
        Color bottom = new Color(46, 111, 130); // #2E6F82

        GradientPaint gp = new GradientPaint(0, 0, top, 0, h, bottom);

        g2.setPaint(gp);
        g2.fillRect(0, 0, w, h);
    }
}
