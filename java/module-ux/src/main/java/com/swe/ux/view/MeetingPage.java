package com.swe.ux.view;

import com.swe.ux.model.User;
import com.swe.ux.theme.ThemeManager;
import com.swe.ux.ui.CustomButton;
import com.swe.ux.viewmodel.MeetingViewModel;
import com.swe.ux.binding.PropertyListeners;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Meeting Page layout (UX Integration Layer)
 * --------------------------------------------------------
 * This is the main meeting container that holds all modules:
 *  - Screensharing (ScreenViewModel)
 *  - Video (VideoViewModel)
 *  - Canvas (CanvasViewModel)
 *  - AI Insights (AIViewModel)
 * along with Chat + Participants sidebar and Meeting Controls.
 *
 * Each module team will later replace their placeholder panels
 * with actual components and logic.
 */
public class MeetingPage extends JPanel {

    private final MeetingViewModel meetingViewModel;

    // UI Components
    private JSplitPane mainSplitPane;
    private JPanel rightPanel;
    private boolean rightPanelVisible = true;
    
    // Placeholder panels for team integration
    private JPanel chatPanel;
    private JPanel participantsPanel;

    public MeetingPage(MeetingViewModel meetingViewModel) {
        this.meetingViewModel = meetingViewModel;
        initializeUI();
        setupBindings();
        applyTheme();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // ---------- HEADER ----------
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel titleLabel = new JLabel("Meeting");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton toggleSidebarBtn = new CustomButton("Hide Sidebar", false);
        toggleSidebarBtn.addActionListener(e -> toggleRightPanel(toggleSidebarBtn));

        JButton endMeetingButton = new CustomButton("Leave/End Meeting", true);
        endMeetingButton.addActionListener(e -> meetingViewModel.endMeeting());

        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerActions.add(toggleSidebarBtn);
        headerActions.add(endMeetingButton);
        headerPanel.add(headerActions, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // ---------- MAIN CENTER (Tabs + Sidebar) ----------
        JTabbedPane centerTabs = new JTabbedPane();
        centerTabs.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // --- PLACEHOLDER PANELS (Team Ownership) ---
        centerTabs.addTab("Video", createPlaceholderPanel(" Video Module - To be integrated by Video Team"));
        centerTabs.addTab("Screensharing", createPlaceholderPanel(" Screensharing Module - To be integrated by Screen Team"));
        centerTabs.addTab("Canvas", createPlaceholderPanel(" Canvas Module - To be integrated by Canvas Team"));
        centerTabs.addTab("AI Insights", createPlaceholderPanel(" AI Insights Module - To be integrated by AI Team"));

        // Right-side (Chat + Participants)
        rightPanel = createRightPanel();

        mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, centerTabs, rightPanel);
        mainSplitPane.setDividerLocation(900);
        mainSplitPane.setResizeWeight(0.75);
        add(mainSplitPane, BorderLayout.CENTER);

        // ---------- FOOTER (Controls) ----------
        add(createMeetingControlsPanel(), BorderLayout.SOUTH);
    }

    /** Creates a labeled placeholder panel for unimplemented modules */
    private JPanel createPlaceholderPanel(String text) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(text, JLabel.CENTER);
        label.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        panel.add(label, BorderLayout.CENTER);
        panel.setBackground(new Color(245, 247, 250));
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true));
        return panel;
    }

    /** 
     * Creates the right panel with chat and participants sections.
     * These are placeholders that will be replaced by the respective teams.
     */
    private JPanel createRightPanel() {
        JPanel sidebar = new JPanel(new BorderLayout(8, 8));

        // Chat Panel (To be implemented by Chat Team)
        chatPanel = createTeamPlaceholderPanel("Chat Module", "To be implemented by Chat Team");
        chatPanel.setBorder(BorderFactory.createTitledBorder("Chat"));
        
        // Participants Panel (To be implemented by Controller Team)
        participantsPanel = createTeamPlaceholderPanel("Participants Module", "To be implemented by Controller Team");
        participantsPanel.setBorder(BorderFactory.createTitledBorder("Participants"));

        // Split vertically
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, chatPanel, participantsPanel);
        split.setDividerLocation(300);
        split.setResizeWeight(0.6);

        sidebar.add(split, BorderLayout.CENTER);
        return sidebar;
    }
    
    /**
     * Creates a placeholder panel for team-specific implementations
     */
    private JPanel createTeamPlaceholderPanel(String title, String description) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("<html><center><h2>" + title + "</h2>" + description + "</center></html>", 
            SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(label, BorderLayout.CENTER);
        panel.setBackground(new Color(245, 247, 250));
        return panel;
    }

    /** Meeting Controls Bar (bottom of the page) */
    private JPanel createMeetingControlsPanel() {
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));

        controlsPanel.add(new CustomButton(" Mute", false));
        controlsPanel.add(new CustomButton(" Camera", false));
        controlsPanel.add(new CustomButton(" Share", false));
        JButton leaveButton = new CustomButton(" Leave", true);
        leaveButton.addActionListener(e -> meetingViewModel.endMeeting());
        controlsPanel.add(leaveButton);

        return controlsPanel;
    }

    private void toggleRightPanel(JButton toggleButton) {
        if (rightPanelVisible) {
            mainSplitPane.setRightComponent(new JPanel());
            toggleButton.setText("Show Sidebar");
        } else {
            mainSplitPane.setRightComponent(rightPanel);
            toggleButton.setText("Hide Sidebar");
        }
        rightPanelVisible = !rightPanelVisible;
        revalidate();
        repaint();
    }

    /** 
     * Setup bindings between ViewModel and UI components.
     * These bindings will be used by the respective team implementations.
     */
    private void setupBindings() {
        // These bindings are now the responsibility of the respective teams
        // Chat team should implement their bindings in their module
        // Controller team should implement participants bindings in their module
        
        // Example structure (commented out as these will be implemented by the respective teams):
        /*
        // Chat team should implement:
        meetingViewModel.messages.addListener(PropertyListeners.onListChanged((List<String> msgs) ->
            SwingUtilities.invokeLater(() -> {
                // Chat team's implementation
            })
        ));
        
        // Controller team should implement:
        meetingViewModel.participants.addListener(PropertyListeners.onListChanged((List<User> users) ->
            SwingUtilities.invokeLater(() -> {
                // Controller team's implementation
            })
        ));
        */
    }

    /**
     * Sends a message in the chat.
     * This method will be called by the chat team's implementation.
     */
    private void sendMessage() {
        // This method will be implemented by the chat team
        // meetingViewModel.sendMessage();
    }

    private void applyTheme() {
        ThemeManager.getInstance().applyTheme(this);
    }
}
