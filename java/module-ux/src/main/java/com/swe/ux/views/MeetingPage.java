package com.swe.ux.views;

import com.swe.canvas.datamodel.canvas.CanvasState;
import com.swe.canvas.datamodel.collaboration.CanvasNetworkService;
import com.swe.canvas.datamodel.manager.ClientActionManager;
import com.swe.canvas.datamodel.manager.HostActionManager;
import com.swe.controller.Meeting.ParticipantRole;
import com.swe.screenNVideo.Utils;
import com.swe.ux.App;
import com.swe.ux.binding.PropertyListeners;
import com.swe.ux.theme.ThemeManager;
import com.swe.ux.ui.FrostedBackgroundPanel;
import com.swe.ux.ui.FrostedBadgeLabel;
import com.swe.ux.ui.FrostedToolbarButton;
import com.swe.ux.ui.MeetingControlButton;
import com.swe.ux.ui.MeetingStageTabs;
import com.swe.ux.ui.ModernTabbedPaneUI;
import com.swe.ux.ui.QuickDoubtPopup;
import com.swe.ux.ui.SoftCardPanel;
import com.swe.ux.ui.ThemeToggleButton;
import com.swe.ux.ui.FontUtil;
import com.swe.ux.viewmodels.ChatViewModel;
import com.swe.ux.viewmodels.MeetingViewModel;
import com.swe.ux.viewmodels.ParticipantsViewModel;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * MeetingPage.
 *
 * <p>Stage on the left (tabs: Screen + Video, Canvas, AI Insights)
 * Right sidebar with internal tabs: Chat | Participants.
 *
 * <p>- Participants button in header
 * - Chat button in bottom bar
 * - Hide Panels in header (toggles sidebar visibility)
 * - Theme-consistent and re-applies custom tab UI on theme changes
 */
public class MeetingPage extends FrostedBackgroundPanel {
    /** Minimum sidebar width. */
    private static final int SIDEBAR_MIN_WIDTH = 280;
    /** Maximum sidebar width. */
    private static final int SIDEBAR_MAX_WIDTH = 520;
    /** Default sidebar width. */
    private static final int SIDEBAR_DEFAULT_WIDTH = 360;
    /** Accent blue color. */
    private static final Color ACCENT_BLUE = new Color(82, 140, 255);
    /** Layout gap size. */
    private static final int LAYOUT_GAP = 20;
    /** Border padding size. */
    private static final int BORDER_PADDING = 24;
    /** Header card blur radius. */
    private static final int HEADER_BLUR_RADIUS = 10;
    /** Header corner radius. */
    private static final int HEADER_CORNER_RADIUS = 18;
    /** Header border padding. */
    private static final int HEADER_BORDER_PADDING = 6;
    /** Flow layout gap. */
    private static final int FLOW_LAYOUT_GAP = 10;
    /** Title font size. */
    private static final float TITLE_FONT_SIZE = 20.0f;
    /** Default font size. */
    private static final float DEFAULT_FONT_SIZE = 12.0f;
    /** Stage card blur radius. */
    private static final int STAGE_BLUR_RADIUS = 18;
    /** Stage layout gap. */
    private static final int STAGE_LAYOUT_GAP = 12;
    /** Stage corner radius. */
    private static final int STAGE_CORNER_RADIUS = 26;
    /** Stage preferred height. */
    private static final int STAGE_PREFERRED_HEIGHT = 680;
    /** Stage border padding. */
    private static final int STAGE_BORDER_PADDING = 6;
    /** Stage content border padding. */
    private static final int STAGE_CONTENT_BORDER = 4;
    /** Stage minimum width. */
    private static final int STAGE_MIN_WIDTH = 400;
    /** Sidebar blur radius. */
    private static final int SIDEBAR_BLUR_RADIUS = 10;
    /** Sidebar layout gap. */
    private static final int SIDEBAR_LAYOUT_GAP = 8;
    /** Panel blur radius. */
    private static final int PANEL_BLUR_RADIUS = 12;
    /** Controls bar blur radius. */
    private static final int CONTROLS_BLUR_RADIUS = 14;
    /** Controls corner radius. */
    private static final int CONTROLS_CORNER_RADIUS = 36;
    /** Controls layout gap horizontal. */
    private static final int CONTROLS_GAP_H = 12;
    /** Controls layout gap vertical. */
    private static final int CONTROLS_GAP_V = 10;
    /** Leave button red color component. */
    private static final int LEAVE_RED = 229;
    /** Leave button green color component. */
    private static final int LEAVE_GREEN = 57;
    /** Leave button blue color component. */
    private static final int LEAVE_BLUE = 53;
    /** Leave button alpha color component. */
    private static final int LEAVE_ALPHA = 200;
    /** Split pane divider size. */
    private static final int SPLIT_DIVIDER_SIZE = 10;
    /** Split pane resize weight. */
    private static final double SPLIT_RESIZE_WEIGHT = 0.8;
    /** Timer delay milliseconds. */
    private static final int TIMER_DELAY_MS = 1000;
    /** Copy feedback timer delay. */
    private static final int COPY_FEEDBACK_DELAY_MS = 3000;
    /** Icon size. */
    private static final int ICON_SIZE = 18;
    /** Icon bar width. */
    private static final int ICON_BAR_WIDTH = 3;
    /** Icon spacing. */
    private static final int ICON_SPACING = 4;
    /** Icon vertical offset. */
    private static final int ICON_VERTICAL_OFFSET = 3;
    /** Icon height adjustment. */
    private static final int ICON_HEIGHT_ADJUST = 6;
    /** Icon round corner. */
    private static final int ICON_ROUND_CORNER = 4;

