package com.swe.ux.views;

import com.swe.ux.theme.Theme;
import com.swe.ux.theme.ThemeManager;
import com.swe.ux.ui.FrostedBackgroundPanel;
import com.swe.ux.ui.FontUtil;
import com.swe.ux.ui.SoftCardPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;

/**
 * Loading screen displayed while joining or creating a meeting.
 */
public class LoadingPage extends FrostedBackgroundPanel {
    
    private JLabel statusLabel;
    private SpinnerPanel spinnerPanel;
    private boolean uiCreated = false;
    
    public LoadingPage() {
        initializeUI();
        uiCreated = true;
        applyTheme();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        
        // Main container with centered content
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        
        // Card panel for loading content
        SoftCardPanel card = new SoftCardPanel(48);
        card.setCornerRadius(32);
        card.setLayout(new BorderLayout(0, 24));
        
        // Spinner
        spinnerPanel = new SpinnerPanel();
        spinnerPanel.setPreferredSize(new Dimension(80, 80));
        
        // Status label
        statusLabel = new JLabel("Connecting to meeting...", SwingConstants.CENTER);
        statusLabel.setFont(FontUtil.getJetBrainsMono(18f, Font.PLAIN));
        
        // Layout
        JPanel spinnerContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        spinnerContainer.setOpaque(false);
        spinnerContainer.add(spinnerPanel);
        
        card.add(spinnerContainer, BorderLayout.CENTER);
        card.add(statusLabel, BorderLayout.SOUTH);
        
        centerPanel.add(card, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }
    
    public void setStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }
    
    public void startSpinner() {
        if (spinnerPanel != null) {
            spinnerPanel.start();
        }
    }
    
    public void stopSpinner() {
        if (spinnerPanel != null) {
            spinnerPanel.stop();
        }
    }
    
    private void applyTheme() {
        if (!uiCreated) return;
        
        Theme theme = ThemeManager.getInstance().getCurrentTheme();
        if (theme == null) return;
        
        setBackground(theme.getBackgroundColor());
        statusLabel.setForeground(theme.getTextColor());
        
        if (spinnerPanel != null) {
            spinnerPanel.setForeground(theme.getPrimaryColor());
        }
        
        revalidate();
        repaint();
    }
    
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            applyTheme();
            startSpinner();
        } else {
            stopSpinner();
        }
    }
    
    @Override
    public void updateUI() {
        super.updateUI();
        if (uiCreated) {
            applyTheme();
        }
    }
    
    /**
     * Custom spinner panel with rotating arc animation.
     */
    private static class SpinnerPanel extends JPanel {
        private Timer timer;
        private int angle = 0;
        private Color spinnerColor = new Color(0x4A86E8);
        
        public SpinnerPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(80, 80));
        }
        
        @Override
        public void setForeground(Color color) {
            this.spinnerColor = color;
            repaint();
        }
        
        public void start() {
            if (timer != null && timer.isRunning()) {
                return;
            }
            timer = new Timer(16, e -> {
                angle = (angle + 8) % 360;
                repaint();
            });
            timer.start();
        }
        
        public void stop() {
            if (timer != null) {
                timer.stop();
            }
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int size = Math.min(getWidth(), getHeight()) - 20;
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;
            
            // Draw rotating arc
            g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(spinnerColor);
            
            Arc2D arc = new Arc2D.Float(x, y, size, size, angle, 270, Arc2D.OPEN);
            g2.draw(arc);
            
            g2.dispose();
        }
    }
}

