package com.swe.ux.view;

import com.swe.ux.theme.Theme;
import com.swe.ux.theme.ThemeManager;
import com.swe.ux.ui.FrostedBackgroundPanel;
import com.swe.ux.ui.FrostedBadgeLabel;
import com.swe.ux.ui.SoftCardPanel;
import com.swe.ux.ui.FrostedToolbarButton;
import com.swe.ux.ui.ThemeToggleButton;
import com.swe.ux.ui.FontUtil;
import com.swe.ux.viewmodel.LoginViewModel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Login view reimagined with a floating frosted-glass card inspired by macOS.
 */
public class LoginPage extends FrostedBackgroundPanel {
    private boolean uiInitialized = false;

    private final LoginViewModel viewModel;
    private SoftCardPanel glassCard;
    private JLabel titleLabel;
    private JLabel helperLabel;
    private JLabel dateLabel;
    private JPanel badgesPanel;
    private FrostedToolbarButton googleLoginButton;
    private Timer clockTimer;

    public LoginPage(LoginViewModel viewModel) {
        this.viewModel = viewModel;
        initializeUI();
        uiInitialized = true;
        applyTheme();

        setupBindings();
        startClock();
        applyTheme();
    }

    private void initializeUI() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));

        glassCard = new SoftCardPanel();
        glassCard.setPreferredSize(new Dimension(560, 520));
        glassCard.setLayout(new BorderLayout());
        glassCard.setOpaque(false);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(20, 28, 28, 28));

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        dateLabel = new JLabel(formatDate(new Date()));
        dateLabel.setFont(FontUtil.getJetBrainsMono(12f, Font.PLAIN));
        headerRow.add(dateLabel, BorderLayout.WEST);
        
        // Wrap theme toggle button in a panel with proper spacing
        JPanel themeButtonWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        themeButtonWrapper.setOpaque(false);
        themeButtonWrapper.setBorder(new EmptyBorder(0, 8, 0, 0)); // Add left padding to separate from edge
        ThemeToggleButton themeButton = new ThemeToggleButton();
        themeButtonWrapper.add(themeButton);
        headerRow.add(themeButtonWrapper, BorderLayout.EAST);
        
        content.add(headerRow);

        content.add(Box.createVerticalStrut(18));

        JPanel titleWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titleWrapper.setOpaque(false);
        titleLabel = new JLabel("Comm-Uni-Cate");
        titleLabel.setFont(FontUtil.getJetBrainsMono(32f, Font.BOLD));
        titleWrapper.add(titleLabel);
        titleWrapper.setBorder(new EmptyBorder(0, 0, 10, 0));
        content.add(titleWrapper);

        badgesPanel = new JPanel();
        badgesPanel.setOpaque(false);
        badgesPanel.setLayout(new GridBagLayout());
        GridBagConstraints badgeGbc = new GridBagConstraints();
        badgeGbc.gridx = 0;
        badgeGbc.gridy = 0;
        badgeGbc.insets = new Insets(0, 0, 8, 10);
        badgeGbc.anchor = GridBagConstraints.WEST;
        List<String> badges = Arrays.asList("IIT PKD", "Secure Google Auth", "HD Ready");
        for (String badge : badges) {
            FrostedBadgeLabel badgeLabel = createBadgeLabel(badge);
            badgesPanel.add(badgeLabel, badgeGbc);
            badgeGbc.gridx++;
        }
        content.add(badgesPanel);

        content.add(Box.createVerticalStrut(22));

        googleLoginButton = new FrostedToolbarButton("Continue with Google");
        googleLoginButton.setPreferredSize(new Dimension(360, 52));
        googleLoginButton.setFont(FontUtil.getJetBrainsMono(15f, Font.BOLD));
        googleLoginButton.addActionListener(e -> viewModel.loginWithGoogle());
        JPanel buttonWrapper = new JPanel(new BorderLayout());
        buttonWrapper.setOpaque(false);
        buttonWrapper.setBorder(new EmptyBorder(6, 0, 6, 0));
        buttonWrapper.add(googleLoginButton, BorderLayout.CENTER);
        content.add(buttonWrapper);

        content.add(Box.createVerticalStrut(22));

        helperLabel = new JLabel("Use your institute Google account to sign in.");
        helperLabel.setFont(FontUtil.getJetBrainsMono(12f, Font.PLAIN));
        helperLabel.setBorder(new EmptyBorder(10, 0, 0, 0));
        content.add(helperLabel);

        glassCard.add(content, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        add(glassCard, gbc);
    }

    private void setupBindings() {
        // No data binding required at present
    }

    private FrostedBadgeLabel createBadgeLabel(String text) {
        FrostedBadgeLabel badge = new FrostedBadgeLabel(text);
        badge.setPreferredSize(new Dimension(badge.getPreferredSize().width + 20, 32));
        return badge;
    }

    private void startClock() {
        if (clockTimer != null) {
            clockTimer.stop();
        }
        clockTimer = new Timer(30_000, e -> dateLabel.setText(formatDate(new Date())));
        clockTimer.setInitialDelay(0);
        clockTimer.start();
    }

    private String formatDate(Date date) {
        return new SimpleDateFormat("EEE, MMM d • hh:mm a").format(date);
    }

    private void applyTheme() {
        ThemeManager themeManager = ThemeManager.getInstance();
        if (themeManager == null) return;
        Theme theme = themeManager.getCurrentTheme();
        if (theme == null) return;

        setBackground(theme.getBackgroundColor());
        titleLabel.setForeground(theme.getTextColor());
        helperLabel.setForeground(theme.getTextColor().darker());
        dateLabel.setForeground(theme.getTextColor());

        SwingUtilities.invokeLater(() -> {
            if (badgesPanel != null) {
                badgesPanel.revalidate();
                badgesPanel.repaint();
            }
            if (glassCard != null) {
                glassCard.repaint();
            }
        });
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            viewModel.reset();
            applyTheme();
            startClock();
        } else if (clockTimer != null) {
            clockTimer.stop();
        }
    }

    @Override
    public void updateUI() {
        super.updateUI();
        if (uiInitialized) {
            applyTheme();
        }
    }

}
