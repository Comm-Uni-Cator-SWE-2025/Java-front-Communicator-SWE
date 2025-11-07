package com.swe.ux.view;

import com.swe.ux.theme.Theme;
import com.swe.ux.theme.ThemeManager;
import com.swe.ux.ui.CustomButton;
import com.swe.ux.viewmodel.LoginViewModel;

import javax.swing.*;
import java.awt.*;

/**
 * Login view that displays a Google login button.
 */
public class LoginPage extends JPanel {
    private final LoginViewModel viewModel;
    private JLabel titleLabel;
    private JPanel formPanel;
    private JButton googleLoginButton;

    /**
     * Creates a new LoginPage with the specified ViewModel.
     * @param viewModel The ViewModel to use for this view
     */
    public LoginPage(LoginViewModel viewModel) {
        this.viewModel = viewModel;
        initializeUI();
        setupBindings();
        applyTheme();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(0, 20));
        setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        // Title
        titleLabel = new JLabel("Welcome Back", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(titleLabel, BorderLayout.NORTH);

        // Form Panel
        formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 8, 20, 8);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 1.0;

        // Google Login Button
        gbc.gridx = 0;
        gbc.gridy = 0;
        googleLoginButton = new CustomButton("Login with Google", true);
        googleLoginButton.setPreferredSize(new Dimension(250, 50));
        googleLoginButton.addActionListener(e -> {
            // Bypass login and go directly to main view
            viewModel.loginWithGoogle();
        });
        formPanel.add(googleLoginButton, gbc);

        add(formPanel, BorderLayout.CENTER);
    }

    private void setupBindings() {
        // No bindings needed for Google login bypass
    }

    private void applyTheme() {
        // Apply theme colors
        ThemeManager themeManager = ThemeManager.getInstance();
        Theme theme = themeManager.getCurrentTheme();
        themeManager.applyTheme(this);
        
        Color bg = theme.getBackgroundColor();
        Color fg = themeManager.getCurrentTheme().getTextColor();
        
        setBackground(bg);
        formPanel.setBackground(bg);
        titleLabel.setForeground(fg);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            // Reset form when made visible
            viewModel.reset();
        }
    }
}
