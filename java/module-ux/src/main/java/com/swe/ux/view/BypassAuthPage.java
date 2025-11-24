package com.swe.ux.view;

import com.swe.ux.theme.Theme;
import com.swe.ux.theme.ThemeManager;
import com.swe.ux.ui.FrostedBackgroundPanel;
import com.swe.ux.ui.PlaceholderTextField;
import com.swe.ux.ui.SoftCardPanel;
import com.swe.ux.ui.FrostedToolbarButton;
import com.swe.ux.ui.ThemeToggleButton;
import com.swe.ux.ui.FontUtil;
import com.swe.ux.viewmodel.BypassAuthViewModel;
import com.swe.ux.binding.PropertyListeners;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Bypass authentication view with email and name input fields.
 */
public class BypassAuthPage extends FrostedBackgroundPanel {
    private boolean uiInitialized = false;

    private final BypassAuthViewModel viewModel;
    private SoftCardPanel glassCard;
    private JLabel titleLabel;
    private JLabel helperLabel;
    private JLabel errorLabel;
    private JLabel dateLabel;
    private PlaceholderTextField emailField;
    private PlaceholderTextField nameField;
    private FrostedToolbarButton submitButton;
    private Timer clockTimer;

    public BypassAuthPage(BypassAuthViewModel viewModel) {
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
        themeButtonWrapper.setBorder(new EmptyBorder(0, 8, 0, 0));
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

        content.add(Box.createVerticalStrut(22));

        // Email input field
        emailField = new PlaceholderTextField("Enter your email");
        emailField.setPreferredSize(new Dimension(360, 48));
        emailField.setMaximumSize(new Dimension(360, 48));
        JPanel emailWrapper = new JPanel(new BorderLayout());
        emailWrapper.setOpaque(false);
        emailWrapper.setBorder(new EmptyBorder(6, 0, 6, 0));
        emailWrapper.add(emailField, BorderLayout.CENTER);
        content.add(emailWrapper);

        content.add(Box.createVerticalStrut(16));

        // Name input field
        nameField = new PlaceholderTextField("Enter your display name");
        nameField.setPreferredSize(new Dimension(360, 48));
        nameField.setMaximumSize(new Dimension(360, 48));
        JPanel nameWrapper = new JPanel(new BorderLayout());
        nameWrapper.setOpaque(false);
        nameWrapper.setBorder(new EmptyBorder(6, 0, 6, 0));
        nameWrapper.add(nameField, BorderLayout.CENTER);
        content.add(nameWrapper);

        content.add(Box.createVerticalStrut(22));

        // Submit button
        submitButton = new FrostedToolbarButton("Enter");
        submitButton.setPreferredSize(new Dimension(360, 52));
        submitButton.setFont(FontUtil.getJetBrainsMono(15f, Font.BOLD));
        submitButton.addActionListener(e -> {
            viewModel.email.set(emailField.getText());
            viewModel.displayName.set(nameField.getText());
            viewModel.submitBypassAuth();
        });
        // Also submit on Enter key in either field
        ActionListener submitAction = e -> {
            viewModel.email.set(emailField.getText());
            viewModel.displayName.set(nameField.getText());
            viewModel.submitBypassAuth();
        };
        emailField.addActionListener(submitAction);
        nameField.addActionListener(submitAction);
        
        JPanel buttonWrapper = new JPanel(new BorderLayout());
        buttonWrapper.setOpaque(false);
        buttonWrapper.setBorder(new EmptyBorder(6, 0, 6, 0));
        buttonWrapper.add(submitButton, BorderLayout.CENTER);
        content.add(buttonWrapper);

        content.add(Box.createVerticalStrut(16));

        // Error label (initially hidden)
        errorLabel = new JLabel("");
        errorLabel.setFont(FontUtil.getJetBrainsMono(12f, Font.PLAIN));
        errorLabel.setBorder(new EmptyBorder(10, 0, 0, 0));
        errorLabel.setVisible(false);
        content.add(errorLabel);

        content.add(Box.createVerticalStrut(8));

        helperLabel = new JLabel("Enter your email and name to continue.");
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
        // Bind error message
        viewModel.errorMessage.addListener(PropertyListeners.onStringChanged(errorMsg -> {
            SwingUtilities.invokeLater(() -> {
                if (errorMsg != null && !errorMsg.isEmpty()) {
                    errorLabel.setText(errorMsg);
                    errorLabel.setVisible(true);
                } else {
                    errorLabel.setVisible(false);
                }
            });
        }));

        // Bind loading state
        viewModel.isLoading.addListener(PropertyListeners.onBooleanChanged(isLoading -> {
            SwingUtilities.invokeLater(() -> {
                submitButton.setEnabled(!isLoading);
                if (isLoading) {
                    submitButton.setText("Loading...");
                } else {
                    submitButton.setText("Enter");
                }
            });
        }));
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
        if (errorLabel != null) {
            // Use a red-tinted color for errors, or fallback to text color
            Color errorColor = new Color(Math.min(255, theme.getTextColor().getRed() + 100), 
                                         Math.max(0, theme.getTextColor().getGreen() - 50), 
                                         Math.max(0, theme.getTextColor().getBlue() - 50));
            errorLabel.setForeground(errorColor);
        }

        SwingUtilities.invokeLater(() -> {
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
            emailField.setText("");
            nameField.setText("");
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

