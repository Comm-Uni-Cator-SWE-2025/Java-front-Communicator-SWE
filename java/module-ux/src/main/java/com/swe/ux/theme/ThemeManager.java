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
import com.swe.ux.App;
import com.swe.ux.ui.ModernTabbedPaneUI;

import datastructures.Entity;
import datastructures.TimeRange;
import functionlibrary.CloudFunctionLibrary;

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
        System.out.println("Loading theme for user: " + username);
        if (username == null || username.isEmpty()) return;

        Entity req = new Entity(THEME_CONTAINER, THEME_TYPE, username, THEME_KEY, -1, new TimeRange(0, 0), null);
        cloudLibrary.cloudGet(req).thenAccept(res -> {
            System.out.println("Theme fetch response: " + res);
            if (res.data() != null) {
                try {
                    JsonNode dataNode = res.data();
                    String themeStr = dataNode.isTextual() ? dataNode.asText() : dataNode.get(THEME_KEY).asText();
                    
                    currentTheme = "dark".equalsIgnoreCase(themeStr) ? new DarkTheme() : new LightTheme();
                    System.out.println("Theme loaded from cloud: " + themeStr);
                    SwingUtilities.invokeLater(this::applyThemeToUI);
                } catch (Exception e) {
                    System.err.println("Error parsing theme data: " + e.getMessage());
                    // Keep current theme as default
                }
            } else {
                System.out.println("No theme found in cloud, using default");
                // First time login - save current theme to cloud
                saveThemeToCloud();
            }
        }).exceptionally(ex -> {
            System.err.println("Failed to load theme from cloud: " + ex.getMessage());
            // Keep current theme as default and try to save it
            saveThemeToCloud();
            return null;
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
            
            // Try to update first, if it fails (doesn't exist), then create it
            cloudLibrary.cloudUpdate(req).thenAccept(response -> {
                System.out.println("Theme updated in cloud: " + themeValue);
            }).exceptionally(ex -> {
                // If update fails, try to post (create new)
                cloudLibrary.cloudPost(req).thenAccept(response -> {
                    System.out.println("Theme created in cloud: " + themeValue);
                }).exceptionally(createEx -> {
                    System.err.println("Failed to save theme to cloud: " + createEx.getMessage());
                    return null;
                });
                return null;
            });
        } catch (Exception e) {
            System.err.println("Exception saving theme: " + e.getMessage());
        }
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
