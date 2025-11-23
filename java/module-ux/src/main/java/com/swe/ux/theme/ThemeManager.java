/*
 * -----------------------------------------------------------------------------
 *  File: ThemeManager.java
 *  Owner: Aryan Mathur
 *  Roll Number : 122201017
 *  Module : UX
 *
 * -----------------------------------------------------------------------------
 */

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
import javax.swing.plaf.TabbedPaneUI;

import com.swe.ux.App;
import com.swe.ux.ui.ModernTabbedPaneUI;


/**
 * Central theme controller. Handles global Swing UI defaults,
 * supports dynamic theme switching, and updates the main frame.
 */
public class ThemeManager {
    private final List<Runnable> themeListeners = new ArrayList<>();


    private static ThemeManager instance;


    public void addThemeChangeListener(Runnable r) {
        if (r != null) themeListeners.add(r);
    }


    private Theme currentTheme;
    private JFrame mainFrame;   // root window
    private App app;            // optional: for notifying the application

    private ThemeManager() {
        // default theme
        currentTheme = new LightTheme();
    }

    public static synchronized ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    // ============================================================
    // Accessors
    // ============================================================
    public Theme getCurrentTheme() {
        return currentTheme;
    }

    public void setMainFrame(JFrame frame) {
        this.mainFrame = frame;
        applyThemeToUIManager();
    }

    public void setApp(App app) {
        this.app = app;
    }
    // recursively reapply custom tab UI to protect against LAF reset
    private void reapplyTabbedUIs(Container root) {
        if (root == null) return;
        for (Component c : root.getComponents()) {
            if (c instanceof JTabbedPane tp) {
                try {
                    tp.setUI(new com.swe.ux.ui.ModernTabbedPaneUI());
                    tp.revalidate();
                    tp.repaint();
                } catch (Throwable ignored) {}
            }
            if (c instanceof Container child) {
                reapplyTabbedUIs(child);
            }
        }
    }

    private void fixTabbedPanes(Container root) {
    for (Component c : root.getComponents()) {
        if (c instanceof JTabbedPane tp) {
            tp.setUI(new ModernTabbedPaneUI());   // <-- Reapply modern UI
            tp.revalidate();
            tp.repaint();
        }
        if (c instanceof Container child) {
            fixTabbedPanes(child);
        }
    }
}


    // ============================================================
    // THEME TOGGLING
    // ============================================================
    public void toggleTheme() {
        if (currentTheme instanceof LightTheme) currentTheme = new DarkTheme();
        else currentTheme = new LightTheme();

        applyThemeToUIManager();

        if (mainFrame != null) {
            // update Swing L&F (this recreates UI delegates)
            SwingUtilities.updateComponentTreeUI(mainFrame);

            // ensure our custom tabbed UI is reapplied after the L&F pass
            SwingUtilities.invokeLater(() -> {
                try {
                    reapplyTabbedUIs(mainFrame.getContentPane());
                    mainFrame.repaint();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            });
        }
        

        // notify registered listeners so view code can do their own re-setup
        for (Runnable r : themeListeners) {
            try { r.run(); } catch (Throwable ignored) {}
        }

        if (app != null) {
            app.refreshTheme();
        }
    }



    // ============================================================
    // APPLY TO SWING UI DEFAULTS
    // ============================================================
    private void applyThemeToUIManager() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        // PANEL
        UIManager.put("Panel.background", currentTheme.getBackgroundColor());
        UIManager.put("Panel.foreground", currentTheme.getTextColor());

        // LABELS
        UIManager.put("Label.foreground", currentTheme.getTextColor());
        UIManager.put("Label.background", currentTheme.getBackgroundColor());
        UIManager.put("Label.disabledForeground", currentTheme.getTextColor().darker());

        // BUTTONS
        UIManager.put("Button.background", currentTheme.getPrimaryColor());
        UIManager.put("Button.foreground", Color.WHITE);

        // TEXT FIELDS
        UIManager.put("TextField.background", currentTheme.getForeground());
        UIManager.put("TextField.foreground", currentTheme.getText());
        UIManager.put("TextField.caretForeground", currentTheme.getText());

        // PASSWORD FIELDS
        UIManager.put("PasswordField.background", currentTheme.getForeground());
        UIManager.put("PasswordField.foreground", currentTheme.getText());

        // CHECKBOXES
        UIManager.put("CheckBox.background", currentTheme.getBackgroundColor());
        UIManager.put("CheckBox.foreground", currentTheme.getTextColor());

        // GENERICS
        UIManager.put("text", currentTheme.getTextColor());
        UIManager.put("controlText", currentTheme.getTextColor());

        // VIEWPORT
        UIManager.put("Viewport.background", currentTheme.getBackgroundColor());
        UIManager.put("Viewport.foreground", currentTheme.getTextColor());
    }

    // ============================================================
    // APPLY THEME TO A COMPONENT TREE (optional helper)
    // ============================================================
    public void applyThemeRecursively(JComponent root) {
        if (root == null) return;

        root.setBackground(currentTheme.getBackgroundColor());
        root.setForeground(currentTheme.getTextColor());

        if (root instanceof Container) {
            for (Component child : ((Container) root).getComponents()) {
                if (child instanceof JComponent jc) {
                    applyThemeRecursively(jc);
                }
            }
        }
    }
}
