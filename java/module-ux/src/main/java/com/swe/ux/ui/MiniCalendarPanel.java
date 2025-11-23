
/*
 * -----------------------------------------------------------------------------
 *  File: MiniCalendarPanel.java
 *  Owner: Aryan Mathur
 *  Roll Number : 122201017
 *  Module : UX
 *
 * -----------------------------------------------------------------------------
 */
package com.swe.ux.ui;

import com.swe.ux.theme.Theme;
import com.swe.ux.theme.ThemeManager;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * Compact current-month calendar widget.
 */
public class MiniCalendarPanel extends JPanel {

    private final JLabel headerLabel;
    private final JPanel gridPanel;
    private LocalDate currentMonth;

    public MiniCalendarPanel() {
        setOpaque(false);
        setLayout(new BorderLayout(0, 6));
        currentMonth = LocalDate.now().withDayOfMonth(1);

        headerLabel = new JLabel("", SwingConstants.CENTER);
        headerLabel.setFont(FontUtil.getJetBrainsMono(16f, Font.BOLD));

        gridPanel = new JPanel(new GridLayout(0, 7, 4, 4));
        gridPanel.setOpaque(false);

        add(headerLabel, BorderLayout.NORTH);
        add(gridPanel, BorderLayout.CENTER);

        refreshCalendar();

        Timer midnightTimer = new Timer(60 * 60 * 1000, e -> refreshCalendar());
        midnightTimer.setRepeats(true);
        midnightTimer.start();
    }

    private void refreshCalendar() {
        LocalDate now = LocalDate.now();
        if (!now.withDayOfMonth(1).equals(currentMonth)) {
            currentMonth = now.withDayOfMonth(1);
        }

        Theme theme = ThemeManager.getInstance().getCurrentTheme();
        Color textColor = theme != null ? theme.getTextColor() : Color.WHITE;
        Color accentColor = theme != null ? theme.getPrimaryColor() : new Color(90, 160, 255);

        headerLabel.setForeground(textColor);
        headerLabel.setText(currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault())
                + " " + currentMonth.getYear());

        gridPanel.removeAll();

        Font dayFont = FontUtil.getJetBrainsMono(12f, Font.BOLD);
        for (DayOfWeek dow : DayOfWeek.values()) {
            JLabel dayLabel = createCell(dow.getDisplayName(TextStyle.SHORT, Locale.getDefault()).substring(0, 2).toUpperCase(),
                    dayFont, accentColor, SwingConstants.CENTER);
            gridPanel.add(dayLabel);
        }

        int firstDayOffset = currentMonth.getDayOfWeek().getValue() % 7; // make Sunday =0
        for (int i = 0; i < firstDayOffset; i++) {
            gridPanel.add(createCell("", FontUtil.getJetBrainsMono(12f, Font.PLAIN), textColor, SwingConstants.CENTER));
        }

        Font dateFont = FontUtil.getJetBrainsMono(13f, Font.PLAIN);
        int length = currentMonth.lengthOfMonth();
        LocalDate today = LocalDate.now();

        for (int day = 1; day <= length; day++) {
            LocalDate date = currentMonth.withDayOfMonth(day);
            boolean isToday = date.equals(today);
            JLabel cell = createCell(String.valueOf(day), dateFont,
                    isToday ? Color.BLACK : textColor, SwingConstants.CENTER);
            if (isToday) {
                cell.setOpaque(true);
                cell.setBackground(accentColor);
            }
            gridPanel.add(cell);
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JLabel createCell(String text, Font font, Color color, int align) {
        JLabel label = new JLabel(text, align);
        label.setFont(font);
        label.setForeground(color);
        return label;
    }
}
