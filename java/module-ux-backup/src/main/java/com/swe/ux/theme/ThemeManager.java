package com.swe.ux.theme;

import com.swe.ux.App;
import javax.swing.JFrame;
import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

public class ThemeManager {
    private static ThemeManager instance;
    private final Map<String, Theme> themes;
    private Theme currentTheme;
    private JFrame mainFrame;
    private App app;

    private ThemeManager() {
        themes = new HashMap<>();
        themes.put("light", new LightTheme());
        themes.put("dark", new DarkTheme());
        
        // Set default theme
        currentTheme = themes.get("light");
    }

    public static synchronized ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    public void setMainFrame(JFrame frame) {
        this.mainFrame = frame;
    }

    public void setApp(App app) {
        this.app = app;
    }

    public Theme getTheme() {
        return currentTheme;
    }

    public void setTheme(String themeName) {
        Theme newTheme = themes.get(themeName.toLowerCase());
        if (newTheme != null && newTheme != currentTheme) {
            currentTheme = newTheme;
            applyThemeToUI();
        }
    }

    public void toggleTheme() {
        if (currentTheme instanceof LightTheme) {
            setTheme("dark");
        } else {
            setTheme("light");
        }
    }

    private void applyThemeToUI() {
        if (mainFrame != null) {
            mainFrame.getContentPane().setBackground(currentTheme.getBackground());
            mainFrame.repaint();
        }
        
        if (app != null) {
            app.refreshTheme();
        }
    }

    // Helper methods for common theme properties
    public Color getBackground() {
        return currentTheme.getBackground();
    }
    
    public Color getBackgroundColor() {
        return currentTheme.getBackground();
    }

    public Color getTextColor() {
        return currentTheme.getTextColor();
    }

    public Color getAccentColor() {
        return currentTheme.getAccentColor();
    }

    public Color getButtonColor() {
        return currentTheme.getButtonColor();
    }

    public Color getButtonTextColor() {
        return currentTheme.getButtonTextColor();
    }

    public Color getPanelBackground() {
        return currentTheme.getPanelBackground();
    }

    public Color getPanelBorder() {
        return currentTheme.getPanelBorder();
    }

    public Font getTitleFont() {
        return currentTheme.getTitleFont();
    }

    public Font getNormalFont() {
        return currentTheme.getNormalFont();
    }

    public Font getSmallFont() {
        return currentTheme.getSmallFont();
    }
}
