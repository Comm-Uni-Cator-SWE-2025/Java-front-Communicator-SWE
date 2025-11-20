package com.swe.ux.view;

import com.swe.networking.NetworkFront;
import com.swe.ux.service.CanvasController;
import com.swe.ux.theme.ThemeManager;
import com.swe.ux.viewmodel.CanvasViewModel;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

import javax.swing.*;
import java.awt.*;

/**
 * Canvas Page - FXML + Swing Wrapper, now identical behavior to CanvasApp.java
 */
public class CanvasPage extends JPanel {

    private CanvasViewModel viewModel;
    private JFXPanel fxPanel;

    public CanvasPage(CanvasViewModel viewModel) {
        this.viewModel = viewModel;

        setLayout(new BorderLayout());
        setBackground(new java.awt.Color(245, 247, 250));

        fxPanel = new JFXPanel();
        add(fxPanel, BorderLayout.CENTER);

        Platform.setImplicitExit(false);
        Platform.runLater(this::initFX);

        applyTheme();
    }

    private void initFX() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/canvas-view.fxml")
            );

            Parent root = loader.load();

            // --- IMPORTANT: Retrieve the controller (same as CanvasApp)
            CanvasController controller = loader.getController();

            if (NetworkFront.getInstance().isReady()) {
                controller.setNetwork(NetworkFront.getInstance());
            }


            // If your controller exposes a viewModel setter, sync it here:
            // controller.setViewModel(this.viewModel);

            Scene scene = new Scene(root);

            // --- Add accelerators (same as CanvasApp)
            scene.getAccelerators().put(
                    new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN),
                    () -> controller.getViewModel().undo()
            );
            scene.getAccelerators().put(
                    new KeyCodeCombination(KeyCode.Y, KeyCombination.SHORTCUT_DOWN),
                    () -> controller.getViewModel().redo()
            );
            scene.getAccelerators().put(
                    new KeyCodeCombination(KeyCode.X, KeyCombination.SHORTCUT_DOWN),
                    () -> controller.getViewModel().undo()
            );

            // CSS
            String cssUrl = getClass().getClassLoader()
                    .getResource("/fxml/canvas-view.css").toExternalForm();
            scene.getStylesheets().add(cssUrl);

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
