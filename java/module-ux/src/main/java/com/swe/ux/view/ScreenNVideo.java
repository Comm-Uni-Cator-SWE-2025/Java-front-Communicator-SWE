/**
 *  Contributed by Sandeep Kumar.
 */
package com.swe.ux.view;

import com.swe.controller.RPCinterface.AbstractRPC;
import com.swe.screenNVideo.Utils;
import com.swe.ux.binding.PropertyListeners;
import com.swe.ux.model.UIImage;
import com.swe.controller.Meeting.UserProfile;
import com.swe.ux.theme.ThemeManager;
import com.swe.ux.ui.ParticipantPanel;
import com.swe.ux.viewmodel.MeetingViewModel;
import com.swe.ux.viewmodel.ScreenNVideoModel;

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

import static com.swe.screenNVideo.Utils.getSelfIP;

public class ScreenNVideo extends JPanel implements ParticipantPanel.ParticipantPanelListener {

    private final JPanel videoGrid;
    /** Tracks participant panels by ip for add/remove operations. */
    private static Map<String, ParticipantPanel> participantPanels;
    /** Container for videoGrid to enable scrolling when full. */
    private JScrollPane scrollPane;

    private JPanel contentPanel;

    /** Panel to hold the main zoomed-in video. */
    private JPanel zoomedPanel;
    /** Tracks the IP of the currently zoomed participant, or null if in gallery view. */
    private String zoomedParticipantIp = null;

    private static final Dimension FILMSTRIP_PANEL_SIZE = new Dimension(180, 120);

    private int currentGalleryCols = -1;

    private static long start = 0;

    private static final AtomicBoolean updating = new AtomicBoolean(false);

    private final MeetingViewModel meetingViewModel;

    public ScreenNVideo(MeetingViewModel meetingViewModel) {
        this.meetingViewModel = meetingViewModel;
        this.videoGrid = new JPanel();
        participantPanels = new HashMap<>();
        initializeUI();
        setupBindings();

        updateVideoGridLayout();
        applyTheme();
    }

    private String getIPFromClientNodeString(String val) {
        if (!val.startsWith("ClientNode")) {
            return val;
        }
        final String ipVal = val.substring(val.indexOf("hostname=") + 9);
        return ipVal.substring(0, ipVal.indexOf(",") );
    }