    /** Meeting view model. */
    private final MeetingViewModel meetingViewModel;

    /** Header card panel. */
    private SoftCardPanel headerCard;
    /** Controls bar panel. */
    private SoftCardPanel controlsBar;
    /** Sidebar toggle button. */
    private JButton sidebarToggleBtn;
    /** Copy link button. */
    private FrostedToolbarButton btnCopyLink;

    /** Stage card panel. */
    private SoftCardPanel stageCard;
    /** Stage tabs component. */
    private MeetingStageTabs stageTabs;
    /** Stage content layout. */
    private CardLayout stageContentLayout;
    /** Stage content panel. */
    private JPanel stageContentPanel;

    /** Center split pane. */
    private JSplitPane centerSplit;
    /** Sidebar card panel. */
    private SoftCardPanel sidebarCard;
    /** Sidebar tabs. */
    private JTabbedPane sidebarTabs;
    /** Sidebar header label. */
    private JLabel sidebarHeaderLabel;
    /** Chat panel. */
    private JPanel chatPanel;
    /** Participants panel. */
    private JPanel participantsPanel;
    /** Sidebar visibility flag. */
    private boolean sidebarVisible = false;
    /** Last sidebar width. */
    private int lastSidebarWidth = SIDEBAR_DEFAULT_WIDTH;

    /** Camera control button. */
    private MeetingControlButton btnCamera;
    /** Share control button. */
    private MeetingControlButton btnShare;
    /** Leave control button. */
    private MeetingControlButton btnLeave;
    /** Mute control button. */
    private MeetingControlButton btnMute;
    /** Raise hand control button. */
    private MeetingControlButton btnRaiseHand;
    /** Chat control button. */
    private MeetingControlButton btnChat;
    /** People control button. */
    private MeetingControlButton btnPeople;
    /** Meeting controls button (host only). */
    private FrostedToolbarButton meetingControlsButton;

    /** Meeting ID badge. */
    private FrostedBadgeLabel meetingIdBadge;
    /** Live clock label. */
    private JLabel liveClockLabel;
    /** Role label. */
    private JLabel roleLabel;
    /** Live timer. */
    private Timer liveTimer;
    /** Quick doubt popup. */
    private final QuickDoubtPopup quickDoubtPopup;
    /** Hand raised flag. */
    private boolean isHandRaised = false;

    /**
     * Creates a new MeetingPage.
     *
     * @param meetingViewModelParam the meeting view model
     */
    public MeetingPage(final MeetingViewModel meetingViewModelParam) {
        this.meetingViewModel = meetingViewModelParam;
        initializeUI();
        quickDoubtPopup = new QuickDoubtPopup();
        quickDoubtPopup.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
            }

