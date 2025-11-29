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
    /**
     * Default accent color red component.
     */
    private static final int DEFAULT_ACCENT_R = 82;
    /**
     * Default accent color green component.
     */
    private static final int DEFAULT_ACCENT_G = 140;
    /**
     * Default accent color blue component.
     */
    private static final int DEFAULT_ACCENT_B = 255;
    /**
     * Preferred button width.
     */
    private static final int PREFERRED_WIDTH = 66;
    /**
     * Preferred button height.
     */
    private static final int PREFERRED_HEIGHT = 56;
    /**
     * Minimum button width.
     */
    private static final int MIN_WIDTH = 60;
    /**
     * Minimum button height.
     */
    private static final int MIN_HEIGHT = 52;
    /**
     * Color mixing ratio.
     */
    private static final float MIX_RATIO = 0.55f;
    /**
     * Alpha value for hover state.
     */
    private static final int HOVER_ALPHA = 210;
    /**
     * Alpha value for active state.
     */
    private static final int ACTIVE_ALPHA = 180;
    /**
     * Alpha value for pressed state.
     */
    private static final int PRESSED_ALPHA = 220;
    /**
     * Corner arc radius.
     */
    private static final int CORNER_ARC = 32;
    /**
     * Padding around button content.
     */
    private static final int PADDING = 8;
    /**
     * Icon padding inside content rect.
     */
    private static final int ICON_PADDING = 12;
    /**
     * Label font size.
     */
    private static final float LABEL_FONT_SIZE = 13.5f;
    /**
     * Spacing between icon and text.
     */
    private static final int ICON_TEXT_SPACING = 4;
    /**
     * Stroke width for drawing.
     */
    private static final float STROKE_WIDTH = 2.2f;
    /**
     * Mic height ratio.
     */
    private static final double MIC_HEIGHT_RATIO = 0.55;
    /**
     * Mic width ratio.
     */
    private static final double MIC_WIDTH_RATIO = 0.35;
    /**
     * Size divisor for mic stand.
     */
    private static final int SIZE_DIV_6 = 6;
    /**
     * Size divisor for mic arc offset.
     */
    private static final int SIZE_DIV_4 = 4;
    /**
     * Arc angle for mic.
     */
    private static final int ARC_ANGLE_180 = 180;
    /**
     * Video body width ratio.
     */
    private static final double VIDEO_BODY_WIDTH_RATIO = 0.6;
    /**
     * Video body height ratio.
     */
    private static final double VIDEO_BODY_HEIGHT_RATIO = 0.45;
    /**
     * Round rect corner radius.
     */
    private static final int ROUND_RECT_8 = 8;
    /**
     * Round rect corner radius.
     */
    private static final int ROUND_RECT_10 = 10;
    /**
     * Round rect corner radius.
     */
    private static final int ROUND_RECT_12 = 12;
    /**
     * Size divisor for video triangle.
     */
    private static final int SIZE_DIV_6_VIDEO = 6;
    /**
     * Size divisor for share icon.
     */
    private static final int SIZE_DIV_8 = 8;
    /**
     * Size divisor for hand icon.
     */
    private static final int SIZE_DIV_3 = 3;
    /**
     * Size divisor for hand icon.
     */
    private static final int SIZE_DIV_10 = 10;
    /**
     * Size divisor for hand icon.
     */
    private static final int SIZE_DIV_7 = 7;
    /**
     * Size divisor for division by 2.
     */
    private static final int SIZE_DIV_2 = 2;
    /**
     * Size divisor for chat icon.
     */
    private static final int SIZE_DIV_4_CHAT = 4;
    /**
     * Size divisor for chat icon.
     */
    private static final int SIZE_DIV_7_CHAT = 7;
    /**
     * Size divisor for leave icon.
     */
    private static final int SIZE_DIV_6_LEAVE = 6;
    /**
     * Size divisor for leave icon.
     */
    private static final int SIZE_DIV_4_LEAVE = 4;
    /**
     * Size divisor for leave icon.
     */
    private static final int SIZE_DIV_3_LEAVE = 3;
    /**
     * Size divisor for leave icon.
     */
    private static final int SIZE_DIV_2_LEAVE = 2;
    /**
     * Arc start angle.
     */
    private static final int ARC_START_0 = 0;
    /**
     * Polygon point offset.
     */
    private static final int POLYGON_OFFSET_8 = 8;
    /**
     * Polygon point offset.
     */
    private static final int POLYGON_OFFSET_3 = 3;
    /**
     * Palm width ratio.
     */
    private static final double PALM_WIDTH_RATIO = 0.5;
    /**
     * Palm height ratio.
     */
    private static final double PALM_HEIGHT_RATIO = 0.45;

    /**
     * Control icon types.
     */
    public enum ControlIcon {
        /**
         * Microphone icon.
         */
        MIC,
        /**
         * Video icon.
         */
        VIDEO,
        /**
         * Share icon.
         */
        SHARE,
        /**
         * Hand icon.
         */
        HAND,
        /**
         * Leave icon.
         */
        LEAVE,
        /**
         * Chat icon.
         */
        CHAT,
        /**
         * People icon.
         */
        PEOPLE
    }

    /**
     * Icon type for this button.
     */
    private final ControlIcon iconType;
    /**
     * Whether button is active.
     */
    private boolean active;
    /**
     * Accent color.
     */
    private Color accentColor = new Color(DEFAULT_ACCENT_R, DEFAULT_ACCENT_G, DEFAULT_ACCENT_B);
    /**
     * Override color for active state.
     */
    private Color activeColorOverride;

    /**
     * Creates a new meeting control button.
     *
     * @param label the button label
     * @param icon the icon type
     */
    public MeetingControlButton(final String label, final ControlIcon icon) {
        super(label);
        this.iconType = Objects.requireNonNull(icon, "icon");
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setPreferredSize(new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT));
        setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
    }

    /**
     * Sets the active state.
     *
     * @param activeParam true if active
     */
    public void setActive(final boolean activeParam) {
        if (this.active != activeParam) {
            this.active = activeParam;
            repaint();
        }
    }

    /**
     * Gets the active state.
     *
     * @return true if active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets the active color override.
     *
     * @param color the override color
     */
    public void setActiveColorOverride(final Color color) {
        this.activeColorOverride = color;
    }

    /**
     * Sets the accent color.
     *
     * @param accent the accent color
     */
    public void setAccentColor(final Color accent) {
        if (accent != null) {
            this.accentColor = accent;
            repaint();
        }
    }

    @Override
    protected void paintComponent(final Graphics g) {
        final Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        final Theme theme = ThemeManager.getInstance().getCurrentTheme();
        final Color base = mix(theme.getPanelBackground(), theme.getBackgroundColor(), MIX_RATIO);
        final Color hover = new Color(base.getRed(), base.getGreen(), base.getBlue(), HOVER_ALPHA);
        final Color accent;
        if (activeColorOverride != null) {
            accent = activeColorOverride;
        } else {
            accent = accentColor;
        }
        final Color foreground = theme.getTextColor();

        final Color fillColor;
        if (active) {
            fillColor = new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), ACTIVE_ALPHA);
        } else if (getModel().isPressed()) {
            fillColor = new Color(base.getRed(), base.getGreen(), base.getBlue(), PRESSED_ALPHA);
        } else if (getModel().isRollover()) {
            fillColor = hover;
        } else {
            fillColor = new Color(base.getRed(), base.getGreen(), base.getBlue(), ACTIVE_ALPHA);
        }
        g2.setColor(fillColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), CORNER_ARC, CORNER_ARC);

        final Font labelFont = FontUtil.getJetBrainsMono(LABEL_FONT_SIZE, Font.BOLD);
        final FontMetrics fm = g2.getFontMetrics(labelFont);
        g2.setFont(labelFont);
        g2.setColor(foreground);
        final int textHeight = fm.getHeight();
        final int textWidth = fm.stringWidth(getText());
        final int textX = (getWidth() - textWidth) / 2;
        final int textY = getHeight() - PADDING;
        g2.drawString(getText(), textX, textY);

        final Rectangle iconBounds = new Rectangle(PADDING, PADDING, getWidth() - PADDING * 2,
                getHeight() - PADDING * 2 - textHeight - ICON_TEXT_SPACING);
        drawIcon(g2, iconBounds, foreground, accent);
        g2.dispose();
    }

    private void drawIcon(final Graphics2D g2, final Rectangle area, final Color strokeColor,
                          final Color accent) {
        final int size = Math.min(area.width, area.height);
        final int x = area.x + (area.width - size) / 2;
        final int y = area.y + (area.height - size) / 2;

        g2.setStroke(new BasicStroke(STROKE_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (iconType) {
            case MIC -> {
                final int micHeight = (int) (size * MIC_HEIGHT_RATIO);
                final int micWidth = (int) (size * MIC_WIDTH_RATIO);
                g2.setColor(strokeColor);
                final int micCornerRadius = micWidth / SIZE_DIV_2;
                g2.drawRoundRect(x + (size - micWidth) / SIZE_DIV_2, y, micWidth, micHeight,
                        micCornerRadius, micCornerRadius);
                final int micStandY = y + micHeight;
                final int micStandEndY = micStandY + size / SIZE_DIV_6;
                g2.drawLine(x + size / SIZE_DIV_2, micStandY, x + size / SIZE_DIV_2, micStandEndY);
                final int arcOffset = SIZE_DIV_4;
                final int arcY = micStandEndY - arcOffset;
                final int arcWidth = micWidth;
                final int arcHeight = micWidth / SIZE_DIV_2;
                g2.drawArc(x + size / SIZE_DIV_2 - micWidth / SIZE_DIV_2, arcY, arcWidth, arcHeight,
                        ARC_ANGLE_180, ARC_ANGLE_180);
            }
            case VIDEO -> {
                final int bodyWidth = (int) (size * VIDEO_BODY_WIDTH_RATIO);
                final int bodyHeight = (int) (size * VIDEO_BODY_HEIGHT_RATIO);
                g2.setColor(strokeColor);
                final int videoY = y + size / SIZE_DIV_6;
                g2.drawRoundRect(x + (size - bodyWidth) / SIZE_DIV_2, videoY, bodyWidth, bodyHeight,
                        ROUND_RECT_8, ROUND_RECT_8);
                final int triangleX1 = x + size / SIZE_DIV_2 + bodyWidth / SIZE_DIV_2;
                final int triangleX2 = triangleX1;
                final int triangleX3 = x + size - SIZE_DIV_6_VIDEO;
                final int triangleY1 = videoY;
                final int triangleY2 = videoY + bodyHeight;
                final int triangleY3 = y + size / SIZE_DIV_2;
                g2.fillPolygon(new int[]{triangleX1, triangleX2, triangleX3},
                        new int[]{triangleY1, triangleY2, triangleY3}, POLYGON_OFFSET_3);
            }
            case SHARE -> {
                g2.setColor(strokeColor);
                final int shareX = x + size / SIZE_DIV_8;
                final int shareY = y + size / SIZE_DIV_4;
                final int shareWidth = size - size / SIZE_DIV_4;
                final int shareHeight = size / SIZE_DIV_2;
                g2.drawRoundRect(shareX, shareY, shareWidth, shareHeight, ROUND_RECT_10, ROUND_RECT_10);
                final int centerX = x + size / SIZE_DIV_2;
                g2.drawLine(centerX, y, centerX, y + shareHeight);
                final int line1EndX = centerX - size / SIZE_DIV_6;
                final int line1EndY = y + size / SIZE_DIV_6;
                g2.drawLine(centerX, y, line1EndX, line1EndY);
                final int line2EndX = centerX + size / SIZE_DIV_6;
                final int line2EndY = line1EndY;
                g2.drawLine(centerX, y, line2EndX, line2EndY);
            }
            case HAND -> {
                g2.setColor(strokeColor);
                final int palmWidth = (int) (size * PALM_WIDTH_RATIO);
                final int palmHeight = (int) (size * PALM_HEIGHT_RATIO);
                final int palmX = x + (size - palmWidth) / SIZE_DIV_2;
                final int palmY = y + size / SIZE_DIV_4;
                g2.drawRoundRect(palmX, palmY, palmWidth, palmHeight, ROUND_RECT_12, ROUND_RECT_12);
                g2.drawLine(palmX, palmY, palmX, y);
                final int finger1X = palmX + palmWidth / SIZE_DIV_3;
                final int finger1EndY = y + size / SIZE_DIV_8;
                g2.drawLine(finger1X, palmY, finger1X, finger1EndY);
                final int finger2X = palmX + SIZE_DIV_2 * palmWidth / SIZE_DIV_3;
                final int finger2EndY = y + size / SIZE_DIV_10;
                g2.drawLine(finger2X, palmY, finger2X, finger2EndY);
                g2.drawLine(palmX + palmWidth, palmY, palmX + palmWidth, y);
            }
            case LEAVE -> {
                final Color leaveColor;
                if (activeColorOverride != null) {
                    leaveColor = activeColorOverride;
                } else {
                    leaveColor = accent;
                }
                g2.setColor(leaveColor);
                final int leaveX = x + size / SIZE_DIV_6_LEAVE;
                final int leaveY = y + size / SIZE_DIV_4;
                final int leaveWidth = size - size / SIZE_DIV_3_LEAVE;
                final int leaveHeight = size / SIZE_DIV_2_LEAVE;
                g2.drawArc(leaveX, leaveY, leaveWidth, leaveHeight, ARC_START_0, ARC_ANGLE_180);
                final int leaveCenterY = y + size / SIZE_DIV_2_LEAVE;
                final int leg1X1 = x + size / SIZE_DIV_4_LEAVE;
                final int leg1X2 = x + size / SIZE_DIV_6_LEAVE;
                final int leg1Y2 = leaveCenterY + size / SIZE_DIV_6_LEAVE;
                g2.drawLine(leg1X1, leaveCenterY, leg1X2, leg1Y2);
                final int leg2X1 = x + size - size / SIZE_DIV_4_LEAVE;
                final int leg2X2 = x + size - size / SIZE_DIV_6_LEAVE;
                final int leg2Y2 = leg1Y2;
                g2.drawLine(leg2X1, leaveCenterY, leg2X2, leg2Y2);
            }
            case CHAT -> {
                g2.setColor(strokeColor);
                final int chatX = x + size / SIZE_DIV_6;
                final int chatY = y + size / SIZE_DIV_6;
                final int chatWidth = size - size / SIZE_DIV_3;
                final int chatHeight = size / SIZE_DIV_2;
                g2.drawRoundRect(chatX, chatY, chatWidth, chatHeight, ROUND_RECT_12, ROUND_RECT_12);
                final int chatCenterX = x + size / SIZE_DIV_2;
                final int chatBottomY = y + size - size / SIZE_DIV_4_CHAT;
                final int chatTipY = y + size - size / SIZE_DIV_7_CHAT;
                g2.fillPolygon(new int[]{chatCenterX - POLYGON_OFFSET_8, chatCenterX + POLYGON_OFFSET_8, chatCenterX},
                        new int[]{chatBottomY, chatBottomY, chatTipY}, POLYGON_OFFSET_3);
            }
            case PEOPLE -> {
                g2.setColor(strokeColor);
                final int peopleX = x + size / SIZE_DIV_3;
                final int peopleWidth = size / SIZE_DIV_3;
                final int peopleHeight = peopleWidth;
                g2.drawOval(peopleX, y, peopleWidth, peopleHeight);
                final int peopleArcX = x + size / SIZE_DIV_6;
                final int peopleArcY = y + size / SIZE_DIV_3;
                final int peopleArcWidth = size - size / SIZE_DIV_3;
                final int peopleArcHeight = size / SIZE_DIV_2;
                g2.drawArc(peopleArcX, peopleArcY, peopleArcWidth, peopleArcHeight, ARC_START_0, ARC_ANGLE_180);
            }
            default -> {
                // No action for unknown icon types
            }
        }
    }

    private Color mix(final Color a, final Color b, final float ratio) {
        final float inv = 1f - ratio;
        final int r = (int) (a.getRed() * ratio + b.getRed() * inv);
        final int g = (int) (a.getGreen() * ratio + b.getGreen() * inv);
        final int bl = (int) (a.getBlue() * ratio + b.getBlue() * inv);
        return new Color(r, g, bl);
    }
}
