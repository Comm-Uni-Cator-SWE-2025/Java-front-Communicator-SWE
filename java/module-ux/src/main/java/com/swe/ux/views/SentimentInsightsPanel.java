package com.swe.ux.views;

import com.swe.ux.theme.ThemeManager;
import com.swe.ux.viewmodels.MeetingViewModel;

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

    /**
     * JavaFX panel for sentiment insights.
     */
    private final JFXPanel fxPanel;

    public SentimentInsightsPanel(final MeetingViewModel meetingViewModel) {
        setLayout(new BorderLayout());
        setOpaque(false);

        fxPanel = new JFXPanel();
        add(fxPanel, BorderLayout.CENTER);

        Platform.setImplicitExit(false);
        Platform.runLater(() -> {
            final SentimentViewPane pane = new SentimentViewPane(meetingViewModel);
            final Scene scene = new Scene(pane);
            fxPanel.setScene(scene);
        });

        ThemeManager.getInstance().applyThemeRecursively(this);
    }
}
