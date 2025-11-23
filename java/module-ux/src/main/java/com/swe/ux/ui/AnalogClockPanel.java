
/*
 * -----------------------------------------------------------------------------
 *  File: AnalogClockPanel.java
 *  Owner: Aryan Mathur
 *  Roll Number : 122201017
 *  Module : UX
 *
 * -----------------------------------------------------------------------------
 */
package com.swe.ux.ui;

import com.swe.ux.theme.Theme;
import com.swe.ux.theme.ThemeManager;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Simple analog clock widget that repaints every second.
 */
public class AnalogClockPanel extends JPanel {

    private final Timer timer;
    private final ZoneId zoneId;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm:ss a");

    public AnalogClockPanel() {
        this(ZoneId.systemDefault());
    }

    public AnalogClockPanel(ZoneId zoneId) {
        this.zoneId = zoneId != null ? zoneId : ZoneId.systemDefault();
        setOpaque(false);
        setPreferredSize(new Dimension(200, 200));
        timer = new Timer(1000, e -> repaint());
        timer.setRepeats(true);
        timer.setInitialDelay(0);
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int size = Math.min(getWidth(), getHeight()) - 10;
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int radius = size / 2;

        Theme theme = ThemeManager.getInstance().getCurrentTheme();
        Color dialColor = theme != null ? theme.getPanelBackground() : new Color(40, 44, 52);
        Color tickColor = theme != null ? theme.getTextColor() : Color.WHITE;

        g2.setColor(dialColor);
        g2.fillOval(centerX - radius, centerY - radius, size, size);

        g2.setStroke(new BasicStroke(2f));
        g2.setColor(tickColor);
        g2.drawOval(centerX - radius, centerY - radius, size, size);

        // Hour markings
        for (int i = 0; i < 12; i++) {
            double angle = Math.toRadians((i * 30) - 90);
            int inner = radius - 12;
            int outer = radius - 2;
            int x1 = centerX + (int) (inner * Math.cos(angle));
            int y1 = centerY + (int) (inner * Math.sin(angle));
            int x2 = centerX + (int) (outer * Math.cos(angle));
            int y2 = centerY + (int) (outer * Math.sin(angle));
            g2.drawLine(x1, y1, x2, y2);
        }

        ZonedDateTime zonedDateTime = ZonedDateTime.now(zoneId);
        LocalTime now = zonedDateTime.toLocalTime();

        drawHand(g2, now.getHour() % 12 * 30 + now.getMinute() * 0.5, radius * 0.5, 6f, tickColor, centerX, centerY);
        drawHand(g2, now.getMinute() * 6, radius * 0.75, 4f, tickColor, centerX, centerY);
        drawHand(g2, now.getSecond() * 6, radius * 0.85, 2f, theme != null ? theme.getAccentColor() : Color.RED, centerX, centerY);

        g2.setColor(tickColor);
        g2.fillOval(centerX - 4, centerY - 4, 8, 8);

        g2.setFont(FontUtil.getJetBrainsMono(12f, Font.PLAIN));
        String digital = formatter.format(zonedDateTime);
        int textWidth = g2.getFontMetrics().stringWidth(digital);
        g2.drawString(digital, centerX - textWidth / 2, centerY + radius + 20);

        g2.dispose();
    }

    private void drawHand(Graphics2D g2, double angleDegrees, double length, float stroke,
                          Color color, int centerX, int centerY) {
        double angle = Math.toRadians(angleDegrees - 90);
        int x = centerX + (int) (length * Math.cos(angle));
        int y = centerY + (int) (length * Math.sin(angle));
        g2.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(color);
        g2.drawLine(centerX, centerY, x, y);
    }
}
