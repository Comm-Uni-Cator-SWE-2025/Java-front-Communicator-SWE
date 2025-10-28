package com.swe.ux.view;

import com.swe.ux.theme.Theme;
import com.swe.ux.theme.ThemeManager;
import com.swe.ux.ui.CustomButton;
import com.swe.ux.ui.PlaceholderTextField;
import com.swe.ux.viewmodel.LoginViewModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Login view that displays a login form and handles user authentication.
 */
public class LoginPage extends JPanel {
    private final LoginViewModel viewModel;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JCheckBox rememberMeCheckbox;
    private JLabel errorLabel;
    private JProgressBar loadingIndicator;
    private JLabel titleLabel;
    private JPanel formPanel;
    private JButton registerLink;
    private JButton loginButton;

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
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Username
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("Username"), gbc);

        gbc.gridy = 1;
        usernameField = new PlaceholderTextField("Enter your username");
        usernameField.setPreferredSize(new Dimension(300, 35));
        formPanel.add(usernameField, gbc);

        // Password
        gbc.gridy = 2;
        gbc.insets = new Insets(20, 8, 8, 8); // Add more space above password
        formPanel.add(new JLabel("Password"), gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(0, 8, 20, 8); // Add more space below password
        passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(300, 35));
        formPanel.add(passwordField, gbc);

        // Login Button
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        loginButton = new CustomButton("Sign In", true);
        loginButton.setPreferredSize(new Dimension(200, 40));
        formPanel.add(loginButton, gbc);

        // Register Link
        gbc.gridy = 5;
        gbc.insets = new Insets(10, 8, 20, 8);
        JPanel registerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        registerPanel.setOpaque(false);
        
        JLabel registerLabel = new JLabel("Don't have an account?");
        registerLink = new JButton("Create Account");
        registerLink.setBorderPainted(false);
        registerLink.setContentAreaFilled(false);
        registerLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerLink.addActionListener(e -> viewModel.showRegisterRequested.set(true));
        
        registerPanel.add(registerLabel);
        registerPanel.add(registerLink);
        formPanel.add(registerPanel, gbc);

        // Error Label
        errorLabel = new JLabel(" ", JLabel.CENTER);
        errorLabel.setForeground(Color.RED);
        errorLabel.setPreferredSize(new Dimension(300, 20));
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 8, 10, 8);
        formPanel.add(errorLabel, gbc);

        // Loading Indicator
        loadingIndicator = new JProgressBar();
        loadingIndicator.setIndeterminate(true);
        loadingIndicator.setVisible(false);
        gbc.gridy = 7;
        formPanel.add(loadingIndicator, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Handle Enter key press in password field
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    loginButton.doClick();
                }
            }
        });

        // Handle login button click
        loginButton.addActionListener(this::handleLogin);
    }

    private void setupBindings() {
        // ViewModel -> View bindings
        viewModel.errorMessage.addListener(evt -> 
            SwingUtilities.invokeLater(() -> 
                errorLabel.setText(viewModel.errorMessage.get())
            )
        );

        viewModel.isLoading.addListener(evt -> 
            SwingUtilities.invokeLater(() -> {
                boolean isLoading = viewModel.isLoading.get();
                loadingIndicator.setVisible(isLoading);
                loginButton.setEnabled(!isLoading);
                usernameField.setEnabled(!isLoading);
                passwordField.setEnabled(!isLoading);
            })
        );

        viewModel.loginSuccess.addListener(evt -> {
            if (viewModel.loginSuccess.get()) {
                // Clear sensitive data
                passwordField.setText("");
                // Could navigate to main app here
            }
        });
    }

    private void applyTheme() {
        // Apply theme colors
        ThemeManager themeManager = ThemeManager.getInstance();
        Theme theme = themeManager.getCurrentTheme();
        themeManager.applyTheme(this);
        
        // Apply theme to components
        if (registerLink != null) {
            registerLink.setForeground(theme.getPrimary());
            registerLink.setFont(registerLink.getFont().deriveFont(Font.BOLD));
        }
        
        Color bg = theme.getBackgroundColor();
        Color fg = themeManager.getCurrentTheme().getTextColor();
        
        setBackground(bg);
        formPanel.setBackground(bg);
        titleLabel.setForeground(fg);
        
        // Style input fields
        Color fieldBg = themeManager.getCurrentTheme().getInputBackgroundColor();
        Color fieldFg = themeManager.getCurrentTheme().getTextColor();
        
        usernameField.setBackground(fieldBg);
        usernameField.setForeground(fieldFg);
        passwordField.setBackground(fieldBg);
        passwordField.setForeground(fieldFg);
    }

    private void handleLogin(ActionEvent e) {
        // Update ViewModel with current field values
        viewModel.username.set(usernameField.getText().trim());
        viewModel.password.set(new String(passwordField.getPassword()));
        
        // Trigger login
        viewModel.login();
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            // Reset form when made visible
            viewModel.reset();
            usernameField.setText("");
            passwordField.setText("");
            errorLabel.setText(" ");
            usernameField.requestFocusInWindow();
        }
    }
}
