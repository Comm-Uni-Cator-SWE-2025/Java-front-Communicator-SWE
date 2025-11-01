package com.conferencing.views;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.JScrollPane;

import com.conferencing.AbstractRPC;
import com.conferencing.App;
import com.conferencing.screenNVideo.RImage;
import com.conferencing.Utils;
import com.conferencing.theme.Theme;
import com.conferencing.theme.ThemeManager;
import com.conferencing.ui.CustomButton;

public class MeetingInterface extends JPanel {

    private final App app;
    private final JPanel videoGrid;
    private final AbstractRPC rpc;
    /** Tracks participant panels by ip for add/remove operations. */
    private static Map<String, ParticipantPanel> participantPanels;
    /** Container for videoGrid to enable scrolling when full. */
    private JScrollPane scrollPane;
    private JPanel controlsPanel;
    private JPanel buttonPanel;
    private JPanel rightControls;
    private JPanel chatPanel;
    private JPanel contentPanel;
    private boolean chatVisible = false;
    private boolean videoOn = false;
    private boolean screenShareOn = false;
    private CustomButton chatButton;

    private static long start = 0;
    private static final AtomicBoolean updating = new AtomicBoolean(false);

    public MeetingInterface(App app, AbstractRPC rpc) {
        this.app = app;
        this.rpc = rpc;
        this.videoGrid = new JPanel();
        this.participantPanels = new HashMap<>();
        initComponents();

        rpc.subscribe(Utils.UPDATE_UI,bytes -> {
            final RImage rImage = RImage.deserialize(bytes);
            final int[][] image = rImage.getImage();
            displayFrame(image, rImage.getIp());
            return new byte[0];
        });
//        addParticipant("Dummy Name 1");
//        addParticipant("Dummy Name 2");
//        addParticipant("Dummy Name 3");
        addParticipant("Other", "10.32.11.242");
        addParticipant("You", getSelfIP());

        updateVideoGridLayout();
    }

    private static String getSelfIP() {
        // Get IP address as string
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            return socket.getLocalAddress().getHostAddress();
        } catch (SocketException | UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Content panel to hold video grid and chat
        contentPanel = new JPanel(new BorderLayout(10, 10));
        scrollPane = new JScrollPane(videoGrid);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // Create chat panel (initially hidden)
        chatPanel = createChatPanel();

        add(contentPanel, BorderLayout.CENTER);

        controlsPanel = createControlsPanel();
        add(controlsPanel, BorderLayout.SOUTH);

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
        System.out.println("Adding " + ip);
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

    private JPanel createChatPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel chatTitle = new JLabel("Chat");
        chatTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        chatTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        panel.add(chatTitle, BorderLayout.NORTH);

        // Empty chat area for now
        JPanel chatArea = new JPanel();
        panel.add(chatArea, BorderLayout.CENTER);

        return panel;
    }

    private void toggleChat() {
        chatVisible = !chatVisible;

        if (chatVisible) {
            // Show chat panel on the right
            contentPanel.add(chatPanel, BorderLayout.EAST);
            chatPanel.setPreferredSize(new Dimension(300, 0));
            chatButton.setText("Close Chat");
        } else {
            // Hide chat panel
            contentPanel.remove(chatPanel);
            chatButton.setText("Chat");
        }

        contentPanel.revalidate();
        contentPanel.repaint();
        applyTheme();
    }

    private JPanel createControlsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JLabel timeLabel = new JLabel("11:45");
        timeLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        panel.add(timeLabel, BorderLayout.WEST);

        buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        CustomButton videoButton = new CustomButton("Video", videoOn);
        videoButton.addActionListener(e -> {
            videoOn = !videoOn;
            if (videoOn) {
                rpc.call(Utils.START_VIDEO_CAPTURE, new byte[0]);
            } else {
                rpc.call(Utils.STOP_VIDEO_CAPTURE, new byte[0]);
            }
            if (!screenShareOn && !videoOn) {

//                SwingUtilities.invokeLater(() -> {
//                    activeParticipantPanel.setImage(null);
//                });
            }
            videoButton.setPrimary(videoOn);
        });
        buttonPanel.add(videoButton);
        buttonPanel.add(new CustomButton("Canvas", false));
        buttonPanel.add(new CustomButton("Participants", false));
        CustomButton shareButton = new CustomButton("Share", videoOn);
        shareButton.addActionListener(e -> {
            screenShareOn = !screenShareOn;
            if (screenShareOn) {
                rpc.call(Utils.START_SCREEN_CAPTURE, new byte[0]);
            } else {
                rpc.call(Utils.STOP_SCREEN_CAPTURE, new byte[0]);
            }
            if (!screenShareOn && !videoOn) {

//                SwingUtilities.invokeLater(() -> {
//                    activeParticipantPanel.setImage(null);
//                });
            }
            shareButton.setPrimary(screenShareOn);
        });
        buttonPanel.add(shareButton);

