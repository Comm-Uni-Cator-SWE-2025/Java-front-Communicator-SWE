package com.swe.ux.view;

import com.swe.controller.RPCinterface.AbstractRPC;
import com.swe.ux.theme.ThemeManager;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;

import javax.swing.JPanel;
import java.awt.BorderLayout;

/**
 * Swing wrapper that embeds the SentimentViewPane inside the Meeting stage tabs.
 */
public class SentimentInsightsPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final JFXPanel fxPanel;

    public SentimentInsightsPanel(AbstractRPC rpc) {
        setLayout(new BorderLayout());
        setOpaque(false);

        fxPanel = new JFXPanel();
        add(fxPanel, BorderLayout.CENTER);

        Platform.setImplicitExit(false);
        Platform.runLater(() -> {
            SentimentViewPane pane = new SentimentViewPane(rpc);
            Scene scene = new Scene(pane);
            fxPanel.setScene(scene);
        });

        ThemeManager.getInstance().applyThemeRecursively(this);
    }
}
