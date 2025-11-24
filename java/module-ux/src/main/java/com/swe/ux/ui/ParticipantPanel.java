/**
 * Contributed by Sandeep Kumar.
 */

package com.swe.ux.ui;

import com.swe.screenNVideo.Utils;
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

/**
 * Panel displaying participant information and video.
 */
public class ParticipantPanel extends JPanel {
    /**
     * Inset for rounded rectangle.
     */
    private static final int ROUNDED_RECT_INSET = 5;
    /**
     * Corner radius for rounded rectangle.
     */
    private static final int ROUNDED_RECT_CORNER = 20;
    /**
     * Divisor for circle size calculation.
     */
    private static final int CIRCLE_DIVISOR = 3;
    /**
     * Y offset for circle positioning.
     */
    private static final int CIRCLE_Y_OFFSET = 10;
    /**
     * Initial font size.
     */
    private static final int INITIAL_FONT_SIZE = 12;
    /**
     * Font size for participant name.
     */
    private static final int NAME_FONT_SIZE = 14;
    /**
     * Y offset for name text.
     */
    private static final int NAME_Y_OFFSET = 20;
    /**
     * Height of status bar.
     */
    private static final int BAR_HEIGHT = 30;
    /**
     * Alpha value for overlay.
     */
    private static final int OVERLAY_ALPHA = 128;
    /**
     * Padding for text elements.
     */
    private static final int TEXT_PADDING = 10;
    /**
     * Offset for rate text.
     */
    private static final int RATE_TEXT_OFFSET = 12;

    /**
     * Participant name.
     */
    private final String name;
    /**
     * Participant IP address.
     */
    private final String ip;
    /**
     * Bounds for zoom icon click area.
     */
    private final Rectangle zoomIconBounds = new Rectangle();
    /**
     * Data rate in bytes per second.
     */
    private long dataRate;
    /**
     * Display image for participant.
     */
    private BufferedImage displayImage;
    /**
     * Listener for panel events.
     */
    private ParticipantPanelListener listener;
    /**
     * Whether mouse is over the panel.
     */
    private boolean isMouseOver = false;
    /**
     * Whether panel is zoomed.
     */
    private boolean isZoomed = false;

