package com.swe.ux.views;

import com.swe.canvas.datamodel.canvas.CanvasState;
import com.swe.controller.Meeting.UserProfile;
import com.swe.canvas.datamodel.collaboration.CanvasNetworkService;
import com.swe.canvas.datamodel.manager.ActionManager;
import com.swe.canvas.datamodel.manager.ClientActionManager;
import com.swe.canvas.datamodel.manager.HostActionManager;
import com.swe.controller.Meeting.ParticipantRole;
import com.swe.screenNVideo.Utils;
import com.swe.ux.App;
import com.swe.ux.binding.PropertyListeners;
import com.swe.ux.theme.ThemeManager;
import com.swe.ux.ui.*;
import com.swe.ux.viewmodels.*;
import com.swe.ux.viewmodels.ChatViewModel;
import com.swe.ux.viewmodels.MeetingViewModel;
import com.swe.ux.viewmodels.ParticipantsViewModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.CardLayout;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * MeetingPage
 *
 * Stage on the left (tabs: Screen + Video, Canvas, AI Insights)
 * Right sidebar with internal tabs: Chat | Participants
 *
 * - Participants button in header
 * - Chat button in bottom bar
 * - Hide Panels in header (toggles sidebar visibility)
 * - Theme-consistent and re-applies custom tab UI on theme changes
 */
public class MeetingPage extends FrostedBackgroundPanel {
    private static final int SIDEBAR_MIN_WIDTH = 280;
    private static final int SIDEBAR_MAX_WIDTH = 520;
    private static final int SIDEBAR_DEFAULT_WIDTH = 360;
    private static final Color ACCENT_BLUE = new Color(82, 140, 255);

    private final MeetingViewModel meetingViewModel;

    // header / controls
    private SoftCardPanel headerCard;
    private SoftCardPanel controlsBar;
    private JButton sidebarToggleBtn;
    private FrostedToolbarButton btnCopyLink;

    // stage (left)
    private SoftCardPanel stageCard;
    private MeetingStageTabs stageTabs;
    private CardLayout stageContentLayout;
    private JPanel stageContentPanel;

    // sidebar (right)
    private JSplitPane centerSplit;
    private SoftCardPanel sidebarCard;
    private JTabbedPane sidebarTabs;
    private JLabel sidebarHeaderLabel;
    private JPanel chatPanel;
    private JPanel participantsPanel;
    private boolean sidebarVisible = false;
    private int lastSidebarWidth = SIDEBAR_DEFAULT_WIDTH;

    // other controls
    private MeetingControlButton btnCamera;
    private MeetingControlButton btnShare;
    private MeetingControlButton btnLeave;
    private MeetingControlButton btnMute;
    private MeetingControlButton btnRaiseHand;
    private MeetingControlButton btnChat;
    private MeetingControlButton btnPeople;

    // badges / labels
    private FrostedBadgeLabel meetingIdBadge;
    private JLabel liveClockLabel;
    private JLabel roleLabel;
    private Timer liveTimer;
    private QuickDoubtPopup quickDoubtPopup;
    private boolean isHandRaised = false;

