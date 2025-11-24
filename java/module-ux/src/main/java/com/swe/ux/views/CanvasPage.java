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

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.URL;

/**
 * Canvas Page - Embeds canvas FXML using JFXPanel.
 * Matches the sizing and layout behavior of ScreenNVideo component.
 * Dynamically resizes to fit within panel bounds without scrollbars.
 */
public class CanvasPage extends JPanel {
    private static final long serialVersionUID = 1L;
    /**
     * Default padding value.
     */
    private static final int DEFAULT_PADDING = 10;
    /**
     * Default canvas width.
     */
    private static final int DEFAULT_CANVAS_WIDTH = 800;
    /**
     * Default canvas height.
     */
    private static final int DEFAULT_CANVAS_HEIGHT = 600;
    /**
     * Error font size.
     */
    private static final int ERROR_FONT_SIZE = 12;

    /**
     * Action manager for canvas operations.
     */
    private final ActionManager actionManager;
    /**
     * User ID for canvas operations.
     */
    private final String userId;
    /**
     * JavaFX panel for canvas.
     */
    private final JFXPanel fxPanel;
    /**
     * Whether the canvas has been initialized.
     */
    private boolean initialized = false;
    /**
     * JavaFX scene.
     */
    private Scene scene;
    /**
     * JavaFX root node.
     */
    private Parent root;

    /**
     * Creates a new CanvasPage.
     *
     * @param actionManagerParam the action manager
     * @param userIdParam the user ID
     */
    public CanvasPage(final ActionManager actionManagerParam, final String userIdParam) {
        this.actionManager = actionManagerParam;
        this.userId = userIdParam;
        
        // Match ScreenNVideo layout: BorderLayout with padding
        setLayout(new BorderLayout(DEFAULT_PADDING, DEFAULT_PADDING));
        setBorder(new EmptyBorder(DEFAULT_PADDING, DEFAULT_PADDING, DEFAULT_PADDING, DEFAULT_PADDING));
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
            public void componentResized(final ComponentEvent e) {
                updateCanvasSize();
            }
        });

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

            final FXMLLoader loader = new FXMLLoader(fxmlUrl);
            root = loader.load();
            final CanvasController controller = loader.getController();

            // Initialize controller
            if (controller != null) {
                controller.initModel(actionManager, userId);
            }

            // Calculate initial size based on available space
            final Dimension availableSize = getAvailableSize();
            
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
     *
     * @return the available size for the canvas
     */
    private Dimension getAvailableSize() {
        Dimension size = getSize();
        if (size.width == 0 || size.height == 0) {
            // Fallback to reasonable defaults if not yet laid out
            final Container parent = getParent();
            if (parent != null) {
                size = parent.getSize();
            }
            if (size.width == 0 || size.height == 0) {
                size = new Dimension(DEFAULT_CANVAS_WIDTH, DEFAULT_CANVAS_HEIGHT); // Default fallback
            }
        }

        // Account for border and padding (10px on each side from EmptyBorder)
        final Insets insets = getInsets();
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

        // JFXPanel automatically scales the Scene to fit its container
        // We just need to trigger a revalidation to ensure proper layout
        SwingUtilities.invokeLater(() -> {
            if (fxPanel != null) {
                // Don't set preferred size - let BorderLayout handle sizing
                // JFXPanel will automatically scale the Scene content
                fxPanel.revalidate();
                fxPanel.repaint();
            }
        });
    }

    private void showError(final String message) {
        removeAll();
        final String errorMessage;
        if (message != null) {
            errorMessage = message;
        } else {
            errorMessage = "Unknown error";
        }
        final JLabel errorLabel = new JLabel("<html><center>Failed to load canvas<br/>"
                + errorMessage + "</center></html>");
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        errorLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, ERROR_FONT_SIZE));
        add(errorLabel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    /**
     * Gets the action manager.
     *
     * @return the action manager
     */
    public ActionManager getActionManager() {
        return actionManager;
    }
}
