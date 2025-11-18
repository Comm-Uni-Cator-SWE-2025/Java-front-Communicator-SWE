/**
 *  Contributed by Sandeep Kumar.
 */
package com.swe.ux.ui;

import com.swe.ux.theme.Theme;
import com.swe.ux.theme.ThemeManager;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class ParticipantPanel extends JPanel {

    /**
     * A listener interface for handling zoom toggle requests from this panel.
     */
    public interface ParticipantPanelListener {
        void onZoomToggle(String ip);
    }

    private final String name;
    private final String ip;
    private BufferedImage displayImage;
    private ParticipantPanelListener listener;

    private boolean isMouseOver = false;
    private boolean isZoomed = false;
    private final Rectangle zoomIconBounds = new Rectangle();

    public ParticipantPanel(String name, String ip) {
        this.name = name;
        this.ip = ip;
        this.displayImage = null;
        setLayout(new BorderLayout());

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isMouseOver = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isMouseOver = false;
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1 && isMouseOver && zoomIconBounds.contains(e.getPoint()) && listener != null) {
                    listener.onZoomToggle(ip);
                }
            }
        });
    }

    public void setImage(BufferedImage image) {
        this.displayImage = image;
        repaint();
    }

    /**
     * Sets the listener that will be notified of zoom toggle events.
     * @param listener The listener to set.
     */
    public void setParticipantListener(ParticipantPanelListener listener) {
        this.listener = listener;
    }

    /**
     * Sets the zoom state of this panel.
     * @param isZoomed true if this panel is currently in the main zoom view, false otherwise.
     */
    public void setZoomed(boolean isZoomed) {
        this.isZoomed = isZoomed;
        if (isMouseOver) {
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Theme theme = ThemeManager.getInstance().getTheme();

        if (displayImage != null) {
            g2d.drawImage(displayImage, 0, 0, getWidth(), getHeight(), null);
        } else {
            // Default participant view
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

        // Draw overlay on hover
        if (isMouseOver) {
            int barHeight = 30;
            g2d.setColor(new Color(0, 0, 0, 128));
            g2d.fillRect(0, getHeight() - barHeight, getWidth(), barHeight);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("SansSerif", Font.BOLD, 14));

            // Draw zoom TEXT on the right
            String zoomText = isZoomed ? "[-]" : "[+]"; // Updated icons
            FontMetrics metrics = g2d.getFontMetrics();
            int textWidth = metrics.stringWidth(zoomText);
            int textX = getWidth() - textWidth - 10;
            int textY = getHeight() - (barHeight / 2) + (metrics.getAscent() - metrics.getDescent()) / 2;

            g2d.drawString(zoomText, textX, textY);

            // Update the clickable bounds
            zoomIconBounds.setBounds(textX - 5, getHeight() - barHeight, textWidth + 10, barHeight);

            if (displayImage != null) {
                g2d.drawString(name, 10, getHeight() - 10);
            }
        }

        g2d.dispose();
    }
}
