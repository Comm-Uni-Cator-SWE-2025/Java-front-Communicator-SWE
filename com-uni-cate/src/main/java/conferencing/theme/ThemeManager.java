package com.conferencing.theme;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class ThemeManager {
    private static ThemeManager instance;
    private Theme currentTheme;
    private JFrame mainFrame;

    private ThemeManager() {
        // Default to LightTheme
        currentTheme = new LightTheme();
    }

    public static synchronized ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }
    
    public void setMainFrame(JFrame frame) {
        this.mainFrame = frame;
        applyThemeToUIManager();
    }

    public Theme getTheme() {
        return currentTheme;
    }

    public void toggleTheme() {
        if (currentTheme instanceof LightTheme) {
            currentTheme = new DarkTheme();
        } else {
            currentTheme = new LightTheme();
        }
        applyThemeToUIManager();
        if (mainFrame != null) {
            SwingUtilities.updateComponentTreeUI(mainFrame);
        }
    }

    private void applyThemeToUIManager() {
        try {
            // This provides a better base look for components.
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        UIManager.put("Panel.background", currentTheme.getBackground());
        UIManager.put("Panel.foreground", currentTheme.getText());
        UIManager.put("Label.foreground", currentTheme.getText());
        UIManager.put("Button.background", currentTheme.getPrimary());
        UIManager.put("Button.foreground", currentTheme.getBackground());
        UIManager.put("TextField.background", currentTheme.getForeground());
        UIManager.put("TextField.foreground", currentTheme.getText());
        UIManager.put("TextField.caretForeground", currentTheme.getText());
        UIManager.put("PasswordField.background", currentTheme.getForeground());
        UIManager.put("PasswordField.foreground", currentTheme.getText());
        UIManager.put("PasswordField.caretForeground", currentTheme.getText());
        UIManager.put("CheckBox.background", currentTheme.getBackground());
        UIManager.put("CheckBox.foreground", currentTheme.getText());
        UIManager.put("Viewport.background", currentTheme.getBackground());

        // For JLabels and other components on a transparent panel
        UIManager.put("Label.background", currentTheme.getBackground());
        UIManager.put("Component.opaque", false);
    }
}

