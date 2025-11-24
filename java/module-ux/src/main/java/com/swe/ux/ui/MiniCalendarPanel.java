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
    /**
     * Font size for header text.
     */
    private static final float HEADER_FONT_SIZE = 16f;
    /**
     * Font size for day names.
     */
    private static final float DAY_NAME_FONT_SIZE = 12f;
    /**
     * Font size for date numbers.
     */
    private static final float DATE_FONT_SIZE = 13f;
    /**
     * Spacing between grid cells.
     */
    private static final int GRID_SPACING = 4;
    /**
     * Border spacing around calendar.
     */
    private static final int BORDER_SPACING = 6;
    /**
     * Number of columns in calendar grid (days of week).
     */
    private static final int GRID_COLUMNS = 7;
    /**
     * First day of month.
     */
    private static final int DAY_OF_MONTH_START = 1;
    /**
     * Number of days in a week.
     */
    private static final int DAYS_IN_WEEK = 7;
    /**
     * Number of characters for day name abbreviation.
     */
    private static final int DAY_NAME_CHARS = 2;
    /**
     * Default color red component.
     */
    private static final int DEFAULT_COLOR_R = 90;
    /**
     * Default color green component.
     */
    private static final int DEFAULT_COLOR_G = 160;
    /**
     * Default color blue component.
     */
    private static final int DEFAULT_COLOR_B = 255;
    /**
     * Milliseconds in one hour.
     */
    private static final int HOUR_IN_MILLIS = 60 * 60 * 1000;

    /**
     * Header label showing month and year.
     */
    private final JLabel headerLabel;
    /**
     * Grid panel containing calendar cells.
     */
    private final JPanel gridPanel;
    /**
     * Current month being displayed.
     */
    private LocalDate currentMonth;

    /**
     * Creates a new mini calendar panel.
     */
    public MiniCalendarPanel() {
        setOpaque(false);
        setLayout(new BorderLayout(0, BORDER_SPACING));
        currentMonth = LocalDate.now().withDayOfMonth(DAY_OF_MONTH_START);

        headerLabel = new JLabel("", SwingConstants.CENTER);
        headerLabel.setFont(FontUtil.getJetBrainsMono(HEADER_FONT_SIZE, Font.BOLD));

        gridPanel = new JPanel(new GridLayout(0, GRID_COLUMNS, GRID_SPACING, GRID_SPACING));
        gridPanel.setOpaque(false);

        add(headerLabel, BorderLayout.NORTH);
        add(gridPanel, BorderLayout.CENTER);

        refreshCalendar();

        final Timer midnightTimer = new Timer(HOUR_IN_MILLIS, e -> refreshCalendar());
        midnightTimer.setRepeats(true);
        midnightTimer.start();
    }

    private void refreshCalendar() {
        final LocalDate now = LocalDate.now();
        if (!now.withDayOfMonth(DAY_OF_MONTH_START).equals(currentMonth)) {
            currentMonth = now.withDayOfMonth(DAY_OF_MONTH_START);
        }

        final Theme theme = ThemeManager.getInstance().getCurrentTheme();
        final Color textColor;
        if (theme != null) {
            textColor = theme.getTextColor();
        } else {
            textColor = Color.WHITE;
        }
        final Color accentColor;
        if (theme != null) {
            accentColor = theme.getPrimaryColor();
        } else {
            accentColor = new Color(DEFAULT_COLOR_R, DEFAULT_COLOR_G, DEFAULT_COLOR_B);
        }

        headerLabel.setForeground(textColor);
        headerLabel.setText(currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault())
                + " " + currentMonth.getYear());

        gridPanel.removeAll();

        final Font dayFont = FontUtil.getJetBrainsMono(DAY_NAME_FONT_SIZE, Font.BOLD);
        for (final DayOfWeek dow : DayOfWeek.values()) {
            final JLabel dayLabel = createCell(
                    dow.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                            .substring(0, DAY_NAME_CHARS).toUpperCase(),
                    dayFont, accentColor, SwingConstants.CENTER);
            gridPanel.add(dayLabel);
        }

        final int firstDayOffset = currentMonth.getDayOfWeek().getValue() % DAYS_IN_WEEK;
        for (int i = 0; i < firstDayOffset; i++) {
            gridPanel.add(createCell("",
                    FontUtil.getJetBrainsMono(DAY_NAME_FONT_SIZE, Font.PLAIN),
                    textColor, SwingConstants.CENTER));
        }

        final Font dateFont = FontUtil.getJetBrainsMono(DATE_FONT_SIZE, Font.PLAIN);
        final int length = currentMonth.lengthOfMonth();
        final LocalDate today = LocalDate.now();

        for (int day = 1; day <= length; day++) {
            final LocalDate date = currentMonth.withDayOfMonth(day);
            final boolean isToday = date.equals(today);
            final JLabel cell = createCell(String.valueOf(day), dateFont,
                    getDayTextColor(isToday, textColor), SwingConstants.CENTER);
            if (isToday) {
                cell.setOpaque(true);
                cell.setBackground(accentColor);
            }
            gridPanel.add(cell);
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    /**
     * Gets the text color for a day cell.
     *
     * @param isToday whether the day is today
     * @param defaultColor default text color
     * @return the text color to use
     */
    private Color getDayTextColor(final boolean isToday, final Color defaultColor) {
        if (isToday) {
            return Color.BLACK;
        }
        return defaultColor;
    }

    private JLabel createCell(final String text, final Font font,
                               final Color color, final int align) {
        final JLabel label = new JLabel(text, align);
        label.setFont(font);
        label.setForeground(color);
        return label;
    }
}
