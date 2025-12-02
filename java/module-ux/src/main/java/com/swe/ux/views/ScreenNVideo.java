package com.swe.ux.views;

import com.swe.screenNVideo.Utils;
import com.swe.ux.analytics.ScreenShareTelemetryCollector;
import com.swe.ux.binding.PropertyListeners;
import com.swe.ux.model.UIImage;
import com.swe.controller.Meeting.UserProfile;
import com.swe.ux.theme.ThemeManager;
import com.swe.ux.ui.ParticipantPanel;
import com.swe.ux.viewmodels.MeetingViewModel;
import com.swe.ux.viewmodels.ScreenNVideoModel;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Contributed by Sandeep Kumar.
 */
public class ScreenNVideo extends JPanel implements ParticipantPanel.ParticipantPanelListener {
    /**
     * Default padding value.
     */
    private static final int DEFAULT_PADDING = 10;
    /**
     * Default gap value.
     */
    private static final int DEFAULT_GAP = 10;
    /**
     * Wide width threshold.
     */
    private static final int WIDE_WIDTH_THRESHOLD = 920;
    /**
     * Medium width threshold.
     */
    private static final int MEDIUM_WIDTH_THRESHOLD = 610;
    /**
     * Wide columns count.
     */
    private static final int WIDE_COLS = 3;
    /**
     * Medium columns count.
     */
    private static final int MEDIUM_COLS = 2;
    /**
     * Narrow columns count.
     */
    private static final int NARROW_COLS = 1;
    /**
     * Aspect ratio width.
     */
    private static final double ASPECT_RATIO_WIDTH = 16.0;
    /**
     * Aspect ratio height.
     */
    private static final double ASPECT_RATIO_HEIGHT = 9.0;
    /**
     * Filmstrip scrollbar padding.
     */
    private static final int FILMSTRIP_SCROLLBAR_PADDING = 20;
    /**
     * Filmstrip panel size.
     */
    private static final Dimension FILMSTRIP_PANEL_SIZE = new Dimension(180, 120);

    /**
     * Video grid panel.
     */
    private final JPanel videoGrid;
    /**
     * Tracks participant panels by ip for add/remove operations.
     */
    private static Map<String, ParticipantPanel> participantPanels;
    /**
     * Container for videoGrid to enable scrolling when full.
     */
    private JScrollPane scrollPane;
    /**
     * Content panel.
     */
    private JPanel contentPanel;
    /**
     * Panel to hold the main zoomed-in video.
     */
    private JPanel zoomedPanel;
    /**
     * Tracks the ip of the currently zoomed participant, or null if in gallery view.
     */
    private String zoomedParticipantIp = null;
    /**
     * Current gallery column count.
     */
    private int currentGalleryCols = -1;
    /**
     * Atomic boolean for update tracking.
     */
    private static final AtomicBoolean UPDATING = new AtomicBoolean(false);
    /**
     * Meeting view model.
     */
    private final MeetingViewModel meetingViewModel;

    /**
     * Creates a new ScreenNVideo.
     *
     * @param mainViewModel the meeting view model
     */
    public ScreenNVideo(final MeetingViewModel mainViewModel) {
        this.meetingViewModel = mainViewModel;
        this.videoGrid = new JPanel();
        participantPanels = new HashMap<>();
        initializeUI();
        setupBindings();

        updateVideoGridLayout();
        applyTheme();
    }

    /**
     * Adds a participant panel to the grid.
     * Prevents duplicates and updates the grid layout.
     * @param name The participant's display name.
     * @param ip The participant's ip.
     */
    private void addParticipant(final String name, final String ip) {
        if (participantPanels.containsKey(ip)) {
            return;
        }

        final ParticipantPanel panel = new ParticipantPanel(name, ip);
        panel.setParticipantListener(this);

        participantPanels.put(ip, panel);
        videoGrid.add(panel);

        updateVideoGridLayout();
    }

    /**
     * Removes a participant panel from the grid.
     * Selects a new active panel if needed and updates the layout.
     * @param ip The participant's ip.
     */
    private void removeParticipant(final String ip) {
        final ParticipantPanel panel = participantPanels.remove(ip);

        if (panel != null) {
            videoGrid.remove(panel);

            // Handle if the removed participant was zoomed
            if (ip.equals(zoomedParticipantIp)) {
                zoomOut();
            }

            updateVideoGridLayout();
        }
    }

