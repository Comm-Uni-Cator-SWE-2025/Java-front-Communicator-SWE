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
    /**
     * Default size for the clock panel.
     */
    private static final int DEFAULT_SIZE = 200;
    /**
     * Timer delay in milliseconds.
     */
    private static final int TIMER_DELAY = 1000;
    /**
     * Padding around the clock.
     */
    private static final int CLOCK_PADDING = 10;
    /**
     * Number of hours on the clock face.
     */
    private static final int HOURS_ON_CLOCK = 12;
    /**
     * Degrees per hour on the clock face.
     */
    private static final int DEGREES_PER_HOUR = 30;
    /**
     * Starting angle offset in degrees.
     */
    private static final int START_ANGLE_OFFSET = 90;
    /**
     * Marking offset from the edge.
     */
    private static final int MARKING_OFFSET = 12;
    /**
     * Outer marking offset.
     */
    private static final int MARKING_OUTER_OFFSET = 2;
    /**
     * Minute hand ratio for hour calculation.
     */
    private static final double MINUTE_HAND_RATIO = 0.5;
    /**
     * Hour hand length ratio.
     */
    private static final double HOUR_HAND_LENGTH = 0.5;
    /**
     * Hour hand stroke width.
     */
    private static final float HOUR_HAND_STROKE = 6f;
    /**
     * Degrees per minute.
     */
    private static final int MINUTE_DEGREES = 6;
    /**
     * Minute hand length ratio.
     */
    private static final double MINUTE_HAND_LENGTH = 0.75;
    /**
     * Minute hand stroke width.
     */
    private static final float MINUTE_HAND_STROKE = 4f;
    /**
     * Second hand length ratio.
     */
    private static final double SECOND_HAND_LENGTH = 0.85;
    /**
     * Center dot size.
     */
    private static final int CENTER_DOT_SIZE = 4;
    /**
     * Default color red component.
     */
    private static final int DEFAULT_COLOR_R = 40;
    /**
     * Default color green component.
     */
    private static final int DEFAULT_COLOR_G = 44;
    /**
     * Default color blue component.
     */
    private static final int DEFAULT_COLOR_B = 52;
    /**
     * Text offset from clock edge.
     */
    private static final int TEXT_OFFSET = 20;
    /**
     * Font size for digital time display.
     */
    private static final float DIGITAL_FONT_SIZE = 12f;

    /**
     * Timer for repainting the clock.
     */
    private final Timer timer;
    /**
     * Zone ID for the clock.
     */
    private final ZoneId zoneId;
    /**
     * Formatter for displaying time.
     */
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm:ss a");

    /**
     * Creates a new analog clock panel with system default timezone.
     */
    public AnalogClockPanel() {
        this(ZoneId.systemDefault());
    }

    /**
     * Creates a new analog clock panel with specified timezone.
     *
     * @param zoneIdParam the zone ID
     */
    public AnalogClockPanel(final ZoneId zoneIdParam) {
        if (zoneIdParam != null) {
            this.zoneId = zoneIdParam;
        } else {
            this.zoneId = ZoneId.systemDefault();
        }
        setOpaque(false);
        setPreferredSize(new Dimension(DEFAULT_SIZE, DEFAULT_SIZE));
        timer = new Timer(TIMER_DELAY, e -> repaint());
        timer.setRepeats(true);
        timer.setInitialDelay(0);
        timer.start();
    }

    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);
        final Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        final int size = Math.min(getWidth(), getHeight()) - CLOCK_PADDING;
        final int centerX = getWidth() / 2;
        final int centerY = getHeight() / 2;
        final int radius = size / 2;

        final Theme theme = ThemeManager.getInstance().getCurrentTheme();
        final Color dialColor = getDialColor(theme);
        final Color tickColor = getTickColor(theme);

        g2.setColor(dialColor);
        g2.fillOval(centerX - radius, centerY - radius, size, size);

        g2.setStroke(new BasicStroke(2f));
        g2.setColor(tickColor);
        g2.drawOval(centerX - radius, centerY - radius, size, size);

        // Hour markings
        for (int i = 0; i < HOURS_ON_CLOCK; i++) {
            final double angle = Math.toRadians((i * DEGREES_PER_HOUR) - START_ANGLE_OFFSET);
            final int inner = radius - MARKING_OFFSET;
            final int outer = radius - MARKING_OUTER_OFFSET;
            final int x1 = centerX + (int) (inner * Math.cos(angle));
            final int y1 = centerY + (int) (inner * Math.sin(angle));
            final int x2 = centerX + (int) (outer * Math.cos(angle));
            final int y2 = centerY + (int) (outer * Math.sin(angle));
            g2.drawLine(x1, y1, x2, y2);
        }

        final ZonedDateTime zonedDateTime = ZonedDateTime.now(zoneId);
        final LocalTime now = zonedDateTime.toLocalTime();

        final double hourAngle = (now.getHour() % HOURS_ON_CLOCK) * DEGREES_PER_HOUR
                + now.getMinute() * MINUTE_HAND_RATIO;
        drawHand(g2, hourAngle, radius * HOUR_HAND_LENGTH, HOUR_HAND_STROKE, tickColor, centerX, centerY);
        drawHand(g2, now.getMinute() * MINUTE_DEGREES, radius * MINUTE_HAND_LENGTH,
                MINUTE_HAND_STROKE, tickColor, centerX, centerY);
        final Color secondColor = getSecondHandColor(theme);
        drawHand(g2, now.getSecond() * MINUTE_DEGREES, radius * SECOND_HAND_LENGTH,
                2f, secondColor, centerX, centerY);

        g2.setColor(tickColor);
        g2.fillOval(centerX - CENTER_DOT_SIZE, centerY - CENTER_DOT_SIZE,
                CENTER_DOT_SIZE * 2, CENTER_DOT_SIZE * 2);

        g2.setFont(FontUtil.getJetBrainsMono(DIGITAL_FONT_SIZE, Font.PLAIN));
        final String digital = formatter.format(zonedDateTime);
        final int textWidth = g2.getFontMetrics().stringWidth(digital);
        g2.drawString(digital, centerX - textWidth / 2, centerY + radius + TEXT_OFFSET);

        g2.dispose();
    }

    /**
     * Gets the dial color based on the theme.
     *
     * @param theme the theme to use
     * @return the dial color
     */
    private Color getDialColor(final Theme theme) {
        if (theme != null) {
            return theme.getPanelBackground();
        }
        return new Color(DEFAULT_COLOR_R, DEFAULT_COLOR_G, DEFAULT_COLOR_B);
    }

    /**
     * Gets the tick color based on the theme.
     *
     * @param theme the theme to use
     * @return the tick color
     */
    private Color getTickColor(final Theme theme) {
        if (theme != null) {
            return theme.getTextColor();
        }
        return Color.WHITE;
    }

    /**
     * Gets the second hand color based on the theme.
     *
     * @param theme the theme to use
     * @return the second hand color
     */
    private Color getSecondHandColor(final Theme theme) {
        if (theme != null) {
            return theme.getAccentColor();
        }
        return Color.RED;
    }

    private void drawHand(final Graphics2D g2, final double angleDegrees, final double length,
                          final float stroke, final Color color, final int centerX, final int centerY) {
        final double angle = Math.toRadians(angleDegrees - START_ANGLE_OFFSET);
        final int x = centerX + (int) (length * Math.cos(angle));
        final int y = centerY + (int) (length * Math.sin(angle));
        g2.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(color);
        g2.drawLine(centerX, centerY, x, y);
    }
}
