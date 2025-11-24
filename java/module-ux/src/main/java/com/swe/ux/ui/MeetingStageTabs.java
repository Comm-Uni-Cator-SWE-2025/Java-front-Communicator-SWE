package com.swe.ux.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import javax.swing.JPanel;

/**
 * Horizontal meeting stage tab bar that mirrors the WPF toolbar layout.
 */
public class MeetingStageTabs extends JPanel {
    private static final long serialVersionUID = 1L;
    /**
     * Spacing between tabs.
     */
    private static final int TAB_SPACING = 12;
    /**
     * Height of each tab.
     */
    private static final int TAB_HEIGHT = 40;
    /**
     * Border padding around tabs.
     */
    private static final int BORDER_PADDING = 6;

    /**
     * Map of tab buttons.
     */
    private final Map<String, MeetingTabButton> buttons = new LinkedHashMap<>();
    /**
     * Selection listener.
     */
    private Consumer<String> selectionListener;
    /**
     * Currently selected tab key.
     */
    private String selectedKey;

    /**
     * Creates a new meeting stage tabs panel.
     *
     * @param tabs map of tab keys to labels
     * @param onSelection selection listener
     */
    public MeetingStageTabs(final Map<String, String> tabs, final Consumer<String> onSelection) {
        super(new FlowLayout(FlowLayout.LEFT, TAB_SPACING, 0));
        this.selectionListener = onSelection;
        setOpaque(false);
        setBorder(javax.swing.BorderFactory.createEmptyBorder(0, BORDER_PADDING, 0, BORDER_PADDING));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, TAB_HEIGHT));
        tabs.forEach((key, label) -> {
            final MeetingTabButton button = new MeetingTabButton(label);
            button.addActionListener(e -> selectTab(key, true));
            buttons.put(key, button);
            add(button);
        });
    }

    /**
     * Sets the selected tab.
     *
     * @param key the tab key
     */
    public void setSelectedTab(final String key) {
        selectTab(key, false);
    }

    private void selectTab(final String key, final boolean userTriggered) {
        if (key == null || Objects.equals(selectedKey, key)) {
            return;
        }
        selectedKey = key;
        buttons.forEach((tabKey, btn) -> btn.setSelectedTab(tabKey.equals(key)));
        if (userTriggered && selectionListener != null) {
            selectionListener.accept(key);
        }
    }

    /**
     * Sets the accent color for all tabs.
     *
     * @param color the accent color
     */
    public void setAccentColor(final Color color) {
        if (color == null) {
            return;
        }
        buttons.values().forEach(btn -> btn.setAccentColor(color));
    }
}