    /**
     * Dynamically updates the video grid's layout (rows/cols) based on
     * participant count. Enables scrolling for 7+ participants.
     */
    // CHECKSTYLE:OFF: JavaNCSS - Complex layout calculation method
    private void updateVideoGridLayout() {
        // CHECKSTYLE:ON: JavaNCSS
        if (zoomedParticipantIp == null) {
            final int width = scrollPane.getViewport().getWidth();
            final int height = scrollPane.getViewport().getHeight();
            if (width == 0 || height == 0) {
                return;
            }

            final int hgap = DEFAULT_GAP;
            final int vgap = DEFAULT_GAP;

            int newCols;
            if (width > WIDE_WIDTH_THRESHOLD) {
                newCols = WIDE_COLS;
            } else if (width > MEDIUM_WIDTH_THRESHOLD) {
                newCols = MEDIUM_COLS;
            } else {
                newCols = NARROW_COLS;
            }

            newCols = Math.min(newCols, participantPanels.size());

            if (newCols == 0) {
                return;
            }

            if (newCols != currentGalleryCols) {
                currentGalleryCols = newCols;
                // Set a GridLayout with 0 rows (as many as needed) and newCols columns
                videoGrid.setLayout(new GridLayout(0, newCols, hgap, vgap));
            }

            // Calculate number of rows needed
            final int participantCount = participantPanels.size();
            int newRows = (int) Math.ceil((double) participantCount / newCols);
            newRows = Math.max(1, newRows);

            // Calculate panel dimensions based on both width and height constraints
            final int availableWidth = width - (hgap * (newCols - 1));
            final int availableHeight = height - (vgap * (newRows - 1));
            
            int newPanelWidth = availableWidth / newCols;
            final int newPanelHeightFromWidth = (int) (newPanelWidth * (ASPECT_RATIO_HEIGHT / ASPECT_RATIO_WIDTH));
            final int newPanelHeightFromHeight = availableHeight / newRows;
            
            // Use the constraint that allows panels to be as large as possible
            // while maintaining 16:9 aspect ratio and fitting within available space
            final int newPanelHeight;
            if (newPanelHeightFromWidth <= newPanelHeightFromHeight) {
                // Width is the limiting factor
                newPanelHeight = newPanelHeightFromWidth;
            } else {
                // Height is the limiting factor
                newPanelHeight = newPanelHeightFromHeight;
                newPanelWidth = (int) (newPanelHeight * (ASPECT_RATIO_WIDTH / ASPECT_RATIO_HEIGHT));
            }
            
            final Dimension newSize = new Dimension(newPanelWidth, newPanelHeight);

            updateGalleryPanelSizes(newSize);

            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setPreferredSize(null);
        } else {
            currentGalleryCols = -1;

            final int filmstripCount = videoGrid.getComponentCount();
            videoGrid.setLayout(new GridLayout(Math.max(1, filmstripCount), 1, DEFAULT_GAP, DEFAULT_GAP));
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setPreferredSize(new Dimension(FILMSTRIP_PANEL_SIZE.width + FILMSTRIP_SCROLLBAR_PADDING, 0));
        }

        videoGrid.revalidate();
        videoGrid.repaint();
        scrollPane.revalidate();
        scrollPane.repaint();

        SwingUtilities.invokeLater(this::calculateVisibleParticipants);
    }

