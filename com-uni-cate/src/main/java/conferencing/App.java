package com.conferencing;

import java.awt.CardLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.conferencing.theme.ThemeManager;
import com.conferencing.views.LoginPage;
import com.conferencing.views.MainPage;
import com.conferencing.views.MeetingInterface;

public class App {
    private JFrame frame;
    private JPanel mainPanel;
    private CardLayout cardLayout;

    public static final String LOGIN_PAGE = "Login";
    public static final String MAIN_PAGE = "Main";
    public static final String MEETING_PAGE = "Meeting";

    public App() {
        // Initialize ThemeManager first
        ThemeManager.getInstance().setMainFrame(frame);
        initComponents();
    }

    private void initComponents() {
        frame = new JFrame("Comm-Uni-Cate");
        ThemeManager.getInstance().setMainFrame(frame); // Pass frame to manager

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(new LoginPage(this), LOGIN_PAGE);
        mainPanel.add(new MainPage(this), MAIN_PAGE);
        mainPanel.add(new MeetingInterface(this), MEETING_PAGE);

        frame.add(mainPanel);
    }

    public void showPage(String pageName) {
        cardLayout.show(mainPanel, pageName);
    }

    public void start() {
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new App().start());
    }
}