    /**
     * Creates a new participant panel.
     *
     * @param nameParam the participant name
     * @param ipParam the participant IP
     */
    public ParticipantPanel(final String nameParam, final String ipParam) {
        this.name = nameParam;
        this.ip = ipParam;
        this.displayImage = null;
        this.dataRate = 0;
        setLayout(new BorderLayout());

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(final MouseEvent e) {
                isMouseOver = true;
                repaint();
            }

            @Override
            public void mouseExited(final MouseEvent e) {
                isMouseOver = false;
                repaint();
            }

            @Override
            public void mouseClicked(final MouseEvent e) {
                if (e.getClickCount() == 1 && isMouseOver
                        && zoomIconBounds.contains(e.getPoint()) && listener != null) {
                    listener.onZoomToggle(ip);
                }
            }
        });
    }

    /**
     * Sets the display image.
     *
     * @param image the image to display
     */
    public void setImage(final BufferedImage image) {
        this.displayImage = image;
        repaint();
    }

    /**
     * Sets the data rate.
     *
     * @param dataRateArgs the data rate in bytes per second
     */
    public void setDataRate(final long dataRateArgs) {
        if (dataRateArgs >= 0) {
            this.dataRate = dataRateArgs;
            repaint();
        }
    }

    /**
     * Sets the listener that will be notified of zoom toggle events.
     *
     * @param listenerParam The listener to set.
     */
    public void setParticipantListener(final ParticipantPanelListener listenerParam) {
        this.listener = listenerParam;
    }

    /**
     * Sets the zoom state of this panel.
     *
     * @param isZoomedParam true if this panel is currently in the main zoom view, false otherwise.
     */
    public void setZoomed(final boolean isZoomedParam) {
        this.isZoomed = isZoomedParam;
        if (isMouseOver) {
            repaint();
        }
    }

    // CHECKSTYLE:OFF: JavaNCSS - Complex paint method
    @Override
    protected void paintComponent(final Graphics g) {
        // CHECKSTYLE:ON: JavaNCSS
        super.paintComponent(g);
        final Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        final Theme theme = ThemeManager.getInstance().getCurrentTheme();

        if (displayImage != null) {
            g2d.drawImage(displayImage, 0, 0, getWidth(), getHeight(), null);
        } else {
            // Default participant view
            g2d.setColor(theme.getForeground());
            g2d.fillRoundRect(ROUNDED_RECT_INSET, ROUNDED_RECT_INSET,
                    getWidth() - ROUNDED_RECT_INSET * 2, getHeight() - ROUNDED_RECT_INSET * 2,
                    ROUNDED_RECT_CORNER, ROUNDED_RECT_CORNER);

            final int circleDiameter = Math.min(getWidth(), getHeight()) / CIRCLE_DIVISOR;
            final int circleX = (getWidth() - circleDiameter) / 2;
            final int circleY = (getHeight() - circleDiameter) / 2 - CIRCLE_Y_OFFSET;
            g2d.setColor(theme.getBackground());
            g2d.fillOval(circleX, circleY, circleDiameter, circleDiameter);

            g2d.setColor(theme.getText());
            final int fontSize = Math.max(INITIAL_FONT_SIZE, circleDiameter / CIRCLE_DIVISOR);
            g2d.setFont(new Font("SansSerif", Font.BOLD, fontSize));
            final String initials;
            if (name.contains(" ")) {
                initials = ("" + name.charAt(0)
                        + name.substring(name.indexOf(" ") + 1).charAt(0)).toUpperCase();
            } else {
                initials = ("" + name.charAt(0)).toUpperCase();
            }
            final FontMetrics initialMetrics = g2d.getFontMetrics();
            final int initialX = circleX + (circleDiameter - initialMetrics.stringWidth(initials)) / 2;
            final int initialY = circleY + (circleDiameter - initialMetrics.getHeight()) / 2
                    + initialMetrics.getAscent();
            g2d.drawString(initials, initialX, initialY);

            g2d.setFont(new Font("SansSerif", Font.PLAIN, NAME_FONT_SIZE));
            final FontMetrics nameMetrics = g2d.getFontMetrics();
            final int nameX = (getWidth() - nameMetrics.stringWidth(name)) / 2;
            g2d.drawString(name, nameX, circleY + circleDiameter + NAME_Y_OFFSET);
        }

        // Draw overlay on hover
        if (isMouseOver) {
            g2d.setColor(new Color(0, 0, 0, OVERLAY_ALPHA));
            g2d.fillRect(0, getHeight() - BAR_HEIGHT, getWidth(), BAR_HEIGHT);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("SansSerif", Font.BOLD, NAME_FONT_SIZE));

            // Draw zoom TEXT on the right
            final String zoomText;
            if (isZoomed) {
                zoomText = "[-]";
            } else {
                zoomText = "[+]";
            }
            final FontMetrics metrics = g2d.getFontMetrics();
            final int textWidth = metrics.stringWidth(zoomText);
            final int textX = getWidth() - textWidth - TEXT_PADDING;
            final int textY = getHeight() - (BAR_HEIGHT / 2)
                    + (metrics.getAscent() - metrics.getDescent()) / 2;

            // Update the clickable bounds
            zoomIconBounds.setBounds(textX - ROUNDED_RECT_INSET, getHeight() - BAR_HEIGHT,
                    textWidth + TEXT_PADDING, BAR_HEIGHT);

            g2d.drawString(name, TEXT_PADDING, getHeight() - TEXT_PADDING);

            final int rateTextX = RATE_TEXT_OFFSET + (g2d.getFontMetrics().stringWidth(name));

            if (dataRate > 0) {
                g2d.drawString(String.format(" : %.2f Kb/s", dataRate / (Utils.KB)),
                        rateTextX, getHeight() - TEXT_PADDING);
            }

            g2d.drawString(zoomText, textX, textY);
        }

        g2d.dispose();
    }

    /**
     * A listener interface for handling zoom toggle requests from this panel.
     */
    public interface ParticipantPanelListener {
        void onZoomToggle(String ip);
    }
}
