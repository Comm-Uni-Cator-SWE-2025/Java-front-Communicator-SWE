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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Simplified Main Page: small header, large insights pane, and
 * meeting join/start controls left-aligned.
 */
public class MainPage extends FrostedBackgroundPanel {
    /**
     * Default spacing value.
     */
    private static final int DEFAULT_SPACING = 20;
    /**
     * Default padding value.
     */
    private static final int DEFAULT_PADDING = 30;
    /**
     * Header height.
     */
    private static final int HEADER_HEIGHT = 150;
    /**
     * Header corner radius.
     */
    private static final int HEADER_CORNER_RADIUS = 24;
    /**
     * Insights corner radius.
     */
    private static final int INSIGHTS_CORNER_RADIUS = 32;
    /**
     * Widget corner radius.
     */
    private static final int WIDGET_CORNER_RADIUS = 20;
    /**
     * Clock card size.
     */
    private static final int CLOCK_CARD_SIZE = 260;
    /**
     * Calendar card height.
     */
    private static final int CALENDAR_CARD_HEIGHT = 280;
    /**
     * Button width.
     */
    private static final int BUTTON_WIDTH = 130;
    /**
     * Button height.
     */
    private static final int BUTTON_HEIGHT = 50;
    /**
     * Field width.
     */
    private static final int FIELD_WIDTH = 280;
    /**
     * Field padding top.
     */
    private static final int FIELD_PADDING_TOP = 12;
    /**
     * Field padding left.
     */
    private static final int FIELD_PADDING_LEFT = 14;
    /**
     * Field padding bottom.
     */
    private static final int FIELD_PADDING_BOTTOM = 12;
    /**
     * Field padding right.
     */
    private static final int FIELD_PADDING_RIGHT = 14;
    /**
     * Welcome font size.
     */
    private static final int WELCOME_FONT_SIZE = 22;
    /**
     * Subtitle font size.
     */
    private static final int SUBTITLE_FONT_SIZE = 13;
    /**
     * Date font size.
     */
    private static final int DATE_FONT_SIZE = 12;
    /**
     * Field font size.
     */
    private static final int FIELD_FONT_SIZE = 16;
    /**
     * Vertical strut size 4.
     */
    private static final int VERTICAL_STRUT_4 = 4;
    /**
     * Vertical strut size 6.
     */
    private static final int VERTICAL_STRUT_6 = 6;
    /**
     * Header vertical gap.
     */
    private static final int HEADER_VERTICAL_GAP = 10;
    /**
     * Timer delay in milliseconds.
     */
    private static final int TIMER_DELAY_MS = 60000;

    /**
     * ViewModel for main page.
     */
    private final MainViewModel viewModel;
    /**
     * Header card panel.
     */
    private SoftCardPanel headerCard;
    /**
     * Insights card panel.
     */
    private SoftCardPanel insightsCard;
    /**
     * Dashboard JavaFX panel.
     */
    private JFXPanel dashboardFxPanel;
    /**
     * Welcome label.
     */
    private JLabel welcomeLabel;
    /**
     * Subtitle label.
     */
    private JLabel subtitleLabel;
    /**
     * Date label.
     */
    private JLabel dateLabel;
    /**
     * Location label.
     */
    private JLabel locationLabel;
    /**
     * Meeting code input field.
     */
    private JTextField meetingCodeField;
    /**
     * Join meeting button.
     */
    private FrostedToolbarButton joinMeetingButton;
    /**
     * Create meeting button.
     */
    private FrostedToolbarButton createMeetingButton;
    /**
     * Logout button.
     */
    private FrostedToolbarButton logoutButton;
    /**
     * Timer for updating clock.
     */
    private Timer minuteTimer;
    /**
     * Whether UI has been created.
     */
    private boolean uiCreated = false;
    /**
     * Dashboard view model.
     */
    private final DashboardViewModel dashboardViewModel;

    /**
     * Creates a new MainPage.
     *
     * @param mainViewModel the main view model
     */
    public MainPage(final MainViewModel mainViewModel) {
        this.viewModel = mainViewModel;
        
        this.dashboardViewModel = new DashboardViewModel();
        initializeUI();
        uiCreated = true;
        setupBindings();
        startClock();
        applyTheme();
    }

