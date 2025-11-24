package com.swe.ux.views;

import com.swe.controller.Meeting.UserProfile;
import com.swe.ux.theme.Theme;
import com.swe.ux.theme.ThemeManager;
import com.swe.ux.ui.AnalogClockPanel;
import com.swe.ux.ui.FrostedBackgroundPanel;
import com.swe.ux.ui.FrostedBadgeLabel;
import com.swe.ux.ui.FrostedToolbarButton;
import com.swe.ux.ui.MiniCalendarPanel;
import com.swe.ux.ui.PlaceholderTextField;
import com.swe.ux.ui.SoftCardPanel;
import com.swe.ux.ui.ThemeToggleButton;
import com.swe.ux.ui.FontUtil;
import com.swe.ux.viewmodels.DashboardViewModel;
import com.swe.ux.viewmodels.MainViewModel;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Simplified Main Page: small header, large insights pane, and
 * meeting join/start controls left-aligned.
 */
public class MainPage extends FrostedBackgroundPanel {

    private final MainViewModel viewModel;

    private SoftCardPanel headerCard;
    private SoftCardPanel insightsCard;
    private JFXPanel dashboardFxPanel;

    private JLabel welcomeLabel;
    private JLabel subtitleLabel;
    private JLabel dateLabel;
    private JLabel locationLabel;

    private JTextField meetingCodeField;

    private FrostedToolbarButton joinMeetingButton;
    private FrostedToolbarButton createMeetingButton;
    private FrostedToolbarButton logoutButton;

    private Timer minuteTimer;
    private boolean uiCreated = false;
    private final DashboardViewModel dashboardViewModel;

    public MainPage(MainViewModel viewModel) {
        this.viewModel = viewModel;
        this.dashboardViewModel = new DashboardViewModel();
        initializeUI();
        uiCreated = true;
        setupBindings();
        startClock();
        applyTheme();
    }

