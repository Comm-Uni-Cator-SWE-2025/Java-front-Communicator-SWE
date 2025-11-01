package com.swe.ux.ui;

import com.swe.ux.theme.Theme;
import com.swe.ux.theme.ThemeManager;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class ParticipantPanel extends JPanel {
    private final String name;
    private BufferedImage displayImage;

    public ParticipantPanel(String name) {
        this.name = name;
        this.displayImage = null;
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(300, 220));
    }

    public void setImage(BufferedImage image) {
        this.displayImage = image;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Theme theme = ThemeManager.getInstance().getTheme();

        // If an image is set, draw it to fill the entire panel
        if (displayImage != null) {
            g2d.drawImage(displayImage, 0, 0, getWidth(), getHeight(), null);
        } else {
            // Default participant view (circular avatar with name)
            g2d.setColor(theme.getForeground());
            g2d.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 20, 20);

            int circleDiameter = Math.min(getWidth(), getHeight()) / 3;
            int circleX = (getWidth() - circleDiameter) / 2;
            int circleY = (getHeight() - circleDiameter) / 2 - 10;
            g2d.setColor(theme.getBackground());
            g2d.fillOval(circleX, circleY, circleDiameter, circleDiameter);

            g2d.setColor(theme.getText());
            g2d.setFont(new Font("SansSerif", Font.BOLD, Math.max(12, circleDiameter / 3)));
            String initials = name.contains(" ") ? ("" + name.charAt(0) + name.substring(name.indexOf(" ") + 1).charAt(0)).toUpperCase() : ("" + name.charAt(0)).toUpperCase();
            g2d.drawString(initials,
                circleX + (circleDiameter - g2d.getFontMetrics().stringWidth(initials)) / 2,
                circleY + (circleDiameter - g2d.getFontMetrics().getHeight()) / 2 + g2d.getFontMetrics().getAscent()
            );

            g2d.setFont(new Font("SansSerif", Font.PLAIN, 14));
            g2d.drawString(name,
                (getWidth() - g2d.getFontMetrics().stringWidth(name)) / 2,
                circleY + circleDiameter + 20);
        }

        g2d.dispose();
    }
}
