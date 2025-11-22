package com.swe.ux.view;

import com.swe.ux.theme.ThemeManager;
import com.swe.ux.viewmodel.CanvasViewModel;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javax.swing.*;
import java.awt.*;

/**
 * Canvas Page - Directly embeds canvas from module-canvas FXML
 */
public class CanvasPage extends JPanel {

    private final CanvasViewModel viewModel;
    private JFXPanel fxPanel;

    public CanvasPage(CanvasViewModel viewModel) {
        this.viewModel = viewModel;
        setLayout(new BorderLayout());
        setBackground(new java.awt.Color(245, 247, 250));
        
        // Create JFXPanel
        fxPanel = new JFXPanel();
        add(fxPanel, BorderLayout.CENTER);
        
        // Initialize JavaFX content
        Platform.setImplicitExit(false);
        Platform.runLater(this::initFX);
        
        applyTheme();
    }

    private void initFX() {
        try {
            // Load the original canvas FXML directly
            FXMLLoader loader = new FXMLLoader(
                getClass().getClassLoader().getResource("com/swe/canvas/fxml/canvas-view.fxml")
            );
            Parent root = loader.load();

            // Create scene with the loaded FXML
            Scene scene = new Scene(root);

            // Load CSS
            String cssUrl = getClass().getClassLoader()
                .getResource("com/swe/canvas/fxml/canvas-view.css").toExternalForm();
            scene.getStylesheets().add(cssUrl);

            // Set the scene
            fxPanel.setScene(scene);

        } catch (Exception e) {
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> {
                removeAll();
                JLabel errorLabel = new JLabel("Failed to load canvas: " + e.getMessage());
                errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
                add(errorLabel, BorderLayout.CENTER);
                revalidate();
                repaint();
            });
        }
    }

    private void applyTheme() {
        ThemeManager.getInstance().applyThemeRecursively(this);
    }

    public CanvasViewModel getViewModel() {
        return viewModel;
    }
}
