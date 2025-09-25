package com.conferencing.views;

import com.conferencing.App;
import com.conferencing.ui.CustomButton;
import com.conferencing.ui.PlaceholderTextField;
import com.conferencing.theme.Theme;
import com.conferencing.theme.ThemeManager;
import com.conferencing.ui.TitledPanel;
import com.conferencing.ui.ThemeToggleButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainPage extends JPanel {

    private final App app;
    private JLabel userInitialLabel;

    public MainPage(App app) {
        this.app = app;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(new EmptyBorder(15, 25, 15, 25));

        JLabel logoLabel = new JLabel("LOGO");
        logoLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        headerPanel.add(logoLabel, BorderLayout.WEST);

        JLabel dateTimeLabel = new JLabel(new SimpleDateFormat("HH:mm, E, MMM d").format(new Date()));
        dateTimeLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        headerPanel.add(dateTimeLabel, BorderLayout.CENTER);
        dateTimeLabel.setHorizontalAlignment(JLabel.CENTER);

        userInitialLabel = new JLabel("Y") {
             @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillOval(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
                g2.dispose();
            }
        };
        userInitialLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        userInitialLabel.setHorizontalAlignment(JLabel.CENTER);
        userInitialLabel.setVerticalAlignment(JLabel.CENTER);
        userInitialLabel.setPreferredSize(new Dimension(40, 40));
        userInitialLabel.setOpaque(false);
        headerPanel.add(userInitialLabel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Center Panel
        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);

        JLabel appTitle = new JLabel("Comm-Uni-Cate");
        appTitle.setFont(new Font("SansSerif", Font.BOLD, 48));
        appTitle.setHorizontalAlignment(JLabel.CENTER);
        centerPanel.add(appTitle, gbc);

        JLabel subtitle = new JLabel("Software Engineering Project by Batch B22");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 18));
        subtitle.setHorizontalAlignment(JLabel.CENTER);
        centerPanel.add(subtitle, gbc);
        
        centerPanel.add(Box.createRigidArea(new Dimension(0, 50)), gbc);

        // Meeting controls
        JPanel meetingControlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        CustomButton newMeetingButton = new CustomButton("New meeting", true);
        newMeetingButton.addActionListener(e -> app.showPage(App.MEETING_PAGE));
        
        PlaceholderTextField meetingCodeField = new PlaceholderTextField("Enter meeting code");
        meetingCodeField.setPreferredSize(new Dimension(300, 45));

        CustomButton joinButton = new CustomButton("Join", true);
        joinButton.addActionListener(e -> {
            if (!meetingCodeField.getText().trim().isEmpty()) {
                app.showPage(App.MEETING_PAGE);
            }
        });

        meetingControlsPanel.add(newMeetingButton);
        meetingControlsPanel.add(meetingCodeField);
        meetingControlsPanel.add(joinButton);
        centerPanel.add(meetingControlsPanel, gbc);

        // Theme Toggle Panel
        TitledPanel themePanel = new TitledPanel();
        themePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 15));
        JLabel changeThemeLabel = new JLabel("CHANGE THEME");
        
        themePanel.add(changeThemeLabel);
        themePanel.add(new ThemeToggleButton());
        
        JPanel themePanelWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        themePanel.setPreferredSize(new Dimension(300, 80));
        themePanelWrapper.add(themePanel);
        
        gbc.gridy = 4;
        gbc.insets = new Insets(50, 0, 10, 0);
        gbc.anchor = GridBagConstraints.CENTER;
        centerPanel.add(themePanelWrapper, gbc);

        add(centerPanel, BorderLayout.CENTER);
    }
    
    @Override
    public void updateUI() {
        super.updateUI();
        // Update colors that UIManager doesn't handle automatically
        if (ThemeManager.getInstance() != null && userInitialLabel != null) {
            Theme theme = ThemeManager.getInstance().getTheme();
            userInitialLabel.setBackground(theme.getPrimary());
            userInitialLabel.setForeground(Color.WHITE);
        }
    }
}