    /**
     * Enables or disables the Start Meeting button and related UI controls.
     *
     * @param enabled whether to enable the controls
     */
    public void setStartControlsEnabled(final boolean enabled) {
        try {
            if (this.createMeetingButton != null) {
                this.createMeetingButton.setEnabled(enabled);
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Enables or disables the Join Meeting button and related UI controls.
     *
     * @param enabled whether to enable the controls
     */
    public void setJoinControlsEnabled(final boolean enabled) {
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

    // CHECKSTYLE:OFF: JavaNCSS - Complex UI initialization method
    private void initializeUI() {
        // CHECKSTYLE:ON: JavaNCSS
        setLayout(new BorderLayout(DEFAULT_SPACING, DEFAULT_SPACING));
        setBorder(new EmptyBorder(DEFAULT_SPACING, DEFAULT_PADDING, DEFAULT_PADDING, DEFAULT_PADDING));

        // ------------------------------------------------------
        // SMALL HEADER
        // ------------------------------------------------------
        headerCard = new SoftCardPanel(HEADER_CORNER_RADIUS);
        headerCard.setCornerRadius(HEADER_CORNER_RADIUS);
        headerCard.setLayout(new BorderLayout(DEFAULT_SPACING, HEADER_VERTICAL_GAP));
        headerCard.setPreferredSize(new Dimension(0, HEADER_HEIGHT));

        final JPanel textStack = new JPanel();
        textStack.setOpaque(false);
        textStack.setLayout(new BoxLayout(textStack, BoxLayout.Y_AXIS));

        welcomeLabel = new JLabel("Welcome");
        welcomeLabel.setFont(FontUtil.getJetBrainsMono(WELCOME_FONT_SIZE, Font.BOLD));

        subtitleLabel = new JLabel("A quick meeting can solve a lot!");
        subtitleLabel.setFont(FontUtil.getJetBrainsMono(SUBTITLE_FONT_SIZE, Font.PLAIN));

        textStack.add(welcomeLabel);
        textStack.add(Box.createVerticalStrut(VERTICAL_STRUT_4));
        textStack.add(subtitleLabel);

        final JPanel metaRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        metaRow.setOpaque(false);

        dateLabel = new JLabel(formatDate(new Date()));
        dateLabel.setFont(FontUtil.getJetBrainsMono(DATE_FONT_SIZE, Font.PLAIN));

        locationLabel = new JLabel("Start or Join one now");
        locationLabel.setFont(FontUtil.getJetBrainsMono(DATE_FONT_SIZE, Font.PLAIN));

        metaRow.add(dateLabel);
        metaRow.add(new FrostedBadgeLabel("Campus"));
        metaRow.add(locationLabel);

        textStack.add(Box.createVerticalStrut(VERTICAL_STRUT_6));
        textStack.add(metaRow);

        // Right side: theme toggle + logout
        final JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
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
        final JPanel joinPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, DEFAULT_SPACING, 5));
        joinPanel.setOpaque(false);

        // Placeholder text field
        meetingCodeField = new PlaceholderTextField("Meeting ID");
        meetingCodeField.setPreferredSize(new Dimension(FIELD_WIDTH, BUTTON_HEIGHT));
        meetingCodeField.setFont(FontUtil.getJetBrainsMono(FIELD_FONT_SIZE, Font.PLAIN));

        meetingCodeField.setBorder(BorderFactory.createEmptyBorder(FIELD_PADDING_TOP, FIELD_PADDING_LEFT,
                FIELD_PADDING_BOTTOM, FIELD_PADDING_RIGHT));
        meetingCodeField.setPreferredSize(new Dimension(FIELD_WIDTH, BUTTON_HEIGHT));
        joinPanel.add(meetingCodeField);

        // Join button
        joinMeetingButton = new FrostedToolbarButton("Join");
        joinMeetingButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        joinMeetingButton.addActionListener(e -> {
            viewModel.getMeetingCode().set(meetingCodeField.getText());
            viewModel.getJoinMeetingRequested().set(true);
        });
        joinPanel.add(joinMeetingButton);

        // Start button
        createMeetingButton = new FrostedToolbarButton("Start");
        createMeetingButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        createMeetingButton.addActionListener(e -> viewModel.getStartMeetingRequested().set(true));
        joinPanel.add(createMeetingButton);

        // Place it ABOVE insights

        // ------------------------------------------------------
        // CONTENT AREA
        // ------------------------------------------------------

        final JPanel mainArea = new JPanel(new BorderLayout(DEFAULT_SPACING, DEFAULT_SPACING));
        mainArea.setOpaque(false);
        mainArea.add(joinPanel, BorderLayout.NORTH);

        // Big insights panel
        insightsCard = new SoftCardPanel(INSIGHTS_CORNER_RADIUS);
        insightsCard.setLayout(new BorderLayout());
        initializeDashboardInsights();

        mainArea.add(insightsCard, BorderLayout.CENTER);

        // Sidebar widgets (clock + calendar)
        final JPanel sideColumn = new JPanel();
        sideColumn.setOpaque(false);
        sideColumn.setLayout(new BoxLayout(sideColumn, BoxLayout.Y_AXIS));

        final SoftCardPanel clockCard = new SoftCardPanel(WIDGET_CORNER_RADIUS);
        clockCard.setLayout(new BorderLayout());
        clockCard.add(new AnalogClockPanel(), BorderLayout.CENTER);
        clockCard.setMaximumSize(new Dimension(CLOCK_CARD_SIZE, CLOCK_CARD_SIZE));
        clockCard.setAlignmentX(Component.CENTER_ALIGNMENT);

        final SoftCardPanel calendarCard = new SoftCardPanel(WIDGET_CORNER_RADIUS);
        calendarCard.setLayout(new BorderLayout());
        calendarCard.add(new MiniCalendarPanel(), BorderLayout.CENTER);
        calendarCard.setMaximumSize(new Dimension(CLOCK_CARD_SIZE, CALENDAR_CARD_HEIGHT));
        calendarCard.setAlignmentX(Component.CENTER_ALIGNMENT);

        sideColumn.add(clockCard);
        sideColumn.add(Box.createVerticalStrut(DEFAULT_SPACING));
        sideColumn.add(calendarCard);

        mainArea.add(sideColumn, BorderLayout.EAST);

        add(mainArea, BorderLayout.CENTER);
    }

    // ----------------------------------------------------------
    // Bindings, updates, clock and theme logic
    // ----------------------------------------------------------
    private void setupBindings() {
        viewModel.getCurrentUser().addListener(evt -> SwingUtilities.invokeLater(this::updateUserInfo));
        updateUserInfo();
    }

    private void updateUserInfo() {
        final UserProfile user = viewModel.getCurrentUser().get();
        final String welcomeText;
        if (user != null) {
            welcomeText = "Welcome, " + user.getDisplayName();
        } else {
            welcomeText = "Welcome";
        }
        welcomeLabel.setText(welcomeText);
        dashboardViewModel.refreshForUser(user);
    }

    private void startClock() {
        if (minuteTimer != null) {
            minuteTimer.stop();
        }

        minuteTimer = new Timer(TIMER_DELAY_MS, e -> dateLabel.setText(formatDate(new Date())));
        minuteTimer.setInitialDelay(0);
        minuteTimer.start();
    }

    private String formatDate(final Date date) {
        return new SimpleDateFormat("EEE, MMM d • hh:mm a").format(date);
    }

    private void applyTheme() {
        if (!uiCreated) {
            return;
        }

        final Theme theme = ThemeManager.getInstance().getCurrentTheme();
        if (theme == null) {
            return;
        }

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
            final DashboardView dashboardView = new DashboardView();
            dashboardView.setViewModel(dashboardViewModel);
            final Scene scene = new Scene(dashboardView);
            dashboardFxPanel.setScene(scene);
        });
    }

    @Override
    public void setVisible(final boolean visible) {
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
