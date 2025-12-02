package com.swe.ux.views;

import com.swe.canvas.datamodel.manager.ActionManager;
import com.swe.ux.canvas.CanvasController;
import com.swe.ux.integration.JavaFXSwingBridge;
import com.swe.ux.theme.ThemeManager;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Region;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.HierarchyBoundsAdapter;
import java.awt.event.HierarchyEvent;
import java.net.URL;

import com.swe.controller.RPC;
import com.swe.controller.RPCinterface.AbstractRPC;

/**
 * Canvas Page - Embeds canvas FXML using JFXPanel
 * Matches the sizing and layout behavior of ScreenNVideo component
 * Dynamically resizes to fit within panel bounds without scrollbars
 */
public class CanvasPage extends JPanel {

    private static final long serialVersionUID = 1L;

    private final ActionManager actionManager;
    private final String userId;
    private final JFXPanel fxPanel;
    private boolean initialized = false;
    private Scene scene;
    private Parent root;

    private final AbstractRPC rpc;

    public CanvasPage(ActionManager actionManager, String userId, AbstractRPC rpcObj) {
        this.actionManager = actionManager;
        this.userId = userId;
        
        // Match ScreenNVideo layout: BorderLayout with padding
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setOpaque(false);
        setMinimumSize(new Dimension(0, 0));
        setPreferredSize(null);

        // Initialize JavaFX early
        try {
            JavaFXSwingBridge.initializeJavaFX();
        } catch (Exception e) {
            System.err.println("Warning: JavaFX initialization issue: " + e.getMessage());
        }

        // Create JFXPanel - no preferred size, let layout manager handle it
        fxPanel = new JFXPanel();
        fxPanel.setOpaque(false);
        fxPanel.setMinimumSize(new Dimension(0, 0));
        fxPanel.setPreferredSize(new Dimension(0, 0));
        add(fxPanel, BorderLayout.CENTER);

        // Add resize listener to match ScreenNVideo behavior
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateCanvasSize();
            }

            @Override
            public void componentShown(ComponentEvent e) {
                updateCanvasSize();
            }
        });

        // Make sure ancestor size changes (e.g. sidebar drag) also resize the canvas
        addHierarchyBoundsListener(new HierarchyBoundsAdapter() {
            @Override
            public void ancestorResized(HierarchyEvent e) {
                updateCanvasSize();
            }
        });

        // Apply theme
        ThemeManager.getInstance().applyThemeRecursively(this);

        if (rpcObj != null) {
            this.rpc = rpcObj;
        } else {
            this.rpc = RPC.getInstance();
        }
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
            root = loader.load();
            CanvasController controller = loader.getController();

            // Initialize controller
            if (controller != null) {
                controller.initModel(actionManager, this.rpc);
            }

            // Calculate initial size based on available space
            Dimension availableSize = getAvailableSize();
            
            // Create scene with calculated size - matching ScreenNVideo's dynamic sizing
            scene = new Scene(root, availableSize.width, availableSize.height);

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
            fxPanel.setScene(scene);
            
            // Ensure initial sizing is correct after scene is set
            SwingUtilities.invokeLater(() -> {
                updateCanvasSize();
            });

        } catch (Exception e) {
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> showError(e.getMessage()));
        }
    }

    /**
     * Calculate available size for the canvas, matching ScreenNVideo's approach.
     * Accounts for borders, padding, and ensures it fits within panel bounds.
     */
    private Dimension getAvailableSize() {
        Dimension size = getSize();
        if (size.width == 0 || size.height == 0) {
            // Fallback to reasonable defaults if not yet laid out
            Container parent = getParent();
            if (parent != null) {
                size = parent.getSize();
            }
            if (size.width == 0 || size.height == 0) {
                size = new Dimension(800, 600); // Default fallback
            }
        }

        // Account for border and padding (10px on each side from EmptyBorder)
        Insets insets = getInsets();
        int availableWidth = size.width - (insets.left + insets.right);
        int availableHeight = size.height - (insets.top + insets.bottom);

        // Allow shrinking fully when sidebar/chat panels are shown
        availableWidth = Math.max(1, availableWidth);
        availableHeight = Math.max(1, availableHeight);

        return new Dimension(availableWidth, availableHeight);
    }

    /**
     * Update canvas size when panel is resized, matching ScreenNVideo's resize behavior.
     * JFXPanel automatically scales the Scene content to fit its bounds.
     * We just need to ensure the panel revalidates and repaints.
     */
    private void updateCanvasSize() {
        if (!initialized || !isDisplayable()) {
            return;
        }

        final Dimension availableSize = getAvailableSize();
        final int targetWidth = Math.max(1, availableSize.width);
        final int targetHeight = Math.max(1, availableSize.height);

        // JFXPanel automatically scales the Scene to fit its container, but we still
        // push preferred size hints so the surrounding layout matches other stages.
        SwingUtilities.invokeLater(() -> {
            if (fxPanel != null) {
                fxPanel.setPreferredSize(new Dimension(targetWidth, targetHeight));
                fxPanel.setMinimumSize(new Dimension(0, 0));
                fxPanel.setSize(targetWidth, targetHeight);
                revalidate();
                fxPanel.revalidate();
                fxPanel.repaint();
            }
        });

        Platform.runLater(() -> {
            if (scene != null && root instanceof Region) {
                final Region region = (Region) root;
                region.setPrefSize(targetWidth, targetHeight);
                region.setMinSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
                region.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                region.requestLayout();
            }
        });
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
