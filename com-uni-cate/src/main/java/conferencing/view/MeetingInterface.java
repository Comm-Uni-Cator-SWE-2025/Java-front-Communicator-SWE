package com.conferencing.views;

import com.conferencing.App;
import com.conferencing.ui.CustomButton;
import com.conferencing.theme.Theme;
import com.conferencing.theme.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MeetingInterface extends JPanel {

    private final App app;
    private final JPanel videoGrid;
    private JPanel controlsPanel;

    public MeetingInterface(App app) {
        this.app = app;
        this.videoGrid = new JPanel(new GridLayout(2, 3, 10, 10)); 
        initComponents();
        
        addParticipant("Dummy Name 1");
        addParticipant("Dummy Name 2");
        addParticipant("Dummy Name 3");
        addParticipant("Dummy Name 4");
        addParticipant("You");
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        add(videoGrid, BorderLayout.CENTER);

        controlsPanel = createControlsPanel();
        add(controlsPanel, BorderLayout.SOUTH);
    }
    
    private void addParticipant(String name) {
        ParticipantPanel panel = new ParticipantPanel(name);
        videoGrid.add(panel);
    }

    private JPanel createControlsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JLabel timeLabel = new JLabel("11:45");
        timeLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        panel.add(timeLabel, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.add(new CustomButton("VidOn", false));
        buttonPanel.add(new CustomButton("Mute", false));
        buttonPanel.add(new CustomButton("Sound", false));
        buttonPanel.add(new CustomButton("Share", false));
        
        CustomButton endCallButton = new CustomButton("End", true);
        endCallButton.addActionListener(e -> app.showPage(App.MAIN_PAGE));
        buttonPanel.add(endCallButton);

        panel.add(buttonPanel, BorderLayout.CENTER);

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightControls.add(new CustomButton("Chat", false));
        JLabel copyMeetingLabel = new JLabel("COPY MEETING LINK");
        copyMeetingLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        rightControls.add(copyMeetingLabel);
        panel.add(rightControls, BorderLayout.EAST);
        
        return panel;
    }
    
    @Override
    public void updateUI() {
        super.updateUI();
        if (ThemeManager.getInstance() != null && controlsPanel != null) {
            Theme theme = ThemeManager.getInstance().getTheme();
            // Special case for end call button color
            for(Component comp : controlsPanel.getComponents()) {
                if(comp instanceof JPanel) {
                     for(Component btn : ((JPanel)comp).getComponents()) {
                         if(btn instanceof CustomButton && "End".equals(((CustomButton)btn).getText())) {
                            btn.setBackground(theme.getAccent());
                         }
                     }
                }
            }
        }
    }
    
    private static class ParticipantPanel extends JPanel {
        private final String name;
        
        ParticipantPanel(String name) {
            this.name = name;
            setLayout(new BorderLayout());
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            Theme theme = ThemeManager.getInstance().getTheme();
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
            
            g2d.dispose();
        }
    }
}

