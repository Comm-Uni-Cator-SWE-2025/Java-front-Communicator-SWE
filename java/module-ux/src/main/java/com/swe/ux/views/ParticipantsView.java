package com.swe.ux.views;

/**
 * Contributed by Pushti Vasoya.
 */

import com.swe.controller.Meeting.ParticipantRole;
import com.swe.controller.Meeting.UserProfile;
import com.swe.ux.binding.PropertyListeners;
import com.swe.ux.theme.Theme;
import com.swe.ux.theme.ThemeManager;
import com.swe.ux.viewmodels.ParticipantsViewModel;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
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
     * Subtitle font size.
     */
    private static final int SUBTITLE_FONT_SIZE = 12;
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
     * Avatar size.
     */
    private static final int AVATAR_SIZE = 36;
    /**
     * Avatar font size.
     */
    private static final int AVATAR_FONT_SIZE = 12;
    /**
     * List corner radius.
     */
    private static final int LIST_CORNER_RADIUS = 18;
    /**
     * Participant item key.
     */
    private static final String PARTICIPANT_ITEM_KEY = "participantItem";
    /**
     * Participant name key.
     */
    private static final String PARTICIPANT_NAME_KEY = "participantName";
    /**
     * Participant detail key.
     */
    private static final String PARTICIPANT_DETAIL_KEY = "participantDetail";
    /**
     * Participant badge key.
     */
    private static final String PARTICIPANT_BADGE_KEY = "participantBadge";

    /**
     * ViewModel for participants data.
     */
    private final ParticipantsViewModel viewModel;
    /**
     * Label displaying participant count.
     */
    private JLabel countLabel;
    /**
     * Subtitle label.
     */
    private JLabel subtitleLabel;
    /**
     * Panel containing the list of participants.
     */
    private JPanel participantsListPanel;
    /**
     * Scroll pane for the participants list.
     */
    private JScrollPane scrollPane;
    /**
     * Rounded list container.
     */
    private RoundedPanel listContainer;
    /** Cached theme colors. */
    private Color backgroundColor;
    private Color surfaceColor;
    private Color textColor;
    private Color mutedTextColor;
    private Color accentColor;

    /**
     * Creates a new ParticipantsView.
     * 
     * @param participantsViewModel The ParticipantsViewModel to use
     */
    public ParticipantsView(final ParticipantsViewModel participantsViewModel) {
        this.viewModel = participantsViewModel;
        initializeUI();
        setupBindings();
        ThemeManager.getInstance().addThemeChangeListener(() ->
                SwingUtilities.invokeLater(this::applyTheme));
        applyTheme();
    }

    /**
     * Initializes the UI components.
     */
    private void initializeUI() {
        setLayout(new BorderLayout(DEFAULT_PADDING, DEFAULT_PADDING));
        setBorder(new EmptyBorder(DEFAULT_PADDING, DEFAULT_PADDING, DEFAULT_PADDING, DEFAULT_PADDING));

        final JPanel headerPanel = new JPanel(new BorderLayout(0, 4));
        headerPanel.setOpaque(false);

        countLabel = new JLabel("Participants (0)", JLabel.LEFT);
        countLabel.setFont(new Font("Segoe UI", Font.BOLD, HEADER_FONT_SIZE));
        headerPanel.add(countLabel, BorderLayout.NORTH);

        subtitleLabel = new JLabel("Stay synced with everyone in the room", JLabel.LEFT);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, SUBTITLE_FONT_SIZE));
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);

        participantsListPanel = new JPanel();
        participantsListPanel.setLayout(new BoxLayout(participantsListPanel, BoxLayout.Y_AXIS));
        participantsListPanel.setOpaque(false);

        listContainer = new RoundedPanel(LIST_CORNER_RADIUS);
        listContainer.setLayout(new BorderLayout());
        listContainer.setBorder(new EmptyBorder(DEFAULT_PADDING, DEFAULT_PADDING, DEFAULT_PADDING, DEFAULT_PADDING));
        listContainer.add(participantsListPanel, BorderLayout.NORTH);

        scrollPane = new JScrollPane(listContainer);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

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
        applyTheme();
    }

    /**
     * Creates a panel for a single participant item.
     * 
     * @param participant The participant user
     * @return A JPanel displaying the participant information
     */
    private JPanel createParticipantItem(final UserProfile participant) {
        final JPanel itemPanel = new JPanel(new BorderLayout(ITEM_PANEL_HGAP, ITEM_PANEL_VGAP));
        itemPanel.setOpaque(false);
        itemPanel.setBorder(new EmptyBorder(ITEM_BORDER_PADDING, ITEM_BORDER_PADDING,
                ITEM_BORDER_PADDING, ITEM_BORDER_PADDING));
        itemPanel.putClientProperty(PARTICIPANT_ITEM_KEY, Boolean.TRUE);

        final JPanel leftPanel = new JPanel();
        leftPanel.setOpaque(false);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.X_AXIS));

        final String name = resolveDisplayName(participant);
        final AvatarCircle avatar = new AvatarCircle(extractInitials(name));
        leftPanel.add(avatar);
        leftPanel.add(Box.createHorizontalStrut(ITEM_PANEL_HGAP));

        final JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        final JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, NAME_FONT_SIZE));
        nameLabel.putClientProperty(PARTICIPANT_NAME_KEY, Boolean.TRUE);
        textPanel.add(nameLabel);

        final JLabel detailLabel = new JLabel(resolveDetailLine(participant));
        detailLabel.setFont(new Font("Segoe UI", Font.PLAIN, SUBTITLE_FONT_SIZE));
        detailLabel.putClientProperty(PARTICIPANT_DETAIL_KEY, Boolean.TRUE);
        textPanel.add(detailLabel);

        leftPanel.add(textPanel);

        final JLabel badgeLabel = new JLabel(resolveRoleBadge(participant));
        badgeLabel.setFont(new Font("Segoe UI", Font.BOLD, SUBTITLE_FONT_SIZE));
        badgeLabel.setOpaque(true);
        badgeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        badgeLabel.setBorder(new EmptyBorder(4, 12, 4, 12));
        badgeLabel.putClientProperty(PARTICIPANT_BADGE_KEY, Boolean.TRUE);

        itemPanel.add(leftPanel, BorderLayout.CENTER);
        itemPanel.add(badgeLabel, BorderLayout.EAST);

        applyThemeToComponentTree(itemPanel);
        return itemPanel;
    }

    private String resolveDisplayName(final UserProfile participant) {
        final String displayName = participant.getDisplayName();
        if (displayName != null && !displayName.isBlank()) {
            return displayName;
        }
        final String email = participant.getEmail();
        if (email != null && !email.isBlank()) {
            return email;
        }
        return "Guest";
    }

    private String resolveDetailLine(final UserProfile participant) {
        final String email = participant.getEmail();
        if (email != null && !email.isBlank()) {
            return email;
        }
        final ParticipantRole role = participant.getRole();
        if (role != null) {
            return capitalize(role.name());
        }
        return "Connected";
    }

    private String resolveRoleBadge(final UserProfile participant) {
        final ParticipantRole role = participant.getRole();
        if (role == null) {
            return "GUEST";
        }
        return role.name().replace('_', ' ');
    }

    private String capitalize(final String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        final String lower = value.toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    private String extractInitials(final String name) {
        if (name == null || name.isBlank()) {
            return "--";
        }
        final String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        final String first = parts[0];
        final String last = parts[parts.length - 1];
        return (first.substring(0, 1) + last.substring(0, 1)).toUpperCase();
    }

    /**
     * Applies the current theme to the component.
     */
    private void applyTheme() {
        final ThemeManager themeManager = ThemeManager.getInstance();
        final Theme theme = themeManager.getCurrentTheme();

        backgroundColor = theme.getBackgroundColor();
        textColor = theme.getTextColor();
        accentColor = theme.getPrimaryColor();
        mutedTextColor = blendColors(textColor, backgroundColor, theme.isDark() ? 0.65 : 0.4);
        surfaceColor = blendColors(backgroundColor, theme.getForeground(), theme.isDark() ? 0.25 : 0.85);

        setBackground(backgroundColor);
        if (countLabel != null) {
            countLabel.setForeground(textColor);
        }
        if (subtitleLabel != null) {
            subtitleLabel.setForeground(mutedTextColor);
        }
        if (listContainer != null) {
            listContainer.setFillColor(surfaceColor);
            listContainer.setBorderColor(blendColors(surfaceColor, accentColor,
                    theme.isDark() ? 0.4 : 0.15));
        }
        if (scrollPane != null) {
            scrollPane.getViewport().setBackground(backgroundColor);
            scrollPane.setBackground(backgroundColor);
        }

        styleParticipantItems();
    }

    private void styleParticipantItems() {
        if (participantsListPanel == null) {
            return;
        }
        for (Component comp : participantsListPanel.getComponents()) {
            if (comp instanceof JPanel panel
                    && Boolean.TRUE.equals(panel.getClientProperty(PARTICIPANT_ITEM_KEY))) {
                styleParticipantItem(panel);
            }
        }
    }

    private void styleParticipantItem(final JPanel panel) {
        final Color rowColor = blendColors(surfaceColor, accentColor, 0.08);
        panel.setOpaque(true);
        panel.setBackground(rowColor);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, ITEM_MAX_HEIGHT));
        applyThemeToComponentTree(panel);
    }

    private void applyThemeToComponentTree(final Component component) {
        if (component instanceof JLabel label) {
            if (Boolean.TRUE.equals(label.getClientProperty(PARTICIPANT_NAME_KEY))) {
                label.setForeground(textColor);
            } else if (Boolean.TRUE.equals(label.getClientProperty(PARTICIPANT_DETAIL_KEY))) {
                label.setForeground(mutedTextColor);
            } else if (Boolean.TRUE.equals(label.getClientProperty(PARTICIPANT_BADGE_KEY))) {
                label.setForeground(Color.WHITE);
                label.setBackground(accentColor);
            }
        } else if (component instanceof AvatarCircle avatar) {
            final Color avatarBg = blendColors(accentColor, surfaceColor, 0.25);
            avatar.setPalette(avatarBg, Color.WHITE);
        }

        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                applyThemeToComponentTree(child);
            }
        }
    }

    private Color blendColors(final Color base, final Color mix, final double ratioParam) {
        if (base == null && mix == null) {
            return null;
        }
        if (base == null) {
            return mix;
        }
        if (mix == null) {
            return base;
        }
        final double ratio = Math.max(0, Math.min(1, ratioParam));
        final double inverse = 1 - ratio;
        final int r = (int) Math.round(base.getRed() * inverse + mix.getRed() * ratio);
        final int g = (int) Math.round(base.getGreen() * inverse + mix.getGreen() * ratio);
        final int b = (int) Math.round(base.getBlue() * inverse + mix.getBlue() * ratio);
        return new Color(r, g, b);
    }

    /**
     * Rounded container for list section.
     */
    private static final class RoundedPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private final int cornerRadius;
        private Color fillColor = Color.WHITE;
        private Color borderColor = new Color(0, 0, 0, 30);

        RoundedPanel(final int radius) {
            this.cornerRadius = radius;
            setOpaque(false);
        }

        void setFillColor(final Color color) {
            if (color != null) {
                this.fillColor = color;
                repaint();
            }
        }

        void setBorderColor(final Color color) {
            if (color != null) {
                this.borderColor = color;
                repaint();
            }
        }

        @Override
        protected void paintComponent(final Graphics g) {
            final Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            final int width = getWidth();
            final int height = getHeight();
            g2.setColor(fillColor);
            g2.fillRoundRect(0, 0, width - 1, height - 1, cornerRadius, cornerRadius);
            g2.setColor(borderColor);
            g2.drawRoundRect(0, 0, width - 1, height - 1, cornerRadius, cornerRadius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Avatar circle for participant initials.
     */
    private static final class AvatarCircle extends JComponent {
        private static final long serialVersionUID = 1L;
        private final String initials;
        private Color backgroundColor = new Color(ONLINE_COLOR_R, ONLINE_COLOR_G, ONLINE_COLOR_B);
        private Color textColor = Color.WHITE;

        AvatarCircle(final String initialsParam) {
            this.initials = initialsParam;
            setFont(new Font("Segoe UI", Font.BOLD, AVATAR_FONT_SIZE));
            final Dimension size = new Dimension(AVATAR_SIZE, AVATAR_SIZE);
            setPreferredSize(size);
            setMinimumSize(size);
            setMaximumSize(size);
        }

        void setPalette(final Color bg, final Color text) {
            if (bg != null) {
                this.backgroundColor = bg;
            }
            if (text != null) {
                this.textColor = text;
            }
            repaint();
        }

        @Override
        protected void paintComponent(final Graphics g) {
            final Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            final int size = Math.min(getWidth(), getHeight());
            g2.setColor(backgroundColor);
            g2.fillOval(0, 0, size, size);
            g2.setColor(textColor);
            g2.setFont(getFont());
            final FontMetrics fm = g2.getFontMetrics();
            final int textWidth = fm.stringWidth(initials);
            final int ascent = fm.getAscent();
            final int x = (size - textWidth) / 2;
            final int y = (size - fm.getHeight()) / 2 + ascent;
            g2.drawString(initials, x, y);
            g2.dispose();
        }
    }
}