        CustomButton endCallButton = new CustomButton("End", true);
        endCallButton.addActionListener(e -> app.showPage(App.MAIN_PAGE));
        buttonPanel.add(endCallButton);

        panel.add(buttonPanel, BorderLayout.CENTER);

        rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        chatButton = new CustomButton("Chat", false);
        chatButton.addActionListener(e -> toggleChat());
        rightControls.add(chatButton);
        JLabel copyMeetingLabel = new JLabel("COPY LINK");
        copyMeetingLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        rightControls.add(copyMeetingLabel);
        panel.add(rightControls, BorderLayout.EAST);

        return panel;
    }

    private void applyTheme() {
        Theme theme = ThemeManager.getInstance().getTheme();
        setBackground(theme.getBackground());
        if (videoGrid != null) {
            videoGrid.setBackground(theme.getBackground());
        }
        if (contentPanel != null) {
            contentPanel.setBackground(theme.getBackground());
        }
        if (chatPanel != null) {
            chatPanel.setBackground(theme.getForeground());
            // Apply theme to chat area components
            for(Component comp : chatPanel.getComponents()) {
                if(comp instanceof JPanel) {
                    comp.setBackground(theme.getForeground());
                }
            }
        }
        if (controlsPanel != null) {
            controlsPanel.setBackground(theme.getBackground());

            // Special case for end call button color
            for(Component comp : controlsPanel.getComponents()) {
                if(comp instanceof JPanel) {
                    for(Component btn : ((JPanel)comp).getComponents()) {
                        if(btn instanceof CustomButton && ((CustomButton)btn).getText().contains("End")) {
                            btn.setBackground(theme.getAccent());
                        }
                    }
                }
            }
        }
        if (buttonPanel != null) {
            buttonPanel.setBackground(theme.getBackground());
        }
        if (rightControls != null) {
            rightControls.setBackground(theme.getBackground());
        }
    }

    @Override
    public void updateUI() {
        super.updateUI();
        if (ThemeManager.getInstance() != null) {
            applyTheme();
        }
    }

    /**
     * Display a frame from int[][] pixels.
     * The image fully covers the active ParticipantPanel.
     * Drops new frames if the previous one is still being processed.
     */
    public static void displayFrame(final int[][] pixels, final String ip) {
        System.out.println("Got : " + ip);
        final ParticipantPanel activeParticipantPanel = participantPanels.get(ip);
        if (activeParticipantPanel == null) {
            System.err.println("No active participant panel initialized");
            return;
        }
//        System.out.println("Received");
        // if already updating, drop this frame
        if (!updating.compareAndSet(false, true)) {
            System.err.println("Dropping frame");
            return;
        }

        int height = pixels.length;
        int width = pixels[0].length;

        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int x = 0; x < height; x++) {
            for (int y = 0; y < width; y++) {
                bufferedImage.setRGB(y, x, pixels[x][y]);
            }
        }

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

    private static class ParticipantPanel extends JPanel {
        private final String name;
        private BufferedImage displayImage;

        ParticipantPanel(String name) {
            this.name = name;
            this.displayImage = null;
            setLayout(new BorderLayout());
            setPreferredSize(new Dimension(300, 220));
        }

        void setImage(BufferedImage image) {
            this.displayImage = image;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Theme theme = ThemeManager.getInstance().getTheme();

            // If an image is set, draw it to fill the entire panel
            if (displayImage != null) {
                g2d.drawImage(displayImage, 0, 0, getWidth(), getHeight(), null);
            } else {
                // Default participant view (circular avatar with name)
                g2d.setColor(theme.getForeground());
                g2d.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 20, 20);

                int circleDiameter = Math.min(getWidth(), getHeight()) / 3;
                int circleX = (getWidth() - circleDiameter) / 2;
                int circleY = (getHeight() - circleDiameter) / 2 - 10;
                g2d.setColor(theme.getBackground());
                g2d.fillOval(circleX, circleY, circleDiameter, circleDiameter);

                g2d.setColor(theme.getText());
                g2d.setFont(new Font("SansSerif", Font.BOLD, Math.max(12, circleDiameter / 3)));
                String initials = name.contains(" ") ? ("" + name.charAt(0) + name.substring(name.indexOf(" ") + 1).charAt(0)).toUpperCase() : ("" + name.charAt(0)).toUpperCase();
                g2d.drawString(initials,
                        circleX + (circleDiameter - g2d.getFontMetrics().stringWidth(initials)) / 2,
                        circleY + (circleDiameter - g2d.getFontMetrics().getHeight()) / 2 + g2d.getFontMetrics().getAscent()
                );

                g2d.setFont(new Font("SansSerif", Font.PLAIN, 14));
                g2d.drawString(name,
                        (getWidth() - g2d.getFontMetrics().stringWidth(name)) / 2,
                        circleY + circleDiameter + 20);
            }

            g2d.dispose();
        }
    }
}