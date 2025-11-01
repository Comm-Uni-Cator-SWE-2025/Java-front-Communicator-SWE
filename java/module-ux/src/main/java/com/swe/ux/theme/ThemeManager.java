package com.swe.ux.theme;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.swe.ux.App;

public class ThemeManager {
    private static ThemeManager instance;
    private Theme currentTheme;
    private JFrame mainFrame;
    private App app;

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
    
    public void setApp(App app) {
        this.app = app;
    }

    public Theme getTheme() {
        return currentTheme;
    }
    
    public Theme getCurrentTheme() {
        return currentTheme;
    }
    
    public void applyTheme(JComponent component) {
        // Apply theme to the component
        component.setBackground(currentTheme.getBackgroundColor());
        component.setForeground(currentTheme.getTextColor());
        
        // If it's a container, apply to all children
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                if (child instanceof JComponent) {
                    applyTheme((JComponent) child);
                }
            }
        }
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
            mainFrame.repaint();
        }
        if (app != null) {
            app.refreshTheme();
        }
    }

    private void applyThemeToUIManager() {
        try {
            // This provides a better base look for components.
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Panel colors
        UIManager.put("Panel.background", currentTheme.getBackground());
        UIManager.put("Panel.foreground", currentTheme.getText());
        
        // Label colors - ensure text is visible
        UIManager.put("Label.foreground", currentTheme.getText());
        UIManager.put("Label.background", currentTheme.getBackground());
        UIManager.put("Label.disabledForeground", currentTheme.getText().darker());
        
        // Button colors
        UIManager.put("Button.background", currentTheme.getPrimary());
        UIManager.put("Button.foreground", Color.WHITE);
        
        // Text field colors
        UIManager.put("TextField.background", currentTheme.getForeground());
        UIManager.put("TextField.foreground", currentTheme.getText());
        UIManager.put("TextField.caretForeground", currentTheme.getText());
        UIManager.put("TextField.inactiveForeground", currentTheme.getText());
        
        // Password field colors
        UIManager.put("PasswordField.background", currentTheme.getForeground());
        UIManager.put("PasswordField.foreground", currentTheme.getText());
        UIManager.put("PasswordField.caretForeground", currentTheme.getText());
        UIManager.put("PasswordField.inactiveForeground", currentTheme.getText());
        
        // Checkbox colors
        UIManager.put("CheckBox.background", currentTheme.getBackground());
        UIManager.put("CheckBox.foreground", currentTheme.getText());
        
        // General text color
        UIManager.put("text", currentTheme.getText());
        UIManager.put("textText", currentTheme.getText());
        UIManager.put("controlText", currentTheme.getText());
        
        // Viewport colors
        UIManager.put("Viewport.background", currentTheme.getBackground());
        UIManager.put("Viewport.foreground", currentTheme.getText());
    }
}

