package com.swe.ux.view;

import com.swe.canvas.datamodel.manager.ActionManager;
import com.swe.ux.canvas.CanvasController;
import com.swe.ux.integration.JavaFXSwingBridge;
import com.swe.ux.theme.ThemeManager;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

/**
 * Canvas Page - Embeds canvas FXML using JFXPanel
 * Matches the pattern used by SentimentInsightsPanel exactly to avoid macOS crashes
 * Fills the exact same space as ScreenNVideo and Analytics tabs
 */
public class CanvasPage extends JPanel {

    private static final long serialVersionUID = 1L;

    private final ActionManager actionManager;
    private final String userId;
    private final JFXPanel fxPanel;
    private boolean initialized = false;

    public CanvasPage(ActionManager actionManager, String userId) {
        this.actionManager = actionManager;
        this.userId = userId;
        setLayout(new BorderLayout());
        setOpaque(false);

        // Initialize JavaFX early
        try {
            JavaFXSwingBridge.initializeJavaFX();
        } catch (Exception e) {
            System.err.println("Warning: JavaFX initialization issue: " + e.getMessage());
        }

        // Create JFXPanel - no preferred size, let layout manager handle it
        fxPanel = new JFXPanel();
        add(fxPanel, BorderLayout.CENTER);

        // Apply theme
        ThemeManager.getInstance().applyThemeRecursively(this);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        // Initialize when component is added to hierarchy, but only once
        if (!initialized) {
            initialized = true;
            Platform.setImplicitExit(false);
            // Initialize immediately - JFXPanel will handle sizing
            Platform.runLater(() -> {
                try {
                    initFX();
                } catch (Exception ex) {
                    System.err.println("Error initializing canvas: " + ex.getMessage());
                    ex.printStackTrace();
                    SwingUtilities.invokeLater(() -> showError(ex.getMessage()));
                }
            });
        }
    }

    private void initFX() {
        try {
            // Load FXML - try multiple classloaders
            URL fxmlUrl = ClassLoader.getSystemClassLoader().getResource("fxml/canvas-view.fxml");
            if (fxmlUrl == null) {
                fxmlUrl = CanvasPage.class.getClassLoader().getResource("fxml/canvas-view.fxml");
            }
            if (fxmlUrl == null) {
                fxmlUrl = Thread.currentThread().getContextClassLoader().getResource("fxml/canvas-view.fxml");
            }

            if (fxmlUrl == null) {
                throw new RuntimeException("FXML resource not found: fxml/canvas-view.fxml");
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            CanvasController controller = loader.getController();

            // Initialize controller
            if (controller != null) {
                controller.initModel(actionManager, userId);
            }

            // Create scene with reasonable default size
            // JFXPanel will automatically resize it when the panel resizes
            // BorderPane will fill the scene automatically
            Scene scene = new Scene(root, 1000, 700);

            // Load CSS
            URL cssUrl = ClassLoader.getSystemClassLoader().getResource("css/canvas-view.css");
            if (cssUrl == null) {
                cssUrl = CanvasPage.class.getClassLoader().getResource("css/canvas-view.css");
            }
            if (cssUrl == null) {
                cssUrl = Thread.currentThread().getContextClassLoader().getResource("css/canvas-view.css");
            }
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            // Set the scene on the JFXPanel
            // JFXPanel will automatically resize the scene when it resizes
            // NO manual resize handling needed - that causes macOS crashes
            fxPanel.setScene(scene);

        } catch (Exception e) {
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> showError(e.getMessage()));
        }
    }

    private void showError(String message) {
        removeAll();
        JLabel errorLabel = new JLabel("<html><center>Failed to load canvas<br/>" +
                (message != null ? message : "Unknown error") + "</center></html>");
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        errorLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        add(errorLabel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    public ActionManager getActionManager() {
        return actionManager;
    }
}
