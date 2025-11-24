package com.swe.ux.views;

/**
 * Contributed by Pushti Vasoya.
 */

import com.swe.controller.Meeting.UserProfile;
import com.swe.ux.binding.PropertyListeners;
import com.swe.ux.theme.Theme;
import com.swe.ux.theme.ThemeManager;
import com.swe.ux.viewmodels.ParticipantsViewModel;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;

/**
 * View component for displaying meeting participants.
 * Shows participant count and list of participant names.
 */
public class ParticipantsView extends JPanel {
    /**
     * Default padding value.
     */
    private static final int DEFAULT_PADDING = 10;
    /**
     * Header font size.
     */
    private static final int HEADER_FONT_SIZE = 16;
    /**
     * Empty label font size.
     */
    private static final int EMPTY_LABEL_FONT_SIZE = 14;
    /**
     * Participant item spacing.
     */
    private static final int PARTICIPANT_ITEM_SPACING = 8;
    /**
     * Item panel horizontal gap.
     */
    private static final int ITEM_PANEL_HGAP = 10;
    /**
     * Item panel vertical gap.
     */
    private static final int ITEM_PANEL_VGAP = 5;
    /**
     * Item border padding.
     */
    private static final int ITEM_BORDER_PADDING = 5;
    /**
     * Item maximum height.
     */
    private static final int ITEM_MAX_HEIGHT = 50;
    /**
     * Name font size.
     */
    private static final int NAME_FONT_SIZE = 14;
    /**
     * Indicator size.
     */
    private static final int INDICATOR_SIZE = 10;
    /**
     * Online color red component.
     */
    private static final int ONLINE_COLOR_R = 76;
    /**
     * Online color green component.
     */
    private static final int ONLINE_COLOR_G = 175;
    /**
     * Online color blue component.
     */
    private static final int ONLINE_COLOR_B = 80;

    /**
     * ViewModel for participants data.
     */
    private final ParticipantsViewModel viewModel;
    /**
     * Label displaying participant count.
     */
    private JLabel countLabel;
    /**
     * Panel containing the list of participants.
     */
    private JPanel participantsListPanel;
    /**
     * Scroll pane for the participants list.
     */
    private JScrollPane scrollPane;

    /**
     * Creates a new ParticipantsView.
     * 
     * @param participantsViewModel The ParticipantsViewModel to use
     */
    public ParticipantsView(final ParticipantsViewModel participantsViewModel) {
        this.viewModel = participantsViewModel;
        initializeUI();
        setupBindings();
        applyTheme();
    }

    /**
     * Initializes the UI components.
     */
    private void initializeUI() {
        setLayout(new BorderLayout(DEFAULT_PADDING, DEFAULT_PADDING));
        setBorder(new EmptyBorder(DEFAULT_PADDING, DEFAULT_PADDING, DEFAULT_PADDING, DEFAULT_PADDING));

        // Header with participant count
        final JPanel headerPanel = new JPanel(new BorderLayout());
        countLabel = new JLabel("Participants (0)", JLabel.LEFT);
        countLabel.setFont(new Font("Segoe UI", Font.BOLD, HEADER_FONT_SIZE));
        headerPanel.add(countLabel, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // Participants list
        participantsListPanel = new JPanel();
        participantsListPanel.setLayout(new BoxLayout(participantsListPanel, BoxLayout.Y_AXIS));
        participantsListPanel.setBorder(new EmptyBorder(DEFAULT_PADDING, 0, 0, 0));

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
        viewModel.getParticipantCountProperty().addListener(evt -> {
            SwingUtilities.invokeLater(() -> {
                final int count = viewModel.getParticipantCount();
                countLabel.setText("Participants (" + count + ")");
            });
        });

        // Listen to participant list changes
        viewModel.getParticipantsProperty().addListener(PropertyListeners.onListChanged(
                (List<UserProfile> participants) -> {
                    SwingUtilities.invokeLater(() -> {
                        updateParticipantsList(participants);
                    });
                }));

        // Initial update
        SwingUtilities.invokeLater(() -> {
            final int count = viewModel.getParticipantCount();
            countLabel.setText("Participants (" + count + ")");
            updateParticipantsList(viewModel.getParticipants());
        });
    }

    /**
     * Updates the participants list display.
     * 
     * @param participants The list of participants to display
     */
    private void updateParticipantsList(final List<UserProfile> participants) {
        participantsListPanel.removeAll();

        if (participants == null || participants.isEmpty()) {
            final JLabel emptyLabel = new JLabel("No participants", JLabel.CENTER);
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, EMPTY_LABEL_FONT_SIZE));
            emptyLabel.setForeground(Color.GRAY);
            participantsListPanel.add(emptyLabel);
        } else {
            for (final UserProfile participant : participants) {
                final JPanel participantItem = createParticipantItem(participant);
                participantsListPanel.add(participantItem);
                participantsListPanel.add(Box.createVerticalStrut(PARTICIPANT_ITEM_SPACING));
            }
        }

        participantsListPanel.revalidate();
        participantsListPanel.repaint();
    }

    /**
     * Creates a panel for a single participant item.
     * 
     * @param participant The participant user
     * @return A JPanel displaying the participant information
     */
    private JPanel createParticipantItem(final UserProfile participant) {
        final JPanel itemPanel = new JPanel(new BorderLayout(ITEM_PANEL_HGAP, ITEM_PANEL_VGAP));
        itemPanel.setBorder(new EmptyBorder(ITEM_BORDER_PADDING, ITEM_BORDER_PADDING,
                ITEM_BORDER_PADDING, ITEM_BORDER_PADDING));
        itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, ITEM_MAX_HEIGHT));

        // Participant name
        final String displayName = participant.getDisplayName();
        final String name;
        if (displayName != null && !displayName.isEmpty()) {
            name = displayName;
        } else {
            name = participant.getEmail();
        }
        final JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, NAME_FONT_SIZE));
        itemPanel.add(nameLabel, BorderLayout.WEST);

        // Online indicator (small circle)
        final JPanel indicatorPanel = new JPanel();
        indicatorPanel.setPreferredSize(new Dimension(INDICATOR_SIZE, INDICATOR_SIZE));
        indicatorPanel.setOpaque(true);
        indicatorPanel.setBackground(new Color(ONLINE_COLOR_R, ONLINE_COLOR_G, ONLINE_COLOR_B)); // Green for online
        itemPanel.add(indicatorPanel, BorderLayout.EAST);

        return itemPanel;
    }

    /**
     * Applies the current theme to the component.
     */
    private void applyTheme() {
        final ThemeManager themeManager = ThemeManager.getInstance();
        final Theme theme = themeManager.getCurrentTheme();

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