    /**
     * Enables or disables the Start Meeting button and related UI controls.
     */
    /**
     * Enables or disables the Start Meeting button and related UI controls.
     */
    public void setStartControlsEnabled(boolean enabled) {
        try {
            if (this.createMeetingButton != null) {
                this.createMeetingButton.setEnabled(enabled);
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Enables or disables the Join Meeting button and related UI controls.
     */
    public void setJoinControlsEnabled(boolean enabled) {
        try {
            if (this.joinMeetingButton != null) {
                this.joinMeetingButton.setEnabled(enabled);
            }
            if (this.meetingCodeField != null) {
                this.meetingCodeField.setEnabled(enabled);
            }
        } catch (Exception ignored) {
        }
    }

    private void initializeUI() {

        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(20, 30, 30, 30));

        // ------------------------------------------------------
        // SMALL HEADER
        // ------------------------------------------------------
        headerCard = new SoftCardPanel(24);
        headerCard.setCornerRadius(24);
        headerCard.setLayout(new BorderLayout(20, 10));
        headerCard.setPreferredSize(new Dimension(0, 150)); // 🔥 SMALL HEIGHT

        JPanel textStack = new JPanel();
        textStack.setOpaque(false);
        textStack.setLayout(new BoxLayout(textStack, BoxLayout.Y_AXIS));

        welcomeLabel = new JLabel("Welcome");
        welcomeLabel.setFont(FontUtil.getJetBrainsMono(22f, Font.BOLD));

        subtitleLabel = new JLabel("A quick meeting can solve a lot!");
        subtitleLabel.setFont(FontUtil.getJetBrainsMono(13f, Font.PLAIN));

        textStack.add(welcomeLabel);
        textStack.add(Box.createVerticalStrut(4));
        textStack.add(subtitleLabel);

        JPanel metaRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        metaRow.setOpaque(false);

        dateLabel = new JLabel(formatDate(new Date()));
        dateLabel.setFont(FontUtil.getJetBrainsMono(12f, Font.PLAIN));

        locationLabel = new JLabel("Start or Join one now");
        locationLabel.setFont(FontUtil.getJetBrainsMono(12f, Font.PLAIN));

        metaRow.add(dateLabel);
        metaRow.add(new FrostedBadgeLabel("Campus"));
        metaRow.add(locationLabel);

        textStack.add(Box.createVerticalStrut(6));
        textStack.add(metaRow);

        // Right side: theme toggle + logout
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(new ThemeToggleButton());

        logoutButton = new FrostedToolbarButton("Logout");
        logoutButton.addActionListener(e -> viewModel.logout());
        actions.add(logoutButton);

        headerCard.add(textStack, BorderLayout.CENTER);
        headerCard.add(actions, BorderLayout.EAST);

        add(headerCard, BorderLayout.NORTH);

        // Replace meetingCodeField definition:

        // Inside initializeUI(), REPLACE your joinPanel block with this:

        // ------------------------------------------------------
        // JOIN / START MEETING BAR (centered UNDER HEADER)
        // ------------------------------------------------------
        JPanel joinPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        joinPanel.setOpaque(false);

        // Placeholder text field
        meetingCodeField = new PlaceholderTextField("Meeting ID");
        meetingCodeField.setPreferredSize(new Dimension(280, 50));
        meetingCodeField.setFont(FontUtil.getJetBrainsMono(16f, Font.PLAIN));

        meetingCodeField.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        meetingCodeField.setPreferredSize(new Dimension(280, 50)); // wider + taller
        joinPanel.add(meetingCodeField);

        // Join button
        joinMeetingButton = new FrostedToolbarButton("Join");
        joinMeetingButton.setPreferredSize(new Dimension(130, 50)); // 🔥 wider pill
        joinMeetingButton.addActionListener(e -> {
            viewModel.meetingCode.set(meetingCodeField.getText());
            viewModel.joinMeetingRequested.set(true);
        });
        joinPanel.add(joinMeetingButton);

        // Start button
        createMeetingButton = new FrostedToolbarButton("Start");
        createMeetingButton.setPreferredSize(new Dimension(130, 50)); // 🔥 wider pill
        createMeetingButton.addActionListener(e -> viewModel.startMeetingRequested.set(true));
        joinPanel.add(createMeetingButton);

        // Place it ABOVE insights

        // ------------------------------------------------------
        // CONTENT AREA
        // ------------------------------------------------------

        JPanel mainArea = new JPanel(new BorderLayout(20, 20));
        mainArea.setOpaque(false);
        mainArea.add(joinPanel, BorderLayout.NORTH);

        // Big insights panel
        insightsCard = new SoftCardPanel(32);
        insightsCard.setLayout(new BorderLayout());
        initializeDashboardInsights();

        mainArea.add(insightsCard, BorderLayout.CENTER);

        // Sidebar widgets (clock + calendar)
        JPanel sideColumn = new JPanel();
        sideColumn.setOpaque(false);
        sideColumn.setLayout(new BoxLayout(sideColumn, BoxLayout.Y_AXIS));

        SoftCardPanel clockCard = new SoftCardPanel(20);
        clockCard.setLayout(new BorderLayout());
        clockCard.add(new AnalogClockPanel(), BorderLayout.CENTER);
        clockCard.setMaximumSize(new Dimension(260, 260));
        clockCard.setAlignmentX(Component.CENTER_ALIGNMENT);

        SoftCardPanel calendarCard = new SoftCardPanel(20);
        calendarCard.setLayout(new BorderLayout());
        calendarCard.add(new MiniCalendarPanel(), BorderLayout.CENTER);
        calendarCard.setMaximumSize(new Dimension(260, 280));
        calendarCard.setAlignmentX(Component.CENTER_ALIGNMENT);

        sideColumn.add(clockCard);
        sideColumn.add(Box.createVerticalStrut(20));
        sideColumn.add(calendarCard);

        mainArea.add(sideColumn, BorderLayout.EAST);

        add(mainArea, BorderLayout.CENTER);
    }

    // ----------------------------------------------------------
    // Bindings, updates, clock and theme logic
    // ----------------------------------------------------------
    private void setupBindings() {
        viewModel.currentUser.addListener(evt -> SwingUtilities.invokeLater(this::updateUserInfo));
        updateUserInfo();
    }

    private void updateUserInfo() {
        UserProfile user = viewModel.currentUser.get();
        welcomeLabel.setText(
                user != null ? "Welcome, " + user.getDisplayName() : "Welcome");
    }

    private void startClock() {
        if (minuteTimer != null)
            minuteTimer.stop();

        minuteTimer = new Timer(60_000, e -> dateLabel.setText(formatDate(new Date())));
        minuteTimer.setInitialDelay(0);
        minuteTimer.start();
    }

    private String formatDate(Date date) {
        return new SimpleDateFormat("EEE, MMM d • hh:mm a").format(date);
    }

    private void applyTheme() {
        if (!uiCreated)
            return;

        Theme theme = ThemeManager.getInstance().getCurrentTheme();
        if (theme == null)
            return;

        setBackground(theme.getBackgroundColor());

        welcomeLabel.setForeground(theme.getTextColor());
        subtitleLabel.setForeground(theme.getTextColor());
        dateLabel.setForeground(theme.getTextColor());
        locationLabel.setForeground(theme.getTextColor());

        meetingCodeField.setBackground(theme.getInputBackgroundColor());
        meetingCodeField.setForeground(theme.getTextColor());
        meetingCodeField.setCaretColor(theme.getTextColor());

        ThemeManager.getInstance().applyThemeRecursively(headerCard);
        ThemeManager.getInstance().applyThemeRecursively(insightsCard);

        revalidate();
        repaint();
    }

    private void initializeDashboardInsights() {
        dashboardFxPanel = new JFXPanel();
        insightsCard.add(dashboardFxPanel, BorderLayout.CENTER);

        Platform.setImplicitExit(false);
        Platform.runLater(() -> {
            DashboardView dashboardView = new DashboardView();
            dashboardView.setViewModel(dashboardViewModel);
            Scene scene = new Scene(dashboardView);
            dashboardFxPanel.setScene(scene);
        });
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            updateUserInfo();
            applyTheme();
            startClock();
        } else if (minuteTimer != null) {
            minuteTimer.stop();
        }
    }

    @Override
    public void updateUI() {
        super.updateUI();
        if (uiCreated) {
            applyTheme();
        }
    }
}
