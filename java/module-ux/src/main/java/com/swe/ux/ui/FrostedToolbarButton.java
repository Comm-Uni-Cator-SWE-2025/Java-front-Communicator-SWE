package com.swe.ux.ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JButton;

import com.swe.ux.theme.ThemeManager;

public class FrostedToolbarButton extends JButton {

    private float alpha = 0.94f;
    private Color customFill = null;   // <- ADDED (fix)
    private Color defaultFillLight = new Color(0, 0, 0, 12);
    private Color defaultFillDark = new Color(255, 255, 255, 20);

    public FrostedToolbarButton(String text) {
        super(text);

        setOpaque(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFont(FontUtil.getJetBrainsMono(15f, Font.BOLD));

        // hover fade
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                setAlpha(1f);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                setAlpha(0.94f);
            }
        });
    }

    // ============================================================
    // NEW: setCustomFill() used by MeetingPage
    // ============================================================
    public void setCustomFill(Color c) {
        this.customFill = c;
        repaint();
    }

    public Color getCustomFill() {
        return customFill;
    }

    // internal fade setter
    public void setAlpha(float a) {
        this.alpha = Math.max(0.5f, Math.min(1f, a));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        boolean dark = ThemeManager.getInstance().getCurrentTheme().isDark();

        // choose fill
        Color fill = customFill != null 
                ? customFill 
                : (dark ? defaultFillDark : defaultFillLight);

        // draw frosted rect
        int arc = 14;
        g2.setComposite(AlphaComposite.SrcOver.derive(alpha));
        g2.setColor(fill);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);

        g2.dispose();

        // draw text
        super.paintComponent(g);
    }
}
