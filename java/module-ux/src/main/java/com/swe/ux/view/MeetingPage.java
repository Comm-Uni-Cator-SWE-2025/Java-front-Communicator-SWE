package com.swe.ux.view;

import com.swe.canvas.datamodel.canvas.CanvasState;
import com.swe.screenNVideo.Utils;
import com.swe.ux.App;
import com.swe.ux.binding.PropertyListeners;
import com.swe.ux.theme.ThemeManager;
import com.swe.ux.ui.*;
import com.swe.ux.viewmodel.CanvasViewModel;
import com.swe.ux.viewmodel.MeetingViewModel;
import com.swe.ux.viewmodel.ParticipantsViewModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    private final MeetingViewModel meetingViewModel;

    // header / controls
    private SoftCardPanel headerCard;
    private SoftCardPanel controlsBar;
    private FrostedToolbarButton btnParticipants;   // header button (under Copy Link)
    private FrostedToolbarButton btnHidePanels;
    private FrostedToolbarButton btnCopyLink;

    // stage (left)
    private SoftCardPanel stageCard;
    private JTabbedPane stageTabs;

    // sidebar (right)
    private SoftCardPanel sidebarCard;
    private JTabbedPane sidebarTabs;
    private JPanel chatPanel;
    private JPanel participantsPanel;
    private boolean sidebarVisible = false;

    // bottom chat toggler
    private FrostedToolbarButton btnChat;

    // other controls
    private FrostedToolbarButton btnCamera;
    private FrostedToolbarButton btnShare;
    private FrostedToolbarButton btnLeave;
    private FrostedToolbarButton btnMute;
    private FrostedToolbarButton btnRaiseHand;

    // badges / labels
    private FrostedBadgeLabel meetingIdBadge;
    private JLabel liveClockLabel;
    private JLabel roleLabel;
    private Timer liveTimer;

    public MeetingPage(MeetingViewModel meetingViewModel) {
        this.meetingViewModel = meetingViewModel;
        initializeUI();
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

        // Center area: stage (left) + sidebar (right)
        JPanel center = new JPanel(new BorderLayout(16, 0));
        center.setOpaque(false);

        stageCard = buildStageCard();
        center.add(stageCard, BorderLayout.CENTER);

        sidebarCard = buildSidebarCard();
        center.add(sidebarCard, BorderLayout.EAST);

        add(center, BorderLayout.CENTER);

        // Bottom controls
        controlsBar = buildControlsBar();
        add(controlsBar, BorderLayout.SOUTH);
    }

    // ---------------- Header ----------------
    private SoftCardPanel buildHeader() {
        SoftCardPanel p = new SoftCardPanel(14);
        p.setLayout(new BorderLayout(10, 0));

        // Left: title + role
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Live Meeting");
        title.setFont(FontUtil.getJetBrainsMono(22f, Font.BOLD));
        roleLabel = new JLabel("Role: Guest");
        roleLabel.setFont(FontUtil.getJetBrainsMono(12f, Font.PLAIN));
        left.add(title);
        left.add(Box.createVerticalStrut(4));
        left.add(roleLabel);

        // Middle: badges
        JPanel mid = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        mid.setOpaque(false);
        meetingIdBadge = new FrostedBadgeLabel("Meeting: --");
        FrostedBadgeLabel ipBadge = new FrostedBadgeLabel("IP: " + Utils.getSelfIP());
        liveClockLabel = new JLabel("Live: --:--");
        liveClockLabel.setFont(FontUtil.getJetBrainsMono(12f, Font.PLAIN));
        mid.add(meetingIdBadge);
        mid.add(ipBadge);
        mid.add(liveClockLabel);

        // Right: actions (ThemeToggle, Copy Link, Participants, Hide Panels)
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        right.add(new ThemeToggleButton());

        btnCopyLink = new FrostedToolbarButton("Copy Link");
        btnCopyLink.addActionListener(e -> copyMeetingId());
        right.add(btnCopyLink);

        // Participants button (selects and opens sidebar on Participants tab)
        btnParticipants = new FrostedToolbarButton("Participants");
        btnParticipants.addActionListener(e -> openSidebarToTab("Participants"));
        right.add(btnParticipants);

        // Hide panels
        btnHidePanels = new FrostedToolbarButton("Hide Panels");
        btnHidePanels.addActionListener(e -> toggleSidebarVisibility());
        right.add(btnHidePanels);

        p.add(left, BorderLayout.WEST);
        p.add(mid, BorderLayout.CENTER);
        p.add(right, BorderLayout.EAST);

        return p;
    }

    // ---------------- Stage ----------------
    private SoftCardPanel buildStageCard() {
        SoftCardPanel card = new SoftCardPanel(20);
        card.setLayout(new BorderLayout(12, 12));
        card.setCornerRadius(20);

        // Stage tabs
        stageTabs = new JTabbedPane();
        stageTabs.setOpaque(false);
        stageTabs.setUI(new ModernTabbedPaneUI());

        // content components (assume these exist in your project)
        ScreenNVideo screenNVideo = new ScreenNVideo(meetingViewModel);
        CanvasViewModel canvasVM = new CanvasViewModel(new CanvasState());
        CanvasPage canvasPage = new CanvasPage(canvasVM);

        stageTabs.addTab("  Screen + Video  ", wrap(screenNVideo));
        stageTabs.addTab("  Canvas  ", wrap(canvasPage));
        stageTabs.addTab("  AI Insights  ", wrap(createPlaceholderPanel("AI Insights coming soon")));

        card.add(stageTabs, BorderLayout.CENTER);
        return card;
    }

    private JPanel wrap(JPanel p) {
        JPanel w = new JPanel(new BorderLayout());
        w.setOpaque(false);
        w.add(p, BorderLayout.CENTER);
        return w;
    }

    private JPanel createPlaceholderPanel(String text) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(FontUtil.getJetBrainsMono(14f, Font.ITALIC));
        p.add(l, BorderLayout.CENTER);
        return p;
    }

    // ---------------- Sidebar ----------------
    private SoftCardPanel buildSidebarCard() {
        SoftCardPanel sb = new SoftCardPanel(12);
        sb.setLayout(new BorderLayout(8, 8));
        sb.setPreferredSize(new Dimension(360, 0));
        sb.setVisible(false); // start hidden

        // Internal tabs: Chat | Participants
        sidebarTabs = new JTabbedPane(SwingConstants.TOP);
        sidebarTabs.setOpaque(false);
        sidebarTabs.setUI(new ModernTabbedPaneUI());

        chatPanel = createChatPanel();
        participantsPanel = createParticipantsPanel();

        sidebarTabs.addTab("Chat", chatPanel);
        sidebarTabs.addTab("Participants", participantsPanel);

        sb.add(sidebarTabs, BorderLayout.CENTER);
        return sb;
    }

    private JPanel createChatPanel() {
        SoftCardPanel panel = new SoftCardPanel(12);
        panel.setLayout(new BorderLayout());
        JLabel l = new JLabel("<html><center>Chat module<br>Coming soon</center></html>", SwingConstants.CENTER);
        l.setFont(FontUtil.getJetBrainsMono(14f, Font.PLAIN));
        panel.add(l, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createParticipantsPanel() {
        SoftCardPanel panel = new SoftCardPanel(12);
        panel.setLayout(new BorderLayout());
        ParticipantsViewModel pvm = new ParticipantsViewModel(meetingViewModel);
        panel.add(new ParticipantsView(pvm), BorderLayout.CENTER);
        return panel;
    }

    private void openSidebarToTab(String tabName) {
        // Ensure sidebar visible then select the tab
        if (!sidebarCard.isVisible()) {
            sidebarCard.setVisible(true);
            sidebarVisible = true;
        }
        // select tab if exists
        for (int i = 0; i < sidebarTabs.getTabCount(); i++) {
            String title = sidebarTabs.getTitleAt(i);
            if (title != null && title.equalsIgnoreCase(tabName)) {
                sidebarTabs.setSelectedIndex(i);
                break;
            }
        }
        // visual state for header/bottom toggles
        btnParticipants.setCustomFill(tabName.equalsIgnoreCase("Participants") ? new Color(90, 160, 255, 160) : null);
        btnChat.setCustomFill(tabName.equalsIgnoreCase("Chat") ? new Color(90, 160, 255, 160) : null);
    }

    private void toggleSidebarVisibility() {
        if (sidebarCard.isVisible()) {
            sidebarCard.setVisible(false);
            sidebarVisible = false;
            btnParticipants.setCustomFill(null);
            btnChat.setCustomFill(null);
            btnHidePanels.setText("Show Panels");
        } else {
            sidebarCard.setVisible(true);
            sidebarVisible = true;
            btnHidePanels.setText("Hide Panels");
        }
        revalidate();
        repaint();
    }

    // ---------------- Controls Bar ----------------
    private SoftCardPanel buildControlsBar() {
        SoftCardPanel bar = new SoftCardPanel(14);
        bar.setLayout(new FlowLayout(FlowLayout.RIGHT, 12, 8)); // chat at bottom-right

        btnChat = new FrostedToolbarButton("Chat");
        btnChat.addActionListener(evt -> {
            openSidebarToTab("Chat");
            sidebarCard.setVisible(true);
            sidebarVisible = true;
            btnHidePanels.setText("Hide Panels");
        });

        btnMute = new FrostedToolbarButton("Mute");

        btnCamera = new FrostedToolbarButton("Camera");
        btnCamera.addActionListener(evt -> {
            meetingViewModel.toggleVideo();
            boolean v = Boolean.TRUE.equals(meetingViewModel.isVideoEnabled.get());
            btnCamera.setCustomFill(v ? new Color(90, 160, 255, 160) : null);
        });

        btnShare = new FrostedToolbarButton("Share");
        btnShare.addActionListener(evt -> {
            meetingViewModel.toggleScreenSharing();
            boolean v = Boolean.TRUE.equals(meetingViewModel.isScreenShareEnabled.get());
            btnShare.setCustomFill(v ? new Color(120, 200, 255, 160) : null);
        });

        btnRaiseHand = new FrostedToolbarButton("Raise Hand");

        btnLeave = new FrostedToolbarButton("Leave");
        btnLeave.setCustomFill(new Color(229, 57, 53, 180));
        btnLeave.addActionListener(e -> {
            meetingViewModel.endMeeting();
            com.swe.ux.App.getInstance(null).showView(App.MAIN_VIEW);
        });


        // order: (others) ... Chat (right-most)
        bar.add(btnMute);
        bar.add(btnCamera);
        bar.add(btnShare);
        bar.add(btnRaiseHand);
        bar.add(btnLeave);
        bar.add(btnChat);

        return bar;
    }

    // ---------------- Bindings & Theme ----------------
    private void setupBindings() {
        // update camera/share active states from viewmodel
        meetingViewModel.isVideoEnabled.addListener(PropertyListeners.onBooleanChanged(v ->
                SwingUtilities.invokeLater(() -> btnCamera.setCustomFill(v ? new Color(90, 160, 255, 160) : null))
        ));

        meetingViewModel.isScreenShareEnabled.addListener(PropertyListeners.onBooleanChanged(v ->
                SwingUtilities.invokeLater(() -> btnShare.setCustomFill(v ? new Color(120, 200, 255, 160) : null))
        ));

        meetingViewModel.meetingId.addListener(evt ->
                SwingUtilities.invokeLater(() -> meetingIdBadge.setText(
                        meetingViewModel.meetingId.get() == null || meetingViewModel.meetingId.get().isEmpty()
                                ? "Meeting: --" : "Meeting: " + meetingViewModel.meetingId.get()))
        );

        meetingViewModel.role.addListener(evt ->
                SwingUtilities.invokeLater(() -> roleLabel.setText(
                        "Role: " + (meetingViewModel.role.get() == null || meetingViewModel.role.get().isEmpty() ? "Guest"
                                : meetingViewModel.role.get())))
        );
    }

    private void registerThemeListener() {
        try {
            ThemeManager tm = ThemeManager.getInstance();
            if (tm != null) {
                tm.addThemeChangeListener(() -> SwingUtilities.invokeLater(() -> {
                    try {
                        // reapply custom tabbed UI to stage and sidebar
                        if (stageTabs != null) {
                            stageTabs.setUI(new ModernTabbedPaneUI());
                            stageTabs.revalidate();
                            stageTabs.repaint();
                        }
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
        if (liveTimer != null) liveTimer.stop();
        liveTimer = new Timer(1000, e ->
                liveClockLabel.setText("Live: " + new SimpleDateFormat("hh:mm:ss a").format(new Date()))
        );
        liveTimer.setInitialDelay(0);
        liveTimer.start();
    }

    private void copyMeetingId() {
        String id = meetingViewModel.meetingId.get();
        if (id != null && !id.isEmpty()) {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(id), null);
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
                }
            }
        } catch (Throwable ignored) {}
    }

    @Override
    public void updateUI() {
        super.updateUI();
        // When Swing updates UI, also reapply our custom tab UI (extra safety)
        SwingUtilities.invokeLater(() -> {
            try {
                if (stageTabs != null) {
                    stageTabs.setUI(new ModernTabbedPaneUI());
                    stageTabs.revalidate();
                    stageTabs.repaint();
                }
                if (sidebarTabs != null) {
                    sidebarTabs.setUI(new ModernTabbedPaneUI());
                    sidebarTabs.revalidate();
                    sidebarTabs.repaint();
                }
                applyTheme();
            } catch (Throwable ignored) {}
        });
    }
}