    /**
     * Adds a participant panel to the grid.
     * Prevents duplicates and updates the grid layout.
     * @param name The participant's display name.
     * @param ipUnConsumable The participant's ip.
     */
    private void addParticipant(String name, String ipUnConsumable) {
        final String ip = getIPFromClientNodeString(ipUnConsumable);
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
    private void removeParticipant(String ip) {
        ParticipantPanel panel = participantPanels.remove(ip);

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
    private void updateVideoGridLayout() {
        if (zoomedParticipantIp == null) {

            int width = scrollPane.getViewport().getWidth();
            int height = scrollPane.getViewport().getHeight();
            if (width == 0 || height == 0) {
                return;
            }

            int hgap = 10;
            int vgap = 10;

            int newCols;
            if (width > 920) {
                newCols = 3;
            } else if (width > 610) {
                newCols = 2;
            } else {
                newCols = 1;
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
            int participantCount = participantPanels.size();
            int newRows = (int) Math.ceil((double) participantCount / newCols);
            newRows = Math.max(1, newRows);

            // Calculate panel dimensions based on both width and height constraints
            int availableWidth = width - (hgap * (newCols - 1));
            int availableHeight = height - (vgap * (newRows - 1));
            
            int newPanelWidth = availableWidth / newCols;
            int newPanelHeightFromWidth = (int) (newPanelWidth * (9.0 / 16.0));
            int newPanelHeightFromHeight = availableHeight / newRows;
            
            // Use the constraint that allows panels to be as large as possible
            // while maintaining 16:9 aspect ratio and fitting within available space
            int newPanelHeight;
            if (newPanelHeightFromWidth <= newPanelHeightFromHeight) {
                // Width is the limiting factor
                newPanelHeight = newPanelHeightFromWidth;
            } else {
                // Height is the limiting factor
                newPanelHeight = newPanelHeightFromHeight;
                newPanelWidth = (int) (newPanelHeight * (16.0 / 9.0));
            }
            
            Dimension newSize = new Dimension(newPanelWidth, newPanelHeight);

            updateGalleryPanelSizes(newSize);

            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setPreferredSize(null);
        }
        else {
            currentGalleryCols = -1;

            int filmstripCount = videoGrid.getComponentCount();
            videoGrid.setLayout(new GridLayout(Math.max(1, filmstripCount), 1, 10, 10)); // Vertical grid (X rows, 1 col)
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setPreferredSize(new Dimension(FILMSTRIP_PANEL_SIZE.width + 20, 0)); // +20 for scrollbar/padding
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
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Panel to hold the zoomed-in video
        zoomedPanel = new JPanel(new BorderLayout());

        // Content panel to hold video grid
        contentPanel = new JPanel(new BorderLayout(10, 10));
        scrollPane = new JScrollPane(videoGrid);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Add listener to the viewport which is the part that changes size
        scrollPane.getViewport().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
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
        Set<String> visibleIps = new HashSet<>();

        if (zoomedParticipantIp != null) {
            visibleIps.add(zoomedParticipantIp);
        }

        Rectangle viewRect = scrollPane.getViewport().getViewRect();

        for (Map.Entry<String, ParticipantPanel> entry : participantPanels.entrySet()) {
            String ip = entry.getKey();
            ParticipantPanel panel = entry.getValue();

            if (ip.equals(zoomedParticipantIp)) {
                continue;
            }

            // Only check panels that are actually in the videoGrid
            if (panel.getParent() == videoGrid) {
                // Get panel bounds relative to the videoGrid
                Rectangle panelBounds = panel.getBounds();

                // Check if the panel intersects with the visible part of the scroll pane
                if (viewRect.intersects(panelBounds)) {
                    visibleIps.add(ip);
                }
            }
        }

        ScreenNVideoModel.getInstance(meetingViewModel.rpc).updateVisibleParticipants(visibleIps);
    }

    /**
     * Nullify the image for a participant panel.
     * @param ip The participant's ip.
     */
    public void nullifyImage(String ip) {

        final ParticipantPanel activeParticipantPanel = participantPanels.get(ip);
        if (activeParticipantPanel == null) {
            System.err.println("No active participant panel initialized");
            return;
        }
        SwingUtilities.invokeLater(() -> {
            activeParticipantPanel.setImage(null);
        });
    }

    /**
     * Display a frame from int[][] pixels.
     * The image fully covers the active ParticipantPanel.
     * Drops new frames if the previous one is still being processed.
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
        if (!updating.compareAndSet(false, true)) {
            System.err.println("Dropping frame");
            uiImage.setIsSuccess(false);
            return;
        }

        final BufferedImage bufferedImage = uiImage.image();

        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("Client FPS : " + (int)(1000.0 / ((System.nanoTime() - start) / 1_000_000.0)));
                activeParticipantPanel.setImage(bufferedImage);
                start = System.nanoTime();
            } finally {
                // release flag so next frame can proceed
                updating.set(false);
            }
        });
    }


    /**
     * Setup bindings between ViewModel and UI components.
     * These bindings will be used by the respective team implementations.
     */
    private void setupBindings() {

        // Bind participant added event
        meetingViewModel.participants.addListener(PropertyListeners.onListChanged((List<UserProfile> participants) -> {
            System.out.println("Participants updated");

            // Handle participant removal
            java.util.Set<String> currentIps = new java.util.HashSet<>();
            for (UserProfile p : participants) {
                currentIps.add(p.getEmail());
            }
            java.util.Set<String> panelsToRemove = new java.util.HashSet<>(participantPanels.keySet());
            panelsToRemove.removeAll(currentIps);

            SwingUtilities.invokeLater(() -> {
                for (String ipToRemove : panelsToRemove) {
                    System.out.println("Removing participant with IP: " + ipToRemove);
                    removeParticipant(ipToRemove);
                }
            });

            // Handle participant addition
            participants.forEach(participant -> {
                System.out.println("Adding participant: " + participant.getDisplayName() + " with email: " + participant.getEmail());
                addParticipant(participant.getDisplayName(), participant.getEmail());
            });
        }));

        ScreenNVideoModel.getInstance(this.meetingViewModel.rpc).setOnImageReceived(ScreenNVideo::displayFrame);

        // AbstractRPC rpc = DummyRPC.getInstance();
        this.meetingViewModel.rpc.subscribe(Utils.STOP_SHARE, (args) -> {
            String ip = new String(args);
            nullifyImage(ip);
            return new byte[0];
        });

    }

    private void applyTheme() {
        ThemeManager.getInstance().applyThemeRecursively(this);
    }

    private void zoomIn(String ip) {
        ParticipantPanel panel = participantPanels.get(ip);
        if (panel == null) return;

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
        if (zoomedParticipantIp == null) return;

        ParticipantPanel panel = participantPanels.get(zoomedParticipantIp);
        if (panel == null) {
            zoomedParticipantIp = null;
        }

        contentPanel.remove(zoomedPanel);
        contentPanel.remove(scrollPane);

        if(panel != null) {
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

    private void swapZoom(String newIp) {
        ParticipantPanel oldZoomedPanel = participantPanels.get(zoomedParticipantIp);
        ParticipantPanel newZoomedPanel = participantPanels.get(newIp);

        if (newZoomedPanel == null) return;

        if(oldZoomedPanel != null) {
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
    public void onZoomToggle(String ip) {
        if (meetingViewModel.participants.get().size() == 1) {
            return;
        }
        if (zoomedParticipantIp == null) {
            ScreenNVideoModel.getInstance(meetingViewModel.rpc).requestUncompressedData(ip);
            zoomIn(ip);
        } else if (zoomedParticipantIp.equals(ip)) {
            ScreenNVideoModel.getInstance(meetingViewModel.rpc).requestCompressedData(ip);
            zoomOut();
        } else {
            ScreenNVideoModel.getInstance(meetingViewModel.rpc).requestUncompressedData(ip);
            swapZoom(ip);
        }
    }

    private void updateFilmstripPanelSizes() {
        for (Component comp : videoGrid.getComponents()) {
            if (comp instanceof ParticipantPanel) {
                ((ParticipantPanel) comp).setPreferredSize(FILMSTRIP_PANEL_SIZE);
            }
        }
    }

    private void updateGalleryPanelSizes(Dimension newSize) {
        for (Component comp : videoGrid.getComponents()) {
            if (comp instanceof ParticipantPanel) {
                ((ParticipantPanel) comp).setPreferredSize(newSize);
            }
        }
    }
}
