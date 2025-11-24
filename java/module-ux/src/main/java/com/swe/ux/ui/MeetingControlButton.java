package com.swe.ux.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.Objects;

import javax.swing.JButton;

import com.swe.ux.theme.Theme;
import com.swe.ux.theme.ThemeManager;

/**
 * Pill-style control button with icon + label stacked vertically.
 */
public class MeetingControlButton extends JButton {

    private static final long serialVersionUID = 1L;

    public enum ControlIcon {
        MIC, VIDEO, SHARE, HAND, LEAVE, CHAT, PEOPLE;
    }

    private final ControlIcon iconType;
    private boolean active;
    private Color accentColor = new Color(82, 140, 255);
    private Color activeColorOverride;

    public MeetingControlButton(String label, ControlIcon icon) {
        super(label);
        this.iconType = Objects.requireNonNull(icon, "icon");
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setPreferredSize(new Dimension(66, 56));
        setMinimumSize(new Dimension(60, 52));

    }

    public void setActive(boolean active) {
        if (this.active != active) {
            this.active = active;
            repaint();
        }
    }

    public boolean isActive() {
        return active;
    }

    public void setActiveColorOverride(Color color) {
        this.activeColorOverride = color;
    }

    public void setAccentColor(Color accent) {
        if (accent != null) {
            this.accentColor = accent;
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Theme theme = ThemeManager.getInstance().getCurrentTheme();
        Color base = mix(theme.getPanelBackground(), theme.getBackgroundColor(), 0.55f);
        Color hover = new Color(base.getRed(), base.getGreen(), base.getBlue(), 210);
        Color accent = activeColorOverride != null ? activeColorOverride : accentColor;
        Color foreground = theme.getTextColor();

        if (active) {
            g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 180));
        } else if (getModel().isPressed()) {
            g2.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), 220));
        } else if (getModel().isRollover()) {
            g2.setColor(hover);
        } else {
            g2.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), 180));
        }
        int arc = 32;
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);

        int padding = 8;
        Font labelFont = FontUtil.getJetBrainsMono(13.5f, Font.BOLD);
        FontMetrics fm = g2.getFontMetrics(labelFont);
        g2.setFont(labelFont);
        g2.setColor(foreground);
        int textHeight = fm.getHeight();
        int textWidth = fm.stringWidth(getText());
        int textX = (getWidth() - textWidth) / 2;
        int textY = getHeight() - padding;
        g2.drawString(getText(), textX, textY);

        Rectangle iconBounds = new Rectangle(padding, padding, getWidth() - padding * 2,
                getHeight() - padding * 2 - textHeight - 4);
        drawIcon(g2, iconBounds, foreground, accent);
        g2.dispose();
    }

    private void drawIcon(Graphics2D g2, Rectangle area, Color strokeColor, Color accent) {
        int size = Math.min(area.width, area.height);
        int x = area.x + (area.width - size) / 2;
        int y = area.y + (area.height - size) / 2;

        g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (iconType) {
            case MIC -> {
                int micHeight = (int) (size * 0.55);
                int micWidth = (int) (size * 0.35);
                g2.setColor(strokeColor);
                g2.drawRoundRect(x + (size - micWidth) / 2, y, micWidth, micHeight, micWidth / 2, micWidth / 2);
                g2.drawLine(x + size / 2, y + micHeight, x + size / 2, y + micHeight + size / 6);
                g2.drawArc(x + size / 2 - micWidth / 2, y + micHeight + size / 6 - 4, micWidth, micWidth / 2, 180, 180);
            }
            case VIDEO -> {
                int bodyWidth = (int) (size * 0.6);
                int bodyHeight = (int) (size * 0.45);
                g2.setColor(strokeColor);
                g2.drawRoundRect(x + (size - bodyWidth) / 2, y + size / 6, bodyWidth, bodyHeight, 8, 8);
                g2.fillPolygon(new int[] { x + size / 2 + bodyWidth / 2, x + size / 2 + bodyWidth / 2, x + size - 6 },
                        new int[] { y + size / 6, y + size / 6 + bodyHeight, y + size / 2 }, 3);
            }
            case SHARE -> {
                g2.setColor(strokeColor);
                g2.drawRoundRect(x + size / 8, y + size / 4, size - size / 4, size / 2, 10, 10);
                g2.drawLine(x + size / 2, y, x + size / 2, y + size / 2);
                g2.drawLine(x + size / 2, y, x + size / 2 - size / 6, y + size / 6);
                g2.drawLine(x + size / 2, y, x + size / 2 + size / 6, y + size / 6);
            }
            case HAND -> {
                g2.setColor(strokeColor);
                int palmWidth = (int) (size * 0.5);
                int palmHeight = (int) (size * 0.45);
                int palmX = x + (size - palmWidth) / 2;
                int palmY = y + size / 4;
                g2.drawRoundRect(palmX, palmY, palmWidth, palmHeight, 12, 12);
                g2.drawLine(palmX, palmY, palmX, y);
                g2.drawLine(palmX + palmWidth / 3, palmY, palmX + palmWidth / 3, y + size / 8);
                g2.drawLine(palmX + 2 * palmWidth / 3, palmY, palmX + 2 * palmWidth / 3, y + size / 10);
                g2.drawLine(palmX + palmWidth, palmY, palmX + palmWidth, y);
            }
            case LEAVE -> {
                g2.setColor(activeColorOverride != null ? activeColorOverride : accent);
                g2.drawArc(x + size / 6, y + size / 4, size - size / 3, size / 2, 0, 180);
                g2.drawLine(x + size / 4, y + size / 2, x + size / 6, y + size / 2 + size / 6);
                g2.drawLine(x + size - size / 4, y + size / 2, x + size - size / 6, y + size / 2 + size / 6);
            }
            case CHAT -> {
                g2.setColor(strokeColor);
                g2.drawRoundRect(x + size / 6, y + size / 6, size - size / 3, size / 2, 12, 12);
                g2.fillPolygon(new int[] { x + size / 2 - 8, x + size / 2 + 8, x + size / 2 },
                        new int[] { y + size - size / 4, y + size - size / 4, y + size - size / 7 }, 3);
            }
            case PEOPLE -> {
                g2.setColor(strokeColor);
                g2.drawOval(x + size / 3, y, size / 3, size / 3);
                g2.drawArc(x + size / 6, y + size / 3, size - size / 3, size / 2, 0, 180);
            }
        }
    }

    private Color mix(Color a, Color b, float ratio) {
        float inv = 1f - ratio;
        int r = (int) (a.getRed() * ratio + b.getRed() * inv);
        int g = (int) (a.getGreen() * ratio + b.getGreen() * inv);
        int bl = (int) (a.getBlue() * ratio + b.getBlue() * inv);
        return new Color(r, g, bl);
    }
}
