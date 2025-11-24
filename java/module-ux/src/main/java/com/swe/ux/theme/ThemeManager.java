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

/**
 * Manages theme switching and persistence across the application.
 */
public class ThemeManager {
    /**
     * Singleton instance.
     */
    private static ThemeManager instance;

    /**
     * Current theme in use.
     */
    private Theme currentTheme = new LightTheme();
    /**
     * Main frame reference.
     */
    private JFrame mainFrame;
    /**
     * Application instance reference.
     */
    private App app;
    /**
     * List of theme change listeners.
     */
    private final List<Runnable> themeListeners = new ArrayList<>();

    /**
     * Cloud library for theme persistence.
     */
    private final CloudFunctionLibrary cloudLibrary = new CloudFunctionLibrary();
    /**
     * Cloud container name for theme data.
     */
    private static final String THEME_CONTAINER = "UX";
    /**
     * Cloud type identifier for theme.
     */
    private static final String THEME_TYPE = "Theme";
    /**
     * Cloud key name for theme value.
     */
    private static final String THEME_KEY = "color";

    /**
     * Gets the singleton instance of ThemeManager.
     *
     * @return the ThemeManager instance
     */
    public static synchronized ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    /**
     * Gets the current theme.
     *
     * @return current theme
     */
    public Theme getCurrentTheme() {
        return currentTheme;
    }

    /**
     * Sets the main frame reference.
     *
     * @param frame the main JFrame
     */
    public void setMainFrame(final JFrame frame) {
        this.mainFrame = frame;
        applyThemeToUIManager();
    }

    /**
     * Sets the application reference.
     *
     * @param application the App instance
     */
    public void setApp(final App application) {
        this.app = application;
    }

    /**
     * Adds a theme change listener.
     *
     * @param r the Runnable to execute on theme change
     */
    public void addThemeChangeListener(final Runnable r) {
        if (r != null) {
            themeListeners.add(r);
        }
    }


    // ======================================
    // CLOUD LOAD & SAVE (SIMPLE)
    // ======================================

    /**
     * Loads theme preference from cloud storage.
     */
    public void loadThemeFromCloud() {
        if (app == null || app.getCurrentUser() == null) {
            return;
        }

        final String username = app.getCurrentUser().getEmail();
        System.out.println("Loading theme for user: " + username);
        if (username == null || username.isEmpty()) {
            return;
        }

        final Entity req = new Entity(THEME_CONTAINER, THEME_TYPE, username,
                THEME_KEY, -1, new TimeRange(0, 0), null);
        cloudLibrary.cloudGet(req).thenAccept(res -> {
            System.out.println("Theme fetch response: " + res);
            if (res.data() != null) {
                try {
                    final JsonNode dataNode = res.data();
                    final String themeStr;
                    if (dataNode.isTextual()) {
                        themeStr = dataNode.asText();
                    } else {
                        themeStr = dataNode.get(THEME_KEY).asText();
                    }

                    if ("dark".equalsIgnoreCase(themeStr)) {
                        currentTheme = new DarkTheme();
                    } else {
                        currentTheme = new LightTheme();
                    }
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
        if (app == null || app.getCurrentUser() == null) {
            return;
        }

        try {
            final String username = app.getCurrentUser().getEmail();
            if (username == null || username.isEmpty()) {
                return;
            }

            final String themeValue;
            if (currentTheme.isDark()) {
                themeValue = "dark";
            } else {
                themeValue = "light";
            }
            final ObjectMapper mapper = new ObjectMapper();
            final JsonNode dataNode = mapper.createObjectNode().put(THEME_KEY, themeValue);

            final Entity req = new Entity(THEME_CONTAINER, THEME_TYPE, username,
                    THEME_KEY, -1, new TimeRange(0, 0), dataNode);

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

    /**
     * Toggles between light and dark themes.
     */
    public void toggleTheme() {
        if (currentTheme instanceof LightTheme) {
            currentTheme = new DarkTheme();
        } else {
            currentTheme = new LightTheme();
        }
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

        themeListeners.forEach(r -> {
            try {
                r.run();
            } catch (Exception ignored) {
                // Ignore listener errors
            }
        });
        if (app != null) {
            app.refreshTheme();
        }
    }


    private void applyThemeToUIManager() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
            // Continue with current look and feel
        }

        UIManager.put("Panel.background", currentTheme.getBackgroundColor());
        UIManager.put("Panel.foreground", currentTheme.getTextColor());
        UIManager.put("Label.foreground", currentTheme.getTextColor());
        UIManager.put("Button.background", currentTheme.getPrimaryColor());
        UIManager.put("Button.foreground", Color.WHITE);
    }

    private void reapplyTabbedUIs(final Container root) {
        if (root == null) {
            return;
        }
        for (Component c : root.getComponents()) {
            if (c instanceof JTabbedPane) {
                final JTabbedPane tp = (JTabbedPane) c;
                tp.setUI(new ModernTabbedPaneUI());
            }
            if (c instanceof Container) {
                final Container child = (Container) c;
                reapplyTabbedUIs(child);
            }
        }
    }


    /**
     * Applies theme recursively to a component tree.
     *
     * @param root the root JComponent
     */
    public void applyThemeRecursively(final JComponent root) {
        if (root == null) {
            return;
        }
        root.setBackground(currentTheme.getBackgroundColor());
        root.setForeground(currentTheme.getTextColor());
        for (Component child : root.getComponents()) {
            if (child instanceof JComponent) {
                final JComponent jc = (JComponent) child;
                applyThemeRecursively(jc);
            }
        }
    }
}