            @Override
            public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
                handleQuickDoubtClosed();
            }

            @Override
            public void popupMenuCanceled(final PopupMenuEvent e) {
                handleQuickDoubtClosed();
            }
        });
        registerThemeListener();
        setupBindings();
        startLiveClock();
        applyTheme();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(LAYOUT_GAP, LAYOUT_GAP));
        setBorder(new EmptyBorder(BORDER_PADDING, BORDER_PADDING, BORDER_PADDING, BORDER_PADDING));

        // Header
        headerCard = buildHeader();
        add(headerCard, BorderLayout.NORTH);

        // Center area: stage (left) + sidebar (right) inside a split pane for responsive resizing
        centerSplit = buildCenterSplit();
        add(centerSplit, BorderLayout.CENTER);

        // Bottom controls
        controlsBar = buildControlsBar();
        add(controlsBar, BorderLayout.SOUTH);
    }

    // ---------------- Header ----------------
    private SoftCardPanel buildHeader() {
        final SoftCardPanel card = new SoftCardPanel(HEADER_BLUR_RADIUS);
        card.setCornerRadius(HEADER_CORNER_RADIUS);
        final JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setBorder(new EmptyBorder(0, HEADER_BORDER_PADDING, 0, HEADER_BORDER_PADDING));

        final JPanel leftCluster = new JPanel(new FlowLayout(FlowLayout.LEFT, FLOW_LAYOUT_GAP, 0));
        leftCluster.setOpaque(false);
        final JLabel title = new JLabel("Live Meeting");
        title.setFont(FontUtil.getJetBrainsMono(TITLE_FONT_SIZE, Font.BOLD));
        leftCluster.add(title);

        final ThemeToggleButton toggle = new ThemeToggleButton();
        leftCluster.add(toggle);

        meetingIdBadge = new FrostedBadgeLabel("Meeting: --");
        leftCluster.add(meetingIdBadge);

        final FrostedBadgeLabel ipBadge = new FrostedBadgeLabel("IP: " + Utils.getSelfIP());
        leftCluster.add(ipBadge);

        roleLabel = new JLabel(buildRoleLabelText());
        roleLabel.setFont(FontUtil.getJetBrainsMono(DEFAULT_FONT_SIZE, Font.PLAIN));
        leftCluster.add(roleLabel);

        row.add(leftCluster);
        row.add(Box.createHorizontalGlue());

        final JPanel rightCluster = new JPanel(new FlowLayout(FlowLayout.RIGHT, FLOW_LAYOUT_GAP, 0));
        rightCluster.setOpaque(false);
        liveClockLabel = new JLabel("Live: --:--");
        liveClockLabel.setFont(FontUtil.getJetBrainsMono(DEFAULT_FONT_SIZE, Font.PLAIN));
        rightCluster.add(liveClockLabel);

        btnCopyLink = new FrostedToolbarButton("Copy Link");
        btnCopyLink.addActionListener(e -> copyMeetingId());
        rightCluster.add(btnCopyLink);

        meetingControlsButton = new FrostedToolbarButton("Meeting Controls");
        meetingControlsButton.addActionListener(e -> openMeetingControlsDialog());
        rightCluster.add(meetingControlsButton);
        updateMeetingControlAvailability();

        sidebarToggleBtn = new JButton(new SidebarToggleIcon());
        sidebarToggleBtn.setToolTipText("Toggle right panel");
        sidebarToggleBtn.setBorderPainted(false);
        sidebarToggleBtn.setContentAreaFilled(false);
        sidebarToggleBtn.setFocusPainted(false);
        sidebarToggleBtn.addActionListener(e -> toggleSidebarVisibility());
        rightCluster.add(sidebarToggleBtn);

        row.add(rightCluster);
        card.setLayout(new BorderLayout());
        card.add(row, BorderLayout.CENTER);
        return card;
    }

    // ---------------- Stage ----------------
    private JSplitPane buildCenterSplit() {
        stageCard = buildStageCard();
        sidebarCard = buildSidebarCard();

        final JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, stageCard, sidebarCard);
        split.setOpaque(false);
        split.setBorder(null);
        split.setDividerSize(SPLIT_DIVIDER_SIZE);
        split.setResizeWeight(SPLIT_RESIZE_WEIGHT);
        split.setContinuousLayout(true);
        split.setOneTouchExpandable(false);
        attachSplitListeners(split);

        stageCard.setMinimumSize(new Dimension(STAGE_MIN_WIDTH, 0));
        sidebarCard.setMinimumSize(new Dimension(SIDEBAR_MIN_WIDTH, 0));

        // start collapsed
        sidebarCard.setVisible(false);
        split.setDividerLocation(1.0);
        return split;
    }

    private SoftCardPanel buildStageCard() {
        final SoftCardPanel card = new SoftCardPanel(STAGE_BLUR_RADIUS);
        card.setLayout(new BorderLayout(STAGE_LAYOUT_GAP, STAGE_LAYOUT_GAP));
        card.setCornerRadius(STAGE_CORNER_RADIUS);
        card.setPreferredSize(new Dimension(0, STAGE_PREFERRED_HEIGHT));

        final Map<String, String> tabs = new LinkedHashMap<>();
        tabs.put("MEETING", "Meeting");
        tabs.put("CANVAS", "Canvas");
        tabs.put("INSIGHTS", "Analytics");

        stageTabs = new MeetingStageTabs(tabs, this::switchStageView);
        stageTabs.setAccentColor(ACCENT_BLUE);
        stageContentLayout = new CardLayout();
        stageContentPanel = new JPanel(stageContentLayout);
        stageContentPanel.setOpaque(false);
        stageContentPanel.setBorder(new EmptyBorder(STAGE_CONTENT_BORDER, STAGE_CONTENT_BORDER,
                STAGE_CONTENT_BORDER, STAGE_BORDER_PADDING));

        final ScreenNVideo screenNVideo = new ScreenNVideo(meetingViewModel);
        final CanvasPage canvasPage;
        final String userId;
        if (meetingViewModel.getCurrentUser() != null) {
            userId = meetingViewModel.getCurrentUser().getEmail();
        } else {
            userId = "user-" + System.nanoTime();
        }

        if (meetingViewModel.getCurrentUser().getRole() == ParticipantRole.INSTRUCTOR) {
            final CanvasState hostCanvasState = new CanvasState();
            final CanvasNetworkService networkService = new CanvasNetworkService(meetingViewModel.getRpc());
            final HostActionManager hostManager = new HostActionManager(userId, hostCanvasState,
                    networkService, meetingViewModel.getRpc());
            canvasPage = new CanvasPage(hostManager, userId, meetingViewModel.getRpc());
        } else {
            final CanvasState clientCanvasState = new CanvasState();
            final CanvasNetworkService networkService = new CanvasNetworkService(meetingViewModel.getRpc());
            final ClientActionManager clientManager = new ClientActionManager(userId, clientCanvasState,
                    networkService, meetingViewModel.getRpc());
            canvasPage = new CanvasPage(clientManager, userId, meetingViewModel.getRpc());
        }
        final SentimentInsightsPanel sentimentInsightsPanel = new SentimentInsightsPanel(meetingViewModel);

        stageContentPanel.add(wrap(screenNVideo), "MEETING");
        stageContentPanel.add(wrap(canvasPage), "CANVAS");
        stageContentPanel.add(wrap(sentimentInsightsPanel), "INSIGHTS");

        card.add(stageTabs, BorderLayout.NORTH);
        card.add(stageContentPanel, BorderLayout.CENTER);
        stageTabs.setSelectedTab("MEETING");
        stageContentLayout.show(stageContentPanel, "MEETING");
        return card;
    }

    private JPanel wrap(final JPanel p) {
        final JPanel w = new JPanel(new BorderLayout());
        w.setOpaque(false);
        w.add(p, BorderLayout.CENTER);
        w.setMinimumSize(new Dimension(0, 0));
        w.setBorder(new EmptyBorder(STAGE_CONTENT_BORDER, STAGE_CONTENT_BORDER,
                STAGE_CONTENT_BORDER, STAGE_CONTENT_BORDER));
        return w;
    }

    private void switchStageView(final String tabKey) {
        if (stageContentLayout == null || stageContentPanel == null) {
            return;
        }
        stageContentLayout.show(stageContentPanel, tabKey);
        if (stageTabs != null) {
            stageTabs.setSelectedTab(tabKey);
        }
    }

    // ---------------- Sidebar ----------------
    private SoftCardPanel buildSidebarCard() {
        final SoftCardPanel sb = new SoftCardPanel(SIDEBAR_BLUR_RADIUS);
        sb.setLayout(new BorderLayout(SIDEBAR_LAYOUT_GAP, SIDEBAR_LAYOUT_GAP));
        sb.setPreferredSize(new Dimension(SIDEBAR_DEFAULT_WIDTH, 0));
        sb.setVisible(false);

        final JPanel sidebarHeader = new JPanel(new BorderLayout());
        sidebarHeader.setOpaque(false);
        sidebarHeaderLabel = new JLabel("Panels");
        sidebarHeaderLabel.setFont(FontUtil.getJetBrainsMono(TITLE_FONT_SIZE, Font.BOLD));
        final JButton closeSidebarBtn = new JButton("x");
        closeSidebarBtn.setToolTipText("Hide panel");
        closeSidebarBtn.setFocusPainted(false);
        closeSidebarBtn.setBorderPainted(false);
        closeSidebarBtn.setContentAreaFilled(false);
        closeSidebarBtn.addActionListener(e -> toggleSidebarVisibility());
        sidebarHeader.add(sidebarHeaderLabel, BorderLayout.WEST);
        sidebarHeader.add(closeSidebarBtn, BorderLayout.EAST);

        // Internal tabs: Chat | Participants
        sidebarTabs = new JTabbedPane(SwingConstants.TOP);
        sidebarTabs.setOpaque(false);
        sidebarTabs.setUI(new ModernTabbedPaneUI());

        chatPanel = createChatPanel();
        participantsPanel = createParticipantsPanel();

        sidebarTabs.addTab("Chat", chatPanel);
        sidebarTabs.addTab("Participants", participantsPanel);

        sb.add(sidebarHeader, BorderLayout.NORTH);
        sb.add(sidebarTabs, BorderLayout.CENTER);
        return sb;
    }

    private JPanel createParticipantsPanel() {
        final SoftCardPanel panel = new SoftCardPanel(PANEL_BLUR_RADIUS);
        panel.setLayout(new BorderLayout());
        final ParticipantsViewModel pvm = new ParticipantsViewModel(meetingViewModel);
        panel.add(new ParticipantsView(pvm), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createChatPanel() {
        final SoftCardPanel panel = new SoftCardPanel(PANEL_BLUR_RADIUS);
        panel.setLayout(new BorderLayout());

        if (meetingViewModel == null || meetingViewModel.getRpc() == null) {
            final JLabel fallback = new JLabel("<html><center>Chat unavailable<br>(no RPC connection)</center></html>",
                    SwingConstants.CENTER);
            final float fallbackFontSize = 14.0f;
            fallback.setFont(FontUtil.getJetBrainsMono(fallbackFontSize, Font.PLAIN));
            panel.add(fallback, BorderLayout.CENTER);
            return panel;
        }

        final ChatViewModel chatViewModel = new ChatViewModel(meetingViewModel.getRpc(),
                meetingViewModel.getCurrentUser());
        final ChatView chatView = new ChatView(chatViewModel);
        panel.add(chatView, BorderLayout.CENTER);
        return panel;
    }

    private void openSidebarToTab(final String tabName) {
        final int widthHint;
        if (sidebarCard.isVisible()) {
            widthHint = getCurrentSidebarWidth();
        } else {
            widthHint = lastSidebarWidth;
        }
        final int finalWidth;
        if (widthHint <= 0) {
            finalWidth = SIDEBAR_DEFAULT_WIDTH;
        } else {
            finalWidth = widthHint;
        }
        ensureSidebarShowing(finalWidth);

        // select tab if exists
        for (int i = 0; i < sidebarTabs.getTabCount(); i++) {
            final String title = sidebarTabs.getTitleAt(i);
            if (title != null && title.equalsIgnoreCase(tabName)) {
                sidebarTabs.setSelectedIndex(i);
                break;
            }
        }

        if (sidebarHeaderLabel != null) {
            sidebarHeaderLabel.setText(tabName);
        }
        if (btnChat != null) {
            btnChat.setActive("Chat".equalsIgnoreCase(tabName));
        }
        if (btnPeople != null) {
            btnPeople.setActive("Participants".equalsIgnoreCase(tabName));
        }

        refreshLayoutContainers();
    }

    private void handleControlTabButton(final String tabName) {
        final String current = getCurrentSidebarTab();
        if (sidebarCard.isVisible() && current != null && tabName.equalsIgnoreCase(current)) {
            toggleSidebarVisibility();
        } else {
            openSidebarToTab(tabName);
        }
    }

    private String getCurrentSidebarTab() {
        if (sidebarTabs == null) {
            return null;
        }
        final int idx = sidebarTabs.getSelectedIndex();
        if (idx < 0 || idx >= sidebarTabs.getTabCount()) {
            return null;
        }
        return sidebarTabs.getTitleAt(idx);
    }

    private void toggleSidebarVisibility() {
        if (sidebarCard.isVisible()) {
            captureSidebarWidth();
            sidebarCard.setVisible(false);
            sidebarVisible = false;
            if (btnChat != null) {
                btnChat.setActive(false);
            }
            if (btnPeople != null) {
                btnPeople.setActive(false);
            }
            collapseSidebarSpace();
        } else {
            final int widthHint;
            if (lastSidebarWidth <= 0) {
                widthHint = SIDEBAR_DEFAULT_WIDTH;
            } else {
                widthHint = lastSidebarWidth;
            }
            ensureSidebarShowing(widthHint);
        }
        refreshLayoutContainers();
    }

    private void ensureSidebarShowing(final int desiredWidth) {
        if (sidebarCard == null) {
            return;
        }
        sidebarCard.setVisible(true);
        sidebarVisible = true;
        setSidebarWidth(desiredWidth);
    }

    private void setSidebarWidth(final int desiredWidth) {
        if (centerSplit == null) {
            return;
        }
        final int widthHint;
        if (desiredWidth > 0) {
            widthHint = desiredWidth;
        } else {
            widthHint = SIDEBAR_DEFAULT_WIDTH;
        }
        if (centerSplit.getWidth() <= 0) {
            final int targetWidth = widthHint;
            SwingUtilities.invokeLater(() -> setSidebarWidth(targetWidth));
            return;
        }

        final int totalWidth = centerSplit.getWidth();
        final int clamped = clampSidebarWidth(widthHint, totalWidth);
        setSidebarWidthInternal(clamped, totalWidth);
    }

    private void setSidebarWidthInternal(final int sidebarWidth, final int totalWidth) {
        if (centerSplit == null) {
            return;
        }
        final int dividerSize = centerSplit.getDividerSize();
        int dividerLocation = Math.max(0, totalWidth - sidebarWidth - dividerSize);
        dividerLocation = Math.min(dividerLocation, centerSplit.getMaximumDividerLocation());
        centerSplit.setDividerLocation(dividerLocation);
        lastSidebarWidth = sidebarWidth;
    }

    private int clampSidebarWidth(final int desiredWidth, final int totalWidth) {
        final int dividerSize;
        if (centerSplit != null) {
            dividerSize = centerSplit.getDividerSize();
        } else {
            dividerSize = 0;
        }
        final int availableForSidebar = Math.max(0, totalWidth - dividerSize);
        int stageMin = 0;
        if (stageCard != null && stageCard.getMinimumSize() != null) {
            stageMin = stageCard.getMinimumSize().width;
        }
        final int maxAllowable = Math.max(0, availableForSidebar - stageMin);
        final int minWidth = Math.min(SIDEBAR_MIN_WIDTH, maxAllowable);
        int maxWidth = Math.min(SIDEBAR_MAX_WIDTH, maxAllowable);
        if (maxWidth < minWidth) {
            maxWidth = minWidth;
        }
        int width = desiredWidth;
        if (width < minWidth) {
            width = minWidth;
        }
        if (width > maxWidth) {
            width = maxWidth;
        }
        return width;
    }

    private void captureSidebarWidth() {
        if (sidebarCard == null) {
            return;
        }
        final int width = getCurrentSidebarWidth();
        if (width > 0) {
            lastSidebarWidth = width;
        }
    }

    private int getCurrentSidebarWidth() {
        if (centerSplit == null) {
            return lastSidebarWidth;
        }
        final int totalWidth = centerSplit.getWidth();
        if (totalWidth <= 0) {
            return lastSidebarWidth;
        }
        final int dividerLocation = centerSplit.getDividerLocation();
        final int dividerSize = centerSplit.getDividerSize();
        return Math.max(0, totalWidth - dividerLocation - dividerSize);
    }

    private void collapseSidebarSpace() {
        if (centerSplit != null) {
            centerSplit.setDividerLocation(1.0);
        }
    }

    private void refreshLayoutContainers() {
        if (sidebarCard == null) {
            return;
        }
        final Container centerPanel = sidebarCard.getParent();
        if (centerPanel != null) {
            centerPanel.revalidate();
            centerPanel.repaint();
        }
        if (stageCard != null) {
            stageCard.revalidate();
            stageCard.repaint();
        }
        SwingUtilities.invokeLater(() -> {
            revalidate();
            repaint();
        });
    }

    private void attachSplitListeners(final JSplitPane split) {
        split.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, evt -> enforceSidebarBounds());
        split.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(final ComponentEvent e) {
                enforceSidebarBounds();
            }
        });
    }

    private void enforceSidebarBounds() {
        if (centerSplit == null || !sidebarCard.isVisible()) {
            return;
        }
        final int totalWidth = centerSplit.getWidth();
        if (totalWidth <= 0) {
            return;
        }
        final int dividerLocation = centerSplit.getDividerLocation();
        if (dividerLocation < 0) {
            return;
        }
        final int currentWidth = totalWidth - dividerLocation - centerSplit.getDividerSize();
        final int clamped = clampSidebarWidth(currentWidth, totalWidth);
        if (clamped != currentWidth) {
            setSidebarWidthInternal(clamped, totalWidth);
        } else {
            lastSidebarWidth = clamped;
        }
    }

    // ---------------- Controls Bar ----------------
    private SoftCardPanel buildControlsBar() {
        final SoftCardPanel bar = new SoftCardPanel(CONTROLS_BLUR_RADIUS);
        bar.setCornerRadius(CONTROLS_CORNER_RADIUS);
        bar.setLayout(new FlowLayout(FlowLayout.CENTER, CONTROLS_GAP_H, CONTROLS_GAP_V));

        btnMute = new MeetingControlButton("Mute", MeetingControlButton.ControlIcon.MIC);
        btnMute.setAccentColor(ACCENT_BLUE);
        btnMute.addActionListener(evt -> meetingViewModel.toggleAudio());

        btnCamera = new MeetingControlButton("Video", MeetingControlButton.ControlIcon.VIDEO);
        btnCamera.setAccentColor(ACCENT_BLUE);
        btnCamera.addActionListener(evt -> meetingViewModel.toggleVideo());

        btnShare = new MeetingControlButton("Share", MeetingControlButton.ControlIcon.SHARE);
        btnShare.setAccentColor(ACCENT_BLUE);
        btnShare.addActionListener(evt -> meetingViewModel.toggleScreenSharing());

        btnRaiseHand = new MeetingControlButton("Raise", MeetingControlButton.ControlIcon.HAND);
        btnRaiseHand.setAccentColor(ACCENT_BLUE);
        btnRaiseHand.addActionListener(evt -> toggleQuickDoubt());

        btnLeave = new MeetingControlButton("Leave", MeetingControlButton.ControlIcon.LEAVE);
        final Color leaveAccent = new Color(LEAVE_RED, LEAVE_GREEN, LEAVE_BLUE);
        btnLeave.setActiveColorOverride(new Color(LEAVE_RED, LEAVE_GREEN, LEAVE_BLUE, LEAVE_ALPHA));
        btnLeave.setAccentColor(leaveAccent);
        btnLeave.addActionListener(e -> {
            meetingViewModel.endMeeting();
            App.getInstance(null).showView(App.MAIN_VIEW);
        });

        btnChat = new MeetingControlButton("Chat", MeetingControlButton.ControlIcon.CHAT);
        btnChat.setAccentColor(ACCENT_BLUE);
        btnChat.addActionListener(evt -> handleControlTabButton("Chat"));

        btnPeople = new MeetingControlButton("People", MeetingControlButton.ControlIcon.PEOPLE);
        btnPeople.setAccentColor(ACCENT_BLUE);
        btnPeople.addActionListener(evt -> handleControlTabButton("Participants"));

        bar.add(btnMute);
        bar.add(btnCamera);
        bar.add(btnShare);
        bar.add(btnRaiseHand);
        bar.add(btnLeave);
        bar.add(btnChat);
        bar.add(btnPeople);

        return bar;
    }

    // ---------------- Bindings & Theme ----------------
    private void setupBindings() {
        // update camera/share active states from viewmodel
        meetingViewModel.getIsVideoEnabled().addListener(PropertyListeners.onBooleanChanged(v ->
                SwingUtilities.invokeLater(() -> btnCamera.setActive(Boolean.TRUE.equals(v)))));

        meetingViewModel.getIsScreenShareEnabled().addListener(PropertyListeners.onBooleanChanged(v ->
                SwingUtilities.invokeLater(() -> btnShare.setActive(Boolean.TRUE.equals(v)))));

        meetingViewModel.getIsAudioEnabled().addListener(PropertyListeners.onBooleanChanged(v ->
                SwingUtilities.invokeLater(() -> btnMute.setActive(Boolean.TRUE.equals(v)))));

        meetingViewModel.getMeetingId().addListener(evt -> SwingUtilities.invokeLater(() -> {
            final String meetingId = meetingViewModel.getMeetingId().get();
            final String badgeText;
            if (meetingId == null || meetingId.isEmpty()) {
                badgeText = "Meeting: --";
            } else {
                badgeText = "Meeting: " + meetingId;
            }
            meetingIdBadge.setText(badgeText);
        }));

        meetingViewModel.getRole().addListener(evt -> SwingUtilities.invokeLater(() -> {
            roleLabel.setText(buildRoleLabelText());
            updateMeetingControlAvailability();
        }));
    }

    private void registerThemeListener() {
        try {
            final ThemeManager tm = ThemeManager.getInstance();
            if (tm != null) {
                tm.addThemeChangeListener(() -> SwingUtilities.invokeLater(() -> {
                    try {
                        // reapply custom tabbed UI to sidebar tabs
                        if (sidebarTabs != null) {
                            sidebarTabs.setUI(new ModernTabbedPaneUI());
                            sidebarTabs.revalidate();
                            sidebarTabs.repaint();
                        }
                        // also reapply for the sidebar card (to update colors etc.)
                        applyTheme();
                    } catch (Throwable ignored) {
                    }
                }));
            }
        } catch (Throwable ignored) {
            // no-op if ThemeManager lacks listener API
        }
    }

    private void startLiveClock() {
        if (liveTimer != null) {
            liveTimer.stop();
        }
        liveTimer = new Timer(TIMER_DELAY_MS,
                e -> liveClockLabel.setText("Live: " + new SimpleDateFormat("hh:mm:ss a").format(new Date())));
        liveTimer.setInitialDelay(0);
        liveTimer.start();
    }

    private boolean isCurrentUserInstructor() {
        return meetingViewModel != null
                && meetingViewModel.getCurrentUser() != null
                && meetingViewModel.getCurrentUser().getRole() == ParticipantRole.INSTRUCTOR;
    }

    private void updateMeetingControlAvailability() {
        if (meetingControlsButton == null) {
            return;
        }
        final boolean instructor = isCurrentUserInstructor();
        meetingControlsButton.setEnabled(instructor);
        meetingControlsButton.setToolTipText(instructor
                ? "Open advanced meeting controls"
                : "Available only for instructors");
    }

    private void openMeetingControlsDialog() {
        if (!isCurrentUserInstructor()) {
            JOptionPane.showMessageDialog(this,
                    "Only instructors can access meeting controls.",
                    "Access Restricted",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        JOptionPane.showMessageDialog(this,
                "Meeting controls coming soon.",
                "Meeting Controls",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void copyMeetingId() {
        final String id = meetingViewModel.getMeetingId().get();
        if (id != null && !id.isEmpty()) {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(id), null);

            // Show "Copied!" feedback
            btnCopyLink.setText("Copied!");
            btnCopyLink.setEnabled(false);

            // Reset after 3 seconds
            final Timer resetTimer = new Timer(COPY_FEEDBACK_DELAY_MS, e -> {
                btnCopyLink.setText("Copy Link");
                btnCopyLink.setEnabled(true);
            });
            resetTimer.setRepeats(false);
            resetTimer.start();
        }
    }

    private void toggleQuickDoubt() {
        isHandRaised = !isHandRaised;
        btnRaiseHand.setActive(isHandRaised);
        if (isHandRaised) {
            quickDoubtPopup.showAbove(btnRaiseHand);
        } else {
            quickDoubtPopup.setVisible(false);
            quickDoubtPopup.reset();
        }
    }

    private void handleQuickDoubtClosed() {
        if (isHandRaised) {
            isHandRaised = false;
            if (btnRaiseHand != null) {
                btnRaiseHand.setActive(false);
            }
        }
    }

    private void applyTheme() {
        try {
            if (ThemeManager.getInstance() != null) {
                final com.swe.ux.theme.Theme theme = ThemeManager.getInstance().getCurrentTheme();
                if (theme != null) {
                    setBackground(theme.getBackgroundColor());
                    ThemeManager.getInstance().applyThemeRecursively(headerCard);
                    ThemeManager.getInstance().applyThemeRecursively(stageCard);
                    ThemeManager.getInstance().applyThemeRecursively(sidebarCard);
                    ThemeManager.getInstance().applyThemeRecursively(controlsBar);
                    if (centerSplit != null) {
                        centerSplit.setBackground(theme.getBackgroundColor());
                    }
                }
            }
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void updateUI() {
        super.updateUI();
        // When Swing updates UI, also reapply our custom tab UI (extra safety)
        SwingUtilities.invokeLater(() -> {
            try {
                if (sidebarTabs != null) {
                    sidebarTabs.setUI(new ModernTabbedPaneUI());
                    sidebarTabs.revalidate();
                    sidebarTabs.repaint();
                }
                applyTheme();
            } catch (Throwable ignored) {
            }
        });
    }

    /**
     * Sidebar toggle icon implementation.
     */
    private static class SidebarToggleIcon implements Icon {
        /** Icon size. */
        private static final int SIZE = ICON_SIZE;

        @Override
        public void paintIcon(final Component c, final Graphics g, final int x, final int y) {
            final Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            final Color color = ThemeManager.getInstance().getCurrentTheme().getTextColor();
            g2.setColor(color);
            final int barWidth = ICON_BAR_WIDTH;
            final int spacing = ICON_SPACING;
            final int numBars = 3;
            for (int i = 0; i < numBars; i++) {
                g2.fillRoundRect(x + i * (barWidth + spacing), y + ICON_VERTICAL_OFFSET, barWidth,
                        SIZE - ICON_HEIGHT_ADJUST, ICON_ROUND_CORNER, ICON_ROUND_CORNER);
            }
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return SIZE;
        }

        @Override
        public int getIconHeight() {
            return SIZE;
        }
    }

    private String buildRoleLabelText() {
        String role = meetingViewModel.getRole().get();
        if (role == null || role.isEmpty()) {
            if (meetingViewModel.getCurrentUser() != null
                    && meetingViewModel.getCurrentUser().getRole() != null) {
                role = meetingViewModel.getCurrentUser().getRole().name();
            }
        }
        if (role == null || role.isEmpty()) {
            role = "Guest";
        }
        return "Role: " + role;
    }
}
