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

    private final Map<String, MeetingTabButton> buttons = new LinkedHashMap<>();
    private Consumer<String> selectionListener;
    private String selectedKey;

    public MeetingStageTabs(Map<String, String> tabs, Consumer<String> onSelection) {
        super(new FlowLayout(FlowLayout.LEFT, 12, 0));
        this.selectionListener = onSelection;
        setOpaque(false);
        setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 6, 0, 6));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        tabs.forEach((key, label) -> {
            MeetingTabButton button = new MeetingTabButton(label);
            button.addActionListener(e -> selectTab(key, true));
            buttons.put(key, button);
            add(button);
        });
    }

    public void setSelectedTab(String key) {
        selectTab(key, false);
    }

    private void selectTab(String key, boolean userTriggered) {
        if (key == null || Objects.equals(selectedKey, key)) {
            return;
        }
        selectedKey = key;
        buttons.forEach((tabKey, btn) -> btn.setSelectedTab(tabKey.equals(key)));
        if (userTriggered && selectionListener != null) {
            selectionListener.accept(key);
        }
    }

    public void setAccentColor(Color color) {
        if (color == null) {
            return;
        }
        buttons.values().forEach(btn -> btn.setAccentColor(color));
    }
}
