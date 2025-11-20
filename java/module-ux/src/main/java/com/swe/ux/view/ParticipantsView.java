/**
 *  Contributed by Sandeep Kumar.
 */
package com.swe.ux.view;

import com.swe.controller.Meeting.UserProfile;
import com.swe.ux.binding.PropertyListeners;
import com.swe.ux.theme.Theme;
import com.swe.ux.theme.ThemeManager;
import com.swe.ux.viewmodel.ParticipantsViewModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * View component for displaying meeting participants.
 * Shows participant count and list of participant names.
 */
public class ParticipantsView extends JPanel {
    private final ParticipantsViewModel viewModel;
    private JLabel countLabel;
    private JPanel participantsListPanel;
    private JScrollPane scrollPane;
    
    /**
     * Creates a new ParticipantsView.
     * @param viewModel The ParticipantsViewModel to use
     */
    public ParticipantsView(ParticipantsViewModel viewModel) {
        this.viewModel = viewModel;
        initializeUI();
        setupBindings();
        applyTheme();
    }
    
    /**
     * Initializes the UI components.
     */
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Header with participant count
        JPanel headerPanel = new JPanel(new BorderLayout());
        countLabel = new JLabel("Participants (0)", JLabel.LEFT);
        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        headerPanel.add(countLabel, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);
        
        // Participants list
        participantsListPanel = new JPanel();
        participantsListPanel.setLayout(new BoxLayout(participantsListPanel, BoxLayout.Y_AXIS));
        participantsListPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        scrollPane = new JScrollPane(participantsListPanel);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        add(scrollPane, BorderLayout.CENTER);
    }
    
    /**
     * Sets up bindings between ViewModel and UI components.
     */
    private void setupBindings() {
        // Listen to participant count changes
        viewModel.participantCount.addListener(evt -> {
            SwingUtilities.invokeLater(() -> {
                int count = viewModel.participantCount.get();
                countLabel.setText("Participants (" + count + ")");
            });
        });
        
        // Listen to participant list changes
        viewModel.participants.addListener(PropertyListeners.onListChanged((List<UserProfile> participants) -> {
            SwingUtilities.invokeLater(() -> {
                updateParticipantsList(participants);
            });
        }));
        
        // Initial update
        SwingUtilities.invokeLater(() -> {
            int count = viewModel.participantCount.get();
            countLabel.setText("Participants (" + count + ")");
            updateParticipantsList(viewModel.getParticipants());
        });
    }
    
    /**
     * Updates the participants list display.
     * @param participants The list of participants to display
     */
    private void updateParticipantsList(List<UserProfile> participants) {
        participantsListPanel.removeAll();
        
        if (participants == null || participants.isEmpty()) {
            JLabel emptyLabel = new JLabel("No participants", JLabel.CENTER);
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            emptyLabel.setForeground(Color.GRAY);
            participantsListPanel.add(emptyLabel);
        } else {
            for (UserProfile participant : participants) {
                JPanel participantItem = createParticipantItem(participant);
                participantsListPanel.add(participantItem);
                participantsListPanel.add(Box.createVerticalStrut(8));
            }
        }
        
        participantsListPanel.revalidate();
        participantsListPanel.repaint();
    }
    
    /**
     * Creates a panel for a single participant item.
     * @param participant The participant user
     * @return A JPanel displaying the participant information
     */
    private JPanel createParticipantItem(UserProfile participant) {
        JPanel itemPanel = new JPanel(new BorderLayout(10, 5));
        itemPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        
        // Participant name
        String displayName = participant.getDisplayName();
        String name = displayName != null && !displayName.isEmpty() ? displayName : participant.getEmail();
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        itemPanel.add(nameLabel, BorderLayout.WEST);
        
        // Online indicator (small circle)
        JPanel indicatorPanel = new JPanel();
        indicatorPanel.setPreferredSize(new Dimension(10, 10));
        indicatorPanel.setOpaque(true);
        indicatorPanel.setBackground(new Color(76, 175, 80)); // Green for online
        itemPanel.add(indicatorPanel, BorderLayout.EAST);
        
        return itemPanel;
    }
    
    /**
     * Applies the current theme to the component.
     */
    private void applyTheme() {
        ThemeManager themeManager = ThemeManager.getInstance();
        Theme theme = themeManager.getCurrentTheme();
        
        setBackground(theme.getBackgroundColor());
        countLabel.setForeground(theme.getTextColor());
        participantsListPanel.setBackground(theme.getBackgroundColor());
        scrollPane.getViewport().setBackground(theme.getBackgroundColor());
        
        // Apply theme to all participant items
        for (Component comp : participantsListPanel.getComponents()) {
            if (comp instanceof JPanel itemPanel) {
                itemPanel.setBackground(theme.getBackgroundColor());
                for (Component child : itemPanel.getComponents()) {
                    if (child instanceof JLabel label) {
                        label.setForeground(theme.getTextColor());
                    }
                }
            }
        }
    }
}