    /**
     * Initialize UI components and layout.
     */
    private void initializeUI() {
        setLayout(new BorderLayout(DEFAULT_PADDING, DEFAULT_PADDING));
        setBorder(new EmptyBorder(DEFAULT_PADDING, DEFAULT_PADDING, DEFAULT_PADDING, DEFAULT_PADDING));

        // Panel to hold the zoomed-in video
        zoomedPanel = new JPanel(new BorderLayout());

        // Content panel to hold video grid
        contentPanel = new JPanel(new BorderLayout(DEFAULT_PADDING, DEFAULT_PADDING));
        scrollPane = new JScrollPane(videoGrid);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Add listener to the viewport which is the part that changes size
        scrollPane.getViewport().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(final ComponentEvent e) {
                updateVideoGridLayout();
            }
        });

        // Add listener for scrolling to update visibility
        scrollPane.getViewport().addChangeListener(e -> calculateVisibleParticipants());

        // Start in gallery view, with scrollPane in the center
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);
    }

    /**
     * Calculates which participants are currently visible on the screen.
     * Updates the ScreenNVideoModel with the list of visible IPs.
     */
    private void calculateVisibleParticipants() {
        final Set<String> visibleIps = new HashSet<>();

        if (zoomedParticipantIp != null) {
            visibleIps.add(zoomedParticipantIp);
        }

        final Rectangle viewRect = scrollPane.getViewport().getViewRect();

        for (final Map.Entry<String, ParticipantPanel> entry : participantPanels.entrySet()) {
            final String ip = entry.getKey();
            final ParticipantPanel panel = entry.getValue();

            if (ip.equals(zoomedParticipantIp)) {
                continue;
            }

            // Only check panels that are actually in the videoGrid
            if (panel.getParent() == videoGrid) {
                // Get panel bounds relative to the videoGrid
                final Rectangle panelBounds = panel.getBounds();

                // Check if the panel intersects with the visible part of the scroll pane
                if (viewRect.intersects(panelBounds)) {
                    visibleIps.add(ip);
                }
            }
        }

        ScreenNVideoModel.getInstance(meetingViewModel.getRpc()).updateVisibleParticipants(visibleIps);
    }

    /**
     * Nullify the image for a participant panel.
     * @param ip The participant's ip.
     */
    public void nullifyImage(final String ip) {

        final ParticipantPanel activeParticipantPanel = participantPanels.get(ip);
        if (activeParticipantPanel == null) {
            System.err.println("No active participant panel initialized");
            return;
        }
        SwingUtilities.invokeLater(() -> {
            activeParticipantPanel.setImage(null);
            activeParticipantPanel.setDataRate(0);
        });
    }

    /**
     * Display a frame from int[][] pixels.
     * The image fully covers the active ParticipantPanel.
     * Drops new frames if the previous one is still being processed.
     *
     * @param uiImage the UI image to display
     */
    public static void displayFrame(final UIImage uiImage) {
        final String ip = uiImage.ip();
//        System.out.println("Got : " + ip);
        final ParticipantPanel activeParticipantPanel = participantPanels.get(ip);
        if (activeParticipantPanel == null) {
            System.err.println("No active participant panel initialized");
            uiImage.setIsSuccess(false);
            return;
        }

        // if already updating, drop this frame
        if (!UPDATING.compareAndSet(false, true)) {
            System.err.println("Dropping frame");
            uiImage.setIsSuccess(false);
            return;
        }

        final BufferedImage bufferedImage = uiImage.image();

        SwingUtilities.invokeLater(() -> {
            try {
                activeParticipantPanel.setImage(bufferedImage);
                activeParticipantPanel.setDataRate(uiImage.dataRate());
                ScreenShareTelemetryCollector.getInstance().recordFrameRendered();
            } finally {
                // release flag so next frame can proceed
                UPDATING.set(false);
            }
        });
    }


    /**
     * Setup bindings between ViewModel and UI components.
     * These bindings will be used by the respective team implementations.
     */
    private void setupBindings() {

        // Bind participant added event
        meetingViewModel.getParticipants().addListener(
                PropertyListeners.onListChanged((List<UserProfile> participants) -> {
                    System.out.println("Participants updated");

                    // Handle participant removal
                    final java.util.Set<String> currentIps = new java.util.HashSet<>();
                    for (final UserProfile p : participants) {
                        currentIps.add(p.getEmail());
                    }
                    final java.util.Set<String> panelsToRemove = new java.util.HashSet<>(participantPanels.keySet());
                    panelsToRemove.removeAll(currentIps);

                    for (final String ipToRemove : panelsToRemove) {
                        System.out.println("Removing participant with ip: " + ipToRemove);
                        removeParticipant(ipToRemove);
                    }

                    // Handle participant addition
                    participants.forEach(participant -> {
                        final String ip = meetingViewModel.getIpToMail().get(participant.getEmail());
                        if (ip == null) {
                            return;
                        }
                        System.out.println("Adding participant: " + participant.getDisplayName() + " with ip: " + ip);
                        addParticipant(participant.getDisplayName(), ip);
                    });
                }));

        ScreenNVideoModel.getInstance(this.meetingViewModel.getRpc()).setOnImageReceived(ScreenNVideo::displayFrame);

        // AbstractRPC rpc = DummyRPC.getInstance();
        this.meetingViewModel.getRpc().subscribe(Utils.STOP_SHARE, args -> {
            final String ip = new String(args);
            nullifyImage(ip);
            return new byte[0];
        });

    }

    private void applyTheme() {
        ThemeManager.getInstance().applyThemeRecursively(this);
    }

    private void zoomIn(final String ip) {
        final ParticipantPanel panel = participantPanels.get(ip);
        if (panel == null) {
            return;
        }

        panel.setPreferredSize(null);
        videoGrid.remove(panel);
        contentPanel.remove(scrollPane);
        zoomedPanel.add(panel, BorderLayout.CENTER);
        contentPanel.add(zoomedPanel, BorderLayout.CENTER);
        contentPanel.add(scrollPane, BorderLayout.EAST);

        zoomedParticipantIp = ip;
        panel.setZoomed(true);

        updateFilmstripPanelSizes();
        updateVideoGridLayout();
        zoomedPanel.revalidate();
        zoomedPanel.repaint();
        contentPanel.revalidate();
        contentPanel.repaint();

        SwingUtilities.invokeLater(this::calculateVisibleParticipants);
    }

    private void zoomOut() {
        if (zoomedParticipantIp == null) {
            return;
        }

        final ParticipantPanel panel = participantPanels.get(zoomedParticipantIp);
        if (panel == null) {
            zoomedParticipantIp = null;
        }

        contentPanel.remove(zoomedPanel);
        contentPanel.remove(scrollPane);

        if (panel != null) {
            zoomedPanel.remove(panel);
            videoGrid.add(panel);
            panel.setZoomed(false);
        }

        contentPanel.add(scrollPane, BorderLayout.CENTER);
        zoomedParticipantIp = null;
        currentGalleryCols = -1;

        updateVideoGridLayout();
        contentPanel.revalidate();
        contentPanel.repaint();

        SwingUtilities.invokeLater(this::calculateVisibleParticipants);
    }

    private void swapZoom(final String newIp) {
        final ParticipantPanel oldZoomedPanel = participantPanels.get(zoomedParticipantIp);
        final ParticipantPanel newZoomedPanel = participantPanels.get(newIp);

        if (newZoomedPanel == null) {
            return;
        }

        if (oldZoomedPanel != null) {
            zoomedPanel.remove(oldZoomedPanel);
            videoGrid.add(oldZoomedPanel);
            oldZoomedPanel.setZoomed(false);
        }
        videoGrid.remove(newZoomedPanel);

        zoomedPanel.add(newZoomedPanel, BorderLayout.CENTER);
        newZoomedPanel.setPreferredSize(null);
        newZoomedPanel.setZoomed(true);

        zoomedParticipantIp = newIp;

        updateFilmstripPanelSizes();
        updateVideoGridLayout();
        zoomedPanel.revalidate();
        zoomedPanel.repaint();
        contentPanel.revalidate();
        contentPanel.repaint();

        SwingUtilities.invokeLater(this::calculateVisibleParticipants);
    }

    @Override
    public void onZoomToggle(final String ip) {
        if (meetingViewModel.getParticipants().get().size() == 1) {
            return;
        }
        if (zoomedParticipantIp == null) {
            ScreenNVideoModel.getInstance(meetingViewModel.getRpc()).requestUncompressedData(ip);
            zoomIn(ip);
        } else if (zoomedParticipantIp.equals(ip)) {
            ScreenNVideoModel.getInstance(meetingViewModel.getRpc()).requestCompressedData(ip);
            zoomOut();
        } else {
            ScreenNVideoModel.getInstance(meetingViewModel.getRpc()).requestUncompressedData(ip);
            swapZoom(ip);
        }
    }

    private void updateFilmstripPanelSizes() {
        for (final Component comp : videoGrid.getComponents()) {
            if (comp instanceof ParticipantPanel) {
                ((ParticipantPanel) comp).setPreferredSize(FILMSTRIP_PANEL_SIZE);
            }
        }
    }

    private void updateGalleryPanelSizes(final Dimension newSize) {
        for (final Component comp : videoGrid.getComponents()) {
            if (comp instanceof ParticipantPanel) {
                ((ParticipantPanel) comp).setPreferredSize(newSize);
            }
        }
    }
}
