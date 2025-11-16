package com.conferencing.views;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.conferencing.App;
import com.conferencing.theme.Theme;
import com.conferencing.theme.ThemeManager;
import com.conferencing.ui.CustomButton;

public class MeetingInterface extends JPanel {

    private final App app;
    private final JPanel videoGrid;
    private JPanel controlsPanel;
    private JPanel buttonPanel;
    private JPanel rightControls;
    private JPanel chatPanel;
    private JPanel contentPanel;
    private boolean chatVisible = false;
    private CustomButton chatButton;

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

        // Content panel to hold video grid and chat
        contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.add(videoGrid, BorderLayout.CENTER);
        
        // Create chat panel (initially hidden)
        chatPanel = createChatPanel();
        
        add(contentPanel, BorderLayout.CENTER);

        controlsPanel = createControlsPanel();
        add(controlsPanel, BorderLayout.SOUTH);
        
        applyTheme();
    }
    
    private void addParticipant(String name) {
        ParticipantPanel panel = new ParticipantPanel(name);
        videoGrid.add(panel);
    }
    
    private JPanel createChatPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JLabel chatTitle = new JLabel("Chat");
        chatTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        chatTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        panel.add(chatTitle, BorderLayout.NORTH);
        
        // Empty chat area for now
        JPanel chatArea = new JPanel();
        panel.add(chatArea, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void toggleChat() {
        chatVisible = !chatVisible;
        
        if (chatVisible) {
            // Show chat panel on the right
            contentPanel.add(chatPanel, BorderLayout.EAST);
            chatPanel.setPreferredSize(new Dimension(300, 0));
            chatButton.setText("Close Chat");
        } else {
            // Hide chat panel
            contentPanel.remove(chatPanel);
            chatButton.setText("Chat");
        }
        
        contentPanel.revalidate();
        contentPanel.repaint();
        applyTheme();
    }

    private JPanel createControlsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JLabel timeLabel = new JLabel("11:45");
        timeLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        panel.add(timeLabel, BorderLayout.WEST);

        buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.add(new CustomButton("Video", false));
        buttonPanel.add(new CustomButton("Canvas", false));
        buttonPanel.add(new CustomButton("Participants", false));
        buttonPanel.add(new CustomButton("Share", false));
        
        CustomButton endCallButton = new CustomButton("End", true);
        endCallButton.addActionListener(e -> app.showPage(App.MAIN_PAGE));
        buttonPanel.add(endCallButton);

        panel.add(buttonPanel, BorderLayout.CENTER);

        rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        chatButton = new CustomButton("Chat", false);
        chatButton.addActionListener(e -> toggleChat());
        rightControls.add(chatButton);
        JLabel copyMeetingLabel = new JLabel("COPY LINK");
        copyMeetingLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        rightControls.add(copyMeetingLabel);
        panel.add(rightControls, BorderLayout.EAST);
        
        return panel;
    }
    
    private void applyTheme() {
        Theme theme = ThemeManager.getInstance().getTheme();
        setBackground(theme.getBackground());
        if (videoGrid != null) {
            videoGrid.setBackground(theme.getBackground());
        }
        if (contentPanel != null) {
            contentPanel.setBackground(theme.getBackground());
        }
        if (chatPanel != null) {
            chatPanel.setBackground(theme.getForeground());
            // Apply theme to chat area components
            for(Component comp : chatPanel.getComponents()) {
                if(comp instanceof JPanel) {
                    comp.setBackground(theme.getForeground());
                }
            }
        }
        if (controlsPanel != null) {
            controlsPanel.setBackground(theme.getBackground());
            
            // Special case for end call button color
            for(Component comp : controlsPanel.getComponents()) {
                if(comp instanceof JPanel) {
                     for(Component btn : ((JPanel)comp).getComponents()) {
                         if(btn instanceof CustomButton && ((CustomButton)btn).getText().contains("End")) {
                            btn.setBackground(theme.getAccent());
                         }
                     }
                }
            }
        }
        if (buttonPanel != null) {
            buttonPanel.setBackground(theme.getBackground());
        }
        if (rightControls != null) {
            rightControls.setBackground(theme.getBackground());
        }
    }
    
    @Override
    public void updateUI() {
        super.updateUI();
        if (ThemeManager.getInstance() != null) {
            applyTheme();
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