    public MeetingPage(MeetingViewModel meetingViewModel) {
        this.meetingViewModel = meetingViewModel;
        initializeUI();
        quickDoubtPopup = new QuickDoubtPopup();
        quickDoubtPopup.addPopupMenuListener(new PopupMenuListener() {
            @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) { }
            @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) { handleQuickDoubtClosed(); }
            @Override public void popupMenuCanceled(PopupMenuEvent e) { handleQuickDoubtClosed(); }
        });
        registerThemeListener();
        setupBindings();
        startLiveClock();
        applyTheme();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(24, 24, 24, 24));

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
        SoftCardPanel card = new SoftCardPanel(10);
        card.setCornerRadius(18);
        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setBorder(new EmptyBorder(0, 6, 0, 6));

        JPanel leftCluster = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftCluster.setOpaque(false);
        JLabel title = new JLabel("Live Meeting");
        title.setFont(FontUtil.getJetBrainsMono(20f, Font.BOLD));
        leftCluster.add(title);

        ThemeToggleButton toggle = new ThemeToggleButton();
        leftCluster.add(toggle);

        meetingIdBadge = new FrostedBadgeLabel("Meeting: --");
        leftCluster.add(meetingIdBadge);

        FrostedBadgeLabel ipBadge = new FrostedBadgeLabel("IP: " + Utils.getSelfIP());
        leftCluster.add(ipBadge);

        roleLabel = new JLabel("Role: Guest");
        roleLabel.setFont(FontUtil.getJetBrainsMono(12f, Font.PLAIN));
        leftCluster.add(roleLabel);

        row.add(leftCluster);
        row.add(Box.createHorizontalGlue());

        JPanel rightCluster = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightCluster.setOpaque(false);
        liveClockLabel = new JLabel("Live: --:--");
        liveClockLabel.setFont(FontUtil.getJetBrainsMono(12f, Font.PLAIN));
        rightCluster.add(liveClockLabel);

        btnCopyLink = new FrostedToolbarButton("Copy Link");
        btnCopyLink.addActionListener(e -> copyMeetingId());
        rightCluster.add(btnCopyLink);

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

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, stageCard, sidebarCard);
        split.setOpaque(false);
        split.setBorder(null);
        split.setDividerSize(10);
        split.setResizeWeight(0.8); // favor stage area
        split.setContinuousLayout(true);
        split.setOneTouchExpandable(false);
        attachSplitListeners(split);

        stageCard.setMinimumSize(new Dimension(400, 0));
        sidebarCard.setMinimumSize(new Dimension(280, 0));

        // start collapsed
        sidebarCard.setVisible(false);
        split.setDividerLocation(1.0);
        return split;
    }

    private SoftCardPanel buildStageCard() {
        SoftCardPanel card = new SoftCardPanel(18);
        card.setLayout(new BorderLayout(12, 12));
        card.setCornerRadius(26);
        card.setPreferredSize(new Dimension(0, 680));

        Map<String, String> tabs = new LinkedHashMap<>();
        tabs.put("MEETING", "Meeting");
        tabs.put("CANVAS", "Canvas");
        tabs.put("INSIGHTS", "AI Insights");

        stageTabs = new MeetingStageTabs(tabs, this::switchStageView);
        stageTabs.setAccentColor(ACCENT_BLUE);
        stageContentLayout = new CardLayout();
        stageContentPanel = new JPanel(stageContentLayout);
        stageContentPanel.setOpaque(false);
        stageContentPanel.setBorder(new EmptyBorder(2, 2, 2, 6));

        ScreenNVideo screenNVideo = new ScreenNVideo(meetingViewModel);
        CanvasPage canvasPage;
        String userId = meetingViewModel.currentUser != null
                ? meetingViewModel.currentUser.getEmail()
                : "user-" + System.nanoTime();

        if (meetingViewModel.currentUser.getRole() == ParticipantRole.INSTRUCTOR) {
            CanvasState hostCanvasState = new CanvasState();
            HostActionManager hostManager = new HostActionManager(userId, hostCanvasState,
                    new CanvasNetworkService(meetingViewModel.rpc));
            canvasPage = new CanvasPage(hostManager, userId);
        } else {
            CanvasState clientCanvasState = new CanvasState();
            ClientActionManager clientManager = new ClientActionManager(userId, clientCanvasState,
                    new CanvasNetworkService(meetingViewModel.rpc));
            canvasPage = new CanvasPage(clientManager, userId);
        }
        SentimentInsightsPanel sentimentInsightsPanel = new SentimentInsightsPanel(meetingViewModel);

        stageContentPanel.add(wrap(screenNVideo), "MEETING");
        stageContentPanel.add(wrap(canvasPage), "CANVAS");
        stageContentPanel.add(wrap(sentimentInsightsPanel), "INSIGHTS");

        card.add(stageTabs, BorderLayout.NORTH);
        card.add(stageContentPanel, BorderLayout.CENTER);
        stageTabs.setSelectedTab("MEETING");
        stageContentLayout.show(stageContentPanel, "MEETING");
        return card;
    }

    private JPanel wrap(JPanel p) {
        JPanel w = new JPanel(new BorderLayout());
        w.setOpaque(false);
        w.add(p, BorderLayout.CENTER);
        w.setMinimumSize(new Dimension(0, 0));
        w.setBorder(new EmptyBorder(4, 4, 4, 4));
        return w;
    }

    private void switchStageView(String tabKey) {
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
        SoftCardPanel sb = new SoftCardPanel(10);
        sb.setLayout(new BorderLayout(8, 8));
        sb.setPreferredSize(new Dimension(360, 0));
        sb.setVisible(false); // start hidden

        JPanel sidebarHeader = new JPanel(new BorderLayout());
        sidebarHeader.setOpaque(false);
        sidebarHeaderLabel = new JLabel("Panels");
        sidebarHeaderLabel.setFont(FontUtil.getJetBrainsMono(20f, Font.BOLD));
        JButton closeSidebarBtn = new JButton("x");
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
        SoftCardPanel panel = new SoftCardPanel(12);
        panel.setLayout(new BorderLayout());
        ParticipantsViewModel pvm = new ParticipantsViewModel(meetingViewModel);
        panel.add(new ParticipantsView(pvm), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createChatPanel() {
        SoftCardPanel panel = new SoftCardPanel(12);
        panel.setLayout(new BorderLayout());

        if (meetingViewModel == null || meetingViewModel.rpc == null) {
            JLabel fallback = new JLabel("<html><center>Chat unavailable<br>(no RPC connection)</center></html>",
                    SwingConstants.CENTER);
            fallback.setFont(FontUtil.getJetBrainsMono(14f, Font.PLAIN));
            panel.add(fallback, BorderLayout.CENTER);
            return panel;
        }

        ChatViewModel chatViewModel = new ChatViewModel(meetingViewModel.rpc, meetingViewModel.currentUser);
        ChatView chatView = new ChatView(chatViewModel);
        panel.add(chatView, BorderLayout.CENTER);
        return panel;
    }

    private void openSidebarToTab(String tabName) {
        int widthHint = sidebarCard.isVisible() ? getCurrentSidebarWidth() : lastSidebarWidth;
        ensureSidebarShowing(widthHint <= 0 ? SIDEBAR_DEFAULT_WIDTH : widthHint);

        // select tab if exists
        for (int i = 0; i < sidebarTabs.getTabCount(); i++) {
            String title = sidebarTabs.getTitleAt(i);
            if (title != null && title.equalsIgnoreCase(tabName)) {
                sidebarTabs.setSelectedIndex(i);
                break;
            }
        }

        if (sidebarHeaderLabel != null) {
            sidebarHeaderLabel.setText(tabName);
        }
        if (btnChat != null) {
            btnChat.setActive(tabName.equalsIgnoreCase("Chat"));
        }
        if (btnPeople != null) {
            btnPeople.setActive(tabName.equalsIgnoreCase("Participants"));
        }

        refreshLayoutContainers();
    }

    private void handleControlTabButton(String tabName) {
        String current = getCurrentSidebarTab();
        if (sidebarCard.isVisible() && current != null && current.equalsIgnoreCase(tabName)) {
            toggleSidebarVisibility();
        } else {
            openSidebarToTab(tabName);
        }
    }

    private String getCurrentSidebarTab() {
        if (sidebarTabs == null) {
            return null;
        }
        int idx = sidebarTabs.getSelectedIndex();
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
            int widthHint = lastSidebarWidth <= 0 ? SIDEBAR_DEFAULT_WIDTH : lastSidebarWidth;
            ensureSidebarShowing(widthHint);
        }
        refreshLayoutContainers();
    }

    private void ensureSidebarShowing(int desiredWidth) {
        if (sidebarCard == null) {
            return;
        }
        sidebarCard.setVisible(true);
        sidebarVisible = true;
        setSidebarWidth(desiredWidth);
    }

    private void setSidebarWidth(int desiredWidth) {
        if (centerSplit == null) {
            return;
        }
        int widthHint = desiredWidth > 0 ? desiredWidth : SIDEBAR_DEFAULT_WIDTH;
        if (centerSplit.getWidth() <= 0) {
            final int targetWidth = widthHint;
            SwingUtilities.invokeLater(() -> setSidebarWidth(targetWidth));
            return;
        }

        int totalWidth = centerSplit.getWidth();
        int clamped = clampSidebarWidth(widthHint, totalWidth);
        setSidebarWidthInternal(clamped, totalWidth);
    }

    private void setSidebarWidthInternal(int sidebarWidth, int totalWidth) {
        if (centerSplit == null) {
            return;
        }
        int dividerSize = centerSplit.getDividerSize();
        int dividerLocation = Math.max(0, totalWidth - sidebarWidth - dividerSize);
        dividerLocation = Math.min(dividerLocation, centerSplit.getMaximumDividerLocation());
        centerSplit.setDividerLocation(dividerLocation);
        lastSidebarWidth = sidebarWidth;
    }

    private int clampSidebarWidth(int desiredWidth, int totalWidth) {
        int dividerSize = centerSplit != null ? centerSplit.getDividerSize() : 0;
        int availableForSidebar = Math.max(0, totalWidth - dividerSize);
        int stageMin = 0;
        if (stageCard != null && stageCard.getMinimumSize() != null) {
            stageMin = stageCard.getMinimumSize().width;
        }
        int maxAllowable = Math.max(0, availableForSidebar - stageMin);
        int minWidth = Math.min(SIDEBAR_MIN_WIDTH, maxAllowable);
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
        int width = getCurrentSidebarWidth();
        if (width > 0) {
            lastSidebarWidth = width;
        }
    }

    private int getCurrentSidebarWidth() {
        if (centerSplit == null) {
            return lastSidebarWidth;
        }
        int totalWidth = centerSplit.getWidth();
        if (totalWidth <= 0) {
            return lastSidebarWidth;
        }
        int dividerLocation = centerSplit.getDividerLocation();
        int dividerSize = centerSplit.getDividerSize();
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
        Container centerPanel = sidebarCard.getParent();
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

    private void attachSplitListeners(JSplitPane split) {
        split.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, evt -> enforceSidebarBounds());
        split.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                enforceSidebarBounds();
            }
        });
    }

    private void enforceSidebarBounds() {
        if (centerSplit == null || !sidebarCard.isVisible()) {
            return;
        }
        int totalWidth = centerSplit.getWidth();
        if (totalWidth <= 0) {
            return;
        }
        int dividerLocation = centerSplit.getDividerLocation();
        if (dividerLocation < 0) {
            return;
        }
        int currentWidth = totalWidth - dividerLocation - centerSplit.getDividerSize();
        int clamped = clampSidebarWidth(currentWidth, totalWidth);
        if (clamped != currentWidth) {
            setSidebarWidthInternal(clamped, totalWidth);
        } else {
            lastSidebarWidth = clamped;
        }
    }

    // ---------------- Controls Bar ----------------
    private SoftCardPanel buildControlsBar() {
        SoftCardPanel bar = new SoftCardPanel(14);
        bar.setCornerRadius(36);
        bar.setLayout(new FlowLayout(FlowLayout.CENTER, 12, 10));

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
        Color leaveAccent = new Color(229, 57, 53);
        btnLeave.setActiveColorOverride(new Color(229, 57, 53, 200));
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
        meetingViewModel.isVideoEnabled.addListener(PropertyListeners.onBooleanChanged(v ->
                SwingUtilities.invokeLater(() -> btnCamera.setActive(Boolean.TRUE.equals(v)))));

        meetingViewModel.isScreenShareEnabled.addListener(PropertyListeners.onBooleanChanged(v ->
                SwingUtilities.invokeLater(() -> btnShare.setActive(Boolean.TRUE.equals(v)))));

        meetingViewModel.isAudioEnabled.addListener(PropertyListeners.onBooleanChanged(v ->
                SwingUtilities.invokeLater(() -> btnMute.setActive(Boolean.TRUE.equals(v)))));

        meetingViewModel.meetingId.addListener(evt -> SwingUtilities.invokeLater(() -> meetingIdBadge.setText(
                meetingViewModel.meetingId.get() == null || meetingViewModel.meetingId.get().isEmpty()
                        ? "Meeting: --"
                        : "Meeting: " + meetingViewModel.meetingId.get())));

        meetingViewModel.role.addListener(evt -> SwingUtilities.invokeLater(() -> roleLabel.setText(
                "Role: " + (meetingViewModel.role.get() == null || meetingViewModel.role.get().isEmpty() ? "Guest"
                        : meetingViewModel.role.get()))));
    }

    private void registerThemeListener() {
        try {
            ThemeManager tm = ThemeManager.getInstance();
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
        if (liveTimer != null)
            liveTimer.stop();
        liveTimer = new Timer(1000,
                e -> liveClockLabel.setText("Live: " + new SimpleDateFormat("hh:mm:ss a").format(new Date())));
        liveTimer.setInitialDelay(0);
        liveTimer.start();
    }

    private void copyMeetingId() {
        String id = meetingViewModel.meetingId.get();
        if (id != null && !id.isEmpty()) {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(id), null);

            // Show "Copied!" feedback
            btnCopyLink.setText("Copied!");
            btnCopyLink.setEnabled(false);

            // Reset after 3 seconds
            Timer resetTimer = new Timer(3000, e -> {
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
                var theme = ThemeManager.getInstance().getCurrentTheme();
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

    private static class SidebarToggleIcon implements Icon {
        private final int size = 18;

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color color = ThemeManager.getInstance().getCurrentTheme().getTextColor();
            int barWidth = 3;
            int spacing = 4;
            for (int i = 0; i < 3; i++) {
                g2.fillRoundRect(x + i * (barWidth + spacing), y + 3, barWidth, size - 6, 4, 4);
            }
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }
}
