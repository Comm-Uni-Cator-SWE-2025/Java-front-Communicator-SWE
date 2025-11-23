package com.swe.ux.theme;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import datastructures.Entity;
import datastructures.TimeRange;
import functionlibrary.CloudFunctionLibrary;
import com.swe.ux.App;
import com.swe.ux.ui.ModernTabbedPaneUI;

public class ThemeManager {

    private static ThemeManager instance;

    private Theme currentTheme = new LightTheme();
    private JFrame mainFrame;
    private App app;
    private final List<Runnable> themeListeners = new ArrayList<>();

    private final CloudFunctionLibrary cloudLibrary = new CloudFunctionLibrary();
    private static final String THEME_CONTAINER = "UX";
    private static final String THEME_TYPE = "Theme";
    private static final String THEME_KEY = "color";

    public static synchronized ThemeManager getInstance() {
        return instance == null ? (instance = new ThemeManager()) : instance;
    }

    public Theme getCurrentTheme() { return currentTheme; }
    public void setMainFrame(JFrame frame) { this.mainFrame = frame; applyThemeToUIManager(); }
    public void setApp(App app) { this.app = app; }
    public void addThemeChangeListener(Runnable r) { if (r != null) themeListeners.add(r); }


    // ======================================
    // CLOUD LOAD & SAVE (SIMPLE)
    // ======================================

    public void loadThemeFromCloud() {
        if (app == null || app.getCurrentUser() == null) return;

        String username = app.getCurrentUser().getEmail();
        System.out.println("Username: " + username);
        if (username == null || username.isEmpty()) return;

        Entity req = new Entity(THEME_CONTAINER, THEME_TYPE, username, THEME_KEY, -1, new TimeRange(0, 0), null);
        cloudLibrary.cloudGet(req).thenAccept(res -> {
            System.out.println("Response: " + res);
            JsonNode dataNode = res.data();
            String themeStr = dataNode.isTextual() ? dataNode.asText() : dataNode.get(THEME_KEY).asText();

            currentTheme = "dark".equalsIgnoreCase(themeStr) ? new DarkTheme() : new LightTheme();
            SwingUtilities.invokeLater(this::applyThemeToUI);
        });
    }

    private void saveThemeToCloud() {
        if (app == null || app.getCurrentUser() == null) return;

        try {
            String username = app.getCurrentUser().getEmail();
            if (username == null || username.isEmpty()) return;

            String themeValue = currentTheme.isDark() ? "dark" : "light";
            ObjectMapper mapper = new ObjectMapper();
            JsonNode dataNode = mapper.createObjectNode().put(THEME_KEY, themeValue);

            Entity req = new Entity(THEME_CONTAINER, THEME_TYPE, username, THEME_KEY, -1, new TimeRange(0, 0), dataNode);
            cloudLibrary.cloudPost(req);
            System.out.println("Theme saved to cloud: " + themeValue);
        } catch (Exception ignored) {}
    }


    // ======================================
    // THEME SWITCH
    // ======================================

    public void toggleTheme() {
        currentTheme = currentTheme instanceof LightTheme ? new DarkTheme() : new LightTheme();
        saveThemeToCloud();
        applyThemeToUI();
    }


    // ======================================
    // APPLY
    // ======================================

    private void applyThemeToUI() {
        applyThemeToUIManager();

        if (mainFrame != null) {
            SwingUtilities.updateComponentTreeUI(mainFrame);
            SwingUtilities.invokeLater(() -> {
                reapplyTabbedUIs(mainFrame.getContentPane());
                mainFrame.repaint();
            });
        }

        themeListeners.forEach(r -> { try { r.run(); } catch (Exception ignored) {} });
        if (app != null) app.refreshTheme();
    }


    private void applyThemeToUIManager() {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); } catch (Exception ignored) {}

        UIManager.put("Panel.background", currentTheme.getBackgroundColor());
        UIManager.put("Panel.foreground", currentTheme.getTextColor());
        UIManager.put("Label.foreground", currentTheme.getTextColor());
        UIManager.put("Button.background", currentTheme.getPrimaryColor());
        UIManager.put("Button.foreground", Color.WHITE);
    }

    private void reapplyTabbedUIs(Container root) {
        if (root == null) return;
        for (Component c : root.getComponents()) {
            if (c instanceof JTabbedPane tp) {
                tp.setUI(new ModernTabbedPaneUI());
            }
            if (c instanceof Container child) reapplyTabbedUIs(child);
        }
    }


    public void applyThemeRecursively(JComponent root) {
        if (root == null) return;
        root.setBackground(currentTheme.getBackgroundColor());
        root.setForeground(currentTheme.getTextColor());
        for (Component child : root.getComponents())
            if (child instanceof JComponent jc) applyThemeRecursively(jc);
    }
}
