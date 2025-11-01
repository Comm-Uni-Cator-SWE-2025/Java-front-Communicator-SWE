/**
 *  Contributed by Priyanshu Pandey.
 */
package com.swe.ux.view;

import com.swe.ux.binding.PropertyListeners;
import com.swe.ux.model.UIImage;
import com.swe.ux.model.User;
import com.swe.ux.theme.ThemeManager;
import com.swe.ux.ui.ParticipantPanel;
import com.swe.ux.viewmodel.MeetingViewModel;
import com.swe.ux.viewmodel.ScreenNVideoModel;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.swe.screenNVideo.Utils.getSelfIP;

public class ScreenNVideo extends JPanel {

    private final JPanel videoGrid;
    /** Tracks participant panels by ip for add/remove operations. */
    private static Map<String, ParticipantPanel> participantPanels;
    /** Container for videoGrid to enable scrolling when full. */
    private JScrollPane scrollPane;

    private JPanel contentPanel;

    private static long start = 0;

    private static final AtomicBoolean updating = new AtomicBoolean(false);

    private final MeetingViewModel meetingViewModel;

    public ScreenNVideo(MeetingViewModel meetingViewModel) {
        this.meetingViewModel = meetingViewModel;
        this.videoGrid = new JPanel();
        participantPanels = new HashMap<>();
        initializeUI();
        setupBindings();

        addParticipant("You", getSelfIP());

        updateVideoGridLayout();
        applyTheme();
    }

    /**
     * Adds a participant panel to the grid.
     * Prevents duplicates and updates the grid layout.
     * @param name The participant's display name.
     * @param ip The participant's ip.
     */
    private void addParticipant(String name, String ip) {
        if (participantPanels.containsKey(ip)) {
            return;
        }

        final ParticipantPanel panel = new ParticipantPanel(name);
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

            updateVideoGridLayout();
        }
    }

    /**
     * Dynamically updates the video grid's layout (rows/cols) based on
     * participant count. Enables scrolling for 7+ participants.
     */
    private void updateVideoGridLayout() {
        int count = participantPanels.size();

        if (count <= 1) {
            videoGrid.setLayout(new GridLayout(1, 1, 10, 10));
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        } else if (count == 2) {
            videoGrid.setLayout(new GridLayout(1, 2, 10, 10));
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        } else if (count <= 4) {
            videoGrid.setLayout(new GridLayout(2, 2, 10, 10));
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        } else if (count <= 6) {
            videoGrid.setLayout(new GridLayout(2, 3, 10, 10));
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        } else {
            videoGrid.setLayout(new GridLayout(0, 3, 10, 10));
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        }

        videoGrid.revalidate();
        videoGrid.repaint();
        scrollPane.revalidate();
        scrollPane.repaint();
    }

    /**
     * Initialize UI components and layout.
     */
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Content panel to hold video grid
        contentPanel = new JPanel(new BorderLayout(10, 10));
        scrollPane = new JScrollPane(videoGrid);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        contentPanel.add(scrollPane, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);
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
            return;
        }

        // if already updating, drop this frame
        if (!updating.compareAndSet(false, true)) {
            System.err.println("Dropping frame");
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
        meetingViewModel.participants.addListener(PropertyListeners.onListChanged((List<User> participants) -> {
            participants.forEach(participant -> {
                addParticipant(participant.getUsername(), participant.getId());
            });
        }));

        ScreenNVideoModel.getInstance().setOnImageReceived(ScreenNVideo::displayFrame);
    }

    private void applyTheme() {
        ThemeManager.getInstance().applyTheme(this);
    }
}
